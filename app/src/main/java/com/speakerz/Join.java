package com.speakerz;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.speakerz.App.App;
import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

import java.util.ArrayList;

public class Join extends Activity{
    //REQUIRED_BEG MODEL
    ListView lvPeersList;
    CommonModel_ViewEventHandler viewEventHandler;
    ArrayAdapter<String> adapter;
    private void initAndStart(){
        if(App.getModel()!=null) {
            BaseModel model = App.getModel();
            viewEventHandler = new CommonModel_ViewEventHandler(this);
            subscribeModel(model);
            App.autoConfigureTexts(this);
            lvPeersList = (ListView) findViewById(R.id.lv_peers);
            App.startModel();
            //adapter for Device list
            //FIXME elszáll a program
            adapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_list_item_1, (((DeviceNetwork) App.getModel().getNetwork()).getDeviceNames()));
            lvPeersList.setAdapter(adapter);
        }
        D.log("initAndStart");
    }
    //REQUIRED_END MODEL
    private void subscribeModel(BaseModel model){
        final Activity selfActivity = this;
        // Wireless changed event
        model.getNetwork().getReciever().WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
                App.getTextValueStorage().setTextValue(R.id.wifi_status, args.status() ? "Wifi is on" : "Wifi is off");
                App.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        });

        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if(args.event()== EVT.update_wifi_status){
                    App.getTextValueStorage().setTextValue(R.id.wifi_status,args.text());
                    App.getTextValueStorage().autoConfigureTexts(selfActivity);
                }else if(args.event()==EVT.update_discovery_status){
                     App.getTextValueStorage().setTextValue(R.id.discover_status,args.text());
                     App.getTextValueStorage().autoConfigureTexts(selfActivity);
                }

            }
        });

        model.getNetwork().ListChanged.addListener(new EventListener<EventArgs>() {
            @Override
            public void action(EventArgs args) {
                    adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.autoConfigureTexts(this);
        registerReceiver(App.getWifiBroadcastReciever(),App.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(App.getWifiBroadcastReciever());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //import model recieved from main??

        //REQUIRED_BEG MODEL
        initAndStart();
        //REQUIRED_END MODEL

        Button discover = (Button) findViewById(R.id.discover);
        discover.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
            //everything, that starts with a letter j is attached to only Joiner devices (DeviceModel)
                App.jStartDiscovering();

            }
        });

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
              //  Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
              //  Act2.putExtra("Hello","Hello World");
              //  startActivity(Act2);
                finish();

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MusicPlayer.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

     }
}
