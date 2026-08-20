package com.epson.epos2_printer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TicketsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        WebView webView = findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        SharedPreferences prefs = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE);
        String url = prefs.getString("tickets_url", "https://www.rancpodbabicou.sk/");

        webView.loadUrl(url);
    }
}