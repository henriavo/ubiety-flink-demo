# Streaming Pipeline Assessment

## Overview

This Flink applicaiton is responsible for processing streaming data from a Kafka topic. It performs the following tasks:
- **Ingests** order events from a Kafka topic named `orders`.
- **Aggregates** the total order amount per country in 1-minute tumbling windows.
- **Publishes** the aggregated results to a new Kafka topic named `processed-orders`, showing the total amount per country per minute.

## What’s Provided


- `docker-compose-combination.yaml` – Sets up a Kafka cluster (with ZooKeeper) and a Flink cluster
with shared network. This design choice allows for simplified communication between Kafka and Flink.
- **Source Code** – The Flink application code that processes the Kafka stream. An assumption that only valid
  and well-formed messages are sent to the `orders` topic is made, so no error handling is implemented.

    
## Prerequisites

- **Docker** – Ensure you have Docker installed and running.
- **jdk17** – Ensure JDK 17 is installed.
- **docker-compose** – Ensure you have Docker Compose installed.


## Running the Application
1. **Start Docker Compose**: Run the following command to start the Kafka and Flink clusters:
   ```bash
   docker-compose -f docker-compose-combination.yaml up -d
   ```  
   
2. **Setup Data**: Please refer to project `sr-backend-home-assessment` for instructions on how to set up the 
   data in the Kafka topic `orders`. This typically involves running a Python script that produces sample 
order events to the Kafka topic.

3. **Build the Flink Application**: Navigate to the source code directory and build the Flink application:
   ```bash
    mvn clean package
    ```
4. **Submit the Flink Job**: Use the Flink web UI to upload the Jar file.
   - Access the Flink web UI at `http://localhost:8081`.
   - Click on "Submit new job" and upload the generated Jar file from the `target` directory.
   - Click "Submit".
5. **Monitor the Job**: Once the job is running, you can monitor its progress through the Flink web UI. The 
 job will process incoming order events and output the aggregated results to the `processed-orders` Kafka topic.

6. **Verify message count**: You can check the number of messages in the `processed-orders` topic using a Kafka consumer or a tool like `kafka-console-consumer.sh`:
   ```bash
   docker exec -it <kafka_container_name> kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic processed-orders --from-beginning
   ```