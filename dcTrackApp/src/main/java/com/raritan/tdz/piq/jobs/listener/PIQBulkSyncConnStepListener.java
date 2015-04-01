/**
 * 
 */
package com.raritan.tdz.piq.jobs.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQSyncOutletClient;

/**
 * @author prasanna
 *
 */
public class PIQBulkSyncConnStepListener extends PIQBulkSyncStepListener{
	
	// Logger for all PIQ sync operations
	private Logger log = Logger.getLogger("PIQSyncLogger");
	
	PIQSyncOutletClient piqSyncOutletClient = null;
	private ItemHome itemHome;
	private PIQBulkSyncJobListener piqSyncJobListener;
	
	StepExecution stepExecution;
	
	PIQBulkSyncConnStepListener(SessionFactory sessionFactory, String queryString, String piqHost,
			PIQSyncOutletClient piqSyncOutletClient, ItemHome itemHome){
		super(sessionFactory,queryString,piqHost);
		this.piqSyncOutletClient = piqSyncOutletClient;
		this.itemHome = itemHome;
	}
	
	public void setPiqSyncJobListener(PIQBulkSyncJobListener piqSyncJobListener) {
		this.piqSyncJobListener = piqSyncJobListener;
	}

	@Override
	public void beforeStep(StepExecution stepExecution){
		
		super.beforeStep(stepExecution);
		
		List<PowerConnection> powerConns = null;
		this.stepExecution = stepExecution;
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			powerConns = getPowerConnections(session);
			if (powerConns.size() > 0){
				
				// CR #42020 - We need to set the total count for power connections just before we start this job step.
				// Reason is because there may be more "eligible" power connections after we completed the Rack PDU synchronization step.
				piqSyncJobListener.setCategoryTotalCount("connection", powerConns.size());
				
				try {
					piqSyncOutletClient.areAssociationsInSync(powerConns, getPiqPduIds(session));
				} catch (RemoteDataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			session.close();
		}
	}
	
	List<Integer> getPiqPduIds(Session session) {
		String qstr = "select pdu.piqId from Item pdu where pdu.classLookup.lkpValueCode = :lkpValueCode order by pdu.piqId";
		Query query = session.createQuery(qstr);
		query.setParameter("lkpValueCode", SystemLookup.Class.RACK_PDU);
		@SuppressWarnings("unchecked")
		List<Integer>  piqPduIds = query.list();
		return piqPduIds;
	}
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution){
		return super.afterStep(stepExecution);
	}
	
	private List<PowerConnection> getPowerConnections(Session session){
		List<PowerConnection> powerConnections = null;
		
		Query query = session.createQuery(queryString);
		query.setParameter("piqHost", piqHost);
		powerConnections = query.list();
				
		return powerConnections;
	}
}
