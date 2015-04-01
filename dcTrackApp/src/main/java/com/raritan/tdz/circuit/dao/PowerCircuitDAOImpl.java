package com.raritan.tdz.circuit.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortDTOBase;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class PowerCircuitDAOImpl  extends DaoImpl<PowerCircuit> implements PowerCircuitDAO {
	@Override
	public List<PowerCircuit> viewPowerCircuitByConnId(Long connectionId) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(this.type);
			cr.add(Restrictions.like("circuitTrace", "," + String.valueOf(connectionId) + ",", MatchMode.ANYWHERE));

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return cr.list();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public List<PowerCircuit> viewPowerCircuitByStartPortId(Long portId) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(this.type);

			cr.createAlias("startConnection", "startConn");
			cr.createAlias("endConnection","endConn");
			cr.createAlias("startConn.sourcePowerPort", "startPort");
			cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
			cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);
			cr.add(Restrictions.eq("startPort.portId", portId) );

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return cr.list();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

    private PowerCircuit getCircuitTraceForPowerPort(PowerPort port) throws DataAccessException {

    	PowerCircuit pwrCir = new PowerCircuit();

    	String trace = new String(); trace = ",";
    	PowerConnection endConn = null;

    	// Session session = this.getSession();
    	PowerPort startPort = port; // (PowerPort) session.get(PowerPort.class, port.getPortId());

    	if (null == startPort) {
    		// session.close();
    		return pwrCir;
    	}
    	Hibernate.initialize(startPort.getSourcePowerConnections());
    	// session.close();

    	Set<PowerConnection> conns = startPort.getSourcePowerConnections();
    	if (conns != null && conns.size() == 1) {
    		for (PowerConnection conn: conns) {
    			trace += conn.getConnectionId();
    			endConn = conn;
    		}
    	}
    	else {
    		// invalid connections in the database
    		return pwrCir;
    	}
    	PowerPort destPort = (PowerPort)endConn.getDestPort();
    	PowerCircuit tmpPwrCir =  null;
    	if (null != destPort) {
    		tmpPwrCir =  getCircuitTraceForPowerPort((PowerPort)endConn.getDestPort());
    	}
    	else {
    		pwrCir.setCircuitTrace(trace + ",");
    		pwrCir.setEndConnection(endConn);
    	}
    	if (null != tmpPwrCir) {
    		pwrCir.setCircuitTrace(trace + tmpPwrCir.getCircuitTrace());
    		pwrCir.setEndConnection(tmpPwrCir.getEndConnection());
    	}

    	return pwrCir;
    }


    @Override
    public List<PowerCircuit> getCircuitsWithTrace(String trace) {
		Session session = this.getSession();

		Criteria cr = session.createCriteria(this.type);

		Criterion cirTraceCond = Restrictions.like("circuitTrace", trace, MatchMode.END);
		Criterion sharedCirTraceCond = Restrictions.like("sharedCircuitTrace", trace, MatchMode.END);
		cr.add(Restrictions.or(cirTraceCond, sharedCirTraceCond));

		//Get Records
		cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		@SuppressWarnings("unchecked")
		List<PowerCircuit> circuits = cr.list();

		return circuits;
    }

    @Override
    public List<Object[]> getCircuitsInfoWithTrace(String trace) {
		Session session = this.getSession();

		session.flush();

		Query q = session.createSQLQuery(new StringBuffer()
		.append("select circuit_power_id, circuit_trace, shared_circuit_trace from dct_circuits_power where circuit_trace like :oldTrace or shared_circuit_trace like :oldTrace ")
		.toString()
	    );

		q.setString("oldTrace", "%" + trace);

		List<Object[]> recList = q.list();

		return recList;

    }

    @Override
    public List<Object[]> getConnAndDestPort(Long portId) {

		Session session = this.getSession();

		session.flush();

		Query q = session.createSQLQuery(new StringBuffer()
		.append("select connection_power_id, dest_port_id from dct_connections_power where source_port_id = :portId ")
		.toString()
	    );

		q.setLong("portId", portId);

		List<Object[]> recList = q.list();

		return recList;

    }

	@Override
	public void changeCircuitTrace(Long circuitId, String circuitTrace, String sharedCircuitTrace, Long endConnId) {

		if (null == circuitId) {
			return;
		}

		Session session = this.getSession();

		Query q = session.createSQLQuery(new StringBuffer()
		.append("update dct_circuits_power set circuit_trace = :circuitTrace, shared_circuit_trace = :sharedCircuitTrace, end_conn_id = :endConnId where circuit_power_id = :circuitId ")
		.toString()
	    );

		q.setString("circuitTrace", circuitTrace);
		q.setString("sharedCircuitTrace", sharedCircuitTrace);
		q.setLong("endConnId", endConnId);
		q.setLong("circuitId", circuitId);

		q.executeUpdate();

	}



    @Override
	public void changeCircuitConnectionChange(PowerPort oldPort, PowerPort newPort) throws DataAccessException {
		PowerCircuit oldPowerCirInfo = getCircuitTraceForPowerPort(oldPort);
		PowerCircuit newPowerCirInfo = getCircuitTraceForPowerPort(newPort);



		Session session = this.getSession();

		Criteria cr = session.createCriteria(this.type);

		Criterion cirTraceCond = Restrictions.like("circuitTrace", oldPowerCirInfo.getCircuitTrace(), MatchMode.END);
		Criterion sharedCirTraceCond = Restrictions.like("sharedCircuitTrace", oldPowerCirInfo.getCircuitTrace(), MatchMode.END);

		cr.add(Restrictions.or(cirTraceCond, sharedCirTraceCond));

		//Get Records
		cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		@SuppressWarnings("unchecked")
		List<PowerCircuit> circuits = cr.list();

		for (PowerCircuit circuit: circuits) {
			if (null != circuit.getCircuitTrace() && circuit.getCircuitTrace().endsWith(oldPowerCirInfo.getCircuitTrace())) {
				String newCirTrace = circuit.getCircuitTrace();
				newCirTrace = newCirTrace.replace(oldPowerCirInfo.getCircuitTrace(), newPowerCirInfo.getCircuitTrace());
				circuit.setCircuitTrace(newCirTrace);
			}
			if (null != circuit.getSharedCircuitTrace() && circuit.getSharedCircuitTrace().endsWith(oldPowerCirInfo.getCircuitTrace())) {
				String newCirTrace = circuit.getSharedCircuitTrace();
				newCirTrace = newCirTrace.replace(oldPowerCirInfo.getCircuitTrace(), newPowerCirInfo.getCircuitTrace());
				circuit.setSharedCircuitTrace(newCirTrace);
			}
			circuit.setEndConnection(newPowerCirInfo.getEndConnection());
			session.update(circuit);
		}

	}

	@Override
	public List<PowerCircuit> viewPowerCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(this.type);

			List<Long> circuitIdList = null;

			//circuit id is primary key, no need to search using other fields
			final long circuitId = cCriteria.getCircuitUID().getCircuitDatabaseId();

			if(circuitId > 0) {
				cr.add(Restrictions.eq("powerCircuitId", circuitId));
			}
			else if(circuitIdList != null){
				cr.add(Restrictions.in("powerCircuitId", circuitIdList));
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
				cr.createAlias("startConn.sourcePowerPort", "startPort");

				if(cCriteria.getStartConnId() != null && cCriteria.getStartConnId() > 0){
					cr.add(Restrictions.eq("startConn.powerConnectionId", cCriteria.getStartConnId()));
				}

				if(cCriteria.getEndConnId() != null && cCriteria.getEndConnId() > 0){
					cr.add(Restrictions.eq("endConn.powerConnectionId", cCriteria.getEndConnId()));
				}

				if(cCriteria.getLocationId() != null && cCriteria.getLocationId() > 0){
					cr.createAlias("startConn.sourcePowerPort.item", "startItem");
					cr.createAlias("startItem.dataCenterLocation", "location");
					cr.add(Restrictions.eq("location.dataCenterLocationId", cCriteria.getLocationId()));
				}

				if(cCriteria.getConnectionId() != null && cCriteria.getConnectionId()> 0){
					cr.add(Restrictions.like("circuitTrace", "%," + cCriteria.getConnectionId() + ",%"));
				}

				if(cCriteria.getStartPortId() != null && cCriteria.getStartPortId() > 0){
					cr.add(Restrictions.eq("startPort.portId", cCriteria.getStartPortId()));
				}			
			}

			cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
			cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			return cr.list();

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));
		}
	}

	@Override
	public  HashMap<Long, PortInterface> getDestinationItemsForItem(long itemId) {
		Session session = this.getSession();

		HashMap<Long, PortInterface> portMap = new HashMap<Long, PortInterface>();
	    /*<return-scalar column="port_id" type="long"/>
	    <return-scalar column="dest_item_name" type="string"/>
	    <return-scalar column="dest_port_name" type="string"/>
	    <return-scalar column="dest_item_id" type="long"/>
	    <return-scalar column="dest_port_id" type="long"/>
	    <return-scalar column="circuit_power_id" type="long"/>
	    <return-scalar column="next_node_class_value_code" type="long"/>*/

		//call name query
		Query query = session.getNamedQuery("getDestinationItemsForItemPower");
		query.setParameter("itemId", itemId);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			PowerPortDTO port = new PowerPortDTO();
			port.setPortId((Long)row[0]);
			port.setConnectedItemName((String)row[1]);
			port.setConnectedPortName((String)row[2]);
			port.setConnectedItemId((Long)row[3]);
			port.setConnectedPortId((Long)row[4]);
			port.setNextNodeClassValueCode((Long)row[6]);
			port.setCircuitStatusLksValueCode((Long)row[7]);
			port.setCircuitStatusLksValue((String)row[8]);

			CircuitUID cid = new CircuitUID((Long)row[5], SystemLookup.PortClass.POWER, false);

			port.setConnectedCircuitId(cid.floatValue());
			portMap.put(port.getPortId(), (PortDTOBase)port);
		}

		return portMap;

	}

	@Override
	public  HashMap<Long, PortInterface> getNextNodeAmpsForItem(long itemId) {
		Session session = this.getSession();

		HashMap<Long, PortInterface> portMap = new HashMap<Long, PortInterface>();

		//call name query
		Query query = session.getNamedQuery("getNextNodeAmpsForItem");
		query.setParameter("itemId", itemId);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			PowerPortDTO port = new PowerPortDTO();
			port.setPortId((Long)row[0]);
			port.setAmpsActualNextNode((Double)row[1]);

			portMap.put(port.getPortId(), (PortDTOBase)port);
		}

		return portMap;

	}

	@Override
	public PowerBankInfo getPowerBankInfo(long bankId) {
		Query q = getSession().getNamedQuery("getPowerBankInfoQuery");
		q.setLong("itemId", bankId);
		q.setResultTransformer(Transformers.aliasToBean(PowerBankInfo.class));
		Object result = q.uniqueResult();

		if (result != null){
			return (PowerBankInfo)result;
		} else {
			return null;
		}
	}

	@Override
	public List<?> getPowerUsage(String queryStr, Object[] queryArgs) {
		final Query namedQuery = getSession().createQuery(queryStr);
		namedQuery.setReadOnly(true);

		for (int i = 0; i < queryArgs.length; i++){
			Object arg = queryArgs[i];
			namedQuery.setParameter(i, arg);
		}

//		if (powerUsageDomain != null){
//			namedQuery.setResultTransformer(Transformers.aliasToBean(powerUsageDomain));
//		}

		List<?> result = (List<?>) namedQuery.list();

		return result == null ? new ArrayList()  : result;
	}

	@Override
	public PowerCircuit getPowerCircuit(long powerCircuitId) {
		PowerCircuit circuit = read(powerCircuitId);
		Session session = this.getSession();

		if(circuit == null) return null;

		//Fill up the transient collection of power connections by
		//parsing the circuitTrace list.
		List<Long> connectionIds = circuit.getConnListFromTrace();
		if (connectionIds != null && connectionIds.size() > 0){
			List<PowerConnection> connectionList = new ArrayList<PowerConnection>();
			PowerConnection conn;

			for (Long connectionId:connectionIds){
				conn = (PowerConnection)session.get(PowerConnection.class, connectionId);
				connectionList.add(conn);
			}
			circuit.setCircuitConnections(connectionList);
		}

		return circuit;
	}


	@Override
	public List<PowerWattUsedSummary> getPowerWattUsedSummary(long portPowerId, Long portIdToExclude, Long fuseLkuId, Long nodePortIdToExclude) {
		Query q = getSession().getNamedQuery("getPowerWattUsedSummary");
		q.setLong("portPowerId", fuseLkuId == null ? portPowerId : -1);
		q.setLong("powerSupplyPortId", portIdToExclude == null ? -1 : portIdToExclude);
		q.setLong("inputCordPortId", fuseLkuId == null ? -1 : portPowerId);
		q.setLong("fuseLkuId", fuseLkuId == null ? -1 : fuseLkuId);
		q.setLong("nodePortIdToExclude", nodePortIdToExclude == null ? -1 : nodePortIdToExclude);
		q.setResultTransformer(Transformers.aliasToBean(PowerWattUsedSummary.class));

		return q.list();
	}

	@Override
	public List<PowerWattUsedSummary> getPowerWattUsedSummaryMeasured(long portPowerId) {
		Query q = getSession().getNamedQuery("getPowerWattUsedSummaryMeasured");
		q.setLong("portPowerId", portPowerId);
		q.setResultTransformer(Transformers.aliasToBean(PowerWattUsedSummary.class));

		List<PowerWattUsedSummary> recList = q.list();

		for(PowerWattUsedSummary rec:recList){
			rec.setMeasured(true);
		}

		return recList;
	}

	@Override
	public List<PowerWattUsedSummary> getPowerWattUsedSummary(long portPowerId, Long portIdToExclude, Long fuseLkuId, Long inputCordToExclude, boolean measured) {
		if(measured){
			return getPowerWattUsedSummaryMeasured(portPowerId);
		}

		return getPowerWattUsedSummary(portPowerId, portIdToExclude, fuseLkuId, inputCordToExclude);
	}

	@Override
	public long getPowerWattUsedTotal(long portPowerId, Long fuseLkuId) {
		long usedWatts = 0;

		Query q = getSession().getNamedQuery("getPowerWattUsedTotal");
		q.setLong("portPowerId", fuseLkuId == null ? portPowerId : -1);
		q.setLong("inputCordPortId", fuseLkuId == null ? -1 : portPowerId);
		q.setLong("fuseLkuId", fuseLkuId == null ? -1 : fuseLkuId);

		for(Object object:q.list()){
			Long watts = (Long)object;

	    	if(watts != null){
	    		usedWatts += watts.longValue();
	    	}
		}

	    return usedWatts;
	}

	@Override
	public  HashMap<Long, PortInterface> getProposedCircuitIdsForItem(long itemId) {
		Session session = this.getSession();

		HashMap<Long, PortInterface> portMap = new HashMap<Long, PortInterface>();

		//call name query
		Query query = session.getNamedQuery("getPowerProposedCircuitIdsForItem");
		query.setParameter("itemId", itemId);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			CircuitUID cid = new CircuitUID((Integer)row[1], SystemLookup.PortClass.POWER, true);

			PowerPortDTO port = new PowerPortDTO();
			port.setPortId((Long)row[0]);
			port.setProposedCircuitId(cid.floatValue());

			portMap.put(port.getPortId(), (PortDTOBase)port);
		}

		return portMap;

	}

	@Override
	public void reconnectPowerPorts (Long oldSrcPortId, Long oldDestPortId, Long newSrcPortId, Long newDestPortId) {
		Session session = this.getSession();
		Query query =  session.getNamedQuery("reconnectPowerPorts");
		query.setLong("oldSrcPortId", oldSrcPortId);
		query.setLong("oldDestPortId", oldDestPortId);
		query.setLong("newSrcPortId", newSrcPortId);
		query.setLong("newDestPortId", newDestPortId);
		query.uniqueResult();
	}

	@Override
	public Long deleteCircuit (Long circuitId, boolean isUpdate) {
		Session session = this.getSession();
		Query query =  session.getNamedQuery("dcDeletePowerCircuit"); //dc_deletepowercircuit
		query.setLong("circuitId", circuitId);
		query.setBoolean("isUpdate", isUpdate);
		return ((Integer)query.uniqueResult()).longValue();
	}

	@Override
	public void processRelatedPowerCircuits (Long startPortId, String oldTrace) {
		Session session = this.getSession();
		Query query =  session.getNamedQuery("processRelatedPowerCircuits");
		query.setLong("startPortId", startPortId);
		query.setString("oldTrace", oldTrace);
		query.uniqueResult();
	}
	

	@Override
	public String getCircuitTrace(Long startPortId) {

		// 
		Query query =  this.getSession().getNamedQuery("dcGetCircuitTrace");
    	query.setString("connType", SystemLookup.PortClass.POWER_DESC);
		query.setLong("startPortId", startPortId);
    	
		String trace = (String) query.uniqueResult();
		
		return trace;
		
	}	

	@Override
	public CircuitViewData getPowerCircuitForStartPort(String location, String itemName, String portName) throws DataAccessException  {
		try{
			Criteria cr = this.getSession().createCriteria(CircuitViewData.class);			
			cr.add(Restrictions.ilike("locationCode", location) );
			cr.add(Restrictions.ilike("startItemName", itemName) );
			cr.add(Restrictions.ilike("startPortName", portName) );
			cr.add(Restrictions.eq("circuitType", SystemLookup.PortClass.POWER) );
			
			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			for(Object obj:cr.list()) {
				return (CircuitViewData)obj; 
			}
			
		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));
		}
		
		return null;
	}
	
	@Override
	public Long getProposedCircuitId(Long circuitId) throws DataAccessException {
		if (circuitId == null) return null;
		
		Long proposedCircuitId = null;

		try {
			Query q = this.getSession().getNamedQuery( "getProposedCircuitIdForPowerCircuit" );
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
}
