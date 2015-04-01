package com.raritan.tdz.port.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.port.dao.SensorPortDAO;

public abstract class SensorPortObject implements IPortObject {
	private long MAX_PORT_LENGTH = 65;
	private long MAX_COMMENTS_LENGTH = 500;
	private long MAX_XYZ_LENGTH = 20;
	
	/* Port instance */
	private IPortInfo port;
	
	/* Saved Port Instance */
	private IPortInfo savedPort;

	/* port object helper */
	@Autowired(required=true)
	PortObjectHelper<SensorPort> sensorPortObjectHelper;

    @Autowired(required=true)
    protected SensorPortDAO sensorPortDAO;

	/** The message source */
	protected ResourceBundleMessageSource messageSource;

	abstract public Set<Long> getPortSubclassLookupValueCodes();
	
	abstract public Set<Long> getItemClassLookupValueCodes();
	
	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	@Override
	public IPortInfo getPortInfo() {
		return port;
	}

	public void setPort(IPortInfo port) {
		this.port = port;
	}

	public IPortInfo getSavedPort() {
		return savedPort;
	}

	public void setSavedPort(IPortInfo savedPort) {
		if (null != port.getPortId() && port.getPortId() > 0) {
			Map<Long, SensorPort> portMap = SavedItemData.getCurrentSensorPorts();
			if (null == portMap) return;
			this.savedPort = SavedItemData.getCurrentSensorPorts().get(port.getPortId());
		}
	}

	@Override
	public void init(IPortInfo port, Errors errors) {
		setPort(port);	
		setSavedPort(port);
		sensorPortObjectHelper.init(port, getPortSubclassLookupValueCodes(), getItemClassLookupValueCodes(), "PortValidator.PortUnsupportedClass", errors);
	}
	
	private void validateInvalidFields(Errors errors) {
		// validate sort order
		sensorPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "sortOrder", "Index", "PortValidator.sensorIncorrectFieldValue", 0L, null);

	}
	
	@Override
	public void validateSave(Object target, Errors errors) {
		// validate supporting item class:subclass
		validateItemClassSubclass(target, errors);
		
		validateRequiredFields( errors );
		
		validateEdit(errors);
		
		validateFieldsLength( errors );

		validateSensorLocation( errors );
		
		// valid field values
		validateInvalidFields(errors);

	}

	private void validateSensorLocation( Errors errors ) {
		SensorPort sensorPort = getSensorPort();
		if( sensorPort.getIsInternal() == null){
			//If isInternal is not set, disregard all location info
			sensorPort.setCabinetItemId( null );
			sensorPort.setCabinetItem(null);
			sensorPort.setCabLocationLookup(null);
			sensorPort.setXyzLocation(null);
		}else if( sensorPort.getIsInternal() == true ){
			//Internal sensor, disregard XYZ
			sensorPort.setXyzLocation(null);
		}else {
			//external sensor, disregard cabinet and position
			sensorPort.setCabinetItemId( null );
			sensorPort.setCabinetItem(null);
			sensorPort.setCabLocationLookup(null);
		}
	}

	@Override
	public void validateItemClassSubclass(Object target, Errors errors) {
		Item item = null;
		if ((null != target) && (target instanceof Item)) {
			item = (Item) target;
		}
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		List<Long> supportingItemClassMountingFormFactor = new ArrayList<Long>();
		supportingItemClassMountingFormFactor.add(701L); // Probe Rackable
		supportingItemClassMountingFormFactor.add(702L); // Probe Non-Reckable
		supportingItemClassMountingFormFactor.add(706L); // Probe ZeroU
		supportingItemClassMountingFormFactor.add(501L); // RackPDU Rackable Fixed
		supportingItemClassMountingFormFactor.add(502L); // RackPDU Non-Rackable
		supportingItemClassMountingFormFactor.add(506L); // Rackable ZeroU
		
		if (!supportingItemClassMountingFormFactor.contains(classMountingFormFactorValue)) {
			Object[] errorArgs = {};
			errors.rejectValue("tabSensorPorts", "PortValidator.sensorPortUnsupportedClass", errorArgs, 
					"Sensor port can only be created for Rack PDU and Probe item");
		}
	}

	protected SensorPort getSensorPort() {
		
		return sensorPortObjectHelper.getPort(getPortInfo());
		
	}
	

	private void validateRequiredFields( Errors errors ){
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("portName", "Port Name");
		fields.put("portSubClassLookup", "Sensor Type");
		
		sensorPortObjectHelper.validateRequiredFields(getPortInfo(), errors, sensorPortDAO, fields, "PortValidator.sensorPortFieldRequired");
		
		SensorPort sensorPort = getSensorPort();
		String requiredFieldsNotProvided = new String();
		
		// Must provide sortOrder
		if( sensorPort.getSortOrder() <= 0 ){
			requiredFieldsNotProvided += "'Index'";
		}
				
		if (requiredFieldsNotProvided.length() > 0) {
			Object[] errorArgs = {"Sensor", requiredFieldsNotProvided};
			errors.rejectValue("tabSensorPorts", "PortValidator.fieldRequired", errorArgs, "SensorPort required fields not provided");
		}
	}
	
	private void validateEdit( Errors errors ){
		//No special requirements
	}

	private void validateFieldsLength( Errors errors ){

		// validate port name length
		sensorPortObjectHelper.validateFieldLength(getPortInfo(), errors, "portName", "PortValidator.sensorPortNameLength", 1L, MAX_PORT_LENGTH);
		
		// validate comments length
		sensorPortObjectHelper.validateFieldLength(getPortInfo(), errors, "comments", "PortValidator.sensorPortCommentLength", 0L, MAX_COMMENTS_LENGTH);
		
		//validate xyz length
		SensorPort sensorPort = getSensorPort();
		if( sensorPort.getXyzLocation() != null ){
			sensorPortObjectHelper.validateFieldLength(getPortInfo(), errors, "xyzLocation", "PortValidator.sensorPortXYZLength", 0L, MAX_XYZ_LENGTH);
		}
	}

	public void delete() {
		
		sensorPortObjectHelper.delete(getPortInfo(), sensorPortDAO);
		
	}

	@Override
	public void validateDelete(Object target, Errors errors) {
		
		sensorPortObjectHelper.validateDelete(getPortInfo(), sensorPortDAO, errors, "PortValidator.connectedSensorPortCannotDelete", savedPort);
		
	}

	
	@Override
	public boolean isConnectorValid() {
		SensorPort sensorPort = getSensorPort();
		return (sensorPort.getConnectorLookup() != null);
	}
	
	@Override
	public boolean isModified() {
		
		return sensorPortObjectHelper.isModified(getPortInfo(), sensorPortDAO);
		
	}

	@Override
	public void validateCommonAttributes(IPortInfo refPort, Errors errors) {
		//Nothing to validate here
	}
	
	@Override
	public void applyCommonAttributes(IPortInfo refPort, Errors errors) {
		// Sensor ports have no common attribute
	}
	
	public void save() {
		
		sensorPortObjectHelper.save(getPortInfo(), sensorPortDAO);
		
	}
	
	@Override
	public IPortInfo refresh() {
		
		setPort(sensorPortObjectHelper.refresh(getPortInfo(), sensorPortDAO));

		return getPortInfo();
	}
	


	@Override
	public void preValidateUpdates(Errors errors) {
		// Do nothing: no updates before the validation
	}

	@Override
	public void setValue(String fieldName, Object value) {
		
		sensorPortObjectHelper.setValue(getPortInfo(), fieldName, value);
		
	}
	
	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
		// Do nothing for now...
		
	}


}
