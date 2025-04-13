package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class TimedAspect {
    @Pointcut("@annotation(com.example.demo.annoration.Timed)")
    public void allTimeMethods() {
    }

    @Around("allTimeMethods()")
    public Object logTimeMethod(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = point.getSignature().getName();

        Object object;
        try {
            object = point.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            long executionTime = finish - start;
            log.info("Процесс: выполнение метода {}. Метод выполнился за {} мс", methodName, executionTime);
        }

        return object;
    }
}
