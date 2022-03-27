package com.richard.eventbus.framework;

import com.richard.eventbus.annotation.DeadLetter;

public interface DeadLetterEventListener {

    @DeadLetter
    void onEvent(Object object);
}