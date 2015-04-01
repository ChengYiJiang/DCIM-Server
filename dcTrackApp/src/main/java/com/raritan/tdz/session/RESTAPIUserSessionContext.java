package com.raritan.tdz.session;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;

/**
 * Provides the current dcTrack user session from the Flex session context.
 * @author andrewc
 *
 */
public class RESTAPIUserSessionContext implements UserSessionContext {
	private String sessionKey;
	private static Logger log = Logger.getLogger(UserSessionManager.class);
	private static boolean allowMockUser = false;
	private Authentication auth = null;
	private List<Cookie> cookies = null;
	private DCTrackSessionManager session = null;
	private String uri = null;
	
	
	public RESTAPIUserSessionContext(Authentication auth, List<Cookie> cookies, DCTrackSessionManager session, String uri){
		this.auth = auth;
		this.cookies = cookies;
		this.session = session;
		this.uri = uri;
	}
	
	/*static {
		FlexSession.addSessionCreatedListener( new SessionListener() );
	}*/
	
	/**
	 * Setting this flag will allow the session context to return a Mock user
	 * if there is no FlexSession. This should ONLY be used for unit tests!!
	 */
	public static void setAllowMockUser(boolean allowMockUser) {
		RESTAPIUserSessionContext.allowMockUser = allowMockUser;
	}
	
	/**
	 * Returns the user associated with the current flex session.
	 * @return
	 */
	public static UserInfo getUser() {
		UserInfo userInfo = null;
		UsernamePasswordAuthenticationToken authObject = (UsernamePasswordAuthenticationToken) getAuthenticationObject();
		
		if (authObject != null) {
			@SuppressWarnings("unchecked")
			Map<String,Object> authDetailMap = (Map<String, Object>) authObject.getDetails();
			
			String sessionKey = getSessionKey(authDetailMap);
			
			DCTrackSessionManager sessionContext = (DCTrackSessionManager)authDetailMap.get(DCTrackSessionManager.DCTRACK_SESSION_CONTEXT);
			if (sessionContext.getAttribute(sessionKey,DCTrackSessionManager.DCTRACK_USER_SESSION_KEY) != null)
				userInfo = (UserInfo)sessionContext.getAttribute(sessionKey,DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
			
			long timeoutPeriod = 0;
			if (sessionContext.getAttribute(sessionKey,DCTrackSessionManager.DCTRACK_USER_TIMEOUT_PERIOD) != null)
			//long timeoutPeriod = (Long) authDetailMap.get(RESTAPISessionWrapper.DCTRACK_USER_TIMEOUT_PERIOD);
				timeoutPeriod = (Long)sessionContext.getAttribute(sessionKey,DCTrackSessionManager.DCTRACK_USER_TIMEOUT_PERIOD);
			
			if (log.isDebugEnabled()) {
				// DEBUG logging for BlazeDS session status
				
				int flexSessionTimeoutMins = (int)(timeoutPeriod / 60000);
				int httpSessionTimeoutMins = (int)((Long)sessionContext.getAttribute(sessionKey,DCTrackSessionManager.DCTRACK_MAX_INACTIVE_INTERVAL) / 60);
				log.debug("getUser: BlazeDS Session ID: " + sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_ID) + 
						",\n\t userInfo: " + userInfo +
						",\n\t RESTAPI Timeout (minutes): " + flexSessionTimeoutMins +
						",\n\t HttpSession Timeout (minutes): " + httpSessionTimeoutMins
				);
			}
			
			// Detect sessions without proper timeout user information
			if (timeoutPeriod <= 0 || userInfo == null) {
				log.warn("Session ID " +  sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_ID) + " : was initiated without authentication credentials! Circuits will not save properly until user logs in again!");
			}
		}
		
		if (userInfo == null && allowMockUser) {
			userInfo = createMockUser();
		}
		
