package com.raritan.tdz.item.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

@Transactional
public class ItemDAOImpl extends DaoImpl<Item> implements ItemDAO {

	@Autowired(required=true)
	PowerPortDAO powerPortDAO;
	
	@Autowired
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	public ItemDAOImpl() {
		
	}

	@Override
	public Long saveItem(Item item) {
		Session session = this.getSession();
		
		boolean isUpdate = item.getItemId(	) > 0 ? true : false;

		if (!isUpdate) {
			// Merge alone will not take care of tables that has only foreign key
			// For a new item with foreign, save shall be performed before merge
			session.save(item);
		}

		Item updatedItem = (Item)session.merge(item);
		
		session.flush();
		
		if(isUpdate){
			// session.refresh(updatedItem);
		}
		
		return updatedItem.getItemId();		
	}
			
	@Override
	public Item getItem(Long id){		
		return this.read(id);
	}
	
	@Override
	public Item getItemWithPortConnections(Long id) {
		Object result = null;
		Session session = null;
		
		session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.add(Restrictions.eq("itemId", id.longValue()));
		criteria.createAlias("dataCenterLocation", "dataCenterLocation", Criteria.LEFT_JOIN);
		criteria.createAlias("powerPorts", "powerPorts", Criteria.LEFT_JOIN);
		criteria.createAlias("powerPorts.sourcePowerConnections", "sourcePowerConn", Criteria.LEFT_JOIN);
		criteria.createAlias("powerPorts.destPowerConnections", "destPowerConn", Criteria.LEFT_JOIN);
		
		result = criteria.uniqueResult();
		
		return (Item)result;
	}


	
	@Override
	public void initPowerPortsAndConnectionsProxy( Item item ) {
		//if( item == null ) return;
		Set<PowerPort> allPowerPorts = item.getPowerPorts();
		if( allPowerPorts != null && allPowerPorts.size() > 0 ){
			for( PowerPort p : allPowerPorts ){
				Hibernate.initialize(p.getSourcePowerConnections());
				Hibernate.initialize(p.getDestPowerConnections());
				Hibernate.initialize(p.getItem());
			}
		}
	}

	@Override
	public void initPowerPortsAndSourceConnectionsProxy( Item item ) {
		//if( item == null ) return;
		Set<PowerPort> allPowerPorts = item.getPowerPorts();
		if( allPowerPorts != null && allPowerPorts.size() > 0 ){
			for( PowerPort p : allPowerPorts ){
				Hibernate.initialize(p.getSourcePowerConnections());
				// Hibernate.initialize(p.getDestPowerConnections());
				Hibernate.initialize(p.getItem());
			}
		}
	}

	@Override
	public void initLocationProxy( Item item ){
		Hibernate.initialize(item.getDataCenterLocation());	
	}
	
	
	@Override
	public Item loadItem(Long id){
		Item item = (Item)loadItem(Item.class, id, false);
		
		//Read item from current session in case that item found by load
		if(item == null){
			item = this.read(id);
		}
		return item;
	}
	
	@Transactional
	@Override
	public Long getItemId(String itemName, String locationCode) {
		
		if (null == itemName || null == locationCode) return null;
		
		Session session = this.getSession();
		
		Criteria c = session.createCriteria(Item.class);
		
		c.add(Restrictions.eq("itemName", itemName));
		
		c.createAlias("dataCenterLocation", "dataCenterLocation");
		c.add(Restrictions.eq("dataCenterLocation.code", locationCode)); 
		
		projectItemId(c);
		
		return (Long) c.uniqueResult();
		
	}

	@Transactional
	@Override
	public Item getItem(String itemName, String locationCode) {
		
		Session session = this.getSession();
		
		Criteria c = session.createCriteria(Item.class);
		
		c.add(Restrictions.eq("itemName", itemName));
		
		c.createAlias("dataCenterLocation", "dataCenterLocation");
		c.add(Restrictions.eq("dataCenterLocation.code", locationCode)); 
		
		// projectItemId(c);
		
		return (Item) c.uniqueResult();
		
	}

	private Criteria vpcItemCriteria(Long locationId, String powerChainLabel) {
		Session session = this.getSession();
		
		Criteria c = session.createCriteria(MeItem.class);
		
		c.createAlias("itemServiceDetails", "itemServiceDetails");
		c.createAlias("itemServiceDetails.originLookup", "originLookup");
		c.add(Restrictions.eq("originLookup.lkpValueCode", SystemLookup.ItemOrigen.VPC));

		if (null != locationId) {
			c.createAlias("dataCenterLocation", "dataCenterLocation");
			c.add(Restrictions.eq("dataCenterLocation.dataCenterLocationId", locationId));
		}

		if (null != powerChainLabel) {
			c.add(Restrictions.eq("chainLabel", powerChainLabel));
		}
		
		return c;
	}

