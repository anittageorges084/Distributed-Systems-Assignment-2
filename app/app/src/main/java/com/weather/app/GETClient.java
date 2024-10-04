package com.weather.app;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GETClient {

    private static final LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server_address>:<port>");
            return;
        }

        String[] addressParts = args[0].split(":");
        String serverAddress = addressParts[0];
        int portNumber = Integer.parseInt(addressParts[1]);

        try (Socket socket = new Socket(serverAddress, portNumber)) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            lamportClock.tick();

            // Send HTTP GET request
            out.println("GET / HTTP/1.1");
            out.println("Host: " + serverAddress);
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println(); // Empty line indicates end of headers

            // Read response
            String responseLine;
            int serverLamportClock = lamportClock.getClock();
            boolean inBody = false;
            StringBuilder responseBody = new StringBuilder();

            while ((responseLine = in.readLine()) != null) {
                if (responseLine.isEmpty()) {
                    inBody = true;
                    continue;
                }

                if (inBody) {
                    responseBody.append(responseLine);
                } else {
                    // Read Lamport-Clock from headers
                    if (responseLine.startsWith("Lamport-Clock:")) {
                        serverLamportClock = Integer.parseInt(responseLine.substring(14).trim());
                    }
                }
            }

            // Update Lamport clock
            lamportClock.update(serverLamportClock);

            // Parse and display data
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray jsonArray = JsonParser.parseString(responseBody.toString()).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject weatherData = element.getAsJsonObject();
                System.out.println("Weather Data:");
                for (Map.Entry<String, JsonElement> entry : weatherData.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue().getAsString());
                }
                System.out.println("-----------------------------");
            }

        } catch (IOException e) {
            System.err.println("Failed to connect to server.");
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse JSON response.");
            e.printStackTrace();
        }
    }

    // LamportClock class
    private static class LamportClock {
        private int clock = 0;

        public synchronized void tick() {
            clock++;
        }

        public synchronized void update(int receivedClock) {
            clock = Math.max(clock, receivedClock) + 1;
        }

        public synchronized int getClock() {
            return clock;
        }
    }
}
