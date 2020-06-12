package com.app.speakerz.App;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.model.BaseModel;
import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.app.speakerz.model.event.CommonViewEventHandler;
import com.app.speakerz.model.event.ViewEventHandler;
import com.app.speakerz.viewModel.TextValueStorage;

public class App extends Application {
   private static BaseModel model;
   private static final TextValueStorage textValueStorage = new TextValueStorage();
    private static WifiManager wifiManager;
    @Override
    public void onCreate() {
        super.onCreate();
        // Your methods here...
    }
    public static void initModel(boolean isHost){
        if (isHost){
            model=new HostModel();
        }else {
            model = new DeviceModel();
        }
        model.init();
        model.setTextValueStorageForViewUpdateEventManager(textValueStorage);
        model.setWifiManager(wifiManager);
        //viewEventhandler.toast("Main : viewEventHandler was null");


    }
    public static void setWifiManager(WifiManager manager){
        wifiManager=manager;
    }
    public static WifiManager getWifiManager(){
        return wifiManager;
    }
    public static void setWifiManagerForModel(WifiManager manager){

    }
    public static void startModel(){
        model.start();
    }
    public static void addUpdateEventListener(ViewEventHandler handler){
        model.addUpdateEventListener(handler);

    }
    public static String getTextFromStorage(Integer id){
        return textValueStorage.getTextValue(id);
    }
    //auto configurates all the textfields if there is any element exists in the storage
    public static void autoConfigureTexts(AppCompatActivity act){
        textValueStorage.autoConfigureTexts(act);
    }
    //REQUIRED
}