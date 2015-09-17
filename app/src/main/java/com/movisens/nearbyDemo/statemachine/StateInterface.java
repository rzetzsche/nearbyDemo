package com.movisens.nearbyDemo.statemachine;

/**
 * Created by rzetzsche on 08.09.2015.
 */
public interface StateInterface {
    void enter();

    void exit();

    String getName();

    void connect();

    void disconnect();
}
