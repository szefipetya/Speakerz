package com.speakerz.util;

public class EventArgs2<T1, T2> extends EventArgs1<T1> {
    T2 _arg2;
    public EventArgs2(Object _sender, T1 _arg1, T2 _arg2) {
        super(_sender, _arg1);
        this._arg2 = _arg2;
    }

    public  T2 arg2(){
        return _arg2;
    }
}
