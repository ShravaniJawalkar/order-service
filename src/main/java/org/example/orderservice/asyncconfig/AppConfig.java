package org.example.orderservice.asyncconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//first AsyncExecutionInterceptor is used to enable asynchronous method execution in Spring applications.
//secondly, it allows you to define a custom thread pool executor for handling asynchronous tasks.
// AsyncExecutionInterceptor i defaultTreadPoolTaskExecutor bean is register then use it directly otherwise use SimpleAsyncTaskExecutor
@Configuration
public class AppConfig {
    //if we have define custome ThreadPoolTaskExecutor, then we can use it directly
    // without providing its name in @Async annotation Spring Boot will automatically detect it
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Set the core pool size
        executor.setMaxPoolSize(5); // Set the maximum pool size
        executor.setQueueCapacity(4); // Set the queue capacity
        executor.setThreadNamePrefix("AsyncExecutor-"); // Set thread name prefix
        executor.initialize(); // Initialize the executor
        return executor;
    }

    //if we are creating a custom thread pool executor, then it is not directly detect by Spring Boot
    // and we need to explicitly add bean name in Async annotation
    @Bean(name = "threadPoolExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5,
                60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new CustomThreadFactory());
        return executor;
    }
}


