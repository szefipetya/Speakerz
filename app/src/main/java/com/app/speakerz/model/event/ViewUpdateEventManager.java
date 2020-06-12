package com.app.speakerz.model.event;

import com.app.speakerz.debug.D;
import com.app.speakerz.viewModel.TextValueStorage;

import java.util.ArrayList;
import java.util.List;

public class ViewUpdateEventManager implements ViewEventHandler {
    private List<ViewEventHandler> listeners = new ArrayList<>();
    TextValueStorage valueStorage;
    public void setValueStorage(TextValueStorage storage){
        valueStorage=storage;
    }
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
        valueStorage.setTextValue(componentId,text);
        for (ViewEventHandler hl : listeners){
            hl.setText(componentId,text);
            D.log("toast event occured");
        }
    }


}
