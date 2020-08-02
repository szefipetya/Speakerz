package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.lang.reflect.Field;
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
    ArrayAdapter songLA;



    // Event handlers for model (needs to be unbound)
    //   - Listening to playback status change events, must be bound to model
    final EventListener<EventArgs1<Boolean>> playbackStateChangedListener = new EventListener<EventArgs1<Boolean>>() {
        @Override
        public void action(EventArgs1<Boolean> args) {
            if(args.arg1()){
                // configure seekbar when media is started
                totalTime = model.mediaPlayer.getDuration();
                seekBar.setMax(totalTime);
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
        songPlayed= (TextView) findViewById(R.id.playedsong);
        buttonBack = (Button) findViewById(R.id.back);
        buttonPlay = (Button) findViewById(R.id.play);
        buttonNext = (Button) findViewById(R.id.next);
        seekBar = (SeekBar) findViewById(R.id.elapsedtime);
        playListView = (ListView) findViewById(R.id.playlist);
        audioListView = (ListView) findViewById(R.id.audiolist);

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
                NextSong(playedSongnum,model.songQueue);
               // System.out.println(playedSongnum);
            }
        });

        seekBar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(model == null) return;

                    if (fromUser) {
                        model.mediaPlayer.seekTo(progress);
                        MusicPlayer.this.seekBar.setProgress(progress);
                        if(progress>=totalTime){
                            System.out.println("vege user");
                            //NextSong(playedSongnum,model.songQueue);
                        }
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
        Field[] fields = R.raw.class.getFields();
        for( int i = 0 ; i < fields.length ; i++){
            if(!model.songQueue.contains(fields[i].getName())){
                model.songQueue.add(fields[i].getName());
            }
        }

        // Connect Song Queue to list view UI conponent
        songLA = new ArrayAdapter<String>(this, R.layout.list_item, model.songQueue);
        playListView.setAdapter(songLA);


        // All audio file list
        songLA = new ArrayAdapter<String>(this, R.layout.list_item, model.audioNameList);
        audioListView.setAdapter(songLA);


        // Add onClick handler to song list view
        playListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Retrieve the resource id of the selected song
                int resID = getResources().getIdentifier(model.songQueue.get(i),"raw",getPackageName()); 
                // Starting song
                model.start(i);
            }
        });

        // starting music if not playing
        if(!model.isPlaying()){
            model.startNext();

        }
    }





    public void NextSong(int lastplayedsongnum, List<String> songQueue){
        model.startNext();
        songPlayed.setText(model.SongPlayed);

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



