package com.example.anama.karenapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private TextView start_stop_text;
    private ImageView play_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play_stop = (ImageView) findViewById(R.id.play_stop);
        start_stop_text = (TextView) findViewById(R.id.start_stop_text);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        SharedPreferences sharedPref = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        long serviceOn = sharedPref.getInt("serviceOn",0);
        if(serviceOn == 0){
            play_stop.setImageResource(R.drawable.playnofondo);
            start_stop_text.setText("Click para Iniciar");
        } else{
            play_stop.setImageResource(R.drawable.stop);
            start_stop_text.setText("Click para Detener");
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void decideImage() {
        SharedPreferences sharedPref = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        long serviceOn = sharedPref.getInt("serviceOn",0);
        if(serviceOn == 0){
            editor.putInt("serviceOn", 1);
            editor.apply();
            play_stop.setImageResource(R.drawable.stop);
            start_stop_text.setText("Click para Detener");
            startService(new Intent(this, runBackground.class));
        } else{
            editor.putInt("serviceOn", 0);
            editor.apply();
            play_stop.setImageResource(R.drawable.playnofondo);
            start_stop_text.setText("Click para Iniciar");
            stopService(new Intent(this, runBackground.class));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void play_stop(View view){
        decideImage();
    }

    public void home(MenuItem item){
        Intent mainIntent = new Intent().setClass(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
    }

    public void contacts(MenuItem item){
        Intent mainIntent = new Intent().setClass(getApplicationContext(), ContactsActivity.class);
        startActivity(mainIntent);
    }

    public void map(MenuItem item){
        Intent mainIntent = new Intent().setClass(getApplicationContext(), MapActivity.class);
        startActivity(mainIntent);
        //Toast.makeText(getApplicationContext(), "Holi", Toast.LENGTH_LONG).show();
    }

}
