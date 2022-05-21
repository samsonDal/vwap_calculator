package com.vwapcalculator.app.api;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class VwapThreadFactory implements ThreadFactory {

    private static final AtomicInteger FACTORY_NUMBER = new AtomicInteger(1);
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);
    private final boolean daemon;
    private final String namePrefix;

    public VwapThreadFactory(String name, boolean daemon) {
        this.daemon = daemon;
        namePrefix = "TF-" + FACTORY_NUMBER.getAndIncrement() + "-" + name + "-";
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread thread = new Thread(r, namePrefix + THREAD_NUMBER.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }
}
