package com.raritan.tdz.sslclient.home;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A customizable SSL socket factory based on the Apache HttpClient SSL contribution:
 * http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/contrib/org/apache/commons/httpclient/contrib/ssl/ 
 * 
 * This implementation combines elements of the flexible AuthSSLProtocolSocketFactory implementation with the
 * StrictSSLProtocolSocketFactory to provide optional host verification.
 * 
 * @author Andrew Cohen
 */
public class ClientSSLProtocolSocketFactory implements SecureProtocolSocketFactory {
	
	// Path of keystore and truststore 
	private static final String DEFAULT_KEYSTORE_PATH = "/var/oculan/tomcat6/";
	private static final String KEYSTORE_FILE = "dcTrack-keystore.jks";
	private static final String TRUSTSTORE_FILE = "dcTrack-truststore.jks";
	private static final String KEYSTORE_PASSWORD = "raritan";
	private static final String TRUSTSTORE_PASSWORD = "raritan";
	
	// Classpath relative location of default keystore and truststore files.
	// If the default keystore path does not contain the keystore files,
	// they will be copied from the default keystore files in this location.
	private static final String TEMPLATE_KEYSTORE_PATH = "keystores/";
	
    /** Log object for this class. */
    private static final Log log = LogFactory.getLog(ClientSSLProtocolSocketFactory.class);

    /** The path to the keystore files */
    private static String keyStorePath = null;
    
    private String keystorePassword = null;
    private String truststorePassword = null;
    private SSLContext sslcontext = null;
    
    /** Host name verify flag. */
    private boolean verifyHostname = true;

    static {
    	keyStorePath = System.getProperty("dcTrack.keyStorePath");
    	if (keyStorePath == null) {
    		keyStorePath = DEFAULT_KEYSTORE_PATH;
    	}
    	
    	initializeKeyStore(KEYSTORE_FILE, KEYSTORE_PASSWORD);
    	initializeKeyStore(TRUSTSTORE_FILE, TRUSTSTORE_PASSWORD);
    }
    
    ClientSSLProtocolSocketFactory(final String keystorePassword, final String truststorePassword)
    {
        super();
        this.keystorePassword = keystorePassword;
        this.truststorePassword = truststorePassword;
    }
    
    
    /**
     * Set the host name verification flag.
     *
     * @param verifyHostname  The host name verification flag. If set to 
     * <code>true</code> the SSL sessions server host name will be compared
     * to the host name returned in the server certificates "Common Name" 
     * field of the "SubjectDN" entry.  If these names do not match a
     * Exception is thrown to indicate this.  Enabling host name verification 
     * will help to prevent from man-in-the-middle attacks.  If set to 
     * <code>false</code> host name verification is turned off.
     */
    public void setHostnameVerification(boolean verifyHostname) {
        this.verifyHostname = verifyHostname;
    }

