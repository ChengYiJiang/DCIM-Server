package com.raritan.tdz.request.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitSearch;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;

public class CircuitStatusWorkOrderComplete implements RequestStageHelper {

    @Autowired(required=true)
    CircuitSearch circuitSearch;

    @Autowired(required=true)
    CircuitRequest circuitRequest;

	@Autowired
	protected ItemDAO itemDAO;

    
	private Long status;

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public CircuitStatusWorkOrderComplete(Long status) {
		super();
		this.status = status;
	}

	public CircuitStatusWorkOrderComplete() {
		super();
	}

	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws Throwable {
		
		setPowerStatusForCircuit(request);
		setCircuitStatus(request);
	}

	private void setPowerStatusForCircuit(Request request) throws Throwable{
		if(request.isPowerOffReq() || RequestLookup.installRequests.contains(request.getRequestTypeLookup().getLkpValueCode())){
			List<CircuitCriteriaDTO> idsList = itemDAO.getAssociatedCircuitsForItem(request.getItemId());
			
			for(CircuitCriteriaDTO cr:idsList){
				List<CircuitViewData> cirList = circuitSearch.searchCircuitsRaw(cr);
	
				for(CircuitViewData cir:cirList){
					if(cir.isStatusPlanned()) continue;
	
					circuitRequest.updateCircuitStatus(cir, status);
				}
			}
			return;
		}
	}

	private void setCircuitStatus(Request request) throws Throwable{
		if(request.isConnectReq()){
			for (RequestPointer rp: request.getRequestPointers() ) {
				circuitRequest.updateCircuitStatus(rp.getRecordId(), rp.getTableName(), status);
			}
		}
	}

}