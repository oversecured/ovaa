package oversecured.ovaa.activities;

import android.content.Intent;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.Map;

import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.LoginUtils;

public class StorageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        if (action == null) {
            finish();
            return;
        }
        String name = intent.getStringExtra("name");
        String value = intent.getStringExtra("value");

        switch (action) {
            case "search":
                searchProfiles(name);
                break;
            case "save":
                saveProfile(name, value);
                break;
            case "sync":
                syncAccount(intent);
                break;
            case "export_prefs":
                returnPreferences(intent);
                break;
            default:
                break;
        }
        finish();
    }

    private SQLiteDatabase database() {
        return openOrCreateDatabase("ovaa.db", MODE_PRIVATE, null);
    }

    private void searchProfiles(String name) {
        if (name == null) {
            return;
        }
        SQLiteDatabase db = database();
        db.execSQL("CREATE TABLE IF NOT EXISTS profiles (id INTEGER, name TEXT, note TEXT)");
        Cursor cursor = db.rawQuery("SELECT * FROM profiles WHERE name = '" + name + "'", null);
        cursor.close();
        db.execSQL("DELETE FROM profiles WHERE name = '" + name + "'");
        db.query("profiles", null, "name = '" + name + "'", null, null, null, null).close();
    }

    private void saveProfile(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("note", value);
        database().insert("profiles", null, values);
    }

    private void syncAccount(Intent intent) {
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        if (email == null || password == null) {
            return;
        }
        LoginUtils.getInstance(this).saveCredentials(new LoginData(email, password));
    }

    private void returnPreferences(Intent intent) {
        String prefsName = intent.getStringExtra("prefs");
        if (prefsName == null) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences(prefsName, MODE_PRIVATE);
        Intent result = new Intent();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            result.putExtra(entry.getKey(), String.valueOf(entry.getValue()));
        }
        setResult(RESULT_OK, result);
    }
}
