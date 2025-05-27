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

    private Context context;
    private final OnVoiceCommandListener listener;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Map<String, CommandAction> commandMap;
    private TTSManager ttsManager;
    private boolean isFirstError = true;
    private boolean isRecognizing = false;
    private boolean isDataEntryMode = false;

    // Patrones de comandos de navegación
    private static final String[] NAVIGATION_PATTERNS = {
            "navega a ",
            "navegue a ",
            "llévame a ",
            "llevame a ",
            "dirígeme a ",
            "dirigeme a ",
            "ir a ",
            "ve a ",
            "vamos a ",
            "quiero ir a ",
            "necesito ir a ",
            "buscar ",
            "busca ",
            "encuentra ",
            "localizar ",
            "localiza "
    };

    // Patrones para guardar ubicación
    private static final String[] SAVE_LOCATION_PATTERNS = {
            "guardar ubicación",
            "guardar ubicacion",
            "guardar localización",
            "guardar localizacion",
            "guardar posición",
            "guardar posicion",
            "guardar lugar",
            "guardar sitio",
            "salvar ubicación",
            "salvar ubicacion",
            "salvar localización",
            "salvar localizacion",
            "salvar posición",
            "salvar posicion",
            "salvar lugar",
            "salvar sitio",
            "grabar ubicación",
            "grabar ubicacion",
            "grabar localización",
            "grabar localizacion",
            "grabar posición",
            "grabar posicion",
            "grabar lugar",
            "grabar sitio"
    };

    // Patrones para eliminar ubicación
    private static final String[] DELETE_LOCATION_PATTERNS = {
            "eliminar ubicación",
            "eliminar ubicacion",
            "eliminar localización",
            "eliminar localizacion",
            "eliminar posición",
            "eliminar posicion",
            "eliminar lugar",
            "eliminar sitio",
            "borrar ubicación",
            "borrar ubicacion",
            "borrar localización",
            "borrar localizacion",
            "borrar posición",
            "borrar posicion",
            "borrar lugar",
            "borrar sitio",
            "quitar ubicación",
            "quitar ubicacion",
            "quitar localización",
            "quitar localizacion",
            "quitar posición",
            "quitar posicion",
            "quitar lugar",
            "quitar sitio",
            "remover ubicación",
            "remover ubicacion",
            "remover localización",
            "remover localizacion",
            "remover posición",
            "remover posicion",
            "remover lugar",
            "remover sitio"
    };

    // Interfaz para notificar comandos procesados
    public interface OnVoiceCommandListener {
        void onCommandProcessed(String command, String result);
        void onError(String errorMessage);
        void onRecognitionCancelled();
        void onNavigationCommand(String destination);
        void onSaveLocationCommand(); // Nuevo método para guardar ubicación
        void onDeleteLocationCommand(String locationName); // Nuevo método para eliminar ubicación
    }

    // Interfaz funcional para acciones de comando
    public interface CommandAction {
        void execute(Context context);
    }

    public SpeedRecognizer(Context context, OnVoiceCommandListener listener) {this.context = context;
        this.context = context;
        this.listener = listener;
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        this.ttsManager = new TTSManager(context, null);
        Log.d(TAG, "SpeedRecognizer initialized with context: " + context.getClass().getSimpleName());

        initializeCommands();
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
    public boolean isDataEntryMode() {
        return isDataEntryMode;
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("es", "ES"));
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
                if (isDataEntryMode) {
                    listener.onCommandProcessed(spokenText, "datos reconocidos");
                } else {
                    executeCommand(spokenText);
                }
            } else {
                handleNoCommand("No se reconoció ningún comando");
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            handleNoCommand("No escuché ningún comando");
        } else {
            handleNoCommand("Error en el reconocimiento de voz");
        }

        // Restablecer el estado de reconocimiento
        isRecognizing = false;
    }

    // Manejar cuando no se recibe un comando válido
    private void handleNoCommand(String errorMessage) {
        if (isDataEntryMode) {
            listener.onError(errorMessage);
        } else {
            ttsManager.speak("Disculpe, no reconozco ese comando. Por favor intente de nuevo diciendo un comando válido como 'mapa', 'login', 'registrar', 'ayuda', 'navega a [destino]', 'guardar ubicación', 'eliminar ubicación [nombre]' o 'cancelar'.", () -> {
                isRecognizing = false;
                startVoiceRecognition();
            });
            listener.onError(errorMessage);
        }
    }

    // Método principal para procesar comandos
    private void executeCommand(String commandText) {
        Log.d(TAG, "=== EJECUTANDO COMANDO ===");
        Log.d(TAG, "Comando recibido: '" + commandText + "'");

        String resultado = "comando no reconocido";
        boolean commandFound = false;
        String lowerCommandText = commandText.toLowerCase().trim();

        // Verificar si es un comando para guardar ubicación
        if (checkSaveLocationCommand(lowerCommandText)) {
            Log.d(TAG, "Comando de guardar ubicación detectado");
            resultado = "guardando ubicación actual";
            listener.onCommandProcessed(commandText, resultado);
            listener.onSaveLocationCommand();
            return;
        }

        // CORRECCIÓN: Verificar si es un comando para eliminar ubicación
        String locationToDelete = checkDeleteLocationCommand(lowerCommandText);
        if (locationToDelete != null) { // null = no es comando de eliminar
            Log.d(TAG, "Comando de eliminar ubicación detectado");

            if (locationToDelete.isEmpty()) {
                // Cadena vacía = comando sin nombre específico
                Log.d(TAG, "Comando de eliminar sin nombre - pidiendo nombre");
                resultado = "pidiendo nombre de ubicación a eliminar";
                listener.onCommandProcessed(commandText, resultado);
                listener.onDeleteLocationCommand(""); // Pasar cadena vacía
            } else {
                // Tiene nombre específico
                Log.d(TAG, "Comando de eliminar con nombre: '" + locationToDelete + "'");
                resultado = "eliminando ubicación: " + locationToDelete;
                listener.onCommandProcessed(commandText, resultado);
                listener.onDeleteLocationCommand(locationToDelete);
            }
            return;
        }

        // Verificar si es un comando de navegación
        String destination = checkNavigationCommand(lowerCommandText);
        if (destination != null) {
            Log.d(TAG, "Comando de navegación detectado - Destino: '" + destination + "'");
            resultado = "navegando a: " + destination;
            listener.onCommandProcessed(commandText, resultado);
            listener.onNavigationCommand(destination);
            return;
        }

        // Buscar coincidencias exactas con otros comandos
        if (commandMap.containsKey(lowerCommandText)) {
            CommandAction action = commandMap.get(lowerCommandText);
            if (action != null) {
                resultado = "ejecutando: " + lowerCommandText;
                listener.onCommandProcessed(commandText, resultado);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    action.execute(context);
                }, 100);
                commandFound = true;
            }
        }

        // Buscar si el texto contiene alguna palabra clave
        if (!commandFound) {
            for (Map.Entry<String, CommandAction> entry : commandMap.entrySet()) {
                String key = entry.getKey();
                if (lowerCommandText.contains(" " + key + " ") ||
                        lowerCommandText.startsWith(key + " ") ||
                        lowerCommandText.endsWith(" " + key) ||
                        lowerCommandText.equals(key)) {

                    CommandAction action = entry.getValue();
                    if (action != null) {
                        resultado = "ejecutando: " + key;
                        listener.onCommandProcessed(commandText, resultado);
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
            Log.d(TAG, "Comando no encontrado: " + commandText);
            if (isDataEntryMode) {
                listener.onCommandProcessed(commandText, "datos reconocidos");
            } else {
                handleNoCommand("Comando no reconocido: " + commandText);
            }
        }
    }

    /**
     * Verifica si el comando es para guardar ubicación
     */
    private boolean checkSaveLocationCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return false;
        }

        Log.d(TAG, "Verificando comando de guardar ubicación: '" + commandText + "'");

        for (String pattern : SAVE_LOCATION_PATTERNS) {
            if (commandText.contains(pattern)) {
                Log.d(TAG, "Patrón de guardar ubicación '" + pattern + "' encontrado");
                return true;
            }
        }

        Log.d(TAG, "No es comando de guardar ubicación");
        return false;
    }

    /**
     * Verifica si el comando es para eliminar ubicación y extrae el nombre
          */
    private String checkDeleteLocationCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return null;
        }

        Log.d(TAG, "Verificando comando de eliminar ubicación: '" + commandText + "'");

        for (String pattern : DELETE_LOCATION_PATTERNS) {
            // CORRECCIÓN: Usar startsWith para patrones que deben estar al inicio
            if (commandText.startsWith(pattern)) {
                Log.d(TAG, "Patrón de eliminar ubicación '" + pattern + "' encontrado al inicio");

                String remainingText = commandText.substring(pattern.length()).trim();

                if (!remainingText.isEmpty()) {
                    Log.d(TAG, "Nombre de ubicación a eliminar: '" + remainingText + "'");
                    return remainingText;
                } else {
                    Log.d(TAG, "Comando de eliminar ubicación sin nombre específico");
                    return ""; // Cadena vacía indica que se debe pedir el nombre
                }
            }
            // También verificar si contiene el patrón (para mayor flexibilidad)
            else if (commandText.contains(pattern)) {
                Log.d(TAG, "Patrón de eliminar ubicación '" + pattern + "' encontrado en el texto");

                int patternIndex = commandText.indexOf(pattern);
                String remainingText = commandText.substring(patternIndex + pattern.length()).trim();

                if (!remainingText.isEmpty()) {
                    Log.d(TAG, "Nombre de ubicación a eliminar: '" + remainingText + "'");
                    return remainingText;
                } else {
                    Log.d(TAG, "Comando de eliminar ubicación sin nombre específico");
                    return "";
                }
            }
        }

        Log.d(TAG, "No es comando de eliminar ubicación");
        return null;
    }

    /**
     * Verifica si el comando es de navegación y extrae el destino
          */
    private String checkNavigationCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return null;
        }

        Log.d(TAG, "Verificando comando de navegación: '" + commandText + "'");

        for (String pattern : NAVIGATION_PATTERNS) {
            if (commandText.startsWith(pattern)) {
                String destination = commandText.substring(pattern.length()).trim();
                Log.d(TAG, "Patrón '" + pattern + "' encontrado -> Destino: '" + destination + "'");

                if (!destination.isEmpty()) {
                    return destination;
                }
            }
        }

        Log.d(TAG, "No es comando de navegación");
        return null;
    }

    public static int getSpeechRequestCode() {
        return SPEECH_REQUEST_CODE;
    }

    public boolean isRecognizing() {
        return isRecognizing;
    }
}