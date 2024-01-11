package kingsman.rabbitmqMongo;

import com.rabbitmq.client.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherDataConsumer {

    private static final String MONGO_HOST = "192.168.10.17";

    private static final int MONGO_PORT = 27017;

    private static final String MONGO_DATABASE = "meteo";
    private static final String MONGO_COLLECTION = "weather_data";


    private static final String RABBITMQ_HOST = "192.168.10.22";
    private static final int RABBITMQ_PORT = 5672;
    private static final String RABBITMQ_QUEUE = "weather-data-queue";
    private static final String RABBITMQ_USER = "kingsman";
    private static final String RABBITMQ_PASSWORD = "kingsman";

    private static final JSONParser jsonParser = new JSONParser();
    public static void main(String[] args) {
        try {
            MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%d", MONGO_HOST, MONGO_PORT));
            MongoDatabase mongoDatabase = mongoClient.getDatabase(MONGO_DATABASE);
            MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(MONGO_COLLECTION);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT);
            factory.setUsername(RABBITMQ_USER);
            factory.setPassword(RABBITMQ_PASSWORD);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(RABBITMQ_QUEUE, false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {

                    mongoCollection.insertOne(Document.parse(message));

                    System.out.println("Weather data received and inserted: " + message);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle parsing exception
                }
            };


            channel.basicConsume(RABBITMQ_QUEUE, false, deliverCallback, consumerTag -> { });

            MongoCollection<Document> mongoCollection2 = mongoDatabase.getCollection("results");
            Connection connection2 = factory.newConnection();
            Channel channel2 = connection2.createChannel();
            channel2.queueDeclare("results-real-time-q", false, false, false, null);

            DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    JSONObject result = (JSONObject) jsonParser.parse(message);

                    Document document = Document.parse(result.toString());
                    mongoCollection2.insertOne(document);

                    System.out.println("result received and inserted: " + result);
                    channel2.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle parsing exception
                }
            };
            channel2.basicConsume("results-real-time-q", false, deliverCallback2, consumerTag -> { });


            MongoCollection<Document> mongoCollection3 = mongoDatabase.getCollection("commands");
            Connection connection3 = factory.newConnection();
            Channel channel3 = connection3.createChannel();
            channel3.queueDeclare("commands", false, false, false, null);

            DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    JSONObject result = (JSONObject) jsonParser.parse(message);

                    Document document = Document.parse(result.toString());
                    mongoCollection3.insertOne(document);

                    System.out.println("command received and inserted: " + result);
                    channel3.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle parsing exception
                }
            };
            channel3.basicConsume("results-real-time-q", false, deliverCallback3, consumerTag -> { });


            MongoCollection<Document> mongoCollection4 = mongoDatabase.getCollection("automatic-commands");
            Connection connection4 = factory.newConnection();
            Channel channel4 = connection4.createChannel();
            channel4.queueDeclare("automatic-commands", false, false, false, null);

            DeliverCallback deliverCallback4 = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    JSONObject result = (JSONObject) jsonParser.parse(message);

                    Document document = Document.parse(result.toString());
                    mongoCollection4.insertOne(document);

                    System.out.println("command received and inserted: " + result);
                    channel4.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle parsing exception
                }
            };


            channel4.basicConsume("results-real-time-q", false, deliverCallback4, consumerTag -> { });
            System.out.println(" [*] Waiting for messages. To exit, press Ctrl+C");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
