/**
 * 
 */
package com.raritan.tdz.dao;

import org.springframework.aop.support.DefaultIntroductionAdvisor;

/**
 * @author prasanna
 *
 */
public class DaoUpdateIntroductionAdvisor extends DefaultIntroductionAdvisor {

	public DaoUpdateIntroductionAdvisor() {
		super(new DaoUpdateIntroductionInterceptor(),DaoUpdateExecutor.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Class clazz) {
		return DaoUpdateExecutor.class.isAssignableFrom(clazz);
	}

}
