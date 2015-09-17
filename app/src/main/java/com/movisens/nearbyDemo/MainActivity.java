package com.movisens.nearbyDemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movisens.nearbyDemo.statemachine.NearbyMessageStateMachine;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements DeviceUiCallbackInterface {
    @Bind(R.id.nearbyMessages)
    SwitchCompat nearbyMessages;
    @Bind(R.id.nearbyConnections)
    SwitchCompat nearbyConnections;
    @Bind(R.id.card_layout)
    LinearLayout linearLayout;
    NearbyMessageStateMachine nearbyStateMachine;
    NearbyConnectionHandler nearbyConnectionHandler;
    HashMap<String, String> deviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_activity);
        ButterKnife.bind(this);
        deviceList = new HashMap<>();

        nearbyMessages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nearbyStateMachine = new NearbyMessageStateMachine(MainActivity.this, MainActivity.this);
                } else {
                    nearbyStateMachine.stopStateMachine();
                    nearbyStateMachine = null;
                }
            }
        });


        nearbyConnections.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nearbyConnectionHandler = new NearbyConnectionHandler(MainActivity.this, MainActivity.this);
                } else {
                    nearbyConnectionHandler.stopHandler();
                    nearbyConnectionHandler = null;
                }
            }
        });
    }


    private void addDeviceAsCard(DeviceMessage deviceMessage) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        CardView cardView = (CardView) layoutInflater.inflate(R.layout.cardview_layout, null);
        TextView cardViewTextView = (TextView) cardView.findViewById(R.id.info_text);
        cardViewTextView.setText(deviceMessage.getModelType());
        linearLayout.addView(cardView);
    }

    @Override
    public void addDevice(final DeviceMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!deviceList.containsKey(message.getInstanceId())) {
                    deviceList.put(message.getInstanceId(), message.getModelType());
                    addDeviceAsCard(message);
                }
            }
        });
    }
}
