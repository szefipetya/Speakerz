package com.speakerz.model.event;

import com.speakerz.model.enums.EVT;

public interface EventHandler {

    void onUpdate(EVT type, Object o);

    void onUpdate(EVT type, Object o, Object o2);
    void onUpdate(EVT type, Object o, Object o2, Object o3);
    void onUpdate(EVT type, Object o, Object o2, Object o3, Object o4);

}
