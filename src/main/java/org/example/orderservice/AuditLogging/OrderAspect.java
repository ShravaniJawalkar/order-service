package org.example.orderservice.AuditLogging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
@Slf4j
public class OrderAspect {


    @Before("execution(* org.example.orderservice.controller.OrderController.*(..))")
    public void beforeOrderController(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}(){}", className, methodName, joinPoint.getSignature().getName());
    }

    // After advice intercept method after method proceeding
    // execution pointcut is used to match any method with given signature
    // * meaning any return type
    // .* means any class in the package
    //(..) zero or more arguments of any type
    @After("execution(* org.example.orderservice.controller.OrderController.*(..))")
    public void afterOrderController(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() completed", className, methodName);
    }

    //this advice is surrounded with method execution
    //it's executed before and after the method execution
    //here we need to Proceed JoinPoint explicitly it doesn't invoke internally
    @Around("execution(* org.example.orderservice.controller.OrderController.*(..))")
    public Object aroundOrderController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Entering {}.{}()", className, methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("Exiting {}.{}() with result: {}", className, methodName, result);
            return result;
        } catch (Throwable throwable) {
            log.error("Exception in {}.{}(): {}", className, methodName, throwable.getMessage());
            throw throwable;
        }

    }

    // This advice is executed before the method execution
    // It matches all methods in the service package
    // within is class or package pointcut expression
    @Before("within(org.example.orderservice.service.*)")
    public void beforeServiceMethods(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() called", className, methodName);
    }

    // @within is ued to match all method in the class which is annotated with this annotation or package which has class with this annotation
    @AfterReturning(pointcut = "@within(org.springframework.web.bind.annotation.RestController)", returning = "result")
    public void afterServiceMethods(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        if (log.isInfoEnabled()) {
            log.info("{}.{}() completed", className, methodName, result);
        }
    }

    // this is method level pointcut expression
    // this matches all method which has @GetMapping annotation
    @Before("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void beforeGetMapping(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() called with @GetMapping", className, methodName);
    }

    // this matches all method which has argument of type Long
    @After(" serviceLayer() && args(long)")
    public void afterMethodWithLongArg(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() completed with long argument", className, methodName);
    }

    // this matches any method which has argument as object of type OrderRequest
    @Before("serviceLayer() && args(org.example.orderservice.dao.OrderRequest)")
    public void beforeOrderRequest(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() called with OrderRequest", className, methodName);
    }

    // if we have a interface and it's two implementations,
    // we can use @args to match the method which has the particular class as argument
    // And that class has given annotation
    @After("serviceLayer() && @args(org.springframework.stereotype.Component)")
    public void afterOrderRequest(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() completed with OrderRequest", className, methodName);
    }

    // target is used to match the method which is calling the instance of the class
    @After("target(org.example.orderservice.repository.OrderRepository)")
    public void afterOrderRepository(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() completed with OrderRepository", className, methodName);
    }

    //here we can see the combination of pointcuts
    // so we can combine multiple pointcuts using || operator Or && operator
    // Much more specific - only your service classes, not all Spring services
    @Around("serviceLayer() || controllerLayer()")
    public Object aroundServiceLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Entering {}.{}()", className, methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("Exiting {}.{}() with result: {}", className, methodName, result);
            return result;
        } catch (Throwable throwable) {
            log.error("Exception in {}.{}(): {}", className, methodName, throwable.getMessage());
            throw throwable;
        } finally {
            log.info("Completed {}.{}()", className, methodName);
        }
    }

    // This advice is executed after the method execution
    // this advice only get executed if method completed it's execution and return result
    //method does not throw any exception
    @AfterReturning(pointcut = "controllerLayer()", returning = "result")
    public void afterReturningController(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("{}.{}() returned with result: {}", className, methodName, result);
    }

    // this advice is executed when method throws an exception
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.error("Exception in {}.{}(): {}", className, methodName, exception.getMessage());
    }

    // this is used to define custom pointcuts
    @Pointcut("execution(* org.example.orderservice.service.*.*(..))")
    public void serviceLayer() {
        // Pointcut for service layer methods
    }

    @Pointcut("execution(* org.example.orderservice.controller.*.*(..))")
    public void controllerLayer() {
        // Pointcut for controller layer methods
    }

    @Pointcut("execution(* org.example.orderservice.repository.*.*(..))")
    public void repositoryLayer() {
        // Pointcut for repository layer methods
    }

}
