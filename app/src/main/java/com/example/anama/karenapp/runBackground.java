package com.example.anama.karenapp;


import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class runBackground extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    List<Float> myList = new ArrayList<Float>();
    List<Float> myListOriginal = new ArrayList<Float>();
    List<String> timestampList = new ArrayList<String>();

    double lat = 0.0;
    double lng = 0.0;

    private float ax = 0, ay = 0, az = 0, mAccelLast = 0, mAccelCurrent = 0, mAccel = 0, segAccion = 0, last = 0;
    private int sacudir = 0, caer = 0, aventar_empujar = 0, flag = 0, printInfo = 0, count = 0;
    private String accion = "";
    private long start;

    RequestQueue queue;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do your jobs here
        Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_LONG).show();

        start = System.nanoTime();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        queue = Volley.newRequestQueue(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getApplicationContext(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateLoc(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,15000,0,locListener);

        return START_STICKY;
    }

    public void startSensor(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onDestroy(){
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
// Shake detection
        ax = event.values[0];
        ay = event.values[1];
        az = event.values[2];

        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;

        long end = System.nanoTime();
        long time = (end - start) / 1000000;

        if(segAccion > 0){
            if(mAccel >= 17.0){
                count += 1;
            }
            myList.add(mAccel);
            myListOriginal.add(mAccel);
/*            Float[] array = {(float)time,mAccel};
            timestampList.add(Arrays.toString(array));*/
            if ((time - segAccion) > 3000 ) {
                Collections.sort(myList);
                DescriptiveStatistics da = new DescriptiveStatistics();
                for(int i = 0; i<myList.size(); i++){
                    da.addValue(myList.get(i));
                }
                double iqr = da.getPercentile(75) - da.getPercentile(25);

                if(iqr >= 6.73 ){
                    accion = "Sacudir";
                } else if(iqr >= 1.63 && iqr <= 3.91 && myListOriginal.get(0) >= 17.0 && count <= 1){
                    accion = "Caer";
                }

                System.out.println("Accion: "+accion);
                System.out.println("List: "+myListOriginal);
                System.out.println("Sort List: "+myList);
                System.out.println("75: "+da.getPercentile(75));
                System.out.println("25: "+da.getPercentile(25));
                System.out.println("iqr: "+iqr);
                System.out.println("datos: "+myList.size());
                printInfo = 0;

                count = sacudir = aventar_empujar = caer  = flag = 0;
                segAccion = 0;
                accion = "";
                myList.clear();
                myListOriginal.clear();
                da.clear();
                stopService(new Intent(this, runBackground.class));
                try {
                    TimeUnit.SECONDS.sleep(3);
                    startService(new Intent(this, runBackground.class));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if(mAccelCurrent < mAccelLast  && flag == 0) {
                if (last <= -14.0 || last >= 15.0) {
                    segAccion = time;
                    //accion = "Sacudir";
                    myList.add(last);
                    myList.add(mAccel);
                    myListOriginal.add(last);
                    myListOriginal.add(mAccel);
                    flag = 1;
                    count += 1;
/*                    Float[] array = {(float)time,last};
                    timestampList.add(Arrays.toString(array));
                    Float[] array2 = {(float)time,mAccel};
                    timestampList.add(Arrays.toString(array2));*/
                }
/*                if (last > 40.0) {
                    segAccion = time;
                    accion = "Aventar/Empujar";
                    myList.clear();
                    myList.add(last);
                    myList.add(mAccel);
                    myListOriginal.add(last);
                    myListOriginal.add(mAccel);
                    flag = 1;
                } else if (last >= 17.0) {
                    segAccion = time;
                    accion = "Caer";
                    myList.clear();
                    myList.add(last);
                    myList.add(mAcmyListOriginalcel);
                    .add(last);
                    myListOriginal.add(mAccel);
                    flag = 1;
                } else if (last <= -14.0 || last >= 15.0) {
                    segAccion = time;
                    accion = "Sacudir";
                    myList.add(last);
                    myList.add(mAccel);
                    myListOriginal.add(last);
                    myListOriginal.add(mAccel);
                    flag = 1;
                }*/
            }
            last = mAccel;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void sms(String accion){
        SharedPreferences sharedPref = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        String contacto1num = sharedPref.getString("contacto1", "");
        String contacto2num = sharedPref.getString("contacto2", "");
        String contacto3num = sharedPref.getString("contacto3", "");
        String contacto4num = sharedPref.getString("contacto4", "");
        String contacto5num = sharedPref.getString("contacto5", "");
        //String msj = accion +" http://maps.google.com/?q="+String.valueOf(lat)+','+String.valueOf(lng);
        String msj = accion +" http://maps.google.com/?q=21.155823,-86.834215";
        Toast.makeText(getApplicationContext(), contacto1num, Toast.LENGTH_LONG).show();
        try {
            SmsManager smsManager = SmsManager.getDefault();
            if(contacto1num != ""){
                smsManager.sendTextMessage(contacto1num, null, msj, null, null);
            }
            if(contacto2num != ""){
                smsManager.sendTextMessage(contacto2num, null, msj, null, null);
            }
            if(contacto3num != ""){
                smsManager.sendTextMessage(contacto3num, null, msj, null, null);
            }
            if(contacto4num != ""){
                smsManager.sendTextMessage(contacto4num, null, msj, null, null);
            }
            if(contacto5num != ""){
                smsManager.sendTextMessage(contacto5num, null, msj, null, null);
            }
            Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_LONG).show();
            sensorManager.unregisterListener(this);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startSensor();
                }
            }, 60000);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //String url ="http://karenapp.000webhostapp.com/saveData.php?latitude="+lat+"&longitude="+lng;
        String url ="http://karenapp.000webhostapp.com/saveData.php?latitude=21.155823&longitude=-86.834215";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
    }

    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLoc(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void updateLoc(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }
    }
}

