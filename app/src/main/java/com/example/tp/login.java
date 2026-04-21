package com.example.tp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    ImageView btnClose;
    TextView forgotPassword;
    TextView signUp;
    Button btnSignIn;
    CheckBox cbRememberMe;

    private TextInputEditText etEmail, etPassword;
    private FirebaseAuth auth;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String CHECKBOX_KEY = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Check if user is already logged in (Firebase Persistence + Our Checkbox)
        checkSession();

        btnClose = findViewById(R.id.btnClose);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUp = findViewById(R.id.signUp);
        cbRememberMe = findViewById(R.id.cb);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> loginUser());

        forgotPassword.setOnClickListener(v -> {
            Intent i = new Intent(login.this, forgotPassword.class);
            startActivity(i);
        });

        signUp.setOnClickListener(v -> {
            Intent i = new Intent(login.this, signup.class);
            startActivity(i);
        });

        btnClose.setOnClickListener(v -> {
            Intent goBack = new Intent(login.this, MainActivity.class);
            startActivity(goBack);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void checkSession() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isRememberMe = sharedPreferences.getBoolean(CHECKBOX_KEY, false);

        if (isRememberMe && auth.getCurrentUser() != null) {
            goToHome();
        }
    }

    private void loginUser() {
        if (etEmail.getText() == null || etPassword.getText() == null) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save "Stay logged in" preference
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(CHECKBOX_KEY, cbRememberMe.isChecked());
                        editor.apply();

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToHome() {
        Intent intent = new Intent(login.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
