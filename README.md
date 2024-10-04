# Distributed-Systems-Assignment-2

This assignment creates a system where weather data is shared between servers and clients using a JSON format. The **Aggregation Server** gets updates from **Content Servers** and provides the weather data to **GET Clients** when they ask for it. All parts of the system use Lamport clocks to keep things in order, making sure updates happen correctly. The Aggregation Server saves data so it can recover after crashes and removes old data that is no longer needed. The system is built to handle multiple requests at once and ensures that everything works smoothly and reliably.

Key Features

1. Aggregation Server
Concurrent Handling: Manages multiple client and content server connections simultaneously using multithreading.
Lamport Clock Synchronization: Implements a Lamport clock to maintain a consistent event ordering across the system.
Data Storage:
Stores weather data in a JSON file (weatherData.json) for persistence.
Uses a temporary file (weatherData.tmp) for atomic write operations to prevent data corruption.
HTTP Protocol Support:
Handles HTTP GET and PUT requests.
Sends appropriate HTTP response codes and messages.
Data Expiration and Management:
Periodically removes weather data entries older than 30 seconds.
Maintains only the most recent 20 updates.
Error Handling:
Validates incoming JSON data.
Handles exceptions and sends meaningful HTTP responses in case of errors.
2. Content Server
Data Reading and Parsing:
Reads weather data from a local file (e.g., weather.txt) formatted with key-value pairs.
Parses the file and converts the data into a JSON object using com.google.gson.
Data Uploading:
Sends an HTTP PUT request to the aggregation server with the JSON payload.
Includes the Lamport clock value in the request headers.
Lamport Clock Maintenance:
Updates its Lamport clock based on communication with the aggregation server.
Error Handling:
Handles server responses and exceptions gracefully.
3. GET Client
Server Communication:
Connects to the aggregation server and sends an HTTP GET request.
Includes the Lamport clock value in the request headers.
Data Display:
Receives the JSON response and parses it using com.google.gson.
Displays each weather data entry in a user-friendly format, listing attributes and their values.
Lamport Clock Maintenance:
Updates its Lamport clock based on the server's response.
Error Handling:
Manages exceptions and provides informative messages to the user.
4. Lamport Clock Implementation
Synchronization:
Ensures a consistent ordering of events in the distributed system.
Each component increments its Lamport clock before sending a message and updates it upon receiving a message.
Thread Safety:
Uses synchronized methods to prevent race conditions when accessing the Lamport clock variable.


Getting Started
Prerequisites
Java Development Kit (JDK): Ensure JDK 8 or higher is installed on your system.
Eclipse IDE: Install the latest version of Eclipse IDE for Java Developers.
Gson Library: Download the com.google.gson library to handle JSON operations.
Project Setup in Eclipse
1. Set Up the Project Structure
Create a New Java Project:

Open Eclipse.
Go to File > New > Java Project.
Name the project (e.g., WeatherDataAggregationSystem) and click Finish.
Create Packages (Optional):

Right-click on src and select New > Package.
Create packages for better organization (e.g., server, client, contentserver).
2. Add the Gson Library to the Project
Download the Gson JAR File:

Download gson-2.8.9.jar (or the latest version) from the Maven Repository.
Add the JAR to the Build Path:

Right-click on your project in the Package Explorer.
Select Build Path > Add External Archives.
Browse to the location where you downloaded gson-2.8.9.jar and select it.
Click Open to add it to your project.
3. Create the Java Classes
AggregationServer.java:

Right-click on the appropriate package (e.g., server) and select New > Class.
Name the class AggregationServer and click Finish.
Paste the code for AggregationServer.java into the class file.
GETClient.java:

Right-click on the appropriate package (e.g., client) and select New > Class.
Name the class GETClient and click Finish.
Paste the code for GETClient.java into the class file.
ContentServer.java:

Right-click on the appropriate package (e.g., contentserver) and select New > Class.
Name the class ContentServer and click Finish.
Paste the code for ContentServer.java into the class file.
4. Create the Weather Data File
Create weather.txt:
In your project directory, create a file named weather.txt.

Paste the weather data in the specified format:

