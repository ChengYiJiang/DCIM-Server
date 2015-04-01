package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
public class PowerPortsEventSubscriberImpl extends LNSubscriberBase implements PowerPortsEventSubscriber {

	LinkedHashMap<String, PowerPortSubscriber> pPortSubscribers;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public PowerPortsEventSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome, LinkedHashMap<String, PowerPortSubscriber> subscribers) {
		super(sessionFactory, lnHome);
		pPortSubscribers = subscribers;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, PowerPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, PowerPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, PowerPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			session.close();
		}
	}

	/**
	 * @param session
	 * @param event
	 * @throws HibernateException
	 * Get the Subscriber given the event
	 */
	private  PowerPortSubscriber getSubscriber(Session session, LNEvent event)
			throws HibernateException {
		
		PowerPortSubscriber subscriber = null;
		
		if (session != null && event != null) {
			
			if (event.getOperationLks().getLkpValueCode() != SystemLookup.VBJavaBridgeOperations.DELETE) {
				PowerPort pPort = getPowerPort(session,event);
				
				if (null == pPort) return null;
				
				Item item = pPort.getItem();
				
				if (item != null) {
					//Then get the classLks out of it
					LksData classLks = item.getClassLookup();
					
					if (classLks != null){
						//Get the itemSubscriber using classLks and call the individual itemSubscriber's handleEvent
						log.debug("classLks " + classLks);
						subscriber = pPortSubscribers.get(classLks.getLkpValueCode().toString());
					}
				}
			}
			else {
				// Handle Deleted items - the items trigger has stored the item class in the first custom field
				
				try {
					Long itemId = Long.parseLong(event.getCustomField1());
					Item item = (Item) session.load(Item.class, itemId);
					LksData lksData =  item.getClassLookup();
					subscriber = pPortSubscribers.get( lksData.getLkpValueCode().toString() );
				}
				catch (NumberFormatException e) {
					log.warn("getSubscriber: Invalid item class LKS : " + event.getCustomField1());
				}
				catch (org.hibernate.ObjectNotFoundException e){
						log.warn("getSubscriber: Item with itemId " + event.getCustomField1() + "not found");
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
	private PowerPort getPowerPort(Session session, LNEvent event)
			throws HibernateException {

		// check if the object exist. A load cannot be used to check because it either returns
		// persistent object or proxy and not null. To find if the object exist, get shall be used
		if (null == session.get(PowerPort.class, event.getTableRowId())) return null;

		//There is a big assumption here. The table row id provided in the event is of an data port :-)
		//Since we subscribe for all item events here, we should be okay.
		PowerPort pPort = (PowerPort) session.load(PowerPort.class, event.getTableRowId());
		return pPort;
	}
	

	@Override
	public void addItemSubscriber(LksData classLks, PowerPortSubscriber subscriber) {
		pPortSubscribers.put(classLks.getLkpValueCode().toString(), subscriber);
	}

	@Override
	protected void processInsertEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			PowerPortSubscriber subscriber = getSubscriber(session, event);
			PowerPort pPort = getPowerPort(session,event);
			
			if (null == pPort) return;
			
			Item item = pPort.getItem();
			if (subscriber != null)
				subscriber.handleInsertEvent(session, item, pPort);
		}catch (HibernateException e){
			e.printStackTrace();
		}
		
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			PowerPortSubscriber subscriber = getSubscriber(session, event);
			if (subscriber != null)
				subscriber.handleDeleteEvent(session, event);
		}catch (HibernateException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			PowerPortSubscriber subscriber = getSubscriber(session, event);
			PowerPort pPort = getPowerPort(session,event);
			if (null == pPort) return;
			
			Item item = pPort.getItem();
			if (subscriber != null)
				subscriber.handleUpdateEvent(session, item, pPort);
		}catch (HibernateException e){
			e.printStackTrace();
		}
		
	}

}
