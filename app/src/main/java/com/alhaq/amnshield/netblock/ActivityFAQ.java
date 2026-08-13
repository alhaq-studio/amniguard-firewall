package com.alhaq.amnshield.netblock;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityFAQ extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        
        addFAQItem(layout, R.string.faq_q1, R.string.faq_a1);
        addFAQItem(layout, R.string.faq_q2, R.string.faq_a2);
        addFAQItem(layout, R.string.faq_q3, R.string.faq_a3);
        addFAQItem(layout, R.string.faq_q4, R.string.faq_a4);
        addFAQItem(layout, R.string.faq_q5, R.string.faq_a5);
        addFAQItem(layout, R.string.faq_q6, R.string.faq_a6);
        addFAQItem(layout, R.string.faq_q7, R.string.faq_a7);
        addFAQItem(layout, R.string.faq_q8, R.string.faq_a8);
        addFAQItem(layout, R.string.faq_q9, R.string.faq_a9);
        addFAQItem(layout, R.string.faq_q10, R.string.faq_a10);
        addFAQItem(layout, R.string.faq_q11, R.string.faq_a11);
        addFAQItem(layout, R.string.faq_q12, R.string.faq_a12);
        
        setContentView(scrollView);
        setTitle(R.string.faq_title);
    }
    
    private void addFAQItem(LinearLayout parent, int questionRes, int answerRes) {
        TextView question = new TextView(this);
        question.setText(questionRes);
        question.setTextSize(14);
        question.setTypeface(null, android.graphics.Typeface.BOLD);
        question.setPadding(0, 12, 0, 4);
        parent.addView(question);
        
        TextView answer = new TextView(this);
        answer.setText(answerRes);
        answer.setTextSize(13);
        answer.setPadding(8, 0, 0, 12);
        parent.addView(answer);
    }
}
