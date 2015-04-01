package com.raritan.tdz.sslclient.home;

/**
 *
 */
public class ClientSSLInitializationError extends Error {

	private static final long serialVersionUID = 1L;

	/**
     * Creates a new AuthSSLInitializationError.
     */
    public ClientSSLInitializationError() {
        super();
    }

    /**
     * Creates a new AuthSSLInitializationError with the specified message.
     *
     * @param message error message
     */
    public ClientSSLInitializationError(String message) {
        super(message);
    }
}
