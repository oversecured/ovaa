package oversecured.ovaa.nav;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

// Dispatches navigation handoffs (links, screens, sections, grants, notifications) from the deeplink router and the post-login flow
public class NavigationManager {
    private static final String CHANNEL = "ovaa";
    private final Activity activity;

    public NavigationManager(Activity activity) {
        this.activity = activity;
    }

    public void resumeSavedTarget(Intent target) {
        if (target == null) {
            return;
        }
        try {
            activity.startActivity(target);
        } catch (ActivityNotFoundException ignored) {
        }
        activity.sendBroadcast(target);
        // Implicit service intents throw IllegalArgumentException for targetSdk 21+
        try {
            activity.startService(target);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void openEncodedLink(String uri) {
        if (uri == null) {
            return;
        }
        try {
            activity.startActivity(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME));
        } catch (Exception ignored) {
        }
    }

    public void openScreen(String packageName, String className, String action, Bundle args) {
        if (packageName == null || className == null) {
            return;
        }
        Intent intent = new Intent(action);
        intent.setClassName(packageName, className);
        if (args != null) {
            intent.putExtras(args);
        }
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public void showSection(String fragmentClass) {
        if (fragmentClass == null) {
            return;
        }
        try {
            android.app.Fragment fragment = (android.app.Fragment)
                    Class.forName(fragmentClass).getDeclaredConstructor().newInstance();
            activity.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        } catch (Exception ignored) {
        }
    }

    public void shareDocumentAccess(String uri, String packageName) {
        if (uri == null || packageName == null) {
            return;
        }
        activity.grantUriPermission(packageName, Uri.parse(uri),
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public void registerResultListener() {
        PendingIntent callback = PendingIntent.getActivity(activity, 0, new Intent(), mutableFlags());
        Intent carrier = new Intent("oversecured.ovaa.action.CALLBACK");
        carrier.putExtra("callback", callback);
        activity.sendBroadcast(carrier);
    }

    public void postResumeNotification(Intent target, String title, String text) {
        NotificationManager manager = activity.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(
                new NotificationChannel(CHANNEL, "ovaa", NotificationManager.IMPORTANCE_DEFAULT));
        if (target == null) {
            target = new Intent();
        }
        PendingIntent content = PendingIntent.getActivity(activity, 0, target, mutableFlags());
        Notification notification = new Notification.Builder(activity, CHANNEL)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(content)
                .build();
        manager.notify(1, notification);
    }

    private int mutableFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT;
        }
        return PendingIntent.FLAG_MUTABLE;
    }
}
