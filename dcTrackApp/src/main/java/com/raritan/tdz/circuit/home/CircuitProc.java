package com.raritan.tdz.circuit.home;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.ConnectionToMove;
import com.raritan.tdz.domain.ConnectorCompat;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class CircuitProc {
	Session session = null;
	
	public CircuitProc(Session session){
		this.session = session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/*
	public boolean isConnUseInCircuit(long connectionId, long portClass) throws DataAccessException{		
		boolean ret = false;
		Criteria criteria;
		
		try{						
			if(portClass == com.raritan.tdz.lookup.SystemLookup.PortClass.POWER){
				criteria = session.createCriteria(PowerCircuit.class);
			}
			else{
				criteria = session.createCriteria(DataCircuit.class);
			}
			
			cr.add(Restrictions.like("circuitTrace", "%," + connectionId + ",%"));
			
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
	*/
	public boolean isPortConnected(long portId, long portClass) throws DataAccessException{		
		boolean ret = false;
		Criteria criteria;
		
		try{						
			if(portClass == com.raritan.tdz.lookup.SystemLookup.PortClass.POWER){
				criteria = session.createCriteria(PowerConnection.class);
				criteria.add(Restrictions.eq("sourcePowerPort.portId", portId) );
			}
			else{
				criteria = session.createCriteria(DataConnection.class);
				criteria.add(Restrictions.eq("sourceDataPort.portId", portId) );
			}
			
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

	
	public boolean isPortUsed(long portId, long portClass) throws DataAccessException{		
		boolean ret = false;
		Criteria criteria;
		
		try{									
			if(portClass == com.raritan.tdz.lookup.SystemLookup.PortClass.POWER){
				criteria = session.createCriteria(PowerPort.class);
				
			}
			else{
				criteria = session.createCriteria(DataPort.class);
			}

			criteria.add(Restrictions.eq("portId", portId));
			criteria.add(Restrictions.eq("used", true));
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
	
	public List<Long> getCircuitIds(long itemId, long portClass) throws DataAccessException{		
		List<Long> ids = new ArrayList<Long>();
		String temp = "";			
		Criteria criteria;
		
		try{						
			
			if(portClass == com.raritan.tdz.lookup.SystemLookup.PortClass.POWER){
				criteria = session.createCriteria(PowerConnection.class);
				criteria.createAlias("sourcePowerPort", "port");
				criteria.add(Restrictions.eq("port.item.itemId", itemId) );
				criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				
				List list = criteria.list();
				
				if(list != null && list.size() > 0)
				{
					for(int i=0; i<list.size(); i++){
						PowerConnection obj = (PowerConnection)list.get(i);
						
						if(obj.getCircuitPowerId() != null){
							ids.add(obj.getCircuitPowerId());
						}
					}
				}
			}
			else{
				criteria = session.createCriteria(DataConnection.class);
				criteria.createAlias("sourceDataPort", "port");
				criteria.add(Restrictions.eq("port.item.itemId", itemId) );
				criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				
				List list = criteria.list();
				
				if(list != null && list.size() > 0)
				{
					for(int i=0; i<list.size(); i++){
						DataConnection obj = (DataConnection)list.get(i);
						
						if(obj.getCircuitDataId() != null){
							ids.add(obj.getCircuitDataId());
						}
					}
				}
			}			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return ids;
	}
	
	public Object getPortConnection(long portId, long portClass, boolean useSourcePort) throws DataAccessException{		
		Criteria criteria;
		
		try{
			String portDesc;
			
			if(useSourcePort){
				portDesc = "source";
			}
			else{
				portDesc = "dest";
			}
			
			if(portClass == com.raritan.tdz.lookup.SystemLookup.PortClass.POWER){
				criteria = session.createCriteria(PowerConnection.class);				
				criteria.add(Restrictions.eq(portDesc + "PowerPort.portId", portId) );
			}
			else{
				criteria = session.createCriteria(DataConnection.class);
				criteria.add(Restrictions.eq(portDesc + "DataPort.portId", portId) );
			}
			
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			List list = criteria.list();
			
			//should get one record, for shared connections, return null
			if(list != null && list.size() == 1)			{
				return list.get(0);
			}
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}	
		
	public boolean checkConnectors(ConnectorLkuData c1, ConnectorLkuData c2)
	{
		boolean retCode = false;
		
		if(c1 != null && c2 != null){
			if(c1.getConnectorId() == c2.getConnectorId()){
				retCode = true;
			}
			else{
				Criteria criteria = session.createCriteria(ConnectorCompat.class);
				criteria.add(Restrictions.eq("connectorLookup", c1));
				criteria.add(Restrictions.eq("connector2Lookup", c2));
				criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				
				List recList = criteria.list();

				if(recList != null && recList.size() > 0){
					retCode = true;
				}
				else{ //switch c1 and c2
					criteria = session.createCriteria(ConnectorCompat.class);
					criteria.add(Restrictions.eq("connectorLookup", c2));
					criteria.add(Restrictions.eq("connector2Lookup", c1));
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
	
	
}
