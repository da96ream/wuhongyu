package org.mediasoup.droid.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BlueToothActivity extends Activity implements OnClickListener, View.OnLongClickListener, TextToSpeech.OnInitListener {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int MSG_NEW_DATA = 1;
    //蓝牙通用串口UUID
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private List<Integer> mBuffer;
    private static final String TAG = "BlueToothActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;
    private Button mScanBtn;
    private TextView mTextView;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bluetooth_activity);

        textToSpeech = new TextToSpeech(this,this);

        mScanBtn = (Button) findViewById(R.id.scanBtn);
        mScanBtn.setOnClickListener(this);
        mScanBtn.setOnLongClickListener(this);

        mTextView = (TextView) findViewById(R.id.mTextView);
        //获取本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mBuffer = new ArrayList<Integer>();

    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            //startActivityForResult(Intent intent, int requestCode)方法打开新的Activity，
            // 新的Activity 关闭后会向前面的Activity传回数据，数据会被OnActivityResult接收
        }

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            StringBuffer buf = new StringBuffer();
            synchronized (mBuffer){
                for(int i : mBuffer){
                    buf.append((char) i);
                }
                buf.append("\n");
            }
            mTextView.setText(buf.toString());
        }
    };

    /**
     * 在进行界面间的跳转和传递数据的时候，我们有的时候要获得跳转之后界面传递回来的状态，数据等信息。
     * 这个时候，我们不一定需要使用Intent进行跳转回原先设置的界面，而是使用onActivityResult方法就可以解决这个问题。
     * @param requestCode 请求码，对应你在startActivityForResult(Intent intent,int requestCode)中传入,
     *                    该参数的作用是让你在有多个activity返回结果时，能判断是哪一个activity返回
     * @param resultCode 返回码，通过该参数判断子activity返回的状态
     * @param data 用来传递数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled Launch the DeviceListActivity to see
                    // devices and do scan
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                } else {
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    connect(device);
                }
                break;
        }
    }

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    /**
     * 连接外部设备（蓝牙）的线程
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString(SPP_UUID));
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {

                Log.e(TAG, "unable to connect() socket", e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                return;
            }

            mConnectThread = null;

            // Start the connected thread
            // Start the thread to manage the connection and perform
            // transmissions
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        //private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            //OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                //tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    synchronized (mBuffer) {
                        for (int i = 0; i < bytes; i++) {
                            mBuffer.add(buffer[i] & 0xFF);
                        }
                    }
                    mHandler.sendEmptyMessage(MSG_NEW_DATA);
                    Thread.sleep(500);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private boolean isBackCliecked = false;

    //点击两次返回退出应用
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isBackCliecked) {
                Intent BackToLogin = new Intent(BlueToothActivity.this,HomePage.class);
                startActivity(BackToLogin);
                finish();
            } else {
                isBackCliecked = true;
                Toast t = Toast.makeText(this, "Press \'Back\' again to exit.",
                        Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        String mScanBtnStr = mScanBtn.getText().toString();
        play(mScanBtnStr);
    }

    @Override
    public boolean onLongClick(View v){
        isBackCliecked = false;
        if(v == mScanBtn) {
            if (mConnectThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
                mConnectThread.cancel();
                mConnectThread = null;
            }
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            play("选择成功");
        }
        return false;
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
    protected void onDestroy() {
        super.onDestroy();
        if(!textToSpeech.isSpeaking()){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            Log.d("Language:","语音读完");
        }
    }

}
