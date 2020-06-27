package com.speakerz.util;

import java.util.LinkedList;

public class Event <E extends EventArgs> {
    private LinkedList<EventListener<E>> listeners = new LinkedList<>();

    public void invoke(E args){
        for (EventListener<E> listener: listeners) {
            listener.action(args);
        }
    }

    public void addListener(EventListener<E> listener){
        listeners.add(listener);
    }

    public void removeListener(EventListener<E> listener){
        listeners.remove(listener);
    }
}
