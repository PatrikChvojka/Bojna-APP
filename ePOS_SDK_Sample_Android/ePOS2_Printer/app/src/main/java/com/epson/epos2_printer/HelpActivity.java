package com.epson.epos2_printer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ((android.widget.TextView) findViewById(R.id.txtPageTitle)).setText("Časté otázky");

        int[][] items = {
                {R.id.q_colors_title, R.id.q_colors_content},
                {R.id.q_printer_red_title, R.id.q_printer_red_content},
                {R.id.q_rasp_title, R.id.q_rasp_content},
                {R.id.q_wifi_title, R.id.q_wifi_content},
                {R.id.q_print_title, R.id.q_print_content},
                {R.id.q_paper_title, R.id.q_paper_content},
                {R.id.q_print_green_title, R.id.q_print_green_content},
                {R.id.q_vstup_title, R.id.q_vstup_content},
                {R.id.q_uses_title, R.id.q_uses_content},
                {R.id.q_qr_title, R.id.q_qr_content},
                {R.id.q_preview_title, R.id.q_preview_content},
                {R.id.q_settings_title, R.id.q_settings_content},
                {R.id.q_gates_title, R.id.q_gates_content},
                {R.id.q_app_title, R.id.q_app_content}
        };

        for (int[] item : items) {
            setupAccordion(item[0], item[1]);
        }
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
