package com.raritan.tdz.port.home;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;

public class PowerPortDomainAdaptor implements ValueIDFieldToDomainAdaptor, PortDomainAdaptor {

	@Autowired
	private UtilHome utilHome;
	
	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	private UserLookupFinderDAO userLookupFinderDAO;
	
	@Autowired(required=true)
	private ConnectorLookupFinderDAO connectorLookupFinderDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	private ResourceBundleMessageSource messageSource;
	
	public ResourceBundleMessageSource getMessageSource() {
        return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
	}
	
	@Override
	public Object convert(Object dbObject, ValueIdDTO valueIdDTO)
			throws BusinessValidationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, DataAccessException {
		if (null == valueIdDTO || null == dbObject) {
			return null;
		}
		convertPowerPortsForAnItem(dbObject, valueIdDTO);
		return null;
	}
	
	private boolean validateDto(Item item, List<PowerPortDTO> powerPortDTOList) throws BusinessValidationException {
		boolean invalidArg = false;
		long itemId = item.getItemId();
		long item_class = item.getClassLookup().getLkpValueCode();
		
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {

			/* note that the existing power ports can only be for an existing item and the item id should match */
			if (itemId > 0 && powerPortDTO.getItemId() == null) {
				invalidArg = true;
				break;
			}
			if ((itemId <= 0 && powerPortDTO.getItemId() != null && powerPortDTO.getItemId() > 0) ||
					(itemId > 0 && (powerPortDTO.getItemId() == null || powerPortDTO.getItemId() <= 0))) {
				invalidArg = true;
				break;
			}
			else if ((itemId > 0 && (powerPortDTO.getItemId() == null || powerPortDTO.getItemId() > 0)) &&
					(itemId != powerPortDTO.getItemId())) {
				invalidArg = true;
				break;
			}

			/* For item class Rack pdu, every outlet should have input port cord model id or input cord port id */
			if (item_class == SystemLookup.Class.RACK_PDU &&
					powerPortDTO.getPortSubClassLksValueCode() == SystemLookup.PortSubClass.RACK_PDU_OUTPUT &&
					((powerPortDTO.getInputCordPortId() == null && powerPortDTO.getInputCordModelPowerPortId() == null) ||
					(powerPortDTO.getInputCordPortId() != null && powerPortDTO.getInputCordModelPowerPortId() != null &&
					powerPortDTO.getInputCordPortId() <= 0 && powerPortDTO.getInputCordModelPowerPortId() <= 0))) {
				invalidArg = true;
				throwBusinessValidationException(null, "powerProc.missingInputCord");
				break;
			}
		}
		if (invalidArg) {
			throw new InvalidPortObjectException("Invalid port dto: port id and item ids are not correct");
		}
		return !invalidArg;
	}
	
