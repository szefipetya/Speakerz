package com.speakerz.util;

public class EventArgs1<T1> extends EventArgs {
    T1 _arg1;
    public EventArgs1(Object _sender, T1 _arg1) {
        super(_sender);
        this._arg1 = _arg1;
    }

    public  T1 arg1(){
        return _arg1;
    }
}
