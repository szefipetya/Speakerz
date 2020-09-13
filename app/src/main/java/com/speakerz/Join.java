package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.speakerz.debug.D;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.Song;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.ServerInfo;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.WifiP2pService;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Join extends Activity {
    //REQUIRED_BEG MODEL
    SpeakerzService _service;
    boolean _isBounded;
    ListView lvPeersList;
    ListView lvSongsList;
    private ArrayAdapter<WifiP2pService> peerListAdapter;
    private ArrayAdapter<Song> songListAdapter;

    private void cleanTextValues() {
    }

    private void initAndStart() {
        D.log("initandstart");
        cleanTextValues();

            subscribeModel((DeviceModel) _service.getModel());
            subscribeServiceEvents();
        lvSongsList = findViewById(R.id.lv_song_list_test);
        lvPeersList = findViewById(R.id.lv_peers);

        songListAdapter = new ArrayAdapter<>(selfActivity.getApplicationContext(), android.R.layout.simple_list_item_1, _service.getModel().getMusicPlayerModel().getSongQueue());
        lvSongsList.setAdapter(songListAdapter);

       peerListAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1, (((DeviceNetwork) _service.getModel().getNetwork()).serviceDevices));
        lvPeersList.setAdapter(peerListAdapter);

        //set up the listView's onclick, so clients can connect to hosts by klicking on a device in a listview
        lvPeersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((DeviceNetwork) _service.getModel().getNetwork()).connect(i);
            }
        });

        _service.getTextValueStorage().autoConfigureTexts(this);
        //setSensitiveTexts();
    }

   /* private void setSensitiveTexts() {
        if (((DeviceNetwork) (_service.getModel().getNetwork())).getHostDevice() != null)
            ((TextView) (findViewById(R.id.host_name))).setText("Connected!\nHost:" + ((DeviceNetwork) (_service.getModel().getNetwork())).getHostDevice().deviceName);
    }*/


    private void subscribeServiceEvents() {

    }

    //REQUIRED_END MODEL
    private void subscribeModel(DeviceModel model) {
        final Activity selfActivity = this;

        //Basemodel events
        model.ExceptionEvent.addListener(new EventListener<EventArgs1<Exception>>() {
            @Override
            public void action(EventArgs1<Exception> args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        songListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        model.SongQueueUpdatedEvent.addListener(new EventListener<EventArgs>() {
            @Override
            public void action(EventArgs args) {

                Runnable run=new Runnable() {
                    @Override
                    public void run() {
                        //TEMPORARY FIXME 1 vvv
                        // ^^^ FIXME 1 ^^^
                        synchronized (songListAdapter) {
                            songListAdapter.notifyDataSetChanged();
                        }

                        lvSongsList.invalidateViews();
                        D.log("songList updated, size: "+_service.getModel().getMusicPlayerModel().getSongQueue().size());
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
        });

        // Wireless changed event



        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if (args.event() == EVT.update_wifi_status) {
                    _service.getTextValueStorage().setTextValue(R.id.wifi_status, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                } else if (args.event() == EVT.update_discovery_status) {
                    _service.getTextValueStorage().setTextValue(R.id.discover_status, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                } else if (args.event() == EVT.update_host_name) {
                    //get the deviceName of the new host
                    _service.getTextValueStorage().setTextValue(R.id.host_name, ("Connecting to "+args.text()+" ..."));
                    ((TextView) findViewById(R.id.host_name)).setText("Connecting to "+args.text()+" ...");
                } else if (args.event() == EVT.update_host_name_failed) {
                    _service.getTextValueStorage().setTextValue(R.id.host_name, ("Connection failure"+args.text()));
                    ((TextView) findViewById(R.id.host_name)).setText("Connection failure"+args.text());
                }

            }
        });

        model.getNetwork().ListChanged.addListener(new EventListener<EventArgs>() {
            @Override
            public void action(EventArgs args) {

                peerListAdapter.notifyDataSetChanged();
            }
        });

        model.getNetwork().getReciever().ConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                if (!args.getValue()) {
                    D.log("recieved disconnect");
                    _service.getTextValueStorage().setTextValue(R.id.host_name, ("Disconnected"));
                    ((TextView) findViewById(R.id.host_name)).setText("Disconnected");
                    isPlayerInstanceAlive=false;
                }else{
                    //connected
                    D.log("CONNECTED YEEY");
                    if(!isPlayerInstanceAlive) {
                        Intent Act2 = new Intent(getApplicationContext(), PlayerRecyclerActivity.class);
                        startActivity(Act2);
                        isPlayerInstanceAlive=true;
                    }
                }

            }
        });



         model.MetaInfoReceivedEvent.addListener(new EventListener<EventArgs1<Body>>() {
            @Override
            public void action(EventArgs1<Body> args) {
                if(args.arg1().SUBTYPE()== SUBTYPE.META_GET_SRV_INFO) {
                 final ServerInfo info = (ServerInfo) args.arg1().getContent();
                    selfActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String str = "Connected!\nHost: " + info.getHostName() + "\n" + info.getMessage();
                            _service.getTextValueStorage().setTextValue(R.id.host_name, str);
                            ((TextView) findViewById(R.id.host_name)).setText(str);
                        }
                    });
                }
            }
        });


    }
    Boolean isPlayerInstanceAlive=false;

    Join selfActivity = this;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            _service = localBinder.getService();
            _isBounded = true;
            if(_service.getModel() instanceof DeviceModel){
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
                            if(!args.getValue())
                            selfActivity.initAndStart();
                        }
                    });
                }
            });
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
        /*if (_service != null){

            registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
            _isRegisterRecieverConnected = true;
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (_service != null && _isBounded && _isRegisterRecieverConnected) {
            unregisterReceiver((_service.getModel().getNetwork().getReciever()));
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        Button discover = findViewById(R.id.discover);
        discover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((DeviceModel) _service.getModel()).discoverPeers();
            }
        });

        Button buttonBack = findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }

        });

        Button buttonMusicPlayer = findViewById(R.id.Musicplayer);

       /* buttonMusicPlayer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent Act2 = new Intent(getApplicationContext(), PlayerRecyclerActivity.class);
                // Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);

            }

        });*/

        findViewById(R.id.btn_add_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //song adding succesful (connection exists)
                    if (((DeviceNetwork) (_service.getModel().getNetwork())).getClientSocketWrapper().controllerSocket != null) {
                        if (((DeviceNetwork) (_service.getModel().getNetwork())).getClientSocketWrapper().controllerSocket.send(
                                // TODO replace "alma" to UUID
                            new ChannelObject(new PutSongRequestBody(new Song("","Title","album","artist", "alma",null,null)), TYPE.MP)
                           )) {
                            Toast.makeText(selfActivity, "Song request sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(selfActivity, "Error: no connections available. ", Toast.LENGTH_SHORT).show();

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(selfActivity, "Could not add song. reason:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    //PERMISSIONS





}
