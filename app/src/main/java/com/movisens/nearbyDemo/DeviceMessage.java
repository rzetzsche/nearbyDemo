package com.movisens.nearbyDemo;

import android.os.Build;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Used to prepare the payload for a
 * {@link com.google.android.gms.nearby.messages.Message Nearby Message}. Adds a unique id (an
 * InstanceID) to the Message payload, which helps Nearby distinguish between multiple devices with
 * the same model name.
 */
public class DeviceMessage {
    private static final Gson gson = new Gson();

    private final String mInstanceId;
    private final String mModelType;

    public String getInstanceId() {
        return mInstanceId;
    }


    /**
     * Builds a new {@link Message} object using a unique identifier.
     */
    public static Message newNearbyMessage(String instanceId) {
        DeviceMessage deviceMessage = new DeviceMessage(instanceId);
        return new Message(gson.toJson(deviceMessage).getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Creates a {@code DeviceMessage} object from the string used to construct the payload to a
     * {@code Nearby} {@code Message}.
     */
    public static DeviceMessage fromNearbyMessage(Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                DeviceMessage.class);
    }

    public DeviceMessage(String instanceId) {
        this.mInstanceId = instanceId;
        this.mModelType = Build.MODEL;
    }

    public DeviceMessage(String instanceId, String mModelType) {
        this.mInstanceId = instanceId;
        this.mModelType = mModelType;
    }

    public String getModelType() {
        return mModelType;
    }

}