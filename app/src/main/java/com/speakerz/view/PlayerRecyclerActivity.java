/**TO DO:**
 * Színeket rendberakni
 *
 * Szebb törlés gomb
 * Sötétíteni a hátteret

 */


package com.speakerz.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowId;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.speakerz.Create;
import com.speakerz.R;
import com.speakerz.SpeakerzService;
import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.HostModel;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameListInitRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.view.components.BottomMusicPlayer;
import com.speakerz.view.components.NameChangeDialog;
import com.speakerz.view.components.TopMenu;
import com.speakerz.view.recyclerview.main.player.RecyclerView_FAB;

import java.net.ConnectException;

public class PlayerRecyclerActivity extends AppCompatActivity implements NameChangeDialog.NameChangeDialogListener{
    RecyclerView_FAB recyclerViewFab;
    TopMenu menu;
    ImageView imageAlbum;
    TextView titleSong;


    public MusicPlayerModel getModel() {
        return model;
    }

    BottomMusicPlayer bottomPlayer;
    PlayerRecyclerActivity self=this;


    MusicPlayerModel model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_real);

        // Configure toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Configure RecyclerView
        recyclerViewFab=new RecyclerView_FAB(self);


        // Configure menu
        menu=new TopMenu(self);

        // Configure BottomPlayer
        bottomPlayer = new BottomMusicPlayer(self);

        imageAlbum = (ImageView) findViewById(R.id.imageAlbum);
        titleSong = (TextView) findViewById(R.id.titleSong);
    }

    boolean doubleBackToExitPressedOnce = false;

    public void onBackPressed(Boolean instant){
        if(instant){
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            _service.onUserDestroy();
            this.finishAffinity();
            return;
        }
        lightOverlay();
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
    public void lightOverlay(){
        ConstraintLayout mConstraintLayout = findViewById(R.id.layout_darker);
        mConstraintLayout.setBackgroundResource(R.color.transparent);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(31, 64, 104)));

    }
    public void darkOverlay(){
        ConstraintLayout mConstraintLayout = findViewById(R.id.layout_darker);
        mConstraintLayout.setBackgroundResource(R.color.darkerBackground);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(12,26,42)));

    }
    private ServiceConnection srvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            // Bind model
            model = localBinder.getService().getModel().getMusicPlayerModel();
            _service=(SpeakerzService)(localBinder.getService());

            // Register model event handlers
            bottomPlayer.initModel(model);
            recyclerViewFab.initModel(model);
                /*
                A Create Activity Megszűnt ezen sorok alább, abból származnak.
                 */


            onServiceReady(localBinder.getService());

            menu.setModel(model.getModel());

            model.songChangedEvent.addListener(songChangedListener);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bottomPlayer.releaseModel();
            recyclerViewFab.releaseModel();

            model = null;
        }
    };
    SpeakerzService _service;
    void onServiceReady(final SpeakerzService _service){
        //már eleve létezik, akkor ez lefut
        if(_service.getModel() instanceof HostModel){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    self.initAndStart(_service);
                }
            });
        }
        //ha a nézet gyorsabb, akkor a service fog szólni, hogy kész a model.
        _service.ModelReadyEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(final BooleanEventArgs args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(args.getValue())
                            self.initAndStart(_service);

                    }
                });
            }
        });
        connectListeners();
    }
    private void connectListeners(){
              _service.getModel().getNetwork().getReciever().ConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                D.log("args:"+args.getValue());
                if (!args.getValue()) {
                    if(!model.isHost()){
                        ((TextView) findViewById(R.id.discover_status)).setText("Disconnected");
                        _service.getTextValueStorage().setTextValue(R.id.discover_status, "Disconnected");

                        goBackToJoinPage();
                    }

                }else{
                    //connected
                    if(!model.isHost()){
                        ((TextView) findViewById(R.id.discover_status)).setText("Connected, Host: "+ _service.getModel().NickNames.get("/192.168.49.1"));
                        _service.getTextValueStorage().setTextValue(R.id.discover_status, "Connected, Host: "+ _service.getModel().NickNames.get("/192.168.49.1"));

                    }

                }

            }
        });
        if(!model.isHost()){
            if(_service.getModel().getNetwork().getReciever().isConnected()){
            String s=_service.getModel().NickNames.get("/192.168.49.1");
            ((TextView) findViewById(R.id.discover_status)).setText("Connected, Host: "+s);
                _service.getTextValueStorage().setTextValue(R.id.discover_status, "Connected, Host: "+s);

            }
        }

    }
    private Integer TextChanged_EVT_ID=10;
    private void subscribeModel(final SpeakerzService _service,final HostModel model) {
        //Basemodel Events
        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if(args.event()== EVT.update_discovery_status){
                    _service.getTextValueStorage().setTextValue(R.id.discover_status, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(self);
                }
                if(args.event()==EVT.h_service_created){
                    _service.getTextValueStorage().setTextValue(R.id.h_service_status, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(self);
                }
            }
        },TextChanged_EVT_ID);

        ImageButton advertiseMe = (ImageButton) findViewById(R.id.btn_advertise);
        advertiseMe.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                model.getNetwork().advertiseMe();
            }

        });
    }



    private void initAndStart(final SpeakerzService _service) {
        subscribeModel(_service,(HostModel) _service.getModel());
        _service.getTextValueStorage().autoConfigureTexts(this);



    }


private void goBackToJoinPage(){
        D.log("backtoJoin");
    if(!model.isHost())
    if(recyclerViewFab.getSongPickerOpen()){
        self.onBackPressed(true);
      finish();
    }else{

         finish();

    }
}

    @Override
    protected void onStop() {
        super.onStop();
        // Bind to LocalService
        unbindService(srvConn);
    }

    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, srvConn, Context.BIND_AUTO_CREATE);
    }

    // David's stuff
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
       return this.menu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.menu.onOptionsItemSelected(item);
    }

    // THis communicates with the NameChange Dialog
    @Override
    public void applyTexts(String username) {
        System.out.println(username+"outside");
        NameItem item = new NameItem(username,"en",model.getModel().deviceID);
        model.getModel().NickName=username;
        model.getModel().editor.putString(model.getModel().myName,username);
        model.getModel().editor.commit();
        System.out.println(model.getModel().sharedpreferences.getString(model.getModel().myName,"nincs Név"));
        PutNameChangeRequestBody body1 = new PutNameChangeRequestBody(null,item);
        model.getModel().NameChangeEvent.invoke(new EventArgs2<Body, TYPE>(null,body1,TYPE.NAME));
    }

    final EventListener<EventArgs1<Song>> songChangedListener = new EventListener<EventArgs1<Song>>() {
        @Override
        public void action(EventArgs1<Song> args) {
            updateSongText(args.arg1());
        }
    };
// TODO : not the played but the first song in the list shows up
    private void updateSongText(final Song song){
        if(song == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                titleSong.setText(song.getTitle());
              //  titleSong.setText(song.getTitle().length()>27? song.getTitle().substring(0,27)+"\n"+song.getTitle().substring(28,song.getTitle().length()):song.getTitle());

                //detailsTV.setText(song.getArtist());
                //totalTime.setText(song.getDuration());
                if(song.getSongCoverArt()!=null)
                    imageAlbum.setImageBitmap(song.getSongCoverArt());
                else{
                    imageAlbum.setImageResource(R.drawable.ic_twotone_music_note_24);
                }
            }
        });
    }
}