public class ContentServer {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ContentServer <file-path>");
            return;
        }

        String filePath = args[0];

        try {
            // Read data from file
            Map<String, String> weatherData = readDataFromFile(filePath);
            if (weatherData == null) {
                System.out.println("Failed to read data from file.");
                return;
            }

            System.out.println("Data from file: " + weatherData);

        } catch (Exception e) {
            System.out.println("ContentServer exception: " + e.getMessage());
        }
    }

    private static Map<String, String> readDataFromFile(String filePath) {
        Map<String, String> dataMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] kv = line.split(":", 2);
                if (kv.length == 2) {
                    dataMap.put(kv[0].trim(), kv[1].trim());
                }
            }
            return dataMap;
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
    }
}
