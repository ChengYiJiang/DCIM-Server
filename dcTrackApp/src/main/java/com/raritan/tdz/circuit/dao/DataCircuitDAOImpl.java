package com.raritan.tdz.circuit.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortDTOBase;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class DataCircuitDAOImpl  extends DaoImpl<DataCircuit> implements DataCircuitDAO {
	@Autowired(required=true)
	DataConnDAO dataConnectionDAO;

	@Override
	public DataCircuit getDataCircuit(Long circuitId) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(this.type);

			cr.add(Restrictions.eq("dataCircuitId", circuitId));
			cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
			cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			List<Object> recList = cr.list();

			if(recList.size() > 0){
				DataCircuit circuit = (DataCircuit)recList.get(0);
				List<DataConnection> dataConnections = new ArrayList<DataConnection>();

				//Load connection records
				for(long connectionId:circuit.getConnListFromTrace()){
					DataConnection dataConnection = dataConnectionDAO.loadConn(connectionId);
					dataConnections.add(dataConnection);
				}

				circuit.setCircuitConnections(dataConnections);

				return circuit;
			}

			return null;

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public List<DataCircuit> viewDataCircuitByConnId(Long connectionId) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(this.type);
			cr.add(Restrictions.like("circuitTrace", "," + String.valueOf(connectionId) + ",", MatchMode.ANYWHERE));

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return cr.list();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}
	
	private Criteria getStartPortCriteria(Long portId) {
		Criteria cr = this.getSession().createCriteria(this.type);

		cr.createAlias("startConnection", "startConn");
		cr.createAlias("endConnection","endConn");
		cr.createAlias("startConn.sourceDataPort", "startPort");
		cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
		cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);
		cr.add(Restrictions.eq("startPort.portId", portId) );

		return cr;
		
	}
	
	private Criteria addEndPortCriteria(Criteria cr, Long endPortId) {
		
		cr.createAlias("endConn.sourceDataPort", "endPort");
		cr.add(Restrictions.eq("endPort.portId", endPortId) );
		
		return cr;
	}

	@Override
	public List<DataCircuit> viewDataCircuitByStartPortId(Long portId) throws DataAccessException  {
		try{
			
			Criteria cr = getStartPortCriteria(portId);
			
			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return cr.list();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public DataCircuit viewDataCircuitByPortIds(Long startPortId, Long endPortId) throws DataAccessException  {
		try{
			Criteria cr = getStartPortCriteria(startPortId);
			
			cr = addEndPortCriteria(cr, endPortId);
			
			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return (DataCircuit) cr.uniqueResult();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

	
	@Override
	public List<DataCircuit> viewDataCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(this.type);

			List<Long> circuitIdList = null;

			//circuit id is primary key, no need to search using other fields
			final long circuitId = cCriteria.getCircuitUID().getCircuitDatabaseId();

			if(circuitId > 0) {
				cr.add(Restrictions.eq("dataCircuitId", circuitId));
			}
			else if(circuitIdList != null){
				cr.add(Restrictions.in("dataCircuitId", circuitIdList));
			}
			else if(cCriteria.getCircuitTrace() != null && cCriteria.getCircuitTrace().trim().length() > 0){
				cr.add(Restrictions.eq("circuitTrace", cCriteria.getCircuitTrace().trim()));
			}
			else if(cCriteria.getContainCircuitTrace() != null && cCriteria.getContainCircuitTrace().trim().length() > 0){
				cr.add(Restrictions.like("circuitTrace", cCriteria.getContainCircuitTrace().trim(), MatchMode.ANYWHERE));
			}
			else{
				cr.createAlias("startConnection", "startConn");
				cr.createAlias("endConnection","endConn");
				cr.createAlias("startConn.sourceDataPort", "startPort");

				if(cCriteria.getStartPortId() != null && cCriteria.getStartPortId() > 0){
					cr.add(Restrictions.eq("startPort.portId", cCriteria.getStartPortId()));
				}
				
				if(cCriteria.getStartConnId() != null && cCriteria.getStartConnId() > 0){
					cr.add(Restrictions.eq("startConn.dataConnectionId", cCriteria.getStartConnId()));
				}

				if(cCriteria.getEndConnId() != null && cCriteria.getEndConnId() > 0){
					cr.add(Restrictions.eq("endConn.dataConnectionId", cCriteria.getEndConnId()));
				}

				if(cCriteria.getLocationId() != null && cCriteria.getLocationId() > 0){
					cr.createAlias("startConn.sourceDataPort.item", "startItem");
					cr.createAlias("startItem.dataCenterLocation", "location");
					cr.add(Restrictions.eq("location.dataCenterLocationId", cCriteria.getLocationId()));
				}

				if(cCriteria.getConnectionId() != null && cCriteria.getConnectionId()> 0){
					cr.add(Restrictions.like("circuitTrace", "%," + cCriteria.getConnectionId() + ",%"));
				}
			}

			cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
			cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return cr.list();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public  HashMap<Long, PortInterface> getDestinationItemsForItem(long itemId) {
		Session session = this.getSession();

		HashMap<Long, PortInterface> portMap = new HashMap<Long, PortInterface>();

		//call name query
		Query query = session.getNamedQuery("getDestinationItemsForItemData");
		query.setParameter("itemId", itemId);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			PortDTOBase port = new DataPortDTO();
			port.setPortId((Long)row[0]);
			port.setConnectedItemName((String)row[1]);
			port.setConnectedPortName((String)row[2]);
			port.setConnectedItemId((Long)row[3]);
			port.setConnectedPortId((Long)row[4]);

			CircuitUID cid = new CircuitUID((Long)row[5], SystemLookup.PortClass.DATA, false);

			port.setConnectedCircuitId(cid.floatValue());
			port.setNextNodeClassValueCode((Long)row[6]);
			port.setCircuitStatusLksValueCode((Long)row[7]);
			port.setCircuitStatusLksValue((String)row[8]);

			portMap.put(port.getPortId(), port);
		}

		return portMap;

	}

	@Override
	public  HashMap<Long, PortInterface> getProposedCircuitIdsForItem(long itemId) {
		Session session = this.getSession();

		HashMap<Long, PortInterface> portMap = new HashMap<Long, PortInterface>();

		//call name query
		Query query = session.getNamedQuery("getDataProposedCircuitIdsForItem");
		query.setParameter("itemId", itemId);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			CircuitUID cid = new CircuitUID((Integer)row[1], SystemLookup.PortClass.DATA, true);

			DataPortDTO port = new DataPortDTO();
			port.setPortId((Long)row[0]);
			port.setProposedCircuitId(cid.floatValue());

			portMap.put(port.getPortId(), (PortDTOBase)port);
		}

		return portMap;

	}

	@Override
	public Long getFanoutCircuitIdForStartPort(long portId) {
		Session session = this.getSession();

		//call name query
		Query query = session.getNamedQuery("getFanoutCircuitIdForStartPort");
		query.setParameter("portId", portId);

		for (Object rec:query.list()) {
			return (Long) rec;
		}

		return null;
	}

	@Override
	public boolean isLogicalConnectionsExist(Long itemId1, Long itemId2) throws DataAccessException {
		Session session = this.getSession();

		//call name query
		Query query = session.getNamedQuery("isLogicalConnectionsExist");
		query.setParameter("itemId1", itemId1);
		query.setParameter("itemId2", itemId2 == null ? 0 : itemId2);
		Long value = (Long)query.uniqueResult();

        return ( value > 0);
	}


	@Override
	public String getCircuitTrace(Long startPortId) {

		// 
		Query query =  this.getSession().getNamedQuery("dcGetCircuitTrace");
    	query.setString("connType", SystemLookup.PortClass.DATA_DESC);
		query.setLong("startPortId", startPortId);
    	
		String trace = (String) query.uniqueResult();
		
		return trace;
		
	}	

	@Override
	public CircuitViewData getDataCircuitForStartPort(String location, String itemName, String portName) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(CircuitViewData.class);			
			cr.add(Restrictions.ilike("locationCode", location) );
			cr.add(Restrictions.ilike("startItemName", itemName) );
			cr.add(Restrictions.ilike("startPortName", portName) );
			cr.add(Restrictions.eq("circuitType", SystemLookup.PortClass.DATA) );
			
			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			for(Object obj:cr.list()) {
				return (CircuitViewData)obj; 
			}
			
		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
		
		return null;
	}
	
	@Override
	public Long getProposedCircuitId(Long circuitId) throws DataAccessException {
		if (circuitId == null) return null;
		
		Long proposedCircuitId = null;

		try {
			Query q = this.getSession().getNamedQuery( "getProposedCircuitIdForDataCircuit" );
			q.setLong("circuitId", circuitId);
			proposedCircuitId = (Long)q.uniqueResult();
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return proposedCircuitId;
	}

	@Override
	public CircuitViewData getDataCircuitForStartPortId(Long portId) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(CircuitViewData.class);			
			cr.add(Restrictions.eq("startPortId", portId) );
			cr.add(Restrictions.eq("circuitType", SystemLookup.PortClass.DATA) );
			
			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			for(Object obj:cr.list()) {
				return (CircuitViewData)obj; 
			}
			
		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}
		
		return null;
	}
}
