package com.epson.epos2_printer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setupAccordion(R.id.q_wifi_title, R.id.q_wifi_content);
        setupAccordion(R.id.q_app_title, R.id.q_app_content);
        setupAccordion(R.id.q_print_title, R.id.q_print_content);
    }

    private void setupAccordion(int titleId, int contentId) {

        View title = findViewById(titleId);
        View content = findViewById(contentId);

        title.setOnClickListener(v -> {
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
            }
        });
    }
}