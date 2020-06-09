package com.app.speakerz.model.network;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DeviceNetwork extends BaseNetwork {
    EndpointDiscoveryCallback endpointDiscoveryCallback;




    @Override
   public void start() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                SERVICE_ID, endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());
        Log.i(TAG, "Discovery started.");

    }

    public DeviceNetwork(AppCompatActivity a) {
        super(a);
        TAG="APP_DeviceNetWork";
    }

    @Override
    public void init() {
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

        endpointDiscoveryCallback=
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                        connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback);
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {}
                };

    }
}
