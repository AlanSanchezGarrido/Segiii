package com.example.segiii.UI;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.R;

public class Carga extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carga);

        // Configura los insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configura el VideoView
        VideoView videoView = findViewById(R.id.videoView);

        // Ruta del video en res/raw (sin la extensión)
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.carga);
        videoView.setVideoURI(videoUri);

        // Inicia el video automáticamente
        videoView.start();

        // Listener para cuando el video termina
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Inicia el activity MapaUI
                Intent intent = new Intent(Carga.this, MapaUI.class);
                startActivity(intent);
                finish(); // Cierra la actividad carga
            }
        });
    }
}