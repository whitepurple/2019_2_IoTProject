package com.example.mqttapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ParkingActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    Button go_btn, camera_btn;
    ImageView p1, p2, p3, p4, p5, carImage;
    TextView tx1;
    private MqttClient mqttClient;
    int count;
    Intent topicIntent;
    String topic = "parking/app/";
    String numName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);
        p1 = findViewById(R.id.imgP1);
        p2 = findViewById(R.id.imgP2);
        p3 = findViewById(R.id.imgP3);
        p4 = findViewById(R.id.imgP4);
        p5 = findViewById(R.id.imgP5);
        tx1 = findViewById(R.id.num_text);
        count=0;
        topicIntent = getIntent();
        numName = topicIntent.getStringExtra("topic");
        tx1.setText(numName);
        carImage = findViewById(R.id.carImage);
        topic = topic+numName;
        go_btn = findViewById(R.id.test);
        camera_btn = findViewById(R.id.camera);
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//carImage 세팅
                try {
                    mqttClient.publish("parking/user/"+numName+"/getImage", new MqttMessage("Parking01".getBytes()));

                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });
        go_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {
                    mqttClient.publish("parking/Parking01/go", new MqttMessage(numName.getBytes()));
                    Toast.makeText(getApplicationContext(), "출차요청완료",Toast.LENGTH_SHORT).show();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });
        try{connectMqtt();}catch(Exception e){
            Log.d(TAG,"MqttConnect Error");
        }
    }
    private void connectMqtt() throws Exception {
        mqttClient = new MqttClient("tcp://192.168.137.81:1883", MqttClient.generateClientId(), null);
        mqttClient.connect();

        mqttClient.subscribe(topic);
        mqttClient.subscribe(topic+"/image");
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
            public void messageArrived(String stopic, MqttMessage message) throws Exception {

                boolean str = stopic.equals(topic);
                boolean str2 = stopic.equals(topic+"/image");

                if(stopic.equals(topic)){
                    String test = new String(message.getPayload());
                    Log.d("TAGGG", test);
                    for (int i = 0; i < test.length(); i++) {
                        if (test.charAt(i) == 'T') {
                            parking(i);
                        } else if (test.charAt(i) == 'F') {
                            noParking(i);
                        } else if (test.charAt(i) == 'M') {
                            myCar(i);
                        }
                    }
                }
                else if(stopic.equals(topic+"/image")){
                    Bitmap image = BitmapFactory.decodeByteArray(message.getPayload(),0,message.getPayload().length-1);
                    carImage.setImageBitmap(image);

                }


                //여기서 메세지 받았을 떄 이벤트 처리
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void noParking(int index){
        switch (index){
            case 0:
                p1.setImageResource(R.mipmap.noparking);
                break;
            case 1:
                p2.setImageResource(R.mipmap.noparking);
                break;
            case 2:
                p3.setImageResource(R.mipmap.noparking);
                break;
            case 3:
                p4.setImageResource(R.mipmap.noparking);
                break;
            case 4:
                Log.d("TAGG", "5F");
                p5.setImageResource(R.mipmap.noparking);
                break;
        }
    }
    public void parking(int index){
        switch (index){
            case 0:
                p1.setImageResource(R.mipmap.parking);
                break;
            case 1:
                p2.setImageResource(R.mipmap.parking);break;
            case 2:
                p3.setImageResource(R.mipmap.parking);break;
            case 3:
                p4.setImageResource(R.mipmap.parking);break;
            case 4:
                Log.d("TAGG", "5T");
                p5.setImageResource(R.mipmap.parking);break;
        }
    }
    public void myCar(int index){
        switch (index){
            case 0:
                p1.setImageResource(R.mipmap.car);
            case 1:
                p2.setImageResource(R.mipmap.car);
            case 2:
                p3.setImageResource(R.mipmap.car);
            case 3:
                p4.setImageResource(R.mipmap.car);
            case 4:
                p5.setImageResource(R.mipmap.car);
        }
    }
    @Override
    protected void onStop() {
        try {
            mqttClient.publish("parking/user/"+numName, new MqttMessage("DD".getBytes()));
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onStop();

    }
}
