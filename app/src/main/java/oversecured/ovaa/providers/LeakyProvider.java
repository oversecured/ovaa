package oversecured.ovaa.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

import oversecured.ovaa.utils.LoginUtils;

public class LeakyProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        MatrixCursor cursor = new MatrixCursor(new String[] {"email", "password"});
        cursor.addRow(new Object[] {
                LoginUtils.getInstance(getContext()).getLoginData().email,
                LoginUtils.getInstance(getContext()).getLoginData().password
        });
        return cursor;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {

        File file = new File(getContext().getFilesDir(), uri.getLastPathSegment());
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "application/octet-stream";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        new File(getContext().getFilesDir(), String.valueOf(selection)).delete();
        return 1;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {

        return 0;
    }
}
