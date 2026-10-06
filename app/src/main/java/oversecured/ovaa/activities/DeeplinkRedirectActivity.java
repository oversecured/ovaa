package oversecured.ovaa.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


public class DeeplinkRedirectActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri data = getIntent().getData();
        if (data == null) {
            finish();
            return;
        }
        if (!isTrustedCaller()) {
            finish();
            return;
        }
        redirect(data);
        finish();
    }

    private boolean isTrustedCaller() {
        if (getCallingActivity() == null) {
            return true;
        }
        String caller = getCallingActivity().getPackageName();
        return caller.startsWith("oversecured");
    }

    private void redirect(Uri data) {
        String target = data.getQueryParameter("next");
        if (target == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(target));
        startActivity(intent);

        Intent webView = new Intent(this, VulnerableWebViewActivity.class);
        webView.putExtra("url", target);
        startActivity(webView);
    }
}
