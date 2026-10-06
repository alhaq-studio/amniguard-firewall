package com.alhaq.amnshield.netblock;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AmnShieldSyncService {
    private static final String TAG = "AmniGuard.SyncService";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    public static void startSync(final Context context) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();

        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    doSync(appContext);
                } catch (Throwable t) {
                    Log.w(TAG, "Failed to complete rules sync with AmnShield", t);
                }
            }
        });
    }

    private static void doSync(final Context context) {
        Log.i(TAG, "Starting rules synchronization with AmnShield main app...");

        final CountDownLatch latch = new CountDownLatch(1);
        final IAmnShieldApi[] apiHolder = new IAmnShieldApi[1];

        final ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    apiHolder[0] = IAmnShieldApi.Stub.asInterface(service);
                } catch (Exception e) {
                    Log.e(TAG, "Error obtaining IAmnShieldApi interface", e);
                }
                latch.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                apiHolder[0] = null;
            }
        };

        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName("com.alhaq.amnshield", "com.alhaq.amnshield.api.AmnShieldApiService"));

        boolean bound = false;
        try {
            bound = context.bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException se) {
            Log.w(TAG, "SecurityException binding to AmnShieldApiService: " + se.getMessage());
            return;
        } catch (Exception e) {
            Log.w(TAG, "Unable to bind to AmnShieldApiService: " + e.getMessage());
            return;
        }

        if (!bound) {
            Log.i(TAG, "AmnShieldApiService not available (AmnShield app not installed or service disabled).");
            return;
        }

        try {
            // Wait up to 3 seconds for binder connection
            if (latch.await(3, TimeUnit.SECONDS)) {
                IAmnShieldApi api = apiHolder[0];
                if (api != null) {
                    performSync(context, api);
                } else {
                    Log.w(TAG, "API stub is null after binding.");
                }
            } else {
                Log.w(TAG, "Binding to AmnShieldApiService timed out.");
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "Sync thread interrupted", e);
        } finally {
            try {
                context.unbindService(connection);
            } catch (Exception ignored) {
            }
        }
    }

    private static void performSync(Context context, IAmnShieldApi api) {
        try {
            Log.i(TAG, "Connected to AmnShield API v" + api.apiVersion());

            // 1. Query status map
            String statusJson = api.list("STATUS");
            if (statusJson == null) {
                Log.w(TAG, "Received null status from AmnShield. Authorization might be pending.");
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

            // 3. Update local SharedPreferences
            SharedPreferences wifi = context.getSharedPreferences("wifi", Context.MODE_PRIVATE);
            SharedPreferences other = context.getSharedPreferences("other", Context.MODE_PRIVATE);

            SharedPreferences.Editor wifiEditor = wifi.edit();
            SharedPreferences.Editor modifierOther = other.edit();

            java.util.List<Rule> currentRules = Rule.getRules(true, context);
            boolean ruleChanged = false;

            for (Rule rule : currentRules) {
                if (rule.packageName == null || rule.packageName.equals("root") || rule.packageName.equals("android.media")) {
                    continue;
                }

                boolean shouldBlock = packagesToBlock.contains(rule.packageName);

                if (rule.wifi_blocked != shouldBlock) {
                    wifiEditor.putBoolean(rule.packageName, shouldBlock);
                    ruleChanged = true;
                }
                if (rule.other_blocked != shouldBlock) {
                    modifierOther.putBoolean(rule.packageName, shouldBlock);
                    ruleChanged = true;
                }
            }

            // 4. Synchronize Website & Domain Blocker rules from AmnShield
            boolean blockAdultSync = status.optBoolean("block_adult", status.optBoolean("website_blocker_enabled", true));
            boolean blockSocialSync = status.optBoolean("block_social", focusActive);

            SharedPreferences defaultPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor defaultEditor = defaultPrefs.edit();
            boolean domainPrefChanged = false;

            if (defaultPrefs.getBoolean("block_adult", true) != blockAdultSync) {
                defaultEditor.putBoolean("block_adult", blockAdultSync);
                domainPrefChanged = true;
            }
            if (defaultPrefs.getBoolean("block_social", false) != blockSocialSync) {
                defaultEditor.putBoolean("block_social", blockSocialSync);
                domainPrefChanged = true;
            }

            String customWebsitesJson = api.list("CUSTOM_BLOCKED_WEBSITES");
            if (customWebsitesJson != null) {
                JSONArray webArray = new JSONArray(customWebsitesJson);
                Set<String> customDomains = new HashSet<>();
                for (int i = 0; i < webArray.length(); i++) {
                    String d = webArray.getString(i);
                    if (d != null && !d.trim().isEmpty()) {
                        customDomains.add(d.trim().toLowerCase());
                    }
                }
                Set<String> currentCustom = defaultPrefs.getStringSet("custom_blocked_domains", new HashSet<String>());
                if (!currentCustom.equals(customDomains)) {
                    defaultEditor.putStringSet("custom_blocked_domains", customDomains);
                    domainPrefChanged = true;
                }
            }

            if (domainPrefChanged) {
                defaultEditor.apply();
                ruleChanged = true;
            }

            if (ruleChanged) {
                wifiEditor.apply();
                modifierOther.apply();
                Log.i(TAG, "Rules updated in local SharedPreferences. Triggering VPN reload...");
                ServiceSinkhole.reload("AmnShield rules sync", context, false);
            } else {
                Log.i(TAG, "Rules are already in sync. No reload required.");
            }

        } catch (RemoteException e) {
            Log.w(TAG, "Binder transaction failed during sync", e);
        } catch (Exception e) {
            Log.w(TAG, "Exception during performSync", e);
        }
    }
}
