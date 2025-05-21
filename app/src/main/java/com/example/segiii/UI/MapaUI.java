package com.example.segiii.UI;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.segiii.Location;
import com.example.segiii.Map;
import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapaUI extends VoiceNavigationActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapaUI";
    private Map mMap;
    private Location locationn;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ui);

        // Inicializa componentes de voz (llamada a la clase padre)
        super.initializeVoiceComponents();

        // Inicializa componentes específicos del mapa
        locationn = new Location(this);
        mMap = new Map(this);

        // Obtiene el fragmento del mapa y lo configura para cargar asíncronamente
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: No se pudo encontrar el fragmento del mapa");
            Toast.makeText(this, "Error al cargar el mapa", Toast.LENGTH_SHORT).show();
        }

        // Configura el botón flotante para centrar el mapa en la ubicación actual
        FloatingActionButton fab = findViewById(R.id.fab_cener_locartion);
        fab.setOnClickListener(v -> {
            // Verifica si se tiene permiso de ubicación
            if (checkLocationPermission()) {
                // Obtiene la ubicación actual
                locationn.getDeviceLocation(location -> {
                    // Centra el mapa en la ubicación
                    mMap.centerOnLocation(location, true);
                });
            } else {
                // Solicita permiso de ubicación si no está otorgado
                requestLocationPermission();
            }
        });

        // Registra comandos específicos del mapa
        registerMapSpecificCommands();
    }

    /**
     * Registra comandos específicos para la pantalla del mapa
     */
    private void registerMapSpecificCommands() {
        // Añade comandos personalizados relacionados con el mapa
        if (speedRecognizer != null) {
            // Comando para centrar el mapa en la ubicación actual
            speedRecognizer.addCustomCommand("ubicación", context -> {
                Log.d(TAG, "Ejecutando comando de centrar ubicación");
                centerOnUserLocation();
            });

            // Alias para el comando de ubicación
            speedRecognizer.addCustomCommand("ubicacion", context -> {
                Log.d(TAG, "Ejecutando comando de centrar ubicación (alias)");
                centerOnUserLocation();
            });

            // Alias para el comando de centrar
            speedRecognizer.addCustomCommand("centrar", context -> {
                Log.d(TAG, "Ejecutando comando de centrar mapa");
                centerOnUserLocation();
            });

            // Alias para el comando de centrar mapa
            speedRecognizer.addCustomCommand("centrar mapa", context -> {
                Log.d(TAG, "Ejecutando comando de centrar mapa (alias completo)");
                centerOnUserLocation();
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
                }
            });
        } else {
            requestLocationPermission();
            if (ttsManager != null) {
                ttsManager.speak("Necesito permiso para acceder a tu ubicación", null);
            }
        }
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        Log.d(TAG, "Recibido comando: " + command + " - isNavigating: " + isNavigating);

        if (isNavigating) {
            Log.d(TAG, "Ignorando comando porque ya está navegando: " + command);
            return;
        }

        // Validar si el comando es un comando válido registrado
        if (speedRecognizer != null && speedRecognizer.isValidCommand(command)) {
            // Los comandos específicos del mapa se manejan automáticamente a través de los comandos registrados
            Log.d(TAG, "Comando válido detectado, el SpeedRecognizer lo manejará: " + command);
            return;
        }

        // Comandos de navegación (estos no requieren registro porque son manejados directamente)
        if (command.contains("login") || command.contains("iniciar sesión") || command.contains("iniciar sesion") || command.contains("ingresar")) {
            navigateToScreen(login.class, "Navegando a la pantalla de inicio de sesión");
        } else if (command.contains("registr")) {
            // Usar una cadena más corta para capturar registrar, registrame, registrarme, etc.
            navigateToScreen(RegistrerUser.class, "Navegando a la pantalla de registro");
        } else if (command.contains("ayuda") || command.contains("tutorial")) {
            navigateToScreen(Ayuda.class, "Navegando a la pantalla de ayuda");
        } else if (command.contains("ubicación") || command.contains("ubicacion") || command.contains("centrar")) {
            centerOnUserLocation();
        } else {
            Log.d(TAG, "Comando no reconocido específicamente en MapaUI: " + command);
            handleNoCommand();
        }
    }

    /**
     * Método auxiliar para navegar a otra pantalla
     */
    private void navigateToScreen(Class<?> destinationClass, String speechText) {
        isNavigating = true;
        Log.d(TAG, "Navegando a pantalla: " + destinationClass.getSimpleName());

        if (ttsManager != null) {
            ttsManager.speak(speechText, () -> {
                Intent intent = new Intent(this, destinationClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                // Asegurarse de que se finalice esta actividad después de un retraso
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 500);
            });
        } else {
            // Si TTS falla, navegar directamente
            Intent intent = new Intent(this, destinationClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap.initializeMap(googleMap);
        // Verifica si se tiene permiso de ubicación
        if (checkLocationPermission()) {
            // Habilita la capa de "Mi ubicación" en el mapa
            mMap.enableMyLocation();
            // Obtiene la ubicación actual
            locationn.getDeviceLocation(location -> {
                // Centra el mapa en la ubicación
                mMap.centerOnLocation(location, true);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.enableMyLocation();
                    locationn.getDeviceLocation(location -> {
                        mMap.centerOnLocation(location, true);
                    });
                }
            } else {
                Toast.makeText(this,
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restablecer la bandera de navegación
        isNavigating = false;
        // Iniciar detección de hotword si es necesario
        if (wordSegui != null && !wordSegui.isListening()) {
            initializeHotwordDetection();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}