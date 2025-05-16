package com.example.segiii;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


    public abstract class VoiceNavigationActivity extends AppCompatActivity {
        protected wordSegui wordSegui;
        protected SpeedRecognizer speedRecognizer;
        protected static final String PICOVOICE_ACCESS_KEY = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initializeVoiceComponents();
        }

        protected void initializeVoiceComponents() {
            speedRecognizer = new SpeedRecognizer(this, new SpeedRecognizer.OnVoiceCommandListener() {
                @Override
                public void onCommandProcessed(String command, String result) {
                    handleVoiceCommand(command, result);
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(VoiceNavigationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

            wordSegui = new wordSegui(this);
            wordSegui.initializeAndStartListening(PICOVOICE_ACCESS_KEY, () -> {
                Toast.makeText(this, "Hotword 'Okey Segui' detectado", Toast.LENGTH_SHORT).show();
                speedRecognizer.startVoiceRecognition();
            });
        }

        protected abstract void handleVoiceCommand(String command, String result);

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            speedRecognizer.processVoiceResult(requestCode, resultCode, data);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (wordSegui != null) {
                wordSegui.cleanup();
            }
        }
    }
