package com.example.segiii.vozSegi.ComandoPrincipal;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.segiii.UI.Ayuda;
import com.example.segiii.UI.MapaUI;
import com.example.segiii.UI.RegistrerUser;
import com.example.segiii.UI.login;

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
    private boolean isRecognizing = false;
    private boolean isDataEntryMode = false; // Nuevo flag para indicar modo de entrada de datos

    // Interfaz para notificar comandos procesados
    public interface OnVoiceCommandListener {
        void onCommandProcessed(String command, String result);
        void onError(String errorMessage);
        void onRecognitionCancelled();
        // Método actualizado para pasar el destino extraído
        void onNavigationCommand(String destination);
    }

    // Interfaz funcional para acciones de comando
    public interface CommandAction {
        void execute(Context context);
    }

    public SpeedRecognizer(Context context, OnVoiceCommandListener listener) {
        this.context = context;
        this.listener = listener;
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        this.ttsManager = new TTSManager(context, null);
        initializeCommands();
    }

    /**
     * Constructor sobrecargado que permite establecer el modo de entrada de datos
     */
    public SpeedRecognizer(Context context, OnVoiceCommandListener listener, boolean isDataEntryMode) {
        this(context, listener);
        this.isDataEntryMode = isDataEntryMode;
    }

    // Inicializar todos los comandos disponibles en la aplicación
    private void initializeCommands() {
        commandMap = new HashMap<>();

        // Comando para cancelar el reconocimiento de voz
        commandMap.put("cancelar", (context) -> {
            cancelRecognition();
        });

        // Alias para cancelar
        commandMap.put("silenciar", commandMap.get("cancelar"));
        commandMap.put("detener", commandMap.get("cancelar"));
        commandMap.put("parar", commandMap.get("cancelar"));
        commandMap.put("apagar", commandMap.get("cancelar"));
        commandMap.put("quieto", commandMap.get("cancelar"));
        commandMap.put("stop", commandMap.get("cancelar"));

        // Comando para registrar usuarios
        commandMap.put("registrar", (context) -> {
            Intent intent = new Intent(context, RegistrerUser.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
        });

        // Alias para registrar
        commandMap.put("registrame", commandMap.get("registrar"));
        commandMap.put("registro", commandMap.get("registrar"));
        commandMap.put("registrarme", commandMap.get("registrar"));

        // Comando para ir al login
        commandMap.put("login", (context) -> {
            Intent intent = new Intent(context, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
        });

        // Alias para login
        commandMap.put("iniciar sesión", commandMap.get("login"));
        commandMap.put("iniciar sesion", commandMap.get("login"));
        commandMap.put("ingresar", commandMap.get("login"));

        // Comando para ir al mapa
        commandMap.put("mapa", (context) -> {
            Intent intent = new Intent(context, MapaUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
        });

        // Alias para mapa
        commandMap.put("ver mapa", commandMap.get("mapa"));
        commandMap.put("ir al mapa", commandMap.get("mapa"));
        commandMap.put("mostrar mapa", commandMap.get("mapa"));

        // Comando para ir a la ayuda/tutorial
        commandMap.put("ayuda", (context) -> {
            Intent intent = new Intent(context, Ayuda.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
        });

        // Alias para ayuda
        commandMap.put("tutorial", commandMap.get("ayuda"));
        commandMap.put("instrucciones", commandMap.get("ayuda"));

        // Comando para salir
        commandMap.put("salir", (context) -> {
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finishAffinity();
            }
        });
    }

    // Método para agregar comandos personalizados para casos específicos
    public void addCustomCommand(String keyword, CommandAction action) {
        commandMap.put(keyword.toLowerCase(), action);
    }

    // Cambiar el modo de entrada de datos
    public void setDataEntryMode(boolean dataEntryMode) {
        this.isDataEntryMode = dataEntryMode;
    }

    // Iniciar el reconocimiento de voz
    public void startVoiceRecognition() {
        if (isRecognizing) {
            Log.d(TAG, "Ya hay un reconocimiento de voz en curso, ignorando solicitud");
            return;
        }

        isRecognizing = true;
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
                isRecognizing = false;
                listener.onError("El contexto debe ser una AppCompatActivity");
            }
        } catch (Exception e) {
            isRecognizing = false;
            listener.onError("Reconocimiento de voz no disponible: " + e.getMessage());
        }
    }

    // Nuevo método para cancelar explícitamente el reconocimiento de voz
    public void cancelRecognition() {
        if (isRecognizing) {
            Log.d(TAG, "Cancelando reconocimiento de voz manualmente");
            cancelTimeout();

            // Detener la actividad de reconocimiento
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.finishActivity(SPEECH_REQUEST_CODE);
            }

            isRecognizing = false;
            ttsManager.speak("Reconocimiento de voz desactivado. Di 'Okey Segui' para volver a activar.", null);

            // Notificar que el reconocimiento fue cancelado
            listener.onRecognitionCancelled();
        }
    }

    // Configura un temporizador para reiniciar el reconocimiento si no hay respuesta
    private void startTimeout() {
        timeoutRunnable = () -> {
            if (isFirstError) {
                Log.d(TAG, "Tiempo de espera agotado, reiniciando reconocimiento");
                isFirstError = false;
                ttsManager.speak("No he escuchado nada. Por favor, intenta hablar ahora.", () -> {
                    isRecognizing = false;
                    startVoiceRecognition();
                });
            } else {
                isFirstError = true;
                isRecognizing = false;
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

                // Si estamos en modo de entrada de datos, pasamos el texto directamente
                // sin verificar si es un comando
                if (isDataEntryMode) {
                    listener.onCommandProcessed(spokenText, "datos reconocidos");
                } else {
                    executeCommand(spokenText);
                }
            } else {
                handleNoCommand("No se reconoció ningún comando");
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            // Si el usuario canceló o no habló, reiniciamos el reconocimiento
            handleNoCommand("No escuché ningún comando");
        } else {
            handleNoCommand("Error en el reconocimiento de voz");
        }

        // Restablecer el estado de reconocimiento
        isRecognizing = false;
    }

    // Manejar cuando no se recibe un comando válido
    private void handleNoCommand(String errorMessage) {
        // Si estamos en modo de entrada de datos, no mostramos el mensaje de comando no reconocido
        if (isDataEntryMode) {
            listener.onError(errorMessage);
        } else {
            ttsManager.speak("Disculpe, no reconozco ese comando. Por favor intente de nuevo diciendo un comando válido como 'mapa', 'login', 'registrar', 'ayuda', 'navega a [destino]' o 'cancelar'.", () -> {
                isRecognizing = false;
                startVoiceRecognition();
            });
            listener.onError(errorMessage);
        }
    }

    // Método para extraer el destino de comandos de navegación
    private String extractDestination(String commandText) {
        if (commandText == null) return null;
        String lowerText = commandText.toLowerCase().trim();

        // Patrones de comandos de navegación
        String[] navigationPatterns = {
                "navega a",
                "navegue a",
                "llévame a",
                "llevame a",
                "dirígeme a",
                "dirigeme a",
                "ir a",
                "ve a",
                "vamos a",
                "quiero ir a",
                "necesito ir a",
                "buscar",
                "busca",
                "encuentra",
                "localizar",
                "localiza"
        };

        for (String pattern : navigationPatterns) {
            if (lowerText.startsWith(pattern)) {
                // Extrae el texto después del patrón
                String destination = lowerText.substring(pattern.length()).trim();
                // Si el destino empieza con "a " (por error en el patrón) lo quitamos
                if (destination.startsWith("a ")) {
                    destination = destination.substring(2).trim();
                }
                // Solo destino, nunca vacío ni el comando
                if (!destination.isEmpty()) {
                    return destination;
                }
            }
        }
        return null;
    }

    // Procesar comandos y ejecutar acciones automatizadas
    private void executeCommand(String commandText) {
        String resultado = "comando no reconocido";
        boolean commandFound = false;

        // Convertir el texto a minúsculas para hacer la comparación insensible a mayúsculas
        String lowerCommandText = commandText.toLowerCase();

        // Primero verificar si es un comando de navegación
        String destination = extractDestination(commandText);
        if (destination != null) {
            resultado = "navegando a: " + destination;
            listener.onCommandProcessed(commandText, resultado);
            // Pasar el destino extraído al listener
            listener.onNavigationCommand(destination);
            commandFound = true;
        }

        // Si no es comando de navegación, buscar coincidencias exactas
        if (!commandFound && commandMap.containsKey(lowerCommandText)) {
            CommandAction action = commandMap.get(lowerCommandText);
            if (action != null) {
                resultado = "ejecutando: " + lowerCommandText;
                // Notificar que se procesó el comando antes de ejecutar la acción
                listener.onCommandProcessed(commandText, resultado);
                // Ejecutar la acción con un pequeño retraso para evitar problemas de navegación
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    action.execute(context);
                }, 100);
                commandFound = true;
            }
        }

        // Si no se encontró coincidencia exacta, buscar si el texto contiene alguna palabra clave
        if (!commandFound) {
            for (Map.Entry<String, CommandAction> entry : commandMap.entrySet()) {
                String key = entry.getKey();
                // Usar palabras completas para evitar falsos positivos
                if (lowerCommandText.contains(" " + key + " ") ||
                        lowerCommandText.startsWith(key + " ") ||
                        lowerCommandText.endsWith(" " + key) ||
                        lowerCommandText.equals(key)) {

                    CommandAction action = entry.getValue();
                    if (action != null) {
                        resultado = "ejecutando: " + key;
                        // Notificar que se procesó el comando antes de ejecutar la acción
                        listener.onCommandProcessed(commandText, resultado);
                        // Ejecutar la acción con un pequeño retraso para evitar problemas de navegación
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            action.execute(context);
                        }, 100);
                        commandFound = true;
                        break;
                    }
                }
            }
        }

        if (!commandFound) {
            // Si el comando no se encontró en el mapa, notificar al usuario
            if (isDataEntryMode) {
                // En modo de entrada de datos, simplemente pasamos el texto como está
                listener.onCommandProcessed(commandText, "datos reconocidos");
            } else {
                handleNoCommand("Comando no reconocido: " + commandText);
            }
        }
    }

    // Verificar si un comando está registrado
    public boolean isValidCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return false;
        }

        String lowerCommandText = commandText.toLowerCase();

        // Verificar si es un comando de navegación
        if (extractDestination(commandText) != null) {
            return true;
        }

        // Verificar coincidencias exactas
        if (commandMap.containsKey(lowerCommandText)) {
            return true;
        }

        // Verificar coincidencias parciales
        for (String key : commandMap.keySet()) {
            if (lowerCommandText.contains(" " + key + " ") ||
                    lowerCommandText.startsWith(key + " ") ||
                    lowerCommandText.endsWith(" " + key) ||
                    lowerCommandText.equals(key)) {
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
                isRecognizing = false;
                startVoiceRecognition();
            });
        }
    }

    public static int getSpeechRequestCode() {
        return SPEECH_REQUEST_CODE;
    }

    public boolean isRecognizing() {
        return isRecognizing;
    }
}