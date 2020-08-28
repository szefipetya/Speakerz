package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.speakerz.debug.D;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.util.List;

public class MusicPlayer extends Activity {
// TODO: 1 bug az első zene után ha a user huzza végiga seekbart valamiért a 3. indul el nema 2. eztleszámítva error nincsen amennyire én látom,kommetelés és kód rendezés

    int playedSongnum=0;
    int totalTime;
    boolean playSong=false;

    MusicPlayerModel model = null;
    MusicPlayer selfActivity=this;

    // UI elements
    TextView songPlayed;
    Button buttonBack;
    Button buttonPlay;
    Button buttonNext;
    SeekBar seekBar;
    ListView playListView;
    ListView audioListView;
    ArrayAdapter songViewLA =null;
    ArrayAdapter songLA;



    // Event handlers for model (needs to be unbound)
    //   - Listening to playback status change events, must be bound to model
    final EventListener<EventArgs1<Boolean>> playbackStateChangedListener = new EventListener<EventArgs1<Boolean>>() {
        @Override
        public void action(EventArgs1<Boolean> args) {
            if(args.arg1()){
                // configure seekbar when media is started
                //totalTime = model.mediaPlayer.getDuration();
                //seekBar.setMax(totalTime);
            }
            buttonPlay.setText(args.arg1() ? "Stop" : "Start");
            playSong = args.arg1();
        }
    };
    //   - Listening to duration changed events, update seekBar
    final EventListener<EventArgs2<Integer, Integer>> playbackDurationChanged = new EventListener<EventArgs2<Integer, Integer>>() {
        @Override
        public void action(EventArgs2<Integer, Integer> args) {
            int current = args.arg1();
            int total = args.arg2();

            seekBar.setMax(total);
            seekBar.setProgress(current);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // connect ui elements
        songPlayed = findViewById(R.id.playedsong);
        buttonBack = findViewById(R.id.back);
        buttonPlay = findViewById(R.id.play);
        buttonNext = findViewById(R.id.next);
        seekBar = findViewById(R.id.elapsedtime);
        playListView = findViewById(R.id.playlist);
        audioListView = findViewById(R.id.audiolist);


        // Register UI event handlers
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                model.togglePause();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                NextSong();
               // System.out.println(playedSongnum);
            }
        });

        seekBar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(model == null) return;

                    if (fromUser) {
                       // model.mediaPlayer.seekTo(progress);
                        //MusicPlayer.this.seekBar.setProgress(progress);
                    }
                    else{
                        MusicPlayer.this.seekBar.setProgress(progress);

                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            }
        );
    }

    // Called after model binding
    public void initAndStart(){

        // Add songs to model's Song Queue
        /*Field[] fields = R.raw.class.getFields();
        for( int i = 0 ; i < fields.length ; i++){
            if(!model.songNameQueue.contains(fields[i].getName())){
                model.songNameQueue.add(fields[i].getName());
            }
        }*/

        // Connect Song Queue to list view UI component
        songViewLA = new ArrayAdapter<Song>(model.context,  R.layout.list_item,model.getSongQueue());
        playListView.setAdapter(songViewLA);
        songViewLA.setNotifyOnChange(true);


        // All audio file list
        songLA = new ArrayAdapter<Song>(this, R.layout.list_item, model.getAudioList());
        audioListView.setAdapter(songLA);
        songLA.setNotifyOnChange(true);


        // Add onClick handler to song list view
        playListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Retrieve the resource id of the selected song
               // int resID = getResources().getIdentifier(model.songNameQueue.get(i),"raw",getPackageName());
                // Starting song
                D.log("VIEWMODEL START");
                model.startONE(model.context,Uri.parse(model.getSongQueue().get(i).getData()),model.getSongQueue().get(i).getId());
            }
        });

        audioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song song=model.getAudioList().get(i);
                model.addSong(song);
            }
        });



        // starting music if not playing,
        // EZ itt bugos, ha ez bent van akkor automatikusan elkezd lejátszani egy zenét és nem lehet leállítani
        //if(!model.isPlaying()){
            //model.startNext();

        //}
    }



    Integer currentSongId=1;

    public void NextSong(){
        model.startNext();
        songPlayed.setText(model.getCurrentSong().getTitle());

    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;

            // Bind model
            model = localBinder.getService().getModel().getMusicPlayerModel();

            // Register model event handlers
            model.playbackStateChanged.addListener(playbackStateChangedListener);
            model.playbackDurationChanged.addListener(playbackDurationChanged);

            // Start activity
            selfActivity.initAndStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // Remove event handlers
            model.playbackStateChanged.removeListener(playbackStateChangedListener);
            model.playbackDurationChanged.removeListener(playbackDurationChanged);

            // Clear model
            model = null;
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
    }


}



