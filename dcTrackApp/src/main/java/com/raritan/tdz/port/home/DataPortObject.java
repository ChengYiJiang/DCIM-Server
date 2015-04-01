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

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.home.IPHome;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.port.dao.DataPortDAO;

public abstract class DataPortObject implements IPortObject {

	@Autowired
	private IPHome ipHome;
	
	private long MAX_PORT_LENGTH = 64;
	private long MAX_COMMENTS_LENGTH = 500;
	private long MAX_COMMUNITY_STR_LENGTH = 50;
	private long MAX_MAC_ADDRESS_LENGTH = 64;
	
	/* Port instance */
	private IPortInfo port;
	
	/* Saved Port Instance */
	private IPortInfo savedPort;
	
	/* port object helper */
	@Autowired(required=true)
	PortObjectHelper<DataPort> dataPortObjectHelper;
	
	/** The message source */
	protected ResourceBundleMessageSource messageSource;
	
	@Autowired
	protected DataPortDAO dataPortDAO;
	
	@Autowired
	protected ItemDAO itemDAO;

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
	
	public IPortInfo getSavedPortInfo() {
		return savedPort;
	}

	protected void setSavedPort(IPortInfo port) {
		if (null != port.getPortId() && port.getPortId() > 0) {
			Map<Long, DataPort> portMap = SavedItemData.getCurrentDataPorts();
			if (null == portMap) return;
			this.savedPort = SavedItemData.getCurrentDataPorts().get(port.getPortId());
		}
	}
	
	abstract public Set<Long> getPortSubclassLookupValueCodes();
	
	abstract public Set<Long> getItemClassLookupValueCodes();

	public void init(IPortInfo port, Errors errors) {
		setPort(port);
		setSavedPort(port);
		dataPortObjectHelper.init(port, getPortSubclassLookupValueCodes(), getItemClassLookupValueCodes(), "PortValidator.PortUnsupportedClass", errors);
	}

	public void delete() {
		
		Item item = getPortInfo().getItem();
		
		dataPortDAO.deletePortIPAddressAndTeaming(getPortInfo().getPortId());
		
		dataPortObjectHelper.delete(getPortInfo(), dataPortDAO);
		
		if( item.getDataPorts() == null || item.getDataPorts().size() == 0){
			item.setGroupingNumber(null);
		}
		
	}
	
	protected boolean errorExist(Errors errors, String errorCode, String errorMessage) {
		List<ObjectError> errorList = errors.getAllErrors();
		for (ObjectError error : errorList) {
			String msg = messageSource.getMessage(error, Locale.getDefault());
			if (error.getCode().equals(errorCode) && null != errorMessage && msg.contains(errorMessage) && msg.contains("Data")) {
				return true;
			}
		}
		return false;
	}

	protected void validateRequiredFields(Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("protocolID", "Protocol");
		fields.put("speedId", "Data rate");
		fields.put("portName", "Port Name");
		fields.put("portSubClassLookup", "Port Type");
		
		dataPortObjectHelper.validateRequiredFields(getPortInfo(), errors, dataPortDAO, fields, "PortValidator.dataPortFieldRequired");
	}
	
	private void validateFieldsLength(Errors errors) {
		// validate port name length
		dataPortObjectHelper.validateFieldLength(getPortInfo(), errors, "portName", "PortValidator.dataPortNameLength", 0L, MAX_PORT_LENGTH);
		
		// validate comments length
		dataPortObjectHelper.validateFieldLength(getPortInfo(), errors, "comments", "PortValidator.dataPortCommentLength", 0L, MAX_COMMENTS_LENGTH);
		
		// validate community string length
		dataPortObjectHelper.validateFieldLength(getPortInfo(), errors, "communityString", "PortValidator.portCommStrLength", 0L, MAX_COMMUNITY_STR_LENGTH);
		
		// validate mac address length
		dataPortObjectHelper.validateFieldLength(getPortInfo(), errors, "macAddress", "PortValidator.portMacAddressLength", 0L, MAX_MAC_ADDRESS_LENGTH);

	}
	
