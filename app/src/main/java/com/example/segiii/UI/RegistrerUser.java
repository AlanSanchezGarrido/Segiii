package com.example.segiii.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.MapaUI;
import com.example.segiii.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class RegistrerUser extends AppCompatActivity {
    private static final String TAG = "RegistrerUser";
    private EditText etNombre, etApellidos, etEmal, etPassword, etConfirmPassword,email;
    private Button btnGuardar;
    private ImageButton btnActualizar, imgBack;
    private ImageView robotImage;
    private FloatingActionButton fabAccessibility;
    private ConstraintLayout mainLayout;
    private TextView title, nombreLabel, apellidosLabel, emailLabel, contrasenaLabel, confirmarContrasenaLabel;
    private SegiDataBase db;
    private Usuario usuarioActual;

    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_registrer_user);

        mainLayout = findViewById(R.id.main_layout);
        etNombre = findViewById(R.id.et_nombre);
        etApellidos = findViewById(R.id.et_apellido);
        etEmal = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirpassword);
        btnGuardar = findViewById(R.id.img_save);
        btnActualizar = findViewById(R.id.edit_button);
        imgBack = findViewById(R.id.img_back);
        robotImage = findViewById(R.id.robot_image);
        fabAccessibility = findViewById(R.id.fab_accessibility);
        title = findViewById(R.id.title);
        nombreLabel = findViewById(R.id.nombre_label);
        apellidosLabel = findViewById(R.id.apellidos_label);
        emailLabel = findViewById(R.id.email_label);
        contrasenaLabel = findViewById(R.id.contrasena_label);
        confirmarContrasenaLabel = findViewById(R.id.confirmar_contrasena_label);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        db = SegiDataBase.getDatabase(this);

        applyAccessibilitySettings();

        imgBack.setOnClickListener(v -> {
            Log.d(TAG, "Clic en img_back");
            Toast.makeText(this, "Redirigiendo a MapaUI", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(RegistrerUser.this, MapaUI.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Error al redirigir a MapaUI: " + e.getMessage());
                Toast.makeText(this, "Error al redirigir: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        btnGuardar.setOnClickListener(v -> guardarUsuario());
        btnActualizar.setOnClickListener(v -> actualizarUsuario());
        robotImage.setOnClickListener(v -> cargarPrimerUsuario());
        fabAccessibility.setOnClickListener(v -> {
            Log.d(TAG, "Clic en fabAccessibility");
            Toast.makeText(this, "Clic en accesibilidad", Toast.LENGTH_SHORT).show();
            showAccessibilityMenu();
        });
        setupPasswordValidation();
        setupPasswordToggle();
        cargarPrimerUsuario();
    }

    private void setupPasswordToggle() {
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ojo);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.visibilidad);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnToggleConfirmPassword.setImageResource(R.drawable.ojo);
            isConfirmPasswordVisible = false;
        } else {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnToggleConfirmPassword.setImageResource(R.drawable.visibilidad);
            isConfirmPasswordVisible = true;
        }
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void setupPasswordValidation() {
        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            Log.d(TAG, "OnEditorAction: actionId=" + actionId + ", event=" + (event != null ? event.getKeyCode() : "null"));
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                Log.d(TAG, "Disparando validación de contraseñas");
                validatePasswords();
                return true;
            }
            return false;
        });
    }

    private void validatePasswords() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        boolean audioEnabled = prefs.getBoolean("audio_enabled", true);
        int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;

        Log.d(TAG, "Validando contraseñas: password=" + password + ", confirmPassword=" + confirmPassword);

        if (!password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                Log.d(TAG, "Contraseñas no coinciden, aplicando borde rojo");
                etPassword.setBackgroundResource(R.drawable.error_border);
                etConfirmPassword.setBackgroundResource(R.drawable.error_border);
                etPassword.setTextColor(defaultTextColor);
                etConfirmPassword.setTextColor(defaultTextColor);
                etPassword.invalidate();
                etConfirmPassword.invalidate();
                if (audioEnabled) {
                }
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Contraseñas coinciden, restableciendo bordes");
                etPassword.setBackgroundResource(android.R.drawable.edit_text);
                etConfirmPassword.setBackgroundResource(android.R.drawable.edit_text);
                etPassword.setTextColor(Color.parseColor("#60B5E3"));
                etConfirmPassword.setTextColor(Color.parseColor("#60B5E3"));
                etPassword.invalidate();
                etConfirmPassword.invalidate();
            }
        } else {
            Log.d(TAG, "Campos incompletos, aplicando borde rojo");
            etPassword.setBackgroundResource(R.drawable.error_border);
            etConfirmPassword.setBackgroundResource(R.drawable.error_border);
            etPassword.setTextColor(defaultTextColor);
            etConfirmPassword.setTextColor(defaultTextColor);
            etPassword.invalidate();
            etConfirmPassword.invalidate();
            if (audioEnabled) {
            }
            Toast.makeText(this, "Completa ambos campos de contraseña", Toast.LENGTH_SHORT).show();
        }
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
            etEmal.setTextColor(Color.WHITE);
            etPassword.setTextColor(Color.WHITE);
            etConfirmPassword.setTextColor(Color.WHITE);
            etNombre.setHintTextColor(Color.LTGRAY);
            etApellidos.setHintTextColor(Color.LTGRAY);
            etEmal.setHintTextColor(Color.LTGRAY);
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
            etEmal.setTextColor(Color.BLACK);
            etPassword.setTextColor(Color.BLACK);
            etConfirmPassword.setTextColor(Color.BLACK);
            etNombre.setHintTextColor(Color.GRAY);
            etApellidos.setHintTextColor(Color.GRAY);
            etEmal.setHintTextColor(Color.GRAY);
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
            etEmal.setTextSize(18);
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
            etEmal.setTextSize(14);
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
            View dialogView = inflater.inflate(R.layout.accessibility_menu, null);
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

    private void cargarPrimerUsuario() {
        new Thread(() -> {
            List<Usuario> usuarios = db.usuarioDAO().getAllUsuarios();
            SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            boolean audioEnabled = prefs.getBoolean("audio_enabled", true);
            if (usuarios != null && !usuarios.isEmpty()) {
                usuarioActual = usuarios.get(0);
                runOnUiThread(() -> {
                    etNombre.setText(usuarioActual.getNombre());
                    etApellidos.setText(usuarioActual.getApellidos());
                    etEmal.setText(usuarioActual.getEmail());
                    etPassword.setText(usuarioActual.getContrasena());
                    etConfirmPassword.setText(usuarioActual.getContrasena());
                    resetColorCampos();
                    if (audioEnabled) {
                    }
                });
            } else {
                usuarioActual = null;
                runOnUiThread(() -> {
                    Toast.makeText(this, "No hay usuarios en la base de datos", Toast.LENGTH_SHORT).show();
                    if (audioEnabled) {
                    }
                });
            }
        }).start();
    }

    private void guardarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String email = etEmal.getText().toString().trim();
        String contrasena = etPassword.getText().toString();
        String confirmarContrasena = etConfirmPassword.getText().toString();
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean audioEnabled = prefs.getBoolean("audio_enabled", true);

        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etPassword.setBackgroundResource(R.drawable.error_border);
            etConfirmPassword.setBackgroundResource(R.drawable.error_border);
            SharedPreferences prefsInner = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            boolean highContrast = prefsInner.getBoolean("high_contrast", false);
            int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;
            etPassword.setTextColor(defaultTextColor);
            etConfirmPassword.setTextColor(defaultTextColor);
            return;
        }

        new Thread(() -> {
            Usuario usuarioExistente = db.usuarioDAO().getUsuarioByEmail(email);
            if (usuarioExistente != null) {
                runOnUiThread(() -> {
                    if (audioEnabled) {
                    }
                    Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setEmail(email);
            usuario.setContrasena(contrasena);

            db.usuarioDAO().insert(usuario);

            runOnUiThread(() -> {
                if (audioEnabled) {
                }
                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                usuarioActual = usuario;
                try {
                    Intent intent = new Intent(RegistrerUser.this, MapaUI.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error al redirigir a MapaUI tras registro: " + e.getMessage());
                    Toast.makeText(this, "Error al redirigir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void actualizarUsuario() {
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean audioEnabled = prefs.getBoolean("audio_enabled", true);

        if (usuarioActual == null) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "No hay usuario para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean camposValidos = true;

        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            etNombre.setBackgroundColor(Color.TRANSPARENT);
        }

        if (etApellidos.getText().toString().trim().isEmpty()) {
            etApellidos.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            etApellidos.setBackgroundColor(Color.TRANSPARENT);
        }

        if (etEmal.getText().toString().trim().isEmpty()) {
            etEmal.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            etEmal.setBackgroundColor(Color.TRANSPARENT);
        }

        if (etPassword.getText().toString().isEmpty()) {
            etPassword.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            etPassword.setBackgroundColor(Color.TRANSPARENT);
        }

        if (etConfirmPassword.getText().toString().isEmpty()) {
            etConfirmPassword.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            etConfirmPassword.setBackgroundColor(Color.TRANSPARENT);
        }

        if (!camposValidos) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Por favor llena todos los campos en rojo", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmal.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etPassword.setBackgroundResource(R.drawable.error_border);
            etConfirmPassword.setBackgroundResource(R.drawable.error_border);
            SharedPreferences prefsInner = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            boolean highContrast = prefsInner.getBoolean("high_contrast", false);
            int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;
            etPassword.setTextColor(defaultTextColor);
            etConfirmPassword.setTextColor(defaultTextColor);
            return;
        }

        new Thread(() -> {
            usuarioActual.setNombre(etNombre.getText().toString().trim());
            usuarioActual.setApellidos(etApellidos.getText().toString().trim());
            usuarioActual.setEmail(etEmal.getText().toString().trim());
            usuarioActual.setContrasena(etPassword.getText().toString());

            db.usuarioDAO().update(usuarioActual);

            runOnUiThread(() -> {
                if (audioEnabled) {
                }
                Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                resetColorCampos();
            });
        }).start();
    }

    private void resetColorCampos() {
        etNombre.setBackgroundColor(Color.TRANSPARENT);
        etApellidos.setBackgroundColor(Color.TRANSPARENT);
        etEmal.setBackgroundColor(Color.TRANSPARENT);
        etPassword.setBackgroundResource(android.R.drawable.edit_text);
        etConfirmPassword.setBackgroundResource(android.R.drawable.edit_text);
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;
        etPassword.setTextColor(defaultTextColor);
        etConfirmPassword.setTextColor(defaultTextColor);
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        btnTogglePassword.setImageResource(R.drawable.ojo);
        btnToggleConfirmPassword.setImageResource(R.drawable.ojo);
        isPasswordVisible = false;
        isConfirmPasswordVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}