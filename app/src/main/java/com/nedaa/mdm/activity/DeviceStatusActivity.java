package com.nedaa.mdm.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

import dagger.hilt.android.AndroidEntryPoint;

public class DeviceStatusActivity extends Activity {
    public static final String EXTRA_STATUS = "status";
    public static final String STATUS_UNREGISTERED = "unregistered";
    public static final String STATUS_DISABLED = "disabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String status = getIntent().getStringExtra(EXTRA_STATUS);
        if (status != null) {
            showStatusDialog(status);
        }
    }

    private void showStatusDialog(String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (STATUS_UNREGISTERED.equals(status)) {
            builder.setTitle("Device Unregistered")
                    .setMessage("This device is not registered with the MDM server.")
                    .setCancelable(false);
        } else if (STATUS_DISABLED.equals(status)) {
            builder.setTitle("Device Disabled")
                    .setMessage("This device has been disabled by the administrator.")
                    .setCancelable(false);

            // Disable all input
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (STATUS_DISABLED.equals(status)) {
                    // Keep showing the dialog
                    showStatusDialog(status);
                } else {
                    finish();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getIntent().getStringExtra(EXTRA_STATUS).equals(STATUS_DISABLED)) {
            return true; // Block all key events
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getIntent().getStringExtra(EXTRA_STATUS).equals(STATUS_DISABLED)) {
            return true; // Block all touch events
        }
        return super.onTouchEvent(event);
    }
}