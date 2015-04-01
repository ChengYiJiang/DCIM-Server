package com.raritan.tdz.circuit.home;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.raritan.tdz.domain.ICircuitConnection;

/**
 * Provides a read only view of a proposed circuit to the service layer.
 * @author Andrew Cohen
 */
public class ProposedCircuitInfo {

	private long circuitId;
	private List<ICircuitConnection> connsToUpdate;
	
	ProposedCircuitInfo() {
		circuitId = 0;
		connsToUpdate = new LinkedList<ICircuitConnection>();
	}
	
	public long getCircuitId() {
		return circuitId;
	}
	
	public List<ICircuitConnection> getConnsToUpdate() {
		return Collections.unmodifiableList( connsToUpdate );
	}
	
	void addConnToUpdate(ICircuitConnection conn) {
		connsToUpdate.add( conn );
	}

	void setCircuitId(long circuitId) {
		this.circuitId = circuitId;
	}
}
