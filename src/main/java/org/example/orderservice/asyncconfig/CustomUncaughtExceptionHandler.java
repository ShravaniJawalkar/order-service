package org.example.orderservice.asyncconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class CustomUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("{} threw an exception: {}", method.getName(), ex.getMessage());
        log.error("Method parameters: {}", String.join(", ", Arrays.stream(params).map(Object::toString).toArray(String[]::new)));
        log.error("Uncaught exception in thread: {}", Thread.currentThread().getName(), ex);
    }
}
