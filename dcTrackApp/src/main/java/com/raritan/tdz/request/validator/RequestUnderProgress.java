package com.raritan.tdz.request.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RequestUnderProgress {

	private static List<Long> requestUnderProcessSync = Collections.synchronizedList(new ArrayList<Long>());
	
	public static void setRequest(Long requestId) {
		
		requestUnderProcessSync.add(requestId);
		
	}
	
	
	public static boolean isRequestUnderProgress(Long requestId) {
		
		return requestUnderProcessSync.contains(requestId);
		
	}
	
	
	public static void clearRequest(Long requestId) {
		
		requestUnderProcessSync.remove(requestId);
		
	}
	
}
