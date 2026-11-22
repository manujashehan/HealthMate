package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;

public class AuthActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private AuthDatabaseHelper users;
    private boolean createMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSharedPreferences("session", MODE_PRIVATE).contains("email")) {
            openDashboard();
            return;
        }

        setContentView(R.layout.activity_auth);

        users = new AuthDatabaseHelper(this);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        Button loginButton = findViewById(R.id.loginButton);
        TextView createLink = findViewById(R.id.createAccountLink);
        TextView forgotLink = findViewById(R.id.forgotPasswordLink);

        loginButton.setOnClickListener(view -> loginOrCreate());

        createLink.setOnClickListener(view -> {
            createMode = !createMode;

            if (createMode) {
                loginButton.setText("Create account");
                createLink.setText("Already have an account? Log in");
                passwordInput.setHint("Create a password (8+ characters)");
            } else {
                loginButton.setText("Log in");
                createLink.setText("Create an account");
                passwordInput.setHint("Password");
            }
        });

        forgotLink.setOnClickListener(view -> resetPassword());
    }

    private void loginOrCreate() {
        String email = emailInput.getText().toString().trim().toLowerCase();
        String password = passwordInput.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return;
        }

        if (password.length() < 8) {
            passwordInput.setError("Use at least 8 characters");
            return;
        }

        String passwordHash = hashPassword(password);

        if (createMode) {
            boolean created = users.createUser(email, passwordHash);

            if (!created) {
                Toast.makeText(this,
                        "An account with this email already exists",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();

        } else {
            boolean correctPassword = users.checkPassword(email, passwordHash);

            if (!correctPassword) {
                Toast.makeText(this,
                        "Email or password is incorrect",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        getSharedPreferences("session", MODE_PRIVATE)
                .edit()
                .putString("email", email)
                .apply();

        openDashboard();
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim().toLowerCase();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter your account email first");
            return;
        }

        EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New password (8+ characters)");
        newPasswordInput.setInputType(129);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Reset password")
                .setMessage("Demo only: this does not verify your email.")
                .setView(newPasswordInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newPassword =
                            newPasswordInput.getText().toString();

                    if (newPassword.length() < 8) {
                        Toast.makeText(this,
                                "Password needs at least 8 characters",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean changed = users.resetPassword(
                            email, hashPassword(newPassword));

                    String message = changed
                            ? "Password updated"
                            : "No account found for this email";

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private String hashPassword(String password) {
        try {
            byte[] salt =
                    "HealthMate-local-demo-v1".getBytes("UTF-8");

            byte[] passwordBytes = password.getBytes("UTF-8");
            byte[] combined = new byte[salt.length + passwordBytes.length];

            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(passwordBytes, 0, combined,
                    salt.length, passwordBytes.length);

            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(combined);

            return Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void openDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}