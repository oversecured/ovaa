package oversecured.ovaa.objects;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class LoginData implements Serializable {
    public String email;
    public String password;

    public LoginData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @NonNull
    @Override
    public String toString() {
        return email + ":" + password;
    }
}
