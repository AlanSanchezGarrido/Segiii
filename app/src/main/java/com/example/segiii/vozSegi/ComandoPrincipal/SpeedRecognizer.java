package com.example.segiii.vozSegi.ComandoPrincipal;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.segiii.UI.Ayuda;
import com.example.segiii.UI.RegistrerUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpeedRecognizer {

    private static final int SPEECH_REQUEST_CODE = 0;
    private static final String TAG = "SpeedRecognizer";
    private static final int LISTENING_TIMEOUT = 10000; // 10 seconds timeout

    private final Context context;
    private final OnVoiceCommandListener listener;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Map<String, CommandAction> commandMap;
    private TTSManager ttsManager;
    private boolean isFirstError = true;

    // Interfaz para notificar comandos procesados
    public interface OnVoiceCommandListener {
        void onCommandProcessed(String command, String result);
        void onError(String errorMessage);
    }

    // Interfaz funcional para acciones de comando
    public interface CommandAction {
        void execute(Context context);
    }

    public SpeedRecognizer(Context context, OnVoiceCommandListener listener) {
        this.context = context;
        this.listener = listener;
        this.timeoutHandler = new Handler();
        this.ttsManager = new TTSManager(context, null);
        initializeCommands();
    }

    // Inicializar todos los comandos disponibles en la aplicación
    private void initializeCommands() {
        commandMap = new HashMap<>();

       // Comando para registrar usuarios
       commandMap.put("registrame", (context) -> {
            Intent intent = new Intent(context, RegistrerUser.class);
           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
        });
//
//        // Comando para ir al login
//        commandMap.put("login", (context) -> {
//            Intent intent = new Intent(context, login.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            context.startActivity(intent);
//            if (context instanceof AppCompatActivity) {
//                ((AppCompatActivity) context).finish();
//            }
//        });
//
//        // Comando para ir al mapa
//        commandMap.put("mapa", (context) -> {
//            Intent intent = new Intent(context, MapaUI.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            context.startActivity(intent);
//            if (context instanceof AppCompatActivity) {
//                ((AppCompatActivity) context).finish();
//            }
//        });

        // Comando para ir a la ayuda/tutorial
        commandMap.put("tutorial", (context) -> {
            Intent intent = new Intent(context, Ayuda.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
        });

        // También puedes agregar alias para los comandos
        commandMap.put("ayuda", commandMap.get("tutorial"));
    }

    // Método para agregar comandos personalizados para casos específicos
    public void addCustomCommand(String keyword, CommandAction action) {
        commandMap.put(keyword.toLowerCase(), action);
    }

    // Iniciar el reconocimiento de voz
    public void startVoiceRecognition() {
        cancelTimeout();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("es", "MX"));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un comando...");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);

        try {
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).startActivityForResult(intent, SPEECH_REQUEST_CODE);
                startTimeout();
            } else {
                listener.onError("El contexto debe ser una AppCompatActivity");
            }
        } catch (Exception e) {
            listener.onError("Reconocimiento de voz no disponible: " + e.getMessage());
        }
    }

    // Configura un temporizador para reiniciar el reconocimiento si no hay respuesta
    private void startTimeout() {
        timeoutRunnable = () -> {
            if (isFirstError) {
                Log.d(TAG, "Tiempo de espera agotado, reiniciando reconocimiento");
                isFirstError = false;
                ttsManager.speak("No he escuchado nada. Por favor, intenta hablar ahora.", () -> {
                    startVoiceRecognition();
                });
            } else {
                isFirstError = true;
                listener.onError("No se detectó ningún comando en el tiempo establecido");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, LISTENING_TIMEOUT);
    }

    // Cancela el temporizador
    private void cancelTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    // Procesar el resultado del reconocimiento
    public void processVoiceResult(int requestCode, int resultCode, @Nullable Intent data) {
        cancelTimeout();

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0).toLowerCase();
                Log.d(TAG, "Texto Reconocido: " + spokenText);
                executeCommand(spokenText);
            } else {
                handleNoCommand("No se reconoció ningún comando");
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // Si el usuario canceló o no habló, reiniciamos el reconocimiento
            handleNoCommand("No escuché ningún comando");
        } else {
            handleNoCommand("Error en el reconocimiento de voz");
        }
    }

    // Manejar cuando no se recibe un comando válido
    private void handleNoCommand(String errorMessage) {
        ttsManager.speak("Disculpe, no reconozco ese comando. Por favor intente de nuevo diciendo un comando válido como 'mapa', 'login', 'registrar' o 'ayuda'.", () -> {
            startVoiceRecognition();
        });
        listener.onError(errorMessage);
    }

    // Procesar comandos y ejecutar acciones automatizadas
    private void executeCommand(String commandText) {
        String resultado = "comando no reconocido";
        boolean commandFound = false;

        // Buscar si el texto contiene alguna palabra clave de comando
        for (Map.Entry<String, CommandAction> entry : commandMap.entrySet()) {
            if (commandText.contains(entry.getKey())) {
                resultado = "ejecutando: " + entry.getKey();
                CommandAction action = entry.getValue();
                if (action != null) {
                    action.execute(context);
                    commandFound = true;
                    break;
                }
            }
        }

        if (!commandFound) {
            // Si el comando no se encontró en el mapa, notificar al usuario
            handleNoCommand("Comando no reconocido: " + commandText);
        } else {
            // Notificar que se procesó el comando
            listener.onCommandProcessed(commandText, resultado);
        }
    }

    // Verificar si un comando está registrado
    public boolean isValidCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return false;
        }

        commandText = commandText.toLowerCase();
        for (String key : commandMap.keySet()) {
            if (commandText.contains(key)) {
                return true;
            }
        }
        return false;
    }

    // Para casos especiales como sí/no
    public void handleYesNoCommand(String command, Runnable onYes, Runnable onNo) {
        if (command == null || command.trim().isEmpty()) {
            handleNoCommand("No escuché ninguna respuesta");
            return;
        }

        String lowerCommand = command.toLowerCase();
        if (lowerCommand.contains("sí") || lowerCommand.contains("si")) {
            if (onYes != null) {
                onYes.run();
            }
        } else if (lowerCommand.contains("no")) {
            if (onNo != null) {
                onNo.run();
            }
        } else {
            ttsManager.speak("No entendí tu respuesta. Por favor di sí o no.", () -> {
                startVoiceRecognition();
            });
        }
    }

    public static int getSpeechRequestCode() {
        return SPEECH_REQUEST_CODE;
    }
}