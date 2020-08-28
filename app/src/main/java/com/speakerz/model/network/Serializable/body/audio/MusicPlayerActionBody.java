package com.speakerz.model.network.Serializable.body.audio;

import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class MusicPlayerActionBody extends Body implements Serializable {
    public MusicPlayerActionBody(  MP_EVT evt,Object object){
        this.evt=evt;
        this.obj=object;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_ACTION_EVT;
    }

    @Override
    public void setContent(Object obj1) {
        this.obj= obj1;
    }

    MP_EVT evt;
    Object obj;

    @Override
    public Object getContent() {
        return obj;
    }

    public MP_EVT getEvt() {
        return evt;
    }


}
