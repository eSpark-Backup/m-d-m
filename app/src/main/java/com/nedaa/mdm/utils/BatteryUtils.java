package com.nedaa.mdm.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryUtils {
    private static final String TAG = "BatteryUtils";

    public static class BatteryInfo {
        public int level;
        public float temperature;
        public boolean isCharging;
        public String health;
        public String technology;
    }

    public static BatteryInfo getBatteryInfo(Context context) {
        BatteryInfo info = new BatteryInfo();
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            // Battery level
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            info.level = (int) (level * 100 / (float) scale);

            // Battery temperature
            info.temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f;

            // Charging status
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            info.isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            // Battery health
            int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            switch (health) {
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    info.health = "Good";
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    info.health = "Overheated";
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    info.health = "Dead";
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    info.health = "Over Voltage";
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    info.health = "Unspecified Failure";
                    break;
                case BatteryManager.BATTERY_HEALTH_COLD:
                    info.health = "Cold";
                    break;
                default:
                    info.health = "Unknown";
            }

            // Battery technology
            info.technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        } catch (Exception e) {
            Log.e(TAG, "Error getting battery info", e);
        }
        return info;
    }

    public static boolean isBatteryLow(Context context) {
        BatteryInfo info = getBatteryInfo(context);
        return info.level <= 15;
    }

    public static boolean isBatteryOverheating(Context context) {
        BatteryInfo info = getBatteryInfo(context);
        return info.temperature > 45.0f; // 45°C is considered overheating
    }
}