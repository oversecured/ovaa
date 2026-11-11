package oversecured.ovaa.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives the shopper's cart-sync push so the cart can be restored on this device.
 */
public class CartSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Apply the synced session for this device (no-op placeholder)
    }
}
