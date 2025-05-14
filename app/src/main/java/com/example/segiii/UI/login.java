package com.example.segiii.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.segiii.MapaUI;
import com.example.segiii.R;
import com.example.segiii.UI.RegistrerUser;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        ImageButton imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, MapaUI.class);
            startActivity(intent);
        });

        TextView txtOptions = findViewById(R.id.txt_options);
        txtOptions.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, RegistrerUser.class);
            startActivity(intent);
        });


        TextView txtRewrite = findViewById(R.id.txt_rewrite);
        txtRewrite.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, RegistrerUser.class);
            startActivity(intent);
        });


        Button btnIngresar = findViewById(R.id.btn_ingresar);
        btnIngresar.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, MapaUI.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}