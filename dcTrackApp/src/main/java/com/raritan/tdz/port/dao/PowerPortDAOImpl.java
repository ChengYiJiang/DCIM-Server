package com.raritan.tdz.port.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dto.BreakerDTO;
import com.raritan.tdz.lookup.SystemLookup;

public class PowerPortDAOImpl extends DaoImpl<PowerPort> implements PowerPortDAO {

	PowerPortDAOImpl(){
	}
	
	@Override
	public PowerPort loadPort(Long portId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setFetchMode("item", FetchMode.JOIN);
		criteria.setFetchMode("portSubClassLookup", FetchMode.JOIN);
		criteria.setFetchMode("connectorLookup", FetchMode.JOIN);
		criteria.setFetchMode("phaseLookup", FetchMode.JOIN);
		criteria.setFetchMode("voltsLookup", FetchMode.JOIN);
		criteria.add(Restrictions.eq("portId", portId));
		
		PowerPort port = (PowerPort)criteria.uniqueResult();
		
		return port;	
	}

	@Override
	public PowerPort loadEvictedPort(Long portId) {
		PowerPort port = loadPort(portId);
		this.getSession().evict(port);
		
		return port;	
	}

	@Override
	public Object loadPortField(Long portId, String field){
		Criteria c = this.getSession().createCriteria(this.type);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property(field)/*, "fieldName"*/);		
		c.setProjection(proList);		
		
		c.add(Restrictions.eq("portId", portId));
		
