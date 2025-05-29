package com.nedaa.mdm.model;


import java.util.Map;

public class DeviceRegisterRequest {
    private String device_hash;
    private Map<String, String> device_info;

    public DeviceRegisterRequest(String device_hash, Map<String, String> device_info) {
        this.device_hash = device_hash;
        this.device_info = device_info;
    }

    public String getDevice_hash() {
        return device_hash;
    }

    public Map<String, String> getDevice_info() {
        return device_info;
    }
}
