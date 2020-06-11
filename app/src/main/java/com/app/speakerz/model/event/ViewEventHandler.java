package com.app.speakerz.model.event;


public interface ViewEventHandler {

   public void toast(String msg);
   public void setText(int componentId, String text);
}
