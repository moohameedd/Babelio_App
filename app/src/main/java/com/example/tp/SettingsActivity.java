package com.example.tp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String CHECKBOX_KEY = "rememberMe";
    
    private TextView tvUserFullname;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        tvUserFullname = findViewById(R.id.tvUserFullname);
        RelativeLayout itemProfile = findViewById(R.id.itemProfile);
        RelativeLayout itemLanguage = findViewById(R.id.itemLanguage);
        RelativeLayout btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnHomeNav = findViewById(R.id.btnHomeNavSettings);
        ImageButton btnNotification = findViewById(R.id.btnNotification);

        // Load dynamic user data
        loadUserData();

        // Navigation
        if (btnHomeNav != null) {
            btnHomeNav.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                startActivity(new Intent(SettingsActivity.this, NotificationActivity.class));
            });
        }

        if (itemProfile != null) {
            itemProfile.setOnClickListener(v -> {
                startActivity(new Intent(SettingsActivity.this, ProfileDetailsActivity.class));
            });
        }

        if (itemLanguage != null) {
            itemLanguage.setOnClickListener(v -> {
                startActivity(new Intent(SettingsActivity.this, activity_langage.class));
            });
        }

        // Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(CHECKBOX_KEY, false);
                editor.apply();

                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SettingsActivity.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String fullname = document.getString("fullname");
                                if (fullname != null && !fullname.isEmpty()) {
                                    tvUserFullname.setText(fullname);
                                } else {
                                    tvUserFullname.setText("User");
                                }
                            }
                        } else {
                            tvUserFullname.setText("Error loading name");
                        }
                    });
        }
    }
}
