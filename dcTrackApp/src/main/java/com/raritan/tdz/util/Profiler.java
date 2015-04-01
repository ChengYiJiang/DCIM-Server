package com.raritan.tdz.util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Profiler {
	@Around("@annotation(Profiled)")
    public Object measureTimeTaken(ProceedingJoinPoint pjp) throws Throwable {
        //record the current time (use System.currentTimeMillis() method)
        long startTimeMillis = System.currentTimeMillis();

        
        String prefix = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

        //allow the method call to proceed, and record the return value
        Object value = pjp.proceed();
        
        //calculate the time taken and print this to the console
        long endTimeMillis = System.currentTimeMillis();
        System.out.printf("#### " + prefix + " - Time taken: %d milliseconds\n", (endTimeMillis -startTimeMillis));
    
        //return the value that was returned from the method call
        return value;
    }
}
