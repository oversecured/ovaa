package oversecured.ovaa.objects;

import java.io.Serializable;

public class MemoryCorruptionSerializable implements Serializable {
    private static final long serialVersionUID = 0L;

    static {
        System.loadLibrary("ovaa");
    }

    private long ptr;

    private native void freePtr(long ptr);

    @Override
    protected void finalize() throws Throwable {
        if (ptr != 0) {
            freePtr(ptr);
            ptr = 0;
        }
    }
}
