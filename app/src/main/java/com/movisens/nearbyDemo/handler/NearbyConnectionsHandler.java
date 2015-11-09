package com.movisens.nearbyDemo.handler;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.movisens.nearbyDemo.model.DeviceMessage;
import com.movisens.nearbyDemo.R;
import com.movisens.nearbyDemo.UpdateViewCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rzetzsche on 10.09.2015.
 */
public class NearbyConnectionsHandler implements
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener, NearbyHandler {

    private GoogleApiClient googleApiClient;
    private UpdateViewCallback deviceUiCallback;
    private Map<String, DeviceMessage> deviceMessages;
    private Context context;
    private static String TAG = NearbyConnectionsHandler.class.getSimpleName();


    public NearbyConnectionsHandler(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
        this.context = googleApiClient.getContext();
        this.deviceMessages = new HashMap<>();
    }

    @Override
    public void onConnectionRequest(String s, String s1, String s2, byte[] bytes) {
        //not used for our purpose
    }

    @Override
    public void onEndpointFound(final String endpointId, String deviceId,
                                String serviceId, final String endpointName) {
        deviceMessages.put(endpointId, new DeviceMessage(deviceId, endpointName));
        deviceUiCallback.updateView();
    }

    @Override
    public void onEndpointLost(String endpointId) {
        deviceMessages.remove(endpointId);
        deviceUiCallback.updateView();
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        //not used for our purpose
    }

    @Override
    public void onDisconnected(String s) {
        //not used for our purpose
    }

    private void stopDiscovery() {
        Nearby.Connections.stopDiscovery(googleApiClient, context.getString(R.string.service_id));
    }

    private void startDiscovery() {
        String serviceId = context.getString(R.string.service_id);

        long DISCOVER_TIMEOUT = 0L;

        Nearby.Connections.startDiscovery(googleApiClient, serviceId, DISCOVER_TIMEOUT, this)
                .setResultCallback(new ResultCallback<Status>() {
                                       @Override
                                       public void onResult(Status status) {
                                           if (status.getStatus().isSuccess()) {
                                               Log.d(TAG, status.getStatus() + "");
                                           } else {
                                               Log.e(TAG, status.getStatus() + "");
                                           }
                                       }
                                   }

                );
    }

    private void stopAdvertising() {
        Nearby.Connections.stopAdvertising(googleApiClient);
    }

    private void startAdvertising() {
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(context.getString(R.string.service_id)));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        long NO_TIMEOUT = 0L;

        Nearby.Connections.startAdvertising(googleApiClient, null, appMetadata, NO_TIMEOUT,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, result.getStatus() + "");
                } else {
                    Log.e(TAG, result.getStatus() + "");
                }
            }
        });
    }

    @Override
    public void stopHandler() {
        stopAdvertising();
        stopDiscovery();
        deviceMessages.clear();
        deviceUiCallback.updateView();
    }

    @Override
    public void startHandler() {
        startAdvertising();
        startDiscovery();
    }

    public void setUpdateViewListener(UpdateViewCallback deviceUiCallback) {
        this.deviceUiCallback = deviceUiCallback;
    }

    public Collection<DeviceMessage> getDeviceMessages() {
        return deviceMessages.values();
    }

    public void removeUpdateViewListener() {
        this.deviceUiCallback = null;
    }
}
