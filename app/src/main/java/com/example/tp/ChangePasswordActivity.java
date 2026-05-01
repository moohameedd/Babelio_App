package com.example.tp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        etCurrentPassword   = findViewById(R.id.etCurrentPassword);
        etNewPassword       = findViewById(R.id.etNewPassword);
        etConfirmPassword   = findViewById(R.id.etConfirmPassword);
        Button btnSave      = findViewById(R.id.btnSavePassword);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnSave != null) btnSave.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        if (etCurrentPassword.getText() == null
                || etNewPassword.getText() == null
                || etConfirmPassword.getText() == null) return;

        String current = etCurrentPassword.getText().toString().trim();
        String newPwd   = etNewPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPwd.equals(confirm)) {
            Toast.makeText(this, getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPwd.length() < 6) {
            Toast.makeText(this, getString(R.string.error_password_short), Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, getString(R.string.search_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate first
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current);
        user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (!reAuthTask.isSuccessful()) {
                Toast.makeText(this, getString(R.string.forgot_password_title), Toast.LENGTH_SHORT).show(); // Simplified fallback
                return;
            }
            // Now update password
            user.updatePassword(newPwd).addOnCompleteListener(updateTask -> {
                if (updateTask.isSuccessful()) {
                    Toast.makeText(this, getString(R.string.password_changed_notif), Toast.LENGTH_SHORT).show();
                    NotificationActivity.addNotification(getString(R.string.password_changed_notif));
                    finish();
                } else {
                    String msg = updateTask.getException() != null
                            ? updateTask.getException().getMessage()
                            : getString(R.string.search_failed);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
