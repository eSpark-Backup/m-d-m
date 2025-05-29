package com.nedaa.mdm.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nedaa.mdm.activity.DeviceStatusActivity;
import com.nedaa.mdm.manager.APNManager;
import com.nedaa.mdm.manager.AppManager;
import com.nedaa.mdm.manager.DeviceManager;
import com.nedaa.mdm.utils.BatteryUtils;
import com.nedaa.mdm.utils.NetworkUtils;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public class MDMService extends Service {
    private static final String TAG = "MDMService";
    private static final String CHANNEL_ID = "MDMServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String SERVER_URL = "https://your-mdm-server.com"; // Replace with your server URL
    private Timer timer;
    private TelephonyManager telephonyManager;
    @Inject DeviceManager deviceManager;
    private Disposable deviceStatusDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        initializeManagers();
        startMonitoring();
//        setDefaultAPN();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "MDM Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        return builder
                .setContentTitle("System Service")
                .setContentText("Running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }

    private void initializeManagers() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    private void startMonitoring() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                checkDeviceStatus();
                monitorNetworkStatus();
                checkBatteryStatus();
                checkNedaaApp();
            }
        }, 0, 60000);
    }

    private void setDefaultAPN() {
        if (!Objects.equals(APNManager.getCurrentAPN(this), "lebara")) {
            APNManager.setDefaultAPN(this);
        }
    }

    @SuppressLint("CheckResult")
    private void checkDeviceStatus() {
        String imei = getImei();
        String iccid = getIccid();
        String deviceHash = calculateDeviceHash(imei, iccid);

        deviceStatusDisposable = Single.defer(() -> {
            if (!deviceManager.isDeviceRegistered(this)) {
                return deviceManager.registerDevice(SERVER_URL, deviceHash, this)
                        .flatMap(response -> {
                            if (!response.is_enabled) {
                                return Single.error(new IllegalStateException("Unregistered"));
                            }
                            return Single.just(response); // proceed if enabled
                        });
            } else {
                return deviceManager.checkDeviceStatus()
                        .flatMap(response -> {
                            if (!response.is_enabled) {
                                return Single.error(new IllegalStateException("Disabled"));
                            }
                            return Single.just(response);
                        });
            }
        }).subscribe(
                response -> {
                    Log.d("IM", "Device is enabled and registered");
                },
                throwable -> {
                    if (throwable.getMessage().equals("Unregistered")) {
                        showDeviceStatus(DeviceStatusActivity.STATUS_UNREGISTERED);
                    } else if (throwable.getMessage().equals("Disabled")) {
                        showDeviceStatus(DeviceStatusActivity.STATUS_DISABLED);
                    } else {
                        showDeviceStatus(DeviceStatusActivity.STATUS_DISABLED);
                    }
                    Log.e("IM", "Error: " + throwable.getMessage(), throwable);
                }
        );
    }

    private void showDeviceStatus(String status) {
        Intent intent = new Intent(this, DeviceStatusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DeviceStatusActivity.EXTRA_STATUS, status);
        startActivity(intent);
    }

    private String getImei() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return telephonyManager.getImei();
        } else {
            return telephonyManager.getDeviceId();
        }
    }

    private String getIccid() {
        try {
            return telephonyManager.getSimSerialNumber();
        } catch (SecurityException e) {
            Log.e(TAG, "Error getting ICCID", e);
            return "";
        }
    }

    private String calculateDeviceHash(String imei, String iccid) {
        try {
            String combined = imei + iccid;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error calculating hash", e);
            return "";
        }
    }

    private void monitorNetworkStatus() {
        String carrierName = NetworkUtils.getCarrierName(this);
        Log.d(TAG, "Carrier: " + carrierName);

        // Get network type
        String networkType = NetworkUtils.getNetworkType(this);
        Log.d(TAG, "Network Type: " + networkType);

        if (NetworkUtils.isWifiConnected(this)) {
            int wifiSignal = NetworkUtils.getWifiSignalStrength(this);
            Log.d(TAG, "WiFi Signal Strength: " + wifiSignal);
        } else if (NetworkUtils.isCellularConnected(this)) {
            int cellularSignal = NetworkUtils.getCellularSignalStrength(this);
            Log.d(TAG, "Cellular Signal Strength: " + cellularSignal);
        }

        long latency = NetworkUtils.measureLatency();
        if (latency != -1) {
            Log.d(TAG, "Network Latency: " + latency + "ms");
        }
    }

    private void checkBatteryStatus() {
        BatteryUtils.BatteryInfo batteryInfo = BatteryUtils.getBatteryInfo(this);

        Log.d(TAG, "Battery Level: " + batteryInfo.level + "%");
        Log.d(TAG, "Battery Temperature: " + batteryInfo.temperature + "°C");
        Log.d(TAG, "Battery Health: " + batteryInfo.health);
        Log.d(TAG, "Battery Technology: " + batteryInfo.technology);
        Log.d(TAG, "Is Charging: " + batteryInfo.isCharging);

        // Check for critical conditions
        if (BatteryUtils.isBatteryLow(this)) {
            Log.w(TAG, "Battery level is low!");
        }
        if (BatteryUtils.isBatteryOverheating(this)) {
            Log.w(TAG, "Battery is overheating!");
        }
    }

    private void checkNedaaApp() {
        if (!AppManager.isAppInstalled(this, "com.nedaa")) {
            // App is not installed, download and install
            String apkUrl = SERVER_URL + "/download/nedaa.apk"; // Replace with actual URL
            AppManager.installApp(this, apkUrl);
        } else {
            // Check for updates
            String currentVersion = AppManager.getAppVersion(this, "com.nedaa");
            // TODO: Compare with server version and update if needed
            // String serverVersion = getServerVersion();
            // if (needsUpdate(currentVersion, serverVersion)) {
            //     AppManager.updateApp(this, SERVER_URL + "/download/nedaa-latest.apk");
            // }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (deviceStatusDisposable != null && !deviceStatusDisposable.isDisposed()) {
            deviceStatusDisposable.dispose();
        }
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}