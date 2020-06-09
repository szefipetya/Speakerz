package com.app.speakerz.model;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.model.network.BaseNetwork;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.Random;

public abstract class BaseModel {
    public BaseModel(){
        r =new Random();
        codeName= ((Integer) r.nextInt(Integer.MAX_VALUE)).toString();
        }
    public BaseModel(AppCompatActivity a){
        r =new Random();
        codeName= ((Integer) r.nextInt(Integer.MAX_VALUE)).toString();
    }
    BaseNetwork network=null;
    protected Strategy STRATEGY=null;
    private Random r;
    protected final String codeName;

    public abstract void init();
    public abstract void start();

}
