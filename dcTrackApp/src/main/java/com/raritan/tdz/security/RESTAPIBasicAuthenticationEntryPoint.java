package com.raritan.tdz.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

public class RESTAPIBasicAuthenticationEntryPoint extends
		BasicAuthenticationEntryPoint {
	public RESTAPIBasicAuthenticationEntryPoint(){
		super.setRealmName("Raritan dcTrack REST API");
	}
	
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
		
		response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
		response.setHeader("Content-Type", "application/json");

        if (request.getCookies() != null && request.getCookies().length > 0) {
            // This is an HTTP Header REST call, if there are cookies in the HTTP request.
            // CR50969: Return other codes instead of 401 to avoid the authentication warning from the browsers.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            // This is a 3rd-party REST call, if there is no cookie in the HTTP request.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

		PrintWriter writer = response.getWriter();
		writer.println("{\"errors\":{},\"warnings\":{},\"trace\":null,\"message\":\"" + authException.getMessage() + "\"}");
		writer.flush();
    }
}
