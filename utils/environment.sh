#!/usr/bin/bash


export WORKING_DIR=*fill this*
export FLINK_HOME=${WORKING_DIR}/flink-1.9.2
export FLINK_MASTER=*fill this* (host:port)
export FLINK_QUERIES_JAR=${WORKING_DIR}/*fill this*
export KAFKA_HOME=${WORKING_DIR}/kafka_2.11-2.4.0
export DATAGEN_HOME=${WORKING_DIR}/*fill this*
export DATAGEN_JAR=${DATAGEN_HOME}/*fill this* (asdf.jar)
export TMP_DIR=${WORKING_DIR}/output *create this directory*
export JOLOKIA_JAR=${WORKING_DIR}/bin/jolokia.jar (download jolokia jar from  https://search.maven.org/remotecontent?filepath=org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar)
export JOLOKIA_URL=*fill this* host:port/jolokia (default port=8778)
export MAX_PARALLELISM=*fill this with the maximum parallelism provided in your system. Make sure to have enough kafka partitions*
