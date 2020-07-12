package com.speakerz.model.network.event;

import com.speakerz.model.enums.EVT;
import com.speakerz.util.EventArgs;

public class BooleanEventArgs extends EventArgs {
    public Boolean getValue() {
        return _value;
    }

    public BooleanEventArgs(Object _sender, EVT _evt, Boolean _value) {
        super(_sender);
        this._evt = _evt;
        this._value = _value;
    }
    public BooleanEventArgs(Object _sender, Boolean _value) {
        super(_sender);
        this._evt =EVT.undefined;
        this._value = _value;
    }

    public EVT event() {
        return _evt;
    }

    EVT _evt;
    Boolean _value;

}
