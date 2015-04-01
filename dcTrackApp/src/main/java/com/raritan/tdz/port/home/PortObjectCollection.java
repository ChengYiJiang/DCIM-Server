package com.raritan.tdz.port.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;

public abstract class PortObjectCollection implements IPortObjectCollection {

	protected Set<IPortObject> ports;
	protected IPortObjectFactory portObjectFactory;
	protected Item item;
	
	@Autowired(required=true)
	protected ItemDAO itemDAO;
	
	protected PortMoveDAO<Serializable> portMoveDAO;

	public PortMoveDAO<Serializable> getPortMoveDAO() {
		return portMoveDAO;
	}

	public void setPortMoveDAO(PortMoveDAO<Serializable> portMoveDAO) {
		this.portMoveDAO = portMoveDAO;
	}

	protected Item getItem() {
		return item;
	}

	protected void setItem(Item item) {
		this.item = item;
	}

	public PortObjectCollection(IPortObjectFactory portObjectFactory) {
		super();
		this.setPortObjectFactory(portObjectFactory);
	}

	private void validateNameUniqueness(Errors errors) {
		String duplicateNames = new String();
		List<String> portNames = new ArrayList<String>();
		for (IPortObject po: ports) {
			String portName = (null != po && null != po.getPortInfo() && null != po.getPortInfo().getPortName()) ? po.getPortInfo().getPortName().trim() : null;
			if (!portNames.contains(portName)) {
				portNames.add(portName);
			}
			else {
				duplicateNames += " '" + po.getPortInfo().getPortName() + "'";
			}
		}
		
		if (duplicateNames.length() > 0) {
			Object[] errorArgs = {duplicateNames};
			errors.rejectValue("Ports", "PortValidator.duplicatePortName", errorArgs, "Port names are not unique");
		}
	}

	private boolean portsDeleted() {
		
		return (this.getDeleteIds().size() > 0);
	}
	
	private boolean portsAdded() {
		
		return (this.getAddedPortCount() > 0);
	}
	
	private boolean portsEdited() {
		for (IPortObject port: ports) {
			if (null != port && port.isModified()) {
				return true;
			}
		}
		
		return false;
	}
	
	private void validatePortsModificationOnMovingItem(Errors errors) {

		if (null == portMoveDAO) return;
		
		String whenMovedItemName = portMoveDAO.getWhenMovedItemName(item.getItemId());
		if (null == whenMovedItemName) return;
		
		// When the ports are added/deleted in the moving item , throw warning exception
		if (portsAdded() || portsDeleted()) {
			if (item.getSkipValidation() != null && item.getSkipValidation()) {
				item.setDeleteWhenMovedItem(true);
			}
			else {
				Object[] errorArgs = {item.getItemName(), whenMovedItemName};
				errors.rejectValue("Ports", "ItemMoveValidator.PortsModificationOnMovingItem", errorArgs, "Move request is pending, port cannot be added/deleted. Do you want to continue?");
			}
		}
		
	}
	
	private void validatePortsModificationOnWhenMovedItem(Errors errors) {
		
		if (null == portMoveDAO) return;
		
		// When the ports are added/deleted/edited in the when moved item, throw exception
		String movingItemName = portMoveDAO.getMovingItemName(item.getItemId());
		if (null == movingItemName) return;
		
		// When the ports are added/deleted/edited in the moving item , throw exception
		if (portsAdded() || portsDeleted() || portsEdited()) {
			Object[] errorArgs = {item.getItemName()};
			errors.rejectValue("Ports", "ItemMoveValidator.PortsModificationOnWhenMovedItem", errorArgs, "Ports cannot be modified on the when moved item.");
		}

		
	}

	private void validateItemStatus(Errors errors) {
		// You can perform CRUD on ports in any item state
		List<Long> itemStatesCRUDNotAllowed = new ArrayList<Long>();
		// You can perform CRUD on ports in any item state
		if (null != item && null != item.getStatusLookup() && null != item.getStatusLookup().getLkpValueCode() &&
				!itemStatesCRUDNotAllowed.contains(item.getStatusLookup().getLkpValueCode().longValue())) {
			return;
		}
		// check if ports were deleted in archived
		String delErrorStr = new String();
		String itemStatusStr = item.getStatusLookup() != null?item.getStatusLookup().getLkpValue():"<Unknown>";
		if (this.getDeleteIds().size() > 0) {
			delErrorStr = "Cannot delete port(s) in " + itemStatusStr + " state\n";
		}
		if (delErrorStr.length() > 0) {
			Object[] errorArgs = { itemStatusStr };
			errors.rejectValue("Ports", "PortValidator.cannotDeletePorts", errorArgs, "Cannot delete ports in archived state");
		}
		
		// check if ports were added in archived state
		String addErrorStr = new String();
		if (this.getAddedPortCount() > 0) {
			addErrorStr = "Cannot add port(s) in " + itemStatusStr + " state\n";
		}
		if (addErrorStr.length() > 0) {
			Object[] errorArgs = { itemStatusStr };
			errors.rejectValue("Ports", "PortValidator.cannotAddPorts", errorArgs, "Cannot add ports in archived state");
		}
		
		String editErrorStr = new String();
		for (IPortObject port: ports) {
			if (port.isModified()) {
				editErrorStr = "Cannot edit port(s) in " + itemStatusStr + " state\n";
				break;
			}
		}
		if (editErrorStr.length() > 0) {
			Object[] errorArgs = { itemStatusStr };
			errors.rejectValue("Ports", "PortValidator.cannotEditPorts", errorArgs, "Cannot edit ports in archived state");
		}

	}
	
