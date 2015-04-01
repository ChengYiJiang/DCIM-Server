/**
 * 
 */
package com.raritan.tdz.security;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.cookie.Cookie;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.raritan.tdz.session.RESTAPIUserSessionContext;
import com.raritan.tdz.util.ClientFormLogin;




/**
 * @author prasanna
 *
 */
@Aspect
public class RESTAPILogoutHandler{
	
	Logger log = Logger.getLogger(this.getClass().getName());
	private final String logoutURL = "https://localhost/login/logout?redir=/webclient/";
	
	@After("execution(public * com.raritan.tdz.*.assetmgmt.AssetMgmtController.*(..)) && @annotation( org.springframework.web.bind.annotation.RequestMapping ) || " +
			"execution(public * com.raritan.tdz.*.assetmgmt.MobileSearchController.*(..)) && @annotation( org.springframework.web.bind.annotation.RequestMapping )")
	public void logout(JoinPoint joinPoint){
		
		
		List<Cookie> cookies = RESTAPIUserSessionContext.getCookies();
		ClientFormLogin.logout(logoutURL, cookies);
	}

}
