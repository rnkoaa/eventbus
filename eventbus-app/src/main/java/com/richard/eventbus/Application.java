package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.eventbus.framework.EventBusException;
import com.richard.product.events.*;
import com.richard.product.events.listener.ProductCategoryCreatedEventListener;
import com.richard.product.events.listener.ProductCreatedEventListener;
import com.richard.product.events.listener.ProductDeactivatedEventListener;
import com.richard.product.events.listener.ProductUpdatedEventListener;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final EventBus eventBus;
    // https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146

    public Application(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void main(String[] args) throws NoSuchMethodException, InterruptedException {

        var productDeactivatedEvent = ProductDeactivatedEvent.newBuilder().aggregateId(UUID.randomUUID())
                .aggregateName(Product.class.getName())
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .build();


//        System.out.println(classes);
        Bus bus = new Bus();
        bus.register(ProductCreatedEvent.class, new ProductCreatedEventListener(), ProductCreatedEventListener.class.getMethod("on", ProductCreatedEvent.class));
        bus.register(ProductUpdatedEvent.class, new ProductUpdatedEventListener(), ProductUpdatedEventListener.class.getMethod("on", ProductUpdatedEvent.class));
        bus.register(ProductDeactivatedEvent.class, new ProductDeactivatedEventListener(), ProductDeactivatedEventListener.class.getMethod("on", ProductDeactivatedEvent.class));
//        bus.register(ProductCategoryCreatedEvent.class,
//                new ProductCategoryCreatedEventListener(),
//                ProductCategoryCreatedEventListener.class.getMethod("on", ProductCategoryCreatedEvent.class)
//        );
//        for (int index = 0; index < 100; index++) {
//            bus.publish(new Post(String.format("%d", index), "First Index %d".formatted(index), "testing Post #%d".formatted(index)));
//        }

        bus.publish(ProductCreatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .name("Product 1")
                .sku("Product 2")
                .build());
//        bus.publish(ProductDeactivatedEvent.newBuilder()
//                .aggregateId(UUID.randomUUID())
//                .id(UUID.randomUUID())
//                .aggregateName(Product.class.getSimpleName())
//                .build());
//        bus.publish(productDeactivatedEvent);

        TimeUnit.SECONDS.sleep(5);
        bus.publish(new PoisonPill());
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        eventBus.registerDeadLetterListener(object ->
                System.out.println("Got a Dead letter event of type " + object.getClass() + ", Value: " + object)
        );
//
        eventBus.publish(ProductCreatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .name("Product 1")
                .sku("Product 2")
                .build());
        eventBus.publish(ProductDeactivatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .build());
    }
}

class SubscriberMethod {
    private final Object eventHandler;
    private final Method method;
    private final Class<?> eventType;

    public SubscriberMethod(Class<?> eventType, Object handler, Method method) {
        this.eventType = eventType;
        this.eventHandler = handler;
        this.method = method;
    }

    public Object getEventHandler() {
        return eventHandler;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getEventType() {
        return eventType;
    }
}

class Bus {
    BlockingQueue<EventPost> queue = new ArrayBlockingQueue<>(10);
    private final AsyncPostingTask asyncPostingTask;
    private final Map<Class<?>, SubscriberMethod> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    Bus() {
        this.asyncPostingTask = new AsyncPostingTask(queue);
        executorService.submit(new EventPostProcessor(queue));
    }

    void register(Class<?> eventClass, Object handler, Method method) {
        Set<Class<?>> allInterfaces = getInterfaces(eventClass);
        allInterfaces.forEach(System.out::println);
        allInterfaces.forEach(it -> subscribers.put(it, new SubscriberMethod(it, handler, method)));
        subscribers.put(eventClass, new SubscriberMethod(eventClass, handler, method));
    }

    void publish(Object object) {
        Objects.requireNonNull(object, "event cannot be null");
        if (object instanceof PoisonPill) {
            shutdownNow();
            return;
        }

        Set<Class<?>> classes = new HashSet<>();
        classes.add(object.getClass());
        classes.addAll(getInterfaces(object.getClass()));
        classes.addAll(getSuperClasses(object.getClass()));

        SubscriberMethod subscriberMethod = null;
        for (Class<?> aClass : classes) {
            subscriberMethod = subscribers.get(aClass);
            if (subscriberMethod != null) {
                break;
            }
        }

        if (subscriberMethod == null) {
            System.out.println("No Event Handler Info found for Event: " + object.getClass());
            return;
        }
        asyncPostingTask.post(new EventPost(object, subscriberMethod));
    }

    private void shutdownNow() {
        executorService.shutdownNow();
    }

    private Set<Class<?>> getInterfaces(Class<?> clzz) {
        Set<Class<?>> interfaces = new HashSet<>();
        addInterfaces(interfaces, clzz.getInterfaces());
        return Set.copyOf(interfaces);
    }

    /**
     * Recurses through super interfaces.
     */
    static void addInterfaces(Set<Class<?>> eventTypes, Class<?>[] interfaces) {
        for (Class<?> interfaceClass : interfaces) {
            eventTypes.add(interfaceClass);
            addInterfaces(eventTypes, interfaceClass.getInterfaces());
        }
    }

    static Set<Class<?>> getSuperClasses(Class<?> clzz) {
        Set<Class<?>> classes = new HashSet<>();
        Class<?> superclass = clzz.getSuperclass();
        while (superclass != Object.class) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

}

record EventPost(Object event, SubscriberMethod subscriberMethod) {
}

record EventPostProcessor(BlockingQueue<EventPost> queue) implements Runnable {

    @Override
    public void run() {
        while (true) {
            EventPost item = queue.poll();
            if (item != null) {
                Object event = item.event();
                System.out.println("Got event" + event);
                if (event instanceof PoisonPill) {
                    return;
                }
                SubscriberMethod subscriberMethod = item.subscriberMethod();

                Method method = subscriberMethod.getMethod();
                Object eventHandler = subscriberMethod.getEventHandler();
                try {
                    method.invoke(eventHandler, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
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
        }
    }
}


class AsyncPostingTask {
    BlockingQueue<EventPost> queue;

    public AsyncPostingTask(BlockingQueue<EventPost> queue) {
        this.queue = queue;
    }

    void post(EventPost event) {
        try {
            this.queue.put(event);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
