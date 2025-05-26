package com.example.segiii.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;

import java.util.ArrayList;
import java.util.Locale;

public class login extends VoiceNavigationActivity {
    private boolean isPasswordVisible = false;
    private static final String TAG = "LoginActivity";
    private static final int SPEECH_REQUEST_CODE = 100;

    private EditText etCorreo, etPassword;
    private ImageButton btnTogglePassword; // Added
    private SegiDataBase segiDataBase;
    private TextToSpeech textToSpeech;

    private enum ConversationState {
        ASKING_ACCOUNT,
        ASKING_EMAIL,
        ASKING_PASSWORD,
        CONFIRMING_LOGIN
    }

    private ConversationState currentState = ConversationState.ASKING_ACCOUNT;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar base de datos
        segiDataBase = SegiDataBase.getDatabase(this);

        // Inicializar campos
        etCorreo = findViewById(R.id.edit_email);
        etPassword = findViewById(R.id.edit_password);
        btnTogglePassword = findViewById(R.id.btnTogglePassword); // Initialize ImageButton

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                // Show password and set open eye icon
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.visibilidad);
            } else {
                // Hide password and set closed eye icon
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ojo);
            }
            // Move cursor to the end of the text
            etPassword.setSelection(etPassword.getText().length());
        });

        // Botón para regresar
        ImageButton imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, MapaUI.class);
            startActivity(intent);
        });

        // Texto para ir a registro
        TextView txtOptions = findViewById(R.id.txt_options);
        txtOptions.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, RegistrerUser.class);
            startActivity(intent);
        });

        // Texto para reescribir
        TextView txtRewrite = findViewById(R.id.txt_rewrite);
        txtRewrite.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, RegistrerUser.class);
            startActivity(intent);
        });

        // Botón para ingresar
        Button btnIngresar = findViewById(R.id.btn_ingresar);
        btnIngresar.setOnClickListener(v -> {
            // Aquí deberías implementar la lógica de autenticación real
            Intent intent = new Intent(login.this, MapaUI.class);
            startActivity(intent);
        });

        // Configuración de Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicializar TTS
        initTextToSpeech();
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        String lowerCommand = command.toLowerCase();

        if (lowerCommand.contains("mapa")) {
            navigateToMap();
        } else if (lowerCommand.contains("salir")) {
            finishAffinity();
        } else if (lowerCommand.contains("mostrar contraseña") || lowerCommand.contains("ver contraseña")) {
            // Optional: Handle voice command to show password
            if (!isPasswordVisible) {
                isPasswordVisible = true;
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.visibilidad);
                etPassword.setSelection(etPassword.getText().length());
                speak("Contraseña visible", "password_visible");
            }
        } else if (lowerCommand.contains("ocultar contraseña")) {
            // Optional: Handle voice command to hide password
            if (isPasswordVisible) {
                isPasswordVisible = false;
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ojo);
                etPassword.setSelection(etPassword.getText().length());
                speak("Contraseña oculta", "password_hidden");
            }
        }
    }

    private void navigateToMap() {
        Intent intent = new Intent(login.this, MapaUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("es", "MX"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "El idioma no está soportado");
                } else {
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            // No necesitamos hacer nada al inicio
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            runOnUiThread(() -> {
                                switch (utteranceId) {
                                    case "ask_account":
                                    case "ask_email":
                                    case "ask_password":
                                    case "confirm_login":
                                        if (ContextCompat.checkSelfPermission(login.this,
                                                Manifest.permission.RECORD_AUDIO) ==
                                                PackageManager.PERMISSION_GRANTED) {
                                            startVoiceRecognition();
                                        } else {
                                            Toast.makeText(login.this,
                                                    "Se requiere permiso para usar el micrófono",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                }
                            });
                        }

                        @Override
                        public void onError(String utteranceId) {
                            runOnUiThread(() -> {
                                if (ContextCompat.checkSelfPermission(login.this,
                                        Manifest.permission.RECORD_AUDIO) ==
                                        PackageManager.PERMISSION_GRANTED) {
                                    startVoiceRecognition();
                                }
                            });
                        }
                    });
                    speak("¿Tienes cuenta?", "ask_account");
                }
            } else {
                Log.e(TAG, "Error al inicializar TextToSpeech");
            }
        });
    }

    private void speak(String text, String utteranceId) {
        if (textToSpeech != null) {
            textToSpeech.stop();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            } else {
                @SuppressWarnings("deprecation")
                int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                Log.d(TAG, "Speak result: " + result);
            }
        }
    }

    private void startVoiceRecognition() {
        if (isListening) {
            Log.d(TAG, "Ya hay un reconocimiento de voz en curso");
            return;
        }

        isListening = true;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("es", "MX"));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un comando...");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
            Log.d(TAG, "Iniciando reconocimiento de voz para estado: " + currentState);
        } catch (Exception e) {
            isListening = false;
            Log.e(TAG, "Error al iniciar reconocimiento de voz: " + e.getMessage());
            Toast.makeText(this, "Error al iniciar reconocimiento de voz", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE) {
            isListening = false;

            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    String spokenText = results.get(0).toLowerCase();
                    Log.d(TAG, "Texto reconocido: " + spokenText);
                    processVoiceInput(spokenText);
                } else {
                    handleNoSpeechDetected();
                }
            } else if (resultCode == RESULT_CANCELED) {
                handleNoSpeechDetected();
            }
        }
    }


    private void processVoiceInput(String spokenText) {
        switch (currentState) {
            case ASKING_ACCOUNT:
                handleAccountResponse(spokenText);
                break;
            case ASKING_EMAIL:
                handleEmailResponse(spokenText);
                break;
            case ASKING_PASSWORD:
                handlePasswordResponse(spokenText);
                break;
            case CONFIRMING_LOGIN:
                handleLoginConfirmation(spokenText);
                break;
        }
    }

    private void handleAccountResponse(String response) {
        response = response.toLowerCase();

        if (response.contains("sí") || response.contains("si") || response.contains("claro") ||
                response.contains("por supuesto") || response.contains("tengo") ||
                response.contains("tengo cuenta")) {
            currentState = ConversationState.ASKING_EMAIL;
            speak("Por favor, dime tu correo electrónico", "ask_email");
        } else if (response.contains("no") || response.contains("no tengo") ||
                response.contains("no tengo cuenta") || response.contains("negativo")) {
            speak("Entendido, te enviaré a la pantalla de registro", "redirect_register");
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(login.this, RegistrerUser.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }, 2000);
        } else {
            speak("No entendí tu respuesta. Por favor, responde sí o no. ¿Tienes cuenta?", "ask_account");
        }
    }

    private void handleEmailResponse(String email) {
        if (email.contains("@") && email.contains(".")) {
            etCorreo.setText(email.replaceAll("\\s+", "").toLowerCase());
            currentState = ConversationState.ASKING_PASSWORD;
            speak("Gracias, ahora dime tu contraseña", "ask_password");
        } else {
            speak("El formato del correo no parece correcto. Por favor, dime tu correo electrónico nuevamente",
                    "ask_email");
        }
    }

    private void handlePasswordResponse(String password) {
        etPassword.setText(password.replaceAll("\\s+", ""));
        currentState = ConversationState.CONFIRMING_LOGIN;
        speak("¿Deseas iniciar sesión ahora?", "confirm_login");
    }

    private void handleLoginConfirmation(String response) {
        response = response.toLowerCase();

        if (response.contains("sí") || response.contains("si") || response.contains("claro") ||
                response.contains("por supuesto") || response.contains("adelante")) {
            speak("Iniciando sesión, por favor espera", "login_process");
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(login.this, MapaUI.class);
                startActivity(intent);
                finish();
            }, 2000);
        } else if (response.contains("no") || response.contains("cancelar") || response.contains("negativo")) {
            speak("He cancelado el inicio de sesión. Regresando a la pantalla principal", "login_cancel");
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(login.this, MapaUI.class);
                startActivity(intent);
                finish();
            }, 2000);
        } else {
            speak("No entendí tu respuesta. Por favor responde sí o no. ¿Deseas iniciar sesión ahora?",
                    "confirm_login");
        }
    }

    private void handleNoSpeechDetected() {
        Log.d(TAG, "No se detectó voz");

        switch (currentState) {
            case ASKING_ACCOUNT:
                speak("No escuché tu respuesta. ¿Tienes cuenta?", "ask_account");
                break;
            case ASKING_EMAIL:
                speak("No escuché tu correo. Por favor, dime tu correo electrónico", "ask_email");
                break;
            case ASKING_PASSWORD:
                speak("No escuché tu contraseña. Por favor, dime tu contraseña", "ask_password");
                break;
            case CONFIRMING_LOGIN:
                speak("No escuché tu confirmación. ¿Deseas iniciar sesión ahora?", "confirm_login");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}