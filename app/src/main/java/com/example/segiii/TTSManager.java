package com.example.segiii;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class TTSManager {

    public interface TTSCallback {
        void onSpeakComplete();
    }

    private TextToSpeech tts;
    private boolean isInitialized = false;

    public TTSManager(Context context, InitializationCallback callback) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSManager", "Idioma no soportado");
                    if (callback != null) callback.onInitComplete(false);
                } else {
                    isInitialized = true;
                    if (callback != null) callback.onInitComplete(true);
                }
            } else {
                Log.e("TTSManager", "Inicialización fallida");
                if (callback != null) callback.onInitComplete(false);
            }
        });
    }

    public void speak(String text, TTSCallback callback) {
        if (isInitialized) {
            String utteranceId = "utterance_" + System.currentTimeMillis();
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    if (callback != null) callback.onSpeakComplete();
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e("TTSManager", "Error en utterance: " + utteranceId);
                }
            });
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            Log.w("TTSManager", "TTS no inicializado");
        }
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
    public boolean isInitialized() {
        return isInitialized;
    }

    public interface InitializationCallback {
        void onInitComplete(boolean success);
    }
}