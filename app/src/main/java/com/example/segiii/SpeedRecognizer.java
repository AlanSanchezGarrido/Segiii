package com.example.segiii;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.segiii.UI.RegistrerUser;
import com.example.segiii.UI.login;

import java.util.ArrayList;
import java.util.Locale;

public class SpeedRecognizer {

    private static final int SPEECH_REQUEST_CODE = 0;
    private final Context context;
    private final OnVoiceCommandListener listener;

    // Interfaz para notificar comandos procesados
    public interface OnVoiceCommandListener {
        void onCommandProcessed(String command, String result);
        void onError(String errorMessage);
    }

    public SpeedRecognizer(Context context, OnVoiceCommandListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Iniciar el reconocimiento de voz
    public void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un comando...");

        try {
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).startActivityForResult(intent, SPEECH_REQUEST_CODE);
            } else {
                listener.onError("El contexto debe ser una AppCompatActivity");
            }
        } catch (Exception e) {
            listener.onError("Reconocimiento de voz no disponible: " + e.getMessage());
        }
    }

    // Procesar el resultado del reconocimiento
    public void processVoiceResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0).toLowerCase();
                Log.d("SpeedRecognizer","Texto Reconocido" + spokenText);
                executeCommand(spokenText);
            } else {
                listener.onError("No se reconoció ningún comando");
            }
        } else {
            listener.onError("Error en el reconocimiento de voz");
        }
    }

    // Procesar comandos y ejecutar acciones automatizadas
    private void executeCommand(String command) {
        String resultado;
        try {
            Intent intent= null;
            if (command.contains("registrar")) {
                resultado = "inicianoo el registro";
                intent = new Intent(context, RegistrerUser.class);
            } else if (command.contains("login")) {
                resultado = "redirigiendo al login";
                intent = new Intent(context, login.class);
            } else if (command.contains("mapa")) {
                resultado = "redirigiendo al mapa";
                intent = new Intent(context, MapaUI.class);
            } else {
                resultado = "Comando no reconocido";
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
                if(context instanceof AppCompatActivity){
                    ((AppCompatActivity) context).finish();
                }
            }
            listener.onCommandProcessed(command, resultado);
        }catch (Exception e) {
            listener.onError("Error al ejecutar el comando: " + e.getMessage());
        }
    }

    // Obtener el código de solicitud para usar en la actividad
    public static int getSpeechRequestCode() {
        return SPEECH_REQUEST_CODE;
    }
}