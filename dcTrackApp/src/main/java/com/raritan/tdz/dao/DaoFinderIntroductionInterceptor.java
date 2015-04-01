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
public class DaoFinderIntroductionInterceptor implements
		IntroductionInterceptor {

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		DaoFinderExecutor daoFinder = (DaoFinderExecutor) invocation.getThis();
		
		String methodName = invocation.getMethod().getName();
		if (methodName.startsWith("find")) {
			Object[] arguments = invocation.getArguments();
			return daoFinder.executeFinder(invocation.getMethod(), arguments, false);
		} else if (methodName.startsWith("find") && methodName.endsWith("ReadOnly")){
			Object[] arguments = invocation.getArguments();
			return daoFinder.executeFinder(invocation.getMethod(), arguments, true);
		} else if (methodName.startsWith("fetch")){
			Object[] arguments = invocation.getArguments();
			return daoFinder.executeFetch(invocation.getMethod(), arguments);		
		}
		
		return invocation.proceed();
	}

	/* (non-Javadoc)
	 * @see org.springframework.aop.DynamicIntroductionAdvice#implementsInterface(java.lang.Class)
	 */
	@Override
	public boolean implementsInterface(Class<?> intf) {
		return intf.isInterface() && DaoFinderExecutor.class.isAssignableFrom(intf);
	}

}
