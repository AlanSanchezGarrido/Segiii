package com.example.segiii.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;

import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.example.segiii.vozSegi.ComandoPrincipal.TTSManager;

public class Ayuda extends VoiceNavigationActivity {

    private boolean isVideoPlaying = false;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);

        // Configurar el botón de retroceso
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Ayuda.this, MapaUI.class);
            startActivity(intent);
            finish();
        });

        videoView = findViewById(R.id.video_view);

        // Agregar comandos específicos para la vista de ayuda
        speedRecognizer.addCustomCommand("reproducir", (context) -> playVideo());
        speedRecognizer.addCustomCommand("detener", (context) -> stopVideo());
        speedRecognizer.addCustomCommand("sí", (context) -> playVideo());
        speedRecognizer.addCustomCommand("no", (context) -> stopVideo());

        // Inicializar el TTS con un callback para hablar cuando esté listo
        ttsManager = new TTSManager(this, initialized -> {
            if (initialized) {
                speakWelcomeMessage();
            }
        });
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        // La mayoría de comandos ya están manejados en SpeedRecognizer
        // Este método solo maneja casos especiales para esta actividad

        if (command.contains("sí") || command.contains("si")) {
            playVideo();
        } else if (command.contains("no")) {
            String message = "De acuerdo no reproduciré el video. ¿En qué más puedo ayudarte?";
            ttsManager.speak(message, () -> {
                speedRecognizer.startVoiceRecognition();
            });
        } else if (!speedRecognizer.isValidCommand(command) && !command.isEmpty()) {
            // Si el comando no está registrado y no está vacío
            handleNoCommand();
        }
    }

    private void speakWelcomeMessage() {
        String message = "Hola Bienvenido, a la ayuda de Segi. " +
                "¿Quieres que reproduzca un video para mostrarte cómo navegar en nuestra aplicación? " +
                "Dime sí o no.";
        ttsManager.speak(message, () -> {
            speedRecognizer.startVoiceRecognition();
        });
    }

    private void playVideo() {
        if (!isVideoPlaying) {
            isVideoPlaying = true;
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videosegi);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(mp -> {
                ttsManager.speak("Reproduciendo video tutorial. Puedes decir 'detener' para pararlo o 'pausar' para pausarlo.", null);
                videoView.start();
            });

            // Agregar un listener para cuando el video termina
            videoView.setOnCompletionListener(mp -> {
                isVideoPlaying = false;
                ttsManager.speak("El video ha terminado. ¿En qué más puedo ayudarte?", () -> {
                    speedRecognizer.startVoiceRecognition();
                });
            });
        }
    }

    private void stopVideo() {
        if (isVideoPlaying) {
            isVideoPlaying = false;
            videoView.stopPlayback();
            ttsManager.speak("Video detenido. ¿En qué más puedo ayudarte?", () -> {
                speedRecognizer.startVoiceRecognition();
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar la reproducción del video si estaba reproduciéndose
        if (isVideoPlaying && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar el video cuando la actividad pasa a segundo plano
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }
}