package oversecured.ovaa.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class IntentVulnActivity extends Activity {
    private static final String CHANNEL = "ovaa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        Intent forwardIntent = intent.getParcelableExtra("forward_intent");
        Intent notificationIntent = intent.getParcelableExtra("notification_intent");
        Bundle extras = intent.getExtras();
        String uri = intent.getStringExtra("uri");
        String grantUri = intent.getStringExtra("grant_uri");
        String packageName = intent.getStringExtra("package");
        String className = intent.getStringExtra("class");
        String action = intent.getStringExtra("action");
        String fragmentClass = intent.getStringExtra("fragment");
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        redirectParcelableIntent(forwardIntent);
        redirectParsedUri(uri);
        launchConfiguredComponent(packageName, className, action, extras);
        startConfiguredFragment(fragmentClass);
        grantViaExplicitUri(grantUri, packageName);
        sendMutablePendingIntent();
        notifyWithPendingIntent(notificationIntent, title, text);
        finish();
    }

    private void redirectParcelableIntent(Intent forward) {
        if (forward == null) {
            return;
        }
        startActivity(forward);
        sendBroadcast(forward);
        startService(forward);
    }

    private void redirectParsedUri(String uri) {
        if (uri == null) {
            return;
        }
        try {
            Intent parsed = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
            startActivity(parsed);
        } catch (Exception ignored) {
        }
    }

    private void launchConfiguredComponent(String packageName, String className, String action,
            Bundle extras) {

        if (packageName == null || className == null) {
            return;
        }
        Intent intent = new Intent(action);
        intent.setClassName(packageName, className);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void startConfiguredFragment(String fragmentClass) {
        if (fragmentClass == null) {
            return;
        }
        try {
            android.app.Fragment fragment = (android.app.Fragment)
                    Class.forName(fragmentClass).getDeclaredConstructor().newInstance();
            getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
        } catch (Exception ignored) {
        }
    }

    private void grantViaExplicitUri(String uri, String packageName) {
        if (uri == null || packageName == null) {
            return;
        }
        grantUriPermission(packageName, Uri.parse(uri),
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    private void sendMutablePendingIntent() {
        Intent empty = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, empty, PendingIntent.FLAG_MUTABLE);
        Intent carrier = new Intent("oversecured.ovaa.action.CALLBACK");
        carrier.putExtra("callback", pendingIntent);
        sendBroadcast(carrier);
    }

    private void notifyWithPendingIntent(Intent content, String title, String text) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(
                new NotificationChannel(CHANNEL, "ovaa", NotificationManager.IMPORTANCE_DEFAULT));

        if (content == null) {
            content = new Intent();
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, content, PendingIntent.FLAG_MUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);
    }
}
