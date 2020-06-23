package com.speakerz.model.network;

import com.speakerz.util.EventArgs;

public class WirelessStatusChangedEventArgs extends EventArgs {
    private boolean _status;

    public WirelessStatusChangedEventArgs(Object _sender, boolean _status) {
        super(_sender);
        this._status = _status;
    }

    public boolean status() {
        return _status;
    }
}
