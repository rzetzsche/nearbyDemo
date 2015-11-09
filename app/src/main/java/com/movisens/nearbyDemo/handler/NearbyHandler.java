package com.movisens.nearbyDemo.handler;

import com.movisens.nearbyDemo.UpdateViewCallback;
import com.movisens.nearbyDemo.model.DeviceMessage;

import java.util.Collection;

/**
 * Created by rzetzsche on 07.10.2015.
 */
public interface NearbyHandler {
    void stopHandler();

    void startHandler();

    Collection<DeviceMessage> getDeviceMessages();

    void setUpdateViewListener(UpdateViewCallback deviceUiCallback);

    void removeUpdateViewListener();

}
