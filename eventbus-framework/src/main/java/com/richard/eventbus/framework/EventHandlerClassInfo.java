package com.richard.eventbus.framework;

import java.lang.reflect.Method;

public record EventHandlerClassInfo(Class<?> eventClass,
                                    Class<?> eventListenerClass,
                                    String methodName,
                                    Object eventListenerInstance,
                                    Method handlerMethod
) {

    public EventHandlerClassInfo(Class<?> eventClass, Class<?> eventListenerClass) {
        this(eventClass, eventListenerClass, "", null, null);
    }

    public EventHandlerClassInfo(Class<?> eventClass, Class<?> eventListenerClass, String methodName) {
        this(eventClass, eventListenerClass, methodName, null, null);
    }

    public EventHandlerClassInfo withEventListenerInstance(Object eventListenerInstance) {
        if (eventListenerInstance == null) {
            throw new IllegalArgumentException("cannot add a null class instance to handler Info");
        }
        return new EventHandlerClassInfo(this.eventClass, this.eventListenerClass,
                this.methodName,
                eventListenerInstance,
                this.handlerMethod);
    }

    public EventHandlerClassInfo withHandlerMethod(Method handlerMethod) {
        if (eventListenerInstance == null) {
            throw new IllegalArgumentException("cannot add a null class instance to handler Info");
        }
        return new EventHandlerClassInfo(
                this.eventClass,
                this.eventListenerClass,
                this.methodName,
                this.eventListenerInstance,
                handlerMethod
        );
    }

    boolean valid() {
        return this.eventClass != null && this.eventListenerClass != null;
    }
}
