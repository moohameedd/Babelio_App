package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends BaseActivity {

    private TextInputEditText etEmail, etPassword, etFullname, etCpassword;
    private RadioGroup rgGender;
    private Button btnSignup;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnClose = findViewById(R.id.btnClose);
        etFullname = findViewById(R.id.etFullname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCpassword = findViewById(R.id.etCpassword);
        rgGender = findViewById(R.id.rgGender);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> registerUser());

        btnClose.setOnClickListener(v -> {
            Intent goBack = new Intent(signup.this, login.class);
            startActivity(goBack);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registerUser() {
        if (etEmail.getText() == null || etPassword.getText() == null || etFullname.getText() == null || etCpassword.getText() == null) return;

        String fullname = etFullname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String cpassword = etCpassword.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, getString(R.string.error_select_gender), Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbSelectedGender = findViewById(selectedGenderId);
        String gender = rbSelectedGender.getText().toString();

        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || cpassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(cpassword)) {
            Toast.makeText(this, getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, getString(R.string.error_password_short), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        
                        // Create user map for Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullname", fullname);
                        user.put("email", email);
                        user.put("gender", gender);

                        // Save to "users" collection
                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(signup.this, login.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Signup failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
