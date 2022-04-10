package com.richard.eventbus.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public record MessageProcessingWorker(BlockingQueue<Message> queue, BusConfig busConfig) implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Message item = queue.poll(busConfig.pollTimeout(), TimeUnit.MILLISECONDS);
                if (item != null) {
                    Object event = item.getContext();
                    System.out.println("Got event" + event);
                    if (event instanceof PoisonPill) {
                        return;
                    }
                    EventHandlerClassInfo eventHandlerClassInfo = item.getEventHandlerClassInfo();
                    Method method = eventHandlerClassInfo.handlerMethod();
                    Object eventHandler = eventHandlerClassInfo.eventListenerClass();
                    try {
                        method.invoke(eventHandler, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {

                        // should either retry or post a special message with the exception  to be handled by exceptionHandler
                        throw new EventBusException(
                                String.format("Failed to dispatch event %s@%d to listener %s@%d",
                                        event.getClass().getName(),
                                        System.identityHashCode(event),
                                        eventHandler.getClass().getName(),
                                        System.identityHashCode(eventHandler)),
                                e);
                    }
                    System.out.println("Read Item -> " + item);

                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}