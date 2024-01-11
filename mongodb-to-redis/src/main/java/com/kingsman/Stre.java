package com.kingsman;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;


public class Stre {

    public static void main(String[] args) throws InterruptedException {
        //final String REDIS_SERVER_HOST = "192.168.10.19";
        final String REDIS_SERVER_HOST = "172.31.252.134";
        final int REDIS_SERVER_PORT = 6379;

        Jedis jedis = new Jedis(REDIS_SERVER_HOST, REDIS_SERVER_PORT);

        //String connectionString = "mongodb://192.168.10.17:27017";
        String connectionString = "mongodb://172.31.252.134:270";
        String databaseName = "meteo";
        String collectionName = "results";

        String postgresUrl = "jdbc:postgresql://172.31.252.134:432/postgres";
        //String postgresUrl = "jdbc:postgresql://192.168.10.14:5432/postgres";
        String postgresUser = "postgres";
        String postgresPassword = "uuuu";
        String postgresQuery = "SELECT * FROM weatherConfig LIMIT 1";

        Document old = null;
        while (true){
            Thread.sleep(500);
            try (MongoClient mongoClient = MongoClients.create(connectionString)) {
                Connection connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(postgresQuery);
                // Connect to the database
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                ResultSet resultSet = preparedStatement.executeQuery();
                int id ;
                int temperatureMin = 0;
                float humidityMax=0 ;
                int windSpeedMax=0 ;
                int rainMax=0 ;
                while (resultSet.next()) {
                    id = resultSet.getInt("id");
                    temperatureMin = resultSet.getInt("temperature-min");
                    humidityMax = resultSet.getFloat("humidity-max");
                    windSpeedMax = resultSet.getInt("wind-speed-max");
                    rainMax = resultSet.getInt("rain-max");


                }

                // Connect to the collection
                MongoCollection<Document> collection = database.getCollection(collectionName);

                // Sort by a timestamp field in descending order to get the latest document
                Bson sort = Sorts.descending("avg_timestamp");
                // Find the latest document in the collection
                Document latestDocument = collection.find().sort(sort).first();

                // Print the latest document
                if (latestDocument != null && !latestDocument.equals(old)) {
                    System.out.println(latestDocument.toJson());

                    // Get the averages
                    // comparaison
                    double avgTemperature = latestDocument.getDouble("avg_temperature");
                    double avgHumidity = latestDocument.getDouble("avg_humidity");
                    double avgWindSpeed = latestDocument.getDouble("avg_wind_speed");
                    double avgPrecipitation = latestDocument.getDouble("avg_precipitation");

                    // Perform your custom comparisons
                    boolean temperatureComparison = avgTemperature >= temperatureMin;
                    boolean humidityComparison = avgHumidity <= humidityMax;
                    boolean windSpeedComparison = avgWindSpeed <= windSpeedMax;
                    boolean precipitationComparison = avgPrecipitation <= rainMax;

                    // Example: If any comparison is false, send to Redis
                    if (!temperatureComparison || !humidityComparison || !windSpeedComparison || !precipitationComparison) {
                        jedis.set("watering-command-automatic", "on");
                        System.out.println("on");
                    }
                }
                old = latestDocument;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
