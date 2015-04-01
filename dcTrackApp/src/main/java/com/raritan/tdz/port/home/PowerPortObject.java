package com.raritan.tdz.port.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.port.dao.PowerPortDAO;

public abstract class PowerPortObject implements IPortObject {

	private long MAX_PORT_LENGTH = 64;
	private long MAX_COMMENTS_LENGTH = 500;

	/* Port instance */
	private IPortInfo port;
	
	/* Saved Port Instance */
	private IPortInfo savedPort;
	
	/* port object helper */
	@Autowired(required=true)
	PortObjectHelper<PowerPort> powerPortObjectHelper;

	@Autowired
	protected PowerPortDAO powerPortDAO;
	
	/** The message source */
	protected ResourceBundleMessageSource messageSource;
	
	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public IPortInfo getPortInfo() {
		return port;
	}

	protected void setPort(IPortInfo port) {
		this.port = port;
	}

	public IPortInfo getSavedPort() {
		return savedPort;
	}

	public void setSavedPort(IPortInfo savedPort) {
		if (null != port.getPortId() && port.getPortId() > 0) {
			Map<Long, PowerPort> portMap = SavedItemData.getCurrentPowerPorts();
			if (null == portMap) return;
			this.savedPort = SavedItemData.getCurrentPowerPorts().get(port.getPortId());
		}
	}

	abstract public Set<Long> getPortSubclassLookupValueCodes();
	
	abstract public Set<Long> getItemClassLookupValueCodes();
	
	public void init(IPortInfo port, Errors errors) {
		setPort(port);
		setSavedPort(port);
		powerPortObjectHelper.init(port, getPortSubclassLookupValueCodes(), getItemClassLookupValueCodes(), "PortValidator.PortUnsupportedClass", errors);
	}

	public void delete() {
		powerPortObjectHelper.delete(getPortInfo(), powerPortDAO);
	}
	
	protected boolean errorExist(Errors errors, String errorCode, String errorMessage) {
		List<ObjectError> errorList = errors.getAllErrors();
		for (ObjectError error : errorList) {
			String msg = messageSource.getMessage(error, Locale.getDefault());
			if (error.getCode().equals(errorCode) && msg.contains(errorMessage) && msg.contains("Power")) {
				return true;
			}
		}
		return false;
	}
	
	protected void validateRequiredFields(Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("phaseLookup", "Phase");
		fields.put("voltsLookup", "Volts");
		fields.put("portName", "Port Name");
		fields.put("portSubClassLookup", "Port Type");
		
		powerPortObjectHelper.validateRequiredFields(getPortInfo(), errors, powerPortDAO, fields, "PortValidator.powerPortFieldRequired");

	}
	
	private void validateFieldsLength(Errors errors) {

		// validate port name length
		powerPortObjectHelper.validateFieldLength(getPortInfo(), errors, "portName", "PortValidator.powerPortNameLength", 1L, MAX_PORT_LENGTH);
		
		// validate comments length
		powerPortObjectHelper.validateFieldLength(getPortInfo(), errors, "comments", "PortValidator.powerPortCommentLength", 0L, MAX_COMMENTS_LENGTH);
		
	}

	
	protected void validateInvalidFields(Errors errors) {

		// validate sort order
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "sortOrder", "Index", "PortValidator.powerIncorrectFieldValue", 0L, null);

		// validate port name range
		long MAX_PORT_LENGTH = 64;
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "portName", "Port Name", "PortValidator.powerIncorrectFieldValue", 0L, MAX_PORT_LENGTH);

	}
	

	/**
	 * validates the port. target is the item domain to be saved
	 */
	public void validateSave(Object target, Errors errors) {
		
		// Validate required fields
		validateRequiredFields(errors);

		// validate edit
		validateEdit(errors);

		// validate field length
		validateFieldsLength(errors);
		
		// validate invalid field value
		validateInvalidFields(errors);
		
		// validate connector
		validateConnector(target, errors);
		
	}
	
	public void validateDelete(Object target, Errors errors) {
		
		powerPortObjectHelper.validateDelete(getPortInfo(), powerPortDAO, errors, "PortValidator.connectedPowerPortCannotDelete", savedPort);
		
	}

	protected void validatePortType(Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("portSubClassLookup", "Port Type");
		
		powerPortObjectHelper.validateRequiredFields(getPortInfo(), errors, powerPortDAO, fields, "PortValidator.powerPortFieldRequired");
		
	}

	
	protected void validateConnector(Object target, Errors errors) {
		
		powerPortObjectHelper.validateIfTrue(getPortInfo(), errors, "PortValidator.powerPortFieldRequired", isConnectorValid(), "Connector");
		
	}

	protected PowerPort getPowerPort() {
		return powerPortObjectHelper.getPort(getPortInfo());

	}
	
	@Override
	public boolean isModified() {
		return powerPortObjectHelper.isModified(getPortInfo(), powerPortDAO);

	}
	
	public void validateEdit(Errors errors) {
		
		List<String> fields = new ArrayList<String>();
		fields.add("portId");
		fields.add("connectorLookup");
		fields.add("phaseLookup");
		fields.add("voltsLookup");
		powerPortObjectHelper.validateEdit(getPortInfo(), errors, powerPortDAO, fields, "PortValidator.connectedPowerPortCannotEdit", savedPort);

	}
	
	@Override
	public void validateCommonAttributes(IPortInfo refPort, Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("connectorLookup", "Connector");
		fields.put("phaseLookup", "Phase Type");
		fields.put("voltsLookup", "Volt");
		fields.put("powerFactor", "Power Factor");
		fields.put("wattsNameplate", "Watts Nameplate");
		fields.put("wattsBudget", "Watts Budget");
		
		powerPortObjectHelper.validateCommonAttributes(getPortInfo(), refPort, errors, fields, "PortValidator.commonAttributeVoilations");

	}
	
	@Override
	public void save() {
		powerPortObjectHelper.save(getPortInfo(), powerPortDAO);
		
	}
	
	@Override
	public IPortInfo refresh() {
		setPort(powerPortObjectHelper.refresh(getPortInfo(), powerPortDAO));
		return getPortInfo();
	}
	
	@Override
	public void preValidateUpdates(Errors errors) {
		// Do nothing
	}

	@Override
	public void setValue(String fieldName, Object value) {
		
		powerPortObjectHelper.setValue(getPortInfo(), fieldName, value);
		
	}
	
	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
		// Do nothing for now...
		
	}


}
