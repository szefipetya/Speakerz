package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.speakerz.model.HostModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.HostNetwork;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.EventListener;

public class Create extends Activity {
    //REQUIRED_BEG MODEL_Declare
    SpeakerzService _service;
    boolean _isBounded;

    private void subscribeModel(HostModel model) {
        final Activity selfActivity = this;
        // Wireless changed event
        model.getNetwork().getReciever().WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
                _service.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        });

        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if(args.event()==EVT.update_discovery_status){
                _service.getTextValueStorage().setTextValue(R.id.discover_status, args.text());
                _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                }
                if(args.event()==EVT.h_service_created){
                    _service.getTextValueStorage().setTextValue(R.id.h_service_status, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                }
            }
        });
        model.getNetwork().GroupConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                if (args.event() == EVT.host_group_creation) {
                    if (args.getValue())
                        ((TextView) findViewById(R.id.h_group_status)).setText("Group created");
                    else {
                        ((TextView) findViewById(R.id.h_group_status)).setText("Group creation Failed");
                    }
                }

            }
        });

    }

    private void initAndStart() {
        subscribeModel((HostModel) _service.getModel());
        _service.getTextValueStorage().autoConfigureTexts(this);
        _service.getModel().start();
        registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
    }
    //REQUIRED_END MODEL_Declare

    Create selfActivity = this;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            _service = localBinder.getService();
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
        if (_service != null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        //a bánat tudja, hogy ez mit csinál, de kell
        if (_service != null)
            registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
        //D.log("main_onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_service != null)
            unregisterReceiver((_service.getModel().getNetwork().getReciever()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
                //  Act2.putExtra("Hello","Hello World");
                //  startActivity(Act2);
                finish();

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent Act2 = new Intent(getApplicationContext(), MusicPlayer.class);
                Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);

            }

        });

        Button startSession = (Button) findViewById(R.id.btn_start_session);
        startSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((HostNetwork) (_service.getModel().getNetwork())).startAdvertising();

            }

        });

    }
}
