/**
 * 
 */
package com.raritan.tdz.piq.jobs.listener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQItem;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.piq.home.PIQSyncRackClient;

/**
 * @author prasanna
 *
 */
public class PIQBulkSyncRPDUStepListener extends PIQBulkSyncStepListener{
	
	// Logger for all PIQ sync operations
	private Logger log = Logger.getLogger("PIQSyncLogger");
	
	PIQSyncPDUClient piqSyncRPDUClient = null;
	private ItemHome itemHome; 
	
	private PIQBulkSyncJobListener piqSyncJobListener;
	
	StepExecution stepExecution;
	
	PIQBulkSyncRPDUStepListener(SessionFactory sessionFactory, String queryString, String piqHost,
			PIQSyncPDUClient piqSyncRPDUClient, ItemHome itemHome){
		super(sessionFactory,queryString,piqHost);
		this.piqSyncRPDUClient = piqSyncRPDUClient;
		this.itemHome = itemHome;
	}
	
	public void setPiqSyncJobListener(PIQBulkSyncJobListener piqSyncJobListener) {
		this.piqSyncJobListener = piqSyncJobListener;
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
				piqSyncJobListener.setCategoryTotalCount("rpdu", piqItems.size());
				try {
					piqSyncRPDUClient.areRPDUsInSync(piqItems);
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
		
		//First let us get all the items based on the queryString (item with classLookup.lkpValueCode=2100)
		Query query = session.createQuery(queryString);
		query.setParameter("piqHost", piqHost);
		List<Item> items = query.list();
		
		//Now create a hashmap of item ids to the PIQItems
		HashMap<Long,PIQItem> piqItemsHash = new HashMap<Long,PIQItem>();
		for (Item item:items){
			piqItemsHash.put(item.getItemId(), new PIQItem(item,null,null));
		}
		
		StringBuffer queryStr = new StringBuffer(); 
				
//		queryString.append("select ta.ipaddress as ipAddress, i.item_id as item_id");
//		queryString.append(" from dct_items i inner join dct_ports_data pd on i.item_id = pd.item_id "); 
//		queryString.append(" inner join tblipteaming t on t.portid = pd.port_data_id "); 
//		queryString.append(" inner join tblipaddresses ta on ta.id = t.ipaddressid "); 
//		queryString.append(" inner join dct_lks_data lks on i.class_lks_id = lks.lks_id "); 
//		queryString.append(" where lks.lkp_value_code in (2100) ");
//		queryString.append(" and t.id = (select min(t.id) from tblipteaming t where t.portid = pd.port_data_id) ");
//		queryString.append(" and pd.port_data_id = (select min(pd.port_data_id) from dct_ports_data pd where pd.item_id = i.item_id) ");
//		queryString.append(" order by  i.item_id ");
		
		queryStr.append("select ta.ipaddress as ipAddress, i.item_id as item_id ");
		queryStr.append("from dct_items i inner join dct_ports_data pd on i.item_id = pd.item_id  ");
		queryStr.append("inner join tblipteaming t on t.portid = pd.port_data_id  ");
		queryStr.append("inner join tblipaddresses ta on ta.id = t.ipaddressid ");
		queryStr.append("inner join dct_lks_data lks on i.class_lks_id = lks.lks_id ");
		queryStr.append(" where lks.lkp_value_code in (2100) ");
		queryStr.append("and t.id = (select min(t.id) from tblipteaming t where t.portid = pd.port_data_id) ");
		queryStr.append("and pd.port_data_id = (select min(pd.port_data_id) from dct_ports_data pd inner join tblipteaming t on t.portid = pd.port_data_id where pd.item_id = i.item_id) ");
		queryStr.append("order by  i.item_id  ");
		
		// Get the ipAddress and the itemId of the RackPDU 
		SQLQuery sqlQuery = session.createSQLQuery(queryStr.toString())
							.addScalar("ipAddress")
							.addScalar("item_id");
		
		// For each RackPDU with IP address, update the Item in the above map 
		for (Object obj: sqlQuery.list()){
			Object[] o = (Object[])obj;
			Long itemId = ((BigInteger)o[1]).longValue();
			String ipAddress = (String)o[0];
			
			// Get piqItem from the HashMap above and set the IpAddress
			PIQItem piqItem = piqItemsHash.get(itemId);
			if (piqItem != null){
				piqItem.setIpAddress(ipAddress);
			}else {
				log.debug("No Item for: " + itemId);
			}
		}
			
		List<PIQItem> piqItems = new ArrayList<PIQItem>(piqItemsHash.values());
		
		return piqItems;
	}
}
