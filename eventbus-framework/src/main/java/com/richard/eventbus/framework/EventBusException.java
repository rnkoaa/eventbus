package com.richard.eventbus.framework;

public class EventBusException extends RuntimeException {

    public EventBusException(String message, Exception e) {
        super(message, e);
    }
}
