package com.raritan.tdz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.authentication.AuthenticationProvider;

import com.raritan.tdz.interceptor.LoginInterceptor;


import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * <p>A Login Annotation to support generic AOP based authentication.</p>
 * <p>This annotation can be used at the class level to ensure that all public methods on
 * that class are first authenticated using the username/password credentials and the
 * specified authentication provider bean.</p>
 * 
 * <p>The {@link LoginInterceptor} will provide the actual AOP proxying.</p>
 * 
 * @author Andrew Cohen
 * @version 3.0
 */
@Documented
@Target({TYPE, METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
	
	/**
	 * The login username.
	 * @return
	 */
	public String username() default "";
	
	/**
	 * The login password.
	 * @return
	 */
	public String password() default "";
	
	/**
	 * The name/id of the Authentication Provider bean.
	 * This bean must implement the Spring {@link AuthenticationProvider} interface.
	 * @return
	 */
	public String authProviderBeanName() default "";
}