	private void validateInvalidFields(Errors errors) {

		// validate sort order
		dataPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "sortOrder", "Index", "PortValidator.powerIncorrectFieldValue", 0L, null);

	}
	
	public void validateSave(Object target, Errors errors) {

		// validate required fields
		validateRequiredFields(errors);
		
		// validate edit for certains fields when port is connected
		validateEdit(errors);
		
		// validate field lengths
		validateFieldsLength(errors);
		
		// valid field values
		validateInvalidFields(errors);
		
		// validate connector
		validateConnector(target, errors);
		
	}

	public void validateDelete(Object target, Errors errors) {
		dataPortObjectHelper.validateDelete(getPortInfo(), dataPortDAO, errors, "PortValidator.connectedDataPortCannotDelete", savedPort);
	}

	protected void validatePortType(Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("portSubClassLookup", "Port Type");
		
		dataPortObjectHelper.validateRequiredFields(getPortInfo(), errors, dataPortDAO, fields, "PortValidator.dataPortFieldRequired");

	}


	protected void validateConnector(Object target, Errors errors) {
		
		dataPortObjectHelper.validateIfTrue(getPortInfo(), errors, "PortValidator.dataPortFieldRequired", isConnectorValid(), "Connector");
		
	}
	
	protected DataPort getDataPort() {
		return dataPortObjectHelper.getPort(getPortInfo());
	}
	
	@Override
	public boolean isModified() {
		return dataPortObjectHelper.isModified(getPortInfo(), dataPortDAO);
	}
	
	public void validateEdit(Errors errors) {
		
		List<String> fields = new ArrayList<String>();
		fields.add("portId");
		fields.add("connectorLookup");
		fields.add("mediaId");
		fields.add("protocolID");
		fields.add("speedId");
		fields.add("portSubClassLookup");
		dataPortObjectHelper.validateEdit(getPortInfo(), errors, dataPortDAO, fields, "PortValidator.connectedDataPortCannotEdit", savedPort);
	}
	
	@Override
	public void validateCommonAttributes(IPortInfo refPort, Errors errors) {
		// data ports have no common attributes
	}
	
	@Override
	public void applyCommonAttributes(IPortInfo refPort, Errors errors) {
		// data ports have no common attributes
	}
	
	public void save() {
		dataPortObjectHelper.save(getPortInfo(), dataPortDAO);
	}

	@Override
	public IPortInfo refresh() {
		setPort(dataPortObjectHelper.refresh(getPortInfo(), dataPortDAO));
		return getPortInfo();
	}
	
	@Override
	public void preValidateUpdates(Errors errors) {
		// Do nothing
	}
	
	@Override
	public void setValue(String fieldName, Object value) {
		
		dataPortObjectHelper.setValue(getPortInfo(), fieldName, value);
		
	}

	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
		
		try {
		
			setIpAddress(userInfo, errors);

		} catch (BusinessValidationException e) {
			
			// can safely consume the exception because I already have the errors updated
			e.printStackTrace();
		}
		
	}
	
	private void setIpAddress(UserInfo userInfo, Errors errors) throws BusinessValidationException {

		DataPort dataPort = getDataPort();
		Item item = dataPort.getItem();
		String proxyIndex = dataPort.getProxyIndexImport();
		String ipAddress = dataPort.getIpAddressImport();
		
		if( proxyIndex == null || proxyIndex.equals(item.getGroupingNumber())) {
			if( ipAddress == null || ipHome.dataPortContainsIpAddress( ipAddress, dataPort.getPortId())) {
				//nothing to do; data same as in DB
				return;
			}
		}
		if (null == dataPort.getPortId() || dataPort.getPortId() < 1) {
			dataPort.setPortId(dataPortDAO.getPortId(dataPort.getItem().getItemId(), dataPort.getPortSubClassLookup().getLkpValueCode(), dataPort.getPortName()));
		}
		ipHome.saveIpAddressAndProxyForDataPort(dataPort, dataPort.getIpAddressImport(), dataPort.getProxyIndexImport(), userInfo, errors);
		itemDAO.merge(dataPort.getItem());
	}

}
