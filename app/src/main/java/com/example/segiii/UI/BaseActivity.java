package com.example.segiii.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE);
        applyAccessibilityFilters();
    }

    protected void applyAccessibilityFilters() {
        // Aplicar alto contraste
        if (sharedPreferences.getBoolean("high_contrast", false)) {
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(android.R.color.black));
        }

        // Aplicar filtro de daltonismo (simulación básica)
        String colorblindFilter = sharedPreferences.getString("colorblind_filter", "");
        switch (colorblindFilter) {
            case "red_green":
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                break;
            case "blue_yellow":
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
        }

        // Aumentar tamaño de texto
        if (sharedPreferences.getBoolean("increase_text_size", false)) {
            Configuration configuration = getResources().getConfiguration();
            configuration.fontScale = 1.3f; // Aumentar en 30%
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }
    }
}