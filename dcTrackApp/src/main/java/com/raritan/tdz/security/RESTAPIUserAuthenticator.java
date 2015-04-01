package com.raritan.tdz.security;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.ServiceLayerException;


import com.raritan.tdz.session.DCTrackSessionManager;
import com.raritan.tdz.session.RESTAPIUserSessionContext;
import com.raritan.tdz.session.UserSession;
import com.raritan.tdz.util.AuthUtil;
import com.raritan.tdz.util.ClientFormLogin;

/**
 * Authenticates the dcTrack session key passed from the Flex client.
 * @author Andrew Cohen
 *
 */
public class RESTAPIUserAuthenticator extends AbstractUserDetailsAuthenticationProvider {

	private SessionFactory sessionFactory;
	
	private String sessionKey = null;
	List<Cookie> sessionCookies = null;
	
	@Autowired
	private DCTrackSessionManager dcTrackSessionMgr;
	
	public final static String debugSessionKey = "IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS";
	
	public RESTAPIUserAuthenticator(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {
//		return super.authenticate(auth);
		sessionKey = getSessionKey(auth);
		return authenticateSessionKey((UsernamePasswordAuthenticationToken)auth);
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authToken)
			throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authToken)
			throws AuthenticationException {
		return null;
	}
	
	private Authentication authenticateSessionKey(UsernamePasswordAuthenticationToken authToken) {
		UserInfo user = null;
		AuthUtil auth = AuthUtil.getInstance(sessionFactory.getCurrentSession());
		boolean ignoreSession = Boolean.parseBoolean(System.getProperty("dcTrack.ignoreSession"));
		
		
		String userSessionKey = sessionKey != null ? sessionKey : debugSessionKey; 
		List<Cookie> cookies = sessionCookies != null ? sessionCookies : new ArrayList<Cookie>();
		if (!ignoreSession && cookies.isEmpty())
			userSessionKey = ClientFormLogin.login("https://localhost/", "https://localhost/login/login?redir=/webclient/", authToken.getPrincipal().toString(), authToken.getCredentials().toString(),cookies);
		
		List<GrantedAuthority> roles = new LinkedList<GrantedAuthority>();
		roles.add( new GrantedAuthorityImpl("ROLE_USER") );
		Authentication newAuth = new UsernamePasswordAuthenticationToken(authToken.getPrincipal(), authToken.getCredentials(), roles);
		
		RESTAPIAuthenticationDetails details = (RESTAPIAuthenticationDetails) authToken.getDetails();
		
		RESTAPIUserSessionContext sessionContext = new RESTAPIUserSessionContext(newAuth,cookies,dcTrackSessionMgr, details.getUri());
		sessionContext.setSessionKey(userSessionKey);
		
		//********************************** N O T E **********************************
		//NOTE: You may think why we are performing authentication after login! 
		//NOTE: The reason for this is that auth.authenticate not only does an authentication
		//NOTE: against the current session key, it also sets up the user info to the 
		//NOTE: UserSessionManager instance. It also sets up timeouts etc in the userInfo
		//NOTE: We do not want to loose all these functionality and repeat some functionality 
		//NOTE: again here. The maintenance will be very difficult. As this is not costly opn.
		//NOTE: calling authenticate will not be a big issue.
		//************************************N O T E ***********************************
		try {
			// The dcTrack session key is the "password". This is what we validate.
			// The dcTrack username is also passed, but we don't do anything with this now.
			user = auth.authenticate(sessionContext);
		}
		catch (ServiceLayerException e) {
			throw new BadCredentialsException("Error authenticating session", e);
		}
		
		if (user == null) {
			throw new BadCredentialsException("Authentication failed.");
		}
		
		if (userSessionKey == null || userSessionKey.isEmpty()){
			throw new BadCredentialsException("You do not have access to the resource.");
		}
		
	
		return newAuth;
	}
	
	
	//-------- Private methods ----------
	
	private String getSessionKey(Authentication auth){
		String sessionKey = null;
		RESTAPIAuthenticationDetails details = (RESTAPIAuthenticationDetails) auth.getDetails();
		sessionCookies = new ArrayList<Cookie>();
		if (details.getCookie() != null){
			
			for (javax.servlet.http.Cookie cookie:details.getCookie()){
				sessionCookies.add(apacheCookieFromServletCookie(cookie));
				if (cookie.getName().equals("DCTRACK_INIT_SESSION")){
					sessionKey = cookie.getValue();
				}
			}
		}
		
		boolean ignoreSession = Boolean.parseBoolean(System.getProperty("dcTrack.ignoreSession"));
		boolean restAPITests = Boolean.parseBoolean(System.getProperty("dcTrack.testRest"));
		if( ignoreSession == true && restAPITests == true ){
			BasicClientCookie debugCookie = new BasicClientCookie("DCTRACK_INIT_SESSION", debugSessionKey);
			sessionCookies.add(debugCookie);
		}
		return sessionKey;
	}
	
	private static BasicClientCookie apacheCookieFromServletCookie(javax.servlet.http.Cookie cookie) {
		  if(cookie == null) {
		   return null;
		  }
		   
		  BasicClientCookie apacheCookie = null;
		   
		  // get all the relevant parameters
		     String domain = cookie.getDomain();
		     String name = cookie.getName();
		     String value = cookie.getValue();
		     String path = cookie.getPath();
		     int maxAge = cookie.getMaxAge();
		     boolean secure = cookie.getSecure();
		      
		     // create the apache cookie
		     apacheCookie = new BasicClientCookie(name, value);
		      
		     // set additional parameters
		     apacheCookie.setDomain(domain);
		     apacheCookie.setPath(path);
		     apacheCookie.setValue(value);
		     apacheCookie.setSecure(secure);
		     apacheCookie.setVersion(cookie.getVersion());
		     apacheCookie.setComment(cookie.getComment());
		 
		     // return the apache cookie
		     return apacheCookie;
		 }
}
