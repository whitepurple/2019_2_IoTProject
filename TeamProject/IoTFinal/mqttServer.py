# -*- coding: utf-8 -*- 
import spidev
import RPi.GPIO as gpio
import time
import paho.mqtt.client as mqtt

Parking01State = 'TTTTT'
Parking01NumState = ['01k0101','07g7560','11p1234','47m6919','05y7831']
subUserList = []
count=0
maxCount = 5
eOpened = False
oOpened = False
camNum = '01'

def on_connect(client, userdata, flags, rc):
    print("connected with result code " + str(rc))
    client.subscribe("parking/Parking01/#")
    client.subscribe("parking/user/#")
    client.subscribe("parking/admin")
    
def on_message(client, userdata, msg):
    topic = (str(msg.topic)).split('/')
    mg = msg.payload
    global count
    global maxCount
    global eOpened
    global oOpened
    global Parking01State
    if topic[1] == 'admin':
        client.publish("parking/Parking01/carNum",count)
    elif topic[1] == 'user':
        carNum = topic[2]
        if mg == 'R':
            if not(carNum in subUserList):
                subUserList.append(carNum)
                print subUserList
            
        elif mg == 'DD':
            renameParkingState()
            if carNum in subUserList:
                subUserList.remove(carNum)
            print subUserList
     
        elif len(topic)>3 and topic[3] == 'getImage':
            print('getImage')
            cn = topic[2]
            f = open(str(mg[7:])+'image.jpg','rb')
            fileContent = f.read()
            byteArr = bytearray(fileContent)
            client.publish("parking/app/"+cn+'/image',byteArr)
            f.close()
            print('imagePublished')
            
    elif topic[2] == 'go':
        print "go"
        adminMessage = camNum + " -> " + str(mg)
        print adminMessage
        client.publish("parking/Parking01/location",adminMessage)
        print 'goend'
            
    elif topic[2] == 'cameraImage':
        print('cameraimage')
        f = open(topic[3]+'image.jpg','wb')
        f.write(mg)
        f.close()
        print(topic[3]+'image received')
        
    elif topic[2] == 'count':
        if mg == "ecount" and not eOpened:
            if count < maxCount :
                count=count+1
                if count == maxCount:
                    client.publish("parking/Parking01/ready","Full")
                    print 'full'
                else:
                    client.publish("parking/Parking01/ready","Ready")
                    print 'ready'
            eOpened = True
        elif mg == "ocount" and not oOpened:
            count=count-1
            if count<0:
                count = 0
            oOpened = True
            client.publish("parking/Parking01/ready","Ready")
            print 'ready'
        elif mg == 'edown':
            if eOpened:
                client.publish("parking/Parking01/carNum",count)
                print count
            eOpened = False
        elif mg == 'odown':
            if oOpened:
                client.publish("parking/Parking01/carNum",count)
                print count
            oOpened = False
        
    elif topic[2] == 'maxCar':
        if(msg.payload =="Max"):
            maxCount = 7
        client.publish("parking/Parking01/carNum",count)
        if maxCount > count:    
            client.publish("parking/Parking01/ready","Ready")
            print 'Extend ready'
            print count
        
    elif topic[2] == 'cds':
        point = int(topic[3])-1
        Parking01State = Parking01State[:point] + mg + Parking01State[point+1:]
        print Parking01State
        renameParkingState()
        
def renameParkingState():
    for i in subUserList:
            ps = Parking01State
            if i in Parking01NumState:
                point = Parking01NumState.index(i)
                if Parking01State[point] == 'F':
                    ps = ps[:point] + 'M' + ps[point+1:]
            client.publish("parking/app/"+str(i),ps)
            
client = mqtt.Client("parkingServer")
client.on_connect = on_connect
client.on_message = on_message
client.connect("localhost")

try:
    client.loop_forever()
    
except KeyboardInterrupt:
    print("Finished!")
    client.unsubscribe(["parking/Parking01/#", "parking/user/#", "parking/admin"])
    client.disconnect()