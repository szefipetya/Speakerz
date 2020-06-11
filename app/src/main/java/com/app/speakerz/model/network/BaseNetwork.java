package com.app.speakerz.model.network;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.event.EventHandler;
import com.app.speakerz.model.event.UpdateEventManager;

public class BaseNetwork{
    UpdateEventManager updateEventManagerToModel;

   public void init(){
        updateEventManagerToModel =new UpdateEventManager();
   }
    public void init(EventHandler e){
        init();
        addUpdateEventListener(e);
    }

   public void addUpdateEventListener(EventHandler event){
       updateEventManagerToModel.addListener(event);
       D.log("event added");
   }
    public void start(){
        updateEventManagerToModel.invokeAll("msg");
        D.log("network sent msg");
    }
    public BaseNetwork(){

    }

}
