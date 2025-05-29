package com.nedaa.mdm;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.nedaa.mdm.service.MDMService;


public class MDMApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        if (DeviceOwnerManger.setupDeviceOwner(this)) {
//            Log.d("IM", "Device owner setup successful");
//        } else {
//            Log.e("IM", "Device owner setup failed");
//        }
        startMDMService();
    }

    private void startMDMService() {
        Intent serviceIntent = new Intent(this, MDMService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}