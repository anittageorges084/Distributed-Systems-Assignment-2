# Distributed-Systems-Assignment-2

This assignment creates a system where weather data is shared between servers and clients using a JSON format. The **Aggregation Server** gets updates from **Content Servers** and provides the weather data to **GET Clients** when they ask for it. All parts of the system use Lamport clocks to keep things in order, making sure updates happen correctly. The Aggregation Server saves data so it can recover after crashes and removes old data that is no longer needed. The system is built to handle multiple requests at once and ensures that everything works smoothly and reliably.

