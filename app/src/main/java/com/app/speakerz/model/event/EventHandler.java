package com.app.speakerz.model.event;

import com.app.speakerz.model.enums.EVT;

public interface EventHandler {

    public void onUpdate(EVT type, Object o);

    public void onUpdate(EVT type, Object o,Object o2);
    public void onUpdate(EVT type, Object o,Object o2,Object o3);
    public void onUpdate(EVT type, Object o,Object o2,Object o3,Object o4);

}
