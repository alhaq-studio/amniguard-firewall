package com.alhaq.amnishield.netblock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FragmentDomains extends Fragment {
    private static final String TAG = "AmniGuard.FragmentDomains";

    private SwitchCompat swDomainMaster;
    private SwitchCompat swBlockAdult;
    private SwitchCompat swBlockSocial;
    private TextView tvMasterStatus;
    private TextView tvBlacklistCount;
    private TextView tvWhitelistCount;
    private MaterialButton btnManageBlacklist;
    private MaterialButton btnManageWhitelist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_domains, container, false);

        swDomainMaster = view.findViewById(R.id.swDomainMaster);
        swBlockAdult = view.findViewById(R.id.swBlockAdult);
        swBlockSocial = view.findViewById(R.id.swBlockSocial);
        tvMasterStatus = view.findViewById(R.id.tvMasterStatus);
        tvBlacklistCount = view.findViewById(R.id.tvBlacklistCount);
        tvWhitelistCount = view.findViewById(R.id.tvWhitelistCount);
        btnManageBlacklist = view.findViewById(R.id.btnManageBlacklist);
        btnManageWhitelist = view.findViewById(R.id.btnManageWhitelist);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        boolean masterEnabled = prefs.getBoolean("domain_blocker_enabled", true);
        boolean blockAdult = prefs.getBoolean("block_adult", true);
        boolean blockSocial = prefs.getBoolean("block_social", false);

        swDomainMaster.setChecked(masterEnabled);
        swBlockAdult.setChecked(blockAdult);
        swBlockSocial.setChecked(blockSocial);

        updateMasterUI(masterEnabled);
        updateCounts();

        swDomainMaster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("domain_blocker_enabled", isChecked).apply();
                updateMasterUI(isChecked);
                ServiceSinkhole.reload("changed domain_blocker_enabled", requireContext(), false);
            }
        });

        swBlockAdult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("block_adult", isChecked).apply();
                ServiceSinkhole.reload("changed block_adult", requireContext(), false);
            }
        });

        swBlockSocial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("block_social", isChecked).apply();
                ServiceSinkhole.reload("changed block_social", requireContext(), false);
            }
        });

        btnManageBlacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!IAB.isPurchased(ActivityPro.SKU_FILTER, requireContext())) {
                    showProFeatureDialog();
                    return;
                }
                showDomainManagementDialog(R.string.title_manage_blacklist, "custom_blocked_domains");
            }
        });

        btnManageWhitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDomainManagementDialog(R.string.title_manage_whitelist, "custom_allowed_domains");
            }
        });

        return view;
    }

    private void showProFeatureDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_pro)
                .setMessage(R.string.msg_pro_blacklist_feature)
                .setPositiveButton(R.string.title_pro_buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(requireContext(), ActivityPro.class));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateMasterUI(boolean enabled) {
        if (tvMasterStatus != null) {
            tvMasterStatus.setText(enabled ? R.string.badge_active : R.string.badge_disabled);
            tvMasterStatus.setTextColor(getResources().getColor(enabled ? R.color.colorSuccess : R.color.colorError));
        }
        if (swBlockAdult != null) swBlockAdult.setEnabled(enabled);
        if (swBlockSocial != null) swBlockSocial.setEnabled(enabled);
    }

    private void updateCounts() {
        if (getContext() == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Set<String> blacklist = prefs.getStringSet("custom_blocked_domains", null);
        Set<String> whitelist = prefs.getStringSet("custom_allowed_domains", null);

        int blackCount = (blacklist != null) ? blacklist.size() : 0;
        int whiteCount = (whitelist != null) ? whitelist.size() : 0;

        if (tvBlacklistCount != null) {
            tvBlacklistCount.setText(blackCount == 0 ?
                    getString(R.string.msg_no_domains_configured) :
                    blackCount + " domain" + (blackCount == 1 ? "" : "s") + " configured to block");
        }

        if (tvWhitelistCount != null) {
            tvWhitelistCount.setText(whiteCount == 0 ?
                    getString(R.string.msg_no_domains_configured) :
                    whiteCount + " domain" + (whiteCount == 1 ? "" : "s") + " excluded from filters");
        }
    }

    private void showDomainManagementDialog(final int titleResId, final String prefKey) {
        final Context context = requireContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final Set<String> existing = prefs.getStringSet(prefKey, new HashSet<String>());
        final List<String> domainList = new ArrayList<>(existing);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, domainList);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResId);

        if (domainList.isEmpty()) {
            builder.setMessage(R.string.msg_no_domains_configured);
        } else {
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    final String selected = domainList.get(which);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.title_remove_domain)
                            .setMessage(selected)
                            .setPositiveButton(R.string.title_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int w) {
                                    Set<String> updated = new HashSet<>(prefs.getStringSet(prefKey, new HashSet<String>()));
                                    updated.remove(selected);
                                    prefs.edit().putStringSet(prefKey, updated).apply();
                                    Toast.makeText(context, getString(R.string.msg_domain_removed, selected), Toast.LENGTH_SHORT).show();
                                    ServiceSinkhole.reload("custom_domain_removed", context, false);
                                    updateCounts();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            });
        }

        builder.setPositiveButton(R.string.btn_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ("custom_blocked_domains".equals(prefKey) && !IAB.isPurchased(ActivityPro.SKU_FILTER, context)) {
                    showProFeatureDialog();
                    return;
                }
                showAddDomainDialog(titleResId, prefKey);
            }
        });

        builder.setNegativeButton(R.string.btn_close, null);
        builder.show();
    }

    private void showAddDomainDialog(final int parentTitleResId, final String prefKey) {
        final Context context = requireContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final EditText input = new EditText(context);
        input.setHint(R.string.hint_enter_domain);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) (20 * getResources().getDisplayMetrics().density);
        params.rightMargin = (int) (20 * getResources().getDisplayMetrics().density);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(context)
                .setTitle(R.string.title_add_domain)
                .setView(container)
                .setPositiveButton(R.string.btn_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String domain = input.getText().toString().trim().toLowerCase();
                        if (domain.isEmpty() || !domain.contains(".")) {
                            Toast.makeText(context, R.string.msg_invalid_domain, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (domain.startsWith("http://")) domain = domain.substring(7);
                        if (domain.startsWith("https://")) domain = domain.substring(8);
                        if (domain.contains("/")) domain = domain.substring(0, domain.indexOf('/'));

                        Set<String> updated = new HashSet<>(prefs.getStringSet(prefKey, new HashSet<String>()));
                        updated.add(domain);
                        prefs.edit().putStringSet(prefKey, updated).apply();
                        Toast.makeText(context, getString(R.string.msg_domain_added, domain), Toast.LENGTH_SHORT).show();
                        ServiceSinkhole.reload("custom_domain_added", context, false);
                        updateCounts();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCounts();
    }
}