    /**
     * Gets the status of the host name verification flag.
     *
     * @return  Host name verification flag.  Either <code>true</code> if host
     * name verification is turned on, or <code>false</code> if host name
     * verification is turned off.
     */
    public boolean getHostnameVerification() {
        return verifyHostname;
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a 
     * controller thread is executed. The controller thread attempts to create a new socket 
     * within the given limit of time. If socket constructor does not return until the 
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param clientHost the local host name/IP to bind the socket to
     * @param clientPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    public Socket createSocket(
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        final HttpConnectionParams params
    ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSSLContext().getSocketFactory();
        SSLSocket sslSocket = null;
        if (timeout == 0) {
        	sslSocket = (SSLSocket)socketfactory.createSocket(host, port, localAddress, localPort);
        	verifyHostname(sslSocket);
            return sslSocket;
        } else {
            Socket socket = socketfactory.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            verifyHostname(sslSocket);
            return socket;
        }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress clientHost,
        int clientPort)
        throws IOException, UnknownHostException {
    	SSLSocket sslSocket = (SSLSocket)getSSLContext().getSocketFactory().createSocket(
            host,
            port,
            clientHost,
            clientPort
        );
    	verifyHostname(sslSocket);
    	return sslSocket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException
    {
    	SSLSocket sslSocket = (SSLSocket)getSSLContext().getSocketFactory().createSocket(
            host,
            port
        );
    	verifyHostname(sslSocket);
        return sslSocket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose)
        throws IOException, UnknownHostException
    {
    	SSLSocket sslSocket = (SSLSocket)getSSLContext().getSocketFactory().createSocket(
            socket,
            host,
            port,
            autoClose
        );
    	verifyHostname(sslSocket);
        return sslSocket;
    }
    
    //
    // Private methods
    //
    
    /**
     * Describe <code>verifyHostname</code> method here.
     *
     * @param socket a <code>SSLSocket</code> value
     * @exception SSLPeerUnverifiedException  If there are problems obtaining
     * the server certificates from the SSL session, or the server host name 
     * does not match with the "Common Name" in the server certificates 
     * SubjectDN.
     * @exception UnknownHostException  If we are not able to resolve
     * the SSL sessions returned server host name. 
     */
    private void verifyHostname(SSLSocket socket) 
        throws SSLPeerUnverifiedException, UnknownHostException {
        if (! verifyHostname) 
            return;

        SSLSession session = socket.getSession();
        String hostname = session.getPeerHost();
        try {
            InetAddress.getByName(hostname);
        } catch (UnknownHostException uhe) {
            throw new UnknownHostException("Could not resolve SSL sessions "
                                           + "server hostname: " + hostname);
        }
        
        javax.security.cert.X509Certificate[] certs = session.getPeerCertificateChain();
        if (certs == null || certs.length == 0) 
            throw new SSLPeerUnverifiedException("No server certificates found!");
        
        //get the servers DN in its string representation
        String dn = certs[0].getSubjectDN().getName();

        //might be useful to print out all certificates we receive from the
        //server, in case one has to debug a problem with the installed certs.
        if (log.isDebugEnabled()) {
            log.debug("Server certificate chain:");
            for (int i = 0; i < certs.length; i++) {
                log.debug("X509Certificate[" + i + "]=" + certs[i]);
            }
        }
        //get the common name from the first cert
        String cn = getCN(dn);
        if (hostname.equalsIgnoreCase(cn)) {
            if (log.isDebugEnabled()) {
                log.debug("Target hostname valid: " + cn);
            }
        } else {
            throw new SSLPeerUnverifiedException(
                "HTTPS hostname invalid: expected '" + hostname + "', received '" + cn + "'");
        }
    }


    private static void initializeKeyStore(final String file, final String password) {
    	 log.debug("Initializing key store");
         InputStream is = null;
         FileOutputStream fos = null;
         char[] pwd = password != null ? password.toCharArray(): null;
        	 
         try {
        	KeyStore keystore = KeyStore.getInstance("jks");
         	File keyStoreFile = new File(keyStorePath + file);
         	if (!keyStoreFile.exists()) {
         		// Copy the template keystore
         		is = Thread.currentThread().getContextClassLoader().getResourceAsStream( TEMPLATE_KEYSTORE_PATH + file );
         		keystore.load(is, pwd);
         		fos = new FileOutputStream( keyStoreFile );
         		keystore.store(fos, pwd);
         	}
         }
         catch (Throwable t) {
         	log.error("Error intializing keystore " + file, t);
         }
         finally {
         	if (fos != null) {
         		try {
					fos.close();
				}
         		catch (IOException e) {
					log.error("", e);
				}
         	}
         }
    }
    
    private static KeyStore loadKeyStore(final String file, final String password) 
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        log.debug("Loading key store");
        KeyStore keystore  = KeyStore.getInstance("jks");
        InputStream is = null;
        FileOutputStream fos = null;
        
        try {
        	File keyStoreFile = new File(keyStorePath + file);
    		is = new FileInputStream( keyStoreFile );
    		keystore.load(is, password != null ? password.toCharArray(): null);
        }
        catch (Throwable t) {
        	log.error("Error loading keystore " + file, t);
        }
        finally {
        	if (is != null) is.close();
        	if (fos != null) fos.close();
        }
        return keystore;
    }
    
    private static KeyManager[] createKeyManagers(final KeyStore keystore, final String password)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException 
    {
        if (keystore == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        log.debug("Initializing key manager");
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keystore, password != null ? password.toCharArray(): null);
        return kmfactory.getKeyManagers(); 
    }

    private static TrustManager[] createTrustManagers(final KeyStore keystore)
        throws KeyStoreException, NoSuchAlgorithmException
    { 
        if (keystore == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        log.debug("Initializing trust manager");
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(keystore);
        TrustManager[] trustmanagers = tmfactory.getTrustManagers();
        for (int i = 0; i < trustmanagers.length; i++) {
            if (trustmanagers[i] instanceof X509TrustManager) {
                trustmanagers[i] = new ClientSSLX509TrustManager(
                    (X509TrustManager)trustmanagers[i]); 
            }
        }
        return trustmanagers; 
    }

    private SSLContext createSSLContext() {
        try {
            KeyManager[] keymanagers = null;
            TrustManager[] trustmanagers = null;
            KeyStore keystore = loadKeyStore(KEYSTORE_FILE, this.keystorePassword);
            if (log.isDebugEnabled()) {
                Enumeration<?> aliases = keystore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String)aliases.nextElement();                        
                    Certificate[] certs = keystore.getCertificateChain(alias);
                    if (certs != null) {
                        log.debug("Certificate chain '" + alias + "':");
                        for (int c = 0; c < certs.length; c++) {
                            if (certs[c] instanceof X509Certificate) {
                                X509Certificate cert = (X509Certificate)certs[c];
                                log.debug(" Certificate " + (c + 1) + ":");
                                log.debug("  Subject DN: " + cert.getSubjectDN());
                                log.debug("  Signature Algorithm: " + cert.getSigAlgName());
                                log.debug("  Valid from: " + cert.getNotBefore() );
                                log.debug("  Valid until: " + cert.getNotAfter());
                                log.debug("  Issuer: " + cert.getIssuerDN());
                            }
                        }
                    }
                }
            }
            
            keymanagers = createKeyManagers(keystore, this.keystorePassword);
            keystore = loadKeyStore(TRUSTSTORE_FILE, this.truststorePassword);
            
            if (log.isDebugEnabled()) {
                Enumeration<?> aliases = keystore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String)aliases.nextElement();
                    log.debug("Trusted certificate '" + alias + "':");
                    Certificate trustedcert = keystore.getCertificate(alias);
                    if (trustedcert != null && trustedcert instanceof X509Certificate) {
                        X509Certificate cert = (X509Certificate)trustedcert;
                        log.debug("  Subject DN: " + cert.getSubjectDN());
                        log.debug("  Signature Algorithm: " + cert.getSigAlgName());
                        log.debug("  Valid from: " + cert.getNotBefore() );
                        log.debug("  Valid until: " + cert.getNotAfter());
                        log.debug("  Issuer: " + cert.getIssuerDN());
                    }
                }
            }
            
