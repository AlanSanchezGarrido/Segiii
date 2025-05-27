package com.example.segiii.vozSegi.ComandoPrincipal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class VoiceNavigationActivity extends AppCompatActivity {
    protected wordSegui wordSegui;
    protected SpeedRecognizer speedRecognizer;
    protected TTSManager ttsManager;
    protected static final String PICOVOICE_ACCESS_KEY = "";
    private static final String TAG = "VoiceNavigation";
    protected boolean isActivityActive = false;
    private boolean isRecognitionEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeVoiceComponents();
    }

    protected abstract void handleSaveLocationCommand();
    protected abstract void handleDeleteLocationCommand(String locationName);

    protected void initializeVoiceComponents() {
        ttsManager = new TTSManager(this, null);
        speedRecognizer = new SpeedRecognizer(this, new SpeedRecognizer.OnVoiceCommandListener() {
            @Override
            public void onCommandProcessed(String command, String result) {
                Log.d(TAG, "Comando procesado: " + command + ", resultado: " + result);
                Toast.makeText(VoiceNavigationActivity.this, result, Toast.LENGTH_SHORT).show();

                // CORRECCIÓN: Permitir procesamiento para comandos críticos y modo de entrada de datos
                boolean shouldProcess = isActivityActive ||
                        speedRecognizer.isDataEntryMode() ||
                        result.contains("pidiendo nombre") ||
                        result.contains("eliminando ubicación") ||
                        result.contains("guardando ubicación");

                if (shouldProcess) {
                    handleVoiceCommand(command.toLowerCase(), result);
                } else {
                    Log.d(TAG, "Ignorando comando: actividad no está activa y no es comando crítico");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error en reconocimiento de voz: " + errorMessage);
                Toast.makeText(VoiceNavigationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecognitionCancelled() {
                Log.d(TAG, "Reconocimiento de voz cancelado manualmente");
                isRecognitionEnabled = false;
                Toast.makeText(VoiceNavigationActivity.this, "Reconocimiento desactivado. Di 'Okey Segui' para activar", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNavigationCommand(String destination) {
                Log.d(TAG, "=== PROCESANDO COMANDO DE NAVEGACIÓN ===");
                Log.d(TAG, "Destino recibido: '" + destination + "'");
                Log.d(TAG, "isActivityActive: " + isActivityActive);

                if (isActivityActive) {
                    Log.d(TAG, "Actividad activa, procesando comando de navegación");
                    handleNavigationCommand(destination);
                } else {
                    Log.w(TAG, "Actividad no activa, pero procesando comando de navegación crítico");
                    handleNavigationCommand(destination);
                }
            }

            @Override
            public void onSaveLocationCommand() {
                Log.d(TAG, "Comando de guardar ubicación recibido");
                Log.d(TAG, "Estado de actividad: " + isActivityActive);

                if (isActivityActive) {
                    handleSaveLocationCommand();
                } else {
                    Log.w(TAG, "Actividad no activa, pero procesando comando crítico de guardar ubicación");
                    handleSaveLocationCommand();
                }
            }

            @Override
            public void onDeleteLocationCommand(String locationName) {
                Log.d(TAG, "Comando de eliminar ubicación recibido: " + locationName);
                Log.d(TAG, "Estado de actividad: " + isActivityActive);

                // CORRECCIÓN: Los comandos de eliminación son críticos, procesarlos siempre
                if (isActivityActive) {
                    handleDeleteLocationCommand(locationName);
                } else {
                    Log.w(TAG, "Actividad no completamente activa, pero procesando comando crítico de eliminación");
                    // Usar un Handler para asegurar que se procese después del onResume
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        handleDeleteLocationCommand(locationName);
                    }, 100);
                }
            }
        });

        initializeHotwordDetection();
    }

    protected void initializeHotwordDetection() {
        try {
            if (wordSegui != null) {
                wordSegui.cleanup();
            }
            wordSegui = new wordSegui(this);
            wordSegui.initializeAndStartListening(PICOVOICE_ACCESS_KEY, () -> {
                if (isActivityActive) {
                    Toast.makeText(this, "Hotword 'Okey Segui' detectado", Toast.LENGTH_SHORT).show();
                    if (isRecognitionEnabled) {
                        ttsManager.speak("Te escucho", () -> speedRecognizer.startVoiceRecognition());
                    } else {
                        isRecognitionEnabled = true;
                        ttsManager.speak("Reconocimiento de voz activado. Te escucho", () -> speedRecognizer.startVoiceRecognition());
                    }
                } else {
                    Log.d(TAG, "Hotword detectado pero la actividad no está activa");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando detector de hotword: " + e.getMessage());
            Toast.makeText(this, "Error al inicializar detector de palabra clave", Toast.LENGTH_SHORT).show();
        }
    }

    protected abstract void handleVoiceCommand(String command, String result);
    protected abstract void handleNavigationCommand(String destination);

    protected void handleNoCommand() {
        ttsManager.speak("Disculpe, no reconozco ese comando. Por favor, intente con un comando válido.", () -> {
            if (isRecognitionEnabled) {
                speedRecognizer.startVoiceRecognition();
            }
        });
    }

    protected void startVoiceListening() {
        if (isRecognitionEnabled) {
            ttsManager.speak("Te escucho", () -> speedRecognizer.startVoiceRecognition());
        } else {
            ttsManager.speak("El reconocimiento de voz está desactivado. Di 'Okey Segui' para activarlo.", null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeedRecognizer.getSpeechRequestCode()) {
            speedRecognizer.processVoiceResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause - Marcando actividad como inactiva");
        isActivityActive = false;
        if (ttsManager != null) {
            ttsManager.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Marcando actividad como activa");
        isActivityActive = true;
        if (wordSegui != null && !wordSegui.isListening()) {
            initializeHotwordDetection();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart - Marcando actividad como activa");
        isActivityActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop - Marcando actividad como inactiva");
        isActivityActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - Limpiando recursos");
        if (wordSegui != null) {
            wordSegui.cleanup();
        }
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        isActivityActive = false;
    }
}