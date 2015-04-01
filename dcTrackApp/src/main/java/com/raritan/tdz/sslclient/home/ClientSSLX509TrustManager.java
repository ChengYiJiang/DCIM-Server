package com.raritan.tdz.sslclient.home;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A trust manager used by the ClientSSLProtocolSocketFactory.
 */
public class ClientSSLX509TrustManager implements X509TrustManager {
	
    private X509TrustManager defaultTrustManager = null;

    /** Log object for this class. */
    private static final Log log = LogFactory.getLog(ClientSSLX509TrustManager.class);
    
    public ClientSSLX509TrustManager(final X509TrustManager defaultTrustManager) {
        super();
        if (defaultTrustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.defaultTrustManager = defaultTrustManager;
    }
    
    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
        if (log.isInfoEnabled() && certificates != null) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                log.info(" Client certificate " + (c + 1) + ":");
                log.info("  Subject DN: " + cert.getSubjectDN());
                log.info("  Signature Algorithm: " + cert.getSigAlgName());
                log.info("  Valid from: " + cert.getNotBefore() );
                log.info("  Valid until: " + cert.getNotAfter());
                log.info("  Issuer: " + cert.getIssuerDN());
            }
        }
        defaultTrustManager.checkClientTrusted(certificates,authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
        if (log.isInfoEnabled() && certificates != null) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                log.info(" Server certificate " + (c + 1) + ":");
                log.info("  Subject DN: " + cert.getSubjectDN());
                log.info("  Signature Algorithm: " + cert.getSigAlgName());
                log.info("  Valid from: " + cert.getNotBefore() );
                log.info("  Valid until: " + cert.getNotAfter());
                log.info("  Issuer: " + cert.getIssuerDN());
            }
        }
        defaultTrustManager.checkServerTrusted(certificates,authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.defaultTrustManager.getAcceptedIssuers();
    }
}
