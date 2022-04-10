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
import jakarta.inject.Singleton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final EventBus eventBus;

    static void testQueueBackground() throws InterruptedException {
        BlockingQueue<String> queue = new LinkedBlockingDeque<>(10);
        var producer = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        String item = queue.poll(5, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            if (item.equals("100")) {
                                System.out.println("exiting consumer, received --> 100");
                                return;
                            }
                            System.out.println(item);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(producer);

        for (int index = 0; index < 100; index++) {
            if (!queue.offer("Item %d".formatted(index), 500, TimeUnit.MILLISECONDS)) {
                break;
            }
        }

        queue.put("100");
        executorService.shutdown();
    }
    // https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146

    public Application(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void main(String[] args) throws NoSuchMethodException, InterruptedException {

        Bus bus = new Bus(new BusConfig());
        bus.register(ProductCreatedEvent.class, new ProductCreatedEventListener(), ProductCreatedEventListener.class.getMethod("on", ProductCreatedEvent.class));
        bus.register(ProductUpdatedEvent.class, new ProductUpdatedEventListener(), ProductUpdatedEventListener.class.getMethod("on", ProductUpdatedEvent.class));
        bus.register(ProductDeactivatedEvent.class, new ProductDeactivatedEventListener(), ProductDeactivatedEventListener.class.getMethod("on", ProductDeactivatedEvent.class));
        bus.register(ProductCategoryCreatedEvent.class,
                new ProductCategoryCreatedEventListener(),
                ProductCategoryCreatedEventListener.class.getMethod("on", ProductCategoryCreatedEvent.class)
        );

        bus.publish(ProductCategoryCreatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .name("Category 10")
                .categoryId(UUID.randomUUID())
                .build());
        bus.publish(ProductCreatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .name("Product 1")
                .sku("Product 2")
                .build());
        bus.publish(ProductDeactivatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .build());
        bus.publish(ProductDeactivatedEvent.newBuilder()
                .aggregateId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .aggregateName(Product.class.getSimpleName())
                .build());

        TimeUnit.SECONDS.sleep(10);
        bus.stop();
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

    public SubscriberMethod(Object handler, Method method) {
        this.eventHandler = handler;
        this.method = method;
    }

    public Object getEventHandler() {
        return eventHandler;
    }

    public Method getMethod() {
        return method;
    }
}

record BusConfig(long offerTimeout, long pollTimeout) {
    public BusConfig() {
        this(500, 500);
    }
}

class Bus {
    private final BusConfig busConfig;
    BlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);
    private final BackgroundMessagePostingThread asyncPostingTask;
    private final Map<Class<?>, SubscriberMethod> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    Bus(BusConfig busConfig) {
        this.busConfig = busConfig;
        this.asyncPostingTask = new BackgroundMessagePostingThread(queue, busConfig);
        executorService.submit(new EventPostProcessor(queue, busConfig));
    }

    void register(Class<?> eventClass, Object handler, Method method) {
        Set<Class<?>> allInterfaces = getInterfaces(eventClass);
        allInterfaces.forEach(it -> subscribers.put(it, new SubscriberMethod(handler, method)));
        subscribers.put(eventClass, new SubscriberMethod(handler, method));
    }

    void publish(Object object) {
        Objects.requireNonNull(object, "event cannot be null");
        if (object instanceof PoisonPill) {
            executorService.shutdownNow();
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

        var message = Message.builder()
                .withData(object)
                .withSubscriberMethod(subscriberMethod)
                .build();
        asyncPostingTask.post(message);
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

    public void stop() {
        publish(new PoisonPill());
    }
}

record EventPostProcessor(BlockingQueue<Message> queue, BusConfig busConfig) implements Runnable {

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
                    SubscriberMethod subscriberMethod = item.getSubscriberMethod();
                    Method method = subscriberMethod.getMethod();
                    Object eventHandler = subscriberMethod.getEventHandler();
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

class BackgroundMessagePostingThread {
    private final BusConfig busConfig;
    BlockingQueue<Message> queue;

    public BackgroundMessagePostingThread(BlockingQueue<Message> queue, BusConfig busConfig) {
        this.queue = queue;
        this.busConfig = busConfig;
    }

    void post(Message message) {
        try {
            this.queue.put(message);
        } catch (InterruptedException e) {
            // handle retry here

            throw new RuntimeException(e);
        }
    }
}

