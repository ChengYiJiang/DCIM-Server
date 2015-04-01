/**
 * 
 */
package com.raritan.tdz.sslclient.home;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * @author prasanna
 *
 */
public class PIQSSLHostnameVerifier implements X509HostnameVerifier {
	
	String hostname = null;
	
	public PIQSSLHostnameVerifier(String hostname){
		this.hostname = hostname;
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
	 */
	@Override
	public boolean verify(String hostname, SSLSession session) {
		return this.hostname.equals(hostname);
	}

	@Override
	public void verify(String host, SSLSocket ssl) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verify(String host, X509Certificate cert) throws SSLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verify(String host, String[] cns, String[] subjectAlts)
			throws SSLException {
		// TODO Auto-generated method stub
		
	}

}
