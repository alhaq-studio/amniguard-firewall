package com.alhaq.amnishield.netblock;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class ActivityHelp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.help_title);
        }

        LinearLayout container = findViewById(R.id.help_container);

        addSection(container, R.string.help_section1_title, R.string.help_section1_content);
        addSection(container, R.string.help_section2_title, R.string.help_section2_content);
        addSection(container, R.string.help_section3_title, R.string.help_section3_content);
        addSection(container, R.string.help_section4_title, R.string.help_section4_content);
        addSection(container, R.string.help_section5_title, R.string.help_section5_content);
        addSection(container, R.string.help_section6_title, R.string.help_section6_content);
    }

    private void addSection(LinearLayout parent, int titleRes, int contentRes) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, Util.dips2pixels(12, this));
        card.setLayoutParams(cardParams);
        card.setRadius(Util.dips2pixels(14, this));
        card.setCardElevation(Util.dips2pixels(2, this));
        card.setStrokeWidth(Util.dips2pixels(1, this));

        TypedValue tvOutline = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorOutlineVariant, tvOutline, true);
        if (tvOutline.data != 0) {
            card.setStrokeColor(tvOutline.data);
        }

        TypedValue tvSurface = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorSurface, tvSurface, true);
        if (tvSurface.data != 0) {
            card.setCardBackgroundColor(tvSurface.data);
        }

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        int pad = Util.dips2pixels(16, this);
        contentLayout.setPadding(pad, pad, pad, pad);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(titleRes);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        TypedValue tvPrimary = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, tvPrimary, true);
        tvTitle.setTextColor(tvPrimary.data);

        TextView tvContent = new TextView(this);
        tvContent.setText(contentRes);
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        tvContent.setLineSpacing(Util.dips2pixels(2, this), 1.15f);
        tvContent.setPadding(0, Util.dips2pixels(8, this), 0, 0);
        TypedValue tvOnSurface = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorSecondary, tvOnSurface, true);
        tvContent.setTextColor(tvOnSurface.data);

        contentLayout.addView(tvTitle);
        contentLayout.addView(tvContent);
        card.addView(contentLayout);
        parent.addView(card);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
