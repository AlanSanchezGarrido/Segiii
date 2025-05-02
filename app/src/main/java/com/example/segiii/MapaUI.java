package com.example.segiii;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapaUI extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE =1;
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


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults.length >0 && grantResults [0]==PackageManager.PERMISSION_GRANTED){
              mMap.enableMyLocation();
              locationn.getDeviceLocation(new Location.LocationCallback() {
                  @Override
                  public void onLocationReceived(LatLng location) {
                      mMap.centerOnLocation(location,true);
                  }
              });
            }else{
                Toast.makeText(this, "Permisso de la ubicacion denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

}