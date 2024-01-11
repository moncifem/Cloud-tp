import pika
import json
from faker import Faker
import random
import time

rabbitmq_host = "172.31.250.226"
rabbitmq_port = 5672
rabbitmq_queue = "weather_queue"
rabbitmq_user = "kingsman"
rabbitmq_password = "kingsman"

fake = Faker()

# Connect to RabbitMQ
credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_password)
connection = pika.BlockingConnection(
    pika.ConnectionParameters(host=rabbitmq_host, port=rabbitmq_port, credentials=credentials)
)
channel = connection.channel()
channel.queue_declare(queue=rabbitmq_queue, durable=True)

interval_seconds = 10

try:
    while True:
        weather_data = {
            "timestamp": int(time.time()),
            "pressure_pa": random.uniform(90000, 110000),
            "wind_speed_mps": random.uniform(0, 20),
            "wind_direction_deg": random.uniform(0, 360),
            "location": fake.city(),
        }

        # Convert the data to JSON
        json_data = json.dumps(weather_data)

        # Publish data to RabbitMQ queue
        channel.basic_publish(
            exchange="",
            routing_key=rabbitmq_queue,
            body=json_data,
            properties=pika.BasicProperties(delivery_mode=2),
        )
        print(f"Weather data published to RabbitMQ: {json_data}")

        time.sleep(interval_seconds)

except KeyboardInterrupt:
    print("Producer script terminated by user.")
finally:
    connection.close()