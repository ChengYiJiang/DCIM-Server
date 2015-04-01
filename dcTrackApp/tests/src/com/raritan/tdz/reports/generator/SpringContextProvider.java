package com.raritan.tdz.reports.generator;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextProvider implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		
		ContextAccess.setApplicationContext(applicationContext);

	}

}
