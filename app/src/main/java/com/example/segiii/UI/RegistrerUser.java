package com.example.segiii.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
    private EditText editTextNombre, editTextApellidos, editTextEmail, editTextContrasena, editTextConfirmarContrasena;
    private Button btnGuardar;
    private ImageButton btnActualizar, imgBack;
    private ImageView robotImage;
    private FloatingActionButton fabAccessibility;
    private ConstraintLayout mainLayout;
    private TextView title, nombreLabel, apellidosLabel, emailLabel, contrasenaLabel, confirmarContrasenaLabel;
    private SegiDataBase db;
    private Usuario usuarioActual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer_user);


        mainLayout = findViewById(R.id.main_layout);
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextApellidos = findViewById(R.id.editTextApellidos);
        editTextEmail = findViewById(R.id.email);
        editTextContrasena = findViewById(R.id.editTextContrasena);
        editTextConfirmarContrasena = findViewById(R.id.editTextConfirmarContrasena);
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
        db = SegiDataBase.getDatabase(this);

        applyAccessibilitySettings();

        // Botón Volver
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
        cargarPrimerUsuario();
    }

    private void setupPasswordValidation() {
        // Validar al presionar "Done" o Enter en confirmar contraseña
        editTextConfirmarContrasena.setOnEditorActionListener((v, actionId, event) -> {
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
        String password = editTextContrasena.getText().toString();
        String confirmPassword = editTextConfirmarContrasena.getText().toString();
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        boolean audioEnabled = prefs.getBoolean("audio_enabled", true);
        int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;

        Log.d(TAG, "Validando contraseñas: password=" + password + ", confirmPassword=" + confirmPassword);

        // Validar si ambos campos tienen contenido
        if (!password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                // Contraseñas no coinciden
                Log.d(TAG, "Contraseñas no coinciden, aplicando borde rojo");
                editTextContrasena.setBackgroundResource(R.drawable.error_border);
                editTextConfirmarContrasena.setBackgroundResource(R.drawable.error_border);
                editTextContrasena.setTextColor(defaultTextColor);
                editTextConfirmarContrasena.setTextColor(defaultTextColor);
                editTextContrasena.invalidate(); // Forzar actualización visual
                editTextConfirmarContrasena.invalidate();
                if (audioEnabled) {
                }
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else {
                // Contraseñas coinciden
                Log.d(TAG, "Contraseñas coinciden, restableciendo bordes");
                editTextContrasena.setBackgroundResource(android.R.drawable.edit_text);
                editTextConfirmarContrasena.setBackgroundResource(android.R.drawable.edit_text);
                editTextContrasena.setTextColor(Color.parseColor("#60B5E3"));
                editTextConfirmarContrasena.setTextColor(Color.parseColor("#60B5E3"));
                editTextContrasena.invalidate();
                editTextConfirmarContrasena.invalidate();
            }
        } else {
            // Al menos un campo está vacío
            Log.d(TAG, "Campos incompletos, aplicando borde rojo");
            editTextContrasena.setBackgroundResource(R.drawable.error_border);
            editTextConfirmarContrasena.setBackgroundResource(R.drawable.error_border);
            editTextContrasena.setTextColor(defaultTextColor);
            editTextConfirmarContrasena.setTextColor(defaultTextColor);
            editTextContrasena.invalidate();
            editTextConfirmarContrasena.invalidate();
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
            editTextNombre.setTextColor(Color.WHITE);
            editTextApellidos.setTextColor(Color.WHITE);
            editTextEmail.setTextColor(Color.WHITE);
            editTextContrasena.setTextColor(Color.WHITE);
            editTextConfirmarContrasena.setTextColor(Color.WHITE);
            editTextNombre.setHintTextColor(Color.LTGRAY);
            editTextApellidos.setHintTextColor(Color.LTGRAY);
            editTextEmail.setHintTextColor(Color.LTGRAY);
            editTextContrasena.setHintTextColor(Color.LTGRAY);
            editTextConfirmarContrasena.setHintTextColor(Color.LTGRAY);
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
            editTextNombre.setTextColor(Color.BLACK);
            editTextApellidos.setTextColor(Color.BLACK);
            editTextEmail.setTextColor(Color.BLACK);
            editTextContrasena.setTextColor(Color.BLACK);
            editTextConfirmarContrasena.setTextColor(Color.BLACK);
            editTextNombre.setHintTextColor(Color.GRAY);
            editTextApellidos.setHintTextColor(Color.GRAY);
            editTextEmail.setHintTextColor(Color.GRAY);
            editTextContrasena.setHintTextColor(Color.GRAY);
            editTextConfirmarContrasena.setHintTextColor(Color.GRAY);
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
            editTextNombre.setTextSize(18);
            editTextApellidos.setTextSize(18);
            editTextEmail.setTextSize(18);
            editTextContrasena.setTextSize(18);
            editTextConfirmarContrasena.setTextSize(18);
            btnGuardar.setTextSize(40);
        } else {
            title.setTextSize(24);
            nombreLabel.setTextSize(14);
            apellidosLabel.setTextSize(14);
            emailLabel.setTextSize(14);
            contrasenaLabel.setTextSize(14);
            confirmarContrasenaLabel.setTextSize(14);
            editTextNombre.setTextSize(14);
            editTextApellidos.setTextSize(14);
            editTextEmail.setTextSize(14);
            editTextContrasena.setTextSize(14);
            editTextConfirmarContrasena.setTextSize(14);
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
                    editTextNombre.setText(usuarioActual.getNombre());
                    editTextApellidos.setText(usuarioActual.getApellidos());
                    editTextEmail.setText(usuarioActual.getEmail());
                    editTextContrasena.setText(usuarioActual.getContrasena());
                    editTextConfirmarContrasena.setText(usuarioActual.getContrasena());
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
        String nombre = editTextNombre.getText().toString().trim();
        String apellidos = editTextApellidos.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String contrasena = editTextContrasena.getText().toString();
        String confirmarContrasena = editTextConfirmarContrasena.getText().toString();
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
            editTextContrasena.setBackgroundResource(R.drawable.error_border);
            editTextConfirmarContrasena.setBackgroundResource(R.drawable.error_border);
            SharedPreferences prefsInner = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            boolean highContrast = prefsInner.getBoolean("high_contrast", false);
            int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;
            editTextContrasena.setTextColor(defaultTextColor);
            editTextConfirmarContrasena.setTextColor(defaultTextColor);
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

        if (editTextNombre.getText().toString().trim().isEmpty()) {
            editTextNombre.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            editTextNombre.setBackgroundColor(Color.TRANSPARENT);
        }

        if (editTextApellidos.getText().toString().trim().isEmpty()) {
            editTextApellidos.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            editTextApellidos.setBackgroundColor(Color.TRANSPARENT);
        }

        if (editTextEmail.getText().toString().trim().isEmpty()) {
            editTextEmail.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            editTextEmail.setBackgroundColor(Color.TRANSPARENT);
        }

        if (editTextContrasena.getText().toString().isEmpty()) {
            editTextContrasena.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            editTextContrasena.setBackgroundColor(Color.TRANSPARENT);
        }

        if (editTextConfirmarContrasena.getText().toString().isEmpty()) {
            editTextConfirmarContrasena.setBackgroundColor(Color.RED);
            camposValidos = false;
        } else {
            editTextConfirmarContrasena.setBackgroundColor(Color.TRANSPARENT);
        }

        if (!camposValidos) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Por favor llena todos los campos en rojo", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = editTextEmail.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!editTextContrasena.getText().toString().equals(editTextConfirmarContrasena.getText().toString())) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            editTextContrasena.setBackgroundResource(R.drawable.error_border);
            editTextConfirmarContrasena.setBackgroundResource(R.drawable.error_border);
            SharedPreferences prefsInner = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            boolean highContrast = prefsInner.getBoolean("high_contrast", false);
            int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;
            editTextContrasena.setTextColor(defaultTextColor);
            editTextConfirmarContrasena.setTextColor(defaultTextColor);
            return;
        }

        new Thread(() -> {
            usuarioActual.setNombre(editTextNombre.getText().toString().trim());
            usuarioActual.setApellidos(editTextApellidos.getText().toString().trim());
            usuarioActual.setEmail(editTextEmail.getText().toString().trim());
            usuarioActual.setContrasena(editTextContrasena.getText().toString());

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
        editTextNombre.setBackgroundColor(Color.TRANSPARENT);
        editTextApellidos.setBackgroundColor(Color.TRANSPARENT);
        editTextEmail.setBackgroundColor(Color.TRANSPARENT);
        editTextContrasena.setBackgroundResource(android.R.drawable.edit_text);
        editTextConfirmarContrasena.setBackgroundResource(android.R.drawable.edit_text);
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        int defaultTextColor = highContrast ? Color.WHITE : Color.BLACK;
        editTextContrasena.setTextColor(defaultTextColor);
        editTextConfirmarContrasena.setTextColor(defaultTextColor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        }

}