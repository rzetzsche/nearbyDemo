package com.movisens.nearbyDemo;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.movisens.nearbyDemo.handler.NearbyConnectionsHandler;
import com.movisens.nearbyDemo.handler.NearbyHandler;
import com.movisens.nearbyDemo.handler.NearbyMessagesHandler;
import com.movisens.nearbyDemo.model.DeviceMessage;

import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, UpdateViewCallback {
    @Bind(R.id.nearbyMessages)
    SwitchCompat nearbyMessages;
    @Bind(R.id.nearbyConnections)
    SwitchCompat nearbyConnections;
    @Bind(R.id.card_layout)
    LinearLayout linearLayout;

    private NearbyHandler nearbyMessagesHandler;
    private NearbyHandler nearbyConnectionsHandler;
    private GoogleApiClient googleApiClient;

    public static final int REQUEST_NEARBY_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_activity);
        ButterKnife.bind(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .addApi(Nearby.MESSAGES_API)
                .build();
        googleApiClient.connect();

        nearbyMessagesHandler = new NearbyMessagesHandler(googleApiClient);
        nearbyConnectionsHandler = new NearbyConnectionsHandler(googleApiClient);

        nearbyMessagesHandler.setUpdateViewListener(this);
        nearbyConnectionsHandler.setUpdateViewListener(this);

        nearbyMessages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nearbyMessagesHandler.startHandler();
                } else {
                    nearbyMessagesHandler.stopHandler();
                }
            }
        });

        nearbyConnections.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nearbyConnectionsHandler.startHandler();
                } else {
                    nearbyConnectionsHandler.stopHandler();
                }
            }
        });

        enableUi(false);
    }

    private Snackbar getPermissionSnackbar() {
        Snackbar permissionSnackbar = Snackbar.make(linearLayout, "No Permissions for Nearby APIs!", Snackbar.LENGTH_INDEFINITE);
        permissionSnackbar.setAction("Grant", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nearby.Messages.getPermissionStatus(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        try {
                            status.startResolutionForResult(MainActivity.this, MainActivity.REQUEST_NEARBY_PERMISSIONS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return permissionSnackbar;
    }


    private void addDevicesAsCards(Collection<DeviceMessage> deviceMessages) {
        for (DeviceMessage deviceMessage :
                deviceMessages) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            CardView cardView = (CardView) layoutInflater.inflate(R.layout.cardview_layout, null);
            TextView cardViewTextView = (TextView) cardView.findViewById(R.id.info_text);
            cardViewTextView.setText(deviceMessage.getModelType());
            linearLayout.addView(cardView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEARBY_PERMISSIONS) {
            if (resultCode == RESULT_OK) {
                enableUi(true);
            } else {
                getPermissionSnackbar().show();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Nearby.Messages.getPermissionStatus(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
                    getPermissionSnackbar().show();
                } else {
                    enableUi(true);
                }
            }
        });
    }

    private void enableUi(boolean enable) {
        nearbyMessages.setEnabled(enable);
        nearbyConnections.setEnabled(enable);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void updateView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.removeAllViews();
                addDevicesAsCards(nearbyConnectionsHandler.getDeviceMessages());
                addDevicesAsCards(nearbyMessagesHandler.getDeviceMessages());
            }
        });
    }
}
