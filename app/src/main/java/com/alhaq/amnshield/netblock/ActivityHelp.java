package com.alhaq.amnshield.netblock;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityHelp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        
        addSection(layout, R.string.help_section1_title, R.string.help_section1_content);
        addSection(layout, R.string.help_section2_title, R.string.help_section2_content);
        addSection(layout, R.string.help_section3_title, R.string.help_section3_content);
        addSection(layout, R.string.help_section4_title, R.string.help_section4_content);
        addSection(layout, R.string.help_section5_title, R.string.help_section5_content);
        addSection(layout, R.string.help_section6_title, R.string.help_section6_content);
        
        setContentView(scrollView);
        setTitle(R.string.help_title);
    }
    
    private void addSection(LinearLayout parent, int titleRes, int contentRes) {
        TextView title = new TextView(this);
        title.setText(titleRes);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 16, 0, 8);
        parent.addView(title);
        
        TextView content = new TextView(this);
        content.setText(contentRes);
        content.setTextSize(14);
        content.setPadding(0, 0, 0, 16);
        parent.addView(content);
    }
}
