package com.raritan.tdz.vbjavabridge.subscribers;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

public class ModelSubscriber extends LNSubscriberBase  {
	private String MODEL_TABLE = "dct_models";
	private ChassisHome chassisHome = null;
	private Logger log = Logger.getLogger(this.getClass());
	private String tableName="\"" + MODEL_TABLE + "\"";
	
	
	public ModelSubscriber(SessionFactory sessionFactory, LNHome lnHome, ChassisHome chassisHome) {
		super(sessionFactory, lnHome);
		this.chassisHome = chassisHome;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),tableName,this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),tableName,this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),tableName,this);
			session.close();
		}
	}

	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		if(event.getTableName().equals(MODEL_TABLE)) {
			Long modelId = event.getTableRowId();
			
			try{				
				chassisHome.updateModelDefinition(modelId.longValue());
			}
			catch(DataAccessException ex){
				log.error("Error Updating Model Definition for model Id " + modelId, ex);
			}
		}
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		
		if(event.getTableName().equals(MODEL_TABLE)) {
			Long modelId = event.getTableRowId();
			
			try{
				chassisHome.updateModelDefinition(modelId.longValue());
			}
			catch(DataAccessException ex){
				log.error("Error Deleting Model Definition for model Id " + modelId, ex);
			}
		}
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {

		if(event.getTableName().equals(MODEL_TABLE)) {
		 
			Long modelId = event.getTableRowId();
			try{
				chassisHome.updateModelDefinition(modelId.longValue());
			}
			catch(DataAccessException ex){
				log.error("Error Updating Model Definition for model Id " + modelId, ex);
			}
		}
	}
}
