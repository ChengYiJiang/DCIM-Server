package com.raritan.tdz.sslclient.home;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;


/**
 * Factory for use with Commons HttpClient to accept all certificates.
 * This should never be used in production!
 * 
 * @author Andrew Cohen
 */
class AcceptAllCertsProtocolSocketFactory implements SecureProtocolSocketFactory {

    private SSLContext sslcontext = null;

    AcceptAllCertsProtocolSocketFactory() {
        super();
    }

    private static SSLContext createEasySSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context
                    .init(
                            null,
                            new TrustManager[] { new AcceptAllCertsTrustManager() }, null);
            return context;
        } catch (Exception e) {
            throw new HttpClientError(e.toString());
        }
    }

    private SSLContext getSSLContext() {
        if (this .sslcontext == null) {
            this .sslcontext = createEasySSLContext();
        }
        return this .sslcontext;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(String host, int port,
            InetAddress clientHost, int clientPort) throws IOException,
            UnknownHostException {

        return getSSLContext().getSocketFactory().createSocket(host,
                port, clientHost, clientPort);
    }

    public Socket createSocket(final String host, final int port,
            final InetAddress localAddress, final int localPort,
            final HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException(
                    "Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSSLContext()
                .getSocketFactory();
        if (timeout == 0) {
            return socketfactory.createSocket(host, port, localAddress,
                    localPort);
        } else {
            Socket socket = socketfactory.createSocket();
            SocketAddress localaddr = new InetSocketAddress(
                    localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(host,
                port);
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(socket,
                host, port, autoClose);
    }

    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(
                AcceptAllCertsProtocolSocketFactory.class));
    }

    public int hashCode() {
        return AcceptAllCertsProtocolSocketFactory.class.hashCode();
    }
}