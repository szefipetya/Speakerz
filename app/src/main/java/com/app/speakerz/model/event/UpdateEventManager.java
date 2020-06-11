package com.app.speakerz.model.event;

import com.app.speakerz.debug.D;

import java.util.ArrayList;
import java.util.List;

public class UpdateEventManager {

    private List<EventHandler> listeners = new ArrayList<EventHandler>();

    public void addListener(EventHandler toAdd) {
        listeners.add(toAdd);
    }

    public void invokeAll() {
        System.out.println("Hello!");

        // Notify everybody that may be interested.
        for (EventHandler hl : listeners){
            hl.onInvoke(null);
            D.log("event occured");
        }

    }

    public void invokeAll(Object o) {
        System.out.println("Hello!");

        // Notify everybody that may be interested.
        for (EventHandler hl : listeners){
            hl.onInvoke(o);
            D.log("event occured o"+o.toString());
        }

    }
}
