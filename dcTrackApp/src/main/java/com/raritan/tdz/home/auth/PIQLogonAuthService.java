package com.raritan.tdz.home.auth;

//import org.apache.log4j.Logger;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ClientFormLogin;

/**
 * Authentication service implementation which performs a PIQ logon
 * on every authentication request.
 * @author Andrew Cohen
 *
 */
public class PIQLogonAuthService implements AuthHome {
	private String loginPageUrl;
	private String loginActionUrl;
	//private static Logger appLogger = Logger.getLogger(AuthServiceImpl.class);
	
	public PIQLogonAuthService(String loginPageUrl, String loginActionUrl)
	{
		this.loginPageUrl = loginPageUrl;
		this.loginActionUrl = loginActionUrl;
	}
	
	@Override
	public UserInfo authenticate(String username, String password)
			throws DataAccessException {
		//return this.authHome.authentication(username, password);
		String sessionId = ClientFormLogin.login(
				loginPageUrl, loginActionUrl, username, password);
		if (sessionId == null || sessionId.trim().length() == 0) return null;
			
		UserInfo user = new UserInfo();
		user.setSessionId(sessionId);
		// TODO: Add additional user info
		return new UserInfo();
	}
}
