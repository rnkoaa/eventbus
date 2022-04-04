package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.product.events.Product;
import com.richard.product.events.ProductCreatedEvent;
import com.richard.product.events.ProductDeactivatedEvent;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final EventBus eventBus;
    // https://github.com/greenrobot/EventBus/blob/842a4a312c4ea1592bd7067a00495eebe129c078/EventBusAnnotationProcessor/src/org/greenrobot/eventbus/annotationprocessor/EventBusAnnotationProcessor.java#L146

    public Application(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
//        Micronaut.run(Application.class, args);
//        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
//        queue.put("10");
//        queue.put("20");
//
//        System.out.println(queue.take());
//        System.out.println(queue.take());
//        System.out.println("Running without Micronaut");
//
//        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
//        var producer = new Producer(queue);
//        var consumer = new Consumer(queue);
////        queue.put("10");
////        queue.put("20");
//
//        new Thread(producer).start();
//        new Thread(consumer).start();
////        System.out.println(queue.take());
////        System.out.println(queue.take());
        System.out.println("Running without Micronaut");
        Bus bus = new Bus();
        bus.publish(new Post("1", "First Post", "testing First Post"));
        bus.publish(new Post("2", "Second Post", "testing Second Post"));
        bus.publish(new Post("3", "Third Post", "testing Third Post"));
        System.in.read();
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

class Bus {
    BlockingQueue<Post> queue = new ArrayBlockingQueue<>(10);
    private final AsyncPostingTask asyncPostingTask;

    Bus() {
        this.asyncPostingTask = new AsyncPostingTask(queue);
        new Thread(new PostConsumer(queue)).start();
    }

    void publish(Object object) {
        asyncPostingTask.post(object);
    }
}

record Post(String id, String title, String content) {
}

record PostConsumer(BlockingQueue<Post> queue) implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Post item = queue.take();
                System.out.println("Read Item -> " + item);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class AsyncPostingTask {
    BlockingQueue<Post> queue;

    public AsyncPostingTask(BlockingQueue<Post> queue) {
        this.queue = queue;
    }

    void post(Object event) {
        this.queue.add((Post) event);
    }
}
