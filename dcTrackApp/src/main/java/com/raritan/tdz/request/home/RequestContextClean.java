package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.progress.RequestProgressUpdate;

public class RequestContextClean implements SessionClean {

	@Autowired(required=true)
	RequestContextRouter requestContextRouter;
	
	@Autowired(required=true)
	RequestProgressUpdate requestProgressUpdateDTO;
	
	public static long MAX_WAIT_TIME_TO_ISSUE_REQUEST = 25000;
	
	@Override
	public void clean(UserInfo userInfo) {
		
		// Wait till the request is being processed
		do {
			
			try {
				
				Thread.sleep(MAX_WAIT_TIME_TO_ISSUE_REQUEST);
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		} while (requestProgressUpdateDTO.active(userInfo));
		
		requestContextRouter.deleteContext(userInfo);

	}

}
