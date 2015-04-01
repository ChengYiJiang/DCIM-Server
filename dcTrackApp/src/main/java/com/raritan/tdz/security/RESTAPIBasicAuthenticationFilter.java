/**
 * 
 */
package com.raritan.tdz.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Assert;

/**
 * @author prasanna
 *
 */
@SuppressWarnings("deprecation")
public class RESTAPIBasicAuthenticationFilter extends BasicAuthenticationFilter {
	
	private RememberMeServices rememberMeServices = new NullRememberMeServices();
	
	private AuthenticationDetailsSource<HttpServletRequest,?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

	  /**
     * @deprecated Use constructor injection
     */
    public RESTAPIBasicAuthenticationFilter() {
    	super();
    }

    /**
     * Creates an instance which will authenticate against the supplied {@code AuthenticationManager}
     * and which will ignore failed authentication attempts, allowing the request to proceed down the filter chain.
     *
     * @param authenticationManager the bean to submit authentication requests to
     */
    public RESTAPIBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    /**
     * Creates an instance which will authenticate against the supplied {@code AuthenticationManager} and
     * use the supplied {@code AuthenticationEntryPoint} to handle authentication failures.
     *
     * @param authenticationManager the bean to submit authentication requests to
     * @param authenticationEntryPoint will be invoked when authentication fails. Typically an instance of
     * {@link BasicAuthenticationEntryPoint}.
     */
    public RESTAPIBasicAuthenticationFilter(AuthenticationManager authenticationManager,
                                     AuthenticationEntryPoint authenticationEntryPoint) {
    	super(authenticationManager, authenticationEntryPoint);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
    	
    	final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        final boolean debug = logger.isDebugEnabled();
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Basic ")) {
            super.doFilter(request, response, chain);
            return;
        }
        
        try {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("", "");
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        Authentication authResult = getAuthenticationManager().authenticate(authRequest);

        if (debug) {
            logger.debug("Authentication success: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

        rememberMeServices.loginSuccess(request, response, authResult);

        onSuccessfulAuthentication(request, response, authResult);
        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();

            if (debug) {
                logger.debug("Authentication request for failed: " + failed);
            }

            rememberMeServices.loginFail(request, response);

            onUnsuccessfulAuthentication(request, response, failed);

            if (isIgnoreFailure()) {
                chain.doFilter(request, response);
            } else {
                getAuthenticationEntryPoint().commence(request, response, failed);
            }

            return;
        }

        chain.doFilter(request, response);
    	
    }
    
    @Override
    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest,?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
        super.setAuthenticationDetailsSource(authenticationDetailsSource);
    }

    @Override
    public void setRememberMeServices(RememberMeServices rememberMeServices) {
        Assert.notNull(rememberMeServices, "rememberMeServices cannot be null");
        this.rememberMeServices = rememberMeServices;
        super.setRememberMeServices(rememberMeServices);
    }
	
}
