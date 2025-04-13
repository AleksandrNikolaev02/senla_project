package com.example.demo.aspect;

import com.example.demo.annoration.Loggable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Component
@Aspect
@Slf4j
public class LoggableAspect {
    @Pointcut("@annotation(com.example.demo.annoration.Loggable)")
    public void allMethodsForLogging() {
    }

    @Pointcut("within(com.example.demo.service.*)")
    public void allPackageForLogging() {
    }

    @Pointcut("within(com.example.demo.service.AuthFilter)")
    public void allPackageForLoggingUnlessFilter() {
    }

    @Before("allMethodsForLogging()")
    public void logBeforeMethod(JoinPoint point) {
        String[] params = process(point);

        if (!params[0].isEmpty()) {
            log.info("Процесс: {}. Начало выполнения метода: {}", params[0], params[1]);
        } else {
            log.info("Процесс: начало выполнения метода: {}", params[1]);
        }
    }

    @After("allMethodsForLogging()")
    public void logAfterMethod(JoinPoint point) {
        String[] params = process(point);

        if (!params[0].isEmpty()) {
            log.info("Процесс: {}. Метод {} выполнился успешно!", params[0], params[1]);
        } else {
            log.info("Процесс: выполнения метода {}. Метод выполнился успешно!", params[1]);
        }
    }

    @AfterThrowing(pointcut = "allPackageForLogging() && !allPackageForLoggingUnlessFilter()",
                                                                        throwing = "exception")
    public void logAfterThrowing(JoinPoint point, Throwable exception) {
        log.error("Исключение в методе {} с сообщением: {}", point.getSignature().getName(),
                                                             exception.getMessage());
    }

    private String[] process(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Loggable annotation = method.getAnnotation(Loggable.class);

        String process = annotation.process();
        String methodName = point.getSignature().getName();

        return new String[]{process, methodName};
    }
}
