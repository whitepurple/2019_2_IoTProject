import time
import paho.mqtt.client as mqtt
parking = ""
leave = ""
completeLeave =""
applyLeave = ""
def on_connect(client, userdata, flags, rc):
    print("connected with result code " + str(rc))
    client.subscribe("parking/serverApplyInfo")#app subscribe
    client.subscribe("parking/serverIsParking")#camera.py
    client.subscribe("parking/serverIsLeave")#camera.py 
    client.subscribe(("parking/serverCompleteLeave"))#camera.py
    
    
def on_message(client, userdata, msg):
    if(msg.topic == "parking/serverIsParking"):
        global parking
        parking = msg.payload
        
        client.publish("parking/appParkingInfo",parking)
    elif(msg.topic == "parking/serverIsLeave"):
        global leave
        leave = msg.payload
        client.publish("parking/appLeaveInfo",parking)
    elif(msg.topic == "parking/serverApplyInfo"):
        global applyLeave
        applyLeave = "OK"#msg.payload
        client.publish("parking/adminApplyLeaveInfo", applyLeave)
    #elif(msg.topic == "parking/serverCompleteLeave"):
    global completeLeave
    completeLeave = "OK"
    client.publish("parking/appLeavingCompleteInfo",completeLeave)
        
   
    print(parking+"\n"+leave+"\n"+completeLeave)
    
    
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect("localhost")



try:
    client.loop_forever()
    
            
except KeyboardInterrupt:
    print("Finished!")
    client.unsubscribe(["parking/isParking", "parking/isLeave","parking/completeLeave", "parking/app", "parking/app2"])
    client.disconnect()