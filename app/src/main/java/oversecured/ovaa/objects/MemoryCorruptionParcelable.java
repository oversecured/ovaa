package oversecured.ovaa.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MemoryCorruptionParcelable implements Parcelable {
    public static final Parcelable.Creator<MemoryCorruptionParcelable> CREATOR = new Parcelable.Creator<MemoryCorruptionParcelable>() {
        @Override
        public MemoryCorruptionParcelable[] newArray(int i) {
            return new MemoryCorruptionParcelable[i];
        }

        @Override
        public MemoryCorruptionParcelable createFromParcel(Parcel parcel) {
            return new MemoryCorruptionParcelable(parcel);
        }
    };

    private static final Gson GSON = new GsonBuilder().create();

    public Object data;

    private MemoryCorruptionParcelable(Parcel parcel) {
        try {
            Class clazz = Class.forName(parcel.readString());
            data = GSON.fromJson(parcel.readString(), clazz);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(data.getClass().getCanonicalName());
        parcel.writeString(GSON.toJson(data));
    }
}
