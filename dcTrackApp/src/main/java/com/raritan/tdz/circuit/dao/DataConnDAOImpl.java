package com.raritan.tdz.circuit.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.ConnectorCompat;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class DataConnDAOImpl extends DaoImpl<DataConnection> implements DataConnDAO  {

	@Autowired(required=true)
	private LksCache lksCache;
	
	public DataConnDAOImpl() {
		
	}	

	@Override
	public DataConnection getConn(Long id) {
		return this.read(id);
	}

	@Override
	public DataConnection loadConn(Long id){
		return loadConn(id, false);
	}

	@Override
	public DataConnection loadConn(Long id, boolean readOnly){
		DataConnection result = null;
		Session session = null;

		try {
			if (id != null && id > 0){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(this.type);
				//We are doing an EAGER loading since we will be filling the data with what
				//client sends and none of them should be null;
				criteria.add(Restrictions.eq("dataConnectionId", id));
				criteria.setReadOnly(readOnly);
				criteria.setFetchMode("sourceDataPort", FetchMode.JOIN);
				criteria.setFetchMode("destDataPort", FetchMode.JOIN);
				criteria.setFetchMode("connectionCord", FetchMode.JOIN);
				criteria.setFetchMode("statusLookup", FetchMode.JOIN);
				criteria.setFetchMode("connectionType", FetchMode.JOIN);
				
				result = (DataConnection) criteria.uniqueResult();
			}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}
		
	
	@Override
	public List<DataConnection> getConnsForItem(long itemId) throws DataAccessException {
		try{
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.createAlias("sourceDataPort", "port");
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
	public DataConnection getPanelToPanelConn(Long portId) throws DataAccessException{		
		try{
			Session session = this.getSession();
			
			Criteria criteria = session.createCriteria(this.type);
			criteria.add(Restrictions.eq("destDataPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List list = criteria.list();
			

			for(Object obj:list){
				DataConnection conn = (DataConnection)obj;
				
				if(conn.isLinkTypeImplicit()){
					return conn;
				}
			}	
						
			//should get one record, for shared connections, return null
			criteria = session.createCriteria(this.type);
			criteria.add(Restrictions.eq("sourceDataPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			list = criteria.list();
			
			for(Object obj:list){
				DataConnection conn = (DataConnection)obj;
				
				if(conn.isLinkTypeImplicit()){
					return conn;
				}
			}			
		}catch(HibernateException e){
			 e.printStackTrace();
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
    public boolean isLogicalConnectionsExist(long sourceItemId, long destItemId) throws DataAccessException {
            Session session = this.getSession();
            
            Criteria criteria = session.createCriteria(this.type);
            criteria.createAlias("sourceDataPort", "portSrc");
            criteria.createAlias("portSrc.item", "itemSrc");
            criteria.createAlias("destDataPort", "portDest");
            criteria.createAlias("portDest.item", "itemDest");
            criteria.createAlias("portSrc.portSubClassLookup", "sourceSubClass");
            
            criteria.add(Restrictions.eq("itemSrc.itemId", sourceItemId));
            criteria.add(Restrictions.eq("itemDest.itemId", destItemId));
            criteria.add(Restrictions.eq("sourceSubClass.lkpValueCode", SystemLookup.PortSubClass.LOGICAL));
            
            criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
                        
            List<DataConnection> connList = criteria.list();
            
            return (null !=  connList && connList.size() > 0);
    }
	
	@Override
	public boolean isSourcePort(long portId) throws DataAccessException{		//old name isPortConnected
		boolean ret = false;
		
		try{						
			Criteria criteria = this.getSession().createCriteria(this.type);
			criteria.add(Restrictions.eq("sourceDataPort.portId", portId) );
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
			criteria.add(Restrictions.eq("destDataPort.portId", portId) );
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

	// TODO: change this template because we have identical implementation in powerConnDAO for power circuit.
	@Override
	public void setConnectionStatus(long connectionId, long connectionStatus) {
		
		Session session = this.getSession();

		DataConnection  dataConn = (DataConnection)this.getSession().get(DataConnection.class, connectionId);

        if(dataConn != null){
        	// dataConn.setStatusLookup(SystemLookup.getLksData(session, connectionStatus));
        	dataConn.setStatusLookup(lksCache.getLksDataUsingLkpCode(connectionStatus));
            session.update(dataConn);
            session.flush();
        }		
		
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
	public DataConnection getPortConnection(long portId,  boolean isSourcePort) throws DataAccessException{		
		Criteria criteria;
		
		try{
			String portDesc = "source";
			
			if(!isSourcePort){
			
				portDesc = "dest";
			}
			
			criteria = this.getSession().createCriteria(DataConnection.class);
			criteria.add(Restrictions.eq(portDesc + "DataPort.portId", portId) );	
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<DataConnection> list = criteria.list();
			
			//should get one record, for shared connections, return null
			if(list != null && list.size() == 1)			{
				return (DataConnection)list.get(0);
			}
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}		
}	