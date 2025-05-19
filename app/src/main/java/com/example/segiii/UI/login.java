package com.example.segiii.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.example.segiii.vozSegi.flujoVoz.DialogFlowManager;
import com.example.segiii.vozSegi.flujoVoz.SpeechManager;
import com.google.protobuf.Value;

import java.util.Locale;
import java.util.Map;

public class login extends VoiceNavigationActivity implements SpeechManager.OnSpeechResultListener,SpeechManager.OnSpeechManagerReadyListener {
    private SpeechManager speechManager;
    private DialogFlowManager dialogFlowManager;
    private TextToSpeech textToSpeech;
    private EditText etCorreo, etPassword;
    private SegiDataBase segiDataBase;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       EdgeToEdge.enable(this);
       setContentView(R.layout.activity_login);
       segiDataBase = SegiDataBase.getDatabase(this);
       etCorreo=findViewById(R.id.edit_email);
       etPassword=findViewById(R.id.edit_password);

       ImageButton imgBack = findViewById(R.id.img_back);
       imgBack.setOnClickListener(v -> {
           Intent intent = new Intent(login.this, MapaUI.class);
           startActivity(intent);
       });

       TextView txtOptions = findViewById(R.id.txt_options);
       txtOptions.setOnClickListener(v -> {
           Intent intent = new Intent(login.this, RegistrerUser.class);
           startActivity(intent);
       });


       TextView txtRewrite = findViewById(R.id.txt_rewrite);
       txtRewrite.setOnClickListener(v -> {
           Intent intent = new Intent(login.this, RegistrerUser.class);
           startActivity(intent);
       });


