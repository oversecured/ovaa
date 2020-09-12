package oversecured.ovaa.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.LoginUtils;

public class CredentialsProvider extends ContentProvider {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LoginData loginData = LoginUtils.getInstance(getContext()).getLoginData();
        MatrixCursor cursor = new MatrixCursor(new String[]{"email", "password"});
        cursor.addRow(new String[]{loginData.email, loginData.password});
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
