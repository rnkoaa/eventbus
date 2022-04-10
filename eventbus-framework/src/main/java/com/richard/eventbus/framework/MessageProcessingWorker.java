package com.richard.eventbus.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public record MessageProcessingWorker(EventBus eventBus, BlockingQueue<Message> queue,
                                      BusConfig busConfig) implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Message item = queue.poll(busConfig.pollTimeout(), TimeUnit.MILLISECONDS);
                if (item != null) {
                    Object event = item.getContext();
                    if (event instanceof PoisonPill) {
                        return;
                    }
                    List<EventHandlerClassInfo> handlers = eventBus.findHandlers(event.getClass());
                    System.out.printf("Found %d handlers for event %s%n", handlers.size(), event.getClass());
                    for (EventHandlerClassInfo handler : handlers) {
                        Method method = handler.handlerMethod();
                        Object eventHandler = handler.eventListenerInstance();
                        try {
                            method.invoke(eventHandler, event);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                            // should either retry or post a special message with the exception  to be handled by exceptionHandler
                            throw new EventBusException(
                                    String.format("Failed to dispatch event %s@%d to listener %s@%d",
                                            event.getClass().getName(),
                                            System.identityHashCode(event),
                                            eventHandler.getClass().getName(),
                                            System.identityHashCode(eventHandler)),
                                    e);
                        }catch (Exception ex){
                            System.out.println(ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}