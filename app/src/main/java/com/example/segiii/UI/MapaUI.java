package com.example.segiii.UI;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.Ubicacion;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.Location;
import com.example.segiii.Map;
import com.example.segiii.NavigationMap;
import com.example.segiii.R;
import com.example.segiii.navigation.Geocode;
import com.example.segiii.navigation.Navigation;
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
import com.example.segiii.vozSegi.ComandoPrincipal.TTSManager;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

public class MapaUI extends VoiceNavigationActivity implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener, GoogleMap.OnMarkerClickListener,
        SpeedRecognizer.OnVoiceCommandListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapaUI";
    private Map mMap;
    private Location locationn;
    private GoogleMap googleMap;
    private MarkerUI markerUI;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isNavigating = false;
    private ImageView splashOverlay;
    private Handler handler;
    private Runnable splashRunnable;
    private FloatingActionButton fabCenterLocation, fab_user, fabAyuda, fabSave;
    private LinearLayout miniWindow;
    private SegiDataBase db;
    private EditText etDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_mapa_ui);

        // Inicializa componentes de voz (llamada a la clase padre)
        super.initializeVoiceComponents();

        // Inicializar base de datos
        db = SegiDataBase.getDatabase(this);

        // Inicializa componentes específicos del mapa
        locationn = new Location(this);
        mMap = new Map(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        etDestination = findViewById(R.id.edit_destination);
        etDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_NULL &&
                                event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // ¡Se presionó Enter! Haz tu magia aquí.
                    searchAndNavigate(etDestination.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // Inicializar vistas
        splashOverlay = findViewById(R.id.splash_overlay);
        fabCenterLocation = findViewById(R.id.fab_center_location);
        fab_user = findViewById(R.id.fab_user);
        fabAyuda = findViewById(R.id.fab_ayuda);
        fabSave = findViewById(R.id.fab_save);
        miniWindow = findViewById(R.id.mini_window);

        // Verificar vistas
        if (splashOverlay == null) Log.e(TAG, "splashOverlay is null - check layout XML");
        else
            Log.d(TAG, "splashOverlay initialized, visibility: " + (splashOverlay.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
        if (fabCenterLocation == null) Log.e(TAG, "fabCenterLocation is null - check layout XML");
        if (fab_user == null) Log.e(TAG, "fab_user is null - check layout XML");
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
                if (checkLocationPermission()) {
                    locationn.getDeviceLocation(location -> {
                        mMap.centerOnLocation(location, true);
                        getDeviceLocation(); // También actualiza googleMap
                    });
                } else {
                    requestLocationPermission();
                }
            });
        }

        // Configurar FAB para usuarios
        if (fab_user != null) {
            fab_user.setOnClickListener(v -> {
                Log.d(TAG, "User FAB clicked");
                navigateToScreen(RegistrerUser.class, "Navegando a la pantalla de registro");
            });
        }

        // Configurar FAB para ayuda
        if (fabAyuda != null) {
            fabAyuda.setOnClickListener(v -> {
                Log.d(TAG, "Ayuda FAB clicked");
                navigateToScreen(Ayuda.class, "Navegando a la pantalla de ayuda");
            });
        }

        // Configurar FAB para mostrar la mini-ventana
        if (fabSave != null) {
            fabSave.setOnClickListener(v -> {
                guardarConMiniVentana(locationn.getCurrentLocation());
            });
        }


        // Configurar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: No se pudo encontrar el fragmento del mapa");
            Toast.makeText(this, "Error al cargar el mapa", Toast.LENGTH_SHORT).show();
        }

        // Registra comandos específicos del mapa
        registerMapSpecificCommands();
    }


    /**
     * Registra comandos específicos para la pantalla del mapa
     */
    private void registerMapSpecificCommands() {
        if (speedRecognizer != null) {
            speedRecognizer.addCustomCommand("ubicación", context -> {
                Log.d(TAG, "Ejecutando comando de centrar ubicación");
                centerOnUserLocation();
            });
            speedRecognizer.addCustomCommand("ubicacion", context -> {
                Log.d(TAG, "Ejecutando comando de centrar ubicación (alias)");
                centerOnUserLocation();
            });
            speedRecognizer.addCustomCommand("centrar", context -> {
                Log.d(TAG, "Ejecutando comando de centrar mapa");
                centerOnUserLocation();
            });
            speedRecognizer.addCustomCommand("centrar mapa", context -> {
                Log.d(TAG, "Ejecutando comando de centrar mapa (alias completo)");
                centerOnUserLocation();
            });
            // Nuevo comando para accesibilidad
            speedRecognizer.addCustomCommand("accesibilidad", context -> {
                Log.d(TAG, "Ejecutando comando de accesibilidad");
                showAccessibilityMenu();
            });
        } else {
            Log.e(TAG, "Error: speedRecognizer es null, no se pueden registrar comandos");
        }
    }

    /**
     * Centra el mapa en la ubicación actual del usuario
     */
    private void centerOnUserLocation() {
        if (checkLocationPermission()) {
            if (ttsManager != null) {
                ttsManager.speak("Centrando el mapa en tu ubicación actual", null);
            }
            locationn.getDeviceLocation(location -> {
                if (mMap != null) {
                    mMap.centerOnLocation(location, true);
                    getDeviceLocation(); // También actualiza googleMap
                }
            });
        } else {
            requestLocationPermission();
            if (ttsManager != null) {
                ttsManager.speak("Necesito permiso para acceder a tu ubicación", null);
            }
        }
    }

    private void getDeviceLocation() {
        try {
            if (checkLocationPermission()) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (googleMap != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                            Log.d(TAG, "Location centered: " + latLng);
                        }
                    } else {
                        Log.w(TAG, "Location is null");
                        LatLng defaultLocation = new LatLng(0.0, 0.0);
                        if (googleMap != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f));
                        }
                        Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission error: " + e.getMessage());
        }
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        Log.d(TAG, "Recibido comando: " + command + " - isNavigating: " + isNavigating);
        if (isNavigating) {
            Log.d(TAG, "Ignorando comando porque ya está navegando: " + command);
            return;
        }
        if (speedRecognizer != null && speedRecognizer.isValidCommand(command)) {
            Log.d(TAG, "Comando válido detectado, el SpeedRecognizer lo manejará: " + command);
            return;
        }
        if (command.contains("login") || command.contains("iniciar sesión") || command.contains("iniciar sesion") || command.contains("ingresar")) {
            navigateToScreen(login.class, "Navegando a la pantalla de inicio de sesión");
        } else if (command.contains("registr")) {
            navigateToScreen(RegistrerUser.class, "Navegando a la pantalla de registro");
        } else if (command.contains("ayuda") || command.contains("tutorial")) {
            navigateToScreen(Ayuda.class, "Navegando a la pantalla de ayuda");
        } else if (command.contains("ubicación") || command.contains("ubicacion") || command.contains("centrar")) {
            centerOnUserLocation();
        } else if (command.contains("accesibilidad")) {
            showAccessibilityMenu();
        } else {
            Log.d(TAG, "Comando no reconocido específicamente en MapaUI: " + command);
            handleNoCommand();
        }
    }

    private void navigateToScreen(Class<?> destinationClass, String speechText) {
        isNavigating = true;
        Log.d(TAG, "Navegando a pantalla: " + destinationClass.getSimpleName());
        if (ttsManager != null) {
            ttsManager.speak(speechText, () -> {
                Intent intent = new Intent(this, destinationClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 500);
            });
        } else {
            Intent intent = new Intent(this, destinationClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "Map ready");
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnPoiClickListener(this);
        markerUI = new MarkerUI(this, googleMap);
        markerUI.printMarkers();
        this.googleMap = googleMap;
        mMap.initializeMap(googleMap);
        if (checkLocationPermission()) {
            mMap.enableMyLocation();
            googleMap.setMyLocationEnabled(true);
            locationn.getDeviceLocation(location -> {
                mMap.centerOnLocation(location, true);
                getDeviceLocation(); // También actualiza googleMap
            });
        } else {
            requestLocationPermission();
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
                if (mMap != null) {
                    mMap.enableMyLocation();
                    locationn.getDeviceLocation(location -> {
                        mMap.centerOnLocation(location, true);
                    });
                }
                if (googleMap != null) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                        getDeviceLocation();
                    } catch (SecurityException e) {
                        Log.e(TAG, "Error enabling location: " + e.getMessage());
                    }
                }
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAccessibilityMenu() {
        Dialog dialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.accesibility_menu, null);
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
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        isNavigating = false;
        if (wordSegui != null && !wordSegui.isListening()) {
            initializeHotwordDetection();
        }
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


    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        guardarConMiniVentana(pointOfInterest.placeId, pointOfInterest.name, pointOfInterest.latLng);
        //markerUI.savePlace(pointOfInterest.placeId, pointOfInterest.name, pointOfInterest.latLng);
    }

    private void navigateByLatLng(LatLng position) {
        Intent intent = new Intent(getApplicationContext(), NavigationMap.class);
        intent.putExtra("lat", position.latitude);
        intent.putExtra("lng", position.longitude);
        startActivity(intent);
    }

    private void navigateByPlaceId(String placeId) {
        Intent intent = new Intent(getApplicationContext(), NavigationMap.class);
        intent.putExtra("place_id", placeId);
        startActivity(intent);
    }

    private void navigateByName(String destination) {
        Geocode.navigateTo(destination, this);
    }

    private void searchAndNavigate(String name) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                final Ubicacion datoObtenido = db.ubicacionDAO().getUbicacionByNombre(name);
                mainHandler.post(() -> {
                    if (datoObtenido != null) {
                        Log.d(TAG, "Dato obtenido: " + datoObtenido);
                        LatLng latLng = new LatLng(datoObtenido.getLatitud(), datoObtenido.getLongitud());
                        navigateByLatLng(latLng);
                    } else {
                        Log.d(TAG, "No se encontró el dato");
                        navigateByName(name);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    // Maneja error UI...
                });
            }
        }).start();
    }


    private void mostrarMiniVentana() {
        if (miniWindow != null) {
            miniWindow.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "MiniWindow are null");
        }
    }

    private void guardarConMiniVentana(String placeId, String placeName, LatLng ubi) {
        if (miniWindow != null) {
            mostrarMiniVentana();
            MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
            MaterialButton btnOK = miniWindow.findViewById(R.id.btn_confirm);
            EditText etPlaceName = miniWindow.findViewById(R.id.edit_name);
            TextView etWindowTitle = miniWindow.findViewById(R.id.mw_tv_title);
            if (btnCancel != null && btnOK != null && etPlaceName != null && etWindowTitle != null) {
                etWindowTitle.setText("UBICACION");
                etPlaceName.setText(placeName);
                btnCancel.setOnClickListener(v -> {
                    Log.d(TAG, "Cancel button clicked");
                    miniWindow.setVisibility(View.GONE);
                });
                btnOK.setOnClickListener(v -> {
                    markerUI.savePlace(placeId, etPlaceName.getText().toString(), ubi);
                    miniWindow.setVisibility(View.GONE);
                });
            } else {
                Log.e(TAG, "MiniWindow buttons are null");
            }
        } else {
            Log.e(TAG, "MiniWindow are null");
        }
    }

    private void guardarConMiniVentana(LatLng latLng) {
        if (miniWindow != null) {
            mostrarMiniVentana();
            MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
            MaterialButton btnOK = miniWindow.findViewById(R.id.btn_confirm);
            EditText etPlaceName = miniWindow.findViewById(R.id.edit_name);
            TextView etWindowTitle = miniWindow.findViewById(R.id.mw_tv_title);
            if (btnCancel != null && btnOK != null && etPlaceName != null && etWindowTitle != null) {
                etWindowTitle.setText("UBICACION");
                etPlaceName.setText(null);
                btnCancel.setOnClickListener(v -> {
                    Log.d(TAG, "Cancel button clicked");
                    miniWindow.setVisibility(View.GONE);
                });
                btnOK.setOnClickListener(v -> {
                    Log.d(TAG, "OK press");
                    markerUI.savePlace(etPlaceName.getText().toString(), latLng);
                    //markerUI.deleteAll();
                    miniWindow.setVisibility(View.GONE);
                });
            } else {
                Log.e(TAG, "MiniWindow buttons are null");
            }
        } else {
            Log.e(TAG, "MiniWindow are null");
        }
    }

    private void eliminarConMiniVentana(String placeName) {
        if (miniWindow != null) {
            mostrarMiniVentana();
            MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
            MaterialButton btnOK = miniWindow.findViewById(R.id.btn_confirm);
            EditText etPlaceName = miniWindow.findViewById(R.id.edit_name);
            TextView etWindowTitle = miniWindow.findViewById(R.id.mw_tv_title);
            if (btnCancel != null && btnOK != null && etPlaceName != null && etWindowTitle != null) {
                etWindowTitle.setText("¿ELIMINAR?");
                etPlaceName.setText(placeName);
                btnCancel.setOnClickListener(v -> {
                    miniWindow.setVisibility(View.GONE);
                });
                btnOK.setOnClickListener(v -> {
                    markerUI.deleteUbication(placeName);
                    //markerUI.deleteAll();
                    miniWindow.setVisibility(View.GONE);
                });
            } else {
                Log.e(TAG, "MiniWindow components are null");
            }
        } else {
            Log.e(TAG, "MiniWindow are null");
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //markerUI.deleteMarker(marker.getTitle());
        eliminarConMiniVentana(marker.getTitle());
        return true;
    }

    @Override
    public void onCommandProcessed(String command, String result) {

    }

    @Override
    public void onError(String errorMessage) {

    }

    @Override
    public void onRecognitionCancelled() {

    }

    @Override
    public void onNavigationCommand(String destination) {
        Log.d(TAG, "Comando de navegación recibido: " + destination);

        if (isNavigating) {
            Log.d(TAG, "Ya está navegando, ignorando comando");
            return;
        }

        if (ttsManager != null && ttsManager.isInitialized()) {
            ttsManager.speak("Navegando a " + destination, new TTSManager.TTSCallback() {
                @Override
                public void onSpeakComplete() {
                    searchAndNavigate(destination); // <--- Aquí debe llegar solo el destino limpio
                }
            });
        } else {
            searchAndNavigate(destination);
        }
    }
}