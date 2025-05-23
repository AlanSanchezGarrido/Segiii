package com.example.segiii.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.SistemaNavegacion;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.UI.MapaUI;
import com.example.segiii.R;
import com.example.segiii.vozSegi.ComandoPrincipal.SpeedRecognizer;
import com.example.segiii.vozSegi.ComandoPrincipal.VoiceNavigationActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class RegistrerUser extends VoiceNavigationActivity {

    private static final String TAG = "RegistrerUser";
    private EditText etNombre, etApellidos, etEmail, etPassword, etConfirmPassword;
    private SegiDataBase segiDataBase;
    private ConstraintLayout mainLayout;
    private TextView title, nombreLabel, apellidosLabel, emailLabel, contrasenaLabel, confirmarContrasenaLabel;
    private Button btnGuardar;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private FloatingActionButton fabAccessibility;
    private ImageButton imgBack;
    private enum RegistrationState {
        NOMBRE, APELLIDOS, EMAIL, CONFIRM_EMAIL, PASSWORD, CONFIRM_PASSWORD, CONFIRM_DATA
    }
    private RegistrationState currentState = RegistrationState.NOMBRE;
    private SpeedRecognizer dataRecognizer;
    private boolean isPasswordVisible = false; // Added for et_password toggle
    private boolean isConfirmPasswordVisible = false;

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
        mainLayout = findViewById(R.id.main_layout);
        title = findViewById(R.id.title);
        nombreLabel = findViewById(R.id.nombre_label);
        apellidosLabel = findViewById(R.id.apellidos_label);
        emailLabel = findViewById(R.id.email_label);
        contrasenaLabel = findViewById(R.id.contrasena_label);
        confirmarContrasenaLabel = findViewById(R.id.confirmar_contrasena_label);
        btnGuardar = findViewById(R.id.img_save);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        fabAccessibility = findViewById(R.id.fab_accessibility);
        imgBack = findViewById(R.id.img_back);// Toggle password visibility for et_password
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

        // Toggle password visibility for et_confirpassword
        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                // Show confirm password and set open eye icon
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleConfirmPassword.setImageResource(R.drawable.visibilidad);
            } else {
                // Hide confirm password and set closed eye icon
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleConfirmPassword.setImageResource(R.drawable.ojo);
            }
            // Move cursor to the end of the text
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });
        // Aplicar configuraciones de accesibilidad
        applyAccessibilitySettings();

        // Configurar botón de accesibilidad
        if (fabAccessibility != null) {
            fabAccessibility.setOnClickListener(v -> {
                Log.d(TAG, "Clic en fabAccessibility");
                Toast.makeText(this, "Clic en accesibilidad", Toast.LENGTH_SHORT).show();
                showAccessibilityMenu();
            });
        } else {
            Log.e(TAG, "fabAccessibility is null - check layout XML");
        }

        // Configurar botón de retroceso
        if (imgBack != null) {
            imgBack.setOnClickListener(v -> {
                Log.d(TAG, "Clic en img_back");
                Toast.makeText(this, "Redirigiendo a MapaUI", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent(RegistrerUser.this, MapaUI.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error al redirigir a MapaUI: " + e.getMessage());
                    Toast.makeText(this, "Error al redirigir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "imgBack is null - check layout XML");
        }

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



    private void applyAccessibilitySettings() {
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        String colorblindFilter = prefs.getString("colorblind_filter", null);
        boolean increaseTextSize = prefs.getBoolean("increase_text_size", false);

        if (highContrast) {
            mainLayout.setBackgroundColor(Color.BLACK);
            title.setTextColor(Color.WHITE);
            nombreLabel.setTextColor(Color.WHITE);
            apellidosLabel.setTextColor(Color.WHITE);
            emailLabel.setTextColor(Color.WHITE);
            contrasenaLabel.setTextColor(Color.WHITE);
            confirmarContrasenaLabel.setTextColor(Color.WHITE);
            btnGuardar.setTextColor(Color.WHITE);
            btnGuardar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.DKGRAY));
            etNombre.setTextColor(Color.WHITE);
            etApellidos.setTextColor(Color.WHITE);
            etEmail.setTextColor(Color.WHITE);
            etPassword.setTextColor(Color.WHITE);
            etConfirmPassword.setTextColor(Color.WHITE);
            etNombre.setHintTextColor(Color.LTGRAY);
            etApellidos.setHintTextColor(Color.LTGRAY);
            etEmail.setHintTextColor(Color.LTGRAY);
            etPassword.setHintTextColor(Color.LTGRAY);
            etConfirmPassword.setHintTextColor(Color.LTGRAY);
            btnTogglePassword.setColorFilter(Color.WHITE);
            btnToggleConfirmPassword.setColorFilter(Color.WHITE);
        } else {
            mainLayout.setBackgroundColor(Color.parseColor("#1976D2"));
            title.setTextColor(Color.BLACK);
            nombreLabel.setTextColor(Color.BLACK);
            apellidosLabel.setTextColor(Color.BLACK);
            emailLabel.setTextColor(Color.BLACK);
            contrasenaLabel.setTextColor(Color.BLACK);
            confirmarContrasenaLabel.setTextColor(Color.BLACK);
            btnGuardar.setTextColor(Color.WHITE);
            btnGuardar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#212121")));
            etNombre.setTextColor(Color.BLACK);
            etApellidos.setTextColor(Color.BLACK);
            etEmail.setTextColor(Color.BLACK);
            etPassword.setTextColor(Color.BLACK);
            etConfirmPassword.setTextColor(Color.BLACK);
            etNombre.setHintTextColor(Color.GRAY);
            etApellidos.setHintTextColor(Color.GRAY);
            etEmail.setHintTextColor(Color.GRAY);
            etPassword.setHintTextColor(Color.GRAY);
            etConfirmPassword.setHintTextColor(Color.GRAY);
            btnTogglePassword.setColorFilter(Color.BLACK);
            btnToggleConfirmPassword.setColorFilter(Color.BLACK);
        }

        if ("red_green".equals(colorblindFilter)) {
            mainLayout.setBackgroundColor(Color.parseColor("#AAAAAA"));
        } else if ("blue_yellow".equals(colorblindFilter)) {
            mainLayout.setBackgroundColor(Color.parseColor("#CCCCFF"));
        }

        if (increaseTextSize) {
            title.setTextSize(30);
            nombreLabel.setTextSize(18);
            apellidosLabel.setTextSize(18);
            emailLabel.setTextSize(18);
            contrasenaLabel.setTextSize(18);
            confirmarContrasenaLabel.setTextSize(18);
            etNombre.setTextSize(18);
            etApellidos.setTextSize(18);
            etEmail.setTextSize(18);
            etPassword.setTextSize(18);
            etConfirmPassword.setTextSize(18);
            btnGuardar.setTextSize(40);
        } else {
            title.setTextSize(24);
            nombreLabel.setTextSize(14);
            apellidosLabel.setTextSize(14);
            emailLabel.setTextSize(14);
            contrasenaLabel.setTextSize(14);
            confirmarContrasenaLabel.setTextSize(14);
            etNombre.setTextSize(14);
            etApellidos.setTextSize(14);
            etEmail.setTextSize(14);
            etPassword.setTextSize(14);
            etConfirmPassword.setTextSize(14);
            btnGuardar.setTextSize(35);
        }
    }

    private void showAccessibilityMenu() {
        try {
            Log.d(TAG, "Attempting to show accessibility menu");
            Dialog dialog = new Dialog(this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            Log.d(TAG, "LayoutInflater obtained: " + (inflater != null));
            View dialogView = inflater.inflate(R.layout.accesibility_menu, null);
            Log.d(TAG, "Dialog view inflated: " + (dialogView != null));
            dialog.setContentView(dialogView);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Log.d(TAG, "Dialog configured");

            Button btnHighContrast = dialogView.findViewById(R.id.btn_high_contrast1);
            Button btnColorblindRedGreen = dialogView.findViewById(R.id.btn_colorblind_red_green2);
            Button btnColorblindBlueYellow = dialogView.findViewById(R.id.btn_colorblind_blue_yellow3);
            Button btnIncreaseTextSize = dialogView.findViewById(R.id.btn_increase_text_size4);
            Button btnResetFilters = dialogView.findViewById(R.id.btn_reset_filters5);

            Log.d(TAG, "Button statuses: " +
                    "btnHighContrast=" + (btnHighContrast != null ? "found" : "null") +
                    ", btnColorblindRedGreen=" + (btnColorblindRedGreen != null ? "found" : "null") +
                    ", btnColorblindBlueYellow=" + (btnColorblindBlueYellow != null ? "found" : "null") +
                    ", btnIncreaseTextSize=" + (btnIncreaseTextSize != null ? "found" : "null") +
                    ", btnResetFilters=" + (btnResetFilters != null ? "found" : "null"));

            SharedPreferences sharedPreferences = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (btnHighContrast != null) {
                btnHighContrast.setOnClickListener(v -> {
                    editor.putBoolean("high_contrast", true);
                    editor.apply();
                    Toast.makeText(this, "Modo de alto contraste activado", Toast.LENGTH_SHORT).show();
                    refreshActivity();
                    dialog.dismiss();
                });
            }

            if (btnColorblindRedGreen != null) {
                btnColorblindRedGreen.setOnClickListener(v -> {
                    editor.putString("colorblind_filter", "red_green");
                    editor.apply();
                    Toast.makeText(this, "Filtro rojo-verde activado", Toast.LENGTH_SHORT).show();
                    refreshActivity();
                    dialog.dismiss();
                });
            }

            if (btnColorblindBlueYellow != null) {
                btnColorblindBlueYellow.setOnClickListener(v -> {
                    editor.putString("colorblind_filter", "blue_yellow");
                    editor.apply();
                    Toast.makeText(this, "Filtro azul-amarillo activado", Toast.LENGTH_SHORT).show();
                    refreshActivity();
                    dialog.dismiss();
                });
            }

            if (btnIncreaseTextSize != null) {
                btnIncreaseTextSize.setOnClickListener(v -> {
                    editor.putBoolean("increase_text_size", true);
                    editor.apply();
                    Toast.makeText(this, "Tamaño de texto aumentado", Toast.LENGTH_SHORT).show();
                    refreshActivity();
                    dialog.dismiss();
                });
            }

            if (btnResetFilters != null) {
                btnResetFilters.setOnClickListener(v -> {
                    editor.clear();
                    editor.apply();
                    Toast.makeText(this, "Filtros restablecidos", Toast.LENGTH_SHORT).show();
                    refreshActivity();
                    dialog.dismiss();
                });
            }

            Log.d(TAG, "Showing accessibility dialog");
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing accessibility menu: " + e.getMessage(), e);
            Toast.makeText(this, "Error al mostrar menú: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, RegistrerUser.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        finish();
        startActivity(intent);
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

    private void startDataRecognition() {
        Log.d(TAG, "Iniciando reconocimiento de datos para estado: " + currentState);
        if (dataRecognizer != null && !dataRecognizer.isRecognizing()) {
            dataRecognizer.startVoiceRecognition();
        }
    }

    private void processRegistrationVoiceInput(String voiceInput) {
        if (voiceInput == null || voiceInput.trim().isEmpty()) {
            askForCurrentField();
            return;
        }

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

    private void handlePasswordConfirmation(String voiceInput) {
        etConfirmPassword.setText(voiceInput);
        String password = etPassword.getText().toString().trim();
        String confirmPassword = voiceInput.trim();

        Log.d(TAG, "Comparando contraseñas - Original: '" + password + "' Confirmación: '" + confirmPassword + "'");

        if (!password.equals(confirmPassword)) {
            etPassword.setText("");
            etConfirmPassword.setText("");
            currentState = RegistrationState.PASSWORD;
            ttsManager.speak("Las contraseñas no coinciden. Vamos a intentarlo de nuevo. Por favor, dime tu contraseña.", () -> {
                startDataRecognition();
            });
        } else {
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
            etEmail.setText("");
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
                nuevoUsuario.setCorreo(correo);
                nuevoUsuario.setContrasena(contrasena);


                List<SistemaNavegacion> sistemas = segiDataBase.sistemaNavegacionDAO().getallSistemaNavegacion();
                if (sistemas.isEmpty()) {
                    SistemaNavegacion sistema = new SistemaNavegacion();
                    sistema.setNivel_detalle("Básico");
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
        String email = input.trim().toLowerCase().replace(" ", "");
        email = email.replace("arroba", "@");
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeedRecognizer.getSpeechRequestCode()) {
            if (speedRecognizer != null) {
                speedRecognizer.processVoiceResult(requestCode, resultCode, data);
            }
            if (dataRecognizer != null) {
                dataRecognizer.processVoiceResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataRecognizer != null) {
            // No hay un método específico para liberar recursos en SpeedRecognizer
            // pero sería bueno agregarlo
        }
    }
}
