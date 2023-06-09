package org.mediasoup.droid.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;

public class DeviceListActivity extends Activity implements TextToSpeech.OnInitListener {
    // Debugging  
    private static final String TAG = "DeviceListActivity";  
    private static final boolean D = true;  
  
    // Return Intent extra  
    public static String EXTRA_DEVICE_ADDRESS = "device_address";  
  
    // Member fields  
    private BluetoothAdapter mBtAdapter;  
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;  
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    TextToSpeech textToSpeech;
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
  
        // Setup the window  
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
        setContentView(R.layout.device_list);  
  
        // Set result CANCELED incase the user backs out  
        setResult(Activity.RESULT_CANCELED);  
  
        Log.d(TAG, "onCreate");
        textToSpeech = new TextToSpeech(this,this);
        
        // Initialize the button to perform device discovery  
        Button scanButton = (Button) findViewById(R.id.button_scan);  
        scanButton.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
                String scanButtonStr = scanButton.getText().toString();
                play(scanButtonStr);
            }  
        });
        scanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                doDiscovery();
                scanButton.setText("scanning......");
                play("选择成功");
                return false;
            }
        });
  
        // Initialize array adapters. One for already paired devices and  
        // one for newly discovered devices  
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);  
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);  
  
        // Find and set up the ListView for paired devices  
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);  
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);  
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setOnItemLongClickListener(mDeviceLongClickListener);
  
        // Find and set up the ListView for newly discovered devices  
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);  
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);  
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnItemLongClickListener(mDeviceLongClickListener);
  
        // Register for broadcasts when a device is discovered  
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);  
        this.registerReceiver(mReceiver, filter);  
  
        // Register for broadcasts when discovery has finished  
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  
        this.registerReceiver(mReceiver, filter);  
  
        // Get the local Bluetooth adapter  
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();  
  
        // Get a set of currently paired devices  
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        //Set<BluetoothDevice> newDevices = mBtAdapter.get
  
        // If there are paired devices, add each one to the ArrayAdapter  
        if (pairedDevices.size() > 0) {  
            findViewById(R.id.device_list_line1).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {  
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());  
            }  
        } else {  
            String noDevices = getResources().getText(R.string.none_paired).toString();  
            mPairedDevicesArrayAdapter.add(noDevices);  
        }  
    }  
  
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
  
        // Make sure we're not doing discovery anymore  
        if (mBtAdapter != null) {  
            mBtAdapter.cancelDiscovery();  
        }  
  
        // Unregister broadcast listeners  
        this.unregisterReceiver(mReceiver);


        if(!textToSpeech.isSpeaking()){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            Log.d("Language:","语音读完");
        }
    }  
  
    /** 
     * Start device discover with the BluetoothAdapter 
     */  
    private void doDiscovery() {  
        if (D) Log.d(TAG, "doDiscovery()");  
  
        // Indicate scanning in the title  
        setProgressBarIndeterminateVisibility(true);  
        setTitle(R.string.scanning);  

        findViewById(R.id.device_list_line2).setVisibility(View.VISIBLE);
  
        // If we're already discovering, stop it  
        if (mBtAdapter.isDiscovering()) {  
            mBtAdapter.cancelDiscovery();  
        }  
  
        // Request discover from BluetoothAdapter  
        mBtAdapter.startDiscovery();  
    }  
  
    // The on-click listener for all devices in the ListViews  
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {  
        public void onItemClick(AdapterView<?> av, View view, int arg2, long arg3) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(0,info.length() - 17);
            play(address);
        }  
    };

    private AdapterView.OnItemLongClickListener mDeviceLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
            play("选择成功");
            return false;
        }
    };
  
    // The BroadcastReceiver that listens for discovered devices and  
    // changes the title when discovery is finished  
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
  
            // When discovery finds a device  
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  
                // Get the BluetoothDevice object from the Intent  
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                // If it's already paired, skip it, because it's been listed already  
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());  
                }  
            // When discovery is finished, change the Activity title  
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  
                setProgressBarIndeterminateVisibility(false);  
                setTitle(R.string.select_device);  
                if (mNewDevicesArrayAdapter.getCount() == 0) {  
                    String noDevices = getResources().getText(R.string.none_found).toString();  
                    mNewDevicesArrayAdapter.add(noDevices);  
                }  
            }  
        }  
    };


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

    /*@Override
    public void onDestroy(){
        super.onDestroy();
        if(!textToSpeech.isSpeaking()){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            Log.d("Language:","语音读完");
        }
    }*/
  
}  