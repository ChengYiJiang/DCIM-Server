/**
 * 
 */
package com.raritan.tdz.dao;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;

/**
 * @author prasanna
 *
 */
public class DaoUpdateIntroductionInterceptor implements
		IntroductionInterceptor {

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		DaoUpdateExecutor daoFinder = (DaoUpdateExecutor) invocation.getThis();
		
		String methodName = invocation.getMethod().getName();
		if (methodName.matches("update\\w+")) {
			Object[] arguments = invocation.getArguments();
			return (daoFinder.executeUpdate(invocation.getMethod(), arguments));
		} else {
			return invocation.proceed();
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.aop.DynamicIntroductionAdvice#implementsInterface(java.lang.Class)
	 */
	@Override
	public boolean implementsInterface(Class<?> intf) {
		return intf.isInterface() && DaoUpdateExecutor.class.isAssignableFrom(intf);
	}

}
