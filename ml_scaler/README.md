# ml_scaler
ML model that will scale automatically the flink job

To run, you should download and package the repos:
* https://github.com/dima-iot-fog-Optimizer/datagen-tool 
* https://github.com/dima-iot-fog-Optimizer/flink-queries

Tested with

* Kafka version version 2.3.1
* Apache Flink version 1.10


Download the jolokia "JVM-Agent" here https://search.maven.org/remotecontent?filepath=org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar

Also, download 
* https://github.com/dima-iot-fog-Optimizer/utils
and fill environment.sh with the correct values for your machine.


To train model and fill Q-table:
`python rl_scaler training`

To test with generated Q-table:
`python rl_scaler testing`

