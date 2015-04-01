package com.raritan.tdz.item.home;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.listener.AuditTrailListener;
import com.raritan.tdz.move.dao.PortMoveDAO;

/**
 * Class to separate the logic for deleting an item.
 * @author Santo Rosario
 */
@Transactional
public class ItemDeleteHelper {
	protected static Logger log = Logger.getLogger("ItemHome");
	private Session session;
	private SessionFactory sessionFactory;

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	ItemDeleteHelper(){

	}

	ItemDeleteHelper(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	@Transactional
	public boolean deleteItem(Item item) throws Throwable {
		int deleted;
		long itemId = item.getItemId();
		Integer itemIdInt = (int)itemId;
		session = sessionFactory.getCurrentSession();

		new AuditTrailListener().saveAuditDataForDelete( item, session );

		// deleteItemIPAddresses(itemId);
		deleteItemIPAddressAndTeaming(itemId);
		deleteItemDataStore(itemId);
		deleteItemApplication(itemIdInt);
		deleteItemSnmpData(itemIdInt);
		// deleteItemTicket(itemId);
		deleteItemCustomField(itemId);
		deleteItemSensorPort(itemId);
		deleteItemDataPort(itemId);
		deleteItemPowerConnections(itemId);
		deleteItemPowerPort(itemId);
		deleteItemRequest(itemIdInt);
		deleteMovedItemRequest(itemId);

		//Delete Item Record
		ItemServiceDetails detail = item.getItemServiceDetails();

		Query q = session.createQuery("delete from Item as i where i.itemId = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " item");
		}

		
		if(detail != null){
			Long itemDetailId = detail.getItemServiceDetailId();

			q = session.createSQLQuery("delete from dct_item_details where item_detail_id = :itemDetailId");
			q.setLong("itemDetailId", itemDetailId);
			deleted = q.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug("Item Delete: Deleted " + deleted + " item detail");
			}
		}

		/*item.setPowerPorts(null);
		item.setDataPorts(null);
		item.setCustomFields(null);*/

		session.flush();

		return true;
	}

	protected void deleteItemIPAddressAndTeaming(long itemId) {

		session = sessionFactory.getCurrentSession();

		// get the list of ip addresses to delete. If the ip address is shared by another port of other item, do not delete the ip address
		Query qIPAddressDeleteList = session.createSQLQuery(new StringBuffer()
			.append("select distinct ipaddressid from tblipteaming where portid in (select port_data_id from dct_ports_data where item_id = :itemId) ")
			.append("and ipaddressid not in (select distinct ipaddressid from tblipteaming where portid not in (select port_data_id from dct_ports_data where item_id = :itemId)) ")
			.toString()
		);

		qIPAddressDeleteList.setLong("itemId", itemId);
		@SuppressWarnings("unchecked")
		List<Long> ipAddressDeleteList = (List<Long>) qIPAddressDeleteList.list();

		// delete the ip teaming(s)
		Query qDeleteTeaming = session.createSQLQuery(new StringBuffer()
			.append("delete from tblipteaming where portid in ") 
			.append("(select port_data_id from dct_ports_data where item_id = :itemId) ")
			.toString()
		);

		qDeleteTeaming.setLong("itemId", itemId);
		qDeleteTeaming.executeUpdate();
		
		// delete the ip addresses
		if (ipAddressDeleteList != null && ipAddressDeleteList.size() > 0) {
			Query qDeleteIpAddresses = session.createSQLQuery(new StringBuffer()
				.append("delete from tblipaddresses where id in (:ipAddressList) ") 
				.toString()
			);
		
			qDeleteIpAddresses.setParameterList("ipAddressList", ipAddressDeleteList.toArray());
			qDeleteIpAddresses.executeUpdate();
		}
		
	}
	
