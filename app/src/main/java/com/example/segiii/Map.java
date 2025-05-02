package com.example.segiii;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map implements GoogleMap.OnMapClickListener{
private GoogleMap mMap;
private final Context context;
SupportMapFragment mapFragment = SupportMapFragment.newInstance();

    public Map(Context context) {
        this.context = context;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }
    public void initializeMap(@NonNull GoogleMap googleMap){
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);


    }

    @SuppressLint("MissingPermission")
    public  void  enableMyLocation(){
        if (mMap != null) {
            // Habilita la visualización de la ubicación del usuario en el mapa
            mMap.setMyLocationEnabled(true);
        }
    }
    @SuppressLint("SuspiciousIndentation")
    public void centerOnLocation(LatLng location, boolean isUserLocation) {
        if (mMap == null) return; // Sale si el mapa no está inicializado

            // Centra la cámara en la ubicación con un nivel de zoom de 15
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            // Agrega un marcador en la ubicación
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(isUserLocation ? "¡Estás aquí!" : "Destino"));
        }
    }




