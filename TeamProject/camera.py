import paho.mqtt.client as mqtt
import random
import time

def getParkingMsg():
    msg = "parking Success or Fail"               
    return msg                
def getLeaveMsg():
    msg = "Leaving Ready or Not Ready"
    return msg
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

def on_publish(client, userdata, mid):
    msg_id = mid
   
    
#camera opencv
#opencv result -> parking, levave : Success or Fail

mqttc = mqtt.Client()
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish


mqttc.connect("195.50.137.194")
mqttc.loop_start()

try:
    while True:
        parking = getParkingMsg()
        leaving = getLeaveMsg()
        (result, m_id) = mqttc.publish("parking/appIsParking", parking)
        (result, m_id) = mqttc.publish("parking/appIsLeave", leaving)
        print("parking : %s leaving : %s"%(parking, leaving))
        time.sleep(2)
        
except KeyboardInterrupt:
    print("Finished!")
    mqttc.loop_stop()
    mqttc.disconnect()


