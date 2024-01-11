import paho.mqtt.client as mqtt
import time

# Define the MQTT broker address and port
broker_address = "172.31.252.134"
broker_port = 2402

# Define the topic to subscribe to
topic = "your_topic"

# Number of messages to receive before measuring duration
messages_per_batch = 30

# Variable to store the start time of receiving a batch
start_time = None

# Counter for received messages in the current batch
received_messages = 0

# Callback when a message is received from the broker
def on_message(client, userdata, msg):
    global start_time, received_messages

    # Start measuring time when receiving the first message in a batch
    if received_messages == 0:
        start_time = time.time()

    #print(f"Received message on topic '{msg.topic}': {msg.payload.decode()}")

    received_messages += 1

    # If received the desired number of messages, calculate and print duration
    if received_messages == messages_per_batch:
        duration = time.time() - start_time
        print(f"Received {messages_per_batch} messages in {duration:.4f} seconds")
        received_messages = 0  # Reset the counter

# Callback when the client connects to the broker
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribe to the topic when connected
    client.subscribe(topic)

# Create an MQTT client instance
client = mqtt.Client()

# Set the callback functions
client.on_connect = on_connect
client.on_message = on_message
client.tls_set(ca_certs="../kingsman.crt")
client.tls_insecure_set(True)
# Connect to the broker
client.connect(broker_address, broker_port, 60)

# Loop to maintain the connection and handle messages
client.loop_forever()
