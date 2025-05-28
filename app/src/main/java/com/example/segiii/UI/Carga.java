package com.example.segiii.UI;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.segiii.R;

public class Carga extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar pantalla completa
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_carga);

        // Configurar el VideoView
        VideoView videoView = findViewById(R.id.videoView);
        // Ruta del video en res/raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.carga);
        videoView.setVideoURI(videoUri);

        // Iniciar el video automáticamente
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false); // No repetir el video
            videoView.start();
        });

        // Listener para cuando el video termina
        videoView.setOnCompletionListener(mp -> {
            // Iniciar MapaUI
            Intent intent = new Intent(Carga.this, MapaUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Manejo de errores del VideoView
        videoView.setOnErrorListener((mp, what, extra) -> {
            // Log del error y notificación al usuario
            android.util.Log.e("Carga", "Error al reproducir el video: what=" + what + ", extra=" + extra);
            android.widget.Toast.makeText(this, "Error al reproducir el video", android.widget.Toast.LENGTH_LONG).show();
            // Redirigir a MapaUI en caso de error
            Intent intent = new Intent(Carga.this, MapaUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        });
    }
}