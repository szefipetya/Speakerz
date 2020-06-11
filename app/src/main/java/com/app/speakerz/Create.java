package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.app.speakerz.model.event.CommonViewEventHandler;
import com.example.speakerz.R;

public class Create extends AppCompatActivity {
    //REQUIRED_BEG MODEL
    HostModel model;
    CommonViewEventHandler viewEventHandler;
    public void setModel(HostModel m){
        model=m;
        viewEventHandler=new CommonViewEventHandler(this);
        model.addUpdateEventListener(viewEventHandler);
    }
    //REQUIRED_END MODEL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

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
