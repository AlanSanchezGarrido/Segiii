package com.example.segiii;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.navigation.Navigation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationMap extends AppCompatActivity {

    Navigation navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        Intent intent = getIntent();

        if(intent != null){
            /*

            double latitude = intent.getDoubleExtra("latitude",0);
            double longitude = intent.getDoubleExtra("longitude",0);
            LatLng destination = new LatLng(latitude,longitude);
             */

            String destination = intent.getStringExtra("place_id");

            SupportNavigationFragment nNavFragment =(SupportNavigationFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_fragment);
            navigation = new Navigation(this.getApplicationContext(), nNavFragment, this, destination);
        }

        FloatingActionButton fab = findViewById(R.id.cancel_btn);
        fab.setOnClickListener(v -> {
            closeActivity();
            //startActivity(new Intent(MainActivity.this, MapaUI.class));
        });

    }

    private void closeActivity(){
        navigation.stopNavigation();
        finish();
    }

    @Override
    public void onBackPressed() {
        // Código personalizado para manejar el botón Atrás
        closeActivity();
        super.onBackPressed(); // Para permitir la navegación normal
    }
}