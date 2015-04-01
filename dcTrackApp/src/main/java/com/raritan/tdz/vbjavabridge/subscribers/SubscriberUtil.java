package com.raritan.tdz.vbjavabridge.subscribers;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;

/**
 * Internal Utility class for Subscribers for loading detached entities in the Listen/Notify framework.
 * Loading detached entities is important to prevent potential DB deadlock issues in the Listen/Notify thread
 * for long running transactions. 
 * @author Andrew Cohen
 */
class SubscriberUtil {

	private static final Logger log = Logger.getLogger("SubscriberUtil");
	
	/**
	 * Loads a detached item.
	 * @param sessionFactory
	 * @param itemId
	 * @return
	 */
	public static Item loadDetachedItem(SessionFactory sessionFactory, long itemId) {
		Session session = null;
		Item item = null;
		
		try {
			session = sessionFactory.openSession();
			item = (Item)session.load(Item.class, itemId);
			if (item != null) {
				item = (Item)item.clone(); // Work with detached instance
			}
		}
		catch (HibernateException ex) {
			throw ex;
		}
		catch (Throwable t) {
			log.error("Error loading detached item with id = " + itemId, t);
		}
		finally {
			if (session != null) {
				session.close();
			}
		}
		
		return item;
	}
	
	/**
	 * Loads a detached data port.
	 * @param sessionFactory
	 * @param dataPortId
	 * @return
	 */
	public static DataPort loadDetachedDataPort(SessionFactory sessionFactory, long dataPortId) {
		Session session = null;
		DataPort dataPort = null;
		
		try {
			session = sessionFactory.openSession();
			dataPort = (DataPort)session.load(DataPort.class, dataPortId);
			if (dataPort != null) {
				dataPort = (DataPort)dataPort.clone(); // Work with detached instance
			}
		}
		catch (HibernateException ex) {
			throw ex;
		}
		catch (Throwable t) {
			log.error("Error loading detached data port with id=" + dataPortId, t);
		}
		finally {
			if (session != null) {
				session.close();
			}
		}
		
		return dataPort;
	}
}
