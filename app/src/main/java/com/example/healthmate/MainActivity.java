package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {



    private HealthDatabaseHelper database;
    private TextView stepsSummary;
    private TextView caloriesSummary;
    private TextView waterSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );
                view.setPadding(
                        bars.left,
                        bars.top,
                        bars.right,
                        bars.bottom
                );
                return insets;
            });
        }

        database = new HealthDatabaseHelper(this);

        stepsSummary = findViewById(R.id.stepsSummary);
        caloriesSummary = findViewById(R.id.caloriesSummary);
        waterSummary = findViewById(R.id.waterSummary);

        Button addButton = findViewById(R.id.addRecordButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddRecordActivity.class
            );
            startActivity(intent);
        });

        Button recordsButton = findViewById(R.id.viewRecordsButton);
        recordsButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RecordsActivity.class
            );
            startActivity(intent);
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(view -> {
            getSharedPreferences("session", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            Intent intent = new Intent(
                    MainActivity.this,
                    AuthActivity.class
            );
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void refreshDashboard() {
        if (database == null) {
            return;
        }

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        try (android.database.Cursor cursor =
                     database.getTodayRecord(today)) {
            if (cursor.moveToFirst()) {
                stepsSummary.setText(cursor.getInt(0) + " steps");
                caloriesSummary.setText(cursor.getInt(1) + " kcal");
                waterSummary.setText(cursor.getInt(2) + " ml");
            } else {
                stepsSummary.setText("0 steps");
                caloriesSummary.setText("0 kcal");
                waterSummary.setText("0 ml");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}