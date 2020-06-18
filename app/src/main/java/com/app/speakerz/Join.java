package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.app.speakerz.App.App;
import com.app.speakerz.debug.D;
import com.app.speakerz.model.event.CommonModel_ViewEventHandler;
import com.example.speakerz.R;

public class Join extends AppCompatActivity{
    //REQUIRED_BEG MODEL
    ListView lvPeersList;
    CommonModel_ViewEventHandler viewEventHandler;
    ArrayAdapter<String> adapter;
    private void initAndStart(){
        App.initModel(false);
        viewEventHandler=new CommonModel_ViewEventHandler(this);
        App.addUpdateEventListener(viewEventHandler);
        App.autoConfigureTexts(this);
        lvPeersList=(ListView) findViewById(R.id.lv_peers);


        App.startModel();

    }
    //REQUIRED_END MODEL


    @Override
    protected void onResume() {
        super.onResume();
        App.autoConfigureTexts(this);
        registerReceiver(App.getWifiBroadcastReciever(),App.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(App.getWifiBroadcastReciever());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //import model recieved from main??

        //REQUIRED_BEG MODEL
        initAndStart();
        //REQUIRED_END MODEL

        Button discover = (Button) findViewById(R.id.discover);
        discover.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
            //everything, that starts with a letter j is attached to only Joiner devices (DeviceModel)
                App.jStartDiscovering(Join.this,lvPeersList);

            }
        });

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
              //  Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
              //  Act2.putExtra("Hello","Hello World");
              //  startActivity(Act2);
                finish();

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MusicPlayer.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

     }
}
