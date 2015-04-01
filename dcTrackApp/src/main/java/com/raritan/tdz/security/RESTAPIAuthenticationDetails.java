/**
 * 
 */
package com.raritan.tdz.security;

import java.io.Serializable;
import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * @author prasanna
 *
 */
public class RESTAPIAuthenticationDetails implements Serializable {
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
	  
	private final String remoteAddress;
	private final String sessionId;
	  
	private final Cookie[] cookie;
	
	private final String uri;
	 
	public RESTAPIAuthenticationDetails(HttpServletRequest request){
		this.remoteAddress = request.getRemoteAddr();
		
		HttpSession session = request.getSession(false);
		this.sessionId = (session != null) ? session.getId() : null;
		this.uri = request.getRequestURI();
		   
		cookie = request.getCookies();
	}

	
	
	public Cookie[] getCookie() {
		return cookie;
	}



	public String getRemoteAddress() {
		return remoteAddress;
	}



	public String getSessionId() {
		return sessionId;
	}



	public String getUri() {
		return uri;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
		result = prime * result
				+ ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RESTAPIAuthenticationDetails other = (RESTAPIAuthenticationDetails) obj;
		if (remoteAddress == null) {
			if (other.remoteAddress != null)
				return false;
		} else if (!remoteAddress.equals(other.remoteAddress))
			return false;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RESTAPIAuthenticationDetails [remoteAddress=" + remoteAddress
				+ ", sessionId=" + sessionId + ", cookie="
				+ Arrays.toString(cookie) + ", uri=" + uri + "]";
	}
	  
	
}
