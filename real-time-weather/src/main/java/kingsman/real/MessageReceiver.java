package kingsman.real;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class MessageReceiver {

    private List<JsonNode> messageBuffer = new ArrayList<>();
    private static final int BATCH_SIZE = 300;
    private RedisTemplate<String, String> redisTemplate;



    @Autowired
    public MessageReceiver(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

    }

    @RabbitListener(queues = "weather-data-queue-real-time")
    public void receiveMessage(String message) {
        // Process the incoming message and store it in Redis
        processAndStore(message);
        System.out.println("Received message: " + message);

        JsonNode jsonNode = convertStringToJsonNode(message);
        messageBuffer.add(jsonNode);
        String result ;
        // Check if the buffer size has reached BATCH_SIZE
        if (messageBuffer.size() >= BATCH_SIZE) {
            // Calculate the average of each parameter and send it to RabbitMQ
            result = this.performCalculations();
            sendResultToRabbitMQ(result);
            // Clear the buffer after sending the average
            messageBuffer.clear();
        }


        // Send the result back to RabbitMQ
        //sendResultToRabbitMQ(result);

    }

    private void sendResultToRabbitMQ(String result) {
        String rabbitMQHost = "192.168.10.22";
        //String rabbitMQHost = "172.31.252.134";
        String rabbitMQQueue = "results-real-time-q";
        int rabbitPort = 5672;
        //int rabbitPort = 1901;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQHost);
        factory.setPort(rabbitPort);
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(rabbitMQQueue, false, false, false, null);
            channel.basicPublish("", rabbitMQQueue, null, result.getBytes());
            System.out.println("Sent result to RabbitMQ: " + result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void processAndStore(String message) {

        redisTemplate.opsForValue().set("real-time-weather", message);
        System.out.println("Processed and stored message: " + message);
    }
    public String performCalculations() {
        double avgTimestamp = 0;
        double avgTemperature = 0;
        double avgPressure = 0;
        double avgHumidity = 0;
        double avgPrecipitation = 0;
        double avgWindSpeed = 0;
        double avgWindDirection = 0;

        for (JsonNode jsonNode : messageBuffer) {
            avgTimestamp += jsonNode.get("timestamp").asDouble();
            avgTemperature += jsonNode.get("temperature").asDouble();
            avgPressure += jsonNode.get("pressure_pa").asDouble();
            avgHumidity += jsonNode.get("humidity_%").asDouble();
            avgPrecipitation += jsonNode.get("precipitation_mm").asDouble();
            avgWindSpeed += jsonNode.get("wind_speed_mps").asDouble();
            avgWindDirection += jsonNode.get("wind_direction_deg").asDouble();
        }

        int bufferSize = messageBuffer.size();
        avgTimestamp /= bufferSize;
        avgTemperature /= bufferSize;
        avgPressure /= bufferSize;
        avgHumidity /= bufferSize;
        avgPrecipitation /= bufferSize;
        avgWindSpeed /= bufferSize;
        avgWindDirection /= bufferSize;

        // Create a JSON object for the average values
        String avgResult = String.format("{\"avg_timestamp\": %f, \"avg_temperature\": %f, " +
                        "\"avg_pressure\": %f, \"avg_humidity\": %f, \"avg_precipitation\": %f, " +
                        "\"avg_wind_speed\": %f, \"avg_wind_direction\": %f}", avgTimestamp, avgTemperature,
                avgPressure, avgHumidity, avgPrecipitation, avgWindSpeed, avgWindDirection);

        return avgResult;
    }
    private JsonNode convertStringToJsonNode(String jsonString) {
        try {
            return new ObjectMapper().readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to JsonNode", e);
        }
    }
}