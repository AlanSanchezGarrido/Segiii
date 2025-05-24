package com.example.segiii.navigation;

import android.app.Activity;
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

    public static void navigateTo(String destination, Activity activity) {
        RequestQueue request = Volley.newRequestQueue(activity.getApplicationContext());

        //Reemplazamos los espacios del destino por +, para anexarlo al link
        destination = destination.trim().replace(" ", "+");

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + destination + "&key=" + API_KEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray results = response.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject firstResult = results.getJSONObject(0);
                        JSONObject geometry = firstResult.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        String placeId = firstResult.getString("place_id");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");

                        Intent intent = new Intent(activity.getApplicationContext(), NavigationMap.class);
                        intent.putExtra("place_id", placeId);
                        intent.putExtra("lat",lat);
                        intent.putExtra("lng",lng);

                        activity.startActivity(intent);
                        /*
                        Toast.makeText(context, placeId, Toast.LENGTH_SHORT).show();
                         */
                    } else {
                        Toast.makeText(activity, "Ubicación no encontrada", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(activity, "error en JSON :(", Toast.LENGTH_SHORT).show();
                    Log.d("ERROR JSON: ", e.getMessage(),e.getCause());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "No se puede conectar " + error.toString(), Toast.LENGTH_LONG).show();
                Log.d("ERROR JSON: ", error.toString());
            }
        }
        );
        request.add(jsonObjectRequest);
    }

}
