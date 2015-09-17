package com.movisens.nearbyDemo.statemachine;

import android.util.Log;

/**
 * Created by rzetzsche on 08.09.2015.
 */
public class AbstractState implements StateInterface {
    @Override
    public void enter() {
        Log.d(getName(), "enter");
    }

    @Override
    public void exit() {
        Log.d(getName(), "exit");
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }
}
