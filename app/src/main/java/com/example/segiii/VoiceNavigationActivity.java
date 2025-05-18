package com.example.segiii;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class VoiceNavigationActivity extends AppCompatActivity {
    protected wordSegui wordSegui;
    protected SpeedRecognizer speedRecognizer;
    protected TTSManager ttsManager;
    protected static final String PICOVOICE_ACCESS_KEY = " tu clave a qui";

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
                // Llamar al método abstracto para permitir comportamientos específicos
                // en cada actividad que extienda esta clase
                handleVoiceCommand(command, result);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(VoiceNavigationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Agregar comandos específicos para esta actividad si es necesario
        // Por ejemplo, un comando para salir de la aplicación podría ser común
        speedRecognizer.addCustomCommand("salir", (context) -> {
            finishAffinity();
        });

        // Inicializar el detector de la palabra clave "Okey Segui"
        wordSegui = new wordSegui(this);
        wordSegui.initializeAndStartListening(PICOVOICE_ACCESS_KEY, () -> {
            Toast.makeText(this, "Hotword 'Okey Segui' detectado", Toast.LENGTH_SHORT).show();
            // Reproducir un sonido de confirmación sería bueno aquí
            ttsManager.speak("Te escucho", () -> {
                speedRecognizer.startVoiceRecognition();
            });
        });
    }


    protected abstract void handleVoiceCommand(String command, String result);


    protected void handleNoCommand() {
        ttsManager.speak("Disculpe, no reconozco ese comando. Por favor, intente con un comando válido.", () -> {
            speedRecognizer.startVoiceRecognition();
        });
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
        // Detener TTS si está hablando
        if (ttsManager != null) {
            ttsManager.stop();
        }
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
    }
}