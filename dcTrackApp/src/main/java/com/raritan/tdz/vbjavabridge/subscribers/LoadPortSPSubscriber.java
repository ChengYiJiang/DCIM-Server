package com.raritan.tdz.vbjavabridge.subscribers;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

public class LoadPortSPSubscriber extends LNSubscriberBase  {

	private Logger log = Logger.getLogger(this.getClass());
	private String tableName="\"dc_loadports\"";
	
	public LoadPortSPSubscriber(SessionFactory sessionFactory, LNHome lnHome) {
		super(sessionFactory, lnHome);
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),tableName,this);
			session.close();
		}
	}

	@Transactional
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		
		if(event.getCustomField1() != null){
			Query query = session.getNamedQuery("dcLoadPortsSP");
			query.setParameter("cabinetId", event.getTableRowId());
			query.setParameter("userName", event.getCustomField1());
			query.setParameter("portType", event.getCustomField2());		
			query.list();
		}
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		 //processInsertEvent(session, event);
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {

		//processInsertEvent(session, event);
	}
}
