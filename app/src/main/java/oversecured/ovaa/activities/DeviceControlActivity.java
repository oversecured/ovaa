package oversecured.ovaa.activities;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceControlActivity extends Activity {
    private MediaRecorder recorder;
    private ParcelFileDescriptor recordOutput;

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
        String settingKey = intent.getStringExtra("key");
        String settingValue = intent.getStringExtra("value");
        String phone = intent.getStringExtra("phone");
        String recordDestination = intent.getStringExtra("record_output");

        boolean recording = false;
        switch (action) {
            case "settings":
                changeDeviceSettings(settingKey, settingValue);
                break;
            case "wallpaper":
                setWallpaper(path);
                break;
            case "install":
                installPackage(path);
                break;
            case "call":
                callPhoneNumber(phone);
                break;
            case "play":
                playMedia(path);
                break;
            case "record":
                recording = recordMicrophone(recordDestination);
                break;
            case "server":
                startLocalServer();
                break;
            default:
                break;
        }
        // stay in the foreground while recording so the mic is not muted
        if (!recording) {
            finish();
        }
    }

    private void changeDeviceSettings(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        try {
            Settings.System.putString(getContentResolver(), key, value);
            Settings.Global.putString(getContentResolver(), key, value);
        } catch (SecurityException ignored) {
        }
    }

    private void setWallpaper(String path) {
        if (path == null) {
            return;
        }
        try (InputStream in = getContentResolver().openInputStream(Uri.parse(path))) {
            WallpaperManager.getInstance(this).setStream(in);
        } catch (IOException ignored) {
        }
    }

    private void installPackage(String path) {
        if (path == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private void callPhoneNumber(String phone) {
        if (phone == null) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
        } catch (SecurityException | ActivityNotFoundException ignored) {
        }
    }

    private void playMedia(String path) {
        MediaPlayer player = new MediaPlayer();
        try {
            if (path != null) {
                player.setDataSource(path);
            } else {
                player.setDataSource(new File(Environment.getExternalStorageDirectory(),
                        "alert.mp3").getAbsolutePath());
            }
            player.prepare();
            player.start();
        } catch (IOException ignored) {
        }
    }

    private boolean recordMicrophone(String destination) {
        if (destination == null) {
            return false;
        }
        try {
            recordOutput = getContentResolver().openFileDescriptor(Uri.parse(destination), "w");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(recordOutput.getFileDescriptor());
            recorder.prepare();
            recorder.start();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void startLocalServer() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(8080)) {
                while (true) {
                    Socket socket = server.accept();
                    String requested = new java.io.BufferedReader(
                            new java.io.InputStreamReader(socket.getInputStream())).readLine();
                    if (requested != null) {
                        org.apache.commons.io.IOUtils.copy(
                                new java.io.FileInputStream(new File(getFilesDir(), requested)),
                                socket.getOutputStream());
                    }
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }).start();
    }
}
