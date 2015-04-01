package com.raritan.tdz.unit.circuit;

import java.util.HashMap;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.DataAccessException;

public class PowerCircuitExpectationsImpl implements PowerCircuitExpectations {
	@Autowired
	PowerCircuitDAO circuitDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerCircuit)
	 */
	public void createRead(Mockery jmockContext,  final Long circuitId, final PowerCircuit retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).read(with(circuitId));will(returnValue(retValue));
		  }});	    
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetPowerCircuit(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerCircuit)
	 */
	public void createGetPowerCircuit(Mockery jmockContext,  final Long circuitId, final PowerCircuit retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getPowerCircuit(with(circuitId));will(returnValue(retValue));
		  }});	    
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createLoadCircuit(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerCircuit)
	 */
	public void createLoadCircuit(Mockery jmockContext,  final Long circuitId, final PowerCircuit retValue){
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).getPowerCircuit(with(circuitId));will(returnValue(retValue));
			  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createViewPowerCircuitByConnId(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createViewPowerCircuitByConnId(Mockery jmockContext,  final Long connectionId, final List<PowerCircuit> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).viewPowerCircuitByConnId(with(connectionId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createViewPowerCircuitByStartPortId(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createViewPowerCircuitByStartPortId(Mockery jmockContext,  final Long portId, final List<PowerCircuit> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).viewPowerCircuitByStartPortId(with(portId));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createViewPowerCircuitByCriteria(org.jmock.Mockery, com.raritan.tdz.circuit.dto.CircuitCriteriaDTO, java.util.List)
	 */
	public void createViewPowerCircuitByCriteria(Mockery jmockContext,  final CircuitCriteriaDTO cCriteria, final List<PowerCircuit> retValue){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).viewPowerCircuitByCriteria(with(cCriteria));will(returnValue(retValue));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetDestinationItemsForItem(org.jmock.Mockery, java.lang.Long, java.util.HashMap)
	 */
	public void createGetDestinationItemsForItem(Mockery jmockContext,  final Long itemId, final  HashMap<Long, PortInterface> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getDestinationItemsForItem(with(itemId));will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetNextNodeAmpsForItem(org.jmock.Mockery, java.lang.Long, java.util.HashMap)
	 */
	public void createGetNextNodeAmpsForItem(Mockery jmockContext,  final Long itemId, final  HashMap<Long, PortInterface> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getNextNodeAmpsForItem(with(itemId));will(returnValue(retValue));
		  }});
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetPowerBankInfo(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.circuit.dto.PowerBankInfo)
	 */
	public void createGetPowerBankInfo(Mockery jmockContext,  final Long bankId, final PowerBankInfo retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getPowerBankInfo(with(bankId));will(returnValue(retValue));
		  }});	    
	}	
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetPowerUsage(org.jmock.Mockery, java.lang.String, java.lang.Object[], java.util.List)
	 */
	public void createGetPowerUsage(Mockery jmockContext,  final String queryStr, final Object[] queryArgs, final List<?> retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getPowerUsage(with(queryStr),with(queryArgs));will(returnValue(retValue));
		  }});	    
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetPowerWattUsedSummary(org.jmock.Mockery, java.lang.Long, java.lang.Long, java.lang.Long, java.util.List)
	 */
	public void createGetPowerWattUsedSummary(Mockery jmockContext,  final Long portPowerId, final Long portIdToExclude, final Long fuseLkuId, final Long inputCordToExclude, final List<PowerWattUsedSummary> retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getPowerWattUsedSummary(with(portPowerId),with(portIdToExclude),with(fuseLkuId), with (inputCordToExclude), with(false));will(returnValue(retValue));
		  }});	    
	}	
			
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetPowerWattUsedTotal(org.jmock.Mockery, java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	public void createGetPowerWattUsedTotal(Mockery jmockContext,  final Long portPowerId, final Long fuseLkuId, final Long retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getPowerWattUsedTotal(with(portPowerId), with(fuseLkuId));will(returnValue(retValue));
		  }});	    
	}	
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createChangeCircuitConnectionChange(org.jmock.Mockery, com.raritan.tdz.domain.PowerPort, com.raritan.tdz.domain.PowerPort)
	 */
	public void createChangeCircuitConnectionChange(Mockery jmockContext,  final PowerPort oldPort, final PowerPort newPort){
		  try {
			jmockContext.checking(new Expectations() {{ 
				  allowing(circuitDAO).changeCircuitConnectionChange(with(oldPort),with(newPort));
			  }});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetCircuitsWithTrace(org.jmock.Mockery, java.lang.String, java.util.List)
	 */
	public void createGetCircuitsWithTrace(Mockery jmockContext,  final String trace, final List<PowerCircuit> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getCircuitsWithTrace(with(trace));will(returnValue(retValue));
		  }});
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetCircuitsInfoWithTrace(org.jmock.Mockery, java.lang.String, java.util.List)
	 */
	public void createGetCircuitsInfoWithTrace(Mockery jmockContext,  final String trace, final List<Object[]> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getCircuitsInfoWithTrace(with(trace));will(returnValue(retValue));
		  }});
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetConnAndDestPort(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public void createGetConnAndDestPort(Mockery jmockContext,  final Long portId, final List<Object[]> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getConnAndDestPort(with(portId));will(returnValue(retValue));
		  }});
	}		

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createChangeCircuitTrace(org.jmock.Mockery, java.lang.Long, java.lang.String, java.lang.String, java.lang.Long)
	 */
	public void createChangeCircuitTrace(Mockery jmockContext,  final Long circuitId, final String circuitTrace, final String sharedCircuitTrace, final Long endConnId){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).changeCircuitTrace(with(circuitId),with(circuitTrace),with(sharedCircuitTrace),with(endConnId));
		  }});
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.PowerCircuitExpectations#createGetProposedCircuitIdsForItem(org.jmock.Mockery, java.lang.Long, java.util.HashMap)
	 */
	public void createGetProposedCircuitIdsForItem(Mockery jmockContext,  final Long itemId, final  HashMap<Long, PortInterface> retValue){
		jmockContext.checking(new Expectations() {{ 
			  allowing(circuitDAO).getProposedCircuitIdsForItem(with(itemId));will(returnValue(retValue));
		  }});
	}		
}

