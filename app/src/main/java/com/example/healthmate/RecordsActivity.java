package com.example.healthmate;



import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

        adapter = new ArrayAdapter<String>(
                this,
                0,
                new ArrayList<>()
        ) {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View row = convertView;

                if (row == null) {
                    row = getLayoutInflater().inflate(
                            R.layout.item_health_record,
                            parent,
                            false
                    );
                }

                TextView dateText = row.findViewById(R.id.recordDate);
                TextView detailsText = row.findViewById(R.id.recordDetails);
                Button editButton = row.findViewById(R.id.editRecordButton);
                Button deleteButton = row.findViewById(R.id.deleteRecordButton);

                String item = getItem(position);

                if (item != null && item.contains("\n")) {
                    String[] parts = item.split("\n", 2);
                    dateText.setVisibility(View.VISIBLE);
                    dateText.setText(parts[0]);
                    detailsText.setText(parts[1]);

                    if (position < dates.size()) {
                        String recordDate = dates.get(position);
                        editButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);

                        editButton.setOnClickListener(
                                view -> editRecord(recordDate));

                        deleteButton.setOnClickListener(
                                view -> confirmDelete(recordDate));
                    } else {
                        editButton.setVisibility(View.GONE);
                        deleteButton.setVisibility(View.GONE);
                    }
                } else {
                    dateText.setVisibility(View.GONE);
                    detailsText.setText(item == null ? "" : item);
                    editButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                }

                return row;
            }
        };

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

        loadRecords("");
    }

    private void loadRecords(String dateFilter) {
        dates.clear();
        adapter.clear();

        Cursor cursor = dateFilter.isEmpty()
                ? database.getAllRecords()
                : database.searchRecords(dateFilter);

        try {
            while (cursor.moveToNext()) {
                String date = cursor.getString(0);
                dates.add(date);

                String row = date
                        + "\n👣 Steps: " + cursor.getInt(1)
                        + "    🔥 Calories: " + cursor.getInt(2) + " kcal"
                        + "    💧 Water: " + cursor.getInt(3) + " ml";

                adapter.add(row);
            }
        } finally {
            cursor.close();
        }

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

