package com.raritan.tdz.circuit.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.ConnectorCompat;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class PowerConnDAOImpl extends DaoImpl<PowerConnection> implements PowerConnDAO  {

	public PowerConnDAOImpl() {
		
	}	

	@Override
	public PowerConnection getConn(Long id) {
		return this.read(id);
	}

	@Override
	public PowerConnection loadConn(Long id){
		return loadConn(id, false);
	}

	@Override
	public PowerConnection loadConn(Long id, boolean readOnly){
		PowerConnection result = null;
		Session session = null;

		try {
			if (id != null && id > 0){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(this.type);
				//We are doing an EAGER loading since we will be filling the power with what
				//client sends and none of them should be null;
				criteria.setFetchMode("sourcePowerPort", FetchMode.JOIN);
				criteria.setFetchMode("destPowerPort", FetchMode.JOIN);
				criteria.setFetchMode("connectionCord", FetchMode.JOIN);
				criteria.setFetchMode("statusLookup", FetchMode.JOIN);
				criteria.setFetchMode("connectionType", FetchMode.JOIN);
				criteria.add(Restrictions.eq("powerConnectionId", id));
				criteria.setReadOnly(readOnly);
				
				result = (PowerConnection) criteria.uniqueResult();
			}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}
		
	
	@Override
	public List<PowerConnection> getConnsForItem(long itemId) throws DataAccessException {
		try{
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.createAlias("sourcePowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.eq("item.itemId", itemId) );			
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			return  criteria.list();
		}catch(HibernateException e){
			 e.printStackTrace();
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		
	}
	
	
	@Override
	public boolean isSourcePort(long portId) throws DataAccessException{		//old name isPortConnected
		boolean ret = false;
		
		try{						
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.add(Restrictions.eq("sourcePowerPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List list = criteria.list();
			
			if(list != null && list.size() > 0)
			{
				ret = true;
			}
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return ret;
	}
	
	@Override
	public boolean isDestinationPort(long portId) throws DataAccessException{		//old name isPortConnected
		boolean ret = false;
		
		try{						
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.add(Restrictions.eq("destPowerPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List list = criteria.list();
			
			if(list != null && list.size() > 0)
			{
				ret = true;
			}
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return ret;
	}

	@Override
	public PowerPort getDestinationPort(long portId)			throws DataAccessException {
		try{						
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.add(Restrictions.eq("sourcePowerPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			PowerConnection conn = (PowerConnection)criteria.uniqueResult();
			
			if(conn != null) return conn.getDestPowerPort();
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;	
	}

	@Override
	public PowerPort getSourcePort(long portId) throws DataAccessException {
		try{						
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.add(Restrictions.eq("sourcePowerPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			PowerConnection conn = (PowerConnection)criteria.uniqueResult();
			
			if(conn != null) return conn.getSourcePowerPort();
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;	
	}

	@Override
	public boolean areConnectorsCompatible(ConnectorLkuData srcConnector, ConnectorLkuData dstConnector) {
		boolean retCode = false;

		
		if(srcConnector != null && dstConnector != null){
			if(srcConnector.getConnectorId() == dstConnector.getConnectorId()){
				retCode = true;
			}
			else{
				Criteria criteria = this.getSession().createCriteria(ConnectorCompat.class);
				criteria.add(Restrictions.eq("connectorLookup", srcConnector));
				criteria.add(Restrictions.eq("connector2Lookup", dstConnector));
				criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				
				List recList = criteria.list();

				if(recList != null && recList.size() > 0){
					retCode = true;
				}
				else{ //switch c1 and c2
					criteria = this.getSession().createCriteria(ConnectorCompat.class);
					criteria.add(Restrictions.eq("connectorLookup", dstConnector));
					criteria.add(Restrictions.eq("connector2Lookup", srcConnector));
					criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
					
					recList = criteria.list();

					if(recList != null && recList.size() > 0){
						retCode = true;
					}					
				}
			}
		}
		
		return retCode;
	}
	
	@Override
	public void migratePowerCircuit() {
		Session session = this.getSession();
		
		// migrate the power circuits using the new port's power chain
		Query query = session.getNamedQuery("dcMigratePowerCircuit");
		
		query.uniqueResult();
		

	}
	
	@Override
	public void completeImportPowerCircuit() {
		Session session = this.getSession();
		
		// migrate the power circuits using the new port's power chain
		Query query = session.getNamedQuery("dcImportCompletePowerCircuit");
		
		query.uniqueResult();
		

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PowerConnection> getConnBetweenPortSubclass(Long srcPortSubclass, Long dstPortSubclass) {
		Criteria criteria = this.getSession().createCriteria(this.type);
		if (null != srcPortSubclass) {
			criteria.createAlias("sourcePowerPort", "srcPort");
			criteria.createAlias("srcPort.portSubClassLookup", "srcPowerPortSubClass");
			/*criteria.createAlias("srcPort.voltsLookup", "srcPortVolts");
			criteria.createAlias("srcPort.phaseLookup", "srcPortPhase");
			criteria.createAlias("srcPort.connectorLookup", "srcPortConnector");*/

			criteria.add(Restrictions.eq("srcPowerPortSubClass.lkpValueCode", srcPortSubclass) );
			criteria.add(Restrictions.isNotNull("srcPort.voltsLookup"));
			criteria.add(Restrictions.isNotNull("srcPort.phaseLookup"));
			criteria.add(Restrictions.isNotNull("srcPort.connectorLookup"));
		}

		if (null != dstPortSubclass) {
			criteria.createAlias("destPowerPort", "dstPort");
			criteria.createAlias("dstPort.portSubClassLookup", "dstPowerPortSubClass");
			/*criteria.createAlias("dstPort.voltsLookup", "dstPortVolts");
			criteria.createAlias("dstPort.phaseLookup", "dstPortPhase");
			criteria.createAlias("dstPort.connectorLookup", "dstPortConnector");*/

			criteria.add(Restrictions.eq("dstPowerPortSubClass.lkpValueCode", dstPortSubclass) );
			criteria.add(Restrictions.isNotNull("dstPort.voltsLookup"));
			criteria.add(Restrictions.isNotNull("dstPort.phaseLookup"));
			criteria.add(Restrictions.isNotNull("dstPort.connectorLookup"));

		}
		
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		return  criteria.list();

	}

	@Override
	public void setConnectionStatus(long connectionId, long connectionStatus) {
		
		Session session = this.getSession();

		PowerConnection  powerConn = (PowerConnection)session.get(PowerConnection.class, connectionId);

        if(powerConn != null){
        	powerConn.setStatusLookup(SystemLookup.getLksData(session, connectionStatus));
            session.update(powerConn);
            session.flush();
        }
        
	}

	@Override
	public List<PowerConnection> getConnectionsForSourcePort(Long portId) {
		if(portId == null || portId.intValue() == 0) return null;
		
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.createAlias("sourcePowerPort", "srcPort");
		criteria.add(Restrictions.eq("srcPort.portId", portId) );
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		return criteria.list();
	}
	
	@Override
	public List<PowerConnection> getConnectionForDestPort(Long portId) {
		if(portId == null || portId.intValue() == 0) return null;
		
		Criteria criteria = this.getSession().createCriteria(this.type);
		criteria.createAlias("destPowerPort", "destPort");
		criteria.add(Restrictions.eq("destPort.portId", portId) );
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		return criteria.list();
	}

}	