       Button btnIngresar = findViewById(R.id.btn_ingresar);
       btnIngresar.setOnClickListener(v -> {
           Intent intent = new Intent(login.this, MapaUI.class);
           startActivity(intent);
       });


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
       } else {
           //Log.e("RegistrerUser", "Root view is null, Edge-to-Edge configuration skipped");
       }

       speechManager = new SpeechManager(this,this);
       dialogFlowManager= new DialogFlowManager(this);
       speechManager.setOnSpeechResultListener(this);
       textToSpeech = new TextToSpeech(this, status -> {
           if (status==TextToSpeech.SUCCESS){
               textToSpeech.setLanguage(new Locale("es","ES"));
               textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                   @Override
                   public void onStart(String utteranceId) {

                   }

                   @Override
                   public void onDone(String utteranceId) {
                       runOnUiThread(()->{
                           if (utteranceId.equals("ask_name")){
                               if(ContextCompat.checkSelfPermission(login.this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED){
                                   speechManager.startVoiceRecognition();

                               }
                           }
                       });

                   }

                   @Override
                   public void onError(String utteranceId) {
                       runOnUiThread(()->{
                           if(ContextCompat.checkSelfPermission(login.this, Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
                               speechManager.startVoiceRecognition();

                           }
                       });

                   }
               });
               //speak("¿Cual es tu Nombre?", "ask_name");
           }

       });
       dialogFlowManager.setOnDialogflowResultListener(new DialogFlowManager.OnDialogflowResultListener() {
           @Override
           public void onRegisterIntentDetected() {

           }

           @Override
           public void onOtherIntentDetected(String fulfillmentText) {

           }

           @Override
           public void onParametersReceived(Map<String, Value> parameters, String intentName) {
               handleDialogFlowParams(parameters,intentName);

           }
       });
   }

    private void speak(String text,String utterancedId) {
        if (textToSpeech != null) {
            textToSpeech.stop();

            // Verificar versión de Android para usar el método adecuado
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utterancedId);
            } else {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        if (command.contains("mapa")) {
            Intent intent = new Intent(this, MapaUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();

        }
    }

    @Override
    public void onTextToSpeechReady() {
        if (speechManager != null) {
            speechManager.speak("¿Tienes cuenta?", "ask_name");
        }

    }

    @Override
    public void onTextToSpeechError() {

    }

    @Override
    public void onSpeechResult(String spokenText) {
        if (dialogFlowManager != null && speechManager != null && speechManager.getTextToSpeech() != null) {
            dialogFlowManager.sendToDialogFlow(spokenText, speechManager.getTextToSpeech(),speechManager);
        } else {
            Log.e("Login", "dialogFlowManager o speechManager no están listos para procesar el resultado de voz.");
        }

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //speechManager.onActivityResult(requestCode,resultCode,data);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(speechManager != null){
            speechManager.shutdown();
        }


        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();

        }
        speechManager.shutdown();
    }

    private void handleDialogFlowParams(Map<String,Value> parameters,String intentName){
        runOnUiThread(() -> {
            Log.d("Login", "Intent received: " + intentName + ", Parameters: " + parameters.toString());
            if (intentName.equals("iniciarSesion")) {
                Value nombreValue = parameters.get("respuesta_confirmacion");
                if (nombreValue != null) {
                    String valueCinfirCuent = nombreValue.getStringValue();
                    if (valueCinfirCuent.equals("sí") || valueCinfirCuent.equals("sí tengo") || valueCinfirCuent.equals("sí tengo cuenta")) {
                        // User confirmed they have an account, proceed to next steps
                       // speechManager.speak("Por favor, dime tu correo electrónico.", "ask_email");
                    } else {
                        speechManager.stopSpeaking();
                        Intent intent = new Intent(login.this, RegistrerUser.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    speechManager.speak("No entendí tu respuesta, ¿tienes cuenta?", "ask_name_retry");
                }
            } else if (intentName.equals("pedirCorreo")) {
                if (parameters.containsKey("correosSconfirm")) {
                    Value nombreValueCorr = parameters.get("correosSconfirm");
                    try {
                        String correo = nombreValueCorr.getStringValue();
                        Log.d("Login", "Setting email in EditText: " + correo);
                        etCorreo.setText(correo);
                        speechManager.speak("Gracias, ahora dime tu contraseña.", "ask_password");
                    } catch (Exception e) {
                        Log.e("Login", "Error setting email: " + e.getMessage());
                        speechManager.speak("No entendí tu correo, por favor repítelo.", "ask_name_retry");
                    }
                } else {
                    Log.e("Login", "Parameter 'correosSconfirm' not found in parameters: " + parameters.toString());
                    speechManager.speak("No entendí tu correo, por favor repítelo.", "ask_name_retry");
                }
            } else if (intentName.equals("pedirContrasenha")) {
                if (parameters.containsKey("contraseaSesion")) {
                    Value nombreValueContra = parameters.get("contraseaSesion");
                    try {
                        String contrasena = nombreValueContra.getStringValue();
                        Log.d("Login", "Setting password in EditText: " + contrasena);
                        etPassword.setText(contrasena);
                        speechManager.speak("Gracias, intentando iniciar sesión.", "login_attempt");
                        // Add login logic here if needed
                    } catch (Exception e) {
                        Log.e("Login", "Error setting password: " + e.getMessage());
                        speechManager.speak("No entendí tu contraseña, por favor repítelo.", "ask_name_retry");
                    }
                } else {
                    Log.e("Login", "Parameter 'contraseaSesion' not found in parameters: " + parameters.toString());
                    speechManager.speak("No entendí tu contraseña, por favor repítelo.", "ask_name_retry");
                }
            } else if (intentName.equals("finalizarsesion")){
                if (parameters.containsKey("finalizarSe")){
                    Value nombreS = parameters.get("finalizarSe");
                    try {
                        String confirSesion = nombreS.getStringValue();
                        if (confirSesion.equals("sí")){

                        }else {
                            speak("cancenlando inicio de sesion,regresando a la pantalla principal","confir_datos_retry");
                            new Handler().postDelayed(()->{
                                Intent intent = new Intent(login.this, MapaUI.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            },5000);
                        }
                    }catch (Exception e){

                    }
                }
            }else {
                speechManager.speak("No entendí tu respuesta, por favor intenta de nuevo.", "ask_name_retry");

            }
        });
    }

}