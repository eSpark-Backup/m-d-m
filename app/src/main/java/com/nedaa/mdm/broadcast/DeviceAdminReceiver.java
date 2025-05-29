package com.nedaa.mdm.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nedaa.mdm.service.MDMService;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
    private static final String TAG = "MDMDeviceAdmin";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.d(TAG, "Device Admin enabled");
        // Start your MDM service here
        Intent serviceIntent = new Intent(context, MDMService.class);
        context.startService(serviceIntent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.d(TAG, "Device Admin disabled");
        // Stop your MDM service here
        Intent serviceIntent = new Intent(context, MDMService.class);
        context.stopService(serviceIntent);
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        Log.d(TAG, "Password changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        Log.d(TAG, "Password failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
        Log.d(TAG, "Password succeeded");
    }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        super.onProfileProvisioningComplete(context, intent);
        Log.d(TAG, "Profile provisioning complete");
        // Start your MDM service here
        Intent serviceIntent = new Intent(context, MDMService.class);
        context.startService(serviceIntent);
    }
}