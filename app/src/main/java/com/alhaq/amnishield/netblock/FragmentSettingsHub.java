package com.alhaq.amnishield.netblock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class FragmentSettingsHub extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        MaterialCardView cardDefaults = view.findViewById(R.id.cardDefaults);
        MaterialCardView cardOptions = view.findViewById(R.id.cardOptions);
        MaterialCardView cardLogging = view.findViewById(R.id.cardLogging);
        MaterialCardView cardNetwork = view.findViewById(R.id.cardNetwork);
        MaterialCardView cardAdvanced = view.findViewById(R.id.cardAdvanced);
        MaterialCardView cardStats = view.findViewById(R.id.cardStats);
        MaterialCardView cardBackup = view.findViewById(R.id.cardBackup);
        MaterialCardView cardTechnical = view.findViewById(R.id.cardTechnical);
        MaterialButton btnAllSettings = view.findViewById(R.id.btnAllSettings);

        cardDefaults.setOnClickListener(v -> openSubscreen("screen_defaults"));
        cardOptions.setOnClickListener(v -> openSubscreen("screen_options"));
        cardLogging.setOnClickListener(v -> openSubscreen("screen_logging"));
        cardNetwork.setOnClickListener(v -> openSubscreen("screen_network_options"));
        cardAdvanced.setOnClickListener(v -> openSubscreen("screen_advanced_options"));
        cardStats.setOnClickListener(v -> openSubscreen("screen_stats"));
        cardBackup.setOnClickListener(v -> openSubscreen("screen_backup"));
        cardTechnical.setOnClickListener(v -> openSubscreen("screen_technical"));
        btnAllSettings.setOnClickListener(v -> openSubscreen(null));

        return view;
    }

    private void openSubscreen(String screenKey) {
        Intent intent = new Intent(requireContext(), ActivitySettings.class);
        if (screenKey != null) {
            intent.putExtra(ActivitySettings.EXTRA_SCREEN, screenKey);
        }
        startActivity(intent);
    }
}
