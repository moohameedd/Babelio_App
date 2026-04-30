package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPassword extends AppCompatActivity {

    ImageView btnClose;
    Button btnResetPassword;
    TextInputEditText emailInput;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        btnClose = findViewById(R.id.btnClose);
        btnResetPassword = findViewById(R.id.btnSignIn);
        emailInput = findViewById(R.id.etEmail);

        btnClose.setOnClickListener(v -> {
            startActivity(new Intent(forgotPassword.this, login.class));
            finish();
        });

        btnResetPassword.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendReset(email);
        });
    }

    private void sendReset(String email) {
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Sending...");

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Password reset email sent to " + email + ". Check your inbox.",
                            Toast.LENGTH_LONG).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(new Intent(forgotPassword.this, login.class));
                        finish();
                    }, 2500);
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();

                    if (errorMsg != null && errorMsg.contains("no user record")) {
                        Toast.makeText(this, "No account found with this email.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }

                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Reset Password");
                });
    }
}