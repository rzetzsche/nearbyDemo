package com.movisens.nearbyDemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rzetzsche on 10.09.2015.
 */
public class NearbyConnectionHandler implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener {

    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private DeviceUiCallbackInterface deviceUiCallback;
    protected static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET};


    public NearbyConnectionHandler(Context context, DeviceUiCallbackInterface deviceUiCallback) {
        this.context = context;
        this.deviceUiCallback = deviceUiCallback;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (isConnectedToNetwork()) {
            startAdvertising();
            startDiscovery();
        } else {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionRequest(String s, String s1, String s2, byte[] bytes) {

    }

    @Override
    public void onEndpointFound(final String endpointId, String deviceId,
                                String serviceId, final String endpointName) {
        deviceUiCallback.addDevice(new DeviceMessage(deviceId, endpointName));
    }

    @Override
    public void onEndpointLost(String s) {

    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {

    }

    @Override
    public void onDisconnected(String s) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    private void stopDiscovery() {
        Nearby.Connections.stopDiscovery(mGoogleApiClient, context.getString(R.string.service_id));
    }

    private void startDiscovery() {
        String serviceId = context.getString(R.string.service_id);

        long DISCOVER_TIMEOUT = 0L;

        Nearby.Connections.startDiscovery(mGoogleApiClient, serviceId, DISCOVER_TIMEOUT, this)
                .setResultCallback(new ResultCallback<Status>() {
                                       @Override
                                       public void onResult(Status status) {
                                           if (status.getStatus().isSuccess()) {
                                               Log.d(getClass().getSimpleName(), status.getStatus() + "");
                                           } else {
                                               Log.e(getClass().getSimpleName(), status.getStatus() + "");
                                           }
                                       }
                                   }

                );
    }

    private void stopAdvertising() {
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
    }

    private void startAdvertising() {
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(context.getString(R.string.service_id)));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        long NO_TIMEOUT = 0L;

        Nearby.Connections.startAdvertising(mGoogleApiClient, null, appMetadata, NO_TIMEOUT,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.d(getClass().getSimpleName(), result.getStatus() + "");
                } else {
                    Log.e(getClass().getSimpleName(), result.getStatus() + "");
                }
            }
        });
    }

    public void stopHandler() {
        stopAdvertising();
        stopDiscovery();
    }

    public void startHandler() {
        startAdvertising();
        startDiscovery();
    }
}
