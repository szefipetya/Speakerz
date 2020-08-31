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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.HostModel;
import com.speakerz.model.Song;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import ealvatag.audio.exceptions.CannotReadException;

public class Create extends Activity {
    //REQUIRED_BEG MODEL_Declare
    SpeakerzService _service;
    boolean _isBounded;
    boolean _isRegisterRecieverConnected=false;
    ListView lvSongsList;
    ArrayAdapter<Song> songListAdapter=null;

    Integer SongListChangedEvent_EVT_ID=1;
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
        model.ExceptionEvent.addListener(new EventListener<EventArgs1<Exception>>() {
            @Override
            public void action(final EventArgs1<Exception> args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(args.arg1() instanceof CannotReadException)
                        Toast.makeText(selfActivity,"Not supported format",Toast.LENGTH_SHORT).show();
                        else{
                            Toast.makeText(selfActivity,args.arg1().getMessage(),Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });
        _service.getModel().SongQueueUpdatedEvent.addListener(new EventListener<EventArgs>() {
            @Override
            public void action(final EventArgs args) {
                D.log("ServerUI: got an object. ");
                if(songListAdapter!=null) {
                    //must run on Ui thread:
                    D.log("SonglistAdapter is not null. ");

                    Runnable run=new Runnable() {
                        @Override
                        public void run() {
                            //TEMPORARY FIXME 1 vvv
                            // ^^^ FIXME 1 ^^^
                            synchronized(songListAdapter){
                                songListAdapter.notifyDataSetChanged();
                                songListAdapter.notify();
                            }
                            lvSongsList.invalidateViews();
                            D.log("dataset updated.");
                            D.log("size: "+_service.getModel().getMusicPlayerModel().getSongQueue().size());
                        }
                    };
                    RunnableFuture<Void> task = new FutureTask<>(run, null);
                    selfActivity.runOnUiThread(task);
                    try {
                        task.get(); // this will block until Runnable completes
                    } catch (InterruptedException | ExecutionException e) {
                        D.log("UiRefresh exception. "+e.getMessage());
                        // handle exception
                    }
                }
            }
        },SongListChangedEvent_EVT_ID);

        // Wireless changed event
        model.getNetwork().getReciever().WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
                _service.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        },WirelessStatusChanged_EVT_ID);


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
        },GroupConnectionChangedEvent_EVT_ID);

    }

    private void initAndStart() {
        lvSongsList=(ListView) findViewById(R.id.lv_song_list_test);


            _service.getModel().setAreUiEventsSubscribed(true);
        subscribeModel((HostModel) _service.getModel());
        _service.getTextValueStorage().autoConfigureTexts(this);
        //_service.getModel().start();

        //registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
        //_isRegisterRecieverConnected=true;
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


        /*Button nameChange = (Button) findViewById(R.id.temp);
        nameChange.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //TODO: change "en" to sender put this into recycleview
                NameItem item = new NameItem(editName.getText().toString(),"en",_service.getModel().deviceID);
                PutNameChangeRequestBody body1 = new PutNameChangeRequestBody(null,item);
                _service.getModel().NameChangeEvent.invoke(new EventArgs1<Body>(null,body1));
            }

        });*/




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
                Intent Act2 = new Intent(getApplicationContext(), PlayerRecyclerActivity.class);
                Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);
            }

        });

        Button startSession = (Button) findViewById(R.id.btn_start_session);
        startSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((HostModel) (_service.getModel())).startAdvertising();

            }

        });

    }
    //ITS A FIXME ATTEMT TO FIXME1

}
