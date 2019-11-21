import spidev
import RPi.GPIO as gpio
import time
import paho.mqtt.client as mqtt

def on_connect(client, userdata, flags, rc):
    print("connected with result code " + str(rc))

    client.subscribe(("parking/appLeavingCompleteInfo"))
    
    
def on_message(client, userdata, msg):
    if(msg.topic == "parking/appLeavingCompleteInfo"):
        global LeaveComplete
        LeaveComplete = msg.payload
        print(LeaveComplete)
    
    
    
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect("localhost")



try:
    client.loop_forever()
    
            
except KeyboardInterrupt:
    print("Finished!")
    client.unsubscribe("parking/appLeavingCompleteInfo")
    client.disconnect()

