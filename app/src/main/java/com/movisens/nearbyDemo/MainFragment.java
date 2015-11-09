package com.movisens.nearbyDemo;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by rzetzsche on 09.11.2015.
 */
public class MainFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, UpdateViewCallback {
    @Bind(R.id.nearbyMessages)
    SwitchCompat nearbyMessages;
    @Bind(R.id.nearbyConnections)
    SwitchCompat nearbyConnections;
    @Bind(R.id.card_layout)
    LinearLayout linearLayout;

    private NearbyHandler nearbyMessagesHandler;
    private NearbyHandler nearbyConnectionsHandler;
    private GoogleApiClient googleApiClient;
    private boolean apiClientIsConnected;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient.Builder(getActivity())
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

        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nearby_fragment, container);
        ButterKnife.bind(this, view);

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

        enableUi(apiClientIsConnected);
        updateView();

        return view;
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
                            status.startResolutionForResult(getActivity(), MainActivity.REQUEST_NEARBY_PERMISSIONS);
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
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            CardView cardView = (CardView) layoutInflater.inflate(R.layout.cardview_layout, null);
            TextView cardViewTextView = (TextView) cardView.findViewById(R.id.info_text);
            cardViewTextView.setText(deviceMessage.getModelType());
            linearLayout.addView(cardView);
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
                    apiClientIsConnected = true;
                    enableUi(apiClientIsConnected);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.removeAllViews();
                addDevicesAsCards(nearbyConnectionsHandler.getDeviceMessages());
                addDevicesAsCards(nearbyMessagesHandler.getDeviceMessages());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_NEARBY_PERMISSIONS) {
            if (resultCode == MainActivity.RESULT_OK) {
                enableUi(true);
            } else {
                getPermissionSnackbar().show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (nearbyConnections.isChecked()) {
            nearbyConnectionsHandler.startHandler();
        }
        if (nearbyMessages.isChecked()) {
            nearbyMessagesHandler.startHandler();
        }
    }

    @Override
    public void onStop() {
        nearbyConnectionsHandler.stopHandler();
        nearbyMessagesHandler.stopHandler();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }
}
