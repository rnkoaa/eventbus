package com.richard.eventbus.framework;

import java.util.concurrent.BlockingQueue;

public class BackgroundMessagePostingThread {
    BlockingQueue<Message> queue;

    public BackgroundMessagePostingThread(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    void post(Message message) {
        try {
            this.queue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
