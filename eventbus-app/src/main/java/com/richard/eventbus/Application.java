package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventBusRegistrar;
import com.richard.product.events.Product;
import com.richard.product.events.ProductCreatedEvent;
import com.richard.product.events.ProductDeactivatedEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final EventBus eventBus;

    public Application(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void main(String[] args) throws InterruptedException {
//        Micronaut.run(Application.class, args);
//        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
//        queue.put("10");
//        queue.put("20");
//
//        System.out.println(queue.take());
//        System.out.println(queue.take());
//        System.out.println("Running without Micronaut");

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        var producer = new Producer(queue);
        var consumer = new Consumer(queue);
//        queue.put("10");
//        queue.put("20");

        new Thread(producer).start();
        new Thread(consumer).start();
//        System.out.println(queue.take());
//        System.out.println(queue.take());
        System.out.println("Running without Micronaut");
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

class Post {
}

class Consumer implements Runnable {

    private final BlockingQueue<String> queue;

    Consumer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String item = queue.take();
                System.out.println("Read Item -> " + item);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Producer implements Runnable {

    private final BlockingQueue<String> queue;

    Producer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            try {
                this.queue.put("Current Time: " + currentTime);
            } catch (InterruptedException e) {
                System.out.println("Producer was interrupted");
                throw new RuntimeException(e);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

class AsyncPostingTask implements Runnable {
    BlockingDeque<Post> queue;

    @Override
    public void run() {

    }
}
