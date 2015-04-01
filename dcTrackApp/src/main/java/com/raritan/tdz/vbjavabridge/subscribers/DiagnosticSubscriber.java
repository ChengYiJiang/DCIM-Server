package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.diagnostics.DiagnosticsHome;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

public class DiagnosticSubscriber extends LNSubscriberBase {

	private DiagnosticsHome diagnosticsHome = null;
	private Logger log = Logger.getLogger(this.getClass());
	private String tableName="\"dct_ports_power\"";
	private List<String> actions = null;
	
	public DiagnosticSubscriber(SessionFactory sessionFactory, LNHome lnHome, DiagnosticsHome diagnosticsHome, List<String> actions) {
		super(sessionFactory, lnHome);
		
		this.diagnosticsHome = diagnosticsHome;
		this.actions = actions;
		
	}

	@Override
	public void subscribe() {
		
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			
			subscribe(session, SystemLookup.VBJavaBridgeOperations.INSERT);
		}
		
	}

	private void subscribe(Session session, long operation) {
		
		if (null == actions) return;
		
		LksData lksData = SystemLookup.getLksData(session, operation);
		
		for (String action: actions) {
			lnHome.subscribe(lksData, tableName, action, this);
			
		}
	}

	private void processEvent(LNEvent event) {
		try {
			diagnosticsHome.processLNEvent(event);
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
