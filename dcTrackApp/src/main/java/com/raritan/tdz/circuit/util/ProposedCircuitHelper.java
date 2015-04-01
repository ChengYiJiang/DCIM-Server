package com.raritan.tdz.circuit.util;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.domain.CircuitUID;


/**
 * Helper class to deal with circuit Ids and proposed circuit Ids coming in various
 * DTOs from the client.
 * @author Andrew Cohen
 */
public class ProposedCircuitHelper {

	/**
	 * Examine the circuit criteria DTO sent by the client and determine if
	 * we are operating on a proposed circuit or not
	 * @param cCriteria the circuit criteria
	 * @return proposed circuit ID or 0 if no proposed circuit is referenced
	 */
	public static long getProposedCircuitId(CircuitCriteriaDTO cCriteria) {
		if (cCriteria == null) return 0;
		final Long proposedCircuitId = cCriteria.getProposeCircuitId();
		
		// See if client specifically passed a proposed circuitId
		if (proposedCircuitId != null && proposedCircuitId > 0) {
			return proposedCircuitId;
		}
		
		// Client did not pass a proposed circuit ID, but the value
		// in the circuitId field may in fact represent a proposed circuit!
		final CircuitUID circuitUID = cCriteria.getCircuitUID();
		if (circuitUID.isProposedCircuit()) {
			return circuitUID.getCircuitDatabaseId();
		}
		
		return 0;
	}
	
	/**
	 * Examine the circuit DTO sent by the client and determine if
	 * we are operating on a proposed circuit or not
	 * @param circuit the circuit DTO
	 * @return proposed circuit ID or 0 if no proposed circuit is referenced
	 */
	public static long getProposedCircuitId(CircuitDTO circuit) {
		if (circuit == null) return 0;
		final Long proposedCircuitId = circuit.getProposeCircuitId();
		
		// See if client specifically passed a proposed circuitId
		if (proposedCircuitId != null && proposedCircuitId > 0) {
			return proposedCircuitId;
		}
		
		// Client did not pass a proposed circuit ID, but the value
		// in the circuitId field may in fact represent a proposed circuit!
		final CircuitUID circuitUID = circuit.getCircuitUID();
		if (circuitUID.isProposedCircuit()) {
			return circuitUID.getCircuitDatabaseId();
		}
		
		return 0;
	}
}
