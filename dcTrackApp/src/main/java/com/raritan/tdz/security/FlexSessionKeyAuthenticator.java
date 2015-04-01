package com.raritan.tdz.security;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.raritan.tdz.session.DCTrackSessionManagerInterface;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.AuthUtil;

/**
 * Authenticates the dcTrack session key passed from the Flex client.
 * @author Andrew Cohen
 *
 */
public class FlexSessionKeyAuthenticator extends AbstractUserDetailsAuthenticationProvider {

	private SessionFactory sessionFactory;
	
	@Autowired
	private DCTrackSessionManagerInterface dcTrackSessionMgr;
	
	public FlexSessionKeyAuthenticator(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {
//		return super.authenticate(auth);
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
		
		FlexUserSessionContext sessionContext = new FlexUserSessionContext(dcTrackSessionMgr);
		sessionContext.setSessionKey( authToken.getCredentials().toString());
		
		try {
			// The dcTrack session key is the "password". This is what we validate.
			// The dcTrack username is also passed, but we don't do anything with this now.
			user = auth.authenticate(sessionContext);
		}
		catch (ServiceLayerException e) {
			throw new BadCredentialsException("Error authenticating session", e);
		}
		
		if (user == null) {
			throw new BadCredentialsException("Your session has timed out.");
		}
		
		List<GrantedAuthority> roles = new LinkedList<GrantedAuthority>();
		roles.add( new GrantedAuthorityImpl("ROLE_USER") );
		//System.out.println("--- Auth ---");
		return new UsernamePasswordAuthenticationToken(authToken.getPrincipal(), authToken.getCredentials(), roles);
	}
}
