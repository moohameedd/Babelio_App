package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        ImageButton btnSettingsNav = findViewById(R.id.btnSettingsNav);

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
            });
        }

        if (btnSettingsNav != null) {
            btnSettingsNav.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            });
        }
    }
}
