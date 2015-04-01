/**
 * 
 */
package com.raritan.tdz.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.raritan.tdz.sslclient.home.PIQSSLHostnameVerifier;

/**
 * @author prasanna
 *
 */
public class dcTrackRestTemplate extends RestTemplate {

	private int soTimeout = 0;
	
	private int connectionTimeout = 0;
	
	private PIQSSLHostnameVerifier piqSSLHostVerifier;
	
	public dcTrackRestTemplate() {
		super();
	}
	
	public void init(){
		this.setRequestFactory(createHttpRequestFactory());
		this.setSoTimeout(getSoTimeout());
		this.setConnectionTimeout(getConnectionTimeout());
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
		
		if (this.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
			HttpComponentsClientHttpRequestFactory rf =
					(HttpComponentsClientHttpRequestFactory) this.getRequestFactory();
			
			rf.setReadTimeout(soTimeout);
		}
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		
		if (this.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory){
			
			HttpComponentsClientHttpRequestFactory rf =
					(HttpComponentsClientHttpRequestFactory) this.getRequestFactory();
			
			rf.setConnectTimeout(connectionTimeout);
		}
	}
	
	
	
	public PIQSSLHostnameVerifier getPiqSSLHostVerifier() {
		return piqSSLHostVerifier;
	}

	public void setPiqSSLHostVerifier(PIQSSLHostnameVerifier piqSSLHostVerifier) {
		this.piqSSLHostVerifier = piqSSLHostVerifier;
	}

	private HttpComponentsClientHttpRequestFactory createHttpRequestFactory(){
		HttpClient httpClient = new DefaultHttpClient();
		
		try {
			httpClient = createHttpClient();
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}
	
	private HttpClient createHttpClient() throws NoSuchAlgorithmException, KeyManagementException{
		SSLContext sslContext = SSLContext.getInstance("SSL");
		
		// Set up a TrustManager that trusts everything
		sslContext.init(null, new TrustManager[] {
				new X509TrustManager() {
					
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						// TODO Auto-generated method stub
						
					}
				}
		}, new SecureRandom());
		
		X509HostnameVerifier verifier = piqSSLHostVerifier == null ? SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER : piqSSLHostVerifier;
		
		SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(sslContext,verifier);
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sf)
                .build());
		
		HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build();
		
		return httpClient;
	}
	
}
