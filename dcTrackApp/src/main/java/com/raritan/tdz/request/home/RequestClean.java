package com.raritan.tdz.request.home;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.DisposableBean;

import com.raritan.tdz.request.progress.RequestProgressDTO;
import com.raritan.tdz.request.progress.SessionToObjectFactory;

public class RequestClean implements DisposableBean {

	private String userSessionId;
	
	private SessionToObjectFactory<RequestProgressDTO> requestProgressDTOFactory;
	
	private SessionToObjectFactory<ReentrantReadWriteLock> requestDTOLockFactory;

	private SessionToObjectFactory<AtomicLong> requestProgressCountFactory;

	public RequestClean(
			String userSessionId,
			SessionToObjectFactory<RequestProgressDTO> requestProgressDTOFactory,
			SessionToObjectFactory<ReentrantReadWriteLock> requestDTOLockFactory, 
			SessionToObjectFactory<AtomicLong> requestProgressCountFactory) {
	
		super();
		
		this.userSessionId = userSessionId;
		this.requestProgressDTOFactory = requestProgressDTOFactory;
		this.requestDTOLockFactory = requestDTOLockFactory;
		this.requestProgressCountFactory = requestProgressCountFactory;
		
	}

	@Override
	public void destroy() throws Exception {
		
		requestProgressDTOFactory.clear(userSessionId);
		
		requestProgressCountFactory.clear(userSessionId);
		
		requestDTOLockFactory.clear(userSessionId);

	}

}
