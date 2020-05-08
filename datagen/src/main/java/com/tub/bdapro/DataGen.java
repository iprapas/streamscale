package com.tub.bdapro;

import com.tub.bdapro.model.Car;
import com.tub.bdapro.model.Click;
import com.tub.bdapro.model.KafkaTopic;
import com.tub.bdapro.util.Json;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class DataGen {

    private static KafkaProducer<String, String> kafka;

    static {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_SERVER"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaTopic.CLICKS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafka = new KafkaProducer<>(props);
    }

    public static void main(String[] args) throws InterruptedException {
        // Get current time
        long start = System.currentTimeMillis();
        long count = 0;
        if (args[0].equals("--clicks")) {
            if (args.length == 3) {
                count = Long.parseLong(args[1]);
                float maxRate = Float.parseFloat(args[2]);
                generateClicks(count, maxRate);
            } else {
                System.out.println("Use arguments: <generator (--cars, --clicks)> <num_events (int)> <max_rate per second>");
                generateClicks(count, 100000);
            }

        } else if (args[0].equals("--cars")) {
            if (args.length == 3) {
                count = Long.parseLong(args[1]);
                float maxRate = Float.parseFloat(args[2]);
                generateCars(count, maxRate);
            } else {
                System.out.println("Use arguments: <generator (--cars, --clicks)> <num_events (int)> <max_rate per second>");
                generateCars(count, 100000);
            }
        }

        long elapsedTimeMillis = System.currentTimeMillis() - start;

        // Get elapsed time in seconds
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        System.out.println("Generated " + count + " in " + elapsedTimeSec + " seconds");
        System.out.println("Rate: " + count / elapsedTimeSec + "/s");

    }

    private static final Random random = new Random();

    /**
     * Generates a random IP Address
     *
     * @return IP
     */
    private static String generateRandomIp() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    public static void generateClicks(long count, float maxRate) throws InterruptedException {
        System.out.println("Generating " + count + " click events on a max_rate of " + maxRate + " events per second");

        long start = System.currentTimeMillis();
        long sent = 0;
        while ((count == 0) || (sent < count)) {
            kafka.send(new ProducerRecord<>(KafkaTopic.CLICKS, getNewClick()));
            sent++;

            // Get elapsed time in seconds
            long elapsedTimeMillis = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMillis / 1000F;
            float rate = sent / elapsedTimeSec;

            while (rate > maxRate) {
                elapsedTimeMillis = System.currentTimeMillis() - start;
                elapsedTimeSec = elapsedTimeMillis / 1000F;
                Thread.sleep(1);
                rate = sent / elapsedTimeSec;
            }
        }
    }

    public static String getNewClick(){
        //create site ids for random assignment
        List<String> siteIds = new ArrayList<>();
        siteIds.add("1");
        siteIds.add("2");
        siteIds.add("3");
        siteIds.add("4");
        siteIds.add("5");
        siteIds.add("6");
        siteIds.add("7");
        siteIds.add("8");

        //create device types for random assignment
        List<String> devices = new ArrayList<>();
        devices.add("iOS");
        devices.add("Mac");
        devices.add("Android");
        devices.add("Windows");
        devices.add("Linux");

        //create countries for random assignment
        List<String> countries = new ArrayList<>();
        countries.add("India");
        countries.add("Greece");
        countries.add("Germany");
        countries.add("Canada");
        countries.add("USA");
        countries.add("Belgium");
        countries.add("Spain");
        countries.add("Sweden");

        final String clickId = UUID.randomUUID().toString();
        final String ip = generateRandomIp();
        final String siteId = siteIds.get(random.nextInt(siteIds.size()));
        final String device = devices.get(random.nextInt(devices.size()));
        final String country = countries.get(random.nextInt(countries.size()));
        final ZonedDateTime timestamp = ZonedDateTime.now(ZoneId.of("UTC"));

        final Click click = new Click(clickId, timestamp, siteId, ip, device, country);
        return Json.toJson(click);
    }


    public static void generateCars ( long count, float maxRate) throws InterruptedException {
        System.out.println("Generating " + count + " car events on a max_rate of " + maxRate + " events per second" );

        long start = System.currentTimeMillis();

        long sent = 0;
        while ((count == 0) || (sent < count)) {
            kafka.send(new ProducerRecord<>(KafkaTopic.CARS, getNewCar()));
            sent++;

            // Get elapsed time in seconds
            long elapsedTimeMillis = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMillis / 1000F;
            float rate = sent / elapsedTimeSec;
            while (rate > maxRate) {
                elapsedTimeMillis = System.currentTimeMillis() - start;
                elapsedTimeSec = elapsedTimeMillis / 1000F;
                Thread.sleep(1);
                rate = sent / elapsedTimeSec;
            }
        }
    }

    public static String getNewCar(){
        //create car types for random assignment
        List<String> carTypes = new ArrayList<>();
        carTypes.add("car");
        carTypes.add("truck");
        carTypes.add("bus");
        carTypes.add("motorbike");
        //create road types for random assignment
        List<String> roadTypes = new ArrayList<>();
        roadTypes.add("avenue");
        roadTypes.add("boulevard");
        roadTypes.add("highway");
        roadTypes.add("living street");
        roadTypes.add("side road");
        roadTypes.add("mountain road");
        String carType = carTypes.get(random.nextInt(carTypes.size())); // assign random car type to record
        String roadType = roadTypes.get(random.nextInt(roadTypes.size())); // assign random car type to record
        Integer streetId = random.nextInt(50); //assigns a random int [0,9] to record
        final Car car = new Car(carType, roadType, streetId);

        return Json.toJson(car);
    }

}