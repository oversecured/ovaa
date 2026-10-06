package oversecured.ovaa.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceControlActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        String settingKey = intent.getStringExtra("key");
        String settingValue = intent.getStringExtra("value");
        String phone = intent.getStringExtra("phone");
        String packageName = intent.getStringExtra("package");

        changeDeviceSettings(settingKey, settingValue);
        setWallpaper(path);
        installPackage(path);
        callPhoneNumber(phone);
        killBackgroundProcesses(packageName);
        playMedia(path);
        recordMicrophone();
        startLocalServer();
        finish();
    }

    private void changeDeviceSettings(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        Settings.System.putString(getContentResolver(), key, value);
        Settings.Global.putString(getContentResolver(), key, value);
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
        startActivity(intent);
    }

    private void callPhoneNumber(String phone) {
        if (phone == null) {
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
    }

    private void killBackgroundProcesses(String packageName) {
        if (packageName == null) {
            return;
        }
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        manager.killBackgroundProcesses(packageName);
        manager.getRunningAppProcesses();
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

    private void recordMicrophone() {
        MediaRecorder recorder = new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(new File(getExternalCacheDir(), "record.m4a").getAbsolutePath());
            recorder.prepare();
            recorder.start();
        } catch (Exception ignored) {
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