	@Override
	public IPortInfo convertOnePortForAnItem( Object itemObj, Long portId, PortInterface dto) throws DataAccessException {
		
		Item item = (Item)itemObj;
		PowerPortDTO powerPortDTO = (PowerPortDTO) dto;
		if (powerPortDTO == null) return null;
		
		java.util.Date date= new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		
		PowerPort pp = null;
		
		// Existing power port
		if (powerPortDTO.getPortId() != null && powerPortDTO.getPortId() > 0) {
			/* For an existing item, edit power port request */
			Set<PowerPort> ppSet = item.getPowerPorts();
			PowerPort ipp = null;
			for (PowerPort editpp: ppSet) {
				if (null != editpp.getPortId() && editpp.getPortId().longValue() == powerPortDTO.getPortId().longValue()) {
					ipp = editpp;
					break;
				}
			}
			if (null != ipp) {
				pp = PortsAdaptor.updatePowerPortDTOToDomain(ipp, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
				pp.setUpdateDate(timeStamp);
			}
			else {
				// FIXME:: code should never come to this point
				PowerPort powerPort = null; // (PowerPort) session.get(PowerPort.class, powerPortDTO.getPortId());
				if (null == powerPort) {
					pp = PortsAdaptor.adaptPowerPortDTOToNewItemDomain(item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
					pp.setCreationDate(timeStamp);
					pp.setUpdateDate(timeStamp);
					item.addPowerPort(pp);
				}
			}
		}
		
		// New power port
		else {
			pp = PortsAdaptor.adaptPowerPortDTOToNewItemDomain(item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
			pp.setCreationDate(timeStamp);
			pp.setUpdateDate(timeStamp);
			pp.setPortId(null);
			item.addPowerPort(pp);
		}
		
		// set the input cord
		PowerPort inputPP = pp.getInputCordPort();
		if (null != inputPP) {
			addConnection(pp);
		}
		
		updateItemRedundancy(item, powerPortDTO);

		return pp;
	}
	
	private void updateItemRedundancy(Item item, PowerPortDTO powerPortDTO) {

		long itemClass = item.getClassLookup().getLkpValueCode();
		boolean updateRedundancyApply = (itemClass == SystemLookup.Class.DEVICE ||
											itemClass == SystemLookup.Class.NETWORK ||
											itemClass == SystemLookup.Class.PROBE) && 
											(item instanceof ItItem) &&
											(null != item.getPowerPorts() && item.getPowerPorts().size() > 0);
		
		if (updateRedundancyApply && 
				powerPortDTO.getPsRedundancy() != null && powerPortDTO.getPsRedundancy().length() > 0) {
			ItItem itItem = (ItItem) item;
			itItem.setPsredundancy(powerPortDTO.getPsRedundancy());
		}

	}
	
	private void convertPowerPortsForAnItem(Object itemObj, ValueIdDTO dto) throws ClassNotFoundException, BusinessValidationException, DataAccessException {
		Item item = (Item)itemObj;
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) dto.getData();
		boolean updateFreeInputCord = false;
		int freeInputCordCount = 0;
		boolean updateFreePowerPort = false;
		int freePowerPortCount = 0;

		// validate dtos
		if (!validateDto(item, powerPortDTOList)) {
			return;
		}

		java.util.Date date = new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());

		/* Delete all the power ports from the database that are not in the DTOs */
		deletePowerPortsNotInDTO(item, powerPortDTOList);
		
		/* ----- EXISTING power ports ----- */
		Map<Long, PowerPort> inputCordPorts = new HashMap<Long, PowerPort>();
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			/* note that the existing power ports can only be for an existing item and the item id should match */
			if (powerPortDTO.getPortId() != null && powerPortDTO.getPortId() > 0) {
				/* For an existing item, edit power port request */
				PowerPort pp = null;
				Set<PowerPort> ppSet = item.getPowerPorts();
				PowerPort ipp = null;
				for (PowerPort editpp: ppSet) {
					if (null != editpp.getPortId() && editpp.getPortId().longValue() == powerPortDTO.getPortId().longValue()) {
						ipp = editpp;
						break;
					}
				}
				if (null != ipp) {
					pp = PortsAdaptor.updatePowerPortDTOToDomain(ipp, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
					pp.setUpdateDate(timeStamp);
				}
				else {
					// FIXME:: code should never come to this point
					PowerPort powerPort = null; // (PowerPort) session.get(PowerPort.class, powerPortDTO.getPortId());
					if (null == powerPort) {
						pp = PortsAdaptor.adaptPowerPortDTOToNewItemDomain(item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
						pp.setCreationDate(timeStamp);
						pp.setUpdateDate(timeStamp);
						item.addPowerPort(pp);
					}
				}
				if (pp.isInputCord()) {
					inputCordPorts.put(pp.getPortId(), pp);
				}
			}
		}
		
		
		/* ----- NEW Input cord power ports ----- */
		/* For input cord,
		 * if new input cord is added, create input cord an add to the HashMap
		 * Create input cord first and make a map of model id against the domain PowerPort */
		Map<Long, PowerPort> inputCordModelPorts = new HashMap<Long, PowerPort>();
		int addrIdx = 1; 
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			// all NEW input cord power ports are created
			if (powerPortDTO.isInputCord()) {
				/* note that the port id should be null for new power ports
				 * to recognize a NEW power port, model port power id is NOT null */
				if (powerPortDTO.getPortId() == null || powerPortDTO.getPortId() <= 0) {
					PowerPort pp = null;
					pp = PortsAdaptor.adaptPowerPortDTOToNewItemDomain(item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
					pp.setPortId(null);
					pp.setCreationDate(timeStamp);
					pp.setUpdateDate(timeStamp);
					inputCordModelPorts.put(powerPortDTO.getModelPowerPortId(), pp);
					/* add input cord address for CV to process SNMP requests */
					String addr = "Input Cord ";
					int sortOrder = powerPortDTO.getSortOrder();
					if (sortOrder > 0 ) { 
						addr += sortOrder; /* assumes sort order for input cord starts with 1 */
					} else {
						addr += addrIdx++;
					}
					pp.setAddress(addr);
					if (item.getItemId() <= 0) {
						/* The free input port count is limited for new item. 
						 * For an existing item  input port count will be updated via the trigger. 
						 * TODO:: This change is for 3.0 to limit the number of changes. Once the trigger starts working this code should be removed */
						freeInputCordCount++;
						updateFreeInputCord = true;
					}
					item.addPowerPort(pp);
				}
			}
		}
		
		// update free input cord count
		if (updateFreeInputCord) {
			item.setFreeInputCordCount(freeInputCordCount);
		}

		/* ----- NEW NON-input cord power ports ----- */
		/* For non-input cord
		 * if new non-input power port is added, create power port
		 * if this new power port is associated with an input cord, get the input cord from the HashMap
		 **/
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			// all other NEW (non input cord) power ports are created
			if (!powerPortDTO.isInputCord()) {
				/* note that the port id should be null for new power ports
				 * to recognize a NEW power port, model port power id is NOT null */

				if (powerPortDTO.getPortId() == null || powerPortDTO.getPortId() <= 0) {
					PowerPort pp = null;
					pp = PortsAdaptor.adaptPowerPortDTOToNewItemDomain(item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
					pp.setCreationDate(timeStamp);
					pp.setUpdateDate(timeStamp);
					pp.setPortId(null);
					PowerPort inputPP = null;
					if (null != powerPortDTO.getInputCordModelPowerPortId() && powerPortDTO.getInputCordModelPowerPortId().longValue() > 0) {
						inputPP = inputCordModelPorts.get(powerPortDTO.getInputCordModelPowerPortId());
					}
					else if (null != powerPortDTO.getInputCordPortId() && powerPortDTO.getInputCordPortId().longValue() > 0) {
						inputPP = inputCordPorts.get(powerPortDTO.getInputCordPortId());
					}
					pp.setInputCordPort(inputPP);
					if (null != inputPP) {
						addConnection(pp);
					}
					if (item.getItemId() <= 0) {
						/* The free power port count is limited for new item. 
						 * For an existing item  power port count will be updated via the trigger. 
						 * TODO:: This change is for 3.0 to limit the number of changes. Once the trigger starts working this code should be removed */
						freePowerPortCount++;
						updateFreePowerPort = true;
					}
					item.addPowerPort(pp);
				}
			}
		}

		// update free power port count
		if (updateFreePowerPort) {
			item.setFreePowerPortCount(freePowerPortCount);
		}
		
	}

	private void deletePowerPortsNotInDTO(Item item, List<PowerPortDTO> powerPortDTOList) throws BusinessValidationException {
		List<Long> delPortIds = getPowerPortDelete(item, powerPortDTOList);
		for (Long delPortId: delPortIds) {
			deletePort(item, delPortId.longValue());
		}
	}
		
	private List<Long> getPowerPortDelete(Item item, List<PowerPortDTO> powerPortDTOList) throws BusinessValidationException {
		List<Long> delPortIds = new ArrayList<Long>();
		if (item.getItemId() <= 0) {
			return delPortIds;
		}
		List<Long> portIds = new ArrayList<Long>();
		for (PowerPortDTO dto: powerPortDTOList) {
			portIds.add(dto.getPortId());
		}
		Set<PowerPort> sList = item.getPowerPorts();
		for (PowerPort pp: sList) {
			if (!portIds.contains(pp.getPortId())) {
				delPortIds.add(pp.getPortId().longValue());
			}
		}
		return delPortIds;
	}

	@Override
	public void deleteOnePortForAnItem(Item item, PortInterface dto) throws BusinessValidationException {
		
		PowerPortDTO powerPortDTO = (PowerPortDTO) dto;
		
		deletePort(item, powerPortDTO.getPortId());
		
		updateItemRedundancy(item, powerPortDTO);
	}
	
	@Override
	public void deletePort(Item item, long powerPortId) throws BusinessValidationException {
		
		Set<PowerPort> ppSet = item.getPowerPorts();
		PowerPort pp = null;
		Iterator<PowerPort> itr = ppSet.iterator();
		while (itr.hasNext()) {
			pp = itr.next();
			if (null != pp.getPortId() && pp.getPortId().longValue() == powerPortId) {
				itr.remove();
			}
		}
	}
	
	private void addConnection(PowerPort pp) {
		PowerConnection connection = new PowerConnection(pp, pp.getInputCordPort(),
				0, null, PortsAdaptor.getLksDataUsingLkpCode(systemLookupFinderDAO, SystemLookup.LinkType.IMPLICIT) /*systemLookupFinderDAO.findByLkpValueCode(SystemLookup.LinkType.IMPLICIT).get(0)*/
				, 2);
		connection.setStatusLookup(PortsAdaptor.getLksDataUsingLkpCode(systemLookupFinderDAO, SystemLookup.ItemStatus.PLANNED) /*systemLookupFinderDAO.findByLkpValueCode(SystemLookup.ItemStatus.PLANNED).get(0)*/);
		// connection.setCreatedBy(sessionUser.getUserName());
		connection.setCreationDate(pp.getCreationDate());
		connection.setUpdateDate(pp.getUpdateDate());
		
		pp.addSourcePowerConnections(connection);
		
	}
	
	private void throwBusinessValidationException (Object args[], String code) throws BusinessValidationException {
		String msg = messageSource.getMessage(code, args, null);	
		BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
		be.addValidationError( msg );
		be.addValidationError(code, msg);
		throw be;
	}
	

	
}
