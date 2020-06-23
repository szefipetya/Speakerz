package com.speakerz.model.event;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;

import java.util.ArrayList;
import java.util.List;

public class UpdateEventManager {

    private List<EventHandler> listeners = new ArrayList<EventHandler>();

    public void addListener(EventHandler toAdd) {
        listeners.add(toAdd);
    }



    public void updateAll(EVT evt, Object o) {
        // Notify everybody that may be interested.
        for (EventHandler hl : listeners){
            hl.onUpdate(evt,o);
        }

    }
    public void updateAll(EVT evt, Object o,Object o2) {
        // Notify everybody that may be interested.
        for (EventHandler hl : listeners){
            hl.onUpdate(evt,o,o2);
        }

    }
    public void updateAll(EVT evt, Object o,Object o2,Object o3) {
        // Notify everybody that may be interested.
        for (EventHandler hl : listeners){
            hl.onUpdate(evt,o,o2,o3);
        }

    }
    public void updateAll(EVT evt, Object o,Object o2,Object o3,Object o4) {
        // Notify everybody that may be interested.
        for (EventHandler hl : listeners){
            hl.onUpdate(evt,o,o2,o3,o4);
        }

    }
}
