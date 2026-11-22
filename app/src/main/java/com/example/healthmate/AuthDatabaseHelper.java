package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AuthDatabaseHelper extends SQLiteOpenHelper{

    public AuthDatabaseHelper(Context context) {
        super(context, "healthmate_auth.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users (" +
                        "email TEXT PRIMARY KEY, " +
                        "password_hash TEXT NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean createUser(String email, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password_hash", passwordHash);

        long result = getWritableDatabase().insert("users", null, values);
        return result != -1;
    }

    public boolean checkPassword(String email, String passwordHash) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT password_hash FROM users WHERE email = ?",
                new String[]{email})) {

            return cursor.moveToFirst()
                    && passwordHash.equals(cursor.getString(0));
        }
    }

    public boolean resetPassword(String email, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put("password_hash", passwordHash);

        int updatedRows = getWritableDatabase().update(
                "users",
                values,
                "email = ?",
                new String[]{email}
        );

        return updatedRows > 0;
    }

}
