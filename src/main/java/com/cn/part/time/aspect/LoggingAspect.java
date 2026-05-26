//package com.cn.part.time.aspect;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//import java.util.Collection;
//
//@Slf4j
//@Aspect
//@Component
//public class LoggingAspect {
//
//    @Before("execution(public * com.cn.part.time.controller..*(..)) || @annotation(LoggingAspect.LogExecution)")
//    public void logBeforeController(JoinPoint point) {
//        log.info("Entering method: {}.{}() with parameters: {}",
//                point.getSignature().getDeclaringTypeName(),
//                point.getSignature().getName(),
//                point.getArgs()
//        );
//    }
//
//    @AfterReturning("execution(public * com.cn.part.time.controller..*(..)) || @annotation(LoggingAspect.LogExecution)")
//    public Object logAfterController(JoinPoint point, Object result) {
//        if (result instanceof Collection<?> res) {
//            log.info("Exiting method: {}.{}() with result size: {}",
//                    point.getSignature().getDeclaringTypeName(),
//                    point.getSignature().getName(),
//                    res.size()
//            );
//        } else {
//            log.info("Exiting method: {}.{}()",
//                    point.getSignature().getDeclaringTypeName(),
//                    point.getSignature().getName()
//            );
//        }
//        return result;
//    }
//
//    @Target(ElementType.METHOD)
//    @Retention(RetentionPolicy.RUNTIME)
//    public @interface LogExecution {
//    }
//}
