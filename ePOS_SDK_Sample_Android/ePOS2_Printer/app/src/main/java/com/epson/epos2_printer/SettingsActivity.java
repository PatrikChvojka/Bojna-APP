package com.epson.epos2_printer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends Activity {

    private EditText edtPrinterIp, edtRaspberryIp, edtNetworkName, edtTicketsUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        edtPrinterIp = findViewById(R.id.edtPrinterIp);
        edtRaspberryIp = findViewById(R.id.edtRaspberryIp);
        edtNetworkName = findViewById(R.id.edtNetworkName);
        edtTicketsUrl = findViewById(R.id.edtTicketsUrl);
        android.view.View btnSave = findViewById(R.id.btnSave);

        loadSettings();

        ((android.widget.TextView) findViewById(R.id.txtPageTitle)).setText("Nastavenia");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE);

        edtPrinterIp.setText(prefs.getString("printer_ip", "192.168.1.50"));
        edtRaspberryIp.setText(prefs.getString("raspberry_ip", ""));
        edtNetworkName.setText(prefs.getString("network_name", ""));
        edtTicketsUrl.setText(prefs.getString("tickets_url", ""));
    }

    private void saveSettings() {
        String printerIp = edtPrinterIp.getText().toString().trim();
        String raspberryIp = edtRaspberryIp.getText().toString().trim();
        String networkName = edtNetworkName.getText().toString().trim();
        String ticketsUrl = edtTicketsUrl.getText().toString().trim();

        // ✅ VALIDÁCIA IP tlačiarne
        if (!printerIp.matches("^((25[0-5]|2[0-4]\\d|1\\d\\d|\\d\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|\\d\\d|\\d)$")) {
            edtPrinterIp.setError("Neplatná IP");
            return;
        }

        // ✅ VALIDÁCIA IP raspberry (môže byť aj prázdna ak chceš)
        if (!raspberryIp.isEmpty() && !raspberryIp.matches("^((25[0-5]|2[0-4]\\d|1\\d\\d|\\d\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|\\d\\d|\\d)$")) {
            edtRaspberryIp.setError("Neplatná IP");
            return;
        }

        // (voliteľné) validácia názvu siete
        if (networkName.isEmpty()) {
            edtNetworkName.setError("Zadaj názov siete");
            return;
        }

        if (!ticketsUrl.isEmpty() && !ticketsUrl.startsWith("http")) {
            edtTicketsUrl.setError("URL musí začínať na http/https");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("printer_ip", printerIp);
        editor.putString("raspberry_ip", raspberryIp);
        editor.putString("network_name", networkName);
        editor.putString("tickets_url", ticketsUrl);

        editor.apply();

        finish();
    }
}