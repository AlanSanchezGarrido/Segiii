package com.example.segiii.UI;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.segiii.BDSegi.DAOs.UbicacionDAO;
import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.Ubicacion;
import com.example.segiii.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MarkerUI {

    private GoogleMap googleMap;
    private Activity activity;
    private ArrayList<MarkerOptions> markerOptions;
    private ArrayList<Marker> markers;
    private SegiDataBase db;
    private boolean existsMark;

    public MarkerUI(Activity activity, GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.activity = activity;
        markerOptions = new ArrayList<MarkerOptions>();
        markers = new ArrayList<Marker>();
        db = SegiDataBase.getDatabase(activity.getApplicationContext());
    }

    public void savePlace(String placeName, LatLng coordinates) {
        savePlace("Not Place Id", placeName, coordinates);
    }

    public void savePlace(String placeId, String placeName, LatLng coordinates) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MarkerUI: ", "savePlace entro");
                Ubicacion ubicacion = new Ubicacion();

                Ubicacion temp = db.ubicacionDAO().getUbicacionByNombre(placeName.toLowerCase());
                if (temp == null) {
                    ubicacion.setPlaceid(placeId);
                    ubicacion.setNombre(placeName.toLowerCase());
                    ubicacion.setLatitud(coordinates.latitude);
                    ubicacion.setLongitud(coordinates.longitude);

                    UbicacionDAO ubicacionDAO = db.ubicacionDAO();
                    ubicacionDAO.insert(ubicacion);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "Ubicación guardada", Toast.LENGTH_SHORT).show();
                            printMarkers();
                        }
                    });
                }

            }
        }).start();
    }

    public void printMarkers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Ubicacion> ubicacions = db.ubicacionDAO().getallUbicaciones();
                Log.d("MarkerUI: ", "print entro: " + ubicacions.size());
                markerOptions.clear();
                LatLng coordinates;
                for (Ubicacion ubi : ubicacions) {
                    coordinates = new LatLng(ubi.getLatitud(), ubi.getLongitud());

                    Bitmap originalBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.tirzino);
                    int nuevoAncho = 90; // Define el nuevo ancho en píxeles
                    int nuevoAlto = 90;  // Define el nuevo alto en píxeles
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, nuevoAncho, nuevoAlto, false);
                    BitmapDescriptor iconoReducido = BitmapDescriptorFactory.fromBitmap(scaledBitmap);

                    markerOptions.add(new MarkerOptions()
                            .position(coordinates)
                            .title(ubi.getNombre())
                            .icon(iconoReducido)
                    );
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!markers.isEmpty()){
                            clearMarkers();
                        }
                        for (MarkerOptions markerOption : markerOptions) {
                            markers.add(googleMap.addMarker(markerOption));
                        }
                    }
                });

            }
        }).start();
    }

    private void clearMarkers(){
        for(Marker m : markers){
            m.remove();
        }
    }

    public void deleteAll(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MarkerUI: ", "deleteAll entro");
                for(Ubicacion ubi : db.ubicacionDAO().getallUbicaciones()){
                    db.ubicacionDAO().delete(ubi);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printMarkers();
                    }
                });
            }
        }).start();
    }

    public void deleteUbication(String placeName){
        new Thread(() -> {
            Log.d("MarkerUI","deleteUbicacion");
            Ubicacion ubicacion = db.ubicacionDAO().getUbicacionByNombre(placeName.toLowerCase());
            if(ubicacion != null){
                db.ubicacionDAO().delete(ubicacion);
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity.getApplicationContext(), "Ubicación Eliminada", Toast.LENGTH_SHORT).show();
                    printMarkers();
                });
            }else{
                Log.d("MarkerUI","ubicacion no encontrada");
            }
        }).start();
    }

}
