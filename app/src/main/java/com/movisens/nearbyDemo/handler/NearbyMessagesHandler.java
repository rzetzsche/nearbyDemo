package com.movisens.nearbyDemo.handler;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.movisens.nearbyDemo.UpdateViewCallback;
import com.movisens.nearbyDemo.model.DeviceMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rzetzsche on 07.10.2015.
 */
public class NearbyMessagesHandler implements NearbyHandler {
    public static final String TAG = NearbyMessagesHandler.class.getSimpleName();
    private final Message deviceMessage;
    private GoogleApiClient googleApiClient;
    private MessageListener deviceMessageListener;
    private UpdateViewCallback deviceUiCallback;
    private Set<DeviceMessage> deviceMessages;

    public NearbyMessagesHandler(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
        this.deviceMessages = new HashSet<>();
        deviceMessage = DeviceMessage.newNearbyMessage(InstanceID.getInstance(googleApiClient.getContext()).getId());
        deviceMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                try {
                    DeviceMessage deviceMessage = DeviceMessage.fromNearbyMessage(message);
                    deviceMessages.add(deviceMessage);
                    deviceUiCallback.updateView();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        };
    }

    @Override
    public void stopHandler() {
        stopAdvertising();
        stopDiscovery();
        deviceMessages.clear();
        deviceUiCallback.updateView();
    }

    private void stopDiscovery() {
        Nearby.Messages.unsubscribe(googleApiClient, deviceMessageListener);
    }

    private void stopAdvertising() {
        Nearby.Messages.unpublish(googleApiClient, deviceMessage);
    }

    @Override
    public void startHandler() {
        startAdvertising();
        startDiscovery();
    }

    private void startDiscovery() {
        Nearby.Messages.subscribe(googleApiClient, deviceMessageListener);
    }

    private void startAdvertising() {
        Nearby.Messages.publish(googleApiClient, deviceMessage);
    }

    public void setUpdateViewListener(UpdateViewCallback deviceUiCallback) {
        this.deviceUiCallback = deviceUiCallback;
    }

    public Set<DeviceMessage> getDeviceMessages() {
        return deviceMessages;
    }

    public void removeUpdateViewListener() {
        this.deviceUiCallback = null;
    }
}
