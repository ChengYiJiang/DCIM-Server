/**
 * 
 */
package com.raritan.tdz.vpc.subscribers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;
import com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase;
import com.raritan.tdz.vpc.home.VPCHome;

/**
 * @author prasanna
 *
 */
public class VPCPowerChainCreateSubscriber extends LNSubscriberBase {

	private String tableName="\"dct_locations\"";
	
	@Autowired
	private VPCHome vpcHome;
	
	public VPCPowerChainCreateSubscriber(SessionFactory sessionFactory,
			LNHome lnHome) {
		super(sessionFactory, lnHome);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber#subscribe()
	 */
	@Override
	public void subscribe() {
		
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			
			// Add new VPC items for the given location
			subscribe(session, SystemLookup.VBJavaBridgeOperations.INSERT);
			
			// delete VPC items for the given location, 
			// this may not be required since the delete locations should also delete all VPC items
			subscribe(session, SystemLookup.VBJavaBridgeOperations.DELETE);
			
		}

	}
	
	private void subscribe(Session session, long operation) {
		LksData lksData = SystemLookup.getLksData(session, operation);
		
		lnHome.subscribe(lksData, tableName, null, this);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processInsertEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException, Throwable {
		// Get the locationId out of the LNEvent
		Long locationId = event.getTableRowId();
		
		try {
			//	Call the vpcHome to create the VPC power chain
			vpcHome.create(locationId, null);
		}
		catch (Throwable e) {
			// keep going to the next event in case of error. Do not stop the processing
		}

	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException, Throwable {
		// Get the locationId out of the LNEvent
		Long locationId = event.getTableRowId();
		
		//Call the vpcHome to create the power chain
		vpcHome.delete(locationId, null); 

	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException, Throwable {
		// TODO Auto-generated method stub
		
	}


}
