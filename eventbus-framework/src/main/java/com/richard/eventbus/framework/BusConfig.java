package com.richard.eventbus.framework;

public record BusConfig(
        long offerTimeout,
        long pollTimeout,
        long defaultQueueSize) {
    public BusConfig() {
        this(1000, 1000, 200);
    }
}
