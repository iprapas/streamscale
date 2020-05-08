import aggregations.*;
import deserializers.CarJsonDeserializer;
import deserializers.ClickJsonDeserializer;
import domain.Car;
import domain.Click;
import domain.KafkaTopic;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import timestamp.CarTimestampExtractor;
import timestamp.ClickTimestampExtractor;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class StreamingJob {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Use arguments: <query (--cars, --clicks, --agg)");
        } else {
            final StreamExecutionEnvironment flink = StreamExecutionEnvironment.getExecutionEnvironment();
            final Properties kafkaProps = kafkaProps();
            final String query = args[0];
            flink.getConfig().setAutoWatermarkInterval(0);

            if (query.equals("--agg")){
                final FlinkKafkaConsumer<String> consumeCarsResult = new FlinkKafkaConsumer<String>(KafkaTopic.CARS_RESULT, new SimpleStringSchema(), kafkaProps);
                final FlinkKafkaConsumer<String> consumeClicksResult = new FlinkKafkaConsumer<String>(KafkaTopic.CLICKS_RESULT, new SimpleStringSchema(), kafkaProps);
                consumeCarsResult.setStartFromEarliest();
                consumeClicksResult.setStartFromEarliest();
                final DataStreamSource<String> carsResult = flink.addSource(consumeCarsResult);
                final DataStreamSource<String> clicksResult = flink.addSource(consumeClicksResult);

                carsResult
                        .map(new StringToTupleMapFunc())
                        .timeWindowAll(Time.seconds(10), Time.seconds(5)) //10 seconds sliding 5
                        .apply(new ResultAggregation())
                        .map(t -> t.toString().substring(1, t.toString().length()-1))
                        .writeAsText("ml_opt_bdapro/output/cars.csv", FileSystem.WriteMode.OVERWRITE)
                        .setParallelism(1);

                clicksResult
                        .map(new StringToTupleMapFunc())
                        .timeWindowAll(Time.seconds(10), Time.seconds(5)) //10 seconds sliding 5
                        .apply(new ResultAggregation())
                        .map(t -> t.toString().substring(1, t.toString().length()-1))
                        .writeAsText("ml_opt_bdapro/output/clicks.csv", FileSystem.WriteMode.OVERWRITE)
                        .setParallelism(1);

            } else if (query.equals("--cars")) {
                final FlinkKafkaConsumer<Car> carsKafkaConsumer = new FlinkKafkaConsumer<>(KafkaTopic.CARS, new CarJsonDeserializer(), kafkaProps);
                carsKafkaConsumer.setStartFromLatest();
                final DataStreamSource<Car> carStreams = flink.addSource(carsKafkaConsumer);
                carStreams.assignTimestampsAndWatermarks(new CarTimestampExtractor());

                FlinkKafkaProducer<String> carsResults = new FlinkKafkaProducer<String>(
                        System.getenv("KAFKA_SERVER"), // broker list
                        KafkaTopic.CARS_RESULT,      // target topic
                        new SimpleStringSchema());   // serialization schema

                // ============  CARS QUERY 1: find intersections/streets with most cars, in order to predict upcoming traffic congestion
                carStreams
                        .keyBy(car -> car.getStreetId())
                        .timeWindow(Time.seconds(10))
                        .apply(new CarByStreetIntersectionFunction(flink.getParallelism()))
                        .map(t -> t.toString())
                        .addSink(carsResults)
                        .name("Find intersections/streets with most cars");

                // ============  CARS QUERY 2: identify tracks on highways on illegal timeslots
                carStreams
                        .filter(car -> car.getCarType().equals("truck") && car.getRoadType().equals("highway"))
                        .keyBy(car -> car.getStreetId())
                        .timeWindow(Time.seconds(10))
                        .apply(new IllegalTrucksFunction(flink.getParallelism()))
                        .map(t -> t.toString())
                        .addSink(carsResults)
                        .name("Illegal trucks observed in highways");

            } else if (query.equals("--clicks")) {

                final FlinkKafkaConsumer<Click> clicksKafkaConsumer = new FlinkKafkaConsumer<>(KafkaTopic.CLICKS, new ClickJsonDeserializer(), kafkaProps);
                clicksKafkaConsumer.setStartFromLatest();
                final DataStreamSource<Click> clickStreams = flink.addSource(clicksKafkaConsumer);
                clickStreams.assignTimestampsAndWatermarks(new ClickTimestampExtractor());

                FlinkKafkaProducer<String> clicksResults = new FlinkKafkaProducer<String>(
                        System.getenv("KAFKA_SERVER"),  // broker list
                        KafkaTopic.CLICKS_RESULT,   // target topic
                        new SimpleStringSchema());  // serialization schema

                // ============  CLICKS QUERY 1: find the most frequent device used (Android, Mac, iOS, ...)
                clickStreams
                        .keyBy(click -> click.getDevice())
                        .timeWindow(Time.seconds(10))
                        .apply(new ClickByDeviceFunction(flink.getParallelism()))
                        .map(t -> t.toString())
                        .addSink(clicksResults)
                        .name("Click by Device aggregation");

                // ============  CLICKS QUERY 2: number of website visitors per country
                clickStreams
                        .keyBy(click -> click.getCountry())
                        .timeWindow(Time.seconds(10))
                        .apply(new ClickByCountryFunction(flink.getParallelism()))
                        .map(t -> t.toString())
                        .addSink(clicksResults)
                        .name("Click by Country aggregation");
            }

            flink.execute(query + "," + flink.getParallelism());
        }
    }


    /**
     * Setup the properties for the Kafka connection
     * @return the initialized properties
     */
    public static Properties kafkaProps() {
        final Properties props = new Properties();
        props.setProperty("bootstrap.servers", System.getenv("KAFKA_SERVER"));
        props.setProperty("zookeeper.connect", System.getenv("ZOOKEEPER_SERVER"));
        props.setProperty("group.id", "flink-queries");
        props.setProperty("client.id", "flink-queries");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    private static class StringToTupleMapFunc implements MapFunction<String, Tuple5<Integer,String, LocalDateTime, LocalDateTime, Double>> {

        @Override
        public Tuple5<Integer, String, LocalDateTime, LocalDateTime, Double> map(String record) throws Exception {
            String[] tokens = record.split("\t");
            Integer parallelism = Integer.parseInt(tokens[0]);
            String queryId = tokens[1];
            LocalDateTime winStart = LocalDateTime.parse(tokens[2]);
            LocalDateTime winEnd = LocalDateTime.parse(tokens[3]);
            Double latency = Double.parseDouble(tokens[4]);
            return new Tuple5<>(parallelism,queryId,winStart,winEnd,latency);
        }
    }

}
