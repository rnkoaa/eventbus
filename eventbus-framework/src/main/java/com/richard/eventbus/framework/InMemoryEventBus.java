package com.richard.eventbus.framework;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class InMemoryEventBus implements EventBus {

    record ScheduledQueueMonitor(BlockingQueue<Message> queue) implements Runnable {

        @Override
        public void run() {
            int size = queue.size();
            System.out.printf("[%s] - current queue size: %d\n", LocalDateTime.now(), size);
        }
    }

    private DeadLetterEventListener deadLetterEventListener;
    private LogListener logListener;
    private static volatile InMemoryEventBus instance;
    private static final Map<Class<?>, List<EventHandlerClassInfo>> handlers = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final BackgroundMessagePostingThread postingThread;
    private boolean enabled = true;

    public InMemoryEventBus(BusConfig busConfig) {
        BlockingQueue<Message> queue = new ArrayBlockingQueue<>((int) busConfig.defaultQueueSize());
        this.postingThread = new BackgroundMessagePostingThread(queue);
        executorService = Executors.newCachedThreadPool();
        executorService.submit(new MessageProcessingWorker(this, queue, busConfig));
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new ScheduledQueueMonitor(queue), 2, 5, TimeUnit.SECONDS);
    }

    public void registerLogger(LogListener logListener) {
        this.logListener = logListener;
    }

    public void registerDeadLetterListener(DeadLetterEventListener deadLetterEventListener) {
        this.deadLetterEventListener = deadLetterEventListener;
    }

    public static InMemoryEventBus getInstance() {
        if (instance == null) {
            synchronized (InMemoryEventBus.class) {
                if (instance == null) {
                    instance = new InMemoryEventBus(new BusConfig());
                }
            }
        }
        return instance;
    }

    @Override
    public void register(EventHandlerClassInfo eventHandlerClassInfo) {
        List<EventHandlerClassInfo> eventHandlers = handlers.getOrDefault(
                eventHandlerClassInfo.eventClass(),
                new ArrayList<>()
        );

        eventHandlers.add(eventHandlerClassInfo);
        handlers.put(eventHandlerClassInfo.eventClass(), eventHandlers);
    }

    @Override
    public List<EventHandlerClassInfo> findHandlers(Class<?> clzz) {
        List<EventHandlerClassInfo> eventHandlers = handlers.getOrDefault(clzz, new ArrayList<>());
        if (eventHandlers.size() > 0) {
            return eventHandlers;
        }

        // potentially, this class implements an interface that is handled.
        record InterfaceHandler(Class<?> interfaceClass, List<EventHandlerClassInfo> eventHandlerClassInfos) {
        }

        // we only care about the immediate interfaces of classes. This prevents deep hierachical code inheritance
        Set<Class<?>> interfaces = getInterfaces(clzz);
        List<InterfaceHandler> interfaceHandlers = interfaces.stream()
                .map(clzzInterface -> new InterfaceHandler(clzzInterface, handlers.get(clzzInterface)))
                .filter(interfaceHandler -> interfaceHandler.eventHandlerClassInfos != null)
                .toList();

        if (interfaceHandlers.size() > 0) {
            for (InterfaceHandler interfaceHandler : interfaceHandlers) {
                handlers.putIfAbsent(interfaceHandler.interfaceClass, interfaceHandler.eventHandlerClassInfos);
            }

            return interfaceHandlers.stream()
                    .flatMap(it -> it.eventHandlerClassInfos.stream())
                    .toList();
        }

        Class<?> superclass = clzz.getSuperclass();
        if (superclass != Object.class) {
            List<EventHandlerClassInfo> eventHandlerClassInfos = handlers.getOrDefault(superclass, new ArrayList<>());
            if (eventHandlerClassInfos.size() > 0) {
                handlers.putIfAbsent(superclass, eventHandlerClassInfos);
                return eventHandlerClassInfos;
            }
        }

        // look for the immediate super class to see if it is available to use
        return Collections.emptyList();
    }

    @Override
    public void publish(Object event) {
        if (event == null) {
            throw new EventListenerException("unable to register a listener. should not be null");
        }

        // stop all processing
        if (event instanceof PoisonPill) {
            executorService.shutdown();
            enabled = false;
            return;
        }

        if (!enabled) {
            System.out.println("Bus is not enabled, message will not be delivered.");
            return;
        }

        if (logListener != null && logListener.isEnabled()) {
            logListener.onEvent(event);
        }

//        List<EventHandlerClassInfo> eventHandlers = findHandlers(event.getClass());
//        if (eventHandlers.size() == 0 && deadLetterEventListener != null) {
//            deadLetterEventListener.onEvent(event);
//            return;
//        }

//        eventHandlers.forEach(eventHandlerClassInfo -> {
//            postingThread.post(
//                    Message.builder()
//                            .withData(event)
////                            .withHandlerClassInfo(eventHandlerClassInfo)
//                            .build()
//            );
//        });

        postingThread.post(
                Message.builder()
                        .withData(event)
                        .build()
        );
    }

    @Override
    public Set<EventHandlerClassInfo> getSubscribers() {
        return handlers.values()
                .stream()
                .filter(it -> it != null && it.size() > 0)
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public void unRegister(EventHandlerClassInfo subscriber) {

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

    /*****/
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