	protected void deleteItemIPAddresses(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery(new StringBuffer()
			.append("select team.ipaddressid from dct_items item ")
			.append("INNER join dct_ports_data d ON item.item_id = d.item_id ")
			.append("INNER join tblipteaming team ON d.port_data_id = team.portid ")
			.append("where item.item_id = :itemId")
			.toString()
		);
		q.setLong("itemId", itemId);

		for (Object result : q.list()) {
			Integer ipAddressId = (Integer)result;

			Query deleteTeaming = session.createSQLQuery("delete from tblipteaming where ipaddressid = :ipaddressid");
			deleteTeaming.setInteger("ipaddressid", ipAddressId);
			deleteTeaming.executeUpdate();

			Query deleteIps = session.createSQLQuery("delete from tblipaddresses where id = :id");
			deleteIps.setInteger("id", ipAddressId);
			deleteIps.executeUpdate();
		}
	}

	protected void deleteItemDataPort(Item item) {
		if (null != item && null != item.getDataPorts()) {
			item.getDataPorts().clear();
		}
	}
	
	protected void deleteItemDataPort(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from dct_ports_data where port_data_id in (select link_id from dct_ports_data where link_id is not null and item_id = :itemId)");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " data ports");
		}
		
		q = session.createSQLQuery("delete from dct_ports_data where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " data ports");
		}
	}

	public void deleteItemPowerPort(Item item) {
		if (null != item && null != item.getPowerPorts()) {
			item.getPowerPorts().clear();
		}
	}
	
	public void deleteItemPowerConnections(long itemId) {
		session = sessionFactory.getCurrentSession();

		//Query q = session.createQuery("delete from PowerPort where item.itemId = :itemId");
		Query q = session.createSQLQuery("delete from dct_connections_power  where source_port_id in (select port_power_id from dct_ports_power where item_id = :itemId OR COALESCE(busway_item_id,-1) = :itemId)");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " power ports");
		}
	}
	
	//@Transactional(propagation=Propagation.REQUIRES_NEW) did not work when use from ChangeModelDAOImpl
	public void deleteItemPowerPort(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from dct_ports_power where item_id = :itemId ");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " power ports");
		}
		
		q = session.createSQLQuery("delete from dct_ports_power where COALESCE(busway_item_id,-1) = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " power ports");
		}
		
		if(deleted > 0){
			MeItem item = (MeItem)session.get(MeItem.class, itemId);
			
			if(item.getPduPanelItem() != null){
				updatePolesQty(item.getPduPanelItem().getItemId());
			}
		}
	}

	protected void deleteItemSensorPort(Item item) {
		if (null != item && null != item.getSensorPorts()) {
			item.getSensorPorts().clear();
		}
	}
	
	protected void deleteItemSensorPort(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from dct_ports_sensor where item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " sensor a ports");
		}

	}

	public void deleteItemDataStore(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("update dct_items_it set datastore_id = null where datastore_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Removed " + deleted + " from ticket");
		}

		q = session.createSQLQuery("delete from dct_datastores where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " DataStore record");
		}
	}

	public void deleteItemApplication(Integer itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from tblappusergroups using tblapplications where tblappusergroups.appinstanceid = tblapplications.id and tblapplications.itemid = :itemId");
		q.setInteger("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " Application User Group record");
		}

		q = session.createSQLQuery("delete from tblapplicationsites using tblapplications where tblapplicationsites.appinstanceid = tblapplications.id and tblapplications.itemid = :itemId");
		q.setInteger("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " Application Site record");
		}

		q = session.createSQLQuery("delete from tblapplications where itemid = :itemId");
		q.setInteger("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " Application record");
		}
	}

	public void deleteItemCustomField(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from dct_custom_fields where item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " custom fields");
		}

	}

	protected void deleteItemTicket(long itemId) {
		session = sessionFactory.getCurrentSession();

		Query qStatus = session.createSQLQuery("update dct_tickets set status_lks_id = 1016 from dct_ticket_fields where dct_ticket_fields.ticket_id = dct_tickets.ticket_id and dct_ticket_fields.item_id = :itemId and status_lks_id <> 1026 and status_lks_id <> 1029");
		qStatus.setLong("itemId", itemId);
		int deleted = qStatus.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Removed " + deleted + " from ticket");
		}

		Query qReqId = session.createSQLQuery("update dct_tickets set request_id = null from dct_ticket_fields where dct_ticket_fields.ticket_id = dct_tickets.ticket_id and dct_ticket_fields.item_id = :itemId");
		qReqId.setLong("itemId", itemId);
		deleted = qReqId.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Removed " + deleted + " from ticket");
		}
		
		Query q = session.createSQLQuery("update dct_ticket_fields set item_id = null where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Removed " + deleted + " from ticket");
		}

	}

	protected void deleteItemSnmpData(Integer itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createQuery("delete from ItemSNMP where item.itemId = :itemId");
		q.setInteger("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " SNMP3 item");
		}

		q = session.createSQLQuery("delete from tblAgtSnmpData where itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblSnmpReadEnv where itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblSnmpReadElec where itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblAgtSnmpData_RptItemIds where itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblSNMPDscvryPorts using tblSNMPDscvryItems where tblSNMPDscvryPorts.DscvrdItemID = tblSNMPDscvryItems.Id and tblSNMPDscvryItems.itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblSNMPDscvryPerph using tblSNMPDscvryItems where tblSNMPDscvryPerph.DscvrdItemID = tblSNMPDscvryItems.Id and tblSNMPDscvryItems.itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblSNMPDscvrySWInstalled using tblSNMPDscvryItems where tblSNMPDscvrySWInstalled.DscvrdItemID = tblSNMPDscvryItems.Id and tblSNMPDscvryItems.itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblSNMPDscvryItems where tblSNMPDscvryItems.itemid = :itemId ");
		q.setInteger("itemId", itemId);
		q.executeUpdate();
	}

	protected void deleteItemRequest(Integer itemId) {
		session = sessionFactory.getCurrentSession();

		Query q = session.createSQLQuery("delete from tblRequestPointer using tblRequest where tblRequestPointer.requestid = tblRequest.id and tblRequest.itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		q = session.createSQLQuery("delete from tblRequestHistory using tblRequest where tblRequestHistory.requestid = tblRequest.id and tblRequest.itemid = :itemId");
		q.setInteger("itemId", itemId);
		q.executeUpdate();

		//Get list of work orders link to request
		//field request.workOrderId is map as a long, not int
		// q = session.createQuery("select request.workOrderId from Request request where request.itemId = :itemId and request.workOrderId is not null");
		q = session.createSQLQuery("select workorderid from tblrequest request where itemid = :itemId and workorderid is not null");
		q.setInteger("itemId", itemId);
		List<Integer> recList = q.list();

		q = session.createSQLQuery("delete from tblRequest where itemid = :itemId");
		q.setInteger("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " requests");
		}

		//Delete work orders link to request
		for(Integer wid:recList){
			//Integer wid = (Integer)rec;
			deleteItemWorkOrder(wid.intValue(), itemId);
		}
	}
	
	protected void deleteMovedItemRequest(Long itemId) {
		Long movedItemId = powerPortMoveDAO.getMovingItemId(itemId);
		
		if (null == movedItemId) return;
		
		deleteItemRequest(movedItemId.intValue());
		
	}

	protected void deleteItemWorkOrder(Integer workOrderId, Integer itemId) {
		session = sessionFactory.getCurrentSession();

		//check that this work order is only link to this item before delete
		Query q = session.createSQLQuery("select itemid from tblrequest where itemid != :itemId and workorderid = :workOrderId");
		q.setInteger("itemId", itemId);
		q.setLong("workOrderId", workOrderId);
		List<Integer> recList = q.list();

		if(recList.size() == 0){
			q = session.createSQLQuery("delete from tblworkorder where id  = :workOrderId");
			q.setInteger("workOrderId", workOrderId);
			int deleted = q.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug("Item Delete: Deleted " + deleted + " work orders");
			}
		}
	}
	
	protected void updatePolesQty(long itemId){
		session = sessionFactory.getCurrentSession();
		Query q = session.createSQLQuery("UPDATE dct_items_me SET poles_qty = t.portCount " +
				" FROM (select count(*) as portCount from dct_ports_power WHERE busway_item_id IS NOT NULL AND item_id = :itemId ) t WHERE dct_items_me.item_id = :itemId");

		q.setLong("itemId", itemId);
		q.executeUpdate();	
	}

}
