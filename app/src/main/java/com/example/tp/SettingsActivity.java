package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // btns
        RelativeLayout itemProfile = findViewById(R.id.itemProfile);
        RelativeLayout itemLanguage = findViewById(R.id.itemLanguage);



        ImageButton btnHomeNav = findViewById(R.id.btnHomeNavSettings);

        //back hoem
        if (btnHomeNav != null) {
            btnHomeNav.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        itemProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ProfileDetailsActivity.class));
        });

        itemLanguage.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, activity_langage.class));
        });
    }
}