package com.richard.eventbus.framework;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Message {

    private final UUID id;
    private final String objectType;
    private final String simpleObjectType;
    private final Object context;
    private final Instant created;
    private EventHandlerClassInfo eventHandlerClassInfo;


    public Message(UUID id, Instant timestamp, Object context) {
        Objects.requireNonNull(context, "message cannot be null");
        this.context = context;
        this.id = id != null ? id : UUID.randomUUID();
        this.created = timestamp != null ? timestamp : Instant.now();
        this.simpleObjectType = context.getClass().getSimpleName();
        this.objectType = context.getClass().getName();
    }

    public Message(UUID id, Instant timestamp, Object context, EventHandlerClassInfo eventHandlerClassInfo) {
        this(id, timestamp, context);
        this.eventHandlerClassInfo = eventHandlerClassInfo;
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public MessageBuilder toBuilder() {
        return builder()
                .withData(context)
                .withId(id)
                .withTimestamp(created);
    }

    public Message(UUID id, Object context) {
        this(id, Instant.now(), context);
    }

    public Message(Object context) {
        this(UUID.randomUUID(), Instant.now(), context);
    }

    public Object getContext() {
        return context;
    }

    public UUID getId() {
        return id;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getSimpleObjectType() {
        return simpleObjectType;
    }

    public Instant getCreated() {
        return created;
    }

    public EventHandlerClassInfo getEventHandlerClassInfo() {
        return eventHandlerClassInfo;
    }

    public void setSubscriberMethod(EventHandlerClassInfo eventHandlerClassInfo) {
        this.eventHandlerClassInfo = eventHandlerClassInfo;
    }
}
