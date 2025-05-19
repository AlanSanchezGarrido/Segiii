package com.example.segiii.navigation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.segiii.NavigationMap;

import org.json.JSONArray;
import org.json.JSONObject;


public class Geocode {

    private static final String API_KEY = "";
    private static String placeId;

    public static void navigateTo(String destination, Context context) {
        RequestQueue request = Volley.newRequestQueue(context);

        //Reemplazamos los espacios del destino por +, para anexarlo al link
        destination = destination.replace(" ", "+");

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + destination + "&key=" + API_KEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray results = response.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject firstResult = results.getJSONObject(0);
                        placeId = firstResult.getString("place_id");

                        Toast.makeText(context, placeId, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "Ubicación no encontrada", Toast.LENGTH_SHORT).show();
                    }

                    /*
                        Intent intent = new Intent(context, NavigationMap.class);
                        intent.putExtra("place_id", placeId);
                        context.startActivity(intent);
                     */

                } catch (Exception e) {
                    Toast.makeText(context, "error en JSON:(", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "No se puede conectar " + error.toString(), Toast.LENGTH_LONG).show();
                Log.d("ERROR: ", error.toString());
            }
        }
        );
        request.add(jsonObjectRequest);

            /* Obtener las coordenadas de un lugar
                JSONArray results = new JSONArray(informationString.toString());
                JSONObject firstResult = results.getJSONObject(0);
                JSONObject geometry = firstResult.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                Toast.makeText(context, "Lat: " + location.getDouble("lat"), Toast.LENGTH_SHORT).show();
            */
    }

}
