package oversecured.ovaa.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import oversecured.ovaa.R;
import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.FileUtils;
import oversecured.ovaa.utils.IntentUtils;
import oversecured.ovaa.utils.LoginUtils;
import oversecured.ovaa.utils.WeakCrypto;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_CODE = 1001;
    private static final int PERMISSIONS_CODE = 1002;

    private LoginUtils loginUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginUtils = LoginUtils.getInstance(this);
        findViewById(R.id.fileTheftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
                Intent pickerIntent = new Intent(Intent.ACTION_PICK);
                pickerIntent.setType("image/*");
                startActivityForResult(pickerIntent, PICK_CODE);
            }
        });
        findViewById(R.id.broadcastButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("oversecured.ovaa.action.UNPROTECTED_CREDENTIALS_DATA");
                i.putExtra("payload", MainActivity.this.loginUtils.getLoginData());
                sendBroadcast(i);
            }
        });
        findViewById(R.id.activityButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginData loginData = MainActivity.this.loginUtils.getLoginData();
                String token = WeakCrypto.encrypt(loginData.toString());
                Intent i = new Intent("oversecured.ovaa.action.WEBVIEW");
                i.putExtra("url", "http://example.com./?token=" + token);
                IntentUtils.protectActivityIntent(MainActivity.this, i);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1 && data != null) {
            if(requestCode == PICK_CODE) {
                FileUtils.copyToCache(this, data.getData());
            }
        }
    }

    private void checkPermissions() {
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for(String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSIONS_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if(requestCode == PERMISSIONS_CODE) {
            for(int grantResult : grantResults) {
                if(grantResult != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                }
            }
        }
    }
}
