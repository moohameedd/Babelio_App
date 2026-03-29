package com.example.tp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);

        // randooom
        tvName.setText("Mohamed ISAMM");
        tvEmail.setText("mohamed@isamm.tn");

        // bakc button
        btnBack.setOnClickListener(v -> finish());
    }
}