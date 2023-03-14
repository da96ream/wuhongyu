package org.mediasoup.droid.demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {

    private LocationManager locationManager;

    private String locationProvider;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    MyTimerTask myTimerTask = new MyTimerTask();
    Timer timer = new Timer();

    String locationStr;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    Date date = new Date(System.currentTimeMillis());
    String dateStr = simpleDateFormat.format(date);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate(){
        super.onCreate();

        //获取地理位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if(providers.contains(LocationManager.GPS_PROVIDER)){
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }else{
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取地理位置
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if(location != null){
            //不为空则获取地理位置经纬度
            locationStr = getLocation(location);
            databaseHelper = new DatabaseHelper(LocationService.this);
            //timer.schedule(myTimerTask,1000);
        }
        locationManager.requestLocationUpdates(locationProvider,1000,1,locationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        timer.schedule(myTimerTask,1000,1000*5);
        return flags;
    }

    /**
     * 获取地理位置经度和纬度信息
     * @param location
     */
    private String getLocation(Location location){
        String locationStr = "纬度：" + location.getLatitude() +" "
                + "经度：" + location.getLongitude();
        return locationStr;
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener =  new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新获取
            getLocation(location);

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager!=null){
            //移除监听器
            locationManager.removeUpdates(locationListener);
        }
        timer.cancel();
    }

    public class MyTimerTask extends TimerTask{
        @Override
        public void run(){
            database = databaseHelper.getWritableDatabase();
            database.execSQL("INSERT INTO location (coordinate,date) VALUES ('"+ locationStr +"','"+ dateStr +"')");
            Log.d("LocationService:","MyTimerTask start run.");
        }
    }

}
