package com.speakerz.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ThreadSafeEvent<E extends EventArgs> {
    private final LinkedList<EventListener<E>> listeners = new LinkedList<>();
    private final Map<Integer,EventListener<E>> indexedListenerMap=new HashMap<>();

    public synchronized void invoke(E args){
        for (EventListener<E> listener: listeners) {
            synchronized (listener) {
                listener.action(args);
            }
        }
    }

    public void addListener(EventListener<E> listener){
        listeners.add(listener);
    }
    public void addListener(EventListener<E> listener,Integer groupId){
        indexedListenerMap.put(groupId,listener);
        listeners.add(listener);
    }
    public void removeAllListeners(){
      indexedListenerMap.clear();
      listeners.clear();
    }
    public void removeListener(EventListener<E> listener){
        listeners.remove(listener);
    }
    public void removeListener(Integer groupId){
        for( Map.Entry ent : indexedListenerMap.entrySet()){
            if(ent.getKey()==groupId){

                indexedListenerMap.remove(ent.getKey());
                listeners.remove(ent.getValue());
            }

        }

    }
}
