package org.mediasoup.droid.demo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.mediasoup.droid.demo.utils.LocationPermissionUtil;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    Button loginbutton;
    TextToSpeech textToSpeech;
    LocationPermissionUtil locationPermissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        loginbutton = (Button) findViewById(R.id.LoginButton);
        String LoginBtnStr = loginbutton.getText().toString();
        textToSpeech = new TextToSpeech(this,this);

        locationPermissionUtil = new LocationPermissionUtil(LoginActivity.this);
        locationPermissionUtil.getVersion();

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //设置音调，值越大声音越尖（女声），值小则为男声，1.0是常规
                textToSpeech.setPitch(1.0f);
                //设置语速
                textToSpeech.setSpeechRate(1.0f);
                play(LoginBtnStr);
            }
        });

        loginbutton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                play("请验证指纹");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(LoginActivity.this)
                            .setTitle("指纹验证")
                            .setNegativeButton("使用密码验证", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("TAG", "cancel");
                                }
                            }).build();
                    biometricPrompt.authenticate(new CancellationSignal(), getMainExecutor(), authenticationCallback);
                }
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


    BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            play("指纹验证错误次数过多，请稍后重试");
            Log.d("TAG", "onAuthenticationError errorCode: " + errorCode + " errString: " + errString);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            Log.d("TAG", "onAuthenticationHelp helpCode:" + helpCode + "helpString: " + helpString);
        }

        @Override
        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            play("指纹验证成功，登陆成功");
            Intent toHomePage = new Intent();
            toHomePage.setClass(LoginActivity.this,HomePage.class);
            startActivity(toHomePage);
            finish();
            Log.d("TAG", "验证成功");
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            play("指纹验证失败，请重新验证");
            Log.d("TAG", "onAuthenticationFailed");
        }
    };

}
