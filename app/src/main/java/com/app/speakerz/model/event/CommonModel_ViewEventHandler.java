package com.app.speakerz.model.event;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.debug.D;

public class CommonModel_ViewEventHandler implements Model_ViewEventHandler {
    AppCompatActivity activity;
   public CommonModel_ViewEventHandler(AppCompatActivity activity){
        this.activity=activity;
    }
    @Override
    public void toast(String msg) {
        Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void setText(int componentId,String text){
       if(activity.findViewById(componentId)!=null)
        ((TextView)activity.findViewById(componentId)).setText(text);
       else{
           D.log(activity.getLocalClassName()+" has no "+componentId + "component");
       }
    }
}
