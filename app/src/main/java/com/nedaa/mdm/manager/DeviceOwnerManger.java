package com.nedaa.mdm.manager;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nedaa.mdm.broadcast.DeviceAdminReceiver;

public class DeviceOwnerManger {
    private static final String TAG = "DeviceOwnerManager";

    public static boolean setupDeviceOwner(Context context) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, DeviceAdminReceiver.class);

            if (dpm.isDeviceOwnerApp(context.getPackageName())) {
                Log.d(TAG, "App is already device owner");
                return true;
            }

            // First, enable the admin component
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required for MDM functionality");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Log.d(TAG, "Device admin component enabled. Please complete device owner setup through proper provisioning method.");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error setting device owner: " + e.getMessage());
            return false;
        }
    }
}