package oversecured.ovaa.activities;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileAccessActivity extends Activity {
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
        String outputPath = intent.getStringExtra("output_path");
        String name = intent.getStringExtra("name");
        String data = intent.getStringExtra("data");

        switch (action) {
            case "export":
                saveToDirectory(path, outputPath);
                break;
            case "write":
                writeToPath(outputPath, data);
                break;
            case "pad":
                appendPadding(outputPath);
                break;
            case "delete":
                removeFile(path);
                break;
            case "share_readable":
                shareReadable(path);
                break;
            case "share_writable":
                shareWritable(path);
                break;
            case "share_read_write":
                shareReadWrite(path);
                break;
            case "open_profile":
                openProfile(name);
                break;
            default:
                break;
        }
        finish();
    }

    private void saveToDirectory(String path, String outputPath) {
        if (path == null || outputPath == null) {
            return;
        }
        try {
            FileUtils.copyFile(new File(getFilesDir(), "credentials.xml"), new File(outputPath));
            FileUtils.copyDirectory(new File(path), new File(outputPath));
        } catch (IOException ignored) {
        }
    }

    private void writeToPath(String outputPath, String data) {
        if (outputPath == null || data == null) {
            return;
        }
        try (OutputStream out = new FileOutputStream(outputPath)) {
            out.write(data.getBytes());
        } catch (IOException ignored) {
        }
    }

    private void appendPadding(String outputPath) {
        if (outputPath == null) {
            return;
        }
        try (OutputStream out = new FileOutputStream(new File(getFilesDir(), outputPath), true)) {
            out.write(new byte[1024]);
        } catch (IOException ignored) {
        }
    }

    private void removeFile(String path) {
        if (path == null) {
            return;
        }
        new File(path).delete();
        FileUtils.deleteQuietly(new File(getFilesDir(), path));
    }

    private void shareReadable(String path) {
        if (path == null) {
            return;
        }
        new File(path).setReadable(true, false);
    }

    private void shareWritable(String path) {
        if (path == null) {
            return;
        }
        new File(path).setWritable(true, false);
    }

    private void shareReadWrite(String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        file.setReadable(true, false);
        file.setWritable(true, false);
    }

    private void openProfile(String name) {
        if (name == null) {
            return;
        }
        File file = new File(getFilesDir() + "/profiles/" + name);
        try (InputStream in = new java.io.FileInputStream(file)) {
            in.read(new byte[16]);
        } catch (IOException ignored) {
        }
    }
}
