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
import com.example.segiii.Location;
import com.example.segiii.Map;
import com.example.segiii.NavigationMap;
import com.example.segiii.R;
import com.example.segiii.navigation.Geocode;
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
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
    private boolean isWaitingForLocationName = false;
    private boolean isWaitingForSaveName = false;
    private boolean isWaitingForDeleteName = false;
    private boolean isWaitingForSaveConfirmation = false;
    private String pendingLocationName = "";
    private LatLng pendingLocation = null;

    @Override
    protected void handleSaveLocationCommand() {
        Log.d(TAG, "=== COMANDO GUARDAR UBICACIÓN ===");
        Log.d(TAG, "Estado de actividad en handleSaveLocationCommand: " + isActivityActive);

        if (ttsManager == null || !ttsManager.isInitialized()) {
            Log.e(TAG, "TTS Manager es null o no está inicializado, usando mini ventana directamente");
            guardarConMiniVentanaVoz();
            return;
        }

        ttsManager.speak("¿Con qué nombre quieres guardar esta ubicación?", () -> {
            isWaitingForLocationName = true;
            isWaitingForSaveName = true;
            if (speedRecognizer != null) {
                speedRecognizer.setDataEntryMode(true);
                speedRecognizer.startVoiceRecognition();
            }
        });
    }

    @Override
    protected void handleDeleteLocationCommand(String locationName) {
        Log.d(TAG, "=== COMANDO ELIMINAR UBICACIÓN ===");
        Log.d(TAG, "Nombre recibido: '" + locationName + "'");

        if (locationName == null || locationName.trim().isEmpty()) {
            if (ttsManager != null) {
                ttsManager.speak("¿Qué ubicación quieres eliminar?", () -> {
                    isWaitingForLocationName = true;
                    isWaitingForDeleteName = true;
                    speedRecognizer.setDataEntryMode(true);
                    speedRecognizer.startVoiceRecognition();
                });
            } else {
                Log.e(TAG, "TTS Manager es null");
                Toast.makeText(this, "Error en el sistema de voz", Toast.LENGTH_SHORT).show();
            }
        } else {
            eliminarUbicacionPorVoz(locationName.trim());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapa_ui);

        // Inicializar componentes de voz primero
        super.initializeVoiceComponents();

        // Dar la bienvenida después de que TTS esté listo
        darBienvenidaEntusiasta();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("destination")) {
            String destination = intent.getStringExtra("destination");
            Log.d(TAG, "Received destination from intent: " + destination);
            if (destination != null && !destination.trim().isEmpty()) {
                onNavigationCommand(destination);
            }
        }
        db = SegiDataBase.getDatabase(this);
        locationn = new Location(this);
        mMap = new Map(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        etDestination = findViewById(R.id.edit_destination);
        etDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (actionId == EditorInfo.IME_NULL && event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchAndNavigate(etDestination.getText().toString());
                return true;
            }
            return false;
        });

        splashOverlay = findViewById(R.id.splash_overlay);
        fabCenterLocation = findViewById(R.id.fab_center_location);
        fab_user = findViewById(R.id.fab_user);
        fabAyuda = findViewById(R.id.fab_ayuda);
        fabSave = findViewById(R.id.fab_save);
        miniWindow = findViewById(R.id.mini_window);

        if (splashOverlay == null) Log.e(TAG, "splashOverlay is null - check layout XML");
        if (fabCenterLocation == null) Log.e(TAG, "fabCenterLocation is null - check layout XML");
        if (fab_user == null) Log.e(TAG, "fab_user is null - check layout XML");
        if (fabAyuda == null) Log.e(TAG, "fabAyuda is null - check layout XML");
        if (fabSave == null) Log.e(TAG, "fabSave is null - check layout XML");
        if (miniWindow == null) Log.e(TAG, "miniWindow is null - check layout XML");

        handler = new Handler(Looper.getMainLooper());
        splashRunnable = () -> {
            Log.d(TAG, "Splash timeout triggered");
            if (splashOverlay != null) {
                splashOverlay.setVisibility(View.GONE);
                Log.d(TAG, "splashOverlay set to GONE");
            } else Log.e(TAG, "splashOverlay is null during timeout");
        };
        handler.postDelayed(splashRunnable, 5000);

        if (fabCenterLocation != null) {
            fabCenterLocation.setOnClickListener(v -> {
                Log.d(TAG, "Center location FAB clicked");
                if (checkLocationPermission()) {
                    locationn.getDeviceLocation(location -> {
                        mMap.centerOnLocation(location, true);
                        getDeviceLocation();
                    });
                } else {
                    requestLocationPermission();
                }
            });
        }

        if (fab_user != null) {
            fab_user.setOnClickListener(v -> {
                Log.d(TAG, "User FAB clicked");
                navigateToScreen(RegistrerUser.class, "Navegando a la pantalla de registro");
            });
        }

        if (fabAyuda != null) {
            fabAyuda.setOnClickListener(v -> {
                Log.d(TAG, "Ayuda FAB clicked");
                navigateToScreen(Ayuda.class, "Navegando a la pantalla de ayuda");
            });
        }

        if (fabSave != null) {
            fabSave.setOnClickListener(v -> {
                LatLng currentLocation = locationn.getCurrentLocation();
                if (currentLocation != null) {
                    guardarConMiniVentana(currentLocation);
                } else {
                    guardarConMiniVentanaVoz();
                }
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: No se pudo encontrar el fragmento del mapa");
            Toast.makeText(this, "Error al cargar el mapa", Toast.LENGTH_SHORT).show();
        }

        registerMapSpecificCommands();
    }
    private void darBienvenidaEntusiasta() {
        // Array de mensajes de bienvenida entusiastas
        String[] mensajesBienvenida = {
                "¡Hola! ¡Bienvenido a Segi, tu asistente de navegación favorito! ¿A dónde te gustaría ir hoy?. Recuerda decir Okey Segi para activar el reconocimiento de voz.",
                "¡Qué alegría tenerte aquí! Soy Segi, tu compañero de aventuras. ¡Vamos a explorar juntos!.  Recuerda decir Okey Segi para activar el reconocimiento de voz.",
                "¡Bienvenido a bordo! Soy Segi y estoy súper emocionado de ayudarte a llegar a donde necesites ir.   Recuerda decir Okey Segi para activar el reconocimiento de voz.",
                "¡Excelente! Has llegado a Segi, tu guía personal de navegación. ¡Prepárate para una experiencia increíble!, Recuerda decir Okey Segi para activar el reconocimiento de voz.",
                "¡Hola! Soy Segi, tu asistente de navegación. ¡Estoy listo para llevarte a cualquier lugar que desees, Recuerda decir Okey Segi para activar el reconocimiento de voz!"
        };

        // Seleccionar un mensaje aleatorio para variedad
        String mensajeBienvenida = mensajesBienvenida[(int) (Math.random() * mensajesBienvenida.length)];

        // Usar Handler para asegurar que TTS esté listo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (ttsManager != null && ttsManager.isInitialized()) {
                Log.d(TAG, "Reproduciendo mensaje de bienvenida: " + mensajeBienvenida);
                ttsManager.speak(mensajeBienvenida, null);
            } else {
                Log.w(TAG, "TTS Manager no está listo, reintentando en 1 segundo...");
                // Reintentar después de 1 segundo más
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (ttsManager != null && ttsManager.isInitialized()) {
                        Log.d(TAG, "Segundo intento - Reproduciendo mensaje de bienvenida");
                        ttsManager.speak(mensajeBienvenida, null);
                    } else {
                        Log.e(TAG, "TTS Manager sigue sin estar listo después del segundo intento");
                        // Como fallback, mostrar un Toast
                        Toast.makeText(this, "¡Bienvenido a Segi!", Toast.LENGTH_LONG).show();
                    }
                }, 1000);
            }
        }, 500); // Esperar 500ms inicialmente
    }

    private void darBienvenidaConCallback() {
        String mensajeBienvenida = "¡Hola! ¡Bienvenido a Segi, tu asistente de navegación! Estoy súper emocionado de ayudarte. ¿A dónde quieres ir hoy?";

        // Verificar si TTS está listo y dar bienvenida
        verificarTTSYhablar(mensajeBienvenida, 0);
    }

    private void verificarTTSYhablar(String mensaje, int intentos) {
        if (intentos >= 5) {
            Log.e(TAG, "Máximo número de intentos alcanzado para TTS");
            Toast.makeText(this, "¡Bienvenido a Segi!", Toast.LENGTH_LONG).show();
            return;
        }

        if (ttsManager != null && ttsManager.isInitialized()) {
            Log.d(TAG, "TTS listo, reproduciendo bienvenida en intento: " + (intentos + 1));
            ttsManager.speak(mensaje, null);
        } else {
            Log.d(TAG, "TTS no listo, reintentando... Intento: " + (intentos + 1));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                verificarTTSYhablar(mensaje, intentos + 1);
            }, 300 * (intentos + 1)); // Incrementar el delay con cada intento
        }
    }

    private void registerMapSpecificCommands() {
        if (speedRecognizer != null) {
            speedRecognizer.addCustomCommand("ubicación", context -> centerOnUserLocation());
            speedRecognizer.addCustomCommand("ubicacion", context -> centerOnUserLocation());
            speedRecognizer.addCustomCommand("centrar", context -> centerOnUserLocation());
            speedRecognizer.addCustomCommand("centrar mapa", context -> centerOnUserLocation());
            speedRecognizer.addCustomCommand("accesibilidad", context -> showAccessibilityMenu());
        } else {
            Log.e(TAG, "Error: speedRecognizer es null, no se pueden registrar comandos");
        }
    }

    private void centerOnUserLocation() {
        if (checkLocationPermission()) {
            if (ttsManager != null) {
                ttsManager.speak("Centrando el mapa en tu ubicación actual", null);
            }
            locationn.getDeviceLocation(location -> {
                if (mMap != null) {
                    mMap.centerOnLocation(location, true);
                    getDeviceLocation();
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

        if (isWaitingForLocationName) {
            handleLocationNameInput(command);
            return;
        }

        if (command.contains("login") || command.contains("iniciar sesión") ||
                command.contains("iniciar sesion") || command.contains("ingresar")) {
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
        }
    }

    private void handleLocationNameInput(String locationName) {
        Log.d(TAG, "=== MANEJANDO ENTRADA DE NOMBRE DE UBICACIÓN ===");
        Log.d(TAG, "Nombre recibido: '" + locationName + "'");
        Log.d(TAG, "isWaitingForSaveName: " + isWaitingForSaveName);
        Log.d(TAG, "isWaitingForDeleteName: " + isWaitingForDeleteName);
        Log.d(TAG, "isWaitingForSaveConfirmation: " + isWaitingForSaveConfirmation);

        String cleanName = locationName.trim();

        // Si estamos esperando confirmación de guardado
        if (isWaitingForSaveConfirmation) {
            handleSaveConfirmation(cleanName);
            return;
        }

        speedRecognizer.setDataEntryMode(false);
        isWaitingForLocationName = false;

        if (isWaitingForSaveName) {
            isWaitingForSaveName = false;
            if (cleanName.isEmpty()) {
                if (ttsManager != null) {
                    ttsManager.speak("No entendí el nombre. Por favor, intenta de nuevo.", null);
                }
                Toast.makeText(this, "Nombre no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar el nombre y ubicación pendientes
            pendingLocationName = cleanName;

            if (checkLocationPermission()) {
                locationn.getDeviceLocation(location -> {
                    if (location != null) {
                        pendingLocation = location;
                        // Pedir confirmación antes de guardar
                        askForSaveConfirmation(cleanName);
                    } else {
                        if (ttsManager != null) {
                            ttsManager.speak("No pude obtener tu ubicación actual", null);
                        }
                        Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                requestLocationPermission();
            }
        } else if (isWaitingForDeleteName) {
            isWaitingForDeleteName = false;
            eliminarUbicacionPorVoz(cleanName);
        }
    }
    private void askForSaveConfirmation(String locationName) {
        Log.d(TAG, "=== PIDIENDO CONFIRMACIÓN DE GUARDADO ===");
        Log.d(TAG, "Nombre de ubicación: " + locationName);

        isWaitingForSaveConfirmation = true;
        isWaitingForLocationName = true;

        if (ttsManager != null) {
            ttsManager.speak("Guardando ubicación. ¿Desea guardar con el nombre " + locationName + "? Confirme diciendo sí o no.", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.setDataEntryMode(true);
                    speedRecognizer.startVoiceRecognition();
                }
            });
        }
    }
    private void handleSaveConfirmation(String response) {
        Log.d(TAG, "=== MANEJANDO CONFIRMACIÓN DE GUARDADO ===");
        Log.d(TAG, "Respuesta recibida: '" + response + "'");
        Log.d(TAG, "Nombre pendiente: " + pendingLocationName);
        Log.d(TAG, "Ubicación pendiente: " + pendingLocation);

        speedRecognizer.setDataEntryMode(false);
        isWaitingForLocationName = false;
        isWaitingForSaveConfirmation = false;

        String cleanResponse = response.toLowerCase().trim();

        // Verificar si la respuesta es afirmativa
        if (cleanResponse.contains("sí") || cleanResponse.contains("si") ||
                cleanResponse.contains("yes") || cleanResponse.equals("s") ||
                cleanResponse.contains("confirmar") || cleanResponse.contains("acepto") ||
                cleanResponse.contains("correcto") || cleanResponse.contains("ok") ||
                cleanResponse.contains("okey")) {

            Log.d(TAG, "Confirmación POSITIVA - Guardando ubicación");

            if (pendingLocation != null && !pendingLocationName.isEmpty()) {
                // Guardar la ubicación
                markerUI.savePlace(pendingLocationName, pendingLocation);

                if (ttsManager != null) {
                    ttsManager.speak("Ubicación guardada exitosamente como " + pendingLocationName, null);
                }
                Toast.makeText(this, "Ubicación guardada como: " + pendingLocationName, Toast.LENGTH_SHORT).show();

            } else {
                Log.e(TAG, "Error: Datos pendientes son nulos");
                if (ttsManager != null) {
                    ttsManager.speak("Error al guardar la ubicación. Intenta de nuevo.", null);
                }
                Toast.makeText(this, "Error al guardar la ubicación", Toast.LENGTH_SHORT).show();
            }

        } else if (cleanResponse.contains("no") || cleanResponse.contains("negar") ||
                cleanResponse.contains("cancelar") || cleanResponse.contains("cancel") ||
                cleanResponse.contains("rechazar")) {

            Log.d(TAG, "Confirmación NEGATIVA - Cancelando guardado");

            if (ttsManager != null) {
                ttsManager.speak("Guardado cancelado", null);
            }
            Toast.makeText(this, "Guardado cancelado", Toast.LENGTH_SHORT).show();

        } else {
            Log.d(TAG, "Respuesta no reconocida - Pidiendo nueva confirmación");

            if (ttsManager != null) {
                ttsManager.speak("No entendí tu respuesta. Por favor di sí para confirmar o no para cancelar.", () -> {
                    isWaitingForSaveConfirmation = true;
                    isWaitingForLocationName = true;
                    if (speedRecognizer != null) {
                        speedRecognizer.setDataEntryMode(true);
                        speedRecognizer.startVoiceRecognition();
                    }
                });
            }
            return; // No limpiar las variables pendientes
        }

        // Limpiar variables pendientes
        pendingLocationName = "";
        pendingLocation = null;
    }



    @Override
    protected void handleNavigationCommand(String destination) {
        Log.d(TAG, "=== COMANDO DE NAVEGACIÓN RECIBIDO EN MAPAUI ===");
        Log.d(TAG, "Destino: '" + destination + "'");
        Log.d(TAG, "isNavigating actual: " + isNavigating);

        if (destination == null || destination.trim().isEmpty()) {
            Log.e(TAG, "Destino vacío o nulo");
            if (ttsManager != null) {
                ttsManager.speak("No entendí el destino. Por favor, intenta de nuevo diciendo 'navega a' seguido del lugar", null);
            }
            return;
        }

        String cleanDestination = destination.trim();
        Log.d(TAG, "Destino limpio: '" + cleanDestination + "'");

        if (ttsManager != null && ttsManager.isInitialized()) {
            ttsManager.speak("Navegando a " + cleanDestination, () -> {
                isNavigating = true;
                searchAndNavigate(cleanDestination);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isNavigating = false;
                    Log.d(TAG, "Flag isNavigating reseteado después de navegación");
                }, 2000);
            });
        } else {
            isNavigating = true;
            searchAndNavigate(cleanDestination);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isNavigating = false;
                Log.d(TAG, "Flag isNavigating reseteado después de navegación");
            }, 2000);
        }
    }

    private void navigateToScreen(Class<?> destinationClass, String speechText) {
        isNavigating = true;
        Log.d(TAG, "Dirigiendo a la pantalla: " + destinationClass.getSimpleName());
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
                getDeviceLocation();
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
                    locationn.getDeviceLocation(location -> mMap.centerOnLocation(location, true));
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
        Log.d(TAG, "=== INICIANDO BÚSQUEDA Y NAVEGACIÓN ===");
        Log.d(TAG, "Buscando: '" + name + "'");

        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                final Ubicacion datoObtenido = db.ubicacionDAO().getUbicacionByNombre(name);
                Log.d(TAG, "Consulta BD completada para: " + name);
                Log.d(TAG, "Resultado BD: " + (datoObtenido != null ? datoObtenido.toString() : "null"));

                mainHandler.post(() -> {
                    if (datoObtenido != null) {
                        Log.d(TAG, "Ubicación encontrada en BD - Lat: " + datoObtenido.getLatitud() + ", Lng: " + datoObtenido.getLongitud());
                        LatLng latLng = new LatLng(datoObtenido.getLatitud(), datoObtenido.getLongitud());
                        navigateByLatLng(latLng);
                    } else {
                        Log.d(TAG, "Ubicación no encontrada en BD, usando geocoding para: " + name);
                        navigateByName(name);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error en searchAndNavigate: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    if (ttsManager != null) {
                        ttsManager.speak("Error al buscar la ubicación. Intenta de nuevo.", null);
                    }
                    Toast.makeText(MapaUI.this, "Error al buscar la ubicación", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarMiniVentana() {
        if (miniWindow != null) {
            miniWindow.setVisibility(View.VISIBLE);
            Log.d(TAG, "MiniWindow set to VISIBLE");
        } else {
            Log.e(TAG, "MiniWindow is null - check activity_mapa_ui layout XML");
            Toast.makeText(this, "Error al mostrar ventana de guardado", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarConMiniVentana(String placeId, String placeName, LatLng ubi) {
        if (miniWindow == null) {
            Log.e(TAG, "MiniWindow is null");
            Toast.makeText(this, "Error al mostrar ventana de guardado", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarMiniVentana();
        MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
        MaterialButton btnOK = miniWindow.findViewById(R.id.btn_confirm);
        EditText etPlaceName = miniWindow.findViewById(R.id.edit_name);
        TextView etWindowTitle = miniWindow.findViewById(R.id.mw_tv_title);

        if (btnCancel != null && btnOK != null && etPlaceName != null && etWindowTitle != null) {
            etWindowTitle.setText("GUARDAR UBICACIÓN");
            etPlaceName.setText(placeName);
            btnCancel.setOnClickListener(v -> {
                Log.d(TAG, "Cancel button clicked");
                miniWindow.setVisibility(View.GONE);
            });
            btnOK.setOnClickListener(v -> {
                String name = etPlaceName.getText().toString().trim();
                if (!name.isEmpty()) {
                    markerUI.savePlace(placeId, name, ubi);
                    miniWindow.setVisibility(View.GONE);
                    if (ttsManager != null) {
                        ttsManager.speak("Ubicación guardada como " + name, null);
                    }
                    Toast.makeText(this, "Ubicación guardada como: " + name, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "MiniWindow components are null - check mini_window layout XML");
            miniWindow.setVisibility(View.GONE);
        }
    }

    private void guardarConMiniVentana(LatLng latLng) {
        guardarConMiniVentana(latLng, "");
    }

    private void guardarConMiniVentana(LatLng latLng, String prefilledName) {
        if (latLng == null) {
            Log.e(TAG, "LatLng is null");
            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (miniWindow == null) {
            Log.e(TAG, "MiniWindow is null");
            Toast.makeText(this, "Error al mostrar ventana de guardado", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarMiniVentana();
        MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
        MaterialButton btnOK = miniWindow.findViewById(R.id.btn_confirm);
        EditText etPlaceName = miniWindow.findViewById(R.id.edit_name);
        TextView etWindowTitle = miniWindow.findViewById(R.id.mw_tv_title);

        if (btnCancel != null && btnOK != null && etPlaceName != null && etWindowTitle != null) {
            etWindowTitle.setText("GUARDAR UBICACIÓN");
            etPlaceName.setText(prefilledName);

            btnCancel.setOnClickListener(v -> {
                Log.d(TAG, "Cancel button clicked");
                miniWindow.setVisibility(View.GONE);
            });

            btnOK.setOnClickListener(v -> {
                String placeName = etPlaceName.getText().toString().trim();
                if (!placeName.isEmpty()) {
                    markerUI.savePlace(placeName, latLng);
                    miniWindow.setVisibility(View.GONE);
                    if (ttsManager != null) {
                        ttsManager.speak("Ubicación guardada como " + placeName, null);
                    }
                    Toast.makeText(this, "Ubicación guardada como: " + placeName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "MiniWindow components are null - check mini_window layout XML");
            miniWindow.setVisibility(View.GONE);
        }
    }

    private void guardarConMiniVentanaVoz() {
        if (checkLocationPermission()) {
            locationn.getDeviceLocation(location -> {
                if (location != null) {
                    ttsManager.speak("Guardando ubicación actual confirma con si o no para guardar" +
                            "ubicacion como "+ location + " en el mapa", null);

                                    guardarConMiniVentana(location);
                    handleLocationNameInput(""+location);


                } else {
                    if (ttsManager != null) {
                        ttsManager.speak("No pude obtener tu ubicación actual", null);
                    }
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestLocationPermission();
        }
    }

    private void eliminarConMiniVentana(String placeName) {
        if (miniWindow == null) {
            Log.e(TAG, "MiniWindow is null");
            Toast.makeText(this, "Error al mostrar ventana de eliminación", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarMiniVentana();
        MaterialButton btnCancel = miniWindow.findViewById(R.id.btn_cancel);
        MaterialButton btnOK = miniWindow.findViewById(R.id.btn_confirm);
        EditText etPlaceName = miniWindow.findViewById(R.id.edit_name);
        TextView etWindowTitle = miniWindow.findViewById(R.id.mw_tv_title);

        if (btnCancel != null && btnOK != null && etPlaceName != null && etWindowTitle != null) {
            etWindowTitle.setText("¿ELIMINAR UBICACIÓN?");
            etPlaceName.setText(placeName);
            etPlaceName.setEnabled(false);

            btnCancel.setOnClickListener(v -> {
                miniWindow.setVisibility(View.GONE);
                if (ttsManager != null) {
                    ttsManager.speak("Eliminación cancelada", null);
                }
            });

            btnOK.setOnClickListener(v -> {
                markerUI.deleteUbication(placeName);
                miniWindow.setVisibility(View.GONE);
                if (ttsManager != null) {
                    ttsManager.speak("Ubicación " + placeName + " eliminada", null);
                }
                Toast.makeText(this, "Ubicación eliminada: " + placeName, Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "MiniWindow components are null - check mini_window layout XML");
            miniWindow.setVisibility(View.GONE);
        }
    }

    private void eliminarUbicacionPorVoz(String locationName) {
        if (ttsManager != null) {
            ttsManager.speak("¿Estás seguro de que quieres eliminar " + locationName + "?", () -> {
                eliminarConMiniVentana(locationName);
            });
        } else {
            eliminarConMiniVentana(locationName);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
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
        Log.d(TAG, "onNavigationCommand llamado (método legacy): " + destination);
    }

    @Override
    public void onSaveLocationCommand() {
    }

    @Override
    public void onDeleteLocationCommand(String locationName) {
    }
}