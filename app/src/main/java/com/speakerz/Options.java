package com.speakerz;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.speakerz.model.BaseModel;
import com.speakerz.model.HostModel;
import com.speakerz.R;

public class Options extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
               // Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
               // Act2.putExtra("Hello","Hello World");
              //  startActivity(Act2);
                finish();

            }

        });
    }
}
