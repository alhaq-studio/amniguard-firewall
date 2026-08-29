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

    Copyright 2015-2026 by Alhaq Initiative
*/

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityPro extends AppCompatActivity {
    private static final String TAG = "AmniGuard.Pro";
    private static final String URL_WEBSITE_PRO = "https://alhaq.uk/amniguard";
    private static final String URL_GITHUB_REPO = "https://github.com/alhaq/AmniGuard-FireWall";

    public static final String SKU_LOG = "log";
    public static final String SKU_FILTER = "filter";
    public static final String SKU_NOTIFY = "notify";
    public static final String SKU_SPEED = "speed";
    public static final String SKU_THEME = "theme";
    public static final String SKU_PRO1 = "pro";
    public static final String SKU_SUPPORT1 = "support1";
    public static final String SKU_SUPPORT2 = "support2";
    public static final String SKU_DONATION = "donation";

    private ImageView ivStatusIcon;
    private TextView tvStatusBadge;
    private TextView tvLicenseDetails;
    private Button btnRemoveLicense;
    private EditText etLicenseKey;
    private ImageButton ibPasteKey;
    private Button btnActivateLicense;
    private Button btnGetLicense;
    private Button btnGitHub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Create ActivityPro");
        Util.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pro);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvLicenseDetails = findViewById(R.id.tvLicenseDetails);
        btnRemoveLicense = findViewById(R.id.btnRemoveLicense);
        etLicenseKey = findViewById(R.id.etLicenseKey);
        ibPasteKey = findViewById(R.id.ibPasteKey);
        btnActivateLicense = findViewById(R.id.btnActivateLicense);
        btnGetLicense = findViewById(R.id.btnGetLicense);
        btnGitHub = findViewById(R.id.btnGitHub);

        // Paste Key Listener
        if (ibPasteKey != null) {
            ibPasteKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                        CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
                        if (text != null && etLicenseKey != null) {
                            etLicenseKey.setText(text.toString().trim());
                            Toast.makeText(ActivityPro.this, "License key pasted from clipboard", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        // Activate Key Listener
        if (btnActivateLicense != null) {
            btnActivateLicense.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etLicenseKey == null) return;
                    String key = etLicenseKey.getText().toString().trim();
                    if (key.isEmpty()) {
                        Toast.makeText(ActivityPro.this, "Please enter a valid ECDSA license key", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = LicenseValidator.saveLicense(ActivityPro.this, key);
                    if (success) {
                        etLicenseKey.setText("");
                        updateLicenseUI();
                        Toast.makeText(ActivityPro.this, "[SUCCESS] AmniGuard Pro Activated Successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ActivityPro.this, "[ERROR] Invalid or Expired ECDSA License Key", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // Deactivate Key Listener
        if (btnRemoveLicense != null) {
            btnRemoveLicense.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LicenseValidator.clearLicense(ActivityPro.this);
                    updateLicenseUI();
                    Toast.makeText(ActivityPro.this, "License removed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // External Links
        if (btnGetLicense != null) {
            btnGetLicense.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_WEBSITE_PRO));
                    startActivity(browserIntent);
                }
            });
        }

        if (btnGitHub != null) {
            btnGitHub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GITHUB_REPO));
                    startActivity(browserIntent);
                }
            });
        }

        updateLicenseUI();
    }

    private void updateLicenseUI() {
        LicensePayload payload = LicenseValidator.getActiveLicense(this);
        boolean isPro = payload != null && !payload.isExpired();

        if (isPro) {
            if (ivStatusIcon != null) {
                ivStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorSuccess));
            }
            if (tvStatusBadge != null) {
                tvStatusBadge.setText("Active — Pro Supporter Pass");
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess));
            }
            if (tvLicenseDetails != null) {
                String expiryText = payload.isLifetime() ? "Lifetime Access" :
                        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(payload.getExpires()));
                String text = "Licensed to: " + payload.getEmail() + "\nType: " + payload.getType().toUpperCase(Locale.ROOT) + "\nValid Until: " + expiryText;
                tvLicenseDetails.setText(text);
            }
            if (btnRemoveLicense != null) {
                btnRemoveLicense.setVisibility(View.VISIBLE);
            }
        } else {
            if (ivStatusIcon != null) {
                ivStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorGrayed));
            }
            if (tvStatusBadge != null) {
                tvStatusBadge.setText("Free & Open-Source Core");
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.colorGrayed));
            }
            if (tvLicenseDetails != null) {
                tvLicenseDetails.setText("No active supporter license key found. Core filtering and firewall protection remain 100% free and open-source forever.");
            }
            if (btnRemoveLicense != null) {
                btnRemoveLicense.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
