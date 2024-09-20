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

            // Convert data to JSON
            String jsonData = convertToJson(weatherData);
            System.out.println("JSON Data: " + jsonData);

        } catch (Exception e) {
            System.out.println("ContentServer exception: " + e.getMessage());
        }
    }

    private static String convertToJson(Map<String, String> dataMap) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        Iterator<Map.Entry<String, String>> iterator = dataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            if (iterator.hasNext()) {
                json.append(",");
            }
        }
        json.append("}");
        return json.toString();
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

