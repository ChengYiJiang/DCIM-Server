package com.raritan.tdz.powerchain.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.events.dao.EventDAO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.logger.PowerChainLogger;

public class PowerChainUpdateAllPortsAndConnectionsPowerChainActionHandler
		implements PowerChainActionHandler {

	@Autowired(required=true)
	private ItemDAO itemDAO;

	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;

	@Autowired(required=true)
	private FPDUBreakerToUPSBankBreakerUpdateActionHandler floorPduBreakerPortToUpsUpdateConnectionActionHandler;
	
	@Autowired(required=true)
	private PowerPanelBreakerPortActionHandler powerPanelBreakerPortActionHandler;
	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Autowired(required=true)
	private PowerConnDAO powerConnDAO;
	
	@Autowired(required=true)
	private EventDAO eventDAO;
	
	private PowerChainLogger powerChainLogger; 

	public void setPowerChainLogger(PowerChainLogger powerChainLogger) {
		this.powerChainLogger = powerChainLogger;
	}

	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		migrationInProgress = true;
		
		eventDAO.generatePowerChainMigrationStartEvent(data1);
		
		try {
			
			// CREATE CONNECTIONS FOR UPS to Floor PDU
			Map<Long, Long> floorPduWithUpsBank = itemDAO.getAllFloorPDUWithUPSBank();
			for (Map.Entry<Long, Long> entry: floorPduWithUpsBank.entrySet()) {
				floorPduBreakerPortToUpsUpdateConnectionActionHandler.process(entry.getKey(), entry.getValue().toString(), null, errors, validateConn, migrationInProgress);
			
			}
							
			// CREATE CONNECTIONS BETWEEN PANEL AND PDU
			List<Long> powerPanels = itemDAO.getAllPowerPanelItemIds();
			for (Long powerPanel: powerPanels) {
				powerPanelBreakerPortActionHandler.process(powerPanel, null, null, errors, validateConn, migrationInProgress);
	
			}
			
			
			if (data1 != null && data1.equals("import")) {
				// complete the circuits imported
				powerConnDAO.completeImportPowerCircuit();
			}
			else {
				// migrate power circuits
				powerConnDAO.migratePowerCircuit();
			}
		
		} finally {
			
			powerChainErrors.addAllErrors(errors);
			
			eventDAO.generatePowerChainMigrationEndEvent(null, data1);
			
			if (data1 == null || !data1.equals("import")) { // Do not run the diagnostics for the import operation
				powerPortDAO.runPowerPortDiagnostics();
			}
			
			if (null != powerChainLogger) {
				powerChainLogger.log(powerChainErrors);
			}
			
		}
		
	}
	
}
