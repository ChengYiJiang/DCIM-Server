package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.PowerChainHome;
import com.raritan.tdz.powerchain.home.PowerChainLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

public class PowerChainSubscriber  extends LNSubscriberBase   {

	private PowerChainHome powerChainHome = null;
	private Logger log = Logger.getLogger(this.getClass());
	private String tableName="\"dct_items\"";
	
	/*@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;*/
	
	public PowerChainSubscriber(SessionFactory sessionFactory, LNHome lnHome, PowerChainHome powerChainHome) {
		super(sessionFactory, lnHome);
		
		this.powerChainHome = powerChainHome;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			
			// Subscribe for Insert events
			List<String> insertActions = new ArrayList<String>();
			insertActions.add(PowerChainLookup.Action.POWER_CHAIN_UPDATE_ALL_PORTS_AND_CONN);
			insertActions.add(PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
			insertActions.add(PowerChainLookup.Action.FLOOR_PDU_CREATE_BREAKER_PORT);
			insertActions.add(PowerChainLookup.Action.FLOOR_PDU_BREAKER_PORT_CONNECTION);
			insertActions.add(PowerChainLookup.Action.UPS_BANK_CREATE_BREAKER_PORT);

			subscribe(session, SystemLookup.VBJavaBridgeOperations.INSERT, insertActions);
			
			// Subscribe for Update  events
			List<String> updateActions = new ArrayList<String>();
			updateActions.add(PowerChainLookup.Action.POWER_PANEL_UPDATE_BREAKER_VALUES);
			updateActions.add(PowerChainLookup.Action.POWER_PANEL_ADD_BRANCH_CIRCUIT_BREAKER);
			updateActions.add(PowerChainLookup.Action.FLOOR_PDU_UPDATE_BREAKER_VALUES);
			updateActions.add(PowerChainLookup.Action.FLOOR_PDU_BREAKER_PORT_CONNECTION);
			updateActions.add(PowerChainLookup.Action.UPS_BANK_UPDATE_BREAKER_PORT_VALUES);

			subscribe(session, SystemLookup.VBJavaBridgeOperations.UPDATE, updateActions);
			
			// Subscribe for Delete events
			List<String> deleteActions = new ArrayList<String>();
			deleteActions.add(PowerChainLookup.Action.FLOOR_PDU_DELETE_BREAKER_PORT);
			deleteActions.add(PowerChainLookup.Action.UPS_BANK_DELETE_BREAKER_PORT);

			subscribe(session, SystemLookup.VBJavaBridgeOperations.DELETE, deleteActions);
			
			session.close();
		}
	}
	
	private void subscribe(Session session, long operation, List<String> actions) {
		LksData lksData = SystemLookup.getLksData(session, operation);
		
		for (String action: actions) {
			lnHome.subscribe(lksData, tableName, action, this);
		}
	}

	private void processEvent(LNEvent event) {
		try {
			powerChainHome.processLNEvent(event);
		}
		catch (BusinessValidationException ex) {
			log.error("Error processing event " + event.toString(), ex);
		}
	}
	
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		processEvent(event);
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		processEvent(event);
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		processEvent(event);
	}
	
	
}
