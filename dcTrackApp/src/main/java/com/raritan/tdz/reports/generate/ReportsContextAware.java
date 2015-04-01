/**
 * 
 */
package com.raritan.tdz.reports.generate;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This will be initialized as a spring bean within a spring context.
 * This will add the application context to the reports spring context aware
 * manager which will be used by the reports  generation to access the application
 * context so that it can get the Spring beans. 
 * @author prasanna
 *
 */
public class ReportsContextAware implements ApplicationContextAware {
	
	private String appCtxKey;
	public ReportsContextAware(String appCtxKey){
		this.appCtxKey = appCtxKey;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		ReportSpringContextAwareMgr.put(appCtxKey, applicationContext);
	}

}
