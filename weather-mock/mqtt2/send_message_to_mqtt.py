import paho.mqtt.client as mqtt
import threading
import time

# Define the MQTT broker address and port
broker_address = "172.31.252.134"
broker_port = 2402

# Define the topic and message to send
topic = "your_topic"
message = "Hello, MQTT!"

# Number of messages to send per second
messages_per_second = 60

# Callback when the client connects to the broker
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

# Function to publish messages
def publish_messages():
    while True:
        # Publish the message
        client.publish(topic, message)
        time.sleep(1 / messages_per_second)

# Create an MQTT client instance
client = mqtt.Client()

# Set the callback functions
client.on_connect = on_connect
client.tls_set(ca_certs="../kingsman.crt")
client.tls_insecure_set(True)
# Connect to the broker
client.connect(broker_address, broker_port, 60)

# Create a thread for publishing messages
publish_thread = threading.Thread(target=publish_messages, daemon=True)

# Start the thread
publish_thread.start()

# Loop to maintain the connection and handle messages
client.loop_forever()
