package oversecured.ovaa.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import oversecured.ovaa.nav.NavigationManager;

public class QuickActionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String screen = intent.getStringExtra("screen");
        if (screen == null) {
            finish();
            return;
        }
        NavigationManager navigation = new NavigationManager(this);
        switch (screen) {
            case "resume":
                navigation.resumeSavedTarget(intent.getParcelableExtra("forward_intent"));
                break;
            case "open_link":
                navigation.openEncodedLink(intent.getStringExtra("uri"));
                break;
            case "open_screen":
                navigation.openScreen(intent.getStringExtra("package"), intent.getStringExtra("class"),
                        intent.getStringExtra("action"), intent.getExtras());
                break;
            case "section":
                navigation.showSection(intent.getStringExtra("fragment"));
                break;
            case "share":
                navigation.shareDocumentAccess(intent.getStringExtra("grant_uri"),
                        intent.getStringExtra("package"));
                break;
            case "listen":
                navigation.registerResultListener();
                break;
            case "notify":
                navigation.postResumeNotification(intent.getParcelableExtra("notification_intent"),
                        intent.getStringExtra("title"), intent.getStringExtra("text"));
                break;
            default:
                break;
        }
        finish();
    }
}
