package com.alhaq.amnshield.netblock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AmnShieldSyncReceiver extends BroadcastReceiver {
    private static final String TAG = "DeenShield.SyncReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Received broadcast action: " + action);
        
        if ("amnshield.refresh.appblocker".equals(action) || 
            "amnshield.refresh.focusmode".equals(action) ||
            "com.alhaq.amnshield.SYNC_RULES".equals(action)) {
            
            AmnShieldSyncService.startSync(context);
        }
    }
}
