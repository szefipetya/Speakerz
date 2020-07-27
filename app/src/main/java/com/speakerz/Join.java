package com.speakerz;

import android.Manifest;
import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.model.event.SongItemEventArgs;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.SongRequestObject;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.model.network.event.channel.ConnectionUpdatedEventArgs;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

import java.io.IOException;

public class Join extends Activity {
    //REQUIRED_BEG MODEL
    SpeakerzService _service;
    boolean _isBounded;
    ListView lvPeersList;
    ListView lvSongsList;
    ArrayAdapter<String> peerListAdapter;
    ArrayAdapter<String> songListAdapter = null;

    boolean _isRegisterRecieverConnected;

    private void cleanTextValues() {
        // _service.getTextValueStorage().cleanValue(R.id.host_name);
    }

    private void initAndStart() {
        cleanTextValues();
        subscribeModel(_service.getModel());
        subscribeServiceEvents();
        lvSongsList = (ListView) findViewById(R.id.lv_song_list_test);
        lvPeersList = (ListView) findViewById(R.id.lv_peers);
        //_service.getModel().start();
        peerListAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_list_item_1, (((DeviceNetwork) _service.getModel().getNetwork()).getDeviceNames()));
        lvPeersList.setAdapter(peerListAdapter);
        //  adapter.notifyDataSetChanged();
        /*registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
        _isRegisterRecieverConnected=true;*/
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

    private void setSensitiveTexts() {
        if (((DeviceNetwork) (_service.getModel().getNetwork())).getHostDevice() != null)
            ((TextView) (findViewById(R.id.host_name))).setText("Connected!\nHost:" + ((DeviceNetwork) (_service.getModel().getNetwork())).getHostDevice().deviceName);
    }

    private void subscribeServiceEvents() {
        _service.PermissionCheckEvent.addListener(new EventListener<PermissionCheckEventArgs>() {
            @Override
            public void action(PermissionCheckEventArgs args) {
                if(args.getReason()== PERM.connectionPermission) {
                    if (ActivityCompat.checkSelfPermission(selfActivity, args.getRequiredPermission()) != args.getSuccessNumber()) {
                        Toast.makeText(selfActivity, "Failure at granting a permission. ", Toast.LENGTH_SHORT).show();
                        //D.log("connection failure: Service");
                        checkPermission(args.getRequiredPermission(), _service.ACCESS_FINE_LOCATION_CODE);
                    } else if (_service.getModel() instanceof DeviceModel) {
                        //critical call. Need to make sure the type before casting...
                        ((DeviceNetwork) (_service.getModel().getNetwork())).connectWithPermissionGranted();
                        //D.log("access granted");
                    }
                }

                if(args.getReason()==PERM.ACCESS_COARSE_LOCATION){
                    checkPermission(args.getRequiredPermission(),_service.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
            }
        });
    }

    //REQUIRED_END MODEL
    private void subscribeModel(BaseModel model) {
        final Activity selfActivity = this;

        //Basemodel events
        model.SongListChangedEvent.addListener(new EventListener<SongItemEventArgs>() {
            @Override
            public void action(SongItemEventArgs args) {
                if (songListAdapter == null) {
                    songListAdapter = new ArrayAdapter<>(selfActivity.getApplicationContext(), android.R.layout.simple_list_item_1, _service.getModel().getSongList());
                    lvSongsList.setAdapter(songListAdapter);
                }

                songListAdapter.notifyDataSetChanged();

            }
        });

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
                    _service.getTextValueStorage().setTextValue(R.id.host_name, ("Connection failure"));
                    ((TextView) findViewById(R.id.host_name)).setText("Connection failure");
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
                }

            }
        });

        model.getNetwork().ConnectionUpdatedEvent.addListener(new EventListener<ConnectionUpdatedEventArgs>() {
            @Override
            public void action(ConnectionUpdatedEventArgs argsd) {
                final ConnectionUpdatedEventArgs args=argsd;
                selfActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String str="Connected!\nHost: "+args.getHostNickName()+"\n"+args.getMessage();
                        _service.getTextValueStorage().setTextValue(R.id.host_name, str);
                        ((TextView) findViewById(R.id.host_name)).setText(str);
                    }
                });
            }
        });


    }


    Join selfActivity = this;
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
        Button discover = (Button) findViewById(R.id.discover);
        discover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((DeviceModel) _service.getModel()).discoverPeers();
            }
        });

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent Act2 = new Intent(getApplicationContext(), MusicPlayer.class);
                // Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);

            }

        });

        ((Button) findViewById(R.id.btn_add_song)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //song adding succesful (connection exists)
                    if (((DeviceNetwork) (_service.getModel().getNetwork())).getClientSocketWrapper().controllerSocket != null) {
                        if (((DeviceNetwork) (_service.getModel().getNetwork())).getClientSocketWrapper().controllerSocket.addNewSong(
                                new SongRequestObject("test_link", "test_title", "test_sender"))) {
                            _service.getModel().getSongList().add("Uj song");
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


    // Function to check and request permission
    public void checkPermission(String permission, int requestCode) {

        // Checking if permission is not granted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&ContextCompat.checkSelfPermission(Join.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            Join.this,
                            new String[]{permission},
                            requestCode);
        } else {
            //permission already granted
            D.log(permission +" already granted.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == _service.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            D.log("ACCESS_COARSE_LOCATION Permission granted.");
        }

    }


}
