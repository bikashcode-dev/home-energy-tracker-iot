package com.homeenergy.userservice.aspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

    // Log before and after service methods
    @Pointcut("execution(* com.homeenergy.userservice.service.*.*(..))")
    public void serviceMethods() {
    }

        // Log before and after controller methods
    @Before("serviceMethods()")
    public void logBeforeServiceMethods(JoinPoint joinPoint) {
        log.info("Entering method: {} with arguments: {}", joinPoint
                .getSignature().toShortString(), joinPoint.getArgs());
    }

    // Log after returning from service methods
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturningServiceMethods(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: {} with result: {}", joinPoint
                .getSignature().getName() ,result);
    }

}
