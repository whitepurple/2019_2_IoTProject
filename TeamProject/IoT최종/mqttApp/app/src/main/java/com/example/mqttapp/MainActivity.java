package com.example.mqttapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    private MqttClient mqttClient;
    Button button, button2;
    Intent intent, intent_admin;
    EditText num_edit;
    String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button); //admin
        button2 = findViewById(R.id.nextactivity);
        num_edit = findViewById(R.id.num_edit);
        intent = new Intent(this, ParkingActivity.class);
        intent_admin = new Intent(this, AdminActivity.class);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startActivity(intent_admin);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    name = num_edit.getText().toString();
                    Log.d("TAGGG", name);
                    intent.putExtra("topic", name);
                    mqttClient.publish("parking/user/"+num_edit.getText().toString(), new MqttMessage("R".getBytes()));
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        try{connectMqtt();}catch(Exception e){
            Log.d(TAG,"MqttConnect Error");
        }

    }
    private void connectMqtt() throws Exception {
        mqttClient = new MqttClient("tcp://192.168.137.81:1883", MqttClient.generateClientId(), null);
        mqttClient.connect();

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.d(TAG, "Mqtt ReConnect");
                        try {
                            connectMqtt();
                        } catch (Exception e) {
                Log.d(TAG, "MqttReConnect Error");
            }
        }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {


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
            mqttClient.publish("parking/user/garbage", new MqttMessage("DD".getBytes()));
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onStop();

    }
    @Override
    protected void onStart() {
        try{connectMqtt();}catch(Exception e){
            Log.d(TAG,"MqttConnect Error");
        }
        super.onStart();
    }
}
