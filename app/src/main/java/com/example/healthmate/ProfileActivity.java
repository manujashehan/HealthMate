package com.example.healthmate;



import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameText;
    private TextView emailText;
    private String email;
    private SharedPreferences profilePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameText = findViewById(R.id.profileName);
        emailText = findViewById(R.id.profileEmail);
        Button editNameButton = findViewById(R.id.editNameButton);

        SharedPreferences sessionPreferences =
                getSharedPreferences("session", MODE_PRIVATE);

        email = sessionPreferences.getString("email", "");

        profilePreferences =
                getSharedPreferences("profile", MODE_PRIVATE);

        emailText.setText(email.isEmpty() ? "No email found" : email);
        showSavedName();

        editNameButton.setOnClickListener(view -> showNameDialog());
    }

    private void showSavedName() {
        String savedName = profilePreferences.getString(
                "name_" + email,
                ""
        );

        if (savedName.isEmpty()) {
            nameText.setText("Add your name");
        } else {
            nameText.setText(savedName);
        }
    }

    private void showNameDialog() {
        EditText nameInput = new EditText(this);
        nameInput.setHint("Enter your name");

        String currentName = profilePreferences.getString(
                "name_" + email,
                ""
        );
        nameInput.setText(currentName);

        new AlertDialog.Builder(this)
                .setTitle("Your name")
                .setView(nameInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText()
                            .toString()
                            .trim();

                    if (name.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Enter your name",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    profilePreferences.edit()
                            .putString("name_" + email, name)
                            .apply();

                    showSavedName();

                    Toast.makeText(
                            this,
                            "Name saved",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }
}