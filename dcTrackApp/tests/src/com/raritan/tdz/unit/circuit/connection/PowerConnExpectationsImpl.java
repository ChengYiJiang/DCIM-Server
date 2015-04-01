package com.raritan.tdz.unit.circuit.connection;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;

public class PowerConnExpectationsImpl implements PowerConnExpectations {
	@Autowired
	PowerConnDAO connDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerConnection)
	 */
	public void createRead(Mockery jmockContext,  final Long connectionId, final PowerConnection retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).read(with(connectionId));will(returnValue(retValue));
		  }});
		  
		  
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createOneOfRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerConnection)
	 */
	public void createOneOfRead(Mockery jmockContext,  final Long connectionId, final PowerConnection retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).read(with(connectionId));will(returnValue(retValue));
		  }});
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createGetConn(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerConnection)
	 */
	public void createGetConn(Mockery jmockContext,  final Long connectionId, final PowerConnection retValue){
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
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createLoadConn(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerConnection)
	 */
	public void createLoadConn(Mockery jmockContext,  final Long connectionId, final PowerConnection retValue){
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
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createLoadConn(org.jmock.Mockery, java.lang.Long, java.lang.Boolean, com.raritan.tdz.domain.PowerConnection)
	 */
	public void createLoadConn(Mockery jmockContext,  final Long connectionId, final Boolean readOnly, final PowerConnection retValue){
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
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createGetConnsForItem(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createGetConnsForItem(Mockery jmockContext,  final Long itemId, final List<PowerConnection> retValue){
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
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createIsSourcePort(org.jmock.Mockery, java.lang.Long, java.lang.Boolean)
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
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createIsDestinationPort(org.jmock.Mockery, java.lang.Long, java.lang.Boolean)
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
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createGetSourcePort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerPort)
	 */
	public void createGetSourcePort(Mockery jmockContext,  final Long portId, final PowerPort retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).getSourcePort(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createGetDestinationPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerPort)
	 */
	public void createGetDestinationPort(Mockery jmockContext,  final Long portId, final PowerPort retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				allowing(connDAO).getDestinationPort(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createAreConnectorsCompatible(org.jmock.Mockery, com.raritan.tdz.domain.ConnectorLkuData, com.raritan.tdz.domain.ConnectorLkuData, boolean)
	 */
	public void createAreConnectorsCompatible(Mockery jmockContext,  final ConnectorLkuData srcConnector, final ConnectorLkuData dstConnector, final boolean retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).areConnectorsCompatible(with(srcConnector),with(dstConnector));will(returnValue(retValue));
		  }});		  		  
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createGetConnBetweenPortSubclass(org.jmock.Mockery, java.lang.Long, java.lang.Long, java.util.List)
	 */
	public void createGetConnBetweenPortSubclass(Mockery jmockContext,  final Long srcPortSubclass, final Long dstPortSubclass, final List<PowerConnection> retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).getConnBetweenPortSubclass(with(srcPortSubclass),with(dstPortSubclass));will(returnValue(retValue));
		  }});		  		  
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createCompleteImportPowerCircuit(org.jmock.Mockery)
	 */
	public void createCompleteImportPowerCircuit(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).completeImportPowerCircuit();
		  }});		  		  
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.PowerConnExpectations#createMigratePowerCircuit(org.jmock.Mockery)
	 */
	public void createMigratePowerCircuit(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(connDAO).migratePowerCircuit();
		  }});		  		  
	}	
}
