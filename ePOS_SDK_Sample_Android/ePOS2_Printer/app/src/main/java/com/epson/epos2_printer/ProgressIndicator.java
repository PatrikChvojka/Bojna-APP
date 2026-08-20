package com.epson.epos2_printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ProgressIndicator extends Activity {
    private AlertDialog mProgressDialog;
    private TextView mProgressDialogMessage;

    ProgressIndicator(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_progress, null);
        mProgressDialogMessage = view.findViewById(R.id.progressMessage);
        builder.setView(view);
        mProgressDialog = builder.create();
        mProgressDialog.setCancelable(false);
    }

    // Indicator
    // Begin progress
    protected void beginProgress(final String msg) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                mProgressDialogMessage.setText(msg);
                mProgressDialog.show();
            }
        });
    }

    // Change progress
    protected void changeProgress(final String msg) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                mProgressDialogMessage.setText(msg);
            }
        });
    }

    // End progress
    protected void endProgress() {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }
}
