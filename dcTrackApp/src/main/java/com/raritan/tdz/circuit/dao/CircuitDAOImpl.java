package com.raritan.tdz.circuit.dao;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.request.home.RequestInfo;

public class CircuitDAOImpl<T extends Serializable> extends DaoImpl<T>
		implements CircuitDAO<T> {

	protected Class<T> type;
	
	private String isCircuitInCabinetQuery;
	
	private String getAssociatedCircuitsForCabinetNamedQuery;
	
	private String getCabinetPlannedCircuitIdsNotMovingQuery;
	
	private String getParentMoveRequestQuery;
	
	private String getPendingCircuitRequestForItems;
	
	private String getReconnectCircuitReqInfo;
	
	private String getDisAndMoveCircuitReqInfo;
	
	public String getIsCircuitInCabinetQuery() {
		return isCircuitInCabinetQuery;
	}



	public void setIsCircuitInCabinetQuery(String isCircuitInCabinetQuery) {
		this.isCircuitInCabinetQuery = isCircuitInCabinetQuery;
	}



	public CircuitDAOImpl(Class<T> type, String isCircuitInCabinetQuery, 
			String getAssociatedCircuitsForCabinetNamedQuery, 
			String getCabinetPlannedCircuitIdsNotMovingQuery, 
			String getParentMoveRequestQuery,
			String getPendingCircuitRequestForItems, 
			String getReconnectCircuitReqInfo, 
			String getDisAndMoveCircuitReqInfo) {
		super();
		this.type = type;
		this.isCircuitInCabinetQuery = isCircuitInCabinetQuery;
		this.getAssociatedCircuitsForCabinetNamedQuery = getAssociatedCircuitsForCabinetNamedQuery;
		this.getCabinetPlannedCircuitIdsNotMovingQuery = getCabinetPlannedCircuitIdsNotMovingQuery;
		this.getParentMoveRequestQuery = getParentMoveRequestQuery;
		this.getPendingCircuitRequestForItems = getPendingCircuitRequestForItems;
		this.getReconnectCircuitReqInfo = getReconnectCircuitReqInfo;
		this.getDisAndMoveCircuitReqInfo = getDisAndMoveCircuitReqInfo;
		
	}



	@Override
	public boolean isCircuitInCabinet(long circuitId, long cabinetId) {
		Session session = this.getSession();
		
		Query qParentItemIdList = session.createSQLQuery(new StringBuffer()
			.append(isCircuitInCabinetQuery)
			.toString()
				);

		qParentItemIdList.setLong("circuitId", circuitId);
		@SuppressWarnings("unchecked")
		List<BigInteger> parentItemIdList = (List<BigInteger>) qParentItemIdList.list();

		if (null == parentItemIdList || parentItemIdList.isEmpty()) 	return false; 
		
		for (BigInteger parentItemId: parentItemIdList) {
			if (null == parentItemId || parentItemId.longValue() != cabinetId) 
				return false;
		}
		
		return true;
		
	}
	
	@Override
	public List<CircuitCriteriaDTO> getAssociatedCircuitsForCabinet( long cabinetId ) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();
		Query query = this.getSession().getNamedQuery(getAssociatedCircuitsForCabinetNamedQuery);
		query.setLong("cabinetItemId", cabinetId);
	 	CircuitCriteriaDTO cCriteria;
		
	 	for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[1];
			
			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));
			
			recList.add(cCriteria);
		}
		
	 	return recList;
	}


	@Override
	public List<CircuitCriteriaDTO> getAllInstalledCircuitsOutsideCabinet(long cabinetId) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();
		StringBuilder queryName = 
					new StringBuilder("getAllInstalled")
						//the next append is actually putting either "DataCircuit", "PowerCircuit"
						//or "SensorCircuit" based on the type of this DAO
						.append(this.type.getName().substring(this.type.getName().lastIndexOf(".") + 1, this.type.getName().length()))
						.append("OutsideCabinet");
		
		Query query = this.getSession().getNamedQuery(queryName.toString());
		query.setLong("cabinetItemId", cabinetId);
		
	 	CircuitCriteriaDTO cCriteria;
		
	 	for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[1];
			
			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));
			
			recList.add(cCriteria);
		}
		
	 	return recList;
	}

	@Override
	public List<CircuitCriteriaDTO> getAllCircuitsOutsideCabinet(long cabinetId) {
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();
		StringBuilder queryName = 
					new StringBuilder("getAll")
						//the next append is actually putting either "DataCircuit", "PowerCircuit"
						//or "SensorCircuit" based on the type of this DAO
						.append(this.type.getName().substring(this.type.getName().lastIndexOf(".") + 1, this.type.getName().length()))
						.append("OutsideCabinet");
		
		Query query = this.getSession().getNamedQuery(queryName.toString());
		query.setLong("cabinetItemId", cabinetId);
		
	 	CircuitCriteriaDTO cCriteria;
		
	 	for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[1];
			
			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));
			
			recList.add(cCriteria);
		}
		
	 	return recList;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getCabinetPlannedCircuitIdsNotMoving(long cabinetId) {

		Query query =  this.getSession().getNamedQuery(getCabinetPlannedCircuitIdsNotMovingQuery);
    	query.setLong("cabinetId", cabinetId);
    	
		return query.list();
	}



	@Override
	public String getCircuitTrace(String connType, Long startPortId) {

		// 
		Query query =  this.getSession().getNamedQuery("dcGetCircuitTrace");
    	query.setString("connType", connType);
		query.setLong("startPortId", startPortId);
    	
		String trace = (String) query.uniqueResult();
		
		return trace;
		
	}



	@Override
	public List<RequestInfo> getParentMoveRequest(List<Long> connectionIds) {
		
		Query query =  this.getSession().getNamedQuery(getParentMoveRequestQuery);
		query.setParameterList("connIds", connectionIds.toArray());
		
		return getTransformedRequest(query);
		
	}
	
	@Override
	public List<RequestInfo> getPendingCircuitRequestForItems(List<Long> itemIds, List<Long> requestTypeLkpCodes) {
		
		Query query =  this.getSession().getNamedQuery(getPendingCircuitRequestForItems);
		query.setParameterList("itemIds", itemIds.toArray());
		query.setParameterList("requestTypeLkpCodes", requestTypeLkpCodes.toArray());
		
		return getTransformedRequest(query);
		
	}
	
	private List<RequestInfo> getTransformedRequest(Query query) {
		
		query.setResultTransformer(Transformers.aliasToBean(RequestInfo.class));
		
		@SuppressWarnings("unchecked")
		List<RequestInfo> requestList = (List<RequestInfo>) query.list();
		
		return requestList;
		
	}

	
	@Override
	public List<RequestInfo> getProposedCircuitRequest(List<Long> itemIds) {
		
		Query reconnectQuery =  this.getSession().getNamedQuery(getReconnectCircuitReqInfo);
		reconnectQuery.setParameterList("itemIds", itemIds.toArray());
		
		List<RequestInfo> reqInfo = getTransformedRequest(reconnectQuery);
		
		Query disAndMoveQuery =  this.getSession().getNamedQuery(getDisAndMoveCircuitReqInfo);
		disAndMoveQuery.setParameterList("itemIds", itemIds.toArray());
		
		reqInfo.addAll(getTransformedRequest(disAndMoveQuery));
		
		return reqInfo;
	}
}
