package com.alhaq.amnishield.netblock;

/*
    This file is part of AmniGuard Firewall.

    AmniGuard Firewall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AmniGuard Firewall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AmniGuard Firewall.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2026 by Marcel Bokhorst (M66B) & Alhaq Initiative
*/

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ActivityPro extends AppCompatActivity {
    private static final String TAG = "AmniGuard.Pro";

    private IAB iab;

    public static final String SKU_LOG = "log";
    public static final String SKU_FILTER = "filter";
    public static final String SKU_NOTIFY = "notify";
    public static final String SKU_SPEED = "speed";
    public static final String SKU_THEME = "theme";
    public static final String SKU_PRO1 = "pro";
    public static final String SKU_SUPPORT1 = "support1";
    public static final String SKU_SUPPORT2 = "support2";
    public static final String SKU_DONATION = "donation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Create ActivityPro");
        Util.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pro);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_pro);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        updateState();

        final Button btnLog = findViewById(R.id.btnLog);
        final Button btnFilter = findViewById(R.id.btnFilter);
        final Button btnNotify = findViewById(R.id.btnNotify);
        final Button btnSpeed = findViewById(R.id.btnSpeed);
        final Button btnTheme = findViewById(R.id.btnTheme);
        final Button btnAll = findViewById(R.id.btnAll);
        final Button btnDev1 = findViewById(R.id.btnDev1);
        final Button btnDev2 = findViewById(R.id.btnDev2);

        View.OnClickListener buyListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iab == null || !iab.isConnected()) {
                    Toast.makeText(ActivityPro.this, "Connecting to Google Play...", Toast.LENGTH_SHORT).show();
                    if (iab != null) iab.bind();
                    return;
                }

                // Buy Lifetime Pro (Single one-time product unlocks everything)
                boolean started = iab.launchPurchaseFlow(ActivityPro.this, IAB.SKU_PRO);
                if (!started) {
                    Toast.makeText(ActivityPro.this, "Unable to initiate purchase. Please check Google Play connection.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnLog.setOnClickListener(buyListener);
        btnFilter.setOnClickListener(buyListener);
        btnNotify.setOnClickListener(buyListener);
        btnSpeed.setOnClickListener(buyListener);
        btnTheme.setOnClickListener(buyListener);
        btnAll.setOnClickListener(buyListener);
        btnDev1.setOnClickListener(buyListener);
        btnDev2.setOnClickListener(buyListener);

        iab = new IAB(new IAB.Delegate() {
            @Override
            public void onReady(final IAB iab) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "IAB is ready");
                        String price = iab.getFormattedPrice(IAB.SKU_PRO);
                        if (btnAll != null && price != null) {
                            btnAll.setText(getString(R.string.title_pro_buy) + " (" + price + ")");
                        }
                        updateState();
                    }
                });
            }

            @Override
            public void onPurchased(String productId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ActivityPro.this, "🎉 AmniGuard Pro Unlocked! Thank you for your support.", Toast.LENGTH_LONG).show();
                        updateState();
                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "IAB error: " + message);
                    }
                });
            }
        }, this);

        iab.bind();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Destroy ActivityPro");
        if (iab != null) {
            iab.unbind();
            iab = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_challenge:
                menu_challenge();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Util.isPlayStoreInstall(this)) {
            // Keep challenge for offline fallback or remove if desired
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void menu_challenge() {
        if (IAB.isPurchased(SKU_DONATION, this)) {
            Toast.makeText(this, getString(R.string.title_pro_already), Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.challenge, null, false);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String challenge = (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? Build.SERIAL : "O3" + android_id);
        String seed = (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? "AmniGuard Firewall2" : "AmniGuard Firewall3");

        TextView tvChallenge = view.findViewById(R.id.tvChallenge);
        tvChallenge.setText(challenge);

        ImageButton ibCopy = view.findViewById(R.id.ibCopy);
        ibCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.title_pro_challenge), challenge);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(ActivityPro.this, android.R.string.copy, Toast.LENGTH_LONG).show();
            }
        });

        final EditText etResponse = view.findViewById(R.id.etResponse);
        try {
            final String response = Util.md5(challenge, seed);
            etResponse.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    if (response.equalsIgnoreCase(editable.toString().trim())) {
                        IAB.setBought(SKU_DONATION, ActivityPro.this);
                        dialog.dismiss();
                        invalidateOptionsMenu();
                        updateState();
                        Toast.makeText(ActivityPro.this, "🎉 AmniGuard Pro Activated!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }

        ImageButton ibPaste = view.findViewById(R.id.ibPaste);
        ibPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null &&
                        clipboard.hasPrimaryClip() &&
                        clipboard.getPrimaryClipDescription() != null &&
                        clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    if (item != null && item.getText() != null) {
                        etResponse.setText(item.getText().toString());
                    }
                }
            }
        });

        dialog.show();
    }

    private void updateState() {
        Button btnLog = findViewById(R.id.btnLog);
        Button btnFilter = findViewById(R.id.btnFilter);
        Button btnNotify = findViewById(R.id.btnNotify);
        Button btnSpeed = findViewById(R.id.btnSpeed);
        Button btnTheme = findViewById(R.id.btnTheme);
        Button btnAll = findViewById(R.id.btnAll);
        Button btnDev1 = findViewById(R.id.btnDev1);
        Button btnDev2 = findViewById(R.id.btnDev2);

        TextView tvLog = findViewById(R.id.tvLog);
        TextView tvFilter = findViewById(R.id.tvFilter);
        TextView tvNotify = findViewById(R.id.tvNotify);
        TextView tvSpeed = findViewById(R.id.tvSpeed);
        TextView tvTheme = findViewById(R.id.tvTheme);
        TextView tvAll = findViewById(R.id.tvAll);
        TextView tvDev1 = findViewById(R.id.tvDev1);
        TextView tvDev2 = findViewById(R.id.tvDev2);

        TextView tvLogUnavailable = findViewById(R.id.tvLogUnavailable);
        TextView tvFilterUnavailable = findViewById(R.id.tvFilterUnavailable);

        boolean isPro = IAB.isPurchased(SKU_PRO1, this);
        boolean can = Util.canFilter(this);

        if (btnLog != null) btnLog.setVisibility(isPro || !can ? View.GONE : View.VISIBLE);
        if (btnFilter != null) btnFilter.setVisibility(isPro || !can ? View.GONE : View.VISIBLE);
        if (btnNotify != null) btnNotify.setVisibility(isPro ? View.GONE : View.VISIBLE);
        if (btnSpeed != null) btnSpeed.setVisibility(isPro ? View.GONE : View.VISIBLE);
        if (btnTheme != null) btnTheme.setVisibility(isPro ? View.GONE : View.VISIBLE);
        if (btnAll != null) btnAll.setVisibility(isPro ? View.GONE : View.VISIBLE);
        if (btnDev1 != null) btnDev1.setVisibility(isPro ? View.GONE : View.VISIBLE);
        if (btnDev2 != null) btnDev2.setVisibility(isPro ? View.GONE : View.VISIBLE);

        if (tvLog != null) tvLog.setVisibility(isPro && can ? View.VISIBLE : View.GONE);
        if (tvFilter != null) tvFilter.setVisibility(isPro && can ? View.VISIBLE : View.GONE);
        if (tvNotify != null) tvNotify.setVisibility(isPro ? View.VISIBLE : View.GONE);
        if (tvSpeed != null) tvSpeed.setVisibility(isPro ? View.VISIBLE : View.GONE);
        if (tvTheme != null) tvTheme.setVisibility(isPro ? View.VISIBLE : View.GONE);
        if (tvAll != null) tvAll.setVisibility(isPro ? View.VISIBLE : View.GONE);
        if (tvDev1 != null) tvDev1.setVisibility(isPro ? View.VISIBLE : View.GONE);
        if (tvDev2 != null) tvDev2.setVisibility(isPro ? View.VISIBLE : View.GONE);

        if (tvLogUnavailable != null) tvLogUnavailable.setVisibility(can ? View.GONE : View.VISIBLE);
        if (tvFilterUnavailable != null) tvFilterUnavailable.setVisibility(can ? View.GONE : View.VISIBLE);
    }
}
