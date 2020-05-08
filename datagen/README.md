# Datagen-Tool

This project is taken by WS-19/20 BDAPro students:
- Ioannis Prapas
- Ankush Sharma
- Sokratis Papadopoulos

### Project description
This repository contains the data generators used in the benchmark for **LEAST SCALER: LEArned STreaming job SCALER**. 
The business use cases covered are for ```Car Streams``` & ```Click Streams```. Naturally, there are two data generators, one for generating cars records & one for clicks.

### Schema
The schema for cars record :

```text
    carId : String 
    carType : String (One out of [car, truck, bus, motorbike])
    roadType : String (One out of [avanue, boulevard, highway, living street, side road, mountain road])
    streetId : Int
    timestamp : String [Format : yyyy-MM-dd HH:mm:ss.SSSSSS]
``` 

Schema for  Click record :
```text
    clickId : String (Of type UUID)
    siteId : String
    ip : String
    device : String (One out of [avanue, boulevard, highway, living street, side road, mountain road])
    country : String (One out of [iOS, Mac, Android, Windows, Linux])
    timestamp : String [Format : yyyy-MM-dd HH:mm:ss.SSSSSS]
``` 


### Kafka Topics
For each stream, we maintain a separate Kafka topic. They are :

1. ```cars_bdapro``` : Topic for car records
2. ```click_bdapro``` : Topic for click records

### Serialization
Each car & click record is serialized to a JSON string before being pushed to its respective topic. 

Example of a car record :
```json
{
   "carId":"1",
   "carType":"truck",
   "roadType":"avenue",
   "streetId":1,
   "timestamp":"2020-03-06 15:25:54.827000"
}
```

Example of a click record :
```json
{
   "clickId":"a90a42ee-ab3b-46c7-b1da-073de3be7995",
   "timestamp":"2020-03-06 15:24:39.967000",
   "siteId":"1",
   "ip":"11.121.1.111",
   "device":"iOS",
   "country":"Greece"
}
```


### Execution
The tool expects three parameters to be passed as program arguments. They template is :

```text
Use arguments: <generator_name> <num_events (int)> <max_rate_per_second (int)>
```

1. ```<generator_name>``` : Either ```--cars``` or ```--clicks```
2. ```<num_events>``` : Total number of events to generate
3. ```<max_rate_per_second>``` : The max number of events that will be generated per second

For instance, suppose you want to generate 1 million car records at a max rate of 1000 per second. The arguments will be :

```text
--cars 1000000 1000
```

In order to run the generator, execute the following steps:
1. 

Execute the steps in order to run the project :

1. Set Kafka address as environment variables. For example:
    ```bash
    export KAFKA_SERVER="localhost:9092"
    ```
2. Make sure Kafka & Zookeeper are running
3. Execute ```java mvn package``` and generate a jar file
4. Pass the program arguments and run the jar file. For instance, if the jar file is called ```datagen.jar``` and you want to run car generator for 1 million records at 1000 per second, do :

```java
java -jar datagen.jar --cars 1000000 1000
```
