package com.example.segiii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.navigation.Navigation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationMap extends AppCompatActivity {

    private Navigation navigation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        Intent intent = getIntent();
        SupportNavigationFragment nNavFragment = (SupportNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_fragment);

        if (intent != null) {
            if (intent.hasExtra("place_id")) {
                String destination = intent.getStringExtra("place_id");
                navigation = new Navigation(this, nNavFragment, destination);
            } else {
                double latitude = intent.getDoubleExtra("lat", 0);
                double longitude = intent.getDoubleExtra("lng", 0);
                LatLng coordinates = new LatLng(latitude, longitude);
                navigation = new Navigation(this, nNavFragment, coordinates);
            }

            FloatingActionButton fab = findViewById(R.id.cancel_btn);
            fab.setOnClickListener(v -> {
                navigation.stopNavigation();
            });
        }
    }


    // Código personalizado para manejar el botón Atrás
    @Override
    public void onBackPressed() {
        navigation.stopNavigation();
        super.onBackPressed(); // Para permitir la navegación normal
    }

}
