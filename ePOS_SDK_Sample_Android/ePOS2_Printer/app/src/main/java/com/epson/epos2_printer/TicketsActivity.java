package com.epson.epos2_printer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class TicketsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        ((TextView) findViewById(R.id.txtPageTitle)).setText("Live preview");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        SharedPreferences prefs = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE);
        String url = prefs.getString("tickets_url", "https://www.rancpodbabicou.sk/");

        webView.loadUrl(url);
    }
}
