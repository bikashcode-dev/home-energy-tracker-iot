package com.homeenergy.userservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {


    @Pointcut("execution(* com.homeenergy.userservice.service.*.*(..))")
    public void controllerMethod() {
    }


    @Around("controllerMethod()")
   public Object measureExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        long  startTime = System.nanoTime();

        try{
            return proceedingJoinPoint.proceed();
        } finally {
            long end = System.nanoTime();
            long elapsedNs = end-startTime;
            long elapsedMs = TimeUnit.MILLISECONDS.toMillis(elapsedNs);
            String signature = proceedingJoinPoint.getSignature().toString();
            log.info("Controller method {} executed in {} ms", signature, elapsedMs);

        }

    }


}
