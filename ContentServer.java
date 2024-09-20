public class ContentServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server-url> <file-path>");
            return;
        }

        String serverUrl = args[0];
        String filePath = args[1];

        try {
            // Read data from file
            Map<String, String> weatherData = readDataFromFile(filePath);
            if (weatherData == null) {
                System.out.println("Failed to read data from file.");
                return;
            }

            // Convert data to JSON
            String jsonData = convertToJson(weatherData);

            // Send PUT request
            sendPutRequest(serverUrl, jsonData);

        } catch (Exception e) {
            System.out.println("ContentServer exception: " + e.getMessage());
        }
    }
private static int lamportClock = 0;

private static void sendPutRequest(String serverUrl, String jsonData) {
    try {
        URL url = new URL(serverUrl);
        Socket socket = new Socket(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Increment Lamport Clock
        lamportClock++;

        // Send PUT request
        out.println("PUT " + url.getPath() + " HTTP/1.1");
        out.println("Host: " + url.getHost());
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonData.getBytes().length);
        out.println("Lamport-Clock: " + lamportClock);
        out.println("Connection: close");
        out.println();
        out.println(jsonData);

        // Read response
        String statusLine = in.readLine();
        if (statusLine == null) {
            System.out.println("No response from server.");
            return;
        }

        // Read headers
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = in.readLine()).equals("")) {
            String[] headerParts = headerLine.split(": ");
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        // Update Lamport Clock
        int serverLamportClock = Integer.parseInt(headers.getOrDefault("Lamport-Clock", "0"));
        lamportClock = Math.max(lamportClock, serverLamportClock) + 1;

        System.out.println("Updated Lamport Clock: " + lamportClock);

        socket.close();

    } catch (Exception e) {
        System.out.println("Error in sendPutRequest: " + e.getMessage());
    }
}



