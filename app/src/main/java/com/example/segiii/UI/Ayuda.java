package com.example.segiii.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;

import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;

public class Ayuda extends VoiceNavigationActivity {

    private static final String TAG = "Ayuda";
    private boolean isVideoPlaying = false;
    private VideoView videoView;
    private Handler handler;

    @Override
    protected void handleSaveLocationCommand() {

    }

    @Override
    protected void handleDeleteLocationCommand(String locationName) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);

        // Inicializar el Handler para operaciones diferidas
        handler = new Handler(Looper.getMainLooper());

        // Inicializar el componente de voz (llama al método de la clase padre)
        super.initializeVoiceComponents();

        // Configurar el botón de retroceso
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> navigateBack());

        // Inicializar el reproductor de video
        videoView = findViewById(R.id.video_view);

        // Agregar comandos específicos para la vista de ayuda
        addCustomCommands();

        // Iniciar el mensaje de bienvenida después de un breve retraso
        handler.postDelayed(this::speakWelcomeMessage, 1000);
    }

    /**
     * Agrega comandos específicos para esta actividad
     */
    private void addCustomCommands() {
        if (speedRecognizer != null) {
            // Comando para reproducir el video
            speedRecognizer.addCustomCommand("reproducir", (context) -> {
                Log.d(TAG, "Comando de voz: reproducir video");
                playVideo();
            });

            // Comando para pausar el video
            speedRecognizer.addCustomCommand("pausar", (context) -> {
                Log.d(TAG, "Comando de voz: pausar video");
                pauseVideo();
            });

            // Comando para detener el video
            speedRecognizer.addCustomCommand("detener", (context) -> {
                Log.d(TAG, "Comando de voz: detener video");
                stopVideo();
            });

            // Comando para reiniciar el video
            speedRecognizer.addCustomCommand("reiniciar", (context) -> {
                Log.d(TAG, "Comando de voz: reiniciar video");
                restartVideo();
            });

            // Comandos de sí/no para responder a preguntas
            speedRecognizer.addCustomCommand("sí", (context) -> {
                Log.d(TAG, "Comando de voz: sí");
                handleYesCommand();
            });

            speedRecognizer.addCustomCommand("si", (context) -> {
                Log.d(TAG, "Comando de voz: si");
                handleYesCommand();
            });

            speedRecognizer.addCustomCommand("no", (context) -> {
                Log.d(TAG, "Comando de voz: no");
                handleNoCommand();
            });
        } else {
            Log.e(TAG, "SpeedRecognizer no está inicializado");
        }
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        Log.d(TAG, "Procesando comando de voz: " + command);

        // Como los comandos ya están registrados con el SpeedRecognizer,
        // aquí solo manejamos casos específicos o no registrados

        if (command.contains("reproduc") || command.contains("play") || command.contains("iniciar")) {
            playVideo();
        }
        else if (command.contains("pausa")) {
            pauseVideo();
        }
        else if (command.contains("detener") || command.contains("parar") || command.contains("stop")) {
            stopVideo();
        }
        else if (command.contains("reinicia") || command.contains("restart")) {
            restartVideo();
        }
        else if (command.contains("sí") || command.contains("si") || command.equals("yes")) {
            handleYesCommand();
        }
        else if (command.contains("no") || command.equals("nope")) {
            handleNoCommand();
        }
        else if (command.contains("volver") || command.contains("regresar") || command.contains("atrás") ||
                command.contains("atras") || command.contains("back")) {
            navigateBack();
        }
        else if (command.contains("mapa")) {
            navigateToMap();
        }
        else if (command.contains("login") || command.contains("iniciar sesión") || command.contains("ingresar")) {
            navigateToLogin();
        }
        else if (command.contains("registr")) {
            navigateToRegister();
        }
        else {
            // Si no es ninguno de los comandos específicos, informamos que no entendimos
            // Nota: No llamamos a super.handleNoCommand() ya que podría reiniciar el reconocimiento
            // en lugar de eso, manejamos la respuesta nosotros mismos
            ttsManager.speak("No he entendido ese comando. Puedes decir 'reproducir', 'pausar', 'detener', 'reiniciar', o 'volver'.",
                    () -> {
                        if (speedRecognizer != null) {
                            speedRecognizer.startVoiceRecognition();
                        }
                    });
        }
    }

    @Override
    protected void handleNavigationCommand(String destination) {

    }

    /**
     * Maneja el comando "sí"
     */
    private void handleYesCommand() {
        if (!isVideoPlaying) {
            playVideo();
        } else {
            ttsManager.speak("El video ya está reproduciéndose. ¿Deseas verlo desde el principio? Di sí para reiniciar o no para continuar.",
                    () -> {
                        if (speedRecognizer != null) {
                            speedRecognizer.startVoiceRecognition();
                        }
                    });
        }
    }

    /**
     * Maneja el comando "no"
     */
    public void handleNoCommand() {
        if (!isVideoPlaying) {
            ttsManager.speak("De acuerdo, no reproduciré el video. Dime 'mapa' para ir al mapa, 'login' para iniciar sesión o 'registrar' para crear una cuenta.",
                    () -> {
                        if (speedRecognizer != null) {
                            speedRecognizer.startVoiceRecognition();
                        }
                    });
        } else {
            // Si el video está reproduciéndose, detenerlo
            stopVideo();
        }
    }

    /**
     * Reproduce el mensaje de bienvenida
     */
    private void speakWelcomeMessage() {
        String message = "Bienvenido a la ayuda de Segi. " +
                "¿Quieres que reproduzca un video tutorial para mostrarte cómo navegar en nuestra aplicación? " +
                "Dime sí o no.";

        ttsManager.speak(message, () -> {
            if (speedRecognizer != null) {
                speedRecognizer.startVoiceRecognition();
            }
        });
    }

    /**
     * Reproduce el video tutorial
     */
    private void playVideo() {
        try {
            if (!isVideoPlaying) {
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tuto);
                videoView.setVideoURI(videoUri);
                videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(false);
                    ttsManager.speak("Reproduciendo video tutorial", () -> {
                        videoView.start();
                        isVideoPlaying = true;
                    });
                });

                // Agregar un listener para cuando el video termina
                videoView.setOnCompletionListener(mp -> {
                    isVideoPlaying = false;
                    ttsManager.speak("El video ha terminado. ¿En qué más puedo ayudarte?", () -> {
                        if (speedRecognizer != null) {
                            speedRecognizer.startVoiceRecognition();
                        }
                    });
                });
            } else if (!videoView.isPlaying()) {
                // Si el video está pausado, reanudarlo
                videoView.start();
                ttsManager.speak("Reanudando video.", null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al reproducir el video: " + e.getMessage());
            ttsManager.speak("Lo siento, hubo un problema al reproducir el video.", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.startVoiceRecognition();
                }
            });
        }
    }

    /**
     * Pausa la reproducción del video
     */
    private void pauseVideo() {
        if (isVideoPlaying && videoView.isPlaying()) {
            videoView.pause();
            ttsManager.speak("Video pausado. Di 'reproducir' para continuar o 'detener' para parar.", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.startVoiceRecognition();
                }
            });
        } else {
            ttsManager.speak("El video no está reproduciéndose actualmente.", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.startVoiceRecognition();
                }
            });
        }
    }

    /**
     * Detiene la reproducción del video
     */
    private void stopVideo() {
        if (isVideoPlaying) {
            isVideoPlaying = false;
            videoView.stopPlayback();
            ttsManager.speak("Video detenido. ¿En qué más puedo ayudarte?", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.startVoiceRecognition();
                }
            });
        } else {
            ttsManager.speak("El video no está reproduciéndose actualmente.", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.startVoiceRecognition();
                }
            });
        }
    }

    /**
     * Reinicia el video desde el principio
     */
    private void restartVideo() {
        try {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tuto);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(false);
                ttsManager.speak("Reiniciando video tutorial.", () -> {
                    videoView.start();
                    isVideoPlaying = true;
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al reiniciar el video: " + e.getMessage());
            ttsManager.speak("Lo siento, hubo un problema al reiniciar el video.", () -> {
                if (speedRecognizer != null) {
                    speedRecognizer.startVoiceRecognition();
                }
            });
        }
    }

    /**
     * Navega de vuelta a la pantalla anterior
     */
    private void navigateBack() {
        Intent intent = new Intent(Ayuda.this, MapaUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Navega a la pantalla del mapa
     */
    private void navigateToMap() {
        Intent intent = new Intent(Ayuda.this, MapaUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Navega a la pantalla de login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(Ayuda.this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Navega a la pantalla de registro
     */
    private void navigateToRegister() {
        Intent intent = new Intent(Ayuda.this, RegistrerUser.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar la reproducción del video si estaba reproduciéndose
        if (isVideoPlaying && videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        // Pausar el video cuando la actividad pasa a segundo plano
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Liberar recursos del video
        if (videoView != null) {
            videoView.stopPlayback();
        }

        // Liberar recursos del Handler
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        super.onDestroy();
    }
}