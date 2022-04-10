package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventBusRegistrar;
import com.richard.eventbus.framework.LogListener;
import com.richard.product.events.Product;
import com.richard.product.events.ProductCategoryCreatedEvent;
import com.richard.product.events.ProductCreatedEvent;
import com.richard.product.events.ProductDeactivatedEvent;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.UUID;
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

    public static void main(String[] args) throws InterruptedException {
        Micronaut.run(Application.class, args);

    }


    @Override
    public void onApplicationEvent(StartupEvent event) {
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
//            String str = br.readLine();
//            if (str != null && !str.isBlank()) {
//                String[] split = str.split(",");
//                if (split.length < 3) {
//                    System.err.println("Required Format: Command,Product Name,Product Sku");
//                    System.exit(1);
//                }
//
//                var product = ProductCreatedEvent.newBuilder()
//                        .aggregateId(UUID.randomUUID())
//                        .id(UUID.randomUUID())
//                        .productId(UUID.randomUUID())
//                        .aggregateName(Product.class.getSimpleName())
//                        .name(split[1])
//                        .sku(split[2])
//                        .build();
//
//                eventBus.publish(product);
////                System.exit(0);
////                System.out.println(str);
//            } else {
//                System.err.println("A non empty String is required");
//                System.exit(1);
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        var scheduledRunnable = new Runnable() {
//
//            @Override
//            public void run() {
//                System.out.println("Time: " + LocalDateTime.now());
//            }
//        };
//        var scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(scheduledRunnable, 5, 5, TimeUnit.SECONDS);
//        String input = null;
//        do {
//
//        }while (input != null && input != "stop");
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        eventBus.registerDeadLetterListener(object ->
//                System.out.println("Got a Dead letter event of type " + object.getClass() + ", Value: " + object)
//        );
//        eventBus.registerLogger(new LogListener() {
//            @Override
//            public void onEvent(Object object) {
//                System.out.println("Logging Event " + object);
//            }
//
//            @Override
//            public boolean isEnabled() {
//                return true;
//            }
//        });
////
//        eventBus.publish();
//        eventBus.publish(ProductDeactivatedEvent.newBuilder()
//                .aggregateId(UUID.randomUUID())
//                .id(UUID.randomUUID())
//                .aggregateName(Product.class.getSimpleName())
//                .build());
////        eventBus.stop();
    }
}