		return c.uniqueResult();
		
	}


	
	@Override
	public boolean isPortUsed(Long portId){		
		boolean ret = false;
		Criteria criteria;
		
		criteria = this.getSession().createCriteria(this.type);
		criteria.add(Restrictions.eq("portId", portId));
		criteria.add(Restrictions.eq("used", true));
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		
		List list = criteria.list();
					
		if(list != null && list.size() > 0)
		{
			ret = true;
		}
			
		return ret;
	}

	@Override
	public List<PowerPort> getPortsForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}

	@Override
	public List<String> getPortsNameForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.addOrder(Order.asc("portName"));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("portName"), "portName");
		criteria.setProjection(proList);

		
		return criteria.list();
	}

	@Override
	public Long getNumOfPortForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.setProjection(Projections.rowCount());

		
		return (Long) criteria.uniqueResult();
	}

	
	@Override
	public List<PowerPort> getFreePortsForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.add(Restrictions.eq("used", false));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}	
	

	@Override
	public List<PowerPort> getPortsFromPortIdList(List<Long>portList) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );	
		criteria.add(Restrictions.in("portId", portList));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}
	
	@Override
	public List<String> getItemNamesWithConnectedPorts(List<Long> itemIds){
		Criteria c = this.getSession().createCriteria(this.type);
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
		
		List<String> list = new ArrayList<String>(recList);
		
		java.util.Collections.sort(list);
		
		return list;
	}

	private String getBreakerUIName(BreakerDTO b) {
		// update the output wiring
		StringBuilder uiBreakerName = new StringBuilder();
		uiBreakerName.append(b.getFpduName()); 
		uiBreakerName.append("/");
		uiBreakerName.append(b.getPowerPanelName());
		uiBreakerName.append(":");
		uiBreakerName.append(b.getBreakerName());

		return uiBreakerName.toString();
	}

	private String getRatingUIName(BreakerDTO b) {
		// update the output wiring
		StringBuilder uiRatingName = new StringBuilder();
		uiRatingName.append(b.getAmpsNameplate().longValue()); 
		uiRatingName.append("A, ");
		uiRatingName.append(b.getLineVolts());
		uiRatingName.append("V");
		return uiRatingName.toString();
	}

	private String getOutputWiringDesc(BreakerDTO b) {
		// update the output wiring
		String uiOutptuWiringName = "";
		
		if( b.getOutputWiringLkpValueCode() == SystemLookup.PhaseIdClass.THREE_DELTA ){
			uiOutptuWiringName = "3-Wire + Ground";
		}else if( b.getOutputWiringLkpValueCode() == SystemLookup.PhaseIdClass.THREE_WYE ){
			uiOutptuWiringName = "4-Wire + Ground";
		} else {
			uiOutptuWiringName = b.getOutputWiringDesc();
		}
		
		return uiOutptuWiringName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<BreakerDTO> getBreakers(Long ampsRating, Boolean[] isUsed,
			Long[] breakerLkpValueCodes, Long[] phases, Long breakerPortId, Long fpduItemId) {
		
		Session session = this.getSession();
		// user has not specified rating return all breaker port whose rating >= 0 Amps	
		if (ampsRating == null) {
			ampsRating = new Long (0);
		}
		
		List<Long> panelSubClassList = new ArrayList<Long>(	Arrays.asList(
						SystemLookup.SubClass.LOCAL, 
						SystemLookup.SubClass.REMOTE) );
		
		// named query to get breaker ports.
		Query query = session.getNamedQuery("getBreakerPorts");
		query.setParameterList("panelSubClassList", panelSubClassList);
		query.setParameterList("branchCircuitBreaker", breakerLkpValueCodes);
		query.setParameterList("isUsed", isUsed);
		query.setParameterList("phaseLookupList", phases);
		query.setParameter("ampsRating", ampsRating.doubleValue());
		query.setParameter("breakerPortId", breakerPortId);
		query.setParameter("fpduItemId",fpduItemId);
		
		// map the query result to BreakerDTO
		query.setResultTransformer(Transformers.aliasToBean(BreakerDTO.class));

		List<BreakerDTO> breakerDtoList = (List<BreakerDTO>) query.list();
		
		// update DTO fields that are composed of concatenating one or more data  
		for (BreakerDTO b : breakerDtoList) {
			// update the output wiring
			b.setUiBreakerColumn(getBreakerUIName(b));
			b.setUiRatingColumn(getRatingUIName(b));
			b.setOutputWiringDesc(getOutputWiringDesc(b));
		}
		
		return breakerDtoList;
	}
	
    public Item getFPDUItemForBreakerPortId(Long breakerPortId) {

        Item fpduItem = null;
        if (breakerPortId == null || breakerPortId == 0) {
            return fpduItem;
        }

        // named query to get breaker ports.
        Session session = this.getSession();
        Query query = session.getNamedQuery("getFPDUItemForBreakerPortId");
        query.setParameter("breakerPortId", breakerPortId);

        List<Item> itemList = query.list();
        
        if (itemList != null && itemList.size() > 0) {
        	fpduItem = (Item) query.list().get(0);
        }

        return fpduItem;
    }
	
	@Override
	public PowerPort getPortWithConnections(Long id) {
		Object result = null;
		Session session = null;
		
		session = this.getSession();
		Criteria criteria = session.createCriteria(PowerPort.class);
		criteria.add(Restrictions.eq("portId", id.longValue()));
		criteria.createAlias("sourcePowerConnections", "sourcePowerConn", Criteria.LEFT_JOIN);
		criteria.createAlias("destPowerConnections", "destPowerConn", Criteria.LEFT_JOIN);
		
		result = criteria.uniqueResult(); 
		
		return (PowerPort)result;
	}

	@Override
	public PowerPort getPortWithSourceConnections(Long id) {
		Object result = null;
		Session session = null;
		
		session = this.getSession();
		Criteria criteria = session.createCriteria(PowerPort.class);
		criteria.add(Restrictions.eq("portId", id.longValue()));
		criteria.createAlias("sourcePowerConnections", "sourcePowerConn", Criteria.LEFT_JOIN);
		
		result = criteria.uniqueResult(); 
		
		return (PowerPort)result;
	}
	
	private Criteria getCriteria(Long itemId, Long portSubClass) {
		Session session = null;
		
		session = this.getSession();
		Criteria c = session.createCriteria(PowerPort.class);
		c.createAlias("item", "item");
		c.createAlias("portSubClassLookup", "portSubClassLookup");
		c.createAlias("destPowerConnections", "destPowerConn", Criteria.LEFT_JOIN);
		c.add(Restrictions.eq("item.itemId", itemId));
		c.add(Restrictions.eq("portSubClassLookup.lkpValueCode", portSubClass.longValue()));	
		
		return c;
	}
	
	@Override
	public PowerPort getPort(Long itemId, Long portSubClass) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		PowerPort port = (PowerPort) c.uniqueResult();
		
		return port;
	}
	
	@Override
	public List<PowerPort> getPorts(Long portSubClass) {
		Session session = null;
		
		session = this.getSession();
		Criteria c = session.createCriteria(PowerPort.class);
		c.createAlias("portSubClassLookup", "portSubClassLookup");
		c.setFetchMode("item", FetchMode.JOIN);
		c.add(Restrictions.eq("portSubClassLookup.lkpValueCode", portSubClass.longValue()));

		@SuppressWarnings("unchecked")
		List<PowerPort> ports = c.list();
		
		return ports;
		
	}

	@Override
	public List<PowerPort> getPorts(List<Long> portSubClass) {
		Session session = null;
		
		session = this.getSession();
		Criteria c = session.createCriteria(PowerPort.class);
		c.createAlias("portSubClassLookup", "portSubClassLookup");
		c.setFetchMode("item", FetchMode.JOIN);
		c.add(Restrictions.in("portSubClassLookup.lkpValueCode", portSubClass));

		@SuppressWarnings("unchecked")
		List<PowerPort> ports = c.list();
		
		return ports;
		
	}

	
	@Override
	public void initPowerPortsAndConnectionsProxy( PowerPort p ) {
		if( p == null ) return;
		Hibernate.initialize(p.getSourcePowerConnections());
		Hibernate.initialize(p.getDestPowerConnections());
		Hibernate.initialize(p.getItem());
	}
	
	@Override
	public void changeUsedFlag(Long portId, Long oldConnectedPortId) {

		if (null == portId) {
			return;
		}
		
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("update dct_ports_power set is_used = case ")
		.append("when (select count(*) from dct_connections_power where dest_port_id = :portId and source_port_id <> :oldConnectedPortId) = 0 THEN false ")
		.append("else true end  ")
		.append("where port_power_id = :portId ")
		.toString()
	    );
		
		q.setLong("portId", portId);
		q.setLong("oldConnectedPortId", (null != oldConnectedPortId) ? oldConnectedPortId : -1L);

		q.executeUpdate();
		
	}

	@Override
	public void clearBrkrPortReference(Long portId) {

		if (null == portId) {
			return;
		}
		
		Session session = this.getSession();
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("update dct_ports_power set brkr_port_id = null ")
		.append("where brkr_port_id = :portId ")
		.toString()
	    );
		
		q.setLong("portId", portId);

		q.executeUpdate();
		
	}
	
	@Override
	public void runPowerPortDiagnostics() {

		Session session = this.getSession();
		
		Query queryPPV = session.getNamedQuery("dcDiagnosisPortPowerValues");
		
		Query queryIPPV = session.getNamedQuery("dcDiagnosisItemPortPowerValues");
		
		Query queryPPL = session.getNamedQuery("dcDiagnosisPortPowerLoad");
		
		Query queryPPC = session.getNamedQuery("dcDiagnosisPortPowerConnection");
		
		queryPPV.uniqueResult();
		queryIPPV.uniqueResult();
		queryPPL.uniqueResult();
		queryPPC.uniqueResult();
		
	}

	@Override
	public void resetPIQId(Item item) {
		Session session = this.getSession();
		
		String queryString = "update PowerPort pp set pp.piqId = :piqId where item = :item";
		Query query = session.createQuery(queryString);
		query.setParameter("piqId", null);
		query.setParameter("item", item);
		query.executeUpdate();
	}

	@Override
	public PowerPort getPort(Long itemId, Long portSubClass, String portName) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.ilike("portName", portName));
		
		PowerPort port = (PowerPort) c.uniqueResult();
		
		return port;
	}
	
	private void projectPortId(Criteria c) {
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("portId"), "portId");
		c.setProjection(proList);
		
	}
	
	@Transactional
	@Override
	public Long getPortId(Long itemId, Long portSubClass, String portName) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.ilike("portName", portName));
		
		projectPortId(c);
		
		return (Long) c.uniqueResult();
	}


	@Override
	public PowerPort getPort(Long itemId, Long portSubClass, int sortOrder) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.eq("sortOrder", sortOrder));
		
		PowerPort port = (PowerPort) c.uniqueResult();
		
		return port;
	}

	private Criteria getCriteria(String location, String itemName, String portName) {
		
		Criteria c = this.getSession().createCriteria(this.type);
		c.createAlias("item", "item");
		c.add(Restrictions.ilike("item.itemName", itemName));
		c.createAlias("item.dataCenterLocation", "dataCenterLocation");
		c.add(Restrictions.ilike("dataCenterLocation.code", location)); 

		c.add(Restrictions.ilike("portName", portName));
		
		return c;
	}
	
	@Override
	public Long getPortId(String location, String itemName, String portName) {
		
		if (null == location || null == itemName || null == portName) return null;
		
		Criteria c = getCriteria(location, itemName, portName);
		
		projectPortId(c);
		
		return (Long) c.uniqueResult();
		
	}

	@Override
	public PowerPort getPort(String location, String itemName, String portName) {
		
		Criteria c = getCriteria(location, itemName, portName);
		
		return (PowerPort) c.uniqueResult();
		
	}
}
