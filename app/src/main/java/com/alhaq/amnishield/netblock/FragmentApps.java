package com.alhaq.amnishield.netblock;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class FragmentApps extends Fragment {
    private static final String TAG = "AmniGuard.FragmentApps";

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvApplication;
    private AdapterRule adapter = null;
    private TextView tvDisabled;
    private TextView tvNotifications;
    private View vwPopupAnchor;

    private Chip chipAll;
    private Chip chipNoInternet;
    private Chip chipDisabled;
    private Chip chipUser;
    private Chip chipSystem;
    private MaterialButton btnSort;

    public static final int REQUEST_NOTIFICATIONS = 4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);

        tvDisabled = view.findViewById(R.id.tvDisabled);
        tvNotifications = view.findViewById(R.id.tvNotifications);
        rvApplication = view.findViewById(R.id.rvApplication);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        vwPopupAnchor = view.findViewById(R.id.vwPopupAnchor);

        chipAll = view.findViewById(R.id.chipAll);
        chipNoInternet = view.findViewById(R.id.chipNoInternet);
        chipDisabled = view.findViewById(R.id.chipDisabled);
        chipUser = view.findViewById(R.id.chipUser);
        chipSystem = view.findViewById(R.id.chipSystem);
        btnSort = view.findViewById(R.id.btnSort);

        rvApplication.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        rvApplication.setLayoutManager(llm);

        adapter = new AdapterRule(requireActivity(), vwPopupAnchor);
        rvApplication.setAdapter(adapter);

        TypedValue tv = new TypedValue();
        requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        swipeRefresh.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE);
        swipeRefresh.setProgressBackgroundColorSchemeColor(tv.data);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Rule.clearCache(requireContext());
                ServiceSinkhole.reload("pull", requireContext(), false);
                if (getActivity() instanceof ActivityMain) {
                    ((ActivityMain) getActivity()).updateApplicationList(null);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            tvNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        getActivity().requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
                    }
                }
            });
        }

        setupFilterSortBar();
        updateState();
        return view;
    }

    private void setupFilterSortBar() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        chipAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit()
                        .putBoolean("show_nointernet", true)
                        .putBoolean("show_disabled", true)
                        .putBoolean("show_user", true)
                        .putBoolean("show_system", false)
                        .apply();
                syncFilterChips();
                notifyRulesRefresh();
            }
        });

        chipNoInternet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    prefs.edit().putBoolean("show_nointernet", isChecked).apply();
                    syncFilterChips();
                    notifyRulesRefresh();
                }
            }
        });

        chipDisabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    prefs.edit().putBoolean("show_disabled", isChecked).apply();
                    syncFilterChips();
                    notifyRulesRefresh();
                }
            }
        });

        chipUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    prefs.edit().putBoolean("show_user", isChecked).apply();
                    syncFilterChips();
                    notifyRulesRefresh();
                }
            }
        });

        chipSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    prefs.edit().putBoolean("show_system", isChecked).apply();
                    syncFilterChips();
                    notifyRulesRefresh();
                }
            }
        });

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v);
            }
        });

        syncFilterChips();
    }

    private void showSortMenu(View anchor) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        final String currentSort = prefs.getString("sort", "name");

        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, R.string.menu_sort_name).setCheckable(true).setChecked("name".equals(currentSort));
        popup.getMenu().add(0, 2, 1, R.string.menu_sort_uid).setCheckable(true).setChecked("uid".equals(currentSort));

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) {
                    prefs.edit().putString("sort", "name").apply();
                } else if (item.getItemId() == 2) {
                    prefs.edit().putString("sort", "uid").apply();
                }
                syncSortButtonText();
                notifyRulesRefresh();
                return true;
            }
        });
        popup.show();
    }

    private void syncFilterChips() {
        if (getContext() == null || chipAll == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        boolean showNoInternet = prefs.getBoolean("show_nointernet", true);
        boolean showDisabled = prefs.getBoolean("show_disabled", true);
        boolean showUser = prefs.getBoolean("show_user", true);
        boolean showSystem = prefs.getBoolean("show_system", false);
        boolean manageSystem = prefs.getBoolean("manage_system", false);

        chipNoInternet.setChecked(showNoInternet);
        chipDisabled.setChecked(showDisabled);

        if (manageSystem) {
            chipUser.setVisibility(View.VISIBLE);
            chipSystem.setVisibility(View.VISIBLE);
            chipUser.setChecked(showUser);
            chipSystem.setChecked(showSystem);
        } else {
            chipUser.setVisibility(View.GONE);
            chipSystem.setVisibility(View.GONE);
        }

        boolean isDefaultAll = showNoInternet && showDisabled && (!manageSystem || (showUser && !showSystem));
        chipAll.setChecked(isDefaultAll);

        syncSortButtonText();
    }

    private void syncSortButtonText() {
        if (getContext() == null || btnSort == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String sort = prefs.getString("sort", "name");
        btnSort.setText("uid".equals(sort) ? R.string.menu_sort_uid : R.string.btn_sort);
    }

    private void notifyRulesRefresh() {
        if (getActivity() instanceof ActivityMain) {
            ((ActivityMain) getActivity()).updateApplicationList(null);
        }
    }

    public void updateState() {
        if (getContext() == null || tvDisabled == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean enabled = prefs.getBoolean("enabled", false);
        tvDisabled.setVisibility(enabled ? View.GONE : View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && tvNotifications != null) {
            boolean hasPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            tvNotifications.setVisibility(hasPermission ? View.GONE : View.VISIBLE);
        }

        syncFilterChips();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
        if (getActivity() instanceof ActivityMain) {
            ((ActivityMain) getActivity()).updateApplicationList(null);
        }
    }

    public AdapterRule getAdapter() {
        return adapter;
    }

    public SwipeRefreshLayout getSwipeRefresh() {
        return swipeRefresh;
    }
}
