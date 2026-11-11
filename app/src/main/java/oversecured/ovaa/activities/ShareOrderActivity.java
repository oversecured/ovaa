package oversecured.ovaa.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import oversecured.ovaa.utils.LoginUtils;

public class ShareOrderActivity extends Activity {
    private static final String TAG = "ovaa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        if (action == null) {
            finish();
            return;
        }
        String path = intent.getStringExtra("path");
        String server = intent.getStringExtra("server");
        String phone = intent.getStringExtra("phone");
        Intent resultIntent = intent.getParcelableExtra("result_intent");
        String secret = LoginUtils.getInstance(this).getLoginData().password;

        switch (action) {
            case "share_activity":
                sendViaImplicitActivityIntent(secret);
                break;
            case "share_broadcast":
                sendViaImplicitBroadcastIntent(secret);
                break;
            case "clipboard":
                copyToClipboard(secret, path);
                break;
            case "sms":
                sendViaSms(phone, secret, path);
                break;
            case "upload":
                uploadToServer(server, secret, path);
                break;
            case "log":
                logShare(secret);
                break;
            case "result":
                setShareResult(resultIntent);
                break;
            default:
                break;
        }
        finish();
    }

    private void sendViaImplicitActivityIntent(String secret) {
        Intent intent = new Intent("oversecured.ovaa.action.SHARE_PROFILE");
        intent.putExtra("password", secret);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private void sendViaImplicitBroadcastIntent(String secret) {
        Intent intent = new Intent("oversecured.ovaa.action.PROFILE_UPDATED");
        intent.putExtra("password", secret);
        sendBroadcast(intent);
    }

    private void copyToClipboard(String secret, String path) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("password", secret));
        if (path != null) {
            clipboard.setPrimaryClip(ClipData.newUri(getContentResolver(), "file", Uri.parse(path)));
        }
    }

    private void sendViaSms(String phone, String secret, String path) {
        if (phone == null) {
            return;
        }
        SmsManager manager = SmsManager.getDefault();
        // secret is null when logged out, which SmsManager rejects; sending also needs the SEND_SMS runtime grant
        try {
            manager.sendTextMessage(phone, null, secret, null, null);
        } catch (IllegalArgumentException | SecurityException ignored) {
        }
        if (path != null) {
            try {
                manager.sendDataMessage(phone, null, (short) 0,
                        FileUtils.readFileToByteArray(new File(path)), null, null);
            } catch (IOException | SecurityException ignored) {
            }
        }
    }

    private void uploadToServer(String server, String secret, String path) {
        if (server == null) {
            return;
        }
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(server).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("X-Session", secret);
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(("password=" + secret).getBytes());
                    if (path != null) {
                        out.write(FileUtils.readFileToByteArray(new File(path)));
                    }
                }
                connection.getResponseCode();
            } catch (IOException ignored) {
            }
        }).start();
    }

    private void logShare(String secret) {
        Log.d(TAG, "current password: " + secret);
    }

    private void setShareResult(Intent result) {
        if (result != null) {
            setResult(RESULT_OK, result);
        } else {
            Intent own = new Intent();
            own.putExtra("password", LoginUtils.getInstance(this).getLoginData().password);
            setResult(RESULT_OK, own);
        }
    }
}
