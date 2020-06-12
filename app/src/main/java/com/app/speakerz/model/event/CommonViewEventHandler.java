package com.app.speakerz.model.event;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CommonViewEventHandler implements ViewEventHandler {
    AppCompatActivity activity;
   public CommonViewEventHandler(AppCompatActivity activity){
        this.activity=activity;
    }
    @Override
    public void toast(String msg) {
        Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void setText(int componentId,String text){
        ((TextView)activity.findViewById(componentId)).setText(text);
    }
}
