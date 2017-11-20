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


public class runBackground extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    List<Float> myList = new ArrayList<Float>();

    double lat = 0.0;
    double lng = 0.0;

    private float ax = 0, ay = 0, az = 0, mAccelLast = 0, mAccelCurrent = 0, mAccel = 0, segSacudir = 0;
    private int sacudir = 0, empujar = 0, caer = 0, aventar = 0;
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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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
        //System.out.println(mAccel);

        if(time > 10000) {
            if (mAccelCurrent > mAccelLast) {
                aventar = 0;
                empujar = 0;
                caer = 0;
            }
            if(sacudir >= 1){
                myList.add(mAccel);
            }
            if ((time - segSacudir) > 3000 && sacudir >= 1) {
                Collections.sort(myList);
                if (sacudir <= 1 && myList.get(0) <= -8) {

                    Log.i("ACCION", "caer < 0 bien");
                    //sms("Caer");
                }
                if (sacudir >= 4) {
                    Log.i("ACCION", "sacudir bien");
                    //sms("Sacudir");

                }


                DescriptiveStatistics da = new DescriptiveStatistics();
                for(int i = 0; i<myList.size(); i++){
                    da.addValue(myList.get(i));
                }
                double iqr = da.getPercentile(75) - da.getPercentile(25);
                System.out.println("List: "+myList);
                System.out.println("75: "+da.getPercentile(75));
                System.out.println("25: "+da.getPercentile(25));
                System.out.println("iqr: "+iqr);
                sacudir = 0;
                segSacudir = 0;
                myList.clear();
            }
            if (mAccel > 65.0 && mAccelCurrent > mAccelLast) {
                aventar += 1;
                empujar += 1;
                Log.i("ACCION", "aventar bien");
                //sms("Aventar/Empujar");
            } else if (mAccel >= 40.0 && mAccelCurrent > mAccelLast) {
                empujar += 1;
                aventar += 1;
                //sms("Aventar/Empujar");
                Log.i("ACCION", "empujar/aventar bien");
            } else if (mAccel >= 25.0 && mAccelCurrent > mAccelLast) {
                caer += 1;
                sacudir = 0;
                //sms("Caer");
                Log.i("ACCION", "caer bien");
            } else if ((mAccel >= 4.0 && mAccelCurrent > mAccelLast) || (mAccel <= -8.0 && mAccelCurrent < mAccelLast && aventar < 1 && caer < 1 && empujar < 1)) {
                if (sacudir == 0) {
                    segSacudir = time;
                    myList.add(mAccel);

                }
                sacudir = sacudir + 1;

            }
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

