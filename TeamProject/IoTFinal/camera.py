# -*- coding: utf-8 -*- 
from time import sleep
import paho.mqtt.client as mqtt
from picamera import PiCamera

def on_connect(client, userdata, flags, rc):
    print("connected with result code " + str(rc))
    
def on_message(client, userdata, msg):
    print('onmessage')
    
client = mqtt.Client("Parking01Camera")
client.on_connect = on_connect
client.on_message = on_message
client.connect("localhost")
client.loop_start()

try:
    while True :
        cam = PiCamera()
        cam.resolution = (1024,768)
        cam.start_preview()
        sleep(2)
        cam.capture("/home/pi/image.jpg", use_video_port=True)
        cam.stop_preview()
        cam.close()
        print('Image Captured')

        sleep(5)
        f=open('/home/pi/image.jpg','rb')
        fileContent = f.read()
        byteArr = bytearray(fileContent)
        client.publish("parking/Parking01/cameraImage/01",byteArr)
        print('imagepublished')
        f.close()
        sleep(5)
        
except KeyboardInterrupt:
    print("Finished!")
    client.disconnect()
