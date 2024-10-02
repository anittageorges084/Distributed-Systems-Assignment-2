# Distributed-Systems-Assignment-2

This assignment creates a system where weather data is shared between servers and clients using a JSON format. The **Aggregation Server** gets updates from **Content Servers** and provides the weather data to **GET Clients** when they ask for it. All parts of the system use Lamport clocks to keep things in order, making sure updates happen correctly. The Aggregation Server saves data so it can recover after crashes and removes old data that is no longer needed. The system is built to handle multiple requests at once and ensures that everything works smoothly and reliably.

Aggregation Server
The Aggregation Server listens for PUT and GET requests on a specified port.
For PUT requests, it receives weather data, checks validity, and updates the stored data. It assigns a Lamport clock timestamp and returns status codes depending on the request.
For GET requests, it sends the latest aggregated weather data to clients.
The server should manage data expiration efficiently and handle crashes by persisting data to a file.

Content Server
The Content Server reads weather data from a local file, converts it to JSON, and sends an HTTP PUT request to the Aggregation Server.
It uses Lamport clocks to maintain synchronization and ensures that multiple content servers update the data in the correct order.

GET Client
The GET Client sends a GET request to the Aggregation Server, retrieves the weather data, and displays it.
It optionally accepts a station ID to retrieve specific data, handles errors, and synchronizes using Lamport clocks.

All components (Aggregation Server, Content Server, and GET Client) need to implement Lamport clocks to ensure that the correct order of operations is maintained, even when requests arrive simultaneously.

 How to Run the System
 1.Run the Aggregation Server
   To specify a port, modify the main method to accept command line arguments
 2.Start the Content Server
   Provide the server URL and the file path for the weather data as arguments.
 3.Start the GET Client
   Specify the server URL and optionally the station ID in the command line arguments.
 4.Testing
   Make sure the Aggregation Server is running before starting the Content Server or GET Client.
   Test sending weather data from the Content Server and retrieving it using the GET Client.
   Use multiple Content Servers and GET Clients simultaneously to test the handling of Lamport clock synchronization and concurrent PUT/GET requests.
   Simulate crashes and ensure the Aggregation Server recovers data from the file.

