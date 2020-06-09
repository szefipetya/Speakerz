package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.speakerz.model.BaseModel;
import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.example.speakerz.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);
                initModelWithDeviceConfig();
            }

        });

        Button buttonCreate = (Button) findViewById(R.id.create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Create.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);
                initModelWithHostConfig();
            }

        });


        Button buttonOptions = (Button) findViewById(R.id.options);
        buttonOptions.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Options.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

    }
    BaseModel model;
    void initModelWithHostConfig(){
        model=new HostModel(this);
        model.init();
        model.start();
    }
    void initModelWithDeviceConfig(){
        model=new DeviceModel(this);
        model.init();
        model.start();
    }




}
