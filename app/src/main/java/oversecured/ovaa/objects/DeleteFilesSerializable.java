package oversecured.ovaa.objects;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import oversecured.ovaa.utils.FileUtils;

public class DeleteFilesSerializable implements Serializable {
    private void readObject(ObjectInputStream in) throws IOException {
        File file = new File(in.readUTF());
        if(file.exists()) {
            FileUtils.deleteRecursive(file);
        }
    }
}
