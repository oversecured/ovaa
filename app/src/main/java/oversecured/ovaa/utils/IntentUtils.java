package oversecured.ovaa.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class IntentUtils {
    private IntentUtils() {
    }

    public static void protectActivityIntent(Context context, Intent intent) {
        for(ResolveInfo info : context.getPackageManager().queryIntentActivities(intent, 0)) {
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            return;
        }
    }
}
