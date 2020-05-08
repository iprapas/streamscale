${FLINK_HOME}/bin/flink run -m ${FLINK_MASTER} -d -p 1 ~/ml_opt_bdapro/flink-queries/target/flink-queries-1.0-SNAPSHOT.jar --clicks
${FLINK_HOME}/bin/flink run -m ${FLINK_MASTER} -d -p 1 ~/ml_opt_bdapro/flink-queries/target/flink-queries-1.0-SNAPSHOT.jar --cars
${FLINK_HOME}/bin/flink run -m ${FLINK_MASTER} -d -p 1 ~/ml_opt_bdapro/flink-queries/target/flink-queries-1.0-SNAPSHOT.jar --agg
