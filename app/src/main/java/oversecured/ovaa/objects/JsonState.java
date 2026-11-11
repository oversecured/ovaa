package oversecured.ovaa.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonState implements Parcelable {
    public static final Parcelable.Creator<JsonState> CREATOR = new Parcelable.Creator<>() {
        @Override
        public JsonState[] newArray(int i) {
            return new JsonState[i];
        }

        @Override
        public JsonState createFromParcel(Parcel parcel) {
            return new JsonState(parcel);
        }
    };

    private static final Gson GSON = new GsonBuilder().create();

    public Object data;

    private JsonState(Parcel parcel) {
        try {
            Class clazz = Class.forName(parcel.readString());
            data = GSON.fromJson(parcel.readString(), clazz);
        } catch (ClassNotFoundException e) {
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
