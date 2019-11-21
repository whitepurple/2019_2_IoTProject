package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    private MqttClient mqttClient;
    private MqttAndroidClient c;
    static final String parkingInfo = "parking/appParkingInfo";
    static final String leaveInfo = "parking/appLeaveInfo";
    static final String leavingCompleteLeave = "parking/appLeavingCompleteLeave";
    Button button;
    MemoryPersistence persistence = new MemoryPersistence();
    boolean toggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            connectMqtt();
        } catch (Exception e) {
            Log.d(TAG, "MqttConnect Error");
        }
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MqttMessage message = new MqttMessage("Go".getBytes());
                    mqttClient.publish("parking/serverApplyInfo", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void connectMqtt() throws Exception {//
        mqttClient = new MqttClient("tcp://192.168.0.21:1883", "Parking", null);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        //connOpts.setAutomaticReconnect(true);
        mqttClient.connect(connOpts);

        mqttClient.subscribe(parkingInfo);
        //mqttClient.subscribe(isParking);
        mqttClient.subscribe(leaveInfo);
        //mqttClient.subscribe(leavingCompleteLeave);
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
                toggle = !toggle;
                button.setText(toggle?"PUBLISH":new String(message.getPayload()));
                JSONObject json = new JSONObject(new String(message.getPayload(), "UTF-8"));
                Log.d(TAG, json.getString("iddd") + json.getString("content"));
                //여기서 메세지 받았을 떄 이벤트 처리
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                toggle = !toggle;
                button.setText(toggle?"PUBLISH":"yapyap");
            }
        });
    }
}

