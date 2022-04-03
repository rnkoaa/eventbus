package com.richard.eventbus.framework;

import java.lang.reflect.Method;
import java.util.Objects;

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
            throw new IllegalArgumentException("cannot add a null class instance to handler Info for event class" + eventClass);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHandlerClassInfo that = (EventHandlerClassInfo) o;
        return eventClass.equals(that.eventClass)
            && eventListenerClass.equals(that.eventListenerClass)
            && methodName.equals(that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventClass, eventListenerClass, methodName);
    }
}
