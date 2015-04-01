package com.raritan.tdz.request.home;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitDelete;
import com.raritan.tdz.circuit.home.CircuitSearch;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;

public class DisconnectWorkOrderComplete implements RequestStageHelper {

	@Autowired
	private CircuitDelete circuitDelete;
	
	@Autowired
	private CircuitSearch circuitSearch;
	
	@Autowired
	private CircuitRequest circuitRequest;
	
	@Autowired
	private RequestDAO requestDAO;
	

	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws Throwable {
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		List<Long> circuitIdsToBeDeleted = new ArrayList<Long>();
		
		for(RequestPointer p:request.getRequestPointers()){
			if(p.getSortOrder().equals(1)){
				
				if(p.getTableName().equals("dct_ports_data")){
					cCriteria.setCircuitType(SystemLookup.PortClass.DATA);
					cCriteria.setStartPortId(p.getRecordId());
				}
				else if(p.getTableName().equals("dct_ports_power")){
					cCriteria.setStartPortId(p.getRecordId());
					cCriteria.setCircuitType(SystemLookup.PortClass.POWER);
				}
				else if(p.getTableName().equals("dct_circuits_data")){
					cCriteria.setCircuitId(CircuitUID.getCircuitUID(p.getRecordId(), SystemLookup.PortClass.DATA));
					cCriteria.setCircuitType(SystemLookup.PortClass.DATA);
				}else{
					cCriteria.setCircuitId(CircuitUID.getCircuitUID(p.getRecordId(), SystemLookup.PortClass.POWER));
					cCriteria.setCircuitType(SystemLookup.PortClass.POWER);					
				}
							
				List<CircuitViewData> circuitViewDataList = circuitSearch.searchCircuitsRaw(cCriteria); // BUG: always return empty list
				for(CircuitViewData circuit:circuitViewDataList){
					circuitIdsToBeDeleted.add(circuit.getCircuitId());
				
					// Archive the item associated with the circuit
					requestDAO.itemArchived(request, userInfo);
					
					// Archive the circuit for future reference 
					requestDAO.circuitArchived(circuit.getCircuitListId(), request, userInfo);
					
					circuitRequest.archiveWorkOrder(request, circuit, userInfo);
					
					if(circuit.isDataCircuit()){
						circuitDelete.deleteDataCircuitByIds(circuitIdsToBeDeleted, false);
					}
					else{
						circuitDelete.deletePowerCircuitByIds(circuitIdsToBeDeleted, false);
					}
					circuitIdsToBeDeleted.clear();
				}				
			}			
		}
	}

}
