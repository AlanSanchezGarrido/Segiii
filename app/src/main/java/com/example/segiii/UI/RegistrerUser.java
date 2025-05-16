package com.example.segiii.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.MapaUI;
import com.example.segiii.R;
import com.example.segiii.SpeedRecognizer;
import com.example.segiii.VoiceNavigationActivity;
import com.example.segiii.wordSegui;

public class RegistrerUser extends VoiceNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer_user);
    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        if (command.contains("login")) {
            Intent intent = new Intent(this, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else if (command.contains("mapa")) {
            Intent intent = new Intent(this, MapaUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    }
