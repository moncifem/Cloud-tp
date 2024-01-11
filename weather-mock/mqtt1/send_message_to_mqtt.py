import paho.mqtt.client as mqtt
import threading
import time
import json
import random


broker_address = "172.31.252.134"
broker_port = 2302
mqtt_topic = "weather_topic"


messages_per_second = 30


def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))


def publish_messages():
    global previous_temperature, previous_pressure, previous_humidity, previous_precipitation
    
    while True:
        temperature = max(min(previous_temperature + random.uniform(-0.01, 0.01), 40), -2)
        pressure = max(min(previous_pressure + random.uniform(-50, 50), 110000), 90000)
        humidity = max(min(previous_humidity + random.uniform(-0.01 , 0.001), 0.8), 0)
        precipitation = max(min(previous_precipitation + random.uniform(-0.1, 0.1), 50), 0)
        wind_direction= random.uniform(0, 360)
        precipitation_mm= "{:.3f}".format(precipitation)
        pressure_pa="{:.3f}".format(pressure)
        wind_direction_deg="{:.3f}".format(wind_direction)
        humidity_p="{:.3f}".format(humidity)
        temp3="{:.3f}".format(temperature)

        weather_data = {
            "timestamp": int(time.time()),
            "temperature": temp3,
            "pressure_pa": pressure_pa,
            "humidity_%": humidity_p,
            "precipitation_mm": precipitation_mm,
            "wind_speed_mps": random.uniform(0, 20),
            "wind_direction_deg": wind_direction_deg,
        }

        json_data = json.dumps(weather_data)

        client.publish(mqtt_topic, json_data)
        print(f"Données envoyées à MQTT: {json_data}")

        previous_temperature = temperature
        previous_pressure = pressure
        previous_humidity = humidity
        previous_precipitation = precipitation

        time.sleep(1 / messages_per_second)


client = mqtt.Client()


client.on_connect = on_connect
client.tls_set(ca_certs="../kingsman.crt")
client.tls_insecure_set(True)


client.connect(broker_address, broker_port, 60)


previous_temperature = random.uniform(-2, 40)
previous_pressure = random.uniform(90000, 110000)
previous_humidity = random.uniform(0, 100)
previous_precipitation = random.uniform(0, 50)


publish_thread = threading.Thread(target=publish_messages, daemon=True)
publish_thread.start()

client.loop_forever()
