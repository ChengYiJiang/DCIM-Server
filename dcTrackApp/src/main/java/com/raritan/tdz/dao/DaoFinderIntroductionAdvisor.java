/**
 * 
 */
package com.raritan.tdz.dao;

import org.springframework.aop.support.DefaultIntroductionAdvisor;

/**
 * @author prasanna
 *
 */
public class DaoFinderIntroductionAdvisor extends DefaultIntroductionAdvisor {

	public DaoFinderIntroductionAdvisor() {
		super(new DaoFinderIntroductionInterceptor(),DaoFinderExecutor.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Class clazz) {
		return DaoFinderExecutor.class.isAssignableFrom(clazz);
	}

}