	@Override
	public void deleteInvalidPorts(Errors errors) {
		// TODO Auto-generated method stub

	}

	public IPortObjectFactory getPortObjectFactory() {
		return portObjectFactory;
	}

	public void setPortObjectFactory(IPortObjectFactory portObjectFactory) {
		this.portObjectFactory = portObjectFactory;
	}
	
	private long getAddedPortCount() {
		long count = 0;
		for (IPortObject port: ports) {
			if (null != port && null != port.getPortInfo() && (port.getPortInfo().getPortId() == null || port.getPortInfo().getPortId().longValue() <= 0)) {
				count++;
			}
		}
		return count;
	}

	protected void validateSave(Errors errors) {
		for (IPortObject port: ports) {
			if (null != port) {
				port.validateSave(item, errors);
			}
		}
	}
	
	protected void validateDelete(Errors errors) {
		List<Long> delIds = getDeleteIds();
		for (Long delId: delIds) {
			IPortObject delPortObj = getDetachedPort(delId, errors);
			if (null != delPortObj) {
				delPortObj.validateDelete(item, errors);
			}
		}
	}
	
	private void validateCommonAttributes(Errors errors) {
		Map<Long, IPortObject> refrencePortMap = new HashMap<Long, IPortObject>();
		for (IPortObject port: ports) {
			if (null != port && null != port.getPortInfo() && 
					null != port.getPortInfo().getPortSubClassLookup() && 
					null != port.getPortInfo().getPortSubClassLookup().getLkpValueCode() &&
					null == refrencePortMap.get(port.getPortInfo().getPortSubClassLookup().getLkpValueCode())) {
				refrencePortMap.put(port.getPortInfo().getPortSubClassLookup().getLkpValueCode(), port);
			}
			if (null != port) {
				port.validateCommonAttributes(refrencePortMap.get(port.getPortInfo().getPortSubClassLookup().getLkpValueCode()).getPortInfo(), errors);
			}
		}
	}
	
	@Override
	public void applyCommonAttributes(IPortObject refPort, Errors errors) {
		
		if (null == refPort ||
				null == refPort.getPortInfo() ||
				null == refPort.getPortInfo().getPortSubClassLookup() ||
				null == refPort.getPortInfo().getPortSubClassLookup().getLkpValueCode()) return;
		
		for (IPortObject port: ports) {
			if (null != port && null != port.getPortInfo() && 
					null != port.getPortInfo().getPortSubClassLookup() && 
					null != port.getPortInfo().getPortSubClassLookup().getLkpValueCode() &&
					port.getPortInfo().getPortSubClassLookup().getLkpValueCode().equals(refPort.getPortInfo().getPortSubClassLookup().getLkpValueCode())) {
				port.applyCommonAttributes(refPort.getPortInfo(), errors);
			}
		}
	}
	
	@Override
	public void validate(Errors errors) {

		// validate that the ports are not added / deleted on the moving item
		validatePortsModificationOnMovingItem(errors);
		
		// validate that the ports are not modified on the when moved item
		validatePortsModificationOnWhenMovedItem(errors);
		
		// validate item status allows port(s) modifications
		validateItemStatus(errors);

		// validate sort order
		validateSortOrder(errors);
		
		//validate name uniqueness
		validateNameUniqueness(errors);
		
		// validate save operation
		validateSave(errors);
		
		// validate delete operation
		validateDelete(errors);
		
		// validate common attributes
		validateCommonAttributes(errors);
		
	}

	abstract protected List<Long> getDeleteIds();
	
	abstract protected IPortObject getDetachedPort(Long delId, Errors errors);
	
	abstract protected void validateSortOrder(Errors errors);
	
	@Override
	public void delete() {
		for (IPortObject port: ports) {
			port.delete();
		}
	}

	abstract protected void updateSortOrder(Errors errors);
	
	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
	}

	@Override
	public void preSave() {
	}

	/**
	 * save the port object to the database
	 */
	@Override
	public void save() {
		for (IPortObject port: ports) {
			port.save();
		}
	}
	
	@Override
	public void clearPortMoveData(Errors errors) {
		List<Long> deletePortIds = this.getDeleteIds();
		
		for (Long portId: deletePortIds) {
			
			portMoveDAO.deletePortMoveData(portId);
		}
	}
	
}
