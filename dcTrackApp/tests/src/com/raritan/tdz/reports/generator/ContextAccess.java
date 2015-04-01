package com.raritan.tdz.reports.generator;

import org.springframework.context.ApplicationContext;

public class ContextAccess {
	
	private static ApplicationContext ctx;
	
	public static void setApplicationContext(ApplicationContext applicationContext) {
		ctx = applicationContext;
	
	}
	
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}
	
}