package com.raritan.tdz.interceptor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.raritan.tdz.annotation.Login;

/**
 * <p>The LoginInterceptor will intercept public method invocations on classes which are annotated
 * with the {@link Login} annotation. It will lookup the {@link AuthenticationProvider} bean and invoke
 * the authenticate method using the username/password credentials provided by the {@link Login} annotation.</p>
 *  
 * @author Andrew Cohen
 * @version 3.0
 */
@Aspect
public class LoginInterceptor implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Pointcut("execution (public * *(..))")
	private void anyPublicOperation() {};
	
	@Pointcut("@target(com.raritan.tdz.annotation.Login)")
	private void processLoginAnnotation() {};
	
	/**
	 * All classes with login annotation will have the spring security perform the username/password authentication.
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Before("processLoginAnnotation() && anyPublicOperation()")
	public void login(JoinPoint joinPoint) throws Throwable {
		Login loginAnnotation = joinPoint.getThis().getClass().getAnnotation(Login.class);

		if (applicationContext != null) {
			Object authProvider = applicationContext.getBean( loginAnnotation.authProviderBeanName() );
		
			if (authProvider == null) {
				throw new IllegalArgumentException("No authProviderBeanName found for " + loginAnnotation.authProviderBeanName());
			}
			
			if (!(authProvider instanceof AuthenticationProvider)) {
				throw new IllegalArgumentException(loginAnnotation.authProviderBeanName() + "must be an AuthenticationProvider!");
			}
			
			((AuthenticationProvider)authProvider).authenticate( 
					new UsernamePasswordAuthenticationToken(loginAnnotation.username(), loginAnnotation.password())
			);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.applicationContext = context;
	}
}
