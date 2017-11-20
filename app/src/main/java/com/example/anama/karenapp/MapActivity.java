package com.example.anama.karenapp;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class MapActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        WebView myWebView = (WebView) this.findViewById(R.id.webView);

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("http://karenapp.000webhostapp.com/");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    }
}
