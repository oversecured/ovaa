package oversecured.ovaa.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResultTheftActivity extends Activity {
    private static final int PICK_ANY_CODE = 2001;
    private static final int PICK_IMAGE_CODE = 2002;
    private static final int GET_CONTENT_CODE = 2003;
    private static final int OPEN_DOCUMENT_CODE = 2004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        pickAnything(type);
        pickImage();
        getContent(type);
        openDocument(type);
    }

    private void pickAnything(String type) {
        Intent pick = new Intent(Intent.ACTION_PICK);
        pick.setType(type != null ? type : "*/*");
        startActivityForResult(pick, PICK_ANY_CODE);
    }

    private void pickImage() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick, PICK_IMAGE_CODE);
    }

    private void getContent(String type) {
        Intent get = new Intent(Intent.ACTION_GET_CONTENT);
        get.setType(type != null ? type : "*/*");
        get.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(get, GET_CONTENT_CODE);
    }

    private void openDocument(String type) {
        Intent open = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        open.setType(type != null ? type : "application/pdf");
        open.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(open, OPEN_DOCUMENT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            finish();
            return;
        }
        Uri uri = data.getData();
        if (uri != null) {
            uploadToServer(uri, data.getStringExtra("server"));
            copyToClipboard(uri);
        }
        forwardResult(data);
        finish();
    }

    private byte[] read(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            return out.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private void uploadToServer(Uri uri, String server) {
        if (server == null) {
            return;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(server).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(read(uri));
            }
            connection.getResponseCode();
        } catch (IOException ignored) {
        }
    }

    private void copyToClipboard(Uri uri) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newRawUri("picked", uri));
    }

    private void forwardResult(Intent data) {
        setResult(RESULT_OK, data);
        startActivity(new Intent(this, VulnerableWebViewActivity.class)
                .putExtra("url", data.getStringExtra("next_url")));
    }
}
