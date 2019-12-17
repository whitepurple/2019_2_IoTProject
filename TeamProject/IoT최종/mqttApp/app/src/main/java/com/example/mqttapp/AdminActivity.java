package com.example.mqttapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class AdminActivity extends AppCompatActivity {
    private MqttClient mqttClient;
    TextView location, max_car;
    Button publish_btn, renew_btn;
    String public_topic = "parking/Parking01/maxCar";
    String sub_topic = "parking/Parking01/carNum";
    String sub_topic2 = "parking/Parking01/location";
    String num, num2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        max_car=findViewById(R.id.max_car);
        location=findViewById(R.id.location_go);
        publish_btn = findViewById(R.id.publish_btn); //만차 요청
        try{connectMqtt();}catch(Exception e){
            Log.d("TAGGG","MqttConnect Error");
        }
        try {
            mqttClient.publish("parking/admin", new MqttMessage("car".getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
        publish_btn.setOnClickListener(new View.OnClickListener() {
            //최대 주차 차량 대수 늘리기
            @Override
            public void onClick(View view) {
                try {
                    mqttClient.publish(public_topic, new MqttMessage("Max".getBytes()));

                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });/*
        renew_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                max_car.setText("현재차 : " + num);
            }
        });*/
    }
    public void setTText(String n){
        max_car.setText("현재차 : " + num);

    }
    private void connectMqtt() throws Exception {
        mqttClient = new MqttClient("tcp://192.168.137.81:1883", MqttClient.generateClientId(), null);
        mqttClient.connect();
        mqttClient.subscribe(sub_topic);//현재 차 수 확인
        mqttClient.subscribe(sub_topic2);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("TAGGG", "Mqtt ReConnect");
                try {
                    connectMqtt();
                } catch (Exception e) {
                    Log.d("TAGGG", "MqttReConnect Error");
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if(topic.equals(sub_topic)){
                    num = new String(message.getPayload());
                    Log.d("TAG", num);
                    setTText(num);
                }
                else if(topic.equals(sub_topic2)){

                    location.setText(new String(message.getPayload()));
                }


                //여기서 메세지 받았을 떄 이벤트 처리
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    @Override
    protected void onStop() {
        try {

            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onStop();

    }
}
