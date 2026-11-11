package oversecured.ovaa.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import oversecured.ovaa.R;
import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.FileUtils;
import oversecured.ovaa.utils.IntentUtils;
import oversecured.ovaa.utils.LoginUtils;
import oversecured.ovaa.utils.TokenCipher;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_CART_SYNC = "oversecured.ovaa.action.CART_SYNC";

    private static final int PICK_CODE = 1001;
    private static final int PERMISSIONS_CODE = 1002;

    private static final String[][] CATALOG = {
            {"Wireless earbuds", "$59.99"},
            {"Cotton crew t-shirt", "$19.99"},
            {"Stainless water bottle", "$24.50"},
            {"Desk lamp", "$32.00"},
            {"Ceramic coffee mug", "$12.50"},
    };

    private LoginUtils loginUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginUtils = LoginUtils.getInstance(this);
        populateProducts();

        findViewById(R.id.sellItemButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Seller flow: pick a listing photo; onActivityResult copies it into the public media provider
                checkPermissions();
                Intent pickerIntent = new Intent(Intent.ACTION_PICK);
                pickerIntent.setType("image/*");
                startActivityForResult(pickerIntent, PICK_CODE);
            }
        });
        findViewById(R.id.syncCartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cart sync: broadcast the login payload for a CartSyncReceiver to carry the cart across devices
                Intent i = new Intent(ACTION_CART_SYNC);
                i.putExtra("payload", MainActivity.this.loginUtils.getLoginData());
                sendBroadcast(i);
            }
        });
        findViewById(R.id.dealsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the member-deals page already signed in by passing the session token
                LoginData loginData = MainActivity.this.loginUtils.getLoginData();
                String token = TokenCipher.encrypt(loginData.toString());
                Intent i = new Intent("oversecured.ovaa.action.WEBVIEW");
                i.putExtra("url", "http://example.com./deals?token=" + token);
                IntentUtils.protectActivityIntent(MainActivity.this, i);
                startActivity(i);
            }
        });
    }

    private void populateProducts() {
        LinearLayout list = findViewById(R.id.productList);
        for (String[] product : CATALOG) {
            TextView row = new TextView(this);
            row.setText(product[0] + "    " + product[1]);
            row.setTextSize(16);
            row.setPadding(0, 18, 0, 18);
            row.setGravity(Gravity.CENTER_VERTICAL);
            list.addView(row);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && data != null) {
            if (requestCode == PICK_CODE) {
                FileUtils.copyToPublicProvider(this, data.getData());
            }
        }
    }

    private void checkPermissions() {
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for(String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                }
            }
        }
    }
}
