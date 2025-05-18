package com.example.segiii.UI;

import android.content.Intent;
import android.os.Bundle;

import com.example.segiii.MapaUI;
import com.example.segiii.R;
import com.example.segiii.VoiceNavigationActivity;

public class RegistrerUser extends VoiceNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer_user);

    }

    @Override
    protected void handleVoiceCommand(String command, String result) {
         if (command.contains("mapa")) {
            Intent intent = new Intent(this, MapaUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }
}