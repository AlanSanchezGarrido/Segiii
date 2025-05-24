package com.example.segiii.UI;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
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

    public void deleteMarker(String placeName){
        deleteMarker(placeName,"");
    }

    public void deleteMarker(String placeName, String placeId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Ubicacion ubicacion = new Ubicacion();
                ubicacion.setNombre(placeName);
                ubicacion.setPlaceid(placeId);
                ubicacion.setLatitud(0);
                ubicacion.setLongitud(0);
                db.ubicacionDAO().delete(ubicacion);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Ubicación eliminada", Toast.LENGTH_SHORT).show();
                        printMarkers();
                    }
                });
            }
        });
    }

    public void savePlace(String placeName, LatLng coordinates){
        savePlace("Not Place Id",placeName,coordinates);
    }

    public void savePlace(String placeId, String placeName, LatLng coordinates) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Ubicacion ubicacion = new Ubicacion();

                Ubicacion temp = db.ubicacionDAO().getUbicacionByNombre(placeName);
                if (temp == null) {
                    ubicacion.setPlaceid(placeId);
                    ubicacion.setNombre(placeName);
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
                        for (MarkerOptions markerOption : markerOptions) {
                            markers.add(googleMap.addMarker(markerOption));
                        }
                    }
                });

            }
        }).start();
    }

}
