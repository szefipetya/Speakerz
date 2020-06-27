package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.DeviceModel;
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
    SpeakerzService _service;
    boolean _isBounded;
    ListView lvPeersList;
    CommonModel_ViewEventHandler viewEventHandler;
    ArrayAdapter<String> adapter;
    private void initAndStart(){

            subscribeModel(_service.getModel());
            _service.getTextValueStorage().autoConfigureTexts(this);
            lvPeersList = (ListView) findViewById(R.id.lv_peers);
            _service.getModel().start();
            adapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_list_item_1, (((DeviceNetwork) _service.getModel().getNetwork()).getDeviceNames()));
            lvPeersList.setAdapter(adapter);
             registerReceiver(_service.getModel().getNetwork().getReciever(),_service.getModel().getNetwork().getIntentFilter());


    }
    //REQUIRED_END MODEL
    private void subscribeModel(BaseModel model){
        final Activity selfActivity = this;
        // Wireless changed event
        model.getNetwork().getReciever().WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
                _service.getTextValueStorage().setTextValue(R.id.wifi_status, args.status() ? "Wifi is on" : "Wifi is off");
                _service.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        });

        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if(args.event()== EVT.update_wifi_status){
                    _service.getTextValueStorage().setTextValue(R.id.wifi_status,args.text());
                    _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                }else if(args.event()==EVT.update_discovery_status){
                    _service.getTextValueStorage().setTextValue(R.id.discover_status,args.text());
                    _service.getTextValueStorage().autoConfigureTexts(selfActivity);
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



    Join selfActivity=this;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            _service =  localBinder.getService();
            _isBounded = true;
            selfActivity.initAndStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBounded = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
             unbindService(connection);
        _isBounded = false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(_service!=null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        //a bánat tudja, hogy ez mit csinál, de kell
        if(_service!=null)
            registerReceiver(_service.getModel().getNetwork().getReciever(),_service.getModel().getNetwork().getIntentFilter());
        D.log("main_onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(_service!=null && _isBounded)
            unregisterReceiver((_service.getModel().getNetwork().getReciever()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        Button discover = (Button) findViewById(R.id.discover);
        discover.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
            //everything, that starts with a letter j is attached to only Joiner devices (DeviceModel)
                ((DeviceModel)_service.getModel()).discoverPeers();

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
