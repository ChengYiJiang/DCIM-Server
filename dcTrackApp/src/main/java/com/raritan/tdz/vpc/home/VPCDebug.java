package com.raritan.tdz.vpc.home;

import org.apache.log4j.Logger;

public class VPCDebug {

	private static Logger log = Logger.getLogger("VPC");
	
	public static void debug(String message) {
		
		if (log.isDebugEnabled()) {
			log.debug(message);
		}
		
	}
	
	public static void info(String message) {
		
		if (log.isInfoEnabled()) {
			log.info(message);
		}
		
	}
	
	public static void trace(String message) {
		
		if (log.isTraceEnabled()) {
			log.trace(message);;
		}
		
	}

	public static void warn(String message) {
		
		log.warn(message);;
		
	}
	
	public static void error(String message) {
		
		log.error(message);;
		
	}

}
