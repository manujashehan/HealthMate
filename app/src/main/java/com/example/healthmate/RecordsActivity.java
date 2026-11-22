package com.example.healthmate;


import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity {

    private HealthDatabaseHelper database;
    private EditText dateSearchInput;
    private ListView recordsList;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> dates = new ArrayList<>();
    private String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        database = new HealthDatabaseHelper(this);
        dateSearchInput = findViewById(R.id.dateSearchInput);
        recordsList = findViewById(R.id.recordsList);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        recordsList.setAdapter(adapter);

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> {
            currentFilter = dateSearchInput.getText().toString().trim();
            loadRecords(currentFilter);
        });

        Button showAllButton = findViewById(R.id.showAllButton);
        showAllButton.setOnClickListener(view -> {
            dateSearchInput.setText("");
            currentFilter = "";
            loadRecords("");
        });

        recordsList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < dates.size()) {
                editRecord(dates.get(position));
            }
        });

        recordsList.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < dates.size()) {
                confirmDelete(dates.get(position));
            }
            return true;
        });

        loadRecords("");
    }

    private void loadRecords(String dateFilter) {
        dates.clear();
        adapter.clear();

        Cursor cursor = dateFilter.isEmpty()
                ? database.getAllRecords()
                : database.searchRecords(dateFilter);

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            dates.add(date);

            String row = date
                    + "\nSteps: " + cursor.getInt(1)
                    + "   Calories: " + cursor.getInt(2) + " kcal"
                    + "   Water: " + cursor.getInt(3) + " ml";

            adapter.add(row);
        }
        cursor.close();

        if (dates.isEmpty()) {
            adapter.add("No records found.");
        }
    }

    private void editRecord(String date) {
        Cursor cursor = database.searchRecords(date);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        int steps = cursor.getInt(1);
        int calories = cursor.getInt(2);
        int water = cursor.getInt(3);
        cursor.close();

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);
        form.setPadding(padding, 0, padding, 0);

        EditText stepsInput = numberInput("Steps", steps);
        EditText caloriesInput = numberInput("Calories", calories);
        EditText waterInput = numberInput("Water (ml)", water);

        form.addView(stepsInput);
        form.addView(caloriesInput);
        form.addView(waterInput);

        new AlertDialog.Builder(this)
                .setTitle("Edit " + date)
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        database.updateRecord(
                                date,
                                Integer.parseInt(stepsInput.getText()
                                        .toString().trim()),
                                Integer.parseInt(caloriesInput.getText()
                                        .toString().trim()),
                                Integer.parseInt(waterInput.getText()
                                        .toString().trim())
                        );

                        loadRecords(currentFilter);
                        Toast.makeText(this, "Record updated",
                                Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter all three numbers",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private EditText numberInput(String hint, int value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(value));
        return input;
    }

    private void confirmDelete(String date) {
        new AlertDialog.Builder(this)
                .setTitle("Delete record?")
                .setMessage("Delete the health record for " + date + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.deleteRecord(date);
                    loadRecords(currentFilter);

                    Toast.makeText(this, "Record deleted",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}