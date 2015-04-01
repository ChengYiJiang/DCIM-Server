package com.raritan.tdz.sslclient.home;

import javax.net.ssl.X509TrustManager;

/**
 * The "all trusting" trust manager used by the AcceptAllCertsProtocolSocketFactory.
 */
class AcceptAllCertsTrustManager implements X509TrustManager {
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(
			java.security.cert.X509Certificate[] certs, String authType) {
	}

	public void checkServerTrusted(
			java.security.cert.X509Certificate[] certs, String authType) {
	}
}