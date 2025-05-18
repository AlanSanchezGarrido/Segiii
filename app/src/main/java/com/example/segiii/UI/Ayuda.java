package com.example.segiii.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.MapaUI;
import com.example.segiii.R;

public class Ayuda extends AppCompatActivity {
    private static final String TAG = "Ayuda";
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);

        // Ajustar márgenes para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar el botón de retroceso
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            // Regresar a login en lugar de MapaUI para mantener el flujo
            Intent intent = new Intent(Ayuda.this, login.class);
            startActivity(intent);
            finish();
        });

        // Configurar el VideoView
        videoView = findViewById(R.id.video_view);
        try {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorialsegi;
            videoView.setVideoURI(Uri.parse(videoPath));
            videoView.start();
            videoView.setOnCompletionListener(mp -> videoView.start());
        } catch (Exception e) {
            Log.e(TAG, "Error al reproducir video: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar el video", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad si el video no se puede cargar
        }
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
            videoView.stopPlayback(); // Libera los recursos del VideoView
            videoView = null;
        }
    }
}