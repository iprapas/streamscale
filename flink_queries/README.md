# Flink-Quries

This project is taken by WS-19/20 BDAPro students:
- Ioannis Prapas
- Ankush Sharma
- Sokratis Papadopoulos

### Project description
This repository contains the streaming queries that were used in the benchmark for **LEAST SCALER: LEArned STreaming job SCALER**. The queries were implemented in Apache Flink version 1.9.1.
The business use cases covered are for ```Car Streams``` & ```Click Streams```. Two queries were implemented for each stream(detailed description below).


### Kafka Topics
There are four Kafka topics in total. Two topics to stream raw car and click events, and two topics for aggregated results:

1. ```clicks_bdapro``` : Topic for raw click events
2. ```cars_bdapro```   : Topic for raw car
3. ```clicks_result``` : Topic for aggregated click. Raw clicks events are aggregated over a time window and sent to this topic
4. ```clicks_bdapro``` : Topic for aggregated cars. Raw car events are aggregated over a time window and sent to this topic


### Jobs & Queries
In total, we have three jobs. We modeled the two business use cases of cars & clicks as two independent jobs : Cars Job & Clicks Job. The queries operate on event-time tumbling windows of 10 seconds with varying number of keys and operations (map, filter, aggregations). 
There is a separate job that runs for aggregations. This job streams aggregated events from  ```clicks_result``` &  ```cars_result``` and writes the aggregations to a csv file. 

We now define our queries : 

- Car stream *(carId, streetId, type, timestamp)*
  - Query 1: find intersections/streets with most cars
  - Query 2: list trucks in highways

- Clickstream *(clickId, siteId, ipAddress, timestamp, userAgent)*
  - Query 1: events counting per device
  - Query 2: events counting per country

Jump right into the code [!https://github.com/dima-iot-fog-Optimizer/flink-queries/blob/master/src/main/java/StreamingJob.java](here) to see how the queries have been implemented.

   
### Deserialization
Each car & click record coming from kafka topics are JSON strings. These JSON strings are deserialized to a Java POJO before entering the query pipeline.

### Execution
Each of the three jobs has its own string id:

1. ```clicks``` : id for click job
2. ```cars``` : id for car job
3. ```agg``` : id for aggregations job


Execute the steps in order to run the project :

1. Set Kafka & Zookeeper address as environment variables. For example:
    ```bash
    export KAFKA_SERVER="localhost:9092"
    export ZOOKEEPER_SERVER="localhost:2181"
    ```
2. Make sure Kafka & Zookeeper are running
3. Execute ```java mvn package``` and generate a jar file
4. As a program arugment, pass the id of the job that needs to be run. For example, if the click job needs to be run and the jar file is called ```flink-queries.jar```, the following command should be executed :

```java
java -jar flink-queries.jar --clicks
```


### Flink Settings
We went with default settings in Flink. Only the following 2 settings were changed :

```text
jobmanager.heap.mb=8024
taskmanager.heap.mb=13000
```   
