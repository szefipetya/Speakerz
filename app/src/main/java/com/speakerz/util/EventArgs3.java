package com.speakerz.util;

public class EventArgs3<T1, T2, T3> extends EventArgs2<T1,T2> {
    T3 _arg3;

    public EventArgs3(Object _sender, T1 _arg1, T2 _arg2,T3 _arg3) {
        super(_sender, _arg1,_arg2);
        this._arg3 = _arg3;
    }

    public  T3 arg3(){
        return _arg3;
    }
}
