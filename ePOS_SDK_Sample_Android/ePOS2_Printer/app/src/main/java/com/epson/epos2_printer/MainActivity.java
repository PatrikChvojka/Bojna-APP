package com.epson.epos2_printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MainActivity extends Activity implements View.OnClickListener, ReceiveListener {

    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    private static final int MIN_MAX_USES = 1;
    private static final int MAX_MAX_USES = 99999;
    private static final String RASPBERRY_API_PORT = "3000";
    private static final String RASPBERRY_QR_PATH = "/api/qr";
    private static final String RASPBERRY_GATE_PATH = "/api/gate"; // TODO: doplniť keď bude endpoint
    private static final String GATE_DIRECTION_IN = "in";
    private static final String GATE_DIRECTION_OUT = "out";

    private Context mContext = null;
    public static EditText mEditTarget = null;
    public static Printer  mPrinter = null;
    public static ToggleButton mDrawer = null;
    public static ProgressIndicator mProgressIndicator = null;

    private android.os.Handler statusHandler = new android.os.Handler();
    private Runnable statusRunnable;

    private android.os.Handler statusUiHandler = new android.os.Handler();
    private Runnable statusUiRunnable;

    private volatile boolean isPrinting = false;
    private boolean printerStableOk = true;
    private int failCount = 0;
    private EditText edtMaxUses;
    private String printerAlert = "";
    private String actionAlert = "";

    private void startStatusPolling() {
        statusRunnable = new Runnable() {
            @Override
            public void run() {

                if (!isPrinting && mPrinter != null) {
                    new Thread(() -> {
                        try {
                            PrinterStatusInfo status = mPrinter.getStatus();
                            runOnUiThread(() -> dispPrinterWarnings(status));
                        } catch (Exception e) {
                            runOnUiThread(() -> setPrinterAlert(
                                    "Nepodarilo sa načítať stav tlačiarne.\n" + e.getMessage()
                            ));
                        }
                    }).start();
                }

                statusHandler.postDelayed(this, 3000);
            }
        };

        statusHandler.post(statusRunnable);
    }

    private void connectOnce() {
        new Thread(() -> {
            try {
                mPrinter.connect("TCP:192.168.1.50", Printer.PARAM_DEFAULT);
            } catch (Exception e) {
                setActionAlert("Nepodarilo sa pripojiť tlačiareň.\n" + e.getMessage(), false);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.util.Log.e("BOOTCHECK", "onCreate STARTED");

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestRuntimePermission();

        enableLocationSetting();

        mContext = this;

        mProgressIndicator = new ProgressIndicator(mContext);

        edtMaxUses = findViewById(R.id.edtMaxUses);
        setupMainActions();

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        initializeObject();

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 50, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }

        // 👉 DOPLŇ TOTO
        connectOnce();
        startStatusPolling();

        // settings icon
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // helper
        findViewById(R.id.btnHelp).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        ImageView btnTickets = findViewById(R.id.btnTickets);

        btnTickets.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TicketsActivity.class);
            startActivity(intent);
        });

        // status
        startUiStatusLoop();

        android.util.Log.e("BOOTCHECK", "onCreate FINISHED OK");
    }

    @Override
    protected void onDestroy() {
        statusHandler.removeCallbacks(statusRunnable);
        finalizeObject();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // Click handling is wired in setupMainActions().
    }

    private void setupMainActions() {
        findViewById(R.id.btnVstup).setOnClickListener(v -> startPrintJob(getSelectedMaxUses()));

        findViewById(R.id.btnQtyMinus).setOnClickListener(v -> changeMaxUses(-1));
        findViewById(R.id.btnQtyPlus).setOnClickListener(v -> changeMaxUses(1));
        findViewById(R.id.btnAlert).setOnClickListener(v -> showAlertPopup());
        setupMaxUsesInput();
        updateVstupCountLabel(getSelectedMaxUses());

        findViewById(R.id.btnGate1In).setOnClickListener(v -> requestGateOpen("1", GATE_DIRECTION_IN, "Hlavná brána"));
        findViewById(R.id.btnGate1Out).setOnClickListener(v -> requestGateOpen("1", GATE_DIRECTION_OUT, "Hlavná brána"));
        findViewById(R.id.btnGate2In).setOnClickListener(v -> requestGateOpen("2", GATE_DIRECTION_IN, "Brána pre vozičkárov a kočíky"));
        findViewById(R.id.btnGate2Out).setOnClickListener(v -> requestGateOpen("2", GATE_DIRECTION_OUT, "Brána pre vozičkárov a kočíky"));
    }

    private void setupMaxUsesInput() {
        edtMaxUses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateVstupCountLabel(parseMaxUses(s.toString()));
            }
        });

        edtMaxUses.setOnEditorActionListener((v, actionId, event) -> {
            boolean done = actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN);

            if (done) {
                getSelectedMaxUses();
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });

        edtMaxUses.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                getSelectedMaxUses();
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private int parseMaxUses(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return MIN_MAX_USES;
        }

        try {
            int value = Integer.parseInt(raw.trim());
            if (value < MIN_MAX_USES) {
                return MIN_MAX_USES;
            }
            if (value > MAX_MAX_USES) {
                return MAX_MAX_USES;
            }
            return value;
        } catch (Exception ignored) {
            return MIN_MAX_USES;
        }
    }

    /**
     * Shared print flow: generate QR, send it to Raspberry with max_uses, then print the ticket.
     * Call this from any future button that should issue an entry ticket.
     */
    private void startPrintJob(final int maxUses) {
        if (isPrinting) {
            return;
        }

        if (printerAlert != null && !printerAlert.trim().isEmpty()) {
            refreshAlertUi();
            showAlertPopup();
            return;
        }

        mProgressIndicator.beginProgress(getString(R.string.progress_msg));
        new Thread(() -> {
            if (!runPrintReceiptSequence(maxUses)) {
                mProgressIndicator.endProgress();
            }
        }).start();
    }

    private int getSelectedMaxUses() {
        int clamped = parseMaxUses(edtMaxUses != null ? edtMaxUses.getText().toString() : "");

        if (edtMaxUses != null && !String.valueOf(clamped).equals(edtMaxUses.getText().toString().trim())) {
            edtMaxUses.setText(String.valueOf(clamped));
        }

        updateVstupCountLabel(clamped);
        return clamped;
    }

    private void changeMaxUses(int delta) {
        int value = getSelectedMaxUses() + delta;
        if (value < MIN_MAX_USES) {
            value = MIN_MAX_USES;
        }
        if (value > MAX_MAX_USES) {
            value = MAX_MAX_USES;
        }
        edtMaxUses.setText(String.valueOf(value));
        updateVstupCountLabel(value);
    }

    private void updateVstupCountLabel(int maxUses) {
        android.widget.TextView label = findViewById(R.id.txtVstupCount);
        if (label == null) {
            return;
        }
        if (maxUses <= 1) {
            label.setText("1 vstup");
        } else if (maxUses <= 4) {
            label.setText(maxUses + " vstupy");
        } else {
            label.setText(maxUses + " vstupov");
        }
    }

    /**
     * Manual gate open. Does not print a ticket and does not generate QR.
     * Raspberry URL/JSON will be filled in when the endpoint is ready.
     */
    private void requestGateOpen(final String gateId, final String direction, final String gateName) {
        new Thread(() -> {
            String raspberryIp = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)
                    .getString("raspberry_ip", "");

            if (raspberryIp == null || raspberryIp.trim().isEmpty()) {
                showWarning("ERROR: Raspberry IP nie je nastavená!");
                return;
            }

            // TODO: nahradiť path a JSON keď bude Raspberry endpoint hotový.
            String payload =
                    "{"
                            + "\"gate\":\"" + gateId + "\","
                            + "\"direction\":\"" + direction + "\""
                            + "}";

            showWarning(gateName + " / " + direction
                    + " — endpoint ešte nie je nastavený.\n"
                    + RASPBERRY_GATE_PATH + "\n" + payload);

            // postJsonToRaspberry(raspberryIp.trim(), RASPBERRY_GATE_PATH, payload);
        }).start();
    }

    private void showWarning(final String message) {
        setActionAlert(message, true);
    }

    private void setActionAlert(final String message, final boolean popup) {
        runOnUiThread(() -> {
            actionAlert = message == null ? "" : message.trim();
            refreshAlertUi();
            if (popup && !actionAlert.isEmpty()) {
                showAlertPopup();
            }
        });
    }

    private void setPrinterAlert(String message) {
        printerAlert = message == null ? "" : message.trim();
        refreshAlertUi();
    }

    private String combinedAlert() {
        StringBuilder sb = new StringBuilder();
        if (printerAlert != null && !printerAlert.isEmpty()) {
            sb.append(printerAlert);
        }
        if (actionAlert != null && !actionAlert.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(actionAlert);
        }
        return sb.toString().trim();
    }

    private void refreshAlertUi() {
        View btn = findViewById(R.id.btnAlert);
        View scroll = findViewById(R.id.contentScroll);
        boolean hasError = !combinedAlert().isEmpty();

        if (btn != null) {
            btn.setVisibility(hasError ? View.VISIBLE : View.GONE);
        }

        if (scroll != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) scroll.getLayoutParams();
            lp.bottomMargin = dp(hasError ? 88 : 16);
            scroll.setLayoutParams(lp);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showAlertPopup() {
        String message = combinedAlert();
        if (message.isEmpty()) {
            return;
        }

        Dialog dialog = new Dialog(this);
        try {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception ignored) {
        }
        dialog.setContentView(R.layout.dialog_alert);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView txt = dialog.findViewById(R.id.txtAlertMessage);
        txt.setText(message);
        dialog.findViewById(R.id.btnAlertClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private int checkNetworkStatus() {

        String expectedSSID = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)
                .getString("network_name", "");

        if (expectedSSID != null) {
            expectedSSID = expectedSSID
                    .replace("\"", "")
                    .trim();
        }

        if (expectedSSID == null || expectedSSID.isEmpty()) {
            return 1; // ORANGE - WiFi nie je nastavená
        }

        WifiManager wifiManager =
                (WifiManager) getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            return 0; // RED
        }

        WifiInfo info = wifiManager.getConnectionInfo();

        if (info == null) {
            return 0; // RED
        }

        String currentSSID = info.getSSID();

        // Honor môže krátkodobo vrátiť <unknown ssid>,
        // aj keď je tablet reálne pripojený.
        if (currentSSID == null ||
                currentSSID.equals("<unknown ssid>") ||
                currentSSID.equals("unknown ssid")) {
            return 1; // ORANGE - SSID zatiaľ nie je dostupné
        }

        currentSSID = currentSSID
                .replace("\"", "")
                .trim();

        if (currentSSID.equals(expectedSSID)) {
            return 2; // GREEN - správna WiFi
        }

        return 1; // ORANGE - pripojená iná WiFi
    }

    private void startUiStatusLoop() {

        statusUiRunnable = new Runnable() {
            @Override
            public void run() {

                new Thread(() -> {

                    boolean printerOk = checkPrinterStatus();
                    int raspberryState = checkRaspberryStatus();
                    int networkState = checkNetworkStatus();

                    runOnUiThread(() ->
                            updateStatusUI(printerOk, raspberryState, networkState)
                    );

                }).start();

                statusUiHandler.postDelayed(this, 5000); // každých 5s
            }
        };

        statusUiHandler.post(statusUiRunnable);
    }

    private boolean checkPrinterStatus() {
        try {
            if (mPrinter == null) return false;

            PrinterStatusInfo status = mPrinter.getStatus();
            if (status == null) return false;

            boolean online = status.getOnline() == Printer.TRUE;

            if (online) {
                failCount = 0;
                printerStableOk = true;
            } else {
                failCount++;
            }

            // 👉 nepanikár hneď
            if (failCount >= 3) {
                printerStableOk = false;
            }

            return printerStableOk;

        } catch (Exception e) {
            failCount++;
            return failCount < 3;
        }
    }

    private int checkRaspberryStatus() {

        String ip = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)
                .getString("raspberry_ip", "");

        // IP nie je nastavená
        if (ip == null || ip.trim().isEmpty()) {
            return 1; // ORANGE
        }

        ip = ip.trim();

        try {
            java.net.Socket socket = new java.net.Socket();

            socket.connect(
                    new java.net.InetSocketAddress(ip, 80),
                    1500
            );

            socket.close();

            return 2; // GREEN - Raspberry dostupné

        } catch (Exception e) {

            return 0; // RED - Raspberry nedostupné
        }
    }

    private void updateStatusUI(boolean printerOk, int raspState, int netState) {

        View printerDot = findViewById(R.id.statusPrinterDot);
        View raspDot = findViewById(R.id.statusRaspberryDot);
        View netDot = findViewById(R.id.statusNetworkDot);

        // 🖨️ printer
        printerDot.setBackgroundColor(
                printerOk ? 0xFF00C853 : 0xFFD50000
        );

        // 🍓 raspberry
        if (raspState == 0) {
            raspDot.setBackgroundColor(0xFFD50000);
        } else if (raspState == 1) {
            raspDot.setBackgroundColor(0xFFFFA000);
        } else {
            raspDot.setBackgroundColor(0xFF00C853);
        }

        // 📶 network
        if (netState == 0) {
            netDot.setBackgroundColor(0xFFD50000);
        } else if (netState == 1) {
            netDot.setBackgroundColor(0xFFFFA000);
        } else {
            netDot.setBackgroundColor(0xFF00C853);
        }
    }

    private String generateQrCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();

        for (int i = 0; i < 15; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private boolean runPrintReceiptSequence(int maxUses) {
        if (maxUses < MIN_MAX_USES) {
            maxUses = MIN_MAX_USES;
        }

        String raspberryIp = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)
                .getString("raspberry_ip", "");

        // 1. kontrola IP
        if (raspberryIp == null || raspberryIp.trim().isEmpty()) {
            showWarning("ERROR: Raspberry IP nie je nastavená!");
            return false;
        }

        // 2. GENERUJ QR IBA TU (čerstvý každý print)
        String qrCode = generateQrCode();

        // 3. vytvor print data (použije ten istý QR)
        if (!createReceiptData(qrCode, maxUses)) {
            return false;
        }

        // 4. odošli na Raspberry (TEN ISTÝ QR + max_uses)
        boolean raspberryOk = sendToRaspberry(raspberryIp, qrCode, maxUses);

        if (!raspberryOk) {
            return false;
        }

        // 5. print až po úspechu
        return printData();
    }

    private boolean sendToRaspberry(String ip, String qrCode, int maxUses) {
        String payload =
                "{"
                        + "\"code\":\"" + qrCode + "\","
                        + "\"source_device\":\"tablet-test\","
                        + "\"max_uses\":" + maxUses
                        + "}";

        return postJsonToRaspberry(ip, RASPBERRY_QR_PATH, payload);
    }

    private boolean postJsonToRaspberry(String ip, String path, String jsonPayload) {
        java.net.HttpURLConnection conn = null;

        try {
            String urlStr = "http://" + ip + ":" + RASPBERRY_API_PORT + path;
            java.net.URL url = new java.net.URL(urlStr);

            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer spinentry_token_123");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            java.io.OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200 && responseCode != 201) {
                showWarning("Raspberry vrátilo chybu HTTP " + responseCode + ".\nSkontrolujte, či server beží a či sedí IP adresa.");
                return false;
            }

            return true;

        } catch (Exception e) {
            showWarning("Nepodarilo sa spojiť s Raspberry.\n" + e.getMessage());
            return false;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private boolean createReceiptData(String qrCode, int maxUses) {

        Date now = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String currentDate = dateFormat.format(now);
        String currentTime = timeFormat.format(now);

        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            method = "addImage";

            int maxWidth = 384;

            int newHeight = (int)((double) logoData.getHeight() / logoData.getWidth() * maxWidth);

            Bitmap resizedLogo = Bitmap.createScaledBitmap(logoData, maxWidth, newHeight, false);

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            mPrinter.addImage(resizedLogo, 0, 0,
                    resizedLogo.getWidth(),
                    resizedLogo.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

            mPrinter.addFeedLine(2);


            method = "addSymbol";
            mPrinter.addSymbol(
                    qrCode, // obsah QR
                    Printer.SYMBOL_QRCODE_MODEL_2,
                    Printer.LEVEL_L,   // L, M, Q, H
                    13,                 // veľkosť (1–16)
                    13,                 // veľkosť modulu
                    0                  // nechať 0
            );


            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            textData.append("Vitajte v Zvieracej zóne !\n");
            textData.append("------------------------------\n");

            textData.append("Dátum: " + currentDate + "\n");
            textData.append("Čas: " + currentTime + "\n");

            textData.append("Ďakujeme za návštevu!\n");
            textData.append("Prajeme príjemný deň a veľa zážitkov.\n");
            textData.append("\n");

            textData.append("VSTUP\n");
            textData.append("------------------------------\n");
            textData.append("Tento QR kód použite pre vstup cez turniket.\n");
            textData.append("Priložte ho k čítačke.\n");
            if (maxUses <= 1) {
                textData.append("QR kód je neplatný po prvom použití.\n");
            } else {
                textData.append("QR kód platí pre " + maxUses + " prechodov turniketom.\n");
            }
            textData.append("\n" + formatEntryValidity(maxUses) + "\n");
            textData.append("\nVstupom súhlasíte  s návštevným poriadkom.\n");
            textData.append("\nĎakujeme, že svojím príspevkom\n");
            textData.append("podporujete OZ POD BABICOU.\n\n");

            method = "addText";
            mPrinter.addText(textData.toString());

            textData.delete(0, textData.length());

            method = "addFeedLine";
            mPrinter.addFeedLine(3);


            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            showWarning("Nepodarilo sa pripraviť lístok na tlač.\n" + e.getMessage());
            return false;
        }

        textData = null;

        return true;
    }

    private String formatEntryValidity(int maxUses) {
        if (maxUses <= 1) {
            return "Platnosť: jednorazový vstup";
        }
        if (maxUses <= 4) {
            return "Platnosť: " + maxUses + " vstupy";
        }
        return "Platnosť: " + maxUses + " vstupov";
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        isPrinting = true; // 🔒 zamkni polling

        if (!connectPrinter()) {
            mPrinter.clearCommandBuffer();
            isPrinting = false;
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            mPrinter.clearCommandBuffer();
            isPrinting = false;
            return false;
        }

        return true;
    }

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(
                    Printer.TM_M30,        // FIX model
                    Printer.MODEL_ANK,     // FIX language
                    mContext
            );
        } catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }

        mPrinter.setReceiveEventListener(this);
        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean connectPrinter() {
        if (mPrinter == null) {
            return false;
        }

        try {
            SharedPreferences prefs = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE);
            String ip = prefs.getString("printer_ip", "192.168.1.50");

            mPrinter.connect("TCP:" + ip, Printer.PARAM_DEFAULT);

            // 👉 DOPLŇ delay + UI update
            new Thread(() -> {
                try {
                    Thread.sleep(300); // malý delay (dôležité!)
                } catch (InterruptedException e) {}

                PrinterStatusInfo status = mPrinter.getStatus();

                runOnUiThread(() -> dispPrinterWarnings(status));
            }).start();

        } catch (Exception e) {
            showWarning("Nepodarilo sa pripojiť k tlačiarni.\nSkontrolujte, či je zapnutá a či sedí IP adresa.");
            return false;
        }

        return true;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        while (true) {
            try {
                mPrinter.disconnect();
                break;
            } catch (final Exception e) {
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
    }

    private String buildPrinterAlert(PrinterStatusInfo status) {
        if (status == null) {
            return "";
        }

        StringBuilder msg = new StringBuilder();

        if (status.getOnline() == Printer.FALSE) {
            msg.append("• Tlačiareň je offline.\n");
        }
        if (status.getConnection() == Printer.FALSE) {
            msg.append("• Tlačiareň neodpovedá. Skontrolujte pripojenie a IP adresu.\n");
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg.append("• Kryt tlačiarne je otvorený. Zatvorte ho.\n");
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg.append("• V tlačiarni nie je papier. Vložte nový kotúč.\n");
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg.append("• Uvoľnite tlačidlo posuvu papiera.\n");
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg.append("• Zaseknutý papier alebo chyba rezačky. Odstráňte papier, zatvorte kryt a reštartujte tlačiareň.\n");
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg.append("• Závažná chyba tlačiarne. Vypnite ju a znova zapnite.\n");
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg.append("• Tlačová hlava je prehriata. Počkajte, kým zhasne chybová LED.\n");
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg.append("• Motor tlačiarne je prehriaty. Počkajte, kým zhasne chybová LED.\n");
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg.append("• Batéria tlačiarne je prehriata. Počkajte, kým zhasne chybová LED.\n");
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg.append("• Vložte správny typ kotúča papiera.\n");
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg.append("• Batéria tlačiarne je takmer vybitá. Pripojte napájanie.\n");
        }
        if (status.getRemovalWaiting() == Printer.REMOVAL_WAIT_PAPER) {
            msg.append("• Vyberte vytlačený lístok z tlačiarne.\n");
        }
        if (status.getUnrecoverError() == Printer.HIGH_VOLTAGE_ERR ||
                status.getUnrecoverError() == Printer.LOW_VOLTAGE_ERR) {
            msg.append("• Skontrolujte napätie tlačiarne.\n");
        }

        return msg.toString().trim();
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        setPrinterAlert(buildPrinterAlert(status));
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(() -> {

            new Thread(() -> disconnectPrinter()).start();

            isPrinting = false;

            mProgressIndicator.endProgress();

            dispPrinterWarnings(status);

            if (code != 0) {
                String printError = buildPrinterAlert(status);
                if (printError.isEmpty()) {
                    printError = "Tlač lístka zlyhala.";
                }
                setActionAlert(printError, true);
            } else {
                actionAlert = "";
                refreshAlertUi();
            }
        });
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            // Android 12+ – Bluetooth
            int permissionBluetoothScan =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                    );

            int permissionBluetoothConnect =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                    );

            // Android 12+ – potrebujeme aj polohu,
            // aby Android dovolil aplikácii zistiť SSID Wi-Fi
            int permissionLocationFine =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    );

            if (permissionBluetoothScan == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            if (permissionBluetoothConnect == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }

            if (permissionLocationFine == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            // Android 10–11
            int permissionLocationFine =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    );

            if (permissionLocationFine == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

        } else {

            // Android 6–9
            int permissionLocationCoarse =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    );

            if (permissionLocationCoarse == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    requestPermissions.toArray(
                            new String[requestPermissions.size()]
                    ),
                    REQUEST_PERMISSION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
                // If your app targets Android 12 (API level 31) and higher, it's recommended that you declare BLUETOOTH permission.
                if (permissions[i].equals(Manifest.permission.BLUETOOTH_SCAN)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
                if (permissions[i].equals(Manifest.permission.BLUETOOTH_CONNECT)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
            } else if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                // If your app targets Android 11 (API level 30) or lower, it's necessary that you declare ACCESS_FINE_LOCATION permission.
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
            } else {
                // If your app targets Android 9 (API level 28) or lower, you can declare the ACCESS_COARSE_LOCATION permission instead of the ACCESS_FINE_LOCATION permission.
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    //When searching for a device running on Android 10 or later as a Bluetooth-capable device, enable access to location information of the device.
    private void enableLocationSetting() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);;

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                CommonStatusCodes.RESOLUTION_REQUIRED);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }
}
