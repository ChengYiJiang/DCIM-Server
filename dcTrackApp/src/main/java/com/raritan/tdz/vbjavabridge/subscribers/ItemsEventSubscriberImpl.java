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
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
public class ItemsEventSubscriberImpl extends LNSubscriberBase implements ItemsEventSubscriber {

	LinkedHashMap<String, ItemSubscriber> itemSubscribers;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public ItemsEventSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome, LinkedHashMap<String, ItemSubscriber> subscribers) {
		super(sessionFactory, lnHome);
		itemSubscribers = subscribers;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, Item.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, Item.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, Item.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			session.close();
		}
	}

	/**
	 * @param session
	 * @param event
	 * @throws HibernateException
	 * Get the Subscriber given the event
	 */
	private  ItemSubscriber getSubscriber(Session session, LNEvent event)
			throws HibernateException {
		
		ItemSubscriber subscriber = null;
		
		if (session != null && event != null) {
			
			if (event.getOperationLks().getLkpValueCode() != SystemLookup.VBJavaBridgeOperations.DELETE) {
				Item item = getItem(session, event);
				
				if (item != null) {
					//Then get the classLks out of it
					LksData classLks = item.getClassLookup();
					
					if (classLks != null){
						//Get the itemSubscriber using classLks and call the individual itemSubscriber's handleEvent
						log.debug("classLks " + classLks);
						subscriber = itemSubscribers.get(classLks.getLkpValueCode().toString());
					}
				}
			}
			else {
				// Handle Deleted items - the items trigger has stored the item class in the first custom field
				Long itemClassLksId;
				try {
					itemClassLksId = Long.parseLong( event.getCustomField1() );
					LksData lksData = (LksData)session.get(LksData.class, itemClassLksId );
					subscriber = itemSubscribers.get( lksData.getLkpValueCode().toString() );
				}
				catch (NumberFormatException e) {
					log.warn("getSubscriber: Invalid item class LKS : " + event.getCustomField1());
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
	private Item getItem(Session session, LNEvent event)
			throws HibernateException {
		//There is a big assumption here. The table row id provided in the event is of an item :-)
		//Since we subscribe for all item events here, we should be okay.
		Item item = (Item) session.load(Item.class, event.getTableRowId());
		return item;
	}

	@Override
	public void addItemSubscriber(LksData classLks, ItemSubscriber subscriber) {
		itemSubscribers.put(classLks.getLkpValueCode().toString(), subscriber);
	}

	@Override
	protected void processInsertEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			ItemSubscriber subscriber = getSubscriber(session, event);
			Item item = getItem(session, event);
			if (subscriber != null)
				subscriber.handleInsertEvent(session, item);
		}catch (HibernateException e){
			log.error(e.getMessage());
		}
		
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			ItemSubscriber subscriber = getSubscriber(session, event);
			if (subscriber != null)
				subscriber.handleDeleteEvent(session, event);
		}catch (HibernateException e){
			log.error(e.getMessage());
		}
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			ItemSubscriber subscriber = getSubscriber(session, event);
			Item item = getItem(session, event);
			if (subscriber != null)
				subscriber.handleUpdateEvent(session, item, event);
		}catch (HibernateException e){
			log.error(e.getMessage());
		}
		
	}

}
