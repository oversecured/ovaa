package oversecured.ovaa.objects;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

public class AutoLoadSerializable implements Serializable {
    private String path;
    private String uploadUrl;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (path == null || uploadUrl == null) {
            return;
        }
        byte[] content = FileUtils.readFileToByteArray(new File(path));
        HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(content);
        }
        connection.getResponseCode();
    }
}
