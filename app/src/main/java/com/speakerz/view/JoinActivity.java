package com.speakerz.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;
import com.speakerz.SpeakerzService;
import com.speakerz.debug.D;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.view.recyclerview.RecyclerItemClickListener;
import com.speakerz.view.recyclerview.join.AdapterJoin;

public class JoinActivity extends Activity {

    private RecyclerView recyclerViewJoin;

    private AdapterJoin adapterJoin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
     //   final View mView = inflater.inflate(R.layout.layout_join, container, false);
        super.onCreate(savedInstanceState);

setContentView(R.layout.layout_join);
        Button discover = (Button) findViewById(R.id.button_discover);
        discover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((DeviceModel) _service.getModel()).discoverPeers();
            }
        });

        ImageButton buttonBack = (ImageButton) findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }

        });

    }

DeviceModel model;
    SpeakerzService _service;
    boolean _isBounded;


    private void cleanTextValues() {
        // _service.getTextValueStorage().cleanValue(R.id.host_name);
    }

    private void initAndStart() {
        D.log("initandstart");
        cleanTextValues();

        subscribeModel((DeviceModel) _service.getModel());
        subscribeServiceEvents();

        recyclerViewJoin =findViewById(R.id.recyclerView_join);
        recyclerViewJoin.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(self, LinearLayoutManager.VERTICAL, false);
        recyclerViewJoin.setLayoutManager(mLinearLayoutManager);




        adapterJoin = new AdapterJoin((((DeviceNetwork) _service.getModel().getNetwork()).serviceDevices),this);
        recyclerViewJoin.setAdapter(adapterJoin);


        //peerListAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_list_item_1, (((DeviceNetwork) _service.getModel().getNetwork()).getDeviceNames()));


        //set up the listView's onclick, so clients can connect to hosts by klicking on a device in a listview
        recyclerViewJoin.addOnItemTouchListener(
                new RecyclerItemClickListener(self, recyclerViewJoin, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(position>=0){
                            adapterJoin.selectedRowIndex=position;
                            ((DeviceNetwork) _service.getModel().getNetwork()).connect(position);

                        }

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
        //setSensitiveTexts();
    }



    private void subscribeServiceEvents() {

    }

    //REQUIRED_END MODEL
    private void subscribeModel(DeviceModel model) {


        //Basemodel events


        // Wireless changed event

        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if (args.event() == EVT.update_wifi_status) {
                   //_service.getTextValueStorage().setTextValue(R.id.wifi_status, args.text());
                  //  _service.getTextValueStorage().autoConfigureTexts(self.getActivity());
                } else if (args.event() == EVT.update_discovery_status) {
                    _service.getTextValueStorage().setTextValue(R.id.button_discover, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(self);
                }

            }
        });
        model.getNetwork().ConnectionStatusChangedEvent.addListener(new EventListener<EventArgs2<Integer, Integer>>() {
            @Override
            public void action(EventArgs2<Integer, Integer> args) {

            }
        });

        model.getNetwork().ListChanged.addListener(new EventListener<EventArgs>() {
            @Override
            public void action(EventArgs args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      adapterJoin.notifyDataSetChanged();
                    }
                });
            }
        });

        model.getNetwork().getReciever().ConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                if (!args.getValue()) {
                    D.log("recieved disconnect");
                   /* _service.getTextValueStorage().setTextValue(R.id.host_name, ("Disconnected"));
                    ((TextView) findViewById(R.id.host_name)).setText("Disconnected");*/
                    isPlayerInstanceAlive=false;
                }else{
                    //connected
                    D.log("CONNECTED YEEY");
                    if(!isPlayerInstanceAlive) {
                        Intent Act2 = new Intent(self, PlayerRecyclerActivity.class);
                        startActivity(Act2);

                        isPlayerInstanceAlive=true;
                    }
                }

            }
        });



       /* model.MetaInfoReceivedEvent.addListener(new EventListener<EventArgs1<Body>>() {
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
        });*/


    }
    Boolean isPlayerInstanceAlive=false;
    JoinActivity self=this;
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
                        self.initAndStart();
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
                                self.initAndStart();
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
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(self, SpeakerzService.class);
        self.bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();

        self.unbindService(connection);
        _isBounded = false;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }


}