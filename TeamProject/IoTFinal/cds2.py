# -*- coding: utf-8 -*- 
import paho.mqtt.client as mqtt
import RPi.GPIO as gpio
import spidev
import time 

connectHost = "192.168.0.11"
spi = spidev.SpiDev()
spi.open(0,0)
spi.max_speed_hz = 976000
light_channel = 2
isParking = True
boolean = {True:'T', False:'F'}

cdsLowLimit = 0.1  #주차 여부 파악을 위한 조도 값 변경 기준
cdsHighLimit = 0.3  #주차 여부 파악을 위한 조도 값 변경 기준
pointer = 2
cdsList = [0,0,0]

def getPoint():
    global pointer
    pointer = (pointer+1+len(cdsList))%len(cdsList)
    return pointer

def currentParking(v):
    i = getPoint()
    cdsList[i] = v
    if sum(cdsList) < cdsLowLimit*len(cdsList):
        print("Parked"+str(light_volts))
        return False
    elif sum(cdsList) > cdsHighLimit*len(cdsList):
        print("Empty"+str(light_volts))
        return True
    else:
        return isParking
        
def readChannel(channel):
    adc = spi.xfer2([1, (8 + channel) << 4, 0])
    adc_out = ((adc[1] & 3) << 8) + adc[2]
    return adc_out

def convert2volts(data, places):
    volts = (data * 3.3) / float(1023)
    volts = round(volts, places)
    return volts

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

def on_publish(client, userdata, mid):
    print("message published: "+str(light_volts))

mqttc = mqtt.Client()
mqttc.on_connect = on_connect
mqttc.on_publish = on_publish
mqttc.connect(connectHost)
mqttc.loop_start()

try:
    while True:
        light_level = readChannel(light_channel)
        light_volts = convert2volts(light_level, 2)
        if isParking != currentParking(light_volts):
            isParking = not isParking
            mqttc.publish("parking/Parking01/cds/3", boolean[isParking])
        time.sleep(1)

except KeyboardInterrupt:
    print("Finished")
    spi.close()