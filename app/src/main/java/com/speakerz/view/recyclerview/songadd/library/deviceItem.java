package com.speakerz.view.recyclerview.songadd.library;

public class deviceItem {
    private String deviceName;
    private String deviceNickName;

    public deviceItem(String deviceName, String deviceNickName) {
        this.deviceName = deviceName;
        this.deviceNickName = deviceNickName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceNickName() {
        return deviceNickName;
    }
}
