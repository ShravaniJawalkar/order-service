package org.example.orderservice.asyncconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
//if we don't want to explicitly mentioned the thread pool executor bean name in @Async annotation
// then we can implement AsyncConfigure interface and override getAsyncExecutor method
@Configuration
public class AppConfigWithAsyncConfig implements AsyncConfigurer {

   private ThreadPoolExecutor executor;
   private CustomUncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    public synchronized ThreadPoolExecutor getAsyncExecutor() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(2, 5,
                    60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new CustomThreadFactory());
        }
        return executor;
    }

    @Override
    public synchronized CustomUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        if (uncaughtExceptionHandler == null) {
            uncaughtExceptionHandler = new CustomUncaughtExceptionHandler();
        }
        return uncaughtExceptionHandler;
    }



}

