/**
 * 
 */
package com.raritan.tdz.piq.jobs.listener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.piq.home.PIQItem;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.piq.home.PIQSyncRackClient;

/**
 * @author prasanna
 *
 */
public class PIQBulkSyncRackStepListener extends PIQBulkSyncStepListener{
	
	// Logger for all PIQ sync operations
	private Logger log = Logger.getLogger("PIQSyncLogger");
	
	PIQSyncRackClient piqSyncRackClient = null;
	private ItemHome itemHome; 
	
	StepExecution stepExecution;
	
	PIQBulkSyncRackStepListener(SessionFactory sessionFactory, String queryString, String piqHost,
			PIQSyncRackClient piqSyncRackClient, ItemHome itemHome){
		super(sessionFactory,queryString,piqHost);
		this.piqSyncRackClient = piqSyncRackClient;
		this.itemHome = itemHome;
	}
	


	@Override
	public void beforeStep(StepExecution stepExecution){
		
		super.beforeStep(stepExecution);
		
		List<PIQItem> piqItems = null;
		this.stepExecution = stepExecution;
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			piqItems = getPIQItems(session);
			if (piqItems.size() > 0){
				try {
					piqSyncRackClient.areRacksInSync(piqItems);
				} catch (RemoteDataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			session.close();
		}
	}
	
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution){
		return super.afterStep(stepExecution);
	}
	
	private List<PIQItem> getPIQItems(Session session){
		List<PIQItem> piqItems = new ArrayList<PIQItem>();
		
		Query query = session.createQuery(queryString);
		query.setParameter("piqHost", piqHost);
		List<Item> items = query.list();
		for (Item item:items){
			PIQItem piqItem = new PIQItem(item,null,null);
			piqItems.add(piqItem);
		}
		
		
//		StringBuffer queryString = new StringBuffer(); 
//				
//		queryString.append("select i.item_id as item_id");
//		queryString.append(" from dct_items i inner join dct_ports_data pd on i.item_id = pd.item_id "); 
//		queryString.append(" inner join tblipteaming t on t.portid = pd.port_data_id "); 
//		queryString.append(" inner join tblipaddresses ta on ta.id = t.ipaddressid "); 
//		queryString.append(" inner join dct_lks_data lks on i.class_lks_id = lks.lks_id "); 
//		queryString.append(" where lks.lkp_value_code in (2100) ");
//		queryString.append(" and t.id = (select min(t.id) from tblipteaming t where t.portid = pd.port_data_id) ");
//		queryString.append(" and pd.port_data_id = (select min(pd.port_data_id) from dct_ports_data pd where pd.item_id = i.item_id) ");
//		queryString.append(" order by  i.item_id ");
//		
//		SQLQuery query = session.createSQLQuery(queryString.toString())
//							.addScalar("ipAddress")
//							.addScalar("item_id");
//		
//		for (Object obj: query.list()){
//			Object[] o = (Object[])obj;
//			Item item = (Item) session.load(Item.class, ((BigInteger)o[1]).longValue());
//			PIQItem piqItem = new PIQItem(item,(String)o[0],null);
//			piqItems.add(piqItem);
//		}
				
		return piqItems;
	}
}
