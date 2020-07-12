package com.speakerz.model.network.Serializable;

import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;

public interface ChannelObject {
    Object getObj();
    TYPE getType();
    SUBTYPE getSubType();
}
