package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.android.material.textfield.TextInputEditText;

public class resetPassword extends BaseActivity {
    ImageView btnClose;
    TextInputEditText etNewPassword, etConfirmPassword;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        btnClose = findViewById(R.id.btnClose);
        etNewPassword = findViewById(R.id.etNewPassword); // Assuming these IDs based on common patterns
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSignIn); // The button ID in your layout was btnSignIn

        btnClose.setOnClickListener(v -> {
            Intent goBack = new Intent(resetPassword.this, login.class);
            startActivity(goBack);
            finish();
        });

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String newPwd = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
                String confirmPwd = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

                if (newPwd.isEmpty() || confirmPwd.isEmpty()) {
                    Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPwd.equals(confirmPwd)) {
                    Toast.makeText(this, getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPwd.length() < 6) {
                    Toast.makeText(this, getString(R.string.error_password_short), Toast.LENGTH_SHORT).show();
                    return;
                }

                // In a real app, you would handle the Firebase password reset completion here.
                Toast.makeText(this, getString(R.string.save_password_btn), Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
