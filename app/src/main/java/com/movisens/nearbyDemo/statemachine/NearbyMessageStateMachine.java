package com.movisens.nearbyDemo.statemachine;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.movisens.nearbyDemo.DeviceMessage;
import com.movisens.nearbyDemo.DeviceUiCallbackInterface;
import com.movisens.nearbyDemo.MainActivity;
import com.movisens.nearbyDemo.NearbyResultCallback;

/**
 * Created by rzetzsche on 10.09.2015.
 */
public class NearbyMessageStateMachine implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    //                          0                   1                       2
    private AbstractState[] states = {new StartState(), new BroadcastState(), new EndState()};
    private int indexState;
    private int currentState;
    private MessageListener deviceMessageListener;
    private Message mDeviceMessage;
    private Context context;
    private DeviceUiCallbackInterface deviceUiCallback;

    public NearbyMessageStateMachine(Context context, final DeviceUiCallbackInterface deviceUiCallback) {
        this.context = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mDeviceMessage = DeviceMessage.newNearbyMessage(InstanceID.getInstance(context).getId());
        deviceMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                try {
                    final DeviceMessage deviceMessage = DeviceMessage.fromNearbyMessage(message);
                    deviceUiCallback.addDevice(deviceMessage);
                } catch (Exception e) {
                    Log.e("MainActivity", e.getMessage());
                }
            }
        };

        indexState = 0;
        states[indexState].enter();
    }

    private void next() {
        if (indexState < states.length) {
            states[currentState].exit();
            states[++currentState].enter();
        }
    }


    class StartState extends AbstractState {
        @Override
        public void connect() {
            super.connect();
            next();
        }

        @Override
        public void enter() {
            super.enter();
            this.connect();
        }
    }

    class EndState extends AbstractState {
        @Override
        public void enter() {
            super.enter();
            this.disconnect();
        }
    }

    class BroadcastState extends AbstractState {
        @Override
        public void enter() {
            super.enter();
            connect();
        }

        @Override
        public void exit() {
            super.exit();
            disconnect();
        }

        @Override
        public void disconnect() {
            super.disconnect();
            Nearby.Messages.unpublish(mGoogleApiClient, mDeviceMessage).setResultCallback(new NearbyResultCallback((MainActivity) deviceUiCallback));
            Nearby.Messages.unsubscribe(mGoogleApiClient, deviceMessageListener).setResultCallback(new NearbyResultCallback((MainActivity) deviceUiCallback));
        }

        @Override
        public void connect() {
            super.connect();
            Nearby.Messages.publish(mGoogleApiClient, mDeviceMessage).setResultCallback(new NearbyResultCallback((MainActivity) deviceUiCallback));
            Nearby.Messages.subscribe(mGoogleApiClient, deviceMessageListener).setResultCallback(new NearbyResultCallback((MainActivity) deviceUiCallback));
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        states[currentState].connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void connect() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        states[currentState].disconnect();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void stopStateMachine() {
        if (states[currentState] instanceof BroadcastState) {
            next();
        }
    }

}
