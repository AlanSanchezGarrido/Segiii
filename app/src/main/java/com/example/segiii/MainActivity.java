package com.example.segiii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.segiii.navigation.Navigation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    Navigation navigation;
    GoogleMap map;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent != null){
            double latitude = intent.getDoubleExtra("latitude",0);
            double longitude = intent.getDoubleExtra("longitude",0);
            LatLng destination = new LatLng(latitude,longitude);

            SupportNavigationFragment nNavFragment =(SupportNavigationFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_fragment);
            navigation = new Navigation(this.getApplicationContext(), nNavFragment, this, destination);
        }

         FloatingActionButton fab = findViewById(R.id.cancel_btn);
        fab.setOnClickListener(v -> {
            navigation.stopNavigation();
            finish();
            //startActivity(new Intent(MainActivity.this, MapaUI.class));
        });

    }
}