            trustmanagers = createTrustManagers(keystore);
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(keymanagers, trustmanagers, null);
            return sslcontext;
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new ClientSSLInitializationError("Unsupported algorithm exception: " + e.getMessage());
        } catch (KeyStoreException e) {
            log.error(e.getMessage(), e);
            throw new ClientSSLInitializationError("Keystore exception: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            throw new ClientSSLInitializationError("Key management exception: " + e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ClientSSLInitializationError("I/O error reading keystore/truststore file: " + e.getMessage());
        }
    }

    private SSLContext getSSLContext() {
        if (this.sslcontext == null) {
            this.sslcontext = createSSLContext();
        }
        return this.sslcontext;
    }

    /**
     * Parses a X.500 distinguished name for the value of the 
     * "Common Name" field.
     * This is done a bit sloppy right now and should probably be done a bit
     * more according to <code>RFC 2253</code>.
     *
     * @param dn  a X.500 distinguished name.
     * @return the value of the "Common Name" field.
     */
    private String getCN(String dn) {
        int i = 0;
        i = dn.indexOf("CN=");
        if (i == -1) {
            return null;
        }
        //get the remaining DN without CN=
        dn = dn.substring(i + 3);  
        // System.out.println("dn=" + dn);
        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) {
            if (dncs[i] == ','  && i > 0 && dncs[i - 1] != '\\') {
                break;
            }
        }
        return dn.substring(0, i);
    }
    
    public boolean equals(Object obj) {
        if ((obj != null) && obj.getClass().equals(ClientSSLProtocolSocketFactory.class)) {
            return ((ClientSSLProtocolSocketFactory) obj).getHostnameVerification() 
                == this.verifyHostname;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return ClientSSLProtocolSocketFactory.class.hashCode();
    }
}
