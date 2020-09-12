package oversecured.ovaa.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import oversecured.ovaa.utils.LoginUtils;

public class DeeplinkActivity extends AppCompatActivity {
    private static final int URI_GRANT_CODE = 1003;

    private LoginUtils loginUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginUtils = LoginUtils.getInstance(this);
        Intent intent = getIntent();
        Uri uri;
        if(intent != null
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && (uri = intent.getData()) != null) {

            processDeeplink(uri);
        }
        finish();
    }

    private void processDeeplink(Uri uri) {
        if("oversecured".equals(uri.getScheme()) && "ovaa".equals(uri.getHost())) {
            String path = uri.getPath();
            if("/logout".equals(path)) {
                loginUtils.logout();
                startActivity(new Intent(this, EntranceActivity.class));
            }
            else if("/login".equals(path)) {
                String url = uri.getQueryParameter("url");
                if(url != null) {
                    loginUtils.setLoginUrl(url);
                }
                startActivity(new Intent(this, EntranceActivity.class));
            }
            else if("/grant_uri_permissions".equals(path)) {
                Intent i = new Intent("oversecured.ovaa.action.GRANT_PERMISSIONS");
                if(getPackageManager().resolveActivity(i, 0) != null) {
                    startActivityForResult(i, URI_GRANT_CODE);
                }
            }
            else if("/webview".equals(path)) {
                String url = uri.getQueryParameter("url");
                if(url != null) {
                    String host = Uri.parse(url).getHost();
                    if(host != null && host.endsWith("example.com")) {
                        Intent i = new Intent(this, WebViewActivity.class);
                        i.putExtra("url", url);
                        startActivity(i);
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1 && requestCode == URI_GRANT_CODE) {
            setResult(resultCode, data);
        }
    }
}