		return userInfo;
	}

	private static String getSessionKey(Map<String, Object> authDetailMap) {
		String sessionKey = null;
		List<Cookie> cookies = (List<Cookie>) authDetailMap.get(DCTrackSessionManager.DCTRACK_COOKIES);
		
		for (Cookie cookie:cookies){
			if (cookie != null && cookie.getName().equals("DCTRACK_INIT_SESSION")){
				sessionKey = cookie.getValue();
			}
		}
		return sessionKey;
	}

    /**
     * Check if session is timed out.
     * @throws Throwable 
     */
    public static boolean sessionTimedOut() throws Throwable {
        UserInfo userInfo = null;
        UsernamePasswordAuthenticationToken authObject = (UsernamePasswordAuthenticationToken) getAuthenticationObject();

        boolean sessionTimedOut = true;

        if (authObject != null) {
            @SuppressWarnings("unchecked")
            Map<String,Object> authDetailMap = (Map<String, Object>) authObject.getDetails();
            String sessionKey = getSessionKey(authDetailMap);
            DCTrackSessionManager sessionContext = (DCTrackSessionManager) authDetailMap.get(DCTrackSessionManager.DCTRACK_SESSION_CONTEXT);
            userInfo = (UserInfo)sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);

            long currentTime = System.currentTimeMillis();
            long lastAccessedTime = 0;
            if (sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) != null) {
            	Timestamp lastAccessedTimestamp =  (Timestamp) sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED);                            
                lastAccessedTime = lastAccessedTimestamp.getTime();
            }

            // Check if idle time > session timeout.
            sessionTimedOut = (currentTime - lastAccessedTime) > userInfo.getSessionTimeout();
        }

        if (sessionTimedOut) {
            clearSession();
        }

        return sessionTimedOut;
    }

    /**
     * Clear all attributes in the RESTAPISessionContext.
     * @throws Throwable 
     */
    public static void clearSession() throws Throwable {
        UserInfo userInfo = null;
        UsernamePasswordAuthenticationToken authObject = (UsernamePasswordAuthenticationToken) getAuthenticationObject();

        if (authObject != null) {
            @SuppressWarnings("unchecked")
            Map<String,Object> authDetailMap = (Map<String, Object>) authObject.getDetails();
            String sessionKey = getSessionKey(authDetailMap);
            DCTrackSessionManager sessionContext = (DCTrackSessionManager) authDetailMap.get(DCTrackSessionManager.DCTRACK_SESSION_CONTEXT);

            sessionContext.clearAttributes(sessionKey);
        }
    }

    /**
     * Get lastAccessedTime (milliseconds).
     */
    public static long getLastAccessedTime() {
        UsernamePasswordAuthenticationToken authObject = (UsernamePasswordAuthenticationToken) getAuthenticationObject();

        if (authObject != null) {
            @SuppressWarnings("unchecked")
            Map<String,Object> authDetailMap = (Map<String, Object>) authObject.getDetails();
            String sessionKey = getSessionKey(authDetailMap);
            DCTrackSessionManager sessionContext = (DCTrackSessionManager)authDetailMap.get(DCTrackSessionManager.DCTRACK_SESSION_CONTEXT);
            if (sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) != null) {
            	Timestamp lastAccessedTimestamp = (Timestamp) sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED);
                return lastAccessedTimestamp.getTime();
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

	@SuppressWarnings("unchecked")
	public static List<Cookie> getCookies() {
		List<Cookie> cookies = null;
		Authentication authObject = getAuthenticationObject();
		
		if (authObject != null){
			Map<String,Object> authDetailMap = (Map<String,Object>)authObject.getDetails();
			String sessionKey = getSessionKey(authDetailMap);
	        DCTrackSessionManager sessionContext = (DCTrackSessionManager)authDetailMap.get(DCTrackSessionManager.DCTRACK_SESSION_CONTEXT);
			cookies = (List<Cookie>) sessionContext.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_COOKIES);
		}
		
		return cookies;
	}
	
	public static Authentication getAuthenticationObject(){
		Authentication authObject = null;
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null){
			authObject = securityContext.getAuthentication();
		}
		
		return authObject;
	}
	
	public UserSession getUserSession() {
		return new RESTAPISessionWrapper(session,auth, cookies, sessionKey, uri);
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
	
	private static UserInfo createMockUser() {
		UserInfo user = new UserInfo();
		user.setUserName("Mock User");
		user.setUserId("1");  //This field is the users.id, not a string field
		user.setAccessLevelId( Integer.toString( UserAccessLevel.ADMIN.getAccessLevel() ) );
		return user;
	}
	
	/*
	 * User Session implementation using Flex session.
	 */
	private class RESTAPISessionWrapper implements UserSession {
//		public static final String DCTRACK_ID = "dcTrack.sessionKey";
//		public static final String DCTRACK_USER_LAST_TIME_ACCESSED = "LastAccessedTime";
//		public static final String DCTRACK_USER_TIMEOUT_PERIOD = "TimeoutPeriod";
//		public static final String DCTRACK_MAX_INACTIVE_INTERVAL = "MaxInactiveInterval";
//		public static final String DCTRACK_COOKIES = "dcTrack.Cookies";
//		public static final String DCTRACK_SESSION_CONTEXT = "dcTrack.SessionContext";
		private DCTrackSessionManager session = null;
		private String sessionKey = null;
		private String uri = null;
		
		private UsernamePasswordAuthenticationToken authObject = null;
		Map<String, Object> authDetailMap = new HashMap<String, Object>();
		
		public RESTAPISessionWrapper(DCTrackSessionManager session, Authentication authObject, List<Cookie> cookies, String sessionKey, String uri) {
			this.sessionKey = sessionKey;
			this.session = session;
			this.authObject = (UsernamePasswordAuthenticationToken) authObject;
			this.uri = uri;
			if (this.authObject != null){
				this.authObject.setDetails(authDetailMap);
				this.session.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_ID, authObject.getCredentials());
				this.session.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_COOKIES, cookies);
				authDetailMap.put(DCTrackSessionManager.DCTRACK_SESSION_CONTEXT, session);
				authDetailMap.put(DCTrackSessionManager.DCTRACK_ID, authObject.getCredentials());
				authDetailMap.put(DCTrackSessionManager.DCTRACK_COOKIES, cookies);
			}
		}
		
		@Override
		public String getId() {
			return (String)session.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_ID);
			//return (String)authDetailMap.get(DCTRACK_ID);
		}
		@Override
		public void setDcTrackUser(UserInfo userInfo) {
			session.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_SESSION_KEY, userInfo);
			//authDetailMap.put(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY, userInfo);
		}
		@Override
		public UserInfo getDcTrackUser() {
			return (UserInfo)session.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
			//return (UserInfo)authDetailMap.get(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
		}
		@Override
		public void removeDcTrackUser() {
			session.removeAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
			//authDetailMap.remove(DCTrackSessionManager.DCTRACK_USER_SESSION_KEY);
			if (log.isDebugEnabled()) {
				log.debug("RESTAPI: Removing dcTrack User session");
			}
		}
		@Override
		public void setLastAccessedTime(long timeInMillis) {
			Boolean skipSetup = uri != null ? (Boolean) uri.contains("checkSession") : false;
			
			if (!skipSetup){
				Timestamp lastAccessedTime = new Timestamp(timeInMillis);
				session.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED, lastAccessedTime);
				//authDetailMap.put(DCTRACK_USER_LAST_TIME_ACCESSED, timeInMillis);
				if (log.isDebugEnabled()) {
					log.debug("RESTAPI Session ID: "+getId());
				}
			}
		}
		@Override
		public long getLastAccessedTime() {
			Timestamp lastAccessedTime = session.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) != null?
					(Timestamp)session.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) : new Timestamp(0L);
			return lastAccessedTime.getTime();
			//return (Long) authDetailMap.get(DCTRACK_USER_LAST_TIME_ACCESSED);
		}
		@Override
		public void setSessionTimeoutPeriod(long milliSecs) {
			session.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_TIMEOUT_PERIOD, milliSecs);
			//authDetailMap.put(DCTRACK_USER_TIMEOUT_PERIOD, milliSecs );
			int timeoutSecs = (int)(milliSecs / 1000);
			session.addAttribute(sessionKey, DCTrackSessionManager.DCTRACK_MAX_INACTIVE_INTERVAL, timeoutSecs);
			//authDetailMap.put(DCTRACK_MAX_INACTIVE_INTERVAL, timeoutSecs);
			if (log.isDebugEnabled()) {
				log.debug("RESTAPI session timeout: " + milliSecs + "(SessionID: " + getId()+")");
			}
		}
		@Override
		public long getSessionTimeoutPeriod() {
			return (Long) session.getAttribute(sessionKey, DCTrackSessionManager.DCTRACK_USER_TIMEOUT_PERIOD);
			//return (Long) authDetailMap.get(DCTRACK_USER_TIMEOUT_PERIOD);
		}
	}


}
