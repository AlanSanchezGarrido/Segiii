package com.example.segiii;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class Location {
    private final Context context;
    public LatLng currentLocation;
    private FusedLocationProviderClient fusedLocationClient;


    public Location(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }
    @SuppressLint("MissingPermission")
    public void getDeviceLocation(LocationCallback locationCallback){
           if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                   == PackageManager.PERMISSION_GRANTED){
               fusedLocationClient.getLastLocation()
                       .addOnSuccessListener(location ->{
                           if (location!= null){
                               currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                               locationCallback.onLocationReceived (currentLocation);

                           }else{
                               Toast.makeText(context, "No se Pudo Obtener la Ubicacion", Toast.LENGTH_SHORT).show();
                               locationCallback.onLocationReceived(currentLocation);

                           }
                       })
                       .addOnFailureListener(e -> {
                           locationCallback.onLocationReceived(currentLocation);
                       });
           }else {
               Toast.makeText(context, "Permisdo de ubicacion no otorgado ", Toast.LENGTH_SHORT).show();
                locationCallback.onLocationReceived(null);
           }

    }
    public LatLng getCurrentLocation (){
        return currentLocation;
    }
    public interface  LocationCallback{
        void onLocationReceived(LatLng location);
    }
}
