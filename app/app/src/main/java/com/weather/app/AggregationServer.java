package com.weather.app;

// File: AggregationServer.java

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {

    private static final int DEFAULT_PORT = 4567;
    private static final String DATA_FILE = "weatherData.json";
    private static final String TEMP_FILE = "weatherData.tmp";
    private static final long EXPIRATION_TIME_MILLIS = 30_000; // 30 seconds
    private static final LamportClock lamportClock = new LamportClock();

    // Data structures to store weather data and timestamps of content servers
    public static final Map<String, JsonObject> weatherData = new ConcurrentHashMap<>();
    private static final Map<String, Long> serverTimestamps = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        int port = DEFAULT_PORT;

        // Check if a port number is passed as a command-line argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port: " + DEFAULT_PORT);
            }
        }

        // Periodically clean up expired entries
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(AggregationServer::cleanExpiredData, 10, 10, TimeUnit.SECONDS);

        // Start server
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Handle each client in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }

        }
    }

    private static void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String requestType = in.readLine();
            if (requestType == null) return;

            System.out.println("Received request: " + requestType);

            String[] requestParts = requestType.split(" ", 2);
            String method = requestParts.length >= 1 ? requestParts[0] : "";
            String path = requestParts.length >= 2 ? requestParts[1] : "";

            // Read headers and extract Lamport-Clock
            Map<String, String> headers = new HashMap<>();
            String line;
            int clientLamportClock = 0;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int separatorIndex = line.indexOf(":");
                if (separatorIndex != -1) {
                    String headerName = line.substring(0, separatorIndex).trim();
                    String headerValue = line.substring(separatorIndex + 1).trim();
                    headers.put(headerName, headerValue);

                    if (headerName.equalsIgnoreCase("Lamport-Clock")) {
                        clientLamportClock = Integer.parseInt(headerValue);
                    }
                }
            }

            // Update server's Lamport clock
            lamportClock.update(clientLamportClock);

            if ("PUT".equalsIgnoreCase(method)) {
                handlePutRequest(in, out, clientSocket.getInetAddress().toString(), headers);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetRequest(out);
            } else {
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Lamport-Clock: " + lamportClock.getClock());
                out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePutRequest(BufferedReader in, PrintWriter out, String contentServer, Map<String, String> headers) throws IOException {
        lamportClock.tick();

        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));

        // No content
        if (contentLength == 0) {
            out.println("HTTP/1.1 204 No Content");
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println();
            return;
        }

        char[] bodyData = new char[contentLength];
        int read = in.read(bodyData, 0, contentLength);
        if (read != contentLength) {
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println();
            return;
        }

        String jsonData = new String(bodyData);

        if (!isValidJson(jsonData)) {
            System.out.println("Invalid JSON received: " + jsonData);
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println();
            return;
        }

        // Add metadata (timestamp and content server origin)
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
        jsonObject.addProperty("origin", contentServer);
        jsonObject.addProperty("timestamp", Instant.now().toEpochMilli());

        String entryId = jsonObject.get("id").getAsString();
        weatherData.put(entryId, jsonObject);
        serverTimestamps.put(contentServer, Instant.now().toEpochMilli());

        boolean isNewFile = !new File(DATA_FILE).exists();

        try {
            writeToTempFile(weatherData);
            if (commitTempFile()) {
                if (isNewFile) {
                    out.println("HTTP/1.1 201 Created");
                } else {
                    out.println("HTTP/1.1 200 OK");
                }
                out.println("Lamport-Clock: " + lamportClock.getClock());
                out.println();
            } else {
                out.println("HTTP/1.1 500 Internal Server Error");
                out.println("Lamport-Clock: " + lamportClock.getClock());
                out.println();
            }
        } catch (IOException e) {
            System.out.println("File write error: " + e.getMessage());
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println();
        }
    }

    public static boolean isValidJson(String jsonData) {
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonData);
            return jsonElement.isJsonObject();
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    private static void handleGetRequest(PrintWriter out) {
        lamportClock.tick();

        String jsonResponse = convertToJson(weatherData);

        // Send headers
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonResponse.length());
        out.println("Lamport-Clock: " + lamportClock.getClock());
        out.println();

        // Send body
        out.print(jsonResponse);
        out.flush();
    }

    private static void writeToTempFile(Map<String, JsonObject> data) throws IOException {
        try (FileWriter fileWriter = new FileWriter(TEMP_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data.values(), fileWriter);
        }
    }

    private static boolean commitTempFile() throws IOException {
        File tempFile = new File(TEMP_FILE);
        File finalFile = new File(DATA_FILE);

        try {
            if (finalFile.exists()) {
                finalFile.delete();
            }
            return tempFile.renameTo(finalFile);
        } finally {
            tempFile.delete();
        }
    }

    public static String convertToJson(Map<String, JsonObject> weatherData) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Collection<JsonObject> dataCollection = weatherData.values();
        return gson.toJson(dataCollection);
    }

    public static void cleanExpiredData() {
        long currentTime = Instant.now().toEpochMilli();
        Iterator<Map.Entry<String, JsonObject>> iterator = weatherData.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, JsonObject> entry = iterator.next();
            JsonObject jsonObject = entry.getValue();
            long timestamp = jsonObject.get("timestamp").getAsLong();
            String origin = jsonObject.get("origin").getAsString();

            if (currentTime - timestamp > EXPIRATION_TIME_MILLIS) {
                System.out.println("Removing expired entry from " + origin);
                iterator.remove();
                serverTimestamps.remove(origin);
            }
        }

        // Limit to the most recent 20 updates
        if (weatherData.size() > 20) {
            List<Map.Entry<String, JsonObject>> entries = new ArrayList<>(weatherData.entrySet());
            // Sort entries by timestamp ascending
            entries.sort(Comparator.comparingLong(e -> e.getValue().get("timestamp").getAsLong()));
            int excess = weatherData.size() - 20;
            for (int i = 0; i < excess; i++) {
                Map.Entry<String, JsonObject> entry = entries.get(i);
                weatherData.remove(entry.getKey());
                serverTimestamps.remove(entry.getValue().get("origin").getAsString());
            }
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
