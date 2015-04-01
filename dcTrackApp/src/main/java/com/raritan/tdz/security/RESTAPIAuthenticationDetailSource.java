/**
 * 
 */
package com.raritan.tdz.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;

/**
 * @author prasanna
 *
 */
public class RESTAPIAuthenticationDetailSource implements
		AuthenticationDetailsSource<HttpServletRequest, RESTAPIAuthenticationDetails> {

	@Override
	public RESTAPIAuthenticationDetails buildDetails(HttpServletRequest context) {
		// TODO Auto-generated method stub
		return new RESTAPIAuthenticationDetails(context);
	}

}
