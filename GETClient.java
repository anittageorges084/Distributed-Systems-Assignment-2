import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GETClient {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server-url> [station-id]");
            return;
        }

        String serverUrl = args[0];
        String stationId = args.length > 1 ? args[1] : null;

        try {
            URL url = new URL(serverUrl);
            Socket socket = new Socket(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send GET request
            String path = url.getPath();
            if (stationId != null) {
                path += "?id=" + stationId;
            }
            out.println("GET " + path + " HTTP/1.1");
            out.println("Host: " + url.getHost());
            out.println("Connection: close");
            out.println();

            socket.close();
            // Inside try block after sending the GET request
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

            // Read response body
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBody.append(line);
            }

            System.out.println("Server response: " + responseBody.toString());


                    } catch (Exception e) {
                        System.out.println("Client exception: " + e.getMessage());
                    }
    }
}

