/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


import com.raritan.tdz.circuit.dao.PowerCircuitFinderDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.home.PowerProc;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerCapacityValidator implements Validator {
	@Autowired
	PowerCircuitFinderDAO powerCircuitFinderDao;
	

	@Autowired
	PowerProc powerProcHome;
	
	@Autowired
	PowerPortDAO powerPortDao;
	
	@Autowired
	PowerConnDAO powerConnDAO;
	
	private Logger log = Logger.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// For now this will support ItItem. In the future there may be other
		// items that needs to validate capacity and we could enhance this.
		return ItItem.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object itemDomainObject = targetMap.get(errors.getObjectName());
		
		Item item = (Item)itemDomainObject;
		
		if (item.getItemId() > 0){
			for (PowerPort pp: item.getPowerPorts()){
				//If this is a newly added port skip it.
				if (pp.getPortId() == null || pp.getPortId() <= 0) continue;
				
				List<PowerCircuit> powerCircuitList = powerCircuitFinderDao.fetchPowerCircuitForStartPort(pp.getPortId());
				
			
				
				try {
					//TODO: There may be room for improvement. Please see the following note.
					//NOTE: As the item is loaded outside the hibernate session while performing validations, we will 
					//ensure to load the powerPort from database and copy the powerInfo that way it is available during
					//calculation of power during validation of power capacity. We copy only those values that user can edit
					copyPowerInfo(pp);
					if (powerCircuitList != null && powerCircuitList.size() > 0){
						PowerCircuit powerCircuit = powerCircuitList.get(0);
						
						//Fill up the transient collection of power connections by 
						//parsing the circuitTrace list.
						List<Long> connectionIds = powerCircuit.getConnListFromTrace();
						if (connectionIds != null && connectionIds.size() > 0){
							List<PowerConnection> connectionList = new ArrayList<PowerConnection>();
							for (Long connectionId:connectionIds){
								connectionList.add(powerConnDAO.read(connectionId));
							}
							powerCircuit.setCircuitConnections(connectionList);
						}
						
						powerProcHome.setupPowerSupplyAmpsValue(powerCircuit);
						//We need to flush here since the power calculations are getting the amps from database.
						powerPortDao.getSession().flush();
						List<String> cirerrors = powerProcHome.validatePowerCircuit(powerCircuitList.get(0));
						if (cirerrors.size() > 0){
							//Collect errors and put that into the errors object
							getErrors(pp, errors);
						}
					}
					
				} catch (DataAccessException e){
					if (log.isDebugEnabled())
						e.printStackTrace();
					Object errArgs[] = { "An internal system error occured. Please contact Raritan Technical Support" };
					//Capture this error as database access error
					errors.reject("PortValidator.powerCapacityExceeds",errArgs,"Internal error occured during validaton of capacity");
				}
			}
		}
		
	}

	private void copyPowerInfo(PowerPort pp) {
		PowerPort ppFromDB = powerPortDao.read(pp.getPortId());
		if (null == ppFromDB) {
			return;
		}
		ppFromDB.setVoltsLookup(pp.getVoltsLookup());
		ppFromDB.setPowerFactor(pp.getPowerFactor());
		ppFromDB.setWattsBudget(pp.getWattsBudget());
		ppFromDB.setWattsNameplate(pp.getWattsNameplate());
	}

	private void getErrors(PowerPort pp, Errors errors) {
			Object errArgs[] = { pp.getPortName() };
			
			errors.reject("PortValidator.powerCapacityExceeds", errArgs, "The power capacity for the connected circuit exceeds");
	}

}
