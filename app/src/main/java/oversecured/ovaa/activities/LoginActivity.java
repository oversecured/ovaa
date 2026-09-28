package oversecured.ovaa.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;

import java.util.Objects;

import oversecured.ovaa.R;
import oversecured.ovaa.network.LoginService;
import oversecured.ovaa.network.RetrofitInstance;
import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.LoginUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    public static final String INTENT_REDIRECT_KEY = "redirect_intent";

    private LoginUtils loginUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUtils = LoginUtils.getInstance(this);
        if (loginUtils.isLoggedIn()) {
            onLoginFinished();
            return;
        }

        findViewById(R.id.loginButton).setOnClickListener(view -> {
            String email = ((TextView) findViewById(R.id.emailView)).getText().toString();
            String password = ((TextView) findViewById(R.id.passwordView)).getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Email is empty!", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Password is empty!", Toast.LENGTH_LONG).show();
                return;
            }
            processLogin(email, password);
        });
    }

    private void processLogin(String email, String password) {
        LoginData loginData = new LoginData(email, password);
        Log.d("ovaa", "Processing " + loginData);

        LoginService loginService = RetrofitInstance.getInstance().create(LoginService.class);
        loginService.login(loginUtils.getLoginUrl(), loginData).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
            }
        });

        loginUtils.saveCredentials(loginData);
        onLoginFinished();
    }

    private void onLoginFinished() {
        @SuppressLint("UnsafeIntentLaunch") Intent redirectIntent = IntentCompat.getParcelableExtra(getIntent(), INTENT_REDIRECT_KEY, Intent.class);
        startActivity(Objects.requireNonNullElseGet(redirectIntent, () -> new Intent(this, MainActivity.class)));
        finish();
    }
}
