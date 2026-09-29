package oversecured.ovaa.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class InsecureLoggerService extends IntentService {
    private static final String ACTION_DUMP = "oversecured.ovaa.action.DUMP";
    private static final String EXTRA_FILE = "oversecured.ovaa.extra.file";

    public InsecureLoggerService() {
        super("InsecureLoggerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_DUMP.equals(intent.getAction())) {
            File dumpFile = getDumpFile(intent);
            if (dumpFile != null) {
                dumpLogs(dumpFile);
            }
        }
    }

    private File getDumpFile(Intent intent) {
        Bundle extras = intent.getExtras();
        Object file = extras == null ? null : extras.get(EXTRA_FILE);
        if (file instanceof String) {
            return new File((String) file);
        } else if (file instanceof File) {
            return (File) file;
        }
        return null;
    }

    private void dumpLogs(File toFile) {
        try {
            Process logcatProcess = Runtime.getRuntime().exec("logcat -d");
            BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(toFile));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                writer.append(line).append('\n');
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
