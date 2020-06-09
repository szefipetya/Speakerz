package com.app.speakerz.model.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.Task;

import java.util.List;

public abstract class BaseNetwork{
    protected final String SERVICE_ID="SPEAKERZ_NETWORK";
    protected String TAG;
    protected Strategy STRATEGY;
    ConnectionsClient connectionsClient;
    PayloadCallback payloadCallback;
    //public  BaseNetwork(){}
    String codeName;
    protected ConnectionLifecycleCallback connectionLifecycleCallback;
   public abstract void init();
    public abstract void start();
public BaseNetwork(){}
    public BaseNetwork(AppCompatActivity a){
        connectionsClient= Nearby.getConnectionsClient(a);
    }
  public  void setStrategy(Strategy strategy){
     STRATEGY=strategy;
    }


}
