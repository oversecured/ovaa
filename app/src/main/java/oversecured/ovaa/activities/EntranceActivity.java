package oversecured.ovaa.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import oversecured.ovaa.utils.LoginUtils;

public class EntranceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(LoginUtils.getInstance(this).isLoggedIn()) {
            startActivity(new Intent("oversecured.ovaa.action.ACTIVITY_MAIN"));
        }
        else {
            startActivity(new Intent("oversecured.ovaa.action.LOGIN"));
        }
        finish();
    }
}
