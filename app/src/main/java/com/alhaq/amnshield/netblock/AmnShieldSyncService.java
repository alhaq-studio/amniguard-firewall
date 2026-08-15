package com.alhaq.amnshield.netblock;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.alhaq.amnshield.api.IAmnShieldApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AmnShieldSyncService extends IntentService {
    private static final String TAG = "AmnGuard.SyncService";
    
    public AmnShieldSyncService() {
        super("AmnShieldSyncService");
    }
    
    public static void startSync(Context context) {
        Intent intent = new Intent(context, AmnShieldSyncService.class);
        context.startService(intent);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Starting rules synchronization with AmnShield main app...");
        
        final CountDownLatch latch = new CountDownLatch(1);
        final IAmnShieldApi[] apiHolder = new IAmnShieldApi[1];
        
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                apiHolder[0] = IAmnShieldApi.Stub.asInterface(service);
                latch.countDown();
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                apiHolder[0] = null;
            }
        };
        
        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName("com.alhaq.amnshield", "com.alhaq.amnshield.api.AmnShieldApiService"));
        
        boolean bound = bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.e(TAG, "Failed to bind to AmnShieldApiService. Main app might not be installed.");
            return;
        }
        
        try {
            // Wait up to 5 seconds for binder connection
            if (latch.await(5, TimeUnit.SECONDS)) {
                IAmnShieldApi api = apiHolder[0];
                if (api != null) {
                    performSync(api);
                } else {
                    Log.e(TAG, "API stub is null after binding.");
                }
            } else {
                Log.e(TAG, "Binding to AmnShieldApiService timed out.");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Sync thread interrupted", e);
        } finally {
            try {
                unbindService(connection);
            } catch (Exception e) {
                // Ignore if already unbound
            }
        }
    }
    
    private void performSync(IAmnShieldApi api) {
        try {
            Log.i(TAG, "Connected to AmnShield API v" + api.apiVersion());
            
            // 1. Query status map
            String statusJson = api.list("STATUS");
            if (statusJson == null) {
                Log.e(TAG, "Received null status from AmnShield. Authorization might be denied.");
                return;
            }
            
            JSONObject status = new JSONObject(statusJson);
            boolean appBlockerEnabled = status.optBoolean("app_blocker_enabled", false);
            boolean focusActive = status.optBoolean("focus_active", false);
            
            Log.i(TAG, "Sync status: appBlockerEnabled=" + appBlockerEnabled + ", focusActive=" + focusActive);
            
            // 2. Fetch blocked apps & focus groups packages
            Set<String> packagesToBlock = new HashSet<>();
            
            if (appBlockerEnabled) {
                String blockedAppsJson = api.list("APP_BLOCKER_GROUPS");
                if (blockedAppsJson != null) {
                    JSONArray array = new JSONArray(blockedAppsJson);
                    for (int i = 0; i < array.length(); i++) {
                        packagesToBlock.add(array.getString(i));
                    }
                }
            }
            
            if (focusActive) {
                String focusAppsJson = api.list("FOCUS_GROUPS");
                if (focusAppsJson != null) {
                    JSONArray array = new JSONArray(focusAppsJson);
                    for (int i = 0; i < array.length(); i++) {
                        packagesToBlock.add(array.getString(i));
                    }
                }
            }
            
            Log.i(TAG, "Packages determined to block: " + packagesToBlock);
            
            // 3. Update NetGuard SharedPreferences
            SharedPreferences wifi = getSharedPreferences("wifi", Context.MODE_PRIVATE);
            SharedPreferences other = getSharedPreferences("other", Context.MODE_PRIVATE);
            
            SharedPreferences.Editor wifiEditor = wifi.edit();
            SharedPreferences.Editor modifierOther = other.edit();
            
            java.util.List<Rule> currentRules = Rule.getRules(true, this);
            boolean ruleChanged = false;
            
            for (Rule rule : currentRules) {
                if (rule.packageName == null || rule.packageName.equals("root") || rule.packageName.equals("android.media")) {
                    continue;
                }
                
                boolean shouldBlock = packagesToBlock.contains(rule.packageName);
                
                // If it is supposed to be blocked, set it as blocked (true). Otherwise, allow it (false).
                if (rule.wifi_blocked != shouldBlock) {
                    wifiEditor.putBoolean(rule.packageName, shouldBlock);
                    ruleChanged = true;
                }
                if (rule.other_blocked != shouldBlock) {
                    modifierOther.putBoolean(rule.packageName, shouldBlock);
                    ruleChanged = true;
                }
            }
            
            if (ruleChanged) {
                wifiEditor.apply();
                modifierOther.apply();
                Log.i(TAG, "Rules updated in local SharedPreferences. Triggering VPN reload...");
                // Reload ServiceSinkhole immediately to apply new blocking rules
                ServiceSinkhole.reload("AmnShield rules sync", this, false);
            } else {
                Log.i(TAG, "Rules are already in sync. No reload required.");
            }
            
        } catch (RemoteException e) {
            Log.e(TAG, "Binder transaction failed during sync", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception during performSync", e);
        }
    }
}
