package com.example.segiii;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.UI.Ayuda;
import com.example.segiii.UI.RegistrerUser;
import com.example.segiii.UI.login;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MapaUI extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapaUI";
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView splashOverlay;
    private Handler handler;
    private Runnable splashRunnable;
    private FloatingActionButton fabCenterLocation, fab_user, fabAyuda, fabSave;
    private LinearLayout miniWindow;
    private SegiDataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_mapa_ui);

        // Inicializar base de datos
        db = SegiDataBase.getDatabase(this);

        // Verificar si hay un usuario registrado
        checkUserRegistration();

        // Inicializar ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar vistas
        splashOverlay = findViewById(R.id.splash_overlay);
        fabCenterLocation = findViewById(R.id.fab_center_location);
        fab_user = findViewById(R.id.fab_user);
        fabAyuda = findViewById(R.id.fab_ayuda);
        fabSave = findViewById(R.id.fab_save);
        miniWindow = findViewById(R.id.mini_window);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);

        // Verificar vistas
        if (splashOverlay == null) Log.e(TAG, "splashOverlay is null - check layout XML");
        else Log.d(TAG, "splashOverlay initialized, visibility: " + (splashOverlay.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
        if (fabCenterLocation == null) Log.e(TAG, "fabCenterLocation is null - check layout XML");
        if (fabAyuda == null) Log.e(TAG, "fabAyuda is null - check layout XML");
        if (fabSave == null) Log.e(TAG, "fabSave is null - check layout XML");
        if (miniWindow == null) Log.e(TAG, "miniWindow is null - check layout XML");

        // Configurar splash screen para desaparecer después de 5 segundos
        handler = new Handler(Looper.getMainLooper());
        splashRunnable = () -> {
            Log.d(TAG, "Splash timeout triggered");
            if (splashOverlay != null) {
                splashOverlay.setVisibility(View.GONE);
                Log.d(TAG, "splashOverlay set to GONE");
            } else Log.e(TAG, "splashOverlay is null during timeout");
        };
        handler.postDelayed(splashRunnable, 5000);

        // Configurar FAB para centrar ubicación
        if (fabCenterLocation != null) {
            fabCenterLocation.setOnClickListener(v -> {
                Log.d(TAG, "Center location FAB clicked");
                if (checkLocationPermission()) getDeviceLocation();
                else requestLocationPermission();
            });
        }

        // Configurar FAB para usuarios
        if (fab_user != null) {
            fab_user.setOnClickListener(v -> {
                Log.d(TAG, "User FAB clicked");
                Intent intent = new Intent(MapaUI.this, RegistrerUser.class);
                startActivity(intent);
            });
        }

        // Configurar FAB para ayuda
        if (fabAyuda != null) {
            fabAyuda.setOnClickListener(v -> {
                Log.d(TAG, "Ayuda FAB clicked");
                Intent intent = new Intent(MapaUI.this, Ayuda.class);
                startActivity(intent);
            });
        }

        // Configurar FAB para mostrar la mini-ventana
        if (fabSave != null) {
            fabSave.setOnClickListener(v -> {
                Log.d(TAG, "Save FAB clicked");
                if (miniWindow != null) {
                    miniWindow.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "miniWindow is null when trying to show");
                }
            });
        }

        // Configurar botón de cancelar (X) para cerrar la mini-ventana
        if (miniWindow != null) {
            MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    Log.d(TAG, "Cancel button clicked");
                    miniWindow.setVisibility(View.GONE);
                });
            } else {
                Log.e(TAG, "btnCancel is null - check layout");
            }
        }

        // Configurar mapa
        if (mapFragment != null) mapFragment.getMapAsync(this);
        else {
            Log.e(TAG, "Map fragment is null");
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserRegistration() {
        new Thread(() -> {
            List<Usuario> usuarios = db.usuarioDAO().getAllUsuarios();
            if (usuarios == null || usuarios.isEmpty()) {
                runOnUiThread(() -> {
                    Log.d(TAG, "No users found, redirecting to Login");
                    Intent intent = new Intent(MapaUI.this, login.class);
                    startActivity(intent);
                    finish();
                });
            } else {
                runOnUiThread(() -> {
                    Log.d(TAG, "User found, staying in MapaUI");
                    Toast.makeText(this, "Usuario registrado detectado", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        Log.d(TAG, "Map ready");
        googleMap = map;
        if (checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
            getDeviceLocation();
        } else requestLocationPermission();
    }

    private void getDeviceLocation() {
        try {
            if (checkLocationPermission()) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        Log.d(TAG, "Location centered: " + latLng);
                    } else {
                        Log.w(TAG, "Location is null");
                        LatLng defaultLocation = new LatLng(0.0, 0.0);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f));
                        Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission error: " + e.getMessage());
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Se requiere permiso de ubicación para mostrar tu posición en el mapa", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                        getDeviceLocation();
                    } catch (SecurityException e) {
                        Log.e(TAG, "Error enabling location: " + e.getMessage());
                    }
                }
            } else Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAccessibilityMenu() {
        Dialog dialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.accessibility_menu, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnHighContrast = dialogView.findViewById(R.id.btn_high_contrast1);
        Button btnColorblindRedGreen = dialogView.findViewById(R.id.btn_colorblind_red_green2);
        Button btnColorblindBlueYellow = dialogView.findViewById(R.id.btn_colorblind_blue_yellow3);
        Button btnIncreaseTextSize = dialogView.findViewById(R.id.btn_increase_text_size4);
        Button btnResetFilters = dialogView.findViewById(R.id.btn_reset_filters5);

        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (btnHighContrast != null) {
            btnHighContrast.setOnClickListener(v -> {
                editor.putBoolean("high_contrast", true);
                editor.apply();
                Toast.makeText(this, "Modo de alto contraste activado", Toast.LENGTH_SHORT).show();
                refreshAllActivities();
                dialog.dismiss();
            });
        } else Log.e(TAG, "btnHighContrast is null - check accessibility_menu layout");

        if (btnColorblindRedGreen != null) {
            btnColorblindRedGreen.setOnClickListener(v -> {
                editor.putString("colorblind_filter", "red_green");
                editor.apply();
                Toast.makeText(this, "Filtro rojo-verde activado", Toast.LENGTH_SHORT).show();
                refreshAllActivities();
                dialog.dismiss();
            });
        } else Log.e(TAG, "btnColorblindRedGreen is null - check accessibility_menu layout");

        if (btnColorblindBlueYellow != null) {
            btnColorblindBlueYellow.setOnClickListener(v -> {
                editor.putString("colorblind_filter", "blue_yellow");
                editor.apply();
                Toast.makeText(this, "Filtro azul-amarillo activado", Toast.LENGTH_SHORT).show();
                refreshAllActivities();
                dialog.dismiss();
            });
        } else Log.e(TAG, "btnColorblindBlueYellow is null - check accessibility_menu layout");

        if (btnIncreaseTextSize != null) {
            btnIncreaseTextSize.setOnClickListener(v -> {
                editor.putBoolean("increase_text_size", true);
                editor.apply();
                Toast.makeText(this, "Tamaño de texto aumentado", Toast.LENGTH_SHORT).show();
                refreshAllActivities();
                dialog.dismiss();
            });
        } else Log.e(TAG, "btnIncreaseTextSize is null - check accessibility_menu layout");

        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> {
                editor.clear();
                editor.apply();
                Toast.makeText(this, "Filtros restablecidos", Toast.LENGTH_SHORT).show();
                refreshAllActivities();
                dialog.dismiss();
            });
        } else Log.e(TAG, "btnResetFilters is null - check accessibility_menu layout");

        dialog.show();
    }

    private void refreshAllActivities() {
        Intent intent = new Intent(this, MapaUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        if (handler != null && splashRunnable != null) {
            handler.removeCallbacks(splashRunnable);
            Log.d(TAG, "Handler callbacks removed");
        }
    }
}