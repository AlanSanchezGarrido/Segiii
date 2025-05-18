package com.example.segiii;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map implements GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    private final Context context;
    SupportMapFragment mapFragment = SupportMapFragment.newInstance();

    public Map(Context context) {
        this.context = context;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    public void initializeMap(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    @SuppressLint("MissingPermission")
    public void enableMyLocation() {
        if (mMap != null) {
            // Habilita la visualización de la ubicación del usuario en el mapa
            mMap.setMyLocationEnabled(true);
        }
    }

    @SuppressLint("SuspiciousIndentation")
    public void centerOnLocation(LatLng location, boolean isUserLocation) {
        if (mMap == null) return; // Sale si el mapa no está inicializado
        /*
         */

        // Centra la cámara en la ubicación con un nivel de zoom de 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        // Agrega un marcador en la ubicación
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(isUserLocation ? "¡Estás aquí!" : "Destino"));

        LatLng centerHuauchinango = new LatLng(20.174774, -98.051906);
        //CalculateRoute.generateRoute(location, centerHuauchinango);
        //Toast.makeText(context, "" + Utilidades.routes.size(), Toast.LENGTH_SHORT).show();

    }
/*


    private void printRoute() {
        LatLng center = null;
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // recorriendo todas las rutas
        for (int i = 0; i < Utilidades.routes.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Obteniendo el detalle de la ruta
            List<HashMap<String, String>> path = Utilidades.routes.get(i);

            // Obteniendo todos los puntos y/o coordenadas de la ruta
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                if (center == null) {
                    //Obtengo la 1ra coordenada para centrar el mapa en la misma.
                    center = new LatLng(lat, lng);
                }
                points.add(position);
            }
            // Agregamos todos los puntos en la ruta al objeto LineOptions
            lineOptions.addAll(points);
            //Definimos el grosor de las Polilíneas
            lineOptions.width(2);
            //Definimos el color de la Polilíneas
            lineOptions.color(Color.BLUE);
        }
        //Evitamos un NullPointerException porque no quiso crear una ruta la cosa esa
        if (lineOptions != null && center != null) {

            mMap.addPolyline(lineOptions);

            LatLng origen = new LatLng(Utilidades.coordenadas.getLatitudInicial(), Utilidades.coordenadas.getLongitudInicial());
            mMap.addMarker(new MarkerOptions().position(origen).title("Lat: " + Utilidades.coordenadas.getLatitudInicial() + " - Long: " + Utilidades.coordenadas.getLongitudInicial()));

            LatLng destino = new LatLng(Utilidades.coordenadas.getLatitudFinal(), Utilidades.coordenadas.getLongitudFinal());
            mMap.addMarker(new MarkerOptions().position(destino).title("Lat: " + Utilidades.coordenadas.getLatitudFinal() + " - Long: " + Utilidades.coordenadas.getLongitudFinal()));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
        }
    }
*/
}




