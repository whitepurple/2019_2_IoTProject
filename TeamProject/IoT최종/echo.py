import paho.mqtt.client as mqtt
import RPi.GPIO as gpio
import time

connectHost = "192.168.0.11"

trig_pin1 = 5
echo_pin1 = 6
trig_pin2 = 19
echo_pin2 = 26

motor_pin1 = 22
motor_pin2 = 17

led_pin1 = 20
led_pin2 = 21
count= 0 
gpio.setmode(gpio.BCM)
gpio.setup(trig_pin1, gpio.OUT)
gpio.setup(echo_pin1, gpio.IN)
gpio.setup(trig_pin2, gpio.OUT)
gpio.setup(echo_pin2, gpio.IN)

gpio.setup(led_pin1, gpio.OUT)
gpio.setup(led_pin2, gpio.OUT)


gpio.setup(motor_pin1, gpio.OUT)
p1 = gpio.PWM(motor_pin1, 50)
p1.start(16)

gpio.setup(motor_pin2, gpio.OUT)
p2 = gpio.PWM(motor_pin2, 50)
p2.start(6)
flag = True

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    client.subscribe("parking/Parking01/ready")
    
def on_message(client, userdata, msg):
    global state, flag
    state = str(msg.payload)
    if(state == "Full"):
        print "full"
        flag = False
    elif(state =="Ready"):
        print "ready"
        flag = True
  
mqttc = mqtt.Client("echo")
mqttc.on_connect = on_connect
mqttc.on_message = on_message

mqttc.connect(connectHost)
mqttc.loop_start()
disLimit = 5

try:
    gpio.output(led_pin2, True)##Green
    gpio.output(led_pin1, False)
    while True:
        gpio.output(trig_pin1, False)
        time.sleep(1)
        gpio.output(trig_pin1, True)
        time.sleep(0.00001)
        gpio.output(trig_pin1, False)
        
        while gpio.input(echo_pin1) == 0:
            pulse_start = time.time()
        while gpio.input(echo_pin1) == 1:
            pulse_end = time.time()
            
        pulse_duration = pulse_end - pulse_start
        distance = pulse_duration * 34000 / 2
        distance1 = round(distance, 2)
    
        #print("Enter Distance : "+str(distance1))
    
        gpio.output(trig_pin2, False)
        time.sleep(1)
        gpio.output(trig_pin2, True)
        time.sleep(0.00001)
        gpio.output(trig_pin2, False)
        while gpio.input(echo_pin2) == 0:
            pulse_start = time.time()
        while gpio.input(echo_pin2) == 1:
            pulse_end = time.time()
        pulse_duration = pulse_end - pulse_start
        distance2 = pulse_duration * 34000 / 2
        distance2 = round(distance2, 2)
        #print("Out Distance : "+str(distance2))
        
        if distance1 < disLimit:
            print("Entrance Detected")
            mqttc.publish("parking/Parking01/count", "ecount")
            if flag: 
                p1.ChangeDutyCycle(6.5)
                time.sleep(1)
        else:
            print("Entrance Detecting...")
            mqttc.publish("parking/Parking01/count", "edown")
            time.sleep(0.5)
            p1.ChangeDutyCycle(16)
    
        if distance2 < disLimit:
            print("Exit Detected")
            mqttc.publish("parking/Parking01/count", "ocount")
            p2.ChangeDutyCycle(2)
            time.sleep(1)
        else:
            print("Exit Detecting...")
            mqttc.publish("parking/Parking01/count", "odown")
            time.sleep(0.5)
            p2.ChangeDutyCycle(6)
                
        if flag:
            gpio.output(led_pin2, True)##Green
            gpio.output(led_pin1, False)
        else:
            gpio.output(led_pin1, True)
            gpio.output(led_pin2, False)


except KeyboardInterrupt:
    gpio.cleanup()
    mqttc.loop_stop()
    client.unsubscribe("parking/Parking01/ready")
    mqttc.disconnect()