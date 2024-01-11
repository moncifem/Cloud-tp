package tatooine.mosquitto;

import org.eclipse.paho.client.mqttv3.*;
import com.rabbitmq.client.*;

public class MosquittoToRabbitMQBridge {

    public static void main(String[] args) throws MqttException, Exception {

        String mqttBroker = "tcp://192.168.10.21:1122";
        String mqttTopic = "weather_topic";

        MqttClient mqttClient = new MqttClient(mqttBroker, MqttClient.generateClientId());
        mqttClient.connect();
        mqttClient.subscribe(mqttTopic, (topic, message) -> {

            String messagePayload = new String(message.getPayload());
            System.out.println("Received from Mosquitto: " + messagePayload);


            publishToRabbitMQ(messagePayload);
        });


        String rabbitMQHost = "192.168.10.22";
        String rabbitMQQueue = "weather-data-queue";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQHost);
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(rabbitMQQueue, false, false, false, null);

            // Your RabbitMQ setup is ready

            // Keep the program running to listen for MQTT messages
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    private static void publishToRabbitMQ(String message) {
        // Set up RabbitMQ connection
        String rabbitMQHost = "192.168.10.22"; 
        String rabbitMQQueue = "weather-data-queue";
        String rabbitMQueue2 = "weather-data-queue-real-time";// RabbitMQ queue to publish to

        try (Connection connection = getConnection(rabbitMQHost);
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(rabbitMQQueue, false, false, false, null);

            // Publish the message to RabbitMQ
            channel.basicPublish("", rabbitMQQueue, null, message.getBytes());
            System.out.println("Sent to RabbitMQ: " + message);
            channel.queueDeclare(rabbitMQueue2, false, false, false, null);
            channel.basicPublish("", rabbitMQueue2, null, message.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection(String host) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        return factory.newConnection();
    }
}
