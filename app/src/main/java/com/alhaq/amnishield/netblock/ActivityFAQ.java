package com.alhaq.amnishield.netblock;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class ActivityFAQ extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.faq_title);
        }

        LinearLayout container = findViewById(R.id.faq_container);

        addFAQItem(container, R.string.faq_q1, R.string.faq_a1);
        addFAQItem(container, R.string.faq_q2, R.string.faq_a2);
        addFAQItem(container, R.string.faq_q3, R.string.faq_a3);
        addFAQItem(container, R.string.faq_q4, R.string.faq_a4);
        addFAQItem(container, R.string.faq_q5, R.string.faq_a5);
        addFAQItem(container, R.string.faq_q6, R.string.faq_a6);
        addFAQItem(container, R.string.faq_q7, R.string.faq_a7);
        addFAQItem(container, R.string.faq_q8, R.string.faq_a8);
        addFAQItem(container, R.string.faq_q9, R.string.faq_a9);
        addFAQItem(container, R.string.faq_q10, R.string.faq_a10);
        addFAQItem(container, R.string.faq_q11, R.string.faq_a11);
        addFAQItem(container, R.string.faq_q12, R.string.faq_a12);
    }

    private void addFAQItem(LinearLayout parent, int questionRes, int answerRes) {
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

        TextView tvQuestion = new TextView(this);
        tvQuestion.setText(questionRes);
        tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvQuestion.setTypeface(null, android.graphics.Typeface.BOLD);
        TypedValue tvPrimary = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, tvPrimary, true);
        tvQuestion.setTextColor(tvPrimary.data);

        TextView tvAnswer = new TextView(this);
        tvAnswer.setText(answerRes);
        tvAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        tvAnswer.setLineSpacing(Util.dips2pixels(2, this), 1.15f);
        tvAnswer.setPadding(0, Util.dips2pixels(8, this), 0, 0);
        TypedValue tvOnSurface = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorSecondary, tvOnSurface, true);
        tvAnswer.setTextColor(tvOnSurface.data);

        contentLayout.addView(tvQuestion);
        contentLayout.addView(tvAnswer);
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
