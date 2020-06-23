package com.speakerz.model.network;

import com.speakerz.util.EventArgs;

public class TextChangedEventArgs extends EventArgs {
    private String _text;

    public TextChangedEventArgs(Object _sender, String _text) {
        super(_sender);
        this._text = _text;
    }

    public String text() {
        return _text;
    }
}
