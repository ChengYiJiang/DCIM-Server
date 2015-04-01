/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * This is a PowerIQ proxy subscriber used to direct any PowerIQ lnEvents to be 
 * put to the on going update application context via a lnHome based gateway
 * @author prasanna
 *
 */
public class PowerIQProxySubscriber extends LNSubscriberBase {

	private LNHome lnHomeGateway;
	
	
	//TODO: Once we have the LNEvents populated with the ipAddress, we don't need appSettings
	@Autowired
	private ApplicationSettings appSettings;
	
	public PowerIQProxySubscriber(SessionFactory sessionFactory, LNHome lnHome, LNHome lnHomeGateway) {
		super(sessionFactory, lnHome);
		this.lnHomeGateway = lnHomeGateway;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber#subscribe()
	 */
	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, Item.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, Item.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, Item.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			
			subscribe(sessionFactory, DataPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, DataPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, DataPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			
			subscribe(sessionFactory, PowerPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, PowerPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, PowerPort.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);

			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),"\"tblipaddresses\"",this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),"\"tblipaddresses\"",this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),"\"tblipaddresses\"",this);
			
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),"\"tblipteaming\"",this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),"\"tblipteaming\"",this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),"\"tblipteaming\"",this);
			
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, PowerConnection.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, PowerConnection.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, PowerConnection.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);			
			
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, DataCenterLocationDetails.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, DataCenterLocationDetails.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, DataCenterLocationDetails.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			
			//Subscribe for Insert, update and delete events.
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),"\"piq_update\"",this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),"\"piq_update\"",this);
			
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),"\"" + PowerIQUpdateSubscriber.PIQ_UPDATE_FAKE_TABLE + "\"",this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),"\"" + PowerIQUpdateSubscriber.PIQ_UPDATE_FAKE_TABLE + "\"",this);
			
			session.close();
		}

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processInsertEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException, Throwable {
		if (event.getCustomField3() != null && !event.getCustomField3().isEmpty())
			lnHomeGateway.processEvent(event);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException, Throwable {
		if (event.getCustomField3() != null && !event.getCustomField3().isEmpty())
			lnHomeGateway.processEvent(event);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processUpdateEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException, Throwable {
		if (event.getCustomField3() != null && !event.getCustomField3().isEmpty())
			lnHomeGateway.processEvent(event);
	}

}
