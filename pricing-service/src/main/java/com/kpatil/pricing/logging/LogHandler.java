package com.kpatil.pricing.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Aspect
@Component
public class LogHandler {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Logger logger =
            LoggerFactory.getLogger(LogHandler.class);

    @Pointcut("this(org.springframework.data.repository.Repository)")
    public void repository() {
    }

    @Pointcut("execution(* *.*(..))")
    protected void allMethods() {
    }

    // before -> Any resource annotated with @Repository annotation
    // and all method and function
    @Before("repository() && allMethods()")
    public void logStart(JoinPoint joinPoint) {
        String currentTime =
                dateFormat.format(new Date(System.currentTimeMillis()));
        logger.info("Running method: '" +
                joinPoint.getSignature().getName() + "' at " + currentTime);
    }

    // After -> All method within resource annotated with @Repository annotation
    @AfterReturning(pointcut = "repository() && allMethods()")
    public void logEnd(JoinPoint joinPoint) {
        String currentTime =
                dateFormat.format(new Date(System.currentTimeMillis()));
        logger.info("Method completed at: " + currentTime);
    }

}
