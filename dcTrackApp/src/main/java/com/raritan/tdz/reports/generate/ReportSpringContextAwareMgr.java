/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

/**
 * This holds the application context for report generation to use when
 * it requires to access any Spring beans.
 * @author prasanna
 *
 */
public class ReportSpringContextAwareMgr {
	private static Map<String, ApplicationContext> reportSpringContextMap = new HashMap<String, ApplicationContext>();
	
	public static void put(String contextKey, ApplicationContext ctx){
		reportSpringContextMap.put(contextKey, ctx);
	}
	
	public static ApplicationContext get(String contextKey){
		return (reportSpringContextMap.get(contextKey));
	}
	
	public static void remove(String contextKey){
		reportSpringContextMap.remove(contextKey);
	}
}
