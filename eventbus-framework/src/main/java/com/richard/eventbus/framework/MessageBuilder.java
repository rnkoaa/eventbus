package com.richard.eventbus.framework;

import java.time.Instant;
import java.util.UUID;

public class MessageBuilder {
    private UUID id;
    private Object context;
    private Instant timestamp;
    private EventHandlerClassInfo eventHandlerClassInfo;

    public MessageBuilder() {
    }

    public MessageBuilder withData(Object data) {
        this.context = data;
        return this;
    }
    public MessageBuilder withHandlerClassInfo(EventHandlerClassInfo eventHandlerClassInfo) {
        this.eventHandlerClassInfo = eventHandlerClassInfo;
        return this;
    }

    public MessageBuilder withTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public Message build() {
        return new Message(id, timestamp, context);
//        return new Message(id, timestamp, context, eventHandlerClassInfo);
    }

}
