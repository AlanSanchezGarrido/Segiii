package com.example.segiii.UI;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.segiii.Location;
import com.example.segiii.Map;
import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapaUI extends VoiceNavigationActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Map mMap;
    private Location locationn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapa_ui);


        locationn = new Location(this);
        mMap = new Map(this);

        // Obtiene el fragmento del mapa y lo configura para cargar asíncronamente
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        mapFragment.getMapAsync(this);
        // Configura el botón flotante para centrar el mapa en la ubicación actual
        FloatingActionButton fab = findViewById(R.id.fab_cener_locartion);
        fab.setOnClickListener(v -> {
            // Verifica si se tiene permiso de ubicación
            if (checkLocationPermission()) {
                // Obtiene la ubicación actual
                locationn.getDeviceLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(LatLng location) {
                        // Centra el mapa en la ubicación
                        mMap.centerOnLocation(location, true);
                    }
                });
            } else {
                // Solicita permiso de ubicación si no está otorgado
                requestLocationPermission();
            }
        });


    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        if (command.contains("login")) {
            Intent intent = new Intent(this, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else if (command.contains("Registrame")) {
            Intent intent = new Intent(this, RegistrerUser.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap.initializeMap(googleMap);
        // Verifica si se tiene permiso de ubicación
        if (checkLocationPermission()) {
            // Habilita la capa de "Mi ubicación" en el mapa
            mMap.enableMyLocation();
            // Obtiene la ubicación actual
            locationn.getDeviceLocation(new Location.LocationCallback() {
                @Override
                public void onLocationReceived(LatLng location) {
                    // Centra el mapa en la ubicación
                    mMap.centerOnLocation(location, true);
                }
            });
        } else {
            // Solicita permiso de ubicación si no está otorgado
            requestLocationPermission();
        }
    }


    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this,
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}