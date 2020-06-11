package com.app.speakerz.model.event;

import com.app.speakerz.debug.D;

import java.util.ArrayList;
import java.util.List;

public class ViewUpdateEventManager implements ViewEventHandler {
    private List<ViewEventHandler> listeners = new ArrayList<>();

    public void addListener(ViewEventHandler toAdd) {
        listeners.add(toAdd);
    }

    public void toast(String msg){
        for (ViewEventHandler hl : listeners){
            hl.toast(msg);
            D.log("toast event occured");
        }
    }

    @Override
    public void setText(int componentId, String text) {
        for (ViewEventHandler hl : listeners){
            hl.setText(componentId,text);
            D.log("toast event occured");
        }
    }


}
