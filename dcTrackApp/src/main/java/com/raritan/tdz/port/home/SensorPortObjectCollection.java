package com.raritan.tdz.port.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.port.dao.SensorPortDAO;


public class SensorPortObjectCollection extends PortObjectCollection {
	private static long MAX_NUM_OF_SENSOR_PORTS = 9999L;
	
    @Autowired(required=true)
    private PortCollectionHelper<SensorPort> sensorPortObjectCollectionHelper;

	/*@Autowired
	private SensorPortHelper sensorPortHelper;*/
	
	@Autowired(required=true)
	SensorPortDAO sensorPortDAO;
	
	/*@Autowired(required=true)
	Validator sensorPortsCabinetValidator;*/


	public SensorPortObjectCollection(IPortObjectFactory portObjectFactory) {
		super(portObjectFactory);
	}

	@Override
	public void init(Object item, Errors errors) {
		setItem((Item) item);
		ports = sensorPortObjectCollectionHelper.init(item, portObjectFactory, errors);
	}
	
    @Override
    public List<Long> getDeleteIds() {
        // get deleted power port ids
        return sensorPortObjectCollectionHelper.getDeleteIds(item);
    }
    
    @Override
    public void deleteInvalidPorts(Errors errors) {
    	/* 
    	 * Nothing to do for sensor ports for now
   		 * We do not examine connector as in case of Data/Power ports
     	 */
    }
	
    private void validatePortLimit(Errors errors) {
		if (ports.size() > MAX_NUM_OF_SENSOR_PORTS) {
			Object errorArgs[]  = { MAX_NUM_OF_SENSOR_PORTS };
			errors.rejectValue("tabSensorPorts", "PortValidator.sensorPortMaxNumOfPorts", errorArgs, "Cannot create more than " + MAX_NUM_OF_SENSOR_PORTS + " Sensor ports");
		}
	}
	
	@Override
	public void validate(Errors errors) {
		super.validate(errors);
		
		// validate port limit
		validatePortLimit(errors);
		
	}

	@Override
	public void validateSortOrder(Errors errors) {
		List<String> nonUniquePortTypes = sensorPortObjectCollectionHelper.getPortTypeOfNonUniqueSortOrder(item);
		
		for (String portType: nonUniquePortTypes) {
			Object errorArgs[]  = { portType };
			errors.rejectValue("tabSensorPorts", "PortValidator.sensorPortSortOrderNotUnique", errorArgs, "Sensor Port order is not unique");
		}
	}

	
	@Override
	public IPortObject getDetachedPort(Long portId, Errors errors) {
		SensorPort dp = sensorPortDAO.loadEvictedPort(portId);
		IPortObject po = getPortObjectFactory().getPortObject(dp, errors);
		return po;
	}

	@Override
	protected void validateDelete(Errors errors) {
		//Nothing to do
	}
	
	@Override
	protected void updateSortOrder(Errors errors) {
		//Order sensors by index based on subclass
		sensorPortObjectCollectionHelper.updateSortOrderByPortSubclass(item/*, itemDAO*/, errors);
	}

	@Override
	public void preValidateUpdates(Errors errors) {
		updateSortOrder(errors);
		for (IPortObject port: ports) {
			port.preValidateUpdates(errors);
		}
	}
	
	@Override
	protected void validateSave(Errors errors) {
		//validate user did not send dup cabinets and he selected only available cabinets
		// sensorPortsCabinetValidator.validate(item, errors);
		for (IPortObject port: ports) {
			port.validateSave(item, errors);
		}
	}
}
