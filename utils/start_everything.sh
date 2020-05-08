#!/usr/bin/bash
# Start Flink
${FLINK_HOME}/bin/start-cluster.sh &

# Start Zookeeper and Kafka
echo "Starting Kafka Zookeper"
${KAFKA_HOME}/bin/zookeeper-server-start.sh ${KAFKA_HOME}/config/zookeeper.properties &
#sleep 5s
echo "Starting Kafka Cluster"
${KAFKA_HOME}/bin/kafka-server-start.sh ${KAFKA_HOME}/config/server.properties &

sleep 5s
echo "Starting Jolokia"
kafka_pid_cmd=`ps ax | grep -i 'kafka.Kafka' | grep -v grep | awk '{print $1}'`
echo ${kafka_pid_cmd}
java -jar ${JOLOKIA_JAR} start ${kafka_pid_cmd}


## List Kafka topics
#${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --list

## Create Kafka topic
#${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic test --replication-factor 1 --partitions 1

## Delete Kafka topic
#${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --delete -topic test

## Consume Kafka topic
#${KAFKA_HOME}/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning

