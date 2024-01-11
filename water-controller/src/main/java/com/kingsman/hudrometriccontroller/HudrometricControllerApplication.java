package com.kingsman.hudrometriccontroller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import com.rabbitmq.client.*;
@SpringBootApplication
@EnableScheduling
public class HudrometricControllerApplication {

    public static void main(String[] args)  {
        SpringApplication.run(HudrometricControllerApplication.class, args);



}
@RestController
@RequestMapping("/watering")
class WateringController {
    private static final String REDIS_SERVER_HOST = "192.168.10.19";
    private static final int REDIS_SERVER_PORT = 6379;
    String broker = "tcp://192.168.10.25:1122";
    String topic = "watering-command-automatic/topic";
    String rabbitMQHost = "192.168.10.22";
    private Jedis jedis= new Jedis(REDIS_SERVER_HOST, REDIS_SERVER_PORT);






    private static Connection getConnection(String host) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        return factory.newConnection();
    }


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        new Thread(this::checkAndPublishAutomaticChanges).start();
    }



    @GetMapping("/status")
    public String getWateringStatus() {

        // Retrieve the current watering status from Redis
        System.out.println(jedis.get("watering_status"));
        return jedis.get("watering_status");
    }

    @PostMapping("/toggle")
    public String toggleWatering() {

        // Toggle the watering status in Redis
        String currentStatus = jedis.get("watering_status");
        String newStatus = "on".equals(currentStatus) ? "off" : "on";
        jedis.set("watering_status", newStatus);

        // Publish the  command to RabbitMQ
        try (Connection connection = getConnection(rabbitMQHost);
             Channel channel = connection.createChannel()) {
            channel.queueDeclare("commands", false, false, false, null);

            // Publish the message to RabbitMQ
            channel.basicPublish("", "commands", null, lastAutomaticStatus.getBytes());
            System.out.println("Sent to RabbitMQ: " + lastAutomaticStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newStatus;
    }
    private volatile boolean isChangeFromApi = false;
    private String lastAutomaticStatus = "";
    @Async
    public void checkAndPublishAutomaticChanges()  {
        while (true) {
            try {
                // Fetch the current automatic watering status from Redis
                String currentStatus = jedis.get("watering_status-automatic");

                // Check for changes and publish updates if they are from the backend
                if (currentStatus != null && !currentStatus.equals(lastAutomaticStatus) && !isChangeFromApi) {
                    lastAutomaticStatus = currentStatus;

                    // Publish the automatic command to RabbitMQ
                    try (Connection connection = getConnection(rabbitMQHost);
                         Channel channel = connection.createChannel()) {
                        channel.queueDeclare("automatic-commands", false, false, false, null);

                        // Publish the message to RabbitMQ
                        channel.basicPublish("", "automatic-commands", null, lastAutomaticStatus.getBytes());
                        System.out.println("Sent to RabbitMQ: " + lastAutomaticStatus);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // Send the automatic command to Mosquitto
                    try {
                        MqttClient client = new MqttClient(broker, MqttClient.generateClientId());
                        client.connect();

                        MqttMessage mqttMessage = new MqttMessage();
                        mqttMessage.setPayload(currentStatus.getBytes());

                        client.publish(topic, mqttMessage);
                        System.out.println("message delivered to mosquitto "+mqttMessage);
                    } catch (MqttException e) {
                        e.printStackTrace(); // Handle the MQTT exception appropriately
                    }
                    jedis.set("watering_status", lastAutomaticStatus);
                }

                // Reset the flag for the next iteration
                isChangeFromApi = false;

                // Sleep for a specific duration before the next iteration
                Thread.sleep(5000); // Sleep for 5 seconds, adjust as needed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Handle the interruption if needed
            }
        }
    }


}

}