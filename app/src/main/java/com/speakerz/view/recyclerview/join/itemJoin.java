package com.speakerz.view.recyclerview.join;

import android.graphics.Bitmap;

public class itemJoin {
    private String deviceName;
    private String connectingText;

    public itemJoin(String deviceName) {
        this.deviceName = deviceName;
        this.connectingText = "Connecting...";
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getConnectingText() {
        return connectingText;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setConnectingText(String connectingText) {
        this.connectingText = connectingText;
    }
}
