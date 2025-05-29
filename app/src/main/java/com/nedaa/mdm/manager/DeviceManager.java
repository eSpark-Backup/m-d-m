package com.nedaa.mdm.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.nedaa.mdm.model.DeviceRegisterRequest;
import com.nedaa.mdm.model.DeviceRegisterResponse;
import com.nedaa.mdm.model.DeviceStatusResponse;
import com.nedaa.mdm.service.retrofit.ApiService;
import com.nedaa.mdm.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class DeviceManager {
    private static final String PREF_NAME = "mdm_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_IS_REGISTERED = "is_registered";
    private static final String KEY_IS_ENABLED = "is_enabled";
    private static final String KEY_SERVER_URL = "server_url";

    private final SharedPreferences prefs;
    private ApiService deviceApi;

    @Inject
    public DeviceManager(Context context, ApiService deviceApi) {
        this.deviceApi = deviceApi;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Single<DeviceRegisterResponse> registerDevice(String serverUrl, String deviceHash, Context context) {
        prefs.edit().putString(KEY_SERVER_URL, serverUrl).apply();


        DeviceRegisterRequest request = new DeviceRegisterRequest(deviceHash, getDeviceInfo(context));

        return deviceApi.registerDevice(request)
                .doOnSuccess(response -> {
                    prefs.edit()
                            .putString(KEY_DEVICE_ID, response.device_id)
                            .putBoolean(KEY_IS_REGISTERED, true)
                            .putBoolean(KEY_IS_ENABLED, response.is_enabled)
                            .apply();
                });
    }

    public Single<DeviceStatusResponse> checkDeviceStatus() {
        String deviceId = prefs.getString(KEY_DEVICE_ID, "");
        if (deviceId.isEmpty()) {
            return Single.error(new IllegalStateException("Device not registered"));
        }
        return deviceApi.getDeviceStatus(deviceId)
                .doOnSuccess(response ->
                        prefs.edit().putBoolean(KEY_IS_ENABLED, response.is_enabled).apply()
                )
                .onErrorResumeNext(throwable -> Single.error(new IllegalStateException("Device not registered")));
    }

    public boolean isDeviceRegistered(Context context) {
        return getPrefs(context).getBoolean(KEY_IS_REGISTERED, false);
    }

    public boolean isDeviceEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_IS_ENABLED, false);
    }

    private Map<String, String> getDeviceInfo(Context context) {
            Map<String, String> info = new HashMap<>();
            info.put("manufacturer", android.os.Build.MANUFACTURER);
            info.put("model", android.os.Build.MODEL);
            info.put("android_version", android.os.Build.VERSION.RELEASE);
            info.put("sdk_version", String.valueOf(android.os.Build.VERSION.SDK_INT));
            info.put("carrier", NetworkUtils.getCarrierName(context));
            return info;
    }
}