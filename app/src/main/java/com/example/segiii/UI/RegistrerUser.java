package com.example.segiii.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;

import java.util.List;

public class RegistrerUser extends VoiceNavigationActivity {

    private static final String TAG = "RegistrerUser";
    private EditText etNombre, etApellidos, etEmail, etPassword, etConfirmPassword;
    private SegiDataBase segiDataBase;
    private enum RegistrationState {
        NOMBRE, APELLIDOS, EMAIL, CONFIRM_EMAIL, PASSWORD, CONFIRM_PASSWORD, CONFIRM_DATA
    }
    private RegistrationState currentState = RegistrationState.NOMBRE;
    private SpeedRecognizer dataRecognizer; // Reconocedor específico para datos

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer_user);

        // Inicializar base de datos y referencias de UI
        segiDataBase = SegiDataBase.getDatabase(this);
        etNombre = findViewById(R.id.et_nombre);
        etApellidos = findViewById(R.id.et_apellido);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirpassword);

        // Configurar edge-to-edge display
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Configurar el reconocedor específico para entrada de datos
        setupDataRecognizer();

        // Iniciar el proceso de registro por voz después de un breve retraso
        new Handler().postDelayed(this::startRegistrationProcess, 1000);
    }

    private void setupDataRecognizer() {
        // Creamos un reconocedor específico para capturar datos (no comandos)
        dataRecognizer = new SpeedRecognizer(this, new SpeedRecognizer.OnVoiceCommandListener() {
            @Override
            public void onCommandProcessed(String command, String result) {
                // Procesamos el texto reconocido como datos, no como comandos
                processRegistrationVoiceInput(command);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error en reconocimiento de voz: " + errorMessage);
                // Reintentar después de un error con un pequeño delay
                new Handler().postDelayed(() -> {
                    ttsManager.speak("No pude entender. Por favor, inténtalo de nuevo.", () -> {
                        new Handler().postDelayed(() -> {
                            askForCurrentField();
                        }, 500);
                    });
                }, 100);
            }

            @Override
            public void onRecognitionCancelled() {
                Log.d(TAG, "Reconocimiento cancelado");
                // Reiniciar el reconocimiento después de una pausa
                new Handler().postDelayed(() -> {
                    askForCurrentField();
                }, 1000);
            }

            @Override
            public void onNavigationCommand(String destination) {

            }
        });

        // Configuramos expresamente el modo de entrada de datos
        dataRecognizer.setDataEntryMode(true);

        // Añadimos algunos comandos de navegación global como excepción
        dataRecognizer.addCustomCommand("mapa", (context) -> {
            navigateToMap();
        });

        dataRecognizer.addCustomCommand("salir", (context) -> {
            finishAffinity();
        });
    }

    private void startRegistrationProcess() {
        ttsManager.speak("Vamos a registrarte en el sistema. Por favor, dime tu nombre.", () -> {
            speedRecognizer.setDataEntryMode(true);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startDataRecognition();
            } else {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        });
    }

    // Método para iniciar el reconocimiento en modo datos
    private void startDataRecognition() {
        Log.d(TAG, "Iniciando reconocimiento de datos para estado: " + currentState);
        if (dataRecognizer != null && !dataRecognizer.isRecognizing()) {
            // Pequeña pausa antes de iniciar para evitar conflictos
                dataRecognizer.startVoiceRecognition();

        }
    }

    private void processRegistrationVoiceInput(String voiceInput) {
        if (voiceInput == null || voiceInput.trim().isEmpty()) {
            askForCurrentField();
            return;
        }

        // Comprobar si es un comando de navegación para permitir salir del flujo
        if (voiceInput.toLowerCase().contains("mapa") ||
                voiceInput.toLowerCase().contains("salir")) {
            handleVoiceCommand(voiceInput.toLowerCase(), "comando global");
            return;
        }

        Log.d(TAG, "Procesando entrada de voz como dato: " + voiceInput + " para estado: " + currentState);

        switch (currentState) {
            case NOMBRE:
                etNombre.setText(voiceInput);
                currentState = RegistrationState.APELLIDOS;
                ttsManager.speak("Gracias. Ahora dime tus apellidos.", () -> {
                    startDataRecognition();
                });
                break;

            case APELLIDOS:
                etApellidos.setText(voiceInput);
                currentState = RegistrationState.EMAIL;
                ttsManager.speak("Perfecto. Ahora necesito tu correo electrónico.", () -> {
                    startDataRecognition();
                });
                break;

            case EMAIL:
                String email = formatEmail(voiceInput);
                etEmail.setText(email);
                currentState = RegistrationState.CONFIRM_EMAIL;
                ttsManager.speak("Tu correo es " + readableEmail(email) + ". Por favor, confirma con sí o no.", () -> {
                    startDataRecognition();
                });
                break;

            case CONFIRM_EMAIL:
                handleEmailConfirmation(voiceInput);
                break;

            case PASSWORD:
                etPassword.setText(voiceInput);
                currentState = RegistrationState.CONFIRM_PASSWORD;
                ttsManager.speak("Por favor, confirma tu contraseña repitiéndola.", () -> {
                    startDataRecognition();
                });
                break;

            case CONFIRM_PASSWORD:
                handlePasswordConfirmation(voiceInput);
                break;

            case CONFIRM_DATA:
                handleConfirmationResponse(voiceInput);
                break;
        }
    }

    // Método separado para manejar la confirmación de contraseña
    private void handlePasswordConfirmation(String voiceInput) {
        etConfirmPassword.setText(voiceInput);
        String password = etPassword.getText().toString().trim();
        String confirmPassword = voiceInput.trim();

        Log.d(TAG, "Comparando contraseñas - Original: '" + password + "' Confirmación: '" + confirmPassword + "'");

        if (!password.equals(confirmPassword)) {
            // Las contraseñas no coinciden - limpiar campos y reiniciar
            etPassword.setText("");
            etConfirmPassword.setText("");
            currentState = RegistrationState.PASSWORD;

            ttsManager.speak("Las contraseñas no coinciden. Vamos a intentarlo de nuevo. Por favor, dime tu contraseña.", () -> {
                // Usar un delay más largo para asegurar que el TTS termine completamente

                    startDataRecognition();

            });
        } else {
            // Las contraseñas coinciden - continuar al siguiente paso
            currentState = RegistrationState.CONFIRM_DATA;
            ttsManager.speak("Perfecto. Todos los campos están completos. ¿Quieres guardar estos datos? Di sí o no.", () -> {
                startDataRecognition();
            });
        }
    }

    private void handleEmailConfirmation(String response) {
        if (response.toLowerCase().contains("sí") || response.toLowerCase().contains("si")) {
            currentState = RegistrationState.PASSWORD;
            ttsManager.speak("Gracias. Ahora dime tu contraseña.", () -> {
                startDataRecognition();
            });
        } else if (response.toLowerCase().contains("no")) {
            etEmail.setText(""); // Limpiar el campo de correo
            currentState = RegistrationState.EMAIL;
            ttsManager.speak("Entendido, vamos a intentarlo de nuevo. Por favor, dime tu correo electrónico.", () -> {
                startDataRecognition();
            });
        } else {
            ttsManager.speak("No entendí tu respuesta. Por favor, di sí para confirmar tu correo o no para volver a ingresarlo.", () -> {
                startDataRecognition();
            });
        }
    }

    private String readableEmail(String email) {
        // Leer el correo de manera más natural, pronunciando los caracteres especiales
        return email.replace("@", " arroba ").replace(".", " punto ");
    }

    private void handleConfirmationResponse(String response) {
        if (response.toLowerCase().contains("sí") || response.toLowerCase().contains("si")) {
            saveUserData();
        } else if (response.toLowerCase().contains("no")) {
            ttsManager.speak("Cancelando registro y volviendo a la pantalla principal.", () -> {
                new Handler().postDelayed(() -> {
                    navigateToMap();
                }, 1500);
            });
        } else {
            ttsManager.speak("No entendí tu respuesta. Por favor, di sí para guardar o no para cancelar.", () -> {
                startDataRecognition();
            });
        }
    }

    private void saveUserData() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String correo = etEmail.getText().toString().trim();
        String contrasena = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            ttsManager.speak("Todos los campos son obligatorios. Vamos a intentarlo de nuevo.", () -> {
                currentState = RegistrationState.NOMBRE;
                askForCurrentField();
            });
            return;
        }

        new Thread(() -> {
            try {
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre(nombre);
                nuevoUsuario.setApellidos(apellidos);
                nuevoUsuario.setUsuario(correo);
                nuevoUsuario.setContrasena(contrasena);
                nuevoUsuario.setId_sistema(1);

                List<SistemaNavegacion> sistemas = segiDataBase.sistemaNavegacionDAO().getallSistemaNavegacion();
                if (sistemas.isEmpty()) {
                    SistemaNavegacion sistema = new SistemaNavegacion();
                    sistema.setNivel_detalle("Básico");
                    sistema.setId_proveedor(1);
                    segiDataBase.sistemaNavegacionDAO().insert(sistema);
                }

                segiDataBase.usuarioDAO().insert(nuevoUsuario);

                runOnUiThread(() -> {
                    ttsManager.speak("Usuario registrado con éxito. Redirigiendo al mapa.", () -> {
                        navigateToMap();
                        new Handler().postDelayed(() -> {
                            Toast.makeText(RegistrerUser.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                            navigateToMap();
                        }, 1500);
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error al guardar usuario: " + e.getMessage());
                    ttsManager.speak("Hubo un error al guardar los datos. Por favor, inténtalo de nuevo más tarde.", () -> {
                        navigateToMap();
                    });
                });
            }
        }).start();
    }

    private void askForCurrentField() {
        Log.d(TAG, "Pidiendo campo actual: " + currentState);
        switch (currentState) {
            case NOMBRE:
                ttsManager.speak("Por favor, dime tu nombre.", () -> startDataRecognition());
                break;
            case APELLIDOS:
                ttsManager.speak("Necesito tus apellidos.", () -> startDataRecognition());
                break;
            case EMAIL:
                ttsManager.speak("¿Cuál es tu correo electrónico?", () -> startDataRecognition());
                break;
            case CONFIRM_EMAIL:
                String email = etEmail.getText().toString().trim();
                ttsManager.speak("Tu correo es " + readableEmail(email) + ". Por favor, confirma con sí o no.", () -> startDataRecognition());
                break;
            case PASSWORD:
                ttsManager.speak("Dime tu contraseña.", () -> startDataRecognition());
                break;
            case CONFIRM_PASSWORD:
                ttsManager.speak("Confirma tu contraseña.", () -> startDataRecognition());
                break;
            case CONFIRM_DATA:
                ttsManager.speak("¿Quieres guardar estos datos? Di sí o no.", () -> startDataRecognition());
                break;
        }
    }

    private String formatEmail(String input) {
        // Eliminar espacios y formatear como correo electrónico si es necesario
        String email = input.trim().toLowerCase().replace(" ", "");

        // Si el usuario dice el correo con "arroba" en lugar de @ podemos reemplazarlo
        email = email.replace("arroba", "@");

        // Si faltara el dominio, podríamos añadir uno por defecto (opcional)
        if (!email.contains("@")) {
            email += "@gmail.com";
        }

        return email;
    }

    private void navigateToMap() {
        Intent intent = new Intent(RegistrerUser.this, MapaUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        if (command.contains("mapa")) {
            navigateToMap();
        } else if (command.contains("salir")) {
            finishAffinity();
        }
        // Aquí puedes agregar más comandos globales que funcionen en esta actividad
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Procesamos los resultados para ambos reconocedores
        if (requestCode == SpeedRecognizer.getSpeechRequestCode()) {
            // Para el reconocedor principal (comandos)
            if (speedRecognizer != null) {
                speedRecognizer.processVoiceResult(requestCode, resultCode, data);
            }

            // Para el reconocedor de datos
            if (dataRecognizer != null) {
                dataRecognizer.processVoiceResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos adicionales
        if (dataRecognizer != null) {
            // No hay un método específico para liberar recursos en SpeedRecognizer
            // pero sería bueno agregarlo
        }
    }
}