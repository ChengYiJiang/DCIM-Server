package com.raritan.tdz.unit.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

public class DataCircuitExpectationsImpl implements DataCircuitExpectations {
	@Autowired
	DataCircuitDAO circuitDAO;
	
	@Autowired
	protected Mockery jmockContext;

	public void addExpectations(DataCircuit circuit){
		Long circuitId = circuit.getDataCircuitId();
		List<DataCircuit> recList = new ArrayList<DataCircuit>();
		recList.add(circuit);
			
		createLoadCircuit(jmockContext, circuitId, circuit);
		createRead(jmockContext, circuitId, circuit);
		
		for(Long id:circuit.getConnListFromTrace()){
			createViewDataCircuitByConnId(jmockContext, id, recList);
		}
		
		Long startPortId = circuit.getStartPortId();
		
		createViewDataCircuitByStartPortId(jmockContext, startPortId, recList);
		
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();		
		cCriteria.setCircuitId( CircuitUID.getCircuitUID(circuitId, SystemLookup.PortClass.DATA));
		cCriteria.setCircuitType(SystemLookup.PortClass.DATA);
		createViewDataCircuitByCriteria(jmockContext, cCriteria , recList);
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataCircuit)
	 */
	public void createRead(Mockery jmockContext,  final Long circuitId, final DataCircuit retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).read(with(circuitId));will(returnValue(retValue));
		  }});	    
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createLoadCircuit(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataCircuit)
	 */
	public void createLoadCircuit(Mockery jmockContext,  final Long circuitId, final DataCircuit retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).getDataCircuit(with(circuitId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createViewDataCircuitByConnId(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createViewDataCircuitByConnId(Mockery jmockContext,  final Long connectionId, final List<DataCircuit> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).viewDataCircuitByConnId(with(connectionId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createViewDataCircuitByStartPortId(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createViewDataCircuitByStartPortId(Mockery jmockContext,  final Long portId, final List<DataCircuit> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).viewDataCircuitByStartPortId(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createViewDataCircuitByCriteria(org.jmock.Mockery, com.raritan.tdz.circuit.dto.CircuitCriteriaDTO, java.util.List)
	 */
	public void createViewDataCircuitByCriteria(Mockery jmockContext,  final CircuitCriteriaDTO cCriteria, final List<DataCircuit> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).viewDataCircuitByCriteria(with(cCriteria));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createGetDestinationItemsForItem(org.jmock.Mockery, java.lang.Long, java.util.HashMap)
	 */
	public void createGetDestinationItemsForItem(Mockery jmockContext,  final Long itemId, final  HashMap<Long, PortInterface> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getDestinationItemsForItem(with(itemId));will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.DataCircuitExpectations#createGetProposedCircuitIdsForItem(org.jmock.Mockery, java.lang.Long, java.util.HashMap)
	 */
	public void createGetProposedCircuitIdsForItem(Mockery jmockContext,  final Long itemId, final  HashMap<Long, PortInterface> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getProposedCircuitIdsForItem(with(itemId));will(returnValue(retValue));
		  }});
	}	
}
