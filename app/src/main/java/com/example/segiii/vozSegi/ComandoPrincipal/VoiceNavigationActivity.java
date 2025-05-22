package com.example.segiii.vozSegi.ComandoPrincipal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class VoiceNavigationActivity extends AppCompatActivity {
    protected wordSegui wordSegui;
    protected SpeedRecognizer speedRecognizer;
    protected TTSManager ttsManager;
    protected static final String PICOVOICE_ACCESS_KEY = "clave_pico";
    private static final String TAG = "VoiceNavigation";
    private boolean isActivityActive = false;
    // Bandera para controlar si el reconocimiento está activo o fue cancelado manualmente
    private boolean isRecognitionEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeVoiceComponents();
    }

    protected void initializeVoiceComponents() {
        // Inicializar el gestor de texto a voz
        ttsManager = new TTSManager(this, null);

        // Inicializar el reconocedor de voz con manejo centralizado de comandos
        speedRecognizer = new SpeedRecognizer(this, new SpeedRecognizer.OnVoiceCommandListener() {
            @Override
            public void onCommandProcessed(String command, String result) {
                Log.d(TAG, "Comando procesado: " + command + ", resultado: " + result);
                // Primero mostramos el resultado
                Toast.makeText(VoiceNavigationActivity.this, result, Toast.LENGTH_SHORT).show();

                // Luego llamamos al método abstracto para permitir comportamientos específicos
                // en cada actividad que extienda esta clase
                if (isActivityActive) {
                    handleVoiceCommand(command.toLowerCase(), result);
                } else {
                    Log.d(TAG, "Ignorando comando: actividad no está activa");
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
                // Marcar que el reconocimiento está inactivo
                isRecognitionEnabled = false;
                Toast.makeText(VoiceNavigationActivity.this, "Reconocimiento desactivado. Di 'Okey Segui' para activar", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNavigationCommand(String destination) {

            }
        });

        // Inicializar el detector de la palabra clave "Okey Segui"
        initializeHotwordDetection();
    }

    protected void initializeHotwordDetection() {
        try {
            // Si ya existe una instancia, limpiarla primero
            if (wordSegui != null) {
                wordSegui.cleanup();
            }

            wordSegui = new wordSegui(this);
            wordSegui.initializeAndStartListening(PICOVOICE_ACCESS_KEY, () -> {
                if (isActivityActive) {
                    Toast.makeText(this, "Hotword 'Okey Segui' detectado", Toast.LENGTH_SHORT).show();

                    // Verificar si el reconocimiento está habilitado o fue desactivado manualmente
                    if (isRecognitionEnabled) {
                        // Si ya está habilitado, activar el reconocimiento
                        ttsManager.speak("Te escucho", () -> {
                            speedRecognizer.startVoiceRecognition();
                        });
                    } else {
                        // Si estaba deshabilitado, volver a habilitarlo
                        isRecognitionEnabled = true;
                        ttsManager.speak("Reconocimiento de voz activado. Te escucho", () -> {
                            speedRecognizer.startVoiceRecognition();
                        });
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

    /**
     * Método abstracto que debe ser implementado por las clases hijas para manejar
     * los comandos de voz específicos de cada pantalla.
     * @param command El comando reconocido en minúsculas
     * @param result Resultado del procesamiento del comando
     */
    protected abstract void handleVoiceCommand(String command, String result);

    protected void handleNoCommand() {
        ttsManager.speak("Disculpe, no reconozco ese comando. Por favor, intente con un comando válido.", () -> {
            // Solo iniciamos el reconocimiento si está habilitado
            if (isRecognitionEnabled) {
                speedRecognizer.startVoiceRecognition();
            }
        });
    }

    /**
     * Método para activar manualmente el reconocimiento de voz
     */
    protected void startVoiceListening() {
        // Verificar si el reconocimiento está habilitado
        if (isRecognitionEnabled) {
            ttsManager.speak("Te escucho", () -> {
                speedRecognizer.startVoiceRecognition();
            });
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
        // Marcar la actividad como inactiva
        isActivityActive = false;

        // Detener TTS si está hablando
        if (ttsManager != null) {
            ttsManager.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Marcar la actividad como activa
        isActivityActive = true;

        // Asegurar que el detector de hotword esté funcionando
        if (wordSegui != null && !wordSegui.isListening()) {
            initializeHotwordDetection();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos
        if (wordSegui != null) {
            wordSegui.cleanup();
        }
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        isActivityActive = false;
    }
}