	private void includeOnlyPowerOutlet(Criteria c) {
		c.createAlias("classLookup", "classLookup");
		c.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.FLOOR_OUTLET));
		
	}

	@Override
	public boolean isVpcPowerOutlet(Long itemId, Long locationId) {
		
		Criteria criteria = vpcItemCriteria(locationId, null);
		
		includeOnlyPowerOutlet(criteria);
		
		projectItemId(criteria);
		
		@SuppressWarnings("unchecked")
		List<Long> itemIds = criteria.list();
		
		return (itemIds.contains(itemId));
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Item> vpcPowerOutlets() {
		
		Criteria criteria = vpcItemCriteria(null, null);
		includeOnlyPowerOutlet(criteria);
		// bunty
		
		return criteria.list();
	}
	
	@Override
	public boolean isVpcInUse(Long locationId) {
		
		Criteria criteria = vpcItemCriteria(locationId, null);
		
		includeOnlyPowerOutlet(criteria);
		
		criteria.createAlias("powerPorts", "powerPorts");
		
		criteria.add(Restrictions.eq("powerPorts.used", true));
		
		criteria.setProjection(Projections.rowCount());
		Long vpcItemsInUse = (Long)criteria.uniqueResult();
		
		return (vpcItemsInUse > 0);
	}
	
	private void excludeUPSBank(Criteria c) {
		c.createAlias("classLookup", "classLookup");
		c.add(Restrictions.not(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.UPS_BANK)));
		
	}

	private void includeOnlyUPSBank(Criteria c) {
		c.createAlias("classLookup", "classLookup");
		c.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.UPS_BANK));
		
	}

	private void includeOnlyItemClass(Criteria c, Long itemClass) {
		c.createAlias("classLookup", "classLookup");
		c.add(Restrictions.eq("classLookup.lkpValueCode", itemClass));
		
	}
	
	private void projectItemId(Criteria c) {
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "itemId");
		c.setProjection(proList);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getVPCItems(Long locationId) {
		
		Criteria c = vpcItemCriteria(locationId, null);
		excludeUPSBank(c);
		projectItemId(c);
		
		return c.list();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getVPCItemsButUpsBank(Long locationId, String powerChainLabel) {
		
		Criteria c = vpcItemCriteria(locationId, powerChainLabel);
		excludeUPSBank(c);
		projectItemId(c);
		
		return c.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Item> getVPCUpsBank(Long locationId, String powerChainLabel) {
		Criteria c = vpcItemCriteria(locationId, powerChainLabel);
		includeOnlyUPSBank(c);
		
		return c.list();
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> getVPCItems(Long locationId, String powerChainLabel) {
		
		Criteria c = vpcItemCriteria(locationId, powerChainLabel);
		projectItemId(c);
		
		return c.list();
	}

	@Override
	public Item getVPCItem(Long locationId, String powerChainLabel, Long itemClass, PowerPort srcPort) {
		
		if (null == srcPort || null == srcPort.getVoltsLookup() || null == srcPort.getPhaseLookup() ||
				null == locationId || null == powerChainLabel) return null; 
		
		Criteria c = vpcItemCriteria(locationId, powerChainLabel);
		includeOnlyItemClass(c, itemClass);
		
		c.createAlias("powerPorts", "powerPorts");
		c.createAlias("powerPorts.voltsLookup", "voltsLks");
		c.createAlias("powerPorts.phaseLookup", "phaseLks");
		
		// for input voltage 120, 3-wire: select 120, 2-wire
		// for input voltage 120~240, 2-wire: select 120, 2-wire
		// for input voltage 120~240, 3-wire: select 208, 3-wire
		Long voltageLkp = srcPort.getVoltsLookup().getLkpValueCode();
		Long phaseLkp = srcPort.getPhaseLookup().getLkpValueCode();
		
		if (voltageLkp.equals(SystemLookup.VoltClass.V_120_240)) {
			if (phaseLkp.equals(SystemLookup.PhaseIdClass.SINGLE_2WIRE)) {
				voltageLkp = SystemLookup.VoltClass.V_120;
			}
			else if (phaseLkp.equals(SystemLookup.PhaseIdClass.SINGLE_3WIRE)) {
				voltageLkp = SystemLookup.VoltClass.V_208;
			} 
		}
		
		c.add(Restrictions.eq("voltsLks.lkpValueCode", voltageLkp));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("unchecked")
		List<Item> matchingPowerOutlets = c.list();
		if (matchingPowerOutlets.size() == 1) {
			// the only power outlet that matches the voltage of the source port
			return matchingPowerOutlets.get(0);
		}
		else {
			// get the exact match
			for (Item item: matchingPowerOutlets) {
				PowerPort port = item.getPowerPorts().iterator().next();
				if (null != port && port.getPhaseLookup().getLkpValueCode().equals(phaseLkp)) {
					return item;
				}
			}
			// get the other single phase
			if (phaseLkp.equals(SystemLookup.PhaseIdClass.SINGLE_2WIRE)) {
				phaseLkp = SystemLookup.PhaseIdClass.SINGLE_3WIRE;
			}
			else if (phaseLkp.equals(SystemLookup.PhaseIdClass.SINGLE_3WIRE)) {
				phaseLkp = SystemLookup.PhaseIdClass.SINGLE_2WIRE;
			}
			// get the other three phase
			else if (phaseLkp.equals(SystemLookup.PhaseIdClass.THREE_DELTA)) {
				phaseLkp = SystemLookup.PhaseIdClass.THREE_WYE;
			}
			else if (phaseLkp.equals(SystemLookup.PhaseIdClass.THREE_WYE)) {
				phaseLkp = SystemLookup.PhaseIdClass.THREE_DELTA;
			}
			for (Item item: matchingPowerOutlets) {
				PowerPort port = item.getPowerPorts().iterator().next();
				if (null != port && port.getPhaseLookup().getLkpValueCode().equals(phaseLkp)) {
					return item;
				}
			}
		}
		
		c.add(Restrictions.eq("phaseLks.lkpValueCode", phaseLkp));
		
		Item item = (Item) c.uniqueResult();
		
		return item;
	}
	
	@Override
	public void deleteVPCItems(Long locationId, String powerChainLabel) {
	
		Session session = this.getSession();
		List<Long> vpcItemIds = getVPCItems(locationId, powerChainLabel);
		
		if (null == vpcItemIds || vpcItemIds.size() == 0) return;
		
		Query qItemDetails = session.createSQLQuery(" select item_detail_id from dct_items where item_id in ( :itemIds ) ");
		qItemDetails.setParameterList("itemIds", vpcItemIds.toArray());
		@SuppressWarnings("unchecked")
		List<Long> itemDetailIds = qItemDetails.list();
		
		Query q = this.getSession().createSQLQuery("delete from dct_items_me where item_id in ( :itemIds )");
		q.setParameterList("itemIds", vpcItemIds.toArray());
		q.executeUpdate();
		session.flush();
		
		q = this.getSession().createSQLQuery(" delete from dct_connections_power where source_port_id in  ( select port_power_id from dct_ports_power where item_id in ( :itemIds ) ) or dest_port_id in ( select port_power_id from dct_ports_power where item_id in ( :itemIds ) ) ");
		q.setParameterList("itemIds", vpcItemIds.toArray());
		q.executeUpdate();
		session.flush();
		
		q = this.getSession().createSQLQuery(" delete from dct_ports_power where item_id in ( :itemIds ) ");
		q.setParameterList("itemIds", vpcItemIds.toArray());
		q.executeUpdate();
		session.flush();
		
		q = this.getSession().createSQLQuery(" delete from dct_items where item_id in ( :itemIds ) ");
		q.setParameterList("itemIds", vpcItemIds.toArray());
		q.executeUpdate();
		session.flush();
		
		if (null == itemDetailIds || itemDetailIds.size() == 0) return;
		
		q = this.getSession().createSQLQuery(" delete from dct_item_details where item_detail_id in ( :itemDetailIds ) ");
		q.setParameterList("itemDetailIds", itemDetailIds.toArray());
		q.executeUpdate();
		session.flush();
		
	}
	
	@Override
	public Object loadItem(Class<?> domainType, Long id, boolean readOnly){
		Object result = null;
		Session session = null;

		try {
			if (id != null && id > 0){
				session = this.getNewSession();
				//FIXME: Find out other ways to load port conenctions,
				//ups banks/fpdu power ports and conneciton. This might be too heavy 
				Criteria criteria = session.createCriteria(domainType);
				criteria.createAlias("itemServiceDetails", "itemServiceDetails", Criteria.LEFT_JOIN);
				criteria.createAlias("powerPorts", "powerPorts", Criteria.LEFT_JOIN);
				criteria.createAlias("powerPorts.sourcePowerConnections", "sourcePowerConn", Criteria.LEFT_JOIN);
				criteria.createAlias("powerPorts.destPowerConnections", "destPowerConn", Criteria.LEFT_JOIN);
				criteria.createAlias("bladeChassis", "bladeChassis", Criteria.LEFT_JOIN);
				criteria.createAlias("bladeChassis.itemServiceDetails", "chassisItemServiceDetails", Criteria.LEFT_JOIN);
				criteria.setFetchMode("dataCenterLocation", FetchMode.JOIN);
				criteria.setFetchMode("model", FetchMode.JOIN);
				criteria.setFetchMode("dataCenterLocation", FetchMode.JOIN);
				criteria.setFetchMode("parentItem", FetchMode.JOIN);
				criteria.setFetchMode("itemServiceDetails", FetchMode.JOIN);
				criteria.setFetchMode("itemServiceDetails.itemAdminUser", FetchMode.JOIN);
				criteria.setFetchMode("cracNwGrpItem", FetchMode.JOIN);
				criteria.setFetchMode("upsBankItem", FetchMode.JOIN);
				criteria.setFetchMode("upsBankItem.powerPorts", FetchMode.JOIN);
				criteria.setFetchMode("customFields", FetchMode.JOIN);
				criteria.setFetchMode("dataPorts", FetchMode.JOIN);
				criteria.setFetchMode("sensorPorts", FetchMode.JOIN);
				criteria.setFetchMode("itemSnmp", FetchMode.JOIN);
				criteria.setFetchMode("childItems", FetchMode.JOIN);
				criteria.add(Restrictions.eq("itemId", id));
				criteria.setReadOnly(readOnly);
				
				result = criteria.uniqueResult();
			}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}

	
	@Override
	public List<Item> getNonRackableItems (long cabinetId, long uPosition, long railsLkpValueCode) {
		// get list of items on shelf order by num port
    	Session session = this.getSession();
    	
		Criteria criteria = session.createCriteria(Item.class);
	
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("mountedRailLookup", "mountedRailLookup", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);	
		criteria.add(Restrictions.eq("parentItem.itemId", cabinetId));
		criteria.add(Restrictions.eq("uPosition", uPosition));
		criteria.add(Restrictions.eq("mountedRailLookup.lkpValueCode", railsLkpValueCode));
		criteria.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.NON_RACKABLE));
		//criteria.add(Restrictions.ne("model.mounting", "ZeroU"));
		
		
		criteria.addOrder(Order.asc("shelfPosition"));
		criteria.addOrder(Order.desc("itemId"));
		
		return (List<Item>) criteria.list();
	}
	
	@Override
	public List<ItItem> getSiblingItems(Item stackItem) throws DataAccessException{ 
		
		if(stackItem.getCracNwGrpItem() == null) return new ArrayList<ItItem>();
		
		Session session = this.getSession();
		Criteria c = session.createCriteria(ItItem.class);
		c.createAlias("cracNwGrpItem", "stack", Criteria.INNER_JOIN);		
		c.add(Restrictions.eq("stack.itemId", stackItem.getCracNwGrpItem().getItemId()));
		c.add(Restrictions.ne("stack.itemId", stackItem.getItemId()));
		c.addOrder(Order.asc("itemName"));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return c.list();
	}	

	private Criteria getChildrenItemCriteria(Item parentItem) {
		Session session = this.getSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("parentItem", "parent", Criteria.INNER_JOIN);		
		c.add(Restrictions.eq("parent.itemId", parentItem.getItemId()));

		return c;
	}
	
	@Override
	public List<Item> getChildrenItems(Item parentItem) throws DataAccessException{ 
		
		Criteria c = getChildrenItemCriteria(parentItem);
		c.addOrder(Order.asc("itemName"));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return c.list();
	}
	

	@Override
	public List<Item> getChildrenItemsOfClass(Item parentItem, Long classValueCode) throws DataAccessException{ 
		
		Criteria c = getChildrenItemCriteria(parentItem);
		c.createAlias("classLookup", "itemClass");
		c.add(Restrictions.eq("itemClass.lkpValueCode", classValueCode));
		c.addOrder(Order.asc("itemName"));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return c.list();
	}
	
	@Override
	public Map<Long, Long> getItemsStatus(List<Long> itemIds) {
		Session session = this.getSession();
		Criteria c = session.createCriteria(Item.class);
		
		c.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		c.add(Restrictions.in("itemId", itemIds));
		
		c.createAlias("statusLookup", "statusLookup");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "itemId");
		proList.add(Projections.property("statusLookup.lkpValueCode"), "status");
		
		c.setProjection(proList);

		@SuppressWarnings("unchecked")
		List<Object> list = c.list();
		
		Map<Long, Long> statusMap = new HashMap<Long, Long>();
		
		for (Object val: list) {
			Object[] map = (Object[]) val;
			statusMap.put((Long)map[0], (Long)map[1]);
		}
		
		return statusMap;
		
	}
	
	@Override
	public List<Long> getChildrenItemsStatus(Item parentItem) throws DataAccessException{ 
		
		Criteria c = getChildrenItemCriteria(parentItem);
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		c.createAlias("statusLookup", "statusLookup");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("statusLookup.lkpValueCode"), "status");

		return c.list();
	}	

	@Override
	public List<Long> getChildrenItemIds(Item parentItem) throws DataAccessException{ 
		
		Criteria c = getChildrenItemCriteria(parentItem);
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		// c.createAlias("itemId", "itemId");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "itemId");

		return c.list();
	}	


	@Override
	public List<ItItem> getChassisItems(ItItem chassis) throws DataAccessException{ 
		
		Session session = this.getSession();
		Criteria c = session.createCriteria(ItItem.class);
		c.createAlias("bladeChassis", "chassis", Criteria.INNER_JOIN);		
		c.add(Restrictions.eq("chassis.itemId", chassis.getItemId()));
		c.addOrder(Order.asc("itemName"));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return c.list();
	}	
	

	@Override
	public Long cloneItem(CloneItemDTO cloningCriteria, String currentUserName) {
		Session session = this.getSession();
				
		//create item using clone store procedure
		Query query = session.getNamedQuery("dcCloneItemSP");
		
		/*
		  <sql-query name="dcCloneItemSP" callable="false">
		    <return-scalar column="firstItemId" type="long"/>
		    select dc_cloneitem(:locationId, :itemId, :quantity, :userName, 
		    :includeChildren, :cloneCustomFieldData, :includeDataPorts, :includePowerPorts, :includeSensorPorts,
		    :keep_parent, :creationDate, :statusValueCode, :parentItemId, :includeFarEndPanel
		     ) as firstItemId;
		  </sql-query>
		*/
		query.setParameter("locationId", cloningCriteria.getClonedItemlocationId());
		query.setParameter("itemId", cloningCriteria.getItemIdToClone());
		query.setParameter("quantity", cloningCriteria.getQuantity());
		query.setParameter("userName", currentUserName);		
		query.setParameter("includeChildren", cloningCriteria.isIncludeChildren() ? 1 : 0);
		query.setParameter("cloneCustomFieldData", (cloningCriteria.isIncludeCustomFieldData() ? 1 : 0));		
		query.setParameter("includeDataPorts", cloningCriteria.isIncludeDataPort() ? 1 : 0);
		query.setParameter("includePowerPorts", cloningCriteria.isIncludePowerPort() ? 1 : 0);
		query.setParameter("includeSensorPorts", cloningCriteria.isIncludeSensorPort() ? 1 : 0);
		query.setParameter("keep_parent", cloningCriteria.isKeepParentChildAssoc() ? 1 : 0);
		query.setParameter("creationDate", cloningCriteria.getCreationDate());
		query.setParameter("statusValueCode", cloningCriteria.getStatusValueCode());
		query.setParameter("parentItemId", -1);
		query.setParameter("includeFarEndPanel", cloningCriteria.isIncludeFarEndDataPanel() ? 1 : 0);
		
		Long firstItemId = (Long)query.uniqueResult();		
		
		//session.flush();
		
		return firstItemId;
	}

	@Override
	public Boolean isItemTagVerified(Long itemId) {
		Session session = this.getSession();
	
		Criteria c = session.createCriteria(Item.class); 
		c.add(Restrictions.eq("itemId", itemId));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return ((Item)c.uniqueResult()).getIsAssetTagVerified();
	}	

	@SuppressWarnings("unchecked")
	@Override
	public List<?> getAllStackItems(Long primaryItemId) {
		Session session = this.getSession();

		Criteria c = session.createCriteria(Item.class);
		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		c.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
		c.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);
		c.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "id");
		proList.add(Projections.property("itemName"), "name");
		proList.add(Projections.property("model.modelName"), "modelName");
		proList.add(Projections.property("parentItem.itemName"), "cabinetName");
		proList.add(Projections.property("uPosition"), "UPosition");
		proList.add(Projections.property("statusLookup.lkpValueCode"), "status");
		proList.add(Projections.property("sibling.itemId"), "siblingId");

		c.addOrder(Order.asc("name"));
		
		// sibling item id of input item is eq to sibling item id of the items returned
		
		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.RACKABLE));
		c.add(Restrictions.eq("model.formFactor", SystemLookup.FormFactor.FIXED));
		c.add(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.NETWORK_STACK));
		c.add(Restrictions.eq("sibling.itemId", primaryItemId));
		c.setProjection(proList);
		c.setResultTransformer(Transformers.TO_LIST);
		
		return c.list();
	}
	
	@Override
	public List<Object> getStackablesItemIds() {
		Session session = this.getSession();
		
		Query query = session.getNamedQuery("getStackableItems");
		query.setLong("subclassLkpValueCode", SystemLookup.SubClass.NETWORK_STACK);
		query.setString("modelMounting", SystemLookup.Mounting.RACKABLE);
		query.setString("modelFormFactor", SystemLookup.FormFactor.FIXED);
	
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getAddableStackItems() {
		Session session = this.getSession();
		
		//get stackable itemIds
		List<Object> itemIds = getStackablesItemIds();
		
		if (itemIds.size() == 0) {
			return null;
		}
		
		// For each itemIds in the above list, project the fields to fill up StackItemDTO 
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		criteria.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
		criteria.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "id");
		proList.add(Projections.property("itemName"), "name");
		proList.add(Projections.property("model.modelName"), "modelName");
		proList.add(Projections.property("parentItem.itemName"), "cabinetName");
		proList.add(Projections.property("uPosition"), "UPosition");
		proList.add(Projections.property("statusLookup.lkpValueCode"), "status");
		proList.add(Projections.property("sibling.itemId"), "siblingId");
		
		criteria.addOrder(Order.asc("itemName"));
		criteria.add(Restrictions.in("itemId", itemIds));
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.TO_LIST);
		
		return criteria.list();
	}
	
	@Override
	public Object getLastStackItemNumber(long primaryItemId) {
		Session session = this.getSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);

		c.add(Restrictions.eq("sibling.itemId", primaryItemId));

		ProjectionList proj = Projections.projectionList();
		proj = proj.add(Projections.max("numPorts"));
		c.setProjection(proj);
		
		return c.uniqueResult();
	}	
	
	@Override
	public Item getNetworkStackItem(long itemId) {
		Session session = this.getSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);

		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.RACKABLE));
		c.add(Restrictions.eq("model.formFactor", SystemLookup.FormFactor.FIXED));
		c.add(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.NETWORK_STACK));
		c.add(Restrictions.eq("itemId", itemId));
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return  (Item)c.uniqueResult();
	}
	

	@Override
	public List<Item> getNetworkStackItems(Long primaryItemId) {
		Session session = this.getSession();

		Criteria c = session.createCriteria(Item.class);
		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
		c.createAlias("cracNwGrpItem", "sibling", Criteria.LEFT_JOIN);

		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.RACKABLE));
		c.add(Restrictions.eq("model.formFactor", SystemLookup.FormFactor.FIXED));
		c.add(Restrictions.eq("subclassLookup.lkpValueCode", SystemLookup.SubClass.NETWORK_STACK));
		c.add(Restrictions.eq("sibling.itemId", primaryItemId));
		return c.list();
	}	

	@Override
	public String getItemName(long itemId) {
		if (itemId > 0) {
			Criteria c = this.getSession().createCriteria(Item.class);
			c.add(Restrictions.eq("itemId", itemId));
			ProjectionList proList = Projections.projectionList();
			proList.add(Projections.property("itemName"), "itemName");
			c.setProjection(proList);
			
			return (String)c.uniqueResult();
		}
		
		return "<Unknown>";
	}
	
	@Override
	public List<String> getItemsToDeleteInvalidStages (List<Long> itemIdList) {
		if(itemIdList.size() == 0) return new ArrayList<String>();
		
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
 		
		// find out if any item in itemIdList has approved pending request
		Session session = this.getSession();	
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestPointers", "pointer");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
		criteria.add(Restrictions.eq("history.current", true));		    
		criteria.add(Restrictions.in("itemId", itemIdList));
    	criteria.add(Restrictions.in("historyStages.lkpValueCode", requestStages));
		criteria.createAlias("requestHistories", "history");
		
		// collect list of item ids matching above criteria 
    	List<Long> itemIds = new ArrayList<Long>();
    	for (Object r: criteria.list()) {
    		itemIds.add(((Request)r).getItemId());
    	}

    	// find out item names for logging 
		List<String>  itemNames = new ArrayList<String>();;
    	if (itemIds.size() > 0) {
			Criteria c = session.createCriteria(Item.class);
			c.add(Restrictions.in("itemId", itemIds));
			c.addOrder(Order.asc("itemName"));

			ProjectionList proList = Projections.projectionList();
			proList.add(Projections.property("itemName"), "itemName");
			c.setProjection(proList);
			c.setResultTransformer(Transformers.TO_LIST);

			for(Object rec:c.list()){
				List row = (List) rec;
				itemNames.add((String)row.get(0));
			}
		}
		return itemNames;
	}		
	
	@Override
	public List<Long> getItemIdsToDelete(long itemId){
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items as i where i.parent_item_id = :itemId ")
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id where it.chassis_id = :itemId ")
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_items as chassis on it.chassis_id = chassis.item_id where chassis.parent_item_id = :itemId order by item_id ")
		.toString()
	    );
		
		q.setLong("itemId", itemId);
		
		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getPassiveChildItemIds(Long itemId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "itemId");		
		criteria.setProjection(proList);

		criteria.createAlias("classLookup", "classLookup");
		criteria.createAlias("parentItem", "parentItem");
		criteria.add(Restrictions.eq("parentItem.itemId", itemId));
		criteria.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.PASSIVE));
		

		return criteria.list();

	}
	
	@Override
	public List<String> getItemsToDeleteNotNew(List<Long> itemIds){
		if(itemIds.size() == 0) return new ArrayList<String>();
		
		Session session = this.getSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("statusLookup", "status");
		c.createAlias("classLookup", "itemClass");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemName"), "itemName");		
		c.setProjection(proList);
		c.setResultTransformer(Transformers.TO_LIST);
		
		//These two classes are set to install by default in windows client
		List<Long> classLks = new ArrayList<Long>();
		//classLks.add(SystemLookup.Class.BLANKING_PLATE);
		classLks.add(SystemLookup.Class.PERFORATED_TILES);
		classLks.add(SystemLookup.Class.PASSIVE);
		
		c.add(Restrictions.in("itemId", itemIds));
		c.add(Restrictions.ne("status.lkpValueCode", SystemLookup.ItemStatus.PLANNED));
		c.add(Restrictions.ne("status.lkpValueCode", SystemLookup.ItemStatus.HIDDEN));
		c.add(Restrictions.not(Restrictions.in("itemClass.lkpValueCode", classLks)));
		c.addOrder(Order.asc("itemName"));
				
		List<String> recList = new ArrayList<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}
		
		return recList;
	}	

	public Object loadPowerPort(Long id){
		Object result = null;
		Session session = null;

		try {
			if (id != null && id > 0){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(PowerPort.class);
				criteria.createAlias("sourcePowerConnections", "sourcePowerConn", Criteria.LEFT_JOIN);
				criteria.createAlias("destPowerConnections", "destPowerConn", Criteria.LEFT_JOIN);
				criteria.createAlias("item", "item", Criteria.LEFT_JOIN);
				criteria.add(Restrictions.eq("portId", id));
				criteria.setReadOnly(true);
				
				result = criteria.uniqueResult();
			}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}

	
	private List<PowerPort> getBranchCircuitBreakerPorts(PowerPort portObj) {
		Set<PowerPort> recList = new HashSet<PowerPort>();
		if (null == portObj) {
			return (new ArrayList<PowerPort>(recList));
		}

		/*Hibernate.initialize(port.getSourcePowerConnections());
		Hibernate.initialize(port.getDestPowerConnections());
		Hibernate.initialize(port.getItem());*/
		
		PowerPort port = (PowerPort) loadPowerPort(portObj.getPortId());
		
		if(port.getDestPowerConnections() != null ) { 
			for (PowerConnection pc: port.getDestPowerConnections()) {
				// PowerPort srcPort = (PowerPort)pc.getSourcePort();
				PowerPort srcPort = (PowerPort) loadPowerPort(((PowerPort)pc.getSourcePort()).getPortId());
				long srcConnPortSubClass = srcPort.getPortSubClassLookup().getLkpValueCode().longValue();
				if (srcConnPortSubClass == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
					Set<PowerConnection> conns = srcPort.getDestPowerConnections();
					if (null == conns || conns.size() > 0) {
						if (!recList.contains(srcPort))
						{
							recList.add(srcPort);
						}
					}
				}
				else {
					List<PowerPort> srcPorts = getBranchCircuitBreakerPorts(srcPort);
					recList.addAll(srcPorts);
				}
			}
		}
		
		return (new ArrayList<PowerPort>(recList));
	}
	
	private List<PowerPort> getFarEndConnectedPort(PowerPort port) {
		Set<PowerPort> recList = new HashSet<PowerPort>();
		if (null == port) {
			return (new ArrayList<PowerPort>(recList));
		}
		Hibernate.initialize(port.getSourcePowerConnections());
		Hibernate.initialize(port.getDestPowerConnections());
		Hibernate.initialize(port.getItem());
		
		if(port.getDestPowerConnections() != null ) { 
			for (PowerConnection pc: port.getDestPowerConnections()) {
				PowerPort srcPort = (PowerPort)pc.getSourcePort();
				long srcConnPortSubClass = srcPort.getPortSubClassLookup().getLkpValueCode().longValue();
				if (srcConnPortSubClass == SystemLookup.PortSubClass.POWER_SUPPLY || 
						srcConnPortSubClass == SystemLookup.PortSubClass.BUSWAY_OUTLET || 
						srcConnPortSubClass == SystemLookup.PortSubClass.WHIP_OUTLET) {
					if (!recList.contains(srcPort))
					{
						recList.add(srcPort);
					}
				}
				else {
					List<PowerPort> srcPorts = getFarEndConnectedPort(srcPort);
					recList.addAll(srcPorts);
				}
			}
		}
		
		return (new ArrayList<PowerPort>(recList));
	}
	
	private List<String> getPortConnectedPanels(PowerPort port) {
		Set<String> recList = new HashSet<String>();
		if (null == port) {
			return (new ArrayList<String>(recList));
		}
		powerPortDAO.initPowerPortsAndConnectionsProxy(port);
		
		String itemName = (null != port.getPortSubClassLookup() && 
									null != port.getPortSubClassLookup().getLkpValueCode() && 
									(port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.PANEL_BREAKER || 
									port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)) ? 
						port.getItem().getItemName() : "";
		
		if(port.getDestPowerConnections() != null ) { 
			for (PowerConnection pc: port.getDestPowerConnections()) {
				PowerPort srcPort = (PowerPort)pc.getSourcePort();
				long srcConnPortSubClass = srcPort.getPortSubClassLookup().getLkpValueCode().longValue();
				if (srcConnPortSubClass == SystemLookup.PortSubClass.POWER_SUPPLY || 
						srcConnPortSubClass == SystemLookup.PortSubClass.BUSWAY_OUTLET || 
						srcConnPortSubClass == SystemLookup.PortSubClass.WHIP_OUTLET) {
					if (srcPort.getUsed() && !recList.contains(itemName)) {
						recList.add(itemName);
					}
				}
				else {
					List<String> panels = getPortConnectedPanels(srcPort);
					recList.addAll(panels);
				}
			}
		}
		
		return (new ArrayList<String>(recList));
	}
	
	@Override
	public List<String> getPanelsConnectedToCircuitedOutlets(Long itemId) {
		Set<String> recList = new HashSet<String>();

		Item item = getItemWithPortConnections(itemId);
		// String itemName = (item.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.FLOOR_PDU && null != item.getSubclassLookup()) ? item.getItemName() : "";

		Set<PowerPort> ports = item.getPowerPorts();
		if (ports == null) return new ArrayList<String>(recList);
		
		Set<PowerPort> srcPortList = new HashSet<PowerPort>();
		for (PowerPort port: ports) {
			List<PowerPort> srcPorts = getFarEndConnectedPort(port);
			for (PowerPort farEndPort: srcPorts) {
				if (farEndPort.getUsed()) {
					srcPortList.add(farEndPort);
				}
			}
			// srcPortList.addAll(srcPorts);
		}
		for (PowerPort port: srcPortList) {
			Set<PowerConnection> conns = port.getSourcePowerConnections();
			for (PowerConnection conn: conns) {
				if (null != conn.getDestPort() && 
						conn.getDestPort().getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
					String itemName = conn.getDestPort().getItem().getItemName();
					if (!recList.contains(itemName)) {
						recList.add(itemName);
					}
				}
			}
		}
		
		return (new ArrayList<String>(recList));
	}
	
	private List<String> getConnectedPanels(Long itemId) {
		Session session = this.getSession();
		
		Query query = session.getNamedQuery("getCircuitedItemsToPanel");
		query.setParameter("itemId", itemId);

		@SuppressWarnings("unchecked")
		List<String> connectedItems = (List<String>) query.list();
	
		return connectedItems;
	}
	
	private List<String> getConnectedPanelsUsingHibernate(Long itemId) {
		Set<String> recList = new HashSet<String>();

		// Item item = getItemWithPortConnections(itemId);
		Item item = loadItem(itemId);
		// String itemName = (item.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.FLOOR_PDU && null != item.getSubclassLookup()) ? item.getItemName() : "";

		if (null == item) return new ArrayList<String>(recList);
		Set<PowerPort> ports = item.getPowerPorts();
		if (ports == null) return new ArrayList<String>(recList);
		
		Set<PowerPort> bcPortList = new HashSet<PowerPort>();
		for (PowerPort port: ports) {
			List<PowerPort> bcPorts = getBranchCircuitBreakerPorts(port);
			bcPortList.addAll(bcPorts);
		}
		for (PowerPort port: bcPortList) {
			String itemName = port.getItem().getItemName();
			Set<PowerConnection> conns = port.getDestPowerConnections();
			if (null != conns && conns.size() > 0) {
				for (PowerConnection conn: conns) {
					PowerPort srcPort = (PowerPort) conn.getSourcePort();
					if (!srcPort.isOutlet() || srcPort.getUsed()) {
						if (!recList.contains(itemName)) {
							recList.add(itemName);
						}
					}
				}
			}
		}
		
		return (new ArrayList<String>(recList));
	}
	
	private List<String> getConnectedPanels1(Long itemId) {
		Set<String> recList = new HashSet<String>();

		Item item = getItemWithPortConnections(itemId);
		String itemName = (item.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.FLOOR_PDU && null != item.getSubclassLookup()) ? item.getItemName() : "";

		Set<PowerPort> ports = item.getPowerPorts();
		if (ports == null) return new ArrayList<String>(recList);
		
		for (PowerPort port: ports) {
			if(port.getDestPowerConnections() != null ) { 
				for (PowerConnection pc: port.getDestPowerConnections()) {
					PowerPort srcPort = (PowerPort)pc.getSourcePort();
					long srcConnPortSubClass = srcPort.getPortSubClassLookup().getLkpValueCode().longValue();
					if (srcConnPortSubClass == SystemLookup.PortSubClass.POWER_SUPPLY || 
							srcConnPortSubClass == SystemLookup.PortSubClass.BUSWAY_OUTLET || 
							srcConnPortSubClass == SystemLookup.PortSubClass.WHIP_OUTLET) {
						if (srcPort.getUsed()) {
							recList.add(itemName);
						}
					}
					else {
						List<String> panels = getConnectedPanels(srcPort.getItem().getItemId());
						recList.addAll(panels);
					}
				}
			}
		}

		return (new ArrayList<String>(recList)); 
	}
	
	@Override
	public List<String> getFPDUItemToDeleteConnected(Long itemId) {
		Session session = this.getSession();
		Criteria c = session.createCriteria(DataPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("item.itemId", itemId));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		Set<String> recList = new HashSet<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}

		//DO POWER
		List<String> panels = getConnectedPanels(itemId);
		recList.addAll(panels);

		
		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}

	private Criteria getOutletListForPanelListCriteria(List<Long> panelItemIds) {
		Session session = this.getSession();
		
		Criteria c = session.createCriteria(MeItem.class);
		c.createAlias("pduPanelItem", "pduPanelItem");
		c.createAlias("pduPanelItem.subclassLookup", "pduPanelSubClass");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemId"), "itemId");
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);

		c.add(Restrictions.in("pduPanelItem.itemId", panelItemIds));
		c.addOrder(Order.asc("itemId"));
		
		return c;
	}

	private List<Long> getCriteriaList(Criteria c) {

		Set<Long> recList = new HashSet<Long>();
		// List<Object> oList = c.list();
		
		for(Object rec: c.list()) {
			@SuppressWarnings("rawtypes")
			List row = (List) rec;

			recList.add((Long)row.get(0));
		}
		
		List<Long> list = new ArrayList<Long>(recList); 
		java.util.Collections.sort(list);
		
		return list;
		
	}
	
	@Override
	public List<Long> getPowerPanelConnectedPowerOutlet(List<Long> panelItemIds) {
		
		Criteria c = getOutletListForPanelListCriteria(panelItemIds);
		
		return getCriteriaList(c);
	}

	private List<Long> getPowerPanelConnectedPowerOutlet(List<Long> panelItemIds, Long subClassLkpValueCode) {

		if (null == panelItemIds || panelItemIds.size() == 0) {
			return (new ArrayList<Long>());
		}
		Criteria c = getOutletListForPanelListCriteria(panelItemIds);
		c.add(Restrictions.eq("pduPanelSubClass.lkpValueCode", subClassLkpValueCode));
		
		return getCriteriaList(c);
	} 
	
	@Override
	public List<Long> getBuswayPowerPanelConnectedPowerOutlet(List<Long> panelItemIds) {
		
		return getPowerPanelConnectedPowerOutlet(panelItemIds, SystemLookup.SubClass.BUSWAY);
		
	}

	@Override
	public List<Long> getLocalPowerPanelConnectedPowerOutlet(List<Long> panelItemIds) {
		
		return getPowerPanelConnectedPowerOutlet(panelItemIds, SystemLookup.SubClass.LOCAL);
		
	}
	
	@Override
	public List<Long> getRemotePowerPanelConnectedPowerOutlet(List<Long> panelItemIds) {
		
		return getPowerPanelConnectedPowerOutlet(panelItemIds, SystemLookup.SubClass.REMOTE);
		
	}


	public List<String> getItemToDeletePowerConnected(List<Long> itemIds) {
		Session session = this.getSession();
		
		Set<String> recList = new HashSet<String>();
		Criteria c = session.createCriteria(PowerPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}
		
		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}
	
	@Override
	public List<String> getItemToDeleteConnected(List<Long> itemIds){
		Session session = this.getSession();
		Criteria c = session.createCriteria(DataPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		Set<String> recList = new HashSet<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}

		//DO POWER
		c = session.createCriteria(PowerPort.class);
		c.createAlias("item", "item");
		
		proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.in("item.itemId", itemIds));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}
		
		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}
	

	@Override
	public List<String> getPowerPanelItemToDeleteConnected(Long itemId){
		Session session = this.getSession();
		Criteria c = session.createCriteria(DataPort.class);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("item.itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("item.itemId", itemId));
		c.add(Restrictions.eq("used", true));		
		c.addOrder(Order.asc("item.itemName"));
		
		Set<String> recList = new HashSet<String>();
		
		for(Object rec:c.list()){
			List row = (List) rec;

			recList.add((String)row.get(0));
		}

		//DO POWER
		List<String> panels = getConnectedPanels(itemId);
		recList.addAll(panels);

		List<String> list = new ArrayList<String>(recList); 
		java.util.Collections.sort(list);
		
		return list;
	}
	

	
	@Override
	public Long getItemClass(long itemId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("classLookup.lkpValueCode"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		Long classLkpValueCode = (Long) criteria.uniqueResult();
		
		return classLkpValueCode; 
	}	

	@Override
	public Long getItemSubClass(long itemId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("subclassLookup.lkpValueCode"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		Object subclass = criteria.uniqueResult();
		if (null == subclass) return null;
		Long subclassLkpValueCode = (Long) subclass;
		
		return subclassLkpValueCode; 
	}	


	@Override
	public String getItemClassName(long itemId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("classLookup", "classLookup", Criteria.INNER_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("classLookup.lkpValue"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		String classLkpValue = (String) criteria.uniqueResult();
		
		return classLkpValue; 
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId){
		Criteria criteria = this.getSession().createCriteria(ItItem.class);
		criteria.createCriteria("bladeChassis").add(Restrictions.eq("itemId", chassisItemId));
		criteria.addOrder(Order.asc("itemName"));
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId, Long faceLksValueCode){
		Criteria criteria = this.getSession().createCriteria(ItItem.class);
		criteria.createAlias("facingLookup", "facingLookup");		
		criteria.createCriteria("bladeChassis").add(Restrictions.eq("itemId", chassisItemId));
		criteria.add(Restrictions.eq("facingLookup.lkpValueCode", faceLksValueCode));
		
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<ItItem> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		criteria.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
    	
		criteria.add(Restrictions.eq("parentItem.itemId", cabinetItemId));
		criteria.add(Restrictions.eq("model.formFactor", "Chassis"));
		
		if (SystemLookup.Class.DEVICE == bladeTypeLkpValueCode) {
			criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("classLookup.lkpValueCode", bladeTypeLkpValueCode));
		}

		criteria.add(Restrictions.and(Restrictions.isNotNull("statusLookup"),
                Restrictions.ne("statusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED)
                ));

		criteria.addOrder(Order.asc("itemName"));
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Item> getItemsWithMounting(String mounting){  //old method name getAllBladeItem(), should be an inner join in criteria
		Criteria criteria =  this.getSession().createCriteria(Item.class);
		//criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("model", "model", Criteria.INNER_JOIN);
		criteria.add(Restrictions.eq("model.mounting", mounting));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getAllChassis(long locationId, long bladeTypeLkpValueCode) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);		
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("dataCenterLocation","dataCenterLocation", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem.statusLookup", "parentStatusLookup", Criteria.LEFT_JOIN);

		
		if (SystemLookup.Class.DEVICE == bladeTypeLkpValueCode) {
			criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("classLookup.lkpValueCode", bladeTypeLkpValueCode));
		}

		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("itemId"))
				.add(Projections.property("parentItem.itemId"))
				.add(Projections.property("parentItem.itemName"))));

    	criteria.add(Restrictions.eq("dataCenterLocation.dataCenterLocationId", locationId));
		criteria.add(Restrictions.eq("model.formFactor", "Chassis"));
		
		criteria.add(Restrictions.and(Restrictions.isNotNull("parentItem.statusLookup"),     
                Restrictions.ne("parentStatusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED)
                ));
		
		criteria.addOrder(Order.asc("itemName"));
		
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getAllChassisModelIdsForBladeModel(long bladeModelId) {
		List<Long> chassisItemIdList = 
				getAllChassisItemIdsForBladeModel(bladeModelId);
		
		if (chassisItemIdList.isEmpty()) {
			return chassisItemIdList;
		}
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("model.modelDetailId"))));
		criteria.add(Restrictions.in("itemId", chassisItemIdList));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override	
	public List<Long> getAllChassisItemIdsForBladeModel(long bladeModelId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("bladeChassis", "bladeChassis", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("model.modelDetailId", bladeModelId));
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("bladeChassis.itemId"))));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return criteria.list();
	}	
	
	
	//Santo WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//code related to delete, do not change code and check-in it without doing proper testing
	//issues were found where deleting items that has children, resulted in constraint violation
	//there is room to make this code better by using HQL
	@Override
	public void removeChildrenFromItem(Item parentItem)	 {
		//Update items that have this cabinet as the parent
		Query q = this.getSession().createSQLQuery("update dct_items set parent_item_id = null where parent_item_id = :itemId");
		q.setLong("itemId", parentItem.getItemId());
		q.executeUpdate();
	}

	@Override
	public void deleteCabinetRecord(Item parentItem)	 {
		removeChildrenFromItem(parentItem);

		//should be able to do session.delete(parentItem) instead of doing code below, keeping code
		Query q = this.getSession().createSQLQuery("delete from dct_items_cabinet where item_id = :itemId");
		q.setLong("itemId", parentItem.getItemId());
		q.executeUpdate();
	}	
	

	@Override
	public void deleteMeRecord(Item item)	 {
		//should be able to do session.delete(item) instead of doing code below, keeping code
		Query q = this.getSession().createSQLQuery("delete from dct_items_me where item_id = :itemId");
		q.setLong("itemId", item.getItemId());
		q.executeUpdate();
	}	

	@Override
	public void deleteItRecord(Item item)	 {
		//should be able to do session.delete(item) instead of doing code below, keeping code
		Query q = this.getSession().createSQLQuery("delete from dct_items_it where item_id = :itemId");
		q.setLong("itemId", item.getItemId());
		q.executeUpdate();
	}	
			

	@Override
	public void delete(Item item) {		
		long itemId = item.getItemId();
		Session session = this.getSession();
		
		//Delete dct_items_me, dct_items_it, dct_items_cabinet
		deleteCabinetRecord(item);
		deleteMeRecord(item);
		deleteItRecord(item);
		
		//Delete Item Record
		ItemServiceDetails detail = item.getItemServiceDetails();
		
		Query q = session.createSQLQuery("delete from dct_items where item_id = :itemId");
		q.setLong("itemId", itemId);
		q.executeUpdate();
		
		if(detail != null){
			Long itemDetailId = detail.getItemServiceDetailId();
	
			q = session.createSQLQuery("delete from dct_item_details where item_detail_id = :itemDetailId");
			q.setLong("itemDetailId", itemDetailId);
			q.executeUpdate();
		}
		
		session.flush();
		// session.refresh(item);
	}
		
	@Override
	public String getLocationCode(Long itemId){
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("dataCenterLocation", "location",Criteria.INNER_JOIN);
		criteria.setProjection(Projections.property("location.code"));
		criteria.add(Restrictions.eq("itemId", itemId));
		String result = (String) criteria.uniqueResult();
		return result;
	}
	
	@Override
	public void updateSiblingItemId(long oldSiblingId, long newSiblingId) {
		Query q = this.getSession().createSQLQuery("update dct_items set sibling_item_id = :newSiblingId where sibling_item_id = :oldSiblingId");
		q.setLong("newSiblingId", newSiblingId);
		q.setLong("oldSiblingId", oldSiblingId);
		q.executeUpdate();
	}
	
	@Override
	public void removeBladesFromChassis(Item chassis){
		// clear the chassis reference
		Session session = this.getSession();
		
		// clear the slot position and grouping name
		Query q = session.createSQLQuery("update dct_items set slot_position=-9,grouping_name=null where item_id in (select item_id from dct_items_it where chassis_id = :itemId)");
		q.setLong("itemId", chassis.getItemId());
		q.executeUpdate();

		// clear the chassis
		q = session.createSQLQuery("update dct_items_it set chassis_id=null where chassis_id = :itemId");
		q.setLong("itemId", chassis.getItemId());
		q.executeUpdate();
	}	

	@Override
	public List<Item> getAllCabinets(String siteCode) {
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("classLookup", "classLookup");
		if( siteCode != null && siteCode.length() > 0){
			criteria.createAlias("dataCenterLocation","dataCenterLocation",Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("dataCenterLocation.code",siteCode));
		}
		criteria.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.CABINET));		
		criteria.addOrder(Order.asc("itemName"));
		
		return (List<Item>) criteria.list();
	}

	private List<Long> getMovingItems() {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(PowerPortMove.class);

		criteria.add(Restrictions.isNotNull("request"));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("moveItem.itemId"), "moveItemId");
		criteria.setProjection(proList);
		
		@SuppressWarnings("unchecked")
		List<Long> moveItemIds = criteria.list();
		
		return moveItemIds;
		
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getAllCabinetsIdNameForSiteCode(String siteCode) {
    	Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);

		criteria.setProjection((Projections.projectionList()
				.add(Projections.alias(Projections.property("itemId"), "itemId"))
				.add(Projections.alias(Projections.property("itemName"), "itemName"))));

		criteria.createAlias("classLookup", "classLookup");
		if( siteCode != null && siteCode.length() > 0){
			criteria.createAlias("dataCenterLocation","dataCenterLocation",Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("dataCenterLocation.code",siteCode));
		}
		criteria.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.CABINET));
		
		List<Long> moveItemIds = getMovingItems();
		if (null != moveItemIds && moveItemIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("itemId", moveItemIds)));
		}
		
		criteria.addOrder(Order.asc("itemName"));
        criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        return criteria.list();
	}	


	@Override
	public List<Long> getAllFloorPDUItemIds() {
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items i where i.class_lks_id = 11 AND i.subclass_lks_id IS NULL")
		.toString());

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}

	@Override
	public Map<Long, Long> getAllFloorPDUWithUPSBank() {
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id, me.ups_bank_item_id ")
		.append("from dct_items i inner join dct_items_me me on i.item_id = me.item_id ") 
		.append("WHERE i.class_lks_id = 11 AND i.subclass_lks_id IS NULL AND me.ups_bank_item_id IS NOT NULL ")
		.toString());
		
		Map<Long, Long> recMap = new HashMap<Long, Long>();
		
		for (Object rec:q.list()) {
			Object[] row = (Object[]) rec;
			BigInteger floorPduId = (BigInteger)row[0];
			BigInteger upsBankId = (BigInteger)row[1];
			recMap.put(floorPduId.longValue(), upsBankId.longValue());
		}

		return recMap;
	}
	
	@Override
	public List<Long> getAllPowerPanelItemIds() {
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items i WHERE i.class_lks_id = 11 AND i.subclass_lks_id IS NOT NULL AND i.parent_item_id IS NOT NULL")
		.toString());

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}

	
	@Override
	public List<Long> getAssociatedItemIds(long itemId){
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items as i where i.parent_item_id = :itemId ")
		.append(" UNION select i.item_id from dct_items_it as i  where i.chassis_id = :itemId ")
		.append(" UNION select i.item_id from dct_items as i where i.sibling_item_id = :itemId and i.item_id <> :itemId order by item_id ")
		.toString()
	    );
		
		q.setLong("itemId", itemId);
		
		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}
	
	private String getOutputWiringDesc( UPSBankDTO upsBank ){
		String outputWiring = null;
		if( upsBank.getOutputWiringLkpValueCode() != null ){
			if( upsBank.getOutputWiringLkpValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_DELTA ){
				outputWiring = "3-Wire + Ground";
			}else if( upsBank.getOutputWiringLkpValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_WYE ){
				outputWiring = "4-Wire + Ground";
			}else{
				System.out.println("Unsupported input wiring for the UPS Bank: " + upsBank.getUpsBankName());
				return null;
			}
		}
		return outputWiring;
	}
	
	
	@Override
	public List<UPSBankDTO> getAllUPSBanksMatchingRating( Long minAmpsRating ){
		List<UPSBankDTO> retval = null;
		Session session = this.getSession();
		
		//If minAmspRating is null, return all ups banks
		if (minAmpsRating == null) {
			minAmpsRating = new Long (0);
		}
		
		Query query = session.getNamedQuery("getUPSBanksMatchingRating");
		query.setParameter("upsBankBreakerPort", SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);
		query.setParameter("upsBankSubClass", SystemLookup.Class.UPS_BANK);
		
		query.setParameter("minAmpsRating", minAmpsRating.doubleValue());
		query.setResultTransformer(Transformers.aliasToBean(UPSBankDTO.class));
		retval = (List<UPSBankDTO>) query.list();
		for (UPSBankDTO upsB : retval ) {
			upsB.setOutputWiringDesc(getOutputWiringDesc(upsB));
		}

		return retval;
	}
	
	@Override
	public List<Item> searchItemsBySearchString( String searchString ){
		List<Item> retval = null;
		Session session = this.getSession();
		StringBuilder partialMatch = new StringBuilder();
		partialMatch.append("%");
		partialMatch.append(searchString);
		partialMatch.append("%");
		
		Query query = session.getNamedQuery("searchItemsBySearchString");
		query.setParameter("exactMatch", searchString);
		query.setParameter("partialMatch", partialMatch.toString());
		
		retval = (List<Item>) query.list();
		
		return retval;
	}

	@Override
	public List<Item> searchItemsBySearchStringWithLocation( String searchString , String locationString, int limit, int offset){
		List<Item> retval = null;
		Session session = this.getSession();
		StringBuilder partialMatch = new StringBuilder();
		partialMatch.append("%");
		partialMatch.append(searchString);
		partialMatch.append("%");
		
		
		String[] temp = locationString.split(",");
		List<Long> locationList = new ArrayList<Long>();
		for (int i=0; i < temp.length; i++)
			locationList.add(Long.valueOf(temp[i]));
				
		
		Query query = session.getNamedQuery("searchItemsBySearchStringWithLocation");
		query.setParameter("exactMatch", searchString);
		query.setParameter("partialMatch", partialMatch.toString());
		query.setParameterList("locations", locationList);
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		//query.setP
		retval = (List<Item>) query.list();
	
		return retval;
	}
	
	
	@Override
	public List<Item> getCabinetChildrenSorted( long cabinetId ){
		List<Item> retval = null;
		Session session = this.getSession();
	
		Query query = session.getNamedQuery("getCabinetChildrenSorted");
		query.setParameter("cabinetId", cabinetId);

		retval = (List<Item>) query.list();
		return retval ;
	}
	
	@Override
	public List<Item> getCabinetChildrenWithoutBladesSorted( long cabinetId ){
		List<Item> retval = null;
		Session session = this.getSession();
	
		Query query = session.getNamedQuery("getCabinetChildrenWithoutBladesSorted");
		query.setParameter("cabinetId", cabinetId);

		retval = (List<Item>) query.list();
	
		return retval;
	}
	
	
	@Override
	public void clearPanelPlacement( List<Long> panelItems ) {
		
		Query q = this.getSession().createSQLQuery("update dct_items set parent_item_id = null, u_position = -9, facing_lks_id = null where item_id in (:itemIds) ");
		
		q.setParameterList("itemIds", panelItems.toArray());
		
		q.executeUpdate();
		
	}
	
	@Override
	public void clearPanelConnections( List<Long> panelItems ) {

		// -- delete all the incoming power connections to the panels
		// delete from dct_connections_power
		// inner join dct_ports_power on dct_connections_power.dest_port_id = dct_ports_power.port_power_id   
		// where dest_port_id in (select port_power_id from dct_ports_power where item_id in (:itemIds)) and 
		// dct_ports_power.subclass_lks_id = 415;
		
		Query q = this.getSession().createSQLQuery(new StringBuffer()
		.append("delete from dct_connections_power where ")
		.append(" dest_port_id in (select port_power_id from dct_ports_power where item_id in (:itemIds)) or ")
		.append(" source_port_id in (select port_power_id from dct_ports_power where item_id in (:itemIds)) ")
		.toString()
	    );
		
		q.setParameterList("itemIds", panelItems.toArray());
		
		q.executeUpdate();
		
	}
	
	@Override
	public void clearPowerOutletPlacement( List<Long> powerOutletItems ) {
		
		Query q = this.getSession().createSQLQuery("update dct_items set parent_item_id = null, u_position = -9, facing_lks_id = null where item_id in (:itemIds) ");
		
		q.setParameterList("itemIds", powerOutletItems.toArray());
		
		q.executeUpdate();
		
	}
	
	@Override
	public void clearPowerOutletAssociationWithPanel(List<Long> powerOutletItems ) {

		/*Query qUpdateConnections = this.getSession().createSQLQuery(new StringBuffer()
		.append("delete from dct_connections_power where source_port_id in ")
		.append(" (select port_power_id from dct_ports_power where item_id in (:itemIds)) ")
		.toString()
	    );
		
		qUpdateConnections.setParameterList("itemIds", powerOutletItems.toArray());*/
		
		if (null == powerOutletItems || powerOutletItems.size() == 0) {
			return;
		}

		Query qUpdatePort = this.getSession().createSQLQuery(new StringBuffer()
		.append("update dct_ports_power set address = null, busway_item_id = null, brkr_port_id = null ")
		.append(" where item_id in (:itemIds) ")
		.toString()
	    );
		
		qUpdatePort.setParameterList("itemIds", powerOutletItems.toArray());

		Query qUpdateItem = this.getSession().createSQLQuery(new StringBuffer()
		.append("update dct_items_me set pdupanel_item_id = null, ups_bank_item_id = null ")
		.append(" where item_id in (:itemIds) ")
		.toString()
	    );
		
		qUpdateItem.setParameterList("itemIds", powerOutletItems.toArray());
		
		/*qUpdateConnections.executeUpdate();*/
		qUpdatePort.executeUpdate();
		qUpdateItem.executeUpdate();
		
		this.getSession().flush();
	}
	
	@Override
	public void setItemState( List<Long> items, Long itemState ) {
		
		// update the Item status
		Query qItem = this.getSession().createSQLQuery("update dct_items set status_lks_id = (:itemState) where item_id in (:itemIds) ");
		
		qItem.setLong("itemState", itemState);
		qItem.setParameterList("itemIds", items.toArray());
		
		qItem.executeUpdate();
		
		// update the port status
		/* Query qPort = this.getSession().createSQLQuery("update dct_ports_power set status_lks_id = (:itemState) where item_id in (:itemIds) ");
		
		qPort.setLong("itemState", itemState);
		qPort.setParameterList("itemIds", items.toArray());
		
		qPort.executeUpdate(); */
	}
	
	@Override
	public void setPortState( List<Long> items, Long itemState ) {
		
		// update the port status
		Query qPort = this.getSession().createSQLQuery("update dct_ports_power set status_lks_id = (:itemState) where item_id in (:itemIds) ");
		
		qPort.setLong("itemState", itemState);
		qPort.setParameterList("itemIds", items.toArray());
		
		qPort.executeUpdate();
	}
	

	@Override
	public List<Long> getChildItemIds(long itemId) {
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items as i where i.parent_item_id = :itemId ")
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id where it.chassis_id = :itemId ")
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_items as chassis on it.chassis_id = chassis.item_id where chassis.parent_item_id = :itemId ")
		.toString()
	    );
		
		q.setLong("itemId", itemId);

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}	

	@Override
	public List<Long> getPanelItemIdsToDelete(long itemId) {
		/**
		 select item_id from dct_items where 
			item_id in (select item_id from dct_ports_power where port_power_id in (select source_port_id from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id = 5084))) and
			class_lks_id = 11 and subclass_lks_id is not null;
		 */
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select item_id from dct_items where ")
		.append(" item_id in (select item_id from dct_ports_power where port_power_id in (select source_port_id from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id = :itemId))) and ")
		.append(" class_lks_id = 11 and subclass_lks_id is not null ")
		.toString()
	    );
		
		q.setLong("itemId", itemId);

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;

	}

	@Override
	public int setPanelUPSBankId (Long fpduId, Long upsBankId ) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("updatePanelUPSBankId");
		query.setLong("newUpsBankId", upsBankId);
		query.setLong("fpduId", fpduId);
		
		return query.executeUpdate();
	}
	
	@Override
	public Long getNumOfChildWithDeltaPhase(Long parentItemId) {

		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select count(panel.phase_lks_id) from dct_items_me panel where  ")
		.append(" panel.item_id in (select item_id from dct_items where parent_item_id = :parentItemId ) and ")
		.append(" panel.phase_lks_id = 72 ")
		.toString()
	    );
		
		q.setLong("parentItemId", parentItemId);
		
		BigInteger childCount = (BigInteger) q.uniqueResult();
		
		return childCount.longValue();
		
	}

	@Override
	public Long getNumOfChildren(Item parentItem) {
		
		Criteria c = getChildrenItemCriteria(parentItem);
		c.setProjection(Projections.rowCount());
		Long itemCount = (Long)c.uniqueResult();
		
		return itemCount;
	}
	
	@Override
	public List<Long> getPanelItemIdsToDelete(List<Long> itemIds) {
		/**
		 select item_id from dct_items where 
			item_id in (select item_id from dct_ports_power where port_power_id in (select source_port_id from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id = 5084))) and
			class_lks_id = 11 and subclass_lks_id is not null;
		 */
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select item_id from dct_items where ")
		.append(" item_id in (select item_id from dct_ports_power where port_power_id in (select source_port_id from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id in (:itemIds) ))) and ")
		.append(" class_lks_id = 11 and subclass_lks_id is not null ")
		.toString()
	    );
		
		q.setParameterList("itemIds", itemIds.toArray());

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;

	}

	@Override
	public List getItemIdsToDelete(List itemIds) {
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items as i where i.parent_item_id in (:itemIds) ")
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id where it.chassis_id in (:itemIds) ")
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_items as chassis on it.chassis_id = chassis.item_id where chassis.parent_item_id in (:itemIds) ")
		.toString()
	    );
		
		q.setParameterList("itemIds", itemIds);

		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}	

	@SuppressWarnings("unchecked")
	public Map<String, Object> getItemLayout (long itemId) {
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.alias(Projections.property("layoutVerticalLeft"), "verticalLeft"))
				.add(Projections.alias(Projections.property("layoutVerticalLeftBack"), "verticalLeftBack"))
				.add(Projections.alias(Projections.property("layoutVerticalRight"), "verticalRight"))
				.add(Projections.alias(Projections.property("layoutVerticalRightBack"), "verticalRightBack"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return (Map<String, Object>) criteria.uniqueResult();
	}

	@Override
	public void switchSiblingItemId(long oldSiblingId, long newSiblingId) {
		Session session = getSession();
		
		Query q = session.createSQLQuery("update dct_items set sibling_item_id = :newSiblingId where sibling_item_id = :oldSiblingId");
		q.setLong("newSiblingId", newSiblingId);
		q.setLong("oldSiblingId", oldSiblingId);
		int ret = q.executeUpdate();
		
	}

	@Override
	public void setUPositon(long itemId, long uPosition) {
		Session session = getSession();
		
		Query q = session.createSQLQuery("update dct_items set u_position = :uPosition where item_id = :itemId");
		q.setLong("uPosition", uPosition);
		q.setLong("itemId", itemId);
		q.executeUpdate();
	}

	@Override
	public void setOrientation(long itemId, long facingLksId) {
		Session session = getSession();
		
		Query q = session.createSQLQuery("update dct_items set facing_lks_id = :facingLksId where item_id = :itemId");
		q.setLong("facingLksId", facingLksId);
		q.setLong("itemId", itemId);
		q.executeUpdate();
		
	}
	
	@Override
	public Long getFreeStandingItemIdForItem(Long itemId){
		Session session = this.getSession();
		
		//call name query
		Query query = session.getNamedQuery("getFreeStandingItemIdForItem");
		query.setParameter("itemId", itemId);
		
		for (Object rec:query.list()) {
			return (Long)rec;
		}
		
		return null;
	}

	@Override
	public String getItemsPiqHost(long itemId) {
        Query query = this.getSession().getNamedQuery("getItemsPiqHost");
        query.setLong("itemId", itemId);
		return (String) query.uniqueResult();
	}

	@Override
	public int getAssociatedCircuitsCountForItem(long itemId){
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getAssociatedCircuitsForItem");
    	query.setLong("itemId", itemId);

    	return query.list().size();
    	
	}

	@Override
	public int getNumOfAssociatedNonPlannedNonRequestCircuitsForItem(long itemId) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getAssociatedNonPlannedNonRequestCircuitsForItem");
    	query.setLong("itemId", itemId);

    	return query.list().size();
    	
	}
	
	// 
	public int getNumOfAssociatedNonPlannedForItem(long itemId) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getAssociatedNonPlannedCircuitsForItem");
    	query.setLong("itemId", itemId);

    	return query.list().size();
    	
	}

	@Override
	public List<Long> getAssociatedNonPlannedNonRequestCircuitsForItem(long itemId) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

		 /*
		    <return-scalar column="circuit_id" type="long"/>
		    <return-scalar column="trace_len" type="long"/>
		    <return-scalar column="circuit_type" type="long"/>
		    */

    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getAssociatedNonPlannedNonRequestCircuitsForItem");
    	query.setLong("itemId", itemId);

    	return query.list();
    	
	}

	
	@Override
	public List<CircuitCriteriaDTO> getAssociatedCircuitsForItem(long itemId) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

		 /*
		    <return-scalar column="circuit_id" type="long"/>
		    <return-scalar column="trace_len" type="long"/>
		    <return-scalar column="circuit_type" type="long"/>
		    */

    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getAssociatedCircuitsForItem");
    	query.setLong("itemId", itemId);

    	CircuitCriteriaDTO cCriteria;

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[2];

			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));

			recList.add(cCriteria);
		}

	    return recList;
	}
	
	private List<CircuitCriteriaDTO> getCircuitCriteriaDTOs(long itemId, String namedQuery) {
		
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

		 /*
		    <return-scalar column="circuit_id" type="long"/>
		    <return-scalar column="trace_len" type="long"/>
		    <return-scalar column="circuit_type" type="long"/>
	    */

	   	Session session = this.getSession();
	   	Query query =  session.getNamedQuery(namedQuery);
	   	query.setLong("itemId", itemId);
	
	   	CircuitCriteriaDTO cCriteria;

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[2];

			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));

			recList.add(cCriteria);
		}

	    return recList;
	} 
	
	@Override
	public List<CircuitCriteriaDTO> getBladeNonLogicalCircuits(long itemId) {
		
		return getCircuitCriteriaDTOs(itemId, "getBladeNonLogicalCircuits");
		
	}

	
	@Override
	public List<CircuitCriteriaDTO> getBladeNonRequestLogicalCircuits(long itemId) {
		
		return getCircuitCriteriaDTOs(itemId, "getBladeNonRequestLogicalCircuits");
		
	}
	
	@Override
	public List<Long> getBladeNonApprovedLogicalCircuitsRequest(Long itemId) {
    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getBladeNonApprovedLogicalCircuitsRequest");
    	query.setLong("itemId", itemId);
    	
    	return query.list();
	}

	
	// getAssociatedCircuitsForItems
	// itemIdList
	@Override
	public List<CircuitCriteriaDTO> getAssociatedCircuitsForItems(List<Long> itemIdList){
		
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

		 /*
		    <return-scalar column="circuit_id" type="long"/>
		    <return-scalar column="trace_len" type="long"/>
		    <return-scalar column="circuit_type" type="long"/>
		    */

    	Session session = this.getSession();
    	Query query =  session.getNamedQuery("getAssociatedCircuitsForItems");
    	query.setParameterList ("itemIdList", itemIdList.toArray());

    	CircuitCriteriaDTO cCriteria;

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[2];

			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));

			recList.add(cCriteria);
		}

	    return recList;
	}


    @Override
    public long savePassiveItem(Item item) {
        if (item.getItemId() > 0) {
            return updatePassiveItem(item);
        } else {
            return insertPassiveItem(item);
        }
    }

    /**
     * Insert a new passive item.
     * 
     * @param item
     *          Item domain object
     * @return item id
     */
    private long insertPassiveItem(Item item) {
        return (Long) getSession().save(item);
    }

    /**
     * Update an existing passive item.
     * 
     * @param item
     *          Item domain object
     * @return item id
     */
    private long updatePassiveItem(Item item) {
        Session session = this.getSession();

        // Update Item
        Query query = session.createQuery("UPDATE Item SET "
            + "itemName = :itemName, "
            + "uPosition = :uPosition, "
            + "mountedRailLookup.lksId = (SELECT lksId FROM LksData WHERE lkpValueCode = :railsUsedId), "
            + "subclassLookup.lksId = :subClassId, "
            + "model.modelDetailId = :modelId "
            + "WHERE itemId = :itemId");

        query.setLong("itemId", item.getItemId());
        query.setString("itemName", item.getItemName());
        query.setLong("uPosition", item.getuPosition());
        query.setLong("railsUsedId", item.getMountedRailLookup().getLkpValueCode());
        query.setLong("subClassId", item.getSubclassLookup().getLksId());
        query.setLong("modelId", item.getModel().getModelDetailId());

        query.executeUpdate();

        // Update sysUpdateDate in ItemDetails
        Query queryItemDetail = session.createQuery("UPDATE ItemServiceDetails SET sysUpdateDate = :sysUpdateDate "
            + "WHERE itemServiceDetailId IN (SELECT itemServiceDetails.itemServiceDetailId FROM Item WHERE itemId = :itemId)");

        queryItemDetail.setLong("itemId", item.getItemId());
        queryItemDetail.setTimestamp("sysUpdateDate", new Date(Calendar.getInstance().getTimeInMillis()));

        queryItemDetail.executeUpdate();

        return item.getItemId();
    }

    @Override
    public void deletePassiveItem(Long itemId) {
        Session session = this.getSession();

        // Delete Item
        Query query = session.createQuery("DELETE Item WHERE itemId = :itemId");
        query.setLong("itemId", itemId);

        query.executeUpdate();

        // Delete ItemDetail
        Query queryItemDetail = session.createQuery("DELETE ItemServiceDetails " +
            "WHERE itemServiceDetailId IN (SELECT itemServiceDetails.itemServiceDetailId FROM Item WHERE itemId = :itemId)");
        queryItemDetail.setLong("itemId", itemId);

        queryItemDetail.executeUpdate();
    }

	@Override
	public boolean getItemsPIQIntegrationStatus(long itemId) {
		Query query = this.getSession().getNamedQuery("getItemsPiqIntegrationStatus");
        query.setLong("itemId", itemId);
		String result = (String)query.uniqueResult();
		return (result != null && result.equals("true")) ? true : false; 
	}
	
	@Override
	public Map<String, Object> getFieldsValue(Long itemId, Map<String, String> dbToAliasField) {

		if (null == itemId || null == dbToAliasField || dbToAliasField.size() == 0) return new HashMap<String, Object>();
		
		StringBuilder queryStr = new StringBuilder(" select ");
		int mapSize = dbToAliasField.size();
		int count = 0;
		for(Map.Entry<String, String> entry: dbToAliasField.entrySet()) {

			String dbField = entry.getKey();
			String alias = entry.getValue();
			
			StringBuilder subQueryStr = new StringBuilder(dbField);
			subQueryStr.append(" as ");
			subQueryStr.append("\"");
			subQueryStr.append(alias);
			subQueryStr.append("\"");
			
			count++;
			if (count != mapSize) {
				subQueryStr.append(" , ");
			}
			
			queryStr.append(subQueryStr);
		}
		
		queryStr.append(" from dct_items ");
		queryStr.append(" left join dct_item_details on dct_items.item_detail_id = dct_item_details.item_detail_id ");
		queryStr.append(" left join dct_lks_data statusLks on dct_items.status_lks_id = statusLks.lks_id ");
		queryStr.append(" left join dct_lks_data facingLks on dct_items.facing_lks_id = facingLks.lks_id ");
		queryStr.append(" left join dct_lks_data mountedRailsLks on dct_items.mounted_rails_pos_lks_id = mountedRailsLks.lks_id ");
		queryStr.append(" left join dct_items_cabinet on dct_items_cabinet.item_id = dct_items.item_id ");
		queryStr.append(" left join dct_items_it on dct_items_it.item_id = dct_items.item_id ");
		queryStr.append(" left join dct_models on dct_models.model_id = dct_items.model_id ");
		queryStr.append(" where dct_items.item_id = :itemId ");
		
		Query query = this.getSession().createSQLQuery(queryStr.toString());
		query.setParameter("itemId", itemId);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		
		Map<String, Object> valueMap = (Map<String, Object>) query.uniqueResult();
		
		return valueMap;
		
	}
	
	@Override
	public List<Long> getIpAddressId(Long itemId) {
		
		Session session = this.getSession();
		
		Query qIPAddressList = session.createSQLQuery(new StringBuffer()
			.append("select distinct ipaddressid from tblipteaming where portid in (select port_data_id from dct_ports_data where item_id = :itemId) ")
			.append("and ipaddressid not in (select distinct ipaddressid from tblipteaming where portid not in (select port_data_id from dct_ports_data where item_id = :itemId)) ")
			.toString()
				);

		qIPAddressList.setLong("itemId", itemId);
		@SuppressWarnings("unchecked")
		List<Long> ipAddressList = (List<Long>) qIPAddressList.list();

		return ipAddressList;
		
	}
	
	@Override
	public void deleteItemIPAddressAndTeaming(long itemId) {

		Session session = this.getSession();

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


	@Override
	public void propagateParentLocationToChildren(long parentItemId, long locationId) {
		Query q = this.getSession().createSQLQuery("update dct_items set location_id=:locationId where parent_item_id=:parentItemId");
        q.setLong("locationId", locationId);
        q.setLong("parentItemId", parentItemId);
        q.executeUpdate();
	}

	@Override
	public void propagateParentStatusToPassiveChildren(long parentItemId, long statusLksId) {
		
		StringBuffer queryBuffer = new StringBuffer()
			.append("update dct_items set status_lks_id=:statusLksId ")
			.append(" where parent_item_id=:parentItemId ")
			.append(" and class_lks_id = 30 ");
		
		Query q = this.getSession().createSQLQuery(queryBuffer.toString());
        q.setLong("statusLksId", statusLksId);
        q.setLong("parentItemId", parentItemId);
        q.executeUpdate();
	}

	@Override
	public Integer getEffectiveBudgetedWattsForAnItem(long itemId) {
	   	Session session = this.getSession();
	   	Query query =  session.getNamedQuery("effectiveBudgetedWatts");
	   	query.setLong("itemId", itemId);
	   	return (Integer)query.uniqueResult();
	}

	@Override
	public void deleteReservations(long cabinetItemId) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("delete from dct_reservations where reservation_id")
				.append(" in (select reservation_id from dct_reservation_details")
				.append(" where parent_item_id = :itemId)");
		Session session = this.getSession();
		SQLQuery query = session.createSQLQuery(queryStr.toString());
		query.setLong("itemId", cabinetItemId);
		query.executeUpdate();
	}

	@Override
	public Long getItemByLocationAndName(String locationCode, String itemName) {
		if (locationCode != null && !locationCode.isEmpty() && 
				itemName != null && !itemName.isEmpty()) {
			Session session = this.getSession();
			Query query = session.getNamedQuery("getItemIdByLocationAnditemName");
			query.setString("itemName", itemName.toUpperCase());
			query.setString("locationCode", locationCode.toUpperCase());
			List<Long> result = query.list();
			return (result != null && result.size() == 1) ? result.get(0) : null;
		} 
		return null;
	}

	@Override
	public void unmapItemWithPIQ(List<Long> associatedItemIds) {
		Session session = this.getSession();
		
		Query query = session.getNamedQuery("clearPiqAssociation");
		query.setParameterList("itemIdList", associatedItemIds.toArray());
		
		query.executeUpdate();
		
	}

	@Override
    public Long getNumOfNonPlannedItems(Long locationId) {
        Session session = this.getSession();     
        Query query = session.getNamedQuery("getNumOfNonPlannedItems");
        query.setLong("locationId", locationId);

        return ((BigInteger) query.uniqueResult()).longValue();
    }
	
	@Override
	public boolean doesItemWithNameExistsAtLocation( String itemName, Long locationId) {
        Session session = this.getSession();
        Query query = session.getNamedQuery("getItemsCountWithNameAtLocation");
        query.setString("itemName",  itemName);
        query.setLong("locationId", locationId);

        int result = ((BigInteger) query.uniqueResult()).intValue();
        return (result > 0) ? true : false;
	}
	
	private Long resolveUPosition(String uPosition) {
		Long uPositionLongValue = null;
		try {
			Integer x = Integer.parseInt(uPosition);
			uPositionLongValue = x.longValue();	
		} catch (NumberFormatException e) {
			if (uPosition.toLowerCase().equals("above")) {
				uPositionLongValue = 	SystemLookup.SpecialUPositions.ABOVE;
			} else if (uPosition.toLowerCase().equals("below")) {
				uPositionLongValue = 	SystemLookup.SpecialUPositions.BELOW;
			}
		}
		
		return uPositionLongValue;
	}
	
	private Long getItemIdByLocationNameAndPlacement(String locationCode, String itemName, String cabinetName, String uPosition, String mountedRails) {
		if (locationCode != null && !locationCode.isEmpty() && 
				itemName != null && !itemName.isEmpty() &&
				cabinetName != null && !cabinetName.isEmpty() &&
				uPosition != null && !uPosition.isEmpty() &&
				mountedRails !=null && !mountedRails.isEmpty()) {
			Long uPositionLong = resolveUPosition(uPosition);
			Session session = this.getSession();
			Criteria c = session.createCriteria(Item.class);
			c.createAlias("parentItem", "parent");
			c.createAlias("dataCenterLocation", "location");
			c.createAlias("mountedRailLookup", "railsUsed");
			c.add(Restrictions.ilike("itemName", itemName));
			c.add(Restrictions.eq("location.code", locationCode.toUpperCase())); 
			c.add(Restrictions.eq("parent.itemName", cabinetName.toUpperCase()));
			c.add(Restrictions.eq("railsUsed.lkpValue", mountedRails));
			c.add(Restrictions.eq("uPosition", uPositionLong));
			projectItemId(c);
			return (Long) c.uniqueResult();
		}
		return null;
	}
	
	/**
	 * Fetch item based on the available parameters. The passive items may have same name. It can be 
	 * identified based on cabinet and u-position it is placed.
	 * The first 2 parameters are required. The cabinet Name and the uPosition is optional. 
	 * @param locationCode
	 * @param itemName
	 * @param cabinetName
	 * @param uPosition
	 * @return
	 */
	@Override
	public Long getUniqueItemId(String locationCode, String itemName, String cabinetName, String uPosition, String mountedRails ) {
		Long result = null;
		result = getItemIdByLocationNameAndPlacement (locationCode, itemName, cabinetName, uPosition, mountedRails);
		if (result == null) {
			// find by locationCode and itemName
			result = getItemByLocationAndName (locationCode, itemName);
		}
		return result;
	}
	
	@Override
	public boolean doesItemWithNameExistsAboveOrBelowCabinet(String itemName, Long parentItemId, Long uPosition) {
		Long result = 0L;
		if (itemName != null && !itemName.isEmpty() &&
				uPosition != null && uPosition < 0 &&
						parentItemId !=null && parentItemId > 0) {
			Session session = this.getSession();
			Criteria c = session.createCriteria(Item.class);
			c.createAlias("parentItem", "parent");
			c.add(Restrictions.eq("itemName", itemName.toUpperCase()));
			c.add(Restrictions.eq("parent.itemId", parentItemId));
			c.add(Restrictions.eq("uPosition", uPosition));
			result = (Long)c.setProjection(Projections.rowCount()).uniqueResult();
		}
		return (result > 0);
	}
	
	@Override
	public boolean isCabinetChanged (Long itemId, Long cabinetId) {
		if (itemId == null || cabinetId == null) return false;
		Session session = this.getSession();
		Query query = session.getNamedQuery("getParentItemIdForItem");
		query.setLong("itemId", itemId);
		BigInteger parent = (BigInteger)query.uniqueResult();
		return (parent != null && parent.longValue() != cabinetId.longValue()) ? true : false;
	}

	@Override
	public boolean isLocationChanged (Long itemId, Long currentLocationId) {
		if (itemId == null || currentLocationId == null) return false;
		Session session = this.getSession();
		Query query = session.getNamedQuery("getLocationIdForItem");
		query.setLong("itemId", itemId);
		BigInteger locationId = (BigInteger)query.uniqueResult();
		return (locationId != null && locationId.longValue() != currentLocationId.longValue()) ? true : false;
	}

	public int removePrimaryStackItem(Long primaryItemId) {
		
		Session session = this.getSession();
		/**
		 * if the item getting removed is the primary item in the sibling, update all other items
		 * that are refenced to this item. Update the sibling_item_id and grouping_name
		 */
		Query q = session.createSQLQuery(new StringBuffer()
		.append(" update dct_items as siblings set sibling_item_id = t.item_id, grouping_name = t.item_name from  ")
		.append(" ( 	select item_id, item_name from dct_items 	where sibling_item_id = :itemId order by num_ports, item_id limit 1 offset 1 	) t ")
		.append(" WHERE siblings.sibling_item_id = :itemId AND siblings.item_id != :itemId ")
		.toString()
	    );

		q.setLong("itemId", primaryItemId);
		
		return q.executeUpdate();
		
	}

	public boolean isPassiveItem(Long itemId) {
		if (itemId == null || itemId <= 0 ) return false;
		Session session = this.getSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("classLookup", "classLookup");
		c.add(Restrictions.eq("itemId", itemId));
		c.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.PASSIVE));
		Long result = (Long)c.setProjection(Projections.rowCount()).uniqueResult();
		return (result > 0);
	}
	
}
