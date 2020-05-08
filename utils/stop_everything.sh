#!/usr/bin/bash
# Stop Flink
echo "Stopping Flink Cluster"
${FLINK_HOME}/bin/stop-cluster.sh

# Stop Zookeeper and Kafka
echo "Deleting Kafka topics"
${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --delete -topic cars_bdapro
${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --delete -topic clicks_bdapro
${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --delete -topic cars_result
${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper localhost:2181 --delete -topic clicks_result

echo "Getting kafka pid"
kafka_pid_cmd=`ps ax | grep -i 'kafka.Kafka' | grep -v grep | awk '{print $1}'`
echo ${kafka_pid_cmd}
echo "Stopping Jolokia"
java -jar ${JOLOKIA_JAR} --quiet stop ${kafka_pid_cmd}

sleep 2s
echo "Stopping Kafka Cluster"
${KAFKA_HOME}/bin/kafka-server-stop.sh
sleep 7s
echo "Stopping Kafka Zookeper"
${KAFKA_HOME}/bin/zookeeper-server-stop.sh ${KAFKA_HOME}/config/zookeeper.properties
sleep 5s
echo "You may want to manually remove kafka-logs, when everything is stopped. Edit utils/stop_everything.sh"
# Uncomment if you want to remove kafka logs and set the directories correctly according to your kafka configuration
#echo "Removing kafka server logs"
# rm -rf /tmp/kafka-logs
#echo "Removing kafka zookeper logs"
# rm -rf /tmp/zookeeper-ml/
