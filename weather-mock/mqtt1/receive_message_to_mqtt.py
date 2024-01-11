import paho.mqtt.client as mqtt
import pymongo
import json

mongo_host = "172.31.252.134"
mongo_port = 27017  
mongo_database = "meteo"
mongo_collection = "weather_data"

mqtt_broker_address = "172.31.252.134"
mqtt_broker_port = 2302
mqtt_topic = "weather_topic"

client = pymongo.MongoClient(mongo_host, mongo_port)
db = client[mongo_database]
collection = db[mongo_collection]


def on_message(client, userdata, msg):
    weather_data = json.loads(msg.payload.decode())

    collection.insert_one(weather_data)
    print(f"Données reçues et insérées: {weather_data}")


client = mqtt.Client()


client.connect(mqtt_broker_address, mqtt_broker_port, 60)


client.subscribe(mqtt_topic)


client.on_message = on_message

print(' En attendant un reponse ...')

client.loop_forever()
