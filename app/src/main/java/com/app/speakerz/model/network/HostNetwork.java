package com.app.speakerz.model.network;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.model.BaseModel;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public class HostNetwork extends BaseNetwork {
   public HostNetwork(AppCompatActivity a){
       super(a);
      TAG="APP_HostNetwork";

   }
   @Override
  public void start() {
         // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
         connectionsClient.startAdvertising(
                 codeName,SERVICE_ID, connectionLifecycleCallback,
                 new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());
       Log.i(TAG, "Advertising started.");

   }

   public HostNetwork(){}

   @Override
 public  void init() {
      payloadCallback =
              new PayloadCallback() {
                 @Override
                 public void onPayloadReceived(String endpointId, Payload payload) {
                    // opponentChoice = GameChoice.valueOf(new String(payload.asBytes(), UTF_8));
                 }

                 @Override
                 public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {

                 }
              };


      connectionLifecycleCallback =
              new ConnectionLifecycleCallback() {
                 @Override
                 public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    //  opponentName = connectionInfo.getEndpointName();
                 }

                 @Override
                 public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                       Log.i(TAG, "onConnectionResult: connection successful");

                       connectionsClient.stopDiscovery();
                       //  opponentEndpointId = endpointId;
                       // setOpponentName(opponentName);
                       // setStatusText(getString(R.string.status_connected));
                       //    setButtonState(true);
                    } else {
                       Log.i(TAG, "onConnectionResult: connection failed");
                    }
                 }

                 @Override
                 public void onDisconnected(String endpointId) {
                    Log.i(TAG, "onDisconnected: disconnected from the opponent");
                    //TODO: OnConnectionLost
                 }
              };

   }



}
