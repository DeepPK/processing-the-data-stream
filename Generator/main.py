from flask import Flask, jsonify
from confluent_kafka import Producer
import threading
import time
import json
import random

app = Flask(__name__)

conf = {
    'bootstrap.servers': 'localhost:9092',
}

producer = Producer(**conf)

i = -1
count1 = random.randint(1,20)
count2 = random.randint(1,20)
timegreen1 = random.randint(1,100)
timegreen2 = random.randint(1,100)
running = False

def generate_data():
    global i
    i+=1
    if i == 10:
        i = 0
    return {
        "sensor_id": i,
        "count1": random.randint(1,20),
        "count2": random.randint(1,20),
        "timegreen1": timegreen1,
        "timegreen2": timegreen2,
    }

def kafka_producer_loop():
    while running:
        try:
            data = json.dumps(generate_data())

            producer.produce('TraficData', value=data)

            producer.poll(0)
            time.sleep(0.1)
        except Exception as e:
            print(f"Error in Kafka producer loop: {e}")
    print("Kafka producer loop stopped.")

@app.route('/start', methods=['POST'])
def start_producer():
    global running
    if not running:
        running = True
        threading.Thread(target=kafka_producer_loop, daemon=True).start()
        return jsonify({"status": "Kafka producer started"}), 200
    else:
        return jsonify({"status": "Kafka producer is already running"}), 200

@app.route('/stop', methods=['POST'])
def stop_producer():
    global running
    running = False
    return jsonify({"status": "Kafka producer stopped"}), 200

@app.route('/status', methods=['GET'])
def status():
    return jsonify({"status": "running" if running else "stopped"}), 200

if __name__ == '__main__':
    threading.Thread(target=kafka_producer_loop, daemon=True).start()
    app.run(port=5002, debug=True)
