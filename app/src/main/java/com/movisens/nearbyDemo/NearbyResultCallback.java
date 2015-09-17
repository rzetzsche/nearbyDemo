package com.movisens.nearbyDemo;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by rzetzsche on 02.09.2015.
 */
public class NearbyResultCallback implements ResultCallback<Status> {

    private static final int REQUEST_RESOLVE_ERROR = 1;
    private Activity activity;

    public NearbyResultCallback(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i("NearbyResultCallback", status + "");
        } else {
            // Currently, the only resolvable error is that the device is not opted
            // in to Nearby. Starting the resolution displays an opt-in dialog.
            if (status.hasResolution()) {
                try {
                    status.startResolutionForResult(activity,
                            REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    Log.i("NearbyResultCallback", e.toString());
                }
            } else {
                // This will be encountered on initial startup because we do
                // both publish and subscribe together.  So having a toast while
                // resolving dialog is in progress is confusing, so just log it.
                Log.i("NearbyResultCallback", status + "");
            }
        }
    }
}
