package com.speakerz.util;

public class EventArgs {
    protected Object _sender;

    public EventArgs(Object _sender) {
        this._sender = _sender;
    }

    public Object sender() {
        return _sender;
    }
}
