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

        } catch (Exception e) {
            System.out.println("Client exception: " + e.getMessage());
        }
    }
}

