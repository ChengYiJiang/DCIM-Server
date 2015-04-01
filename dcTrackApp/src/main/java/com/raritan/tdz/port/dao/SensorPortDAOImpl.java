package com.raritan.tdz.port.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.lookup.SystemLookup;


public class SensorPortDAOImpl extends DaoImpl<SensorPort> implements SensorPortDAO {

	@Override
	public SensorPort loadPort(Long portId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setFetchMode("item", FetchMode.JOIN);
		criteria.setFetchMode("portSubClassLookup", FetchMode.JOIN);
		criteria.setFetchMode("connectorLookup", FetchMode.JOIN);
		criteria.setFetchMode("cabinetItem", FetchMode.JOIN);
		criteria.setFetchMode("cabLocationLookup", FetchMode.JOIN);
		criteria.add(Restrictions.eq("portId", portId));
		
		SensorPort port = (SensorPort)criteria.uniqueResult();
		
		return port;	

	}

	@Override
	public SensorPort loadEvictedPort(Long portId) {
		SensorPort port = loadPort(portId);
		this.getSession().evict(port);
		
		return port;	
	}

	@Override
	public boolean isPortUsed(Long portId) {	
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
	public List<SensorPort> getPortsForItem(Long itemId) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );		
		criteria.add(Restrictions.eq("item.itemId", itemId));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}

	@Override
	public List<SensorPort> getPortsFromPortIdList(List<Long> portList) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );	
		criteria.add(Restrictions.in("portId", portList));
		criteria.addOrder(Order.asc("portName"));

		return criteria.list();
	}

	@Override
	public List<Long> getAllAssetStripSensorCabinets(String siteCode, List<Long> excludeSensorId) {
    	Session session = this.getSession();
    	 	
		Criteria criteria = session.createCriteria(SensorPort.class);
		
		criteria.createAlias("portSubClassLookup", "portSubClassLookup");
		criteria.createAlias("cabinetItem","cabinetItem",Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("cabinetItemId"))));
		
		if( siteCode != null && siteCode.length() > 0){	
			criteria.createAlias("cabinetItem.dataCenterLocation","dataCenterLocation",Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("dataCenterLocation.code",siteCode));
			if( excludeSensorId != null && excludeSensorId.size() > 0){
				criteria.add(Restrictions.not(Restrictions.in("portId", excludeSensorId)));				
				/*for( Long sensorId : excludeSensorId ){
					criteria.add(Restrictions.ne("portId",sensorId));
				}*/
			}
		}
		criteria.add(Restrictions.isNotNull("cabinetItem.itemId"));
		criteria.add(Restrictions.eq("portSubClassLookup.lkpValueCode", SystemLookup.PortSubClass.ASSET_STRIP));
		
		return (List<Long>) criteria.list();
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
		c.add(Restrictions.eq("portSubClassLookup.lkpValueCode", portSubClass.longValue()));	
		
		return c;
	}
	
	@Transactional
	@Override
	public Long getPortId(Long itemId, Long portSubClass, String portName) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.eq("portName", portName));
		
		projectPortId(c);
		
		return (Long) c.uniqueResult();
		
	}
	
	private Criteria getCriteria(String location, String itemName, String portName) {
		
		Criteria c = this.getSession().createCriteria(this.type);
		c.createAlias("item", "item");
		c.add(Restrictions.eq("itemName", itemName));
		c.createAlias("item.dataCenterLocation", "dataCenterLocation");
		c.add(Restrictions.eq("dataCenterLocation.code", location)); 

		c.add(Restrictions.eq("portName", portName));
		
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
	public SensorPort getPort(String location, String itemName, String portName) {
		
		Criteria c = getCriteria(location, itemName, portName);
		
		return (SensorPort) c.uniqueResult();
		
	}
	
	@Override
	public SensorPort getPort(Long itemId, Long portSubClass, String portName) {
		Criteria c = getCriteria(itemId, portSubClass);
		
		c.add(Restrictions.eq("portName", portName));
		
		SensorPort port = (SensorPort) c.uniqueResult();
		
		return port;
	}
	

}
