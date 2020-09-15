package com.speakerz.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.speakerz.R;
import com.speakerz.SpeakerzService;
import com.speakerz.debug.D;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

public class ExtendedPlayerActivity extends Activity {

    ImageView backBtn;
    ImageView albumArt;
    MusicPlayerModel model;

    TextView titleSongTV;
    TextView detailsTV;
    TextView totalTime;
    ImageButton playButton;
    ImageButton prevButton;
    ImageButton nextButton;
    ImageButton shuffleButton;
    ImageButton repeatButton;
    SeekBar seekBar;


    final EventListener<EventArgs2<Integer, Integer>> playbackDurationChanged = new EventListener<EventArgs2<Integer, Integer>>() {
        @Override
        public void action(EventArgs2<Integer, Integer> args) {
            int current = args.arg1();
            int total = args.arg2();

            //seekBar.setMax(total);
            //seekBar.setProgress(current);
            D.log("dur " + current + " " + total );
        }
    };


    final EventListener<EventArgs1<Boolean>> playbackStateChangedListener = new EventListener<EventArgs1<Boolean>>() {
        @Override
        public void action(EventArgs1<Boolean> args) {
            if(args.arg1()){
                // TODO configure seekbar when media is started

                D.log("state " + args.arg1() );
            }
            final boolean _isPlaying = args.arg1();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPlayIcon(_isPlaying);
                }
            });
        }
    };

    final EventListener<EventArgs1<Song>> songChangedListener = new EventListener<EventArgs1<Song>>() {
        @Override
        public void action(EventArgs1<Song> args) {
            updateSongText(args.arg1());
        }
    };

    private ServiceConnection srvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;

            // Bind model
            model = localBinder.getService().getModel().getMusicPlayerModel();
            model.songChangedEvent.addListener(songChangedListener);
            model.playbackStateChanged.addListener(playbackStateChangedListener);
            model.playbackDurationChanged.addListener(playbackDurationChanged);

            setPlayIcon(model.isPlaying());
            updateSongText(model.getCurrentSong());

            // Register model event handlers
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            model.songChangedEvent.removeListener(songChangedListener);
            model.playbackStateChanged.removeListener(playbackStateChangedListener);
            model.playbackDurationChanged.removeListener(playbackDurationChanged);

            model = null;
        }
    };


    private void setPlayIcon(boolean isPlaying){
        playButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_m_playbutton);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_player);


        titleSongTV = findViewById(R.id.textTitle);
        detailsTV  = findViewById(R.id.textArtist);
        playButton = findViewById(R.id.buttonPlay);
        prevButton = findViewById(R.id.buttonPrevious);
        nextButton = findViewById(R.id.buttonNext);
        repeatButton = findViewById(R.id.buttonRepeat);
        shuffleButton = findViewById(R.id.buttonShuffle);
        backBtn=(ImageView)findViewById(R.id.extended_back);
        albumArt = findViewById(R.id.imageAlbum);
        totalTime = findViewById(R.id.textTotalTime);

        prevButton.setImageResource(R.drawable.ic_m_prev);
        nextButton.setImageResource(R.drawable.ic_m_next);
        repeatButton.setImageResource(R.drawable.ic_m_repeat);
        shuffleButton.setImageResource(R.drawable.ic_m_shuffle);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null) model.togglePause();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null) model.startNext();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null) model.startPrev();
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
        backBtn.setOnClickListener(onClickListener);
    }

    private void updateSongText(final Song song){
        if(song == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titleSongTV.setText(song.getTitle());
                detailsTV.setText(song.getArtist());
                totalTime.setText(song.getDuration());
                albumArt.setImageBitmap(song.getSongCoverArt());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Bind to LocalService
        if(model != null) unbindService(srvConn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, srvConn, Context.BIND_AUTO_CREATE);
    }
}