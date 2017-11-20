package com.example.anama.karenapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ContactsActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    public EditText contacto1,contacto2,contacto3,contacto4,contacto5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        contacto1 = (EditText) findViewById(R.id.contacto1);
        contacto2 = (EditText) findViewById(R.id.contacto2);
        contacto3 = (EditText) findViewById(R.id.contacto3);
        contacto4 = (EditText) findViewById(R.id.contacto4);
        contacto5 = (EditText) findViewById(R.id.contacto5);

        SharedPreferences sharedPref = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        String contacto1num = sharedPref.getString("contacto1", "");
        String contacto2num = sharedPref.getString("contacto2", "");
        String contacto3num = sharedPref.getString("contacto3", "");
        String contacto4num = sharedPref.getString("contacto4", "");
        String contacto5num = sharedPref.getString("contacto5", "");

        contacto1.setText(contacto1num);
        contacto2.setText(contacto2num);
        contacto3.setText(contacto3num);
        contacto4.setText(contacto4num);
        contacto5.setText(contacto5num);

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
        //Toast.makeText(getApplicationContext(), "Holi", Toast.LENGTH_LONG).show();
    }

    public void saveContacts(View view){
        SharedPreferences sharedPref = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("contacto1", String.valueOf(contacto1.getText()));
        editor.putString("contacto2", String.valueOf(contacto2.getText()));
        editor.putString("contacto3", String.valueOf(contacto3.getText()));
        editor.putString("contacto4", String.valueOf(contacto4.getText()));
        editor.putString("contacto5", String.valueOf(contacto5.getText()));
        editor.commit();
        Toast.makeText(getApplicationContext(), "Contactos guardados", Toast.LENGTH_LONG).show();
    }
}
