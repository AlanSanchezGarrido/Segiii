package com.example.segiii.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.MapaUI;
import com.example.segiii.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class login extends AppCompatActivity {
    private static final String TAG = "Login";
    private EditText editTextEmail, editTextPassword;
    private Button btnLogin;
    private ImageButton imgBack;
    private TextView txtOptions, txtRewrite, welcomeText, emailLabel, passwordLabel;
    private FloatingActionButton fabAccessibility;
    private ConstraintLayout mainLayout;
    private SegiDataBase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mainLayout = findViewById(R.id.main_layout);
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_ingresar);
        imgBack = findViewById(R.id.img_back);
        txtOptions = findViewById(R.id.txt_options);
        txtRewrite = findViewById(R.id.txt_rewrite);
        welcomeText = findViewById(R.id.welcome_text);
        emailLabel = findViewById(R.id.email_label);
        passwordLabel = findViewById(R.id.password_label);
        fabAccessibility = findViewById(R.id.fab_accessibility);
        db = SegiDataBase.getDatabase(this);

        applyAccessibilitySettings();

        btnLogin.setOnClickListener(v -> loginUsuario());

        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, MapaUI.class);
            startActivity(intent);
            finish();
        });

        txtOptions.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, RegistrerUser.class);
            startActivity(intent);
        });

        txtRewrite.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, RegistrerUser.class);
            startActivity(intent);
        });

        fabAccessibility.setOnClickListener(v -> {
            Log.d(TAG, "Clic en fabAccessibility");
            Toast.makeText(this, "Clic en accesibilidad", Toast.LENGTH_SHORT).show();
            showAccessibilityMenu();
        });


    }

    private void applyAccessibilitySettings() {
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        boolean increaseTextSize = prefs.getBoolean("increase_text_size", false);
        String colorblindFilter = prefs.getString("colorblind_filter", null);

        Log.d(TAG, "Applying accessibility settings: highContrast=" + highContrast +
                ", increaseTextSize=" + increaseTextSize + ", colorblindFilter=" + colorblindFilter);

        if (highContrast) {
            mainLayout.setBackgroundColor(Color.BLACK);
            welcomeText.setTextColor(Color.WHITE);
            emailLabel.setTextColor(Color.WHITE);
            passwordLabel.setTextColor(Color.WHITE);
            txtOptions.setTextColor(Color.WHITE);
            txtRewrite.setTextColor(Color.WHITE);
            editTextEmail.setTextColor(Color.WHITE);
            editTextPassword.setTextColor(Color.WHITE);
            editTextEmail.setHintTextColor(Color.LTGRAY);
            editTextPassword.setHintTextColor(Color.LTGRAY);
            btnLogin.setTextColor(Color.WHITE);
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.DKGRAY));
        } else {
            mainLayout.setBackgroundColor(Color.parseColor("#1976D2"));
            welcomeText.setTextColor(Color.BLACK);
            emailLabel.setTextColor(Color.BLACK);
            passwordLabel.setTextColor(Color.BLACK);
            txtOptions.setTextColor(Color.BLACK);
            txtRewrite.setTextColor(Color.BLACK);
            editTextEmail.setTextColor(Color.BLACK);
            editTextPassword.setTextColor(Color.BLACK);
            editTextEmail.setHintTextColor(Color.GRAY);
            editTextPassword.setHintTextColor(Color.GRAY);
            btnLogin.setTextColor(Color.WHITE);
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#212121")));
        }

        if ("red_green".equals(colorblindFilter)) {
            mainLayout.setBackgroundColor(Color.parseColor("#AAAAAA"));
        } else if ("blue_yellow".equals(colorblindFilter)) {
            mainLayout.setBackgroundColor(Color.parseColor("#CCCCFF"));
        }

        if (increaseTextSize) {
            welcomeText.setTextSize(24);
            emailLabel.setTextSize(18);
            passwordLabel.setTextSize(18);
            txtOptions.setTextSize(26);
            txtRewrite.setTextSize(26);
            editTextEmail.setTextSize(18);
            editTextPassword.setTextSize(18);
            btnLogin.setTextSize(35);
            Log.d(TAG, "Applied increased text sizes");
        } else {
            welcomeText.setTextSize(20);
            emailLabel.setTextSize(16);
            passwordLabel.setTextSize(16);
            txtOptions.setTextSize(22);
            txtRewrite.setTextSize(22);
            editTextEmail.setTextSize(16);
            editTextPassword.setTextSize(16);
            btnLogin.setTextSize(30);
            Log.d(TAG, "Applied normal text sizes");
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
                    Log.d(TAG, "Preferences cleared");
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
        Log.d(TAG, "Refreshing activity");
        Intent intent = new Intent(this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        finish();
        startActivity(intent);
    }

    private void loginUsuario() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean audioEnabled = prefs.getBoolean("audio_enabled", true);

        editTextEmail.setBackgroundResource(android.R.drawable.edit_text);
        editTextPassword.setBackgroundResource(android.R.drawable.edit_text);

        if (email.isEmpty() || password.isEmpty()) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            if (email.isEmpty()) editTextEmail.setBackgroundResource(R.drawable.error_border);
            if (password.isEmpty()) editTextPassword.setBackgroundResource(R.drawable.error_border);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (audioEnabled) {
            }
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            editTextEmail.setBackgroundResource(R.drawable.error_border);
            return;
        }

        new Thread(() -> {
            Usuario usuario = db.usuarioDAO().getUsuarioByEmail(email);
            runOnUiThread(() -> {
                if (usuario != null && usuario.getContrasena().equals(password)) {
                    if (audioEnabled) {
                    }
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(login.this, MapaUI.class);
                        startActivity(intent);
                        finish();
                    }, 2000);
                } else {
                    if (audioEnabled) {
                    }
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    editTextEmail.setBackgroundResource(R.drawable.error_border);
                    editTextPassword.setBackgroundResource(R.drawable.error_border);
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}