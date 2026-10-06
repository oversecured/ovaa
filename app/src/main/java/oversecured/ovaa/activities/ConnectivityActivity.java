package oversecured.ovaa.activities;

import android.content.Intent;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import oversecured.ovaa.utils.LoginUtils;

public class ConnectivityActivity extends Activity {
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String CHANNEL = "ovaa_connectivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        String payload = intent.getStringExtra("payload");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int notificationId = intent.getIntExtra("id", 2);
        int cancelId = intent.getIntExtra("cancel_id", -1);
        boolean cancelAll = intent.getBooleanExtra("cancel_all", false);
        String secret = LoginUtils.getInstance(this).getLoginData().password;

        sendOverBluetooth(path, payload, secret);
        sendOverNfc(tag, path, payload, secret);
        postNotification(title, text, notificationId);
        hideNotifications(cancelId, cancelAll);
        finish();
    }

    private byte[] payloadFor(String path, String payload, String secret) {
        if (path != null) {
            try {
                return FileUtils.readFileToByteArray(new File(path));
            } catch (IOException ignored) {
            }
        }
        return (payload != null ? payload : secret).getBytes();
    }

    private void sendOverBluetooth(String path, String payload, String secret) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
                socket.connect();
                try (OutputStream out = socket.getOutputStream()) {
                    out.write(payloadFor(path, payload, secret));
                }
                socket.close();
            } catch (IOException ignored) {
            }
            return;
        }
    }

    private void sendOverNfc(Tag tag, String path, String payload, String secret) {
        if (tag == null) {
            return;
        }
        NdefRecord record = NdefRecord.createMime("application/octet-stream",
                payloadFor(path, payload, secret));
        Ndef ndef = Ndef.get(tag);
        try {
            ndef.connect();
            ndef.writeNdefMessage(new NdefMessage(new NdefRecord[] {record}));
            ndef.close();
        } catch (IOException | FormatException ignored) {
        }
    }

    private void postNotification(String title, String text, int notificationId) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(
                new NotificationChannel(CHANNEL, "ovaa", NotificationManager.IMPORTANCE_HIGH));
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        manager.notify(notificationId, notification);
    }

    private void hideNotifications(int cancelId, boolean cancelAll) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (cancelId >= 0) {
            manager.cancel(cancelId);
        }
        if (cancelAll) {
            manager.cancelAll();
        }
    }
}
