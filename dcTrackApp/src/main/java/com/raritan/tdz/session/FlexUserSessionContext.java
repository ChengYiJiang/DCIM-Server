package com.raritan.tdz.session;

import java.sql.Timestamp;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.FlexSessionAttributeListener;
import flex.messaging.FlexSessionBindingEvent;
import flex.messaging.FlexSessionListener;

/**
 * Provides the current dcTrack user session from the Flex session context.
 * @author andrewc
 *
 */
public class FlexUserSessionContext implements UserSessionContext {
	private String sessionKey;
	private static Logger log = Logger.getLogger(UserSessionManager.class);
	private static boolean allowMockUser = false;
	private static String mockUserId = "mockUser";
	private DCTrackSessionManagerInterface sessionMgr;
	private final static String debugSessionKey = "IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS";
	
	public FlexUserSessionContext(){
		//Do nothing.
	}
	/*static {
		FlexSession.addSessionCreatedListener( new SessionListener() );
	}*/
	
	public FlexUserSessionContext(DCTrackSessionManagerInterface sessionMgr){
		this.sessionMgr = sessionMgr;
	}
	/**
	 * Setting this flag will allow the session context to return a Mock user
	 * if there is no FlexSession. This should ONLY be used for unit tests!!
	 */
	public static void setAllowMockUser(boolean allowMockUser) {
		FlexUserSessionContext.allowMockUser = allowMockUser;
	}
	
	public static void setMockUserId(String mockUserId) {
		FlexUserSessionContext.mockUserId = mockUserId;
	}
	
	/**
	 * Returns the user associated with the current flex session.
	 * @return
	 */
	public static UserInfo getUser() {
		UserInfo userInfo = null;
		FlexSession session = FlexContext.getFlexSession();
		
		if (session != null) {
			userInfo = (UserInfo)session.getAttribute(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
			
			long timeoutPeriod = session.getTimeoutPeriod();
			
			if (log.isDebugEnabled()) {
				// DEBUG logging for BlazeDS session status
				HttpSession httpSession = FlexContext.getHttpRequest().getSession();
				int flexSessionTimeoutMins = (int)(timeoutPeriod / 60000);
				int httpSessionTimeoutMins = (int)(httpSession.getMaxInactiveInterval() / 60);
				log.debug("getUser: BlazeDS Session ID: " + session.getId() + 
						",\n\t Http Session ID: " + httpSession.getId() +
						",\n\t userInfo: " + userInfo +
						",\n\t FlexSession Timeout (minutes): " + flexSessionTimeoutMins +
						",\n\t HttpSession Timeout (minutes): " + httpSessionTimeoutMins
				);
			}
			
			// Detect sessions without proper timeout user information
			if (timeoutPeriod <= 0 || userInfo == null) {
				log.warn("Session ID " + session.getId() + " : was initiated without authentication credentials! Circuits will not save properly until user logs in again!");
			}
		}
		
		if (userInfo == null && allowMockUser) {
			userInfo = createMockUser();
		}
		
		return userInfo;
	}

    /**
     * Get lastAccessedTime (milliseconds).
     */
    public static long getLastAccessedTime() {
        FlexSession session = FlexContext.getFlexSession();
        if (session != null) {
            return session.getLastUse();
        }
        return -1;
    }

	public UserSession getUserSession() {
		return new FlexSessionWrapper(sessionMgr,sessionKey);
	}
	
	public String getSessionKey() {
		return sessionKey;
	}
	
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	
	//
	// Private methods and classses
	//
	
	public static UserInfo createMockUser() {
		UserInfo user = new UserInfo();
		user.setUserName("Mock User");
		user.setUserId("1");  //This field is the users.id, not a string field
		user.setAccessLevelId( Integer.toString( UserAccessLevel.ADMIN.getAccessLevel() ) );
		user.setSessionId(debugSessionKey);
		user.setSessionTimeout(5);
		user.setId(1);
		return user;
	}
	
	/*
	 * User Session implementation using Flex session.
	 */
	private class FlexSessionWrapper implements UserSession {
		private FlexSession session;
		private HttpSession httpSession;
		private DCTrackSessionManagerInterface sessionMgr;
		private String sessionKey;
		//public static final String DCTRACK_USER_LAST_TIME_ACCESSED = "LastAccessedTime";
		
		public FlexSessionWrapper(DCTrackSessionManagerInterface sessionMgr,String sessionKey) {
			this.session = FlexContext.getFlexSession();
			this.httpSession = FlexContext.getHttpRequest().getSession();
			this.sessionMgr = sessionMgr;
			this.sessionKey = sessionKey;
		}
		@Override
		public String getId() {
			return session.getId();
		}
		@Override
		public void setDcTrackUser(UserInfo userInfo) {
			session.setAttribute(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY, userInfo);
		}
		@Override
		public UserInfo getDcTrackUser() {
			return (UserInfo)session.getAttribute(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
		}
		@Override
		public void removeDcTrackUser() {
			session.removeAttribute(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
			if (log.isDebugEnabled()) {
				log.debug("BlazeDS: Removing dcTrack User session");
			}
		}
		@Override
		public void setLastAccessedTime(long timeInMillis) {
			session.setLastUse(timeInMillis);
			if (sessionMgr != null){
				Timestamp lastAccessedTime = new Timestamp(timeInMillis);
				sessionMgr.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED, lastAccessedTime);
			}
			if (log.isDebugEnabled()) {
				log.debug("BlazeDS Session ID: "+session.getId());
			}
		}
		@Override
		public long getLastAccessedTime() {
			return session.getLastUse();
		}
		@Override
		public void setSessionTimeoutPeriod(long milliSecs) {
			session.setTimeoutPeriod( milliSecs );
			int timeoutSecs = (int)(milliSecs / 1000);
			httpSession.setMaxInactiveInterval( timeoutSecs );
			
			if (log.isDebugEnabled()) {
				log.debug("BlazeDS session timeout: " + milliSecs + "(SessionID: " + session.getId()+")");
				session.addSessionDestroyedListener( new SessionListener() );
				session.addSessionAttributeListener( new AttributeListener() );
			}
		}
		@Override
		public long getSessionTimeoutPeriod() {
			return session.getTimeoutPeriod();
		}
	}
	
	private static class AttributeListener implements FlexSessionAttributeListener {

		@Override
		public void attributeAdded(FlexSessionBindingEvent arg0) {
			log.debug("session attributeAdded: name=" + arg0.getName() + ", value=" + arg0.getValue());
		}

		@Override
		public void attributeRemoved(FlexSessionBindingEvent arg0) {
			log.debug("session attributeRemoved: name=" + arg0.getName() + ", value=" + arg0.getValue());
		}

		@Override
		public void attributeReplaced(FlexSessionBindingEvent arg0) {
			log.debug("session attributeReplaced: name=" + arg0.getName() + ", value=" + arg0.getValue());
		}
	}
	
	private static class SessionListener implements FlexSessionListener {

		@Override
		public void sessionCreated(FlexSession arg0) {
			log.debug("FlexSession: sessionCreated: " + arg0.getId());
		}

		@Override
		public void sessionDestroyed(FlexSession arg0) {
			log.debug("FlexSession: sessionDestroyed: " + arg0.getId());
		}
	}
}
