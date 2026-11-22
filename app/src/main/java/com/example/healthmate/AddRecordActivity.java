package com.example.healthmate;



import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private HealthDatabaseHelper database;
    private EditText stepsInput;
    private EditText caloriesInput;
    private EditText waterInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        database = new HealthDatabaseHelper(this);

        stepsInput = findViewById(R.id.stepsInput);
        caloriesInput = findViewById(R.id.caloriesInput);
        waterInput = findViewById(R.id.waterInput);

        String displayDate = new SimpleDateFormat(
                "EEEE, d MMMM yyyy", Locale.getDefault()
        ).format(new Date());

        TextView dateText = findViewById(R.id.todayDateText);
        dateText.setText(displayDate);

        Button saveButton = findViewById(R.id.saveRecordButton);
        saveButton.setOnClickListener(view -> saveRecord());

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> finish());
    }

    private void saveRecord() {
        String stepsText = stepsInput.getText().toString().trim();
        String caloriesText = caloriesInput.getText().toString().trim();
        String waterText = waterInput.getText().toString().trim();

        if (stepsText.isEmpty()) {
            stepsInput.setError("Enter your steps");
            return;
        }

        if (caloriesText.isEmpty()) {
            caloriesInput.setError("Enter calories burned");
            return;
        }

        if (waterText.isEmpty()) {
            waterInput.setError("Enter water intake");
            return;
        }

        String date = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());

        database.saveRecord(
                date,
                Integer.parseInt(stepsText),
                Integer.parseInt(caloriesText),
                Integer.parseInt(waterText)
        );

        Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}