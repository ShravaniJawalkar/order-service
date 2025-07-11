package org.example.orderservice.asyncconfig;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class  CustomThreadFactory implements ThreadFactory {
    private final AtomicInteger threadId = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("AsyncExecutorWithConfig-" + threadId.getAndIncrement());
        return thread;
    }
}
