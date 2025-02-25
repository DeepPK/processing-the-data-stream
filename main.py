from flask import Flask, render_template
from flask_socketio import SocketIO
import threading
import json
from datetime import datetime

from kafka import KafkaConsumer

app = Flask(__name__)
app.config['SECRET_KEY'] = '123'
socketio = SocketIO(app, async_mode='eventlet')

sensor_data = {}
data_lock = threading.Lock()

def kafka_consumer_thread():
    consumer = KafkaConsumer(
        'ProcessedData',
        bootstrap_servers='localhost:9092',
        auto_offset_reset='earliest',
        value_deserializer=lambda a: json.loads(a.decode('utf-8'))
    )

    for message in consumer:
        with data_lock:
            data = message.value
            sensor_id = str(data['sensor_id']+1)

            sensor_data[sensor_id] = {
                'count1': data['count1'],
                'count2': data['count2'],
                'timegreen1': data['timegreen1'],
                'timegreen2': data['timegreen2'],
                'new_green1': data['new_green1'],
                'new_green2': data['new_green2']
            }

            socketio.emit('sensor_update', {
                'sensor_id': sensor_id,
                'data': sensor_data[sensor_id],
                'timestamp': datetime.now().isoformat()
            })

@app.route('/')
def index():
    return render_template('index.html',
                         dashboard_title="Sensor Traffic Monitoring Dashboard",
                         welcome_message="1 and 2 is number of tracks on crossroad. <Cars> is number of cars on crossroads in current time. <Old Time> is default time of green light. <New Time> is optimized green time (When green is on one track, another is red).")

@socketio.on('connect')
def handle_connect():
    with data_lock:
        for sensor_id, data in sensor_data.items():
            socketio.emit('sensor_update', {
                'sensor_id': sensor_id,
                'data': data,
                'timestamp': datetime.now().isoformat()
            })

if __name__ == '__main__':
    threading.Thread(target=kafka_consumer_thread, daemon=True).start()
    socketio.run(app, host='0.0.0.0', port=5001, allow_unsafe_werkzeug=True, debug=True)
