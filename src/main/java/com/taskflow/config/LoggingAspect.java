package com.taskflow.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // @Around معناها: نفذ الكود ده حول (قبل وبعد) الدالة اللي عليها الـ Annotation
    @Around("@annotation(com.taskflow.annotation.LogExecutionTime)")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // تنفيذ الدالة الأصلية (مثلاً createUser)
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();

        // طباعة اسم الدالة والوقت اللي أخدته
        logger.info("Method {} executed in {} ms",
                joinPoint.getSignature().getName(),
                (endTime - startTime));

        return result;
    }
}