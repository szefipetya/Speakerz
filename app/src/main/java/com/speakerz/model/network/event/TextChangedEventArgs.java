package com.speakerz.model.network.event;

import com.speakerz.model.enums.EVT;
import com.speakerz.util.EventArgs;

public class TextChangedEventArgs extends EventArgs {
    private String _text;
    private EVT _evt;
    public TextChangedEventArgs(Object _sender, String _text) {
        super(_sender);
        this._text = _text;
    }
    public TextChangedEventArgs(Object _sender,EVT evt, String _text) {
        super(_sender);
        this._evt=evt;
        this._text = _text;
    }
    public EVT event(){return _evt;}
    public String text() {
        return _text;
    }
}
