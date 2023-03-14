package org.mediasoup.droid.demo;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class HomePage extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private Button BlueTooth;
    private Button Camera;

    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_activity);

        BlueTooth = (Button) findViewById(R.id.bluetooth);
        Camera = (Button) findViewById(R.id.camera);

        textToSpeech = new TextToSpeech(this,this);
        String BlueToothBtnStr = BlueTooth.getText().toString();
        String CameraBtnStr = Camera.getText().toString();

        BlueTooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //设置音调，值越大声音越尖（女声），值小则为男声，1.0是常规
                textToSpeech.setPitch(1.0f);
                //设置语速
                textToSpeech.setSpeechRate(1.0f);
                play(BlueToothBtnStr);
            }
        });

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //设置音调，值越大声音越尖（女声），值小则为男声，1.0是常规
                textToSpeech.setPitch(1.0f);
                //设置语速
                textToSpeech.setSpeechRate(1.0f);
                play(CameraBtnStr);
            }
        });

        BlueTooth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent toBlueTooth = new Intent();
                Intent startService = new Intent();
                startService.setClass(HomePage.this,LocationService.class);
                toBlueTooth.setClass(HomePage.this,BlueToothActivity.class);
                startService(startService);
                startActivity(toBlueTooth);
                finish();
                play("选择成功");
                return false;
            }
        });

        Camera.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent toRoom = new Intent();
                toRoom.setClass(HomePage.this,RoomActivity.class);
                startActivity(toRoom);
                finish();
                play("选择成功");
                return false;
            }
        });

    }

    @Override
    public void onInit(int status){
        if(status == TextToSpeech.SUCCESS){
            int result = textToSpeech.setLanguage(Locale.CHINA);
            if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE && result != TextToSpeech.LANG_AVAILABLE){
                Toast.makeText(this, "不支持使用TTS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void play(String string){
        if(TextUtils.isEmpty(string)){
            Toast.makeText(this, "字符串为空", Toast.LENGTH_SHORT).show();
        } else if(textToSpeech != null && !textToSpeech.isSpeaking()){
            textToSpeech.speak(string,TextToSpeech.QUEUE_ADD,null);
            Log.d("Language","speak已经运行");
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!textToSpeech.isSpeaking()){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            Log.d("Language:","语音读完");
        }
    }

}
