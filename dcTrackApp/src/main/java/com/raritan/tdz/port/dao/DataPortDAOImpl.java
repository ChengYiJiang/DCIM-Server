package com.raritan.tdz.port.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;

public class DataPortDAOImpl extends DaoImpl<DataPort> implements DataPortDAO {

	DataPortDAOImpl(){
	}
	
	@Override
	public DataPort loadPort(Long portId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setFetchMode("item", FetchMode.JOIN);
		criteria.setFetchMode("portSubClassLookup", FetchMode.JOIN);
		criteria.setFetchMode("connectorLookup", FetchMode.JOIN);
		criteria.setFetchMode("mediaId", FetchMode.JOIN);
		criteria.setFetchMode("speedId", FetchMode.JOIN);
		criteria.setFetchMode("protocolID", FetchMode.JOIN);
		criteria.add(Restrictions.eq("portId", portId));
		
		DataPort port = (DataPort)criteria.uniqueResult();
		
		return port;	
	}

	@Override
	public DataPort loadEvictedPort(Long portId) {
		if (null == portId) return null;
		DataPort port = loadPort(portId);
		if (null == port) return null;
		this.getSession().evict(port);
		
		return port;	
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
	public List<DataPort> getPortsForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}
	
	@Override
	public List<DataPort> getFreePortsForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.add(Restrictions.eq("used", false));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}	
	

	@Override
	public List<DataPort> getPortsFromPortIdList(List<Long>portList) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );	
		criteria.add(Restrictions.in("portId", portList));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}
	
	@Override
	public List<DataPort> getPortByName(String portName, Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );	
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.add(Restrictions.eq("portName", portName));
		criteria.addOrder(Order.asc("portId"));

		return criteria.list();
	}
	
	@Override
	public DataPort getFirstPort(Long itemId) {
		DataPort retVal = null;
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );	
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.addOrder(Order.asc("sortOrder"));
		criteria.setMaxResults(1);
		List<DataPort> list = criteria.list();
		if( list.size() > 0 ) retVal = list.get(0);
		return retVal;
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
	
	@Override
	public void deletePortIPAddressAndTeaming(long portId) {

		Session session = this.getSession();
		
		Query qIPAddressDeleteList = session.createSQLQuery(new StringBuffer()
			.append("(select distinct ipaddressid from tblipteaming where portid = :portId ")
			.append("and ipaddressid not in (select ipaddressid from tblipteaming where portid <> :portId)) ")
			.toString()
		);

		qIPAddressDeleteList.setLong("portId", portId);
		@SuppressWarnings("unchecked")
		List<Long> ipAddressDeleteList = (List<Long>) qIPAddressDeleteList.list();

		Query qDeleteTeaming = session.createSQLQuery(new StringBuffer()
			.append("delete from tblipteaming where portid = :portId ") 
			.toString()
		);

		qDeleteTeaming.setLong("portId", portId);
		qDeleteTeaming.executeUpdate();
		
		if (ipAddressDeleteList != null && ipAddressDeleteList.size() > 0) {
			Query qDeleteIpAddresses = session.createSQLQuery(new StringBuffer()
				.append("delete from tblipaddresses where id in (:ipAddressList) ") 
				.toString()
			);
			
			qDeleteIpAddresses.setParameterList("ipAddressList", ipAddressDeleteList.toArray());
			qDeleteIpAddresses.executeUpdate();
		}
		
	}

	private void projectPortId(Criteria c) {
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("portId"), "portId");
		c.setProjection(proList);
		
	}
	
	private Criteria getCriteria(Long itemId, Long portSubClass) {
		Session session = null;
		
		session = this.getSession();
		Criteria c = session.createCriteria(DataPort.class);
		c.createAlias("item", "item");
		c.createAlias("portSubClassLookup", "portSubClassLookup");
		c.add(Restrictions.eq("item.itemId", itemId));
		if( portSubClass != null ){
			c.add(Restrictions.eq("portSubClassLookup.lkpValueCode", portSubClass.longValue()));
		}
		
		return c;
	}
	
	@Transactional
	@Override
	public Long getPortId(Long itemId, Long portSubClass, String portName) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.ilike("portName", portName));
		
		projectPortId(c);
		
		return (Long) c.uniqueResult();
		
	}
	
	/**
	 * 
	 * @param item
	 * @param portName
	 * @return
	 * 	Requested port or if it does not exists, then first one
	 *  If there are no ports or requested port does nto exist, return null
	 *   
	 */
	@Override
	public DataPort getDataPortForItem( Long itemId, String portName){
		DataPort port = null;
		if( itemId <= 0 ) return port;
		
		if( portName != null && portName.length() > 0 ){
			List<DataPort> ports = getPortByName(portName, itemId);
			if( ports.size() > 0 ) 	port = ports.get(0);
		}else{
			port = getFirstPort(itemId);
		}
		

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
	public DataPort getPort(Long itemId, Long portSubClass, String portName) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.ilike("portName", portName));
		
		DataPort port = (DataPort) c.uniqueResult();
		
		return port;
	}
	
	@Override
	public DataPort getPort(String location, String itemName, String portName) {
		
		Criteria c = getCriteria(location, itemName, portName);
				
		return (DataPort) c.uniqueResult();		
	}			
}
