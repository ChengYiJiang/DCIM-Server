package com.raritan.tdz.unit.circuit.connection;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.DataConnDAO;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.exception.DataAccessException;

public class DataConnExpectationsImpl implements DataConnExpectations {
	@Autowired
	DataConnDAO connDAO;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataConnection)
	 */
	public void createRead(Mockery jmockContext,  final Long connectionId, final DataConnection retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).read(with(connectionId));will(returnValue(retValue));
		  }});
		  
		  
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createOneOfRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataConnection)
	 */
	public void createOneOfRead(Mockery jmockContext,  final Long connectionId, final DataConnection retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).read(with(connectionId));will(returnValue(retValue));
		  }});
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createGetConn(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataConnection)
	 */
	public void createGetConn(Mockery jmockContext,  final Long connectionId, final DataConnection retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).getConn(with(connectionId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createLoadConn(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataConnection)
	 */
	public void createLoadConn(Mockery jmockContext,  final Long connectionId, final DataConnection retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).loadConn(with(connectionId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createLoadConn(org.jmock.Mockery, java.lang.Long, java.lang.Boolean, com.raritan.tdz.domain.DataConnection)
	 */
	public void createLoadConn(Mockery jmockContext,  final Long connectionId, final Boolean readOnly, final DataConnection retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).loadConn(with(connectionId),with(readOnly));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createGetConnsForItem(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createGetConnsForItem(Mockery jmockContext,  final Long itemId, final List<DataConnection> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).getConnsForItem(with(itemId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createIsSourcePort(org.jmock.Mockery, java.lang.Long, java.lang.Boolean)
	 */
	public void createIsSourcePort(Mockery jmockContext,  final Long portId, final Boolean retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).isSourcePort(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createIsDestinationPort(org.jmock.Mockery, java.lang.Long, java.lang.Boolean)
	 */
	public void createIsDestinationPort(Mockery jmockContext,  final Long portId, final Boolean retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).isDestinationPort(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createGetPanelToPanelConn(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataConnection)
	 */
	public void createGetPanelToPanelConn(Mockery jmockContext,  final Long portId, final DataConnection retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).getPanelToPanelConn(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnExpectations#createIsLogicalConnectionsExist(org.jmock.Mockery, java.lang.Long, java.lang.Long, com.raritan.tdz.domain.DataConnection)
	 */
	public void createIsLogicalConnectionsExist(Mockery jmockContext,  final Long sourceItemId, final Long destItemId, final DataConnection retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).isLogicalConnectionsExist(with(sourceItemId),with(destItemId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
