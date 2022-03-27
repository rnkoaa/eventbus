package com.richard.eventbus.framework;

public interface LogListener {

    void onEvent(Object object);

    boolean isEnabled();
}