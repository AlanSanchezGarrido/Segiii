package com.example.segiii.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.SistemaNavegacion;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.example.segiii.vozSegi.flujoVoz.DialogFlowManager;
import com.example.segiii.vozSegi.flujoVoz.SpeechManager;
import com.google.protobuf.Value;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistrerUser extends VoiceNavigationActivity implements SpeechManager.OnSpeechResultListener,SpeechManager.OnSpeechManagerReadyListener {

    private SpeechManager speechManager;
    private DialogFlowManager dialogFlowManager;
    private TextToSpeech textToSpeech;
    private EditText etNombre, etApellidos, etEmail, etPassword, etConfirmPassword;
    private SegiDataBase segiDataBase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer_user);

        segiDataBase = SegiDataBase.getDatabase(this);
        etNombre = findViewById(R.id.et_nombre);
        etApellidos = findViewById(R.id.et_apellido);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirpassword);

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
                                if(ContextCompat.checkSelfPermission(RegistrerUser.this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED){
                                    speechManager.startVoiceRecognition();

                                }
                            }
                        });

                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(()->{
                            if(ContextCompat.checkSelfPermission(RegistrerUser.this, Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
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
            speechManager.speak("Vamos a registarnos, ¿Cuál es tu Nombre?", "ask_name");
        }
    }

    @Override
    public void onTextToSpeechError() {

    }

    @Override
    public void onSpeechResult(String spokenText) {
        if (dialogFlowManager != null && speechManager != null && speechManager.getTextToSpeech() != null) {
            dialogFlowManager.sendToDialogFlow(spokenText, speechManager.getTextToSpeech(), speechManager);
        } else {
            Log.e("RegistrerUser", "dialogFlowManager o speechManager no están listos para procesar el resultado de voz.");
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
            Log.d("RegistrerUser", "Intent: " + intentName + ", Parameters: " + parameters.toString());
            switch (intentName) {
                case "collectName":
                    if (parameters.containsKey("nombre")) {
                        Value nombreValue = parameters.get("nombre");
                        try {
                            String nombre = nombreValue.getStringValue();
                            etNombre.setText(nombre);
                        } catch (Exception e) {
                            Log.e("RegistrerUser", "Failed to get string value for 'nombre': " + e.getMessage());
                            speechManager.speak("No entendí tu nombre, por favor repítelo.", "ask_name_retry");
                        }
                    } else {
                        Log.e("RegistrerUser", "Parameter 'nombre' not found");
                        speechManager.speak("No entendí tu nombre, por favor repítelo.", "ask_name_retry");
                    }
                    break;
                case "RecopilarApellido":
                    if (parameters.containsKey("apellidos")) {
                        Value apellidosValue = parameters.get("apellidos");
                        try {
                            String apellidos = apellidosValue.getStringValue();
                            etApellidos.setText(apellidos);
                            //speak("¿Cuál es tu nombre de usuario?", "ask_nickname");
                        } catch (Exception e) {
                            Log.e("RegistrerUser", "Failed to get string value for 'apellidos': " + e.getMessage());
                            speechManager.speak("No entendí tus apellidos, por favor repítelos.", "ask_surname_retry");
                        }
                    } else {
                        Log.e("RegistrerUser", "Parameter 'apellidos' not found");
                        speechManager.speak("No entendí tus apellidos, por favor repítelos.", "ask_surname_retry");
                    }
                    break;
                case "RecopilarCorreos":
                    if (parameters.containsKey("correos")) {
                        Value nomUsuarioValue = parameters.get("correos");
                        try {
                            String usuario = nomUsuarioValue.getStringValue();
                            etEmail.setText(usuario);
                            // speak("Ingresa tu contraseña.", "ask_password");
                        } catch (Exception e) {
                            Log.e("RegistrerUser", "Failed to get string value for 'nomUsuario': " + e.getMessage());
                            speechManager.speak("No entendí tu nombre de usuario, por favor repítelo.", "ask_nickname_retry");
                        }
                    } else {
                        Log.e("RegistrerUser", "Parameter 'nomUsuario' not found");
                        speechManager.speak("No entendí tu nombre de usuario, por favor repítelo.", "ask_nickname_retry");
                    }
                    break;
                case "RecopilarContrasenhas":
                    if (parameters.containsKey("contrasena")) {
                        Value contrasenaValue = parameters.get("contrasena");
                        try {
                            String password = contrasenaValue.getStringValue();
                            etPassword.setText(password);
                            // speak("Confirma tu contraseña.", "ask_confirm");
                        } catch (Exception e) {
                            Log.e("RegistrerUser", "Failed to get string value for 'contrasena': " + e.getMessage());
                            speechManager.speak("No entendí tu contraseña, por favor repítela.", "ask_password_retry");
                        }
                    } else {
                        Log.e("RegistrerUser", "Parameter 'contrasena' not found");
                        speechManager.speak("No entendí tu contraseña, por favor repítela.", "ask_password_retry");
                    }
                    break;
                case "confirmarContras":
                    if (parameters.containsKey("conContrasena")) {
                        Value conContrasenaValue = parameters.get("conContrasena");
                        try {
                            String confirmacion = conContrasenaValue.getStringValue();
                            etConfirmPassword.setText(confirmacion);
                            if (etPassword.getText().toString().equals(confirmacion)) {
                                // speak("Registro completado.", "registration_success");
                            } else {
                                speechManager.speak("Las contraseñas no coinciden.", "password_error");
                                etPassword.setText("");
                                etConfirmPassword.setText("");
                            }
                        } catch (Exception e) {
                            Log.e("RegistrerUser", "Failed to get string value for 'conContrasena': " + e.getMessage());
                            speechManager.speak("No entendí la confirmación de tu contraseña, por favor repítela.", "ask_confirm_retry");
                        }
                    } else {
                        Log.e("RegistrerUser", "Parameter 'conContrasena' not found");
                        speechManager.speak("No entendí la confirmación de tu contraseña, por favor repítela.", "ask_confirm_retry");
                    }
                    break;
                case "confirDatos":
                    if (parameters.containsKey("datosGuardados")) {
                        Value condatosValue = parameters.get("datosGuardados");
                        try {
                            String usuario = condatosValue.getStringValue();
                            if (usuario.equals("sí")||usuario.equals("sí guárdalos")||usuario.equals("sí quiero que los guardes")){
                                String nombre = etNombre.getText().toString().trim();
                                String apellidos = etApellidos.getText().toString().trim();
                                String correo = etEmail.getText().toString().trim();
                                String contrasena = etPassword.getText().toString().trim();
                                String confirmarContrasena = etConfirmPassword.getText().toString().trim();

                                if (nombre.isEmpty() || apellidos.isEmpty() || usuario.isEmpty() || contrasena.isEmpty()) {
                                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (!contrasena.equals(confirmarContrasena)) {
                                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                new Thread(() -> {
                                    Usuario nuevoUsuario = new Usuario();
                                    nuevoUsuario.setNombre(nombre);
                                    nuevoUsuario.setApellidos(apellidos);
                                    nuevoUsuario.setUsuario(correo);
                                    nuevoUsuario.setContrasena(contrasena);
                                    nuevoUsuario.setId_sistema(1);
                                    long sistemaId;
                                    List<SistemaNavegacion> sistemas = segiDataBase.sistemaNavegacionDAO().getallSistemaNavegacion();
                                    if (sistemas.isEmpty()) {
                                        SistemaNavegacion sistema = new SistemaNavegacion();
                                        sistema.setNivel_detalle("Básico");
                                        sistema.setId_proveedor(1);
                                        segiDataBase.sistemaNavegacionDAO().insert(sistema);
                                        // Fetch the inserted sistema to get its ID
                                        //segiDataBase.sistemaNavegacionDAO().insert(sistema);
                                        sistema = new SistemaNavegacion();
                                        sistema.setId_sistema(1);
                                        segiDataBase.usuarioDAO().insert(nuevoUsuario);

                                    } else {
                                        //sistemaId = sistemas.get(0).getId_sistema();
                                    }
                                    // Volver al hilo principal para mostrar el resultado

                                }).start();
                                runOnUiThread(() -> {
                                    speak("Datos guardados","confir_datos" );
                                    new Handler().postDelayed(()->{
                                        Toast.makeText(RegistrerUser.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegistrerUser.this, MapaUI.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        finish();
                                    },2000);

                                });


                            }else{
                                speak("cancenlando registro,regresando a la pantalla principal","confir_datos_retry");
                                new Handler().postDelayed(()->{
                                    Intent intent = new Intent(RegistrerUser.this, MapaUI.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                },4000);

                            }
                            // speak("Ingresa tu contraseña.", "ask_password");
                        } catch (Exception e) {
                            Log.e("RegistrerUser", "Failed to get string value for 'nomUsuario': " + e.getMessage());
                            speechManager.speak("No entendí tu nombre de usuario, por favor repítelo.", "ask_nickname_retry");
                        }
                    } else {
                        Log.e("RegistrerUser", "Parameter 'nomUsuario' not found");
                        speechManager.speak("No entendí tu nombre de usuario, por favor repítelo.", "ask_nickname_retry");
                    }

                    break;
                default:
                    Log.w("RegistrerUser", "Unhandled intent: " + intentName);
                    speechManager.speak("Intent no reconocido, por favor intenta de nuevo.", "unknown_intent");
                    break;
            }
        });
    }
}