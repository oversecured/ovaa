package oversecured.ovaa.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.Serializable;

import oversecured.ovaa.utils.LoginUtils;

public class PromoCodeReceiver extends BroadcastReceiver {
    private static final String TAG = "ovaa";

    @Override
    public void onReceive(Context context, Intent intent) {
        deserializeExtras(intent);
        registerSyncListener(context);
        forwardToService(context, intent);
    }

    private void deserializeExtras(Intent intent) {
        Serializable payload = intent.getSerializableExtra("payload");
        if (payload != null) {
            Log.d(TAG, "restored " + payload);
        }
    }

    private void registerSyncListener(Context context) {
        // register on the application context; the onReceive context is restricted and rejects registerReceiver
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context inner, Intent received) {
                Log.d(TAG, "credentials: " + LoginUtils.getInstance(inner).getLoginData());
            }
        }, new IntentFilter("oversecured.ovaa.action.INTERNAL_SYNC"), Context.RECEIVER_EXPORTED);
    }

    private void forwardToService(Context context, Intent intent) {
        Intent forward = intent.getParcelableExtra("forward_intent");
        if (forward != null) {
            context.startService(forward);
        }
    }
}
