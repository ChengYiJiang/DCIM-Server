/**
 * 
 */
package com.raritan.tdz.request.home;

import org.apache.log4j.Logger;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.session.DCTrackUserLogoutEventHandler;

/**
 * @author prasanna
 *
 */
public class RequestBypassCleanupHandler implements
		DCTrackUserLogoutEventHandler {

	Logger log = Logger.getLogger(getClass());

	/*@Autowired(required=true)
	RequestContextRouter requestContextRouter;
	
	@Autowired(required=true)
	RequestProgressUpdate requestProgressUpdateDTO;*/
	
	private SessionClean requestContextCleaner;
	
	

	public SessionClean getRequestContextCleaner() {
		return requestContextCleaner;
	}



	public void setRequestContextCleaner(SessionClean requestContextCleaner) {
		this.requestContextCleaner = requestContextCleaner;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.session.DCTrackUserLogoutEventHandler#handleUserLogout(com.raritan.tdz.domain.UserInfo, java.lang.String)
	 */
	@Override
	public void handleUserLogout(UserInfo userInfo, String sessionKey) {

		if (log.isDebugEnabled()) {
			log.debug("UserInfo: " + userInfo);
			log.debug("Session key: " + sessionKey);
		}

		requestContextCleaner.clean(userInfo);
		
	}


}
