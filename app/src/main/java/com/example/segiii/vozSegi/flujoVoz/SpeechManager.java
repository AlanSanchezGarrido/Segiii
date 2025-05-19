package com.example.segiii.vozSegi.flujoVoz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeechManager {
    private TextToSpeech textToSpeech;
    private boolean isListening = false;
    private final AppCompatActivity activity;
    private final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private boolean isTextToSpeechInitialized = false;
    private static final int RESULT_OK = -1;

    private OnSpeechResultListener speechResultListener;
    private static final String TAG = "SpeechManager";
    private SpeechRecognizer speechRecognizer;
    private OnSpeechManagerReadyListener speechManagerReadyListener;


    public interface OnSpeechManagerReadyListener {
        void onTextToSpeechReady();
        void onTextToSpeechError(); // Opcional: para manejar errores de inicialización del TTS
    }
    public interface OnSpeechResultListener{

        void onSpeechResult (String spokenText);

    }
    public SpeechManager(AppCompatActivity activity, OnSpeechManagerReadyListener readyListener){
        this.activity = activity;
        this.speechManagerReadyListener = readyListener;
        inicializeTextToSpeach();
        initializeSpeechRecognizer();
    }

    public void setOnSpeechResultListener(OnSpeechResultListener listener) {
        this.speechResultListener = listener;
    }
    private void inicializeTextToSpeach (){
        Log.d(TAG, "Initializing TextToSpeech...");
        textToSpeech = new TextToSpeech(activity, status -> {
            Log.d(TAG, "TextToSpeech initialization status: " + status);
            if (status == TextToSpeech.SUCCESS){
                textToSpeech.setLanguage(new Locale("es","ES"));
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "TextToSpeech started: " + utteranceId);

                    }
                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "TextToSpeech completed: " + utteranceId);

                        activity.runOnUiThread(()->{
                            if(utteranceId.equals("welcome_message")|| utteranceId.equals("dialogflow_response") || utteranceId.equals("ask_name")){
                                if (ContextCompat.checkSelfPermission(activity,Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED){
                                    Log.d(TAG, "Starting voice recognition after utterance: " + utteranceId);

                                    startVoiceRecognition();


                                }else {
                                    Log.e(TAG, "Audio permission not granted");
                                    Toast.makeText(activity, "Permiso de grabación de audio denegado", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.d(TAG, "TextToSpeech completed: " + utteranceId);
                        activity.runOnUiThread(()->{
                            Toast.makeText(activity, "Error al producir el mensaje",Toast.LENGTH_LONG).show();
                            if (ContextCompat.checkSelfPermission(activity,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
                                startVoiceRecognition();

                            }
                        });

                    }
                });
                isTextToSpeechInitialized = true;
                if (speechManagerReadyListener != null) {
                    speechManagerReadyListener.onTextToSpeechReady(); // Notificar que el TTS está listo
                }

            }else{
                Log.e(TAG, "TextToSpeech initialization failed: " + status);
                Toast.makeText(activity, "Error al inicializar Text-to-Speech", Toast.LENGTH_SHORT).show();
                isTextToSpeechInitialized = false;
            }
        });
    }
    private void initializeSpeechRecognizer() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permiso
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                isListening = false;
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    if (speechResultListener != null) {
                        speechResultListener.onSpeechResult(spokenText);
                    }
                }
                isListening = false;
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }



    public void startVoiceRecognition (){
        if (isListening || speechRecognizer == null) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            speechRecognizer.startListening(intent);
            isListening = true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition", e);
            isListening = false;
            // Fallback al método con UI si falla
           startVoiceRecognitionWithUI();
        }

        //speechRecognizer.startListening(intent);
        //isListening = true;

        /*
        if (isListening)return;
        isListening = true;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un comando");
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            activity.startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);

        }catch (Exception e){
            Toast.makeText(activity, "Error al iniciar reconocimiento de voz", Toast.LENGTH_SHORT).show();
            speak("Error al iniciar el micrófono. Intenta de nuevo.", "error_message");
            isListening = false;
        }
         */
    }
    private void startVoiceRecognitionWithUI() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un comando");

        try {
            activity.startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(activity, "Error al iniciar reconocimiento de voz",
                    Toast.LENGTH_SHORT).show();
            speak("Error al iniciar el micrófono. Intenta de nuevo.", "error_message");
            isListening = false;
        }
    }
    public void onActivityResult (int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null){
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()){
                String spokenText = results.get(0).toLowerCase().trim();
                Log.d(TAG, "Texto reconocido: " + spokenText);

                if (speechResultListener != null){
                    speechResultListener.onSpeechResult(spokenText);
                }
            }
            isListening= false;

        }else{
            isListening= false;
            startVoiceRecognition();
        }
    }
    public void speak(String text, String utterancedId){
        if (textToSpeech != null && isTextToSpeechInitialized){
            Log.d(TAG, "Speaking: " + text + " with utteranceId: " + utterancedId);
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null, utterancedId);
        }else{
            Log.e(TAG, "TextToSpeech is not initialized, cannot speak: " + text);
            Toast.makeText(activity, "Text-to-Speech no está listo", Toast.LENGTH_SHORT).show();

        }
    }
    public void stopSpeaking() {
        if (textToSpeech != null && isTextToSpeechInitialized) {
            Log.d(TAG, "Stopping TextToSpeech");
            textToSpeech.stop();
        } else {
            Log.e(TAG, "TextToSpeech is not initialized, cannot stop");
            Toast.makeText(activity, "Text-to-Speech no está listo", Toast.LENGTH_SHORT).show();
        }
    }
    public TextToSpeech getTextToSpeech(){

        return textToSpeech;
    }
    public void shutdown(){
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
