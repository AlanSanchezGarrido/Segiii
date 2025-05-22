package com.example.segiii.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Ayuda extends AppCompatActivity {
    private static final String TAG = "Ayuda";
    private VideoView videoView;
    private ConstraintLayout mainLayout;
    private FloatingActionButton fabAccessibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);

        // Ajustar márgenes para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias UI
        mainLayout = findViewById(R.id.main);
        fabAccessibility = findViewById(R.id.fab_accessibility);
        ImageButton backButton = findViewById(R.id.btn_back);
        videoView = findViewById(R.id.video_view);

        // Aplicar configuraciones de accesibilidad
        applyAccessibilitySettings();

        // Botón flotante de accesibilidad
        fabAccessibility.setOnClickListener(v -> {
            Log.d(TAG, "Clic en FAB accesibilidad desde Ayuda");
            showAccessibilityMenu();
        });

        // Botón para regresar al login
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Ayuda.this, login.class);
            startActivity(intent);
            finish();
        });

        // Reproducir video
        try {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorialsegi;
            videoView.setVideoURI(Uri.parse(videoPath));
            videoView.start();
            videoView.setOnCompletionListener(mp -> videoView.start());
        } catch (Exception e) {
            Log.e(TAG, "Error al reproducir video: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar el video", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void applyAccessibilitySettings() {
        SharedPreferences prefs = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        boolean highContrast = prefs.getBoolean("high_contrast", false);
        String colorblindFilter = prefs.getString("colorblind_filter", null);

        if (highContrast) {
            mainLayout.setBackgroundColor(Color.BLACK);
        } else if ("red_green".equals(colorblindFilter)) {
            mainLayout.setBackgroundColor(Color.parseColor("#AAAAAA")); // Gris para rojo-verde
        } else if ("blue_yellow".equals(colorblindFilter)) {
            mainLayout.setBackgroundColor(Color.parseColor("#CCCCFF")); // Azul pálido para azul-amarillo
        } else {
            // No hay filtros activos: usa el azul original definido en el XML (#1976D2)
            mainLayout.setBackgroundColor(Color.parseColor("#1976D2"));
        }
    }


    private void showAccessibilityMenu() {
        try {
            Dialog dialog = new Dialog(this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.accessibility_menu, null);
            dialog.setContentView(dialogView);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Button btnHighContrast = dialogView.findViewById(R.id.btn_high_contrast1);
            Button btnColorblindRedGreen = dialogView.findViewById(R.id.btn_colorblind_red_green2);
            Button btnColorblindBlueYellow = dialogView.findViewById(R.id.btn_colorblind_blue_yellow3);
            Button btnIncreaseTextSize = dialogView.findViewById(R.id.btn_increase_text_size4);
            Button btnResetFilters = dialogView.findViewById(R.id.btn_reset_filters5);

            SharedPreferences sharedPreferences = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            btnHighContrast.setOnClickListener(v -> {
                editor.putBoolean("high_contrast", true);
                editor.apply();
                Toast.makeText(this, "Modo alto contraste activado", Toast.LENGTH_SHORT).show();
                refreshActivity();
                dialog.dismiss();
            });

            btnColorblindRedGreen.setOnClickListener(v -> {
                editor.putString("colorblind_filter", "red_green");
                editor.apply();
                Toast.makeText(this, "Filtro rojo-verde activado", Toast.LENGTH_SHORT).show();
                refreshActivity();
                dialog.dismiss();
            });

            btnColorblindBlueYellow.setOnClickListener(v -> {
                editor.putString("colorblind_filter", "blue_yellow");
                editor.apply();
                Toast.makeText(this, "Filtro azul-amarillo activado", Toast.LENGTH_SHORT).show();
                refreshActivity();
                dialog.dismiss();
            });

            btnIncreaseTextSize.setOnClickListener(v -> {
                editor.putBoolean("increase_text_size", true);
                editor.apply();
                Toast.makeText(this, "Tamaño de texto aumentado", Toast.LENGTH_SHORT).show();
                refreshActivity();
                dialog.dismiss();
            });

            btnResetFilters.setOnClickListener(v -> {
                editor.clear();
                editor.apply();
                Toast.makeText(this, "Filtros restablecidos", Toast.LENGTH_SHORT).show();
                refreshActivity();
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar menú accesibilidad: " + e.getMessage(), e);
            Toast.makeText(this, "Error al mostrar menú", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, Ayuda.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
        }
    }
}
