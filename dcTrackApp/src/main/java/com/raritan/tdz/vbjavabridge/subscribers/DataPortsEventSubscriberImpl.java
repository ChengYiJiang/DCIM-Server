package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.home.ItemDeleteHelper;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
public class DataPortsEventSubscriberImpl extends LNSubscriberBase implements DataPortsEventSubscriber {

	LinkedHashMap<String, DataPortSubscriber> dPortSubscribers;
	
	@Autowired
	protected DataPortDAO dataPortDAO;
	
	@Autowired
	protected ItemDeleteHelper itemDeleteHelper;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public DataPortsEventSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome, LinkedHashMap<String, DataPortSubscriber> subscribers) {
		super(sessionFactory, lnHome);
		dPortSubscribers = subscribers;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, DataPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, DataPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, DataPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			session.close();
		}
	}

	/**
	 * @param session
	 * @param event
	 * @throws HibernateException
	 * Get the Subscriber given the event
	 */
	private  DataPortSubscriber getSubscriber(Session session, LNEvent event)
			throws HibernateException {
		
		DataPortSubscriber subscriber = null;
		
		if (session != null && event != null) {
			
			if (event.getOperationLks().getLkpValueCode() != SystemLookup.VBJavaBridgeOperations.DELETE) {
				DataPort dPort = getDataPort(session,event);
				if (null == dPort) return null;
				
				Item item = dPort.getItem();
				
				if (item != null) {
					//Then get the classLks out of it
					LksData classLks = item.getClassLookup();
					
					if (classLks != null){
						//Get the itemSubscriber using classLks and call the individual itemSubscriber's handleEvent
						log.debug("classLks " + classLks);
						subscriber = dPortSubscribers.get(classLks.getLkpValueCode().toString());
					}
				}
			}
			else {
				// Handle Deleted items - the items trigger has stored the item class in the first custom field
				
				try {
					Long itemId = Long.parseLong(event.getCustomField1());
					Item item = (Item) session.load(Item.class, itemId);
					LksData lksData =  item.getClassLookup();
					subscriber = dPortSubscribers.get( lksData.getLkpValueCode().toString() );
				}
				catch (NumberFormatException e) {
					log.warn("getSubscriber: Invalid item class LKS : " + event.getCustomField1());
				}
				catch (org.hibernate.ObjectNotFoundException e){
					log.warn("getSubscriber: Item with itemId " + event.getCustomField1() + "no longer exist");
				}
			}
		}
		return subscriber;
	}

	/**
	 * @param session
	 * @param event
	 * @return
	 * @throws HibernateException
	 */
	private DataPort getDataPort(Session session, LNEvent event)
			throws HibernateException {
		
		// check if the object exist. A load cannot be used to check because it either returns
		// persistent object or proxy and not null. To find if the object exist, get shall be used
		if (null == session.get(DataPort.class, event.getTableRowId())) return null;
		
		//There is a big assumption here. The table row id provided in the event is of an data port :-)
		//Since we subscribe for all item events here, we should be okay.
		DataPort dPort = (DataPort) session.load(DataPort.class, event.getTableRowId());
		return dPort;
	}
	

	@Override
	public void addItemSubscriber(LksData classLks, DataPortSubscriber subscriber) {
		dPortSubscribers.put(classLks.getLkpValueCode().toString(), subscriber);
	}

	@Override
	protected void processInsertEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			DataPortSubscriber subscriber = getSubscriber(session, event);
			DataPort dPort = getDataPort(session,event);
			if (null == dPort) return;
			
			Item item = dPort.getItem();
			if (subscriber != null)
				subscriber.handleInsertEvent(session, item, dPort);
		}catch (HibernateException e){
			e.printStackTrace();
		}
		
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			DataPortSubscriber subscriber = getSubscriber(session, event);
			if (subscriber != null)
				subscriber.handleDeleteEvent(session, event);
		}catch (HibernateException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event) throws Throwable {
		try {			
			DataPortSubscriber subscriber = getSubscriber(session, event);
			DataPort dPort = getDataPort(session,event);
			if (null == dPort) return;
			
			Item item = dPort.getItem();
			if (subscriber != null)
				subscriber.handleUpdateEvent(session, item, dPort);
																				 			
		}catch (HibernateException e){
			e.printStackTrace();
		}
		
	}
}
