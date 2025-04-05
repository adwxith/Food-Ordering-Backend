package com.foodordering.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.foodordering.controller..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            
            logger.info("Entering: {}.{}() with arguments = {}", className, methodName, 
                Arrays.toString(joinPoint.getArgs()));
            
            Object result = joinPoint.proceed();
            
            logger.info("Exiting: {}.{}() with result = {}", className, methodName, result);
            
            return result;
        } catch (Exception e) {
            logger.error("Exception in {}.{}() with cause = {}", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getCause() != null ? e.getCause() : "NULL");
            throw e;
        } finally {
            long elapsedTime = System.currentTimeMillis() - start;
            logger.debug("Method execution time: {} milliseconds", elapsedTime);
        }
    }
}