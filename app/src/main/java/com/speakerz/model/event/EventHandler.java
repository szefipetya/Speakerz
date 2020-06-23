package com.speakerz.model.event;

import com.speakerz.model.enums.EVT;

public interface EventHandler {

    public void onUpdate(EVT type, Object o);

    public void onUpdate(EVT type, Object o,Object o2);
    public void onUpdate(EVT type, Object o,Object o2,Object o3);
    public void onUpdate(EVT type, Object o,Object o2,Object o3,Object o4);

}
