/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import org.hibernate.Session;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;


/**
 * @author prasanna
 * This interface represents the individual Item Subscribers which will be called by ItemsEventSubscriber
 */
public interface DataPortSubscriber {
	void handleInsertEvent(Session session, Item item, DataPort dPort) throws RemoteDataAccessException;
	void handleUpdateEvent(Session session, Item item, DataPort dPort)  throws RemoteDataAccessException;
	void handleDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException;
}
