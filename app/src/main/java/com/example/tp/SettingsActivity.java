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

    private static final String SHARED_PREFS  = "sharedPrefs";
    private static final String CHECKBOX_KEY  = "rememberMe";

    private TextView      tvUserFullname;
    private FirebaseAuth  auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        tvUserFullname = findViewById(R.id.tvUserFullname);

        RelativeLayout itemProfile  = findViewById(R.id.itemProfile);
        RelativeLayout itemSecurity = findViewById(R.id.itemSecurity);   // Change password
        RelativeLayout itemLanguage = findViewById(R.id.itemLanguage);
        RelativeLayout btnLogout    = findViewById(R.id.btnLogout);
        ImageButton    btnNotif     = findViewById(R.id.btnNotification);

        loadUserData();


        if (btnNotif != null)
            btnNotif.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationActivity.class)));

        if (itemProfile != null)
            itemProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileDetailsActivity.class)));

        // ── Change password ──
        if (itemSecurity != null)
            itemSecurity.setOnClickListener(v ->
                    startActivity(new Intent(this, ChangePasswordActivity.class)));

        if (itemLanguage != null)
            itemLanguage.setOnClickListener(v ->
                    startActivity(new Intent(this, activity_langage.class)));

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                sp.edit().putBoolean(CHECKBOX_KEY, false).apply();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            });
        }
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String name = doc.getString("fullname");
                            tvUserFullname.setText(
                                    (name != null && !name.isEmpty()) ? name : "User");
                        }
                    } else {
                        tvUserFullname.setText("Error loading name");
                    }
                });
    }
}
