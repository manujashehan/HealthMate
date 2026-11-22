package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class HealthDatabaseHelper extends SQLiteOpenHelper {

    public HealthDatabaseHelper(Context context) {
        super(context, "healthmate.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE health_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT UNIQUE, steps INTEGER, calories INTEGER, water INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS health_records");
        onCreate(db);
    }

    public void saveRecord(String date, int steps, int calories, int water) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("steps", steps);
        values.put("calories", calories);
        values.put("water", water);

        db.insertWithOnConflict(
                "health_records", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Cursor getAllRecords() {
        return getReadableDatabase().rawQuery(
                "SELECT date, steps, calories, water FROM health_records ORDER BY date DESC",
                null);
    }

    public Cursor getTodayRecord(String date) {
        return getReadableDatabase().rawQuery(
                "SELECT steps, calories, water FROM health_records WHERE date = ?",
                new String[]{date});
    }
    public Cursor searchRecords(String date) {
        return getReadableDatabase().rawQuery(
                "SELECT date, steps, calories, water FROM health_records " +
                        "WHERE date LIKE ? ORDER BY date DESC",
                new String[]{"%" + date + "%"});
    }

    public void updateRecord(String date, int steps, int calories, int water) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        values.put("calories", calories);
        values.put("water", water);

        getWritableDatabase().update(
                "health_records", values, "date = ?", new String[]{date});
    }

    public void deleteRecord(String date) {
        getWritableDatabase().delete(
                "health_records", "date = ?", new String[]{date});
    }
}
