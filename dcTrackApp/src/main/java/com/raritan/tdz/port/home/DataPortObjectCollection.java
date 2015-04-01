package com.raritan.tdz.port.home;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.port.dao.DataPortDAO;

public class DataPortObjectCollection extends PortObjectCollection {

	@Autowired(required=true)
	DataPortDAO dataPortDAO;
	
	@Autowired(required=true)
	PortCollectionHelper<DataPort> dataPortObjectCollectionHelper;
	
	public DataPortObjectCollection(IPortObjectFactory portObjectFactory) {
		super(portObjectFactory);
	}
	
	@Override
	public void init(Object itemObj, Errors errors) {
		setItem((Item)itemObj);
		ports = dataPortObjectCollectionHelper.init(itemObj, portObjectFactory, errors);
	}
	
	@Override
	public List<Long> getDeleteIds() {
		// get deleted power port ids
		return dataPortObjectCollectionHelper.getDeleteIds(item);
	}
	
	private void deleteItemPort(IPortInfo portInfo) {
		dataPortObjectCollectionHelper.deleteItemPort(item, portInfo);
	}
	
	@Override
	public void deleteInvalidPorts(Errors errors) {
		for (IPortObject port: ports) {
			if (null != port && !port.isConnectorValid()) {
				port.validateDelete(item, errors);
				deleteItemPort(port.getPortInfo());
			}
		}
	}

	@Override
	public IPortObject getDetachedPort(Long portId, Errors errors) {
		DataPort dp = dataPortDAO.loadEvictedPort(portId);
		IPortObject po = getPortObjectFactory().getPortObject(dp, errors);
		return po;
	}
	
	@Override
	public void validateSortOrder(Errors errors) {
		List<String> nonUniquePortTypes = dataPortObjectCollectionHelper.getPortTypeOfNonUniqueSortOrder(item);
		
		for (String portType: nonUniquePortTypes) {
			Object errorArgs[]  = { portType };
			errors.rejectValue("tabDataPorts", "PortValidator.dataPortSortOrderNotUnique", errorArgs, "Data Port order is not unique");
		}
	}
	
	private DataPort getPort(Item item, Long portId) {
		return dataPortObjectCollectionHelper.getPort(item, portId);
	}
	
	private boolean isCommStringModified(DataPort dp, Item origItem) {
		DataPort dpPort = getPort(origItem, dp.getPortId());
		String dbCommStr = dpPort.getCommunityString(); 
		String dpObjCommStr = dp.getCommunityString();
		if ( ( null == dbCommStr || dbCommStr.length() <= 0 ) ) {
			return ( null != dpObjCommStr && dpObjCommStr.length() > 0 );
		}
		else {
			return dbCommStr.equals(dpObjCommStr);
		}
	}
	
	private void validateSnmpCommunityString(Errors errors) {
		Set<DataPort> dataPorts = getItem().getDataPorts();
		
		if (null == dataPorts || dataPorts.size() == 0) {
			// if snmp community string is provided and no data port, report an error
			if (null != item.getSnmpV2CommunityString() && item.getSnmpV2CommunityString().length() > 0) {
				Object errorArgs[]  = { };
				errors.rejectValue("tabDataPorts", "PortValidator.CannotSetDataPortSnmpCommunity", errorArgs, "No data ports available to set the SNMP community string.");
			}
			return;
		}
		
		long snmpCommunityStringCount = 0;
		for (DataPort dataPort: dataPorts) {
			String commStr = dataPort.getCommunityString();
			if (null != commStr && commStr.length() > 0 && !commStr.matches("\\s+")) {
				snmpCommunityStringCount++;
			}
		}
		
		if (snmpCommunityStringCount > 1) {
			Object errorArgs[]  = { };
			errors.rejectValue("tabDataPorts", "PortValidator.dataPortSnmpCommunityCount", errorArgs, "More than one port has snmp community string");
		}
	}

	@SuppressWarnings("unused")
	private void updateSNMPCommunity(Set<DataPort> dataPorts,
			long snmpCommunityStringCount, Item origItem) {
		{
			boolean changedCommStrFound = false;
			long processedPortCount = snmpCommunityStringCount;
			for (DataPort dataPort: dataPorts) {
				String commStr = dataPort.getCommunityString();
				if (null != commStr && commStr.length() > 0) {
					processedPortCount--;
					if (!changedCommStrFound) {
						if (isCommStringModified(dataPort, origItem)) {
							changedCommStrFound = true;
						}
						else {
							if (processedPortCount > 0) {
								dataPort.setCommunityString(null);
							}
						}
					}
					else {
						dataPort.setCommunityString(null);
					}
				}
			}
		}
	}
	
	private static long MAX_NUM_OF_DATA_PORTS = 288L;
	
	private void validatePortLimit(Errors errors) {
		if (ports.size() > MAX_NUM_OF_DATA_PORTS) {
			Object errorArgs[]  = { MAX_NUM_OF_DATA_PORTS };
			errors.rejectValue("tabDataPorts", "PortValidator.dataPortMaxNumOfPorts", errorArgs, "Cannot create more than " + MAX_NUM_OF_DATA_PORTS + " Data ports");
		}
	}
	
	@Override
	public void validate(Errors errors) {
		super.validate(errors);
		
		// validate port limit
		validatePortLimit(errors);
		
		// validate snmp community string
		validateSnmpCommunityString(errors);
	}

	@Override
	protected void updateSortOrder(Errors errors) {
		dataPortObjectCollectionHelper.updateSortOrderByPortSubclass(item, errors);
	}

	final int MAX_INT = 2147483647;
	
	@Override
	public void preValidateUpdates(Errors errors) {
		// update the index, if can be duplicate or incorrect
		updateSortOrder(errors);
		
		// run pre-validate updates on individual object and 
		// also find the port with minimum index
		IPortObject minIndexPort = null;
		int minIndex = MAX_INT;
		
		for (IPortObject port: ports) {
			if (null != port) {
				
				// run pre-validate updates on individual object and
				port.preValidateUpdates(errors);
				
				// also find the port with minimum index
				int portIndex = port.getPortInfo().getSortOrder();
				if (portIndex < minIndex) {
					minIndexPort = port;
					minIndex = portIndex;
				}
				
			}
		}
		
		// set the community string to the port with minimum index value 
		// from the item's data only if the community string is been provided and port name not provided
		if (null != minIndexPort && 
				null != item.getSnmpV2CommunityString() /*&& item.getSnmpV2CommunityString().length() > 0*/) {
			
			minIndexPort.setValue("communityString", item.getSnmpV2CommunityString());
		}
		
	}
	
	@Override
	public void preSave() {
		
		List<Long> deletedPortIds = getDeleteIds();
		
		if (null == deletedPortIds || deletedPortIds.size() == 0) return;

		for (Long portId: deletedPortIds) {
			dataPortDAO.deletePortIPAddressAndTeaming(portId);
		}
		Set<DataPort> dataPorts = getItem().getDataPorts();
		if( dataPorts == null || dataPorts.size() == 0){
			getItem().setGroupingNumber(null);
		}
	}
	
	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
		
		super.postSave(userInfo, errors);
		
		// post save on individual data port
		for (IPortObject port: ports) {
			// port.refresh();
			port.postSave(userInfo, errors);
		}
		
	}


}
