package com.example.tp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvGender;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvGender = findViewById(R.id.tvProfileGender);

        loadUserData();

        btnBack.setOnClickListener(v -> finish());
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
                                String email = document.getString("email");
                                String gender = document.getString("gender");

                                if (fullname != null) tvName.setText(fullname);
                                if (email != null) tvEmail.setText(email);
                                if (gender != null) tvGender.setText(gender);
                            }
                        } else {
                            Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
