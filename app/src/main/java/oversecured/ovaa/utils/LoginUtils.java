package oversecured.ovaa.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import oversecured.ovaa.R;
import oversecured.ovaa.objects.LoginData;

public class LoginUtils {
    private static final String EMAIL_KEY;
    private static final String PASSWORD_KEY;
    private static final String LOGIN_URL_KEY;

    private static LoginUtils utils;

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private LoginUtils(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("login_data", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static LoginUtils getInstance(Context context) {
        if(utils == null) {
            utils = new LoginUtils(context);
        }
        return utils;
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(preferences.getString(EMAIL_KEY, null));
    }

    public void saveCredentials(LoginData loginData) {
        editor.putString(EMAIL_KEY, loginData.email)
                .putString(PASSWORD_KEY, loginData.password)
                .commit();
    }

    public LoginData getLoginData() {
        return new LoginData(preferences.getString(EMAIL_KEY, null),
                preferences.getString(PASSWORD_KEY, null));
    }

    public void setLoginUrl(String url) {
        editor.putString(LOGIN_URL_KEY, url).commit();
    }

    public String getLoginUrl() {
        String url = preferences.getString(LOGIN_URL_KEY, null);
        if(TextUtils.isEmpty(url)) {
            url = context.getString(R.string.login_url);
            editor.putString(LOGIN_URL_KEY, url).commit();
        }
        return url;
    }

    public void logout() {
        editor.clear().commit();
    }
}
