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
import com.speakerz.model.HostModel;
import com.speakerz.model.Song;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;

public class Create extends Activity {
    //REQUIRED_BEG MODEL_Declare
    SpeakerzService _service;
    boolean _isBounded;
    boolean _isRegisterRecieverConnected=false;
    ListView lvSongsList;
    ArrayAdapter<Song> songListAdapter=null;

    Integer WirelessStatusChanged_EVT_ID=2;
    Integer TextChanged_EVT_ID=3;
    Integer GroupConnectionChangedEvent_EVT_ID=4;

    private void subscribeModel(final HostModel model) {
        D.log("events subscribed.");
        final Create selfActivity = this;
        songListAdapter=new ArrayAdapter<>(selfActivity.getApplicationContext(), android.R.layout.simple_list_item_1,model.getMusicPlayerModel().getSongQueue());
        songListAdapter.setNotifyOnChange(true);
        lvSongsList.setAdapter(songListAdapter);

        //Basemodel Events
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
        },TextChanged_EVT_ID);



    }

    private void initAndStart() {
        lvSongsList= findViewById(R.id.lv_song_list_test);

        subscribeModel((HostModel) _service.getModel());
        _service.getTextValueStorage().autoConfigureTexts(this);

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
            if(_service.getModel() instanceof  HostModel){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selfActivity.initAndStart();
                    }
                });
            }
            _service.ModelReadyEvent.addListener(new EventListener<BooleanEventArgs>() {
                @Override
                public void action(final BooleanEventArgs args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(args.getValue())
                            selfActivity.initAndStart();

                        }
                    });
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            D.log("service disconnected");
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
        unSubscribeEvents();
        super.onStop();
        unbindService(connection);
        _isBounded = false;

    }

    private void unSubscribeEvents() {
        D.log("events unsubscribed");
        _service.getModel().getNetwork().getReciever().WirelessStatusChanged.removeListener(WirelessStatusChanged_EVT_ID);
        _service.getModel().getNetwork().TextChanged.removeListener(TextChanged_EVT_ID);
        ((HostModel)(_service.getModel())).getNetwork().GroupConnectionChangedEvent.removeListener(GroupConnectionChangedEvent_EVT_ID);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (_service != null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        //a bánat tudja, hogy ez mit csinál, de kell
        /*if (_service != null) {
            if(!_isRegisterRecieverConnected) {
                registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
                _isRegisterRecieverConnected=true;
            }
        }*/
        //D.log("main_onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unSubscribeEvents();
        /*if (_service != null){
                if(_isRegisterRecieverConnected )
                { unregisterReceiver((_service.getModel().getNetwork().getReciever()));}
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);





        Button buttonBack = findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                finish();

            }

        });

        Button buttonMusicPlayer = findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent Act2 = new Intent(getApplicationContext(), PlayerRecyclerActivity.class);
                Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);
            }

        });


    }
    //ITS A FIXME ATTEMT TO FIXME1

}
