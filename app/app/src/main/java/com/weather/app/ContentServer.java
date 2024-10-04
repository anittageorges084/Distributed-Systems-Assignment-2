package com.weather.app;

import com.google.gson.*;
import java.io.*;
import java.net.*;

public class ContentServer {

    private static final LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server_address>:<port> <file_path>");
            return;
        }

        String[] addressParts = args[0].split(":");
        String serverAddress = addressParts[0];
        int portNumber = Integer.parseInt(addressParts[1]);
        String filePath = args[1];

        // Read weather data from file
        JsonObject jsonData = parseFileToJson(filePath);
        if (jsonData == null) {
            System.err.println("Failed to parse input file.");
            return;
        }

        try (Socket socket = new Socket(serverAddress, portNumber)) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            lamportClock.tick();

            String jsonString = new Gson().toJson(jsonData);

            // Send HTTP PUT request
            out.println("PUT / HTTP/1.1");
            out.println("Host: " + serverAddress);
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonString.length());
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println(); // Empty line indicates end of headers
            out.print(jsonString);
            out.flush();

            // Read response
            String responseLine;
            int serverLamportClock = lamportClock.getClock();
            while ((responseLine = in.readLine()) != null) {
                if (responseLine.startsWith("Lamport-Clock:")) {
                    serverLamportClock = Integer.parseInt(responseLine.substring(14).trim());
                }
                if (responseLine.isEmpty()) {
                    break;
                }
            }

            // Update Lamport clock
            lamportClock.update(serverLamportClock);

            System.out.println("Data sent successfully. Lamport Clock: " + lamportClock.getClock());

        } catch (IOException e) {
            System.err.println("Failed to connect to server.");
            e.printStackTrace();
        }
    }

    private static JsonObject parseFileToJson(String filePath) {
        JsonObject jsonData = new JsonObject();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int separatorIndex = line.indexOf(':');
                if (separatorIndex == -1) continue;
                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();
                jsonData.addProperty(key, value);
            }
            return jsonData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
