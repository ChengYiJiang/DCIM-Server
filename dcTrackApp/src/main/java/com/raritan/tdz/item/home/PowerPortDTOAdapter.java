package com.raritan.tdz.item.home;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.adapter.DTOAdapterBase;
import com.raritan.tdz.cache.ConnectorLkuCache;
import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.GlobalConstants;

/**
 * adapts the power port dto from rest API format to the format that server understands and process
 * @author bunty
 *
 */
public class PowerPortDTOAdapter extends DTOAdapterBase {

	@Autowired(required=true)
	private PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
    @Autowired(required=true)
    private UserLookupFinderDAO userLookup;

    @Autowired
    private ConnectorLkuCache connectorLkuCache;
    
    @Autowired
    private LksCache lksCache;

	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;
	
	public PowerPortDTO adaptPowerPortToDTO(PowerPort pp, long itemId ){
		List<Long> movePortIds = new ArrayList<Long>();
		movePortIds.add(pp.getPortId());
		Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);
		PowerPortDTO dto = PortsAdaptor.adaptPowerPortDomainToDTO(pp, portsAction);
		
		HashMap<Long, PortInterface> portMap = powerCircuitDAO.getDestinationItemsForItem(itemId);

		if( portMap != null ){
			PowerPortDTO portIf = (PowerPortDTO)portMap.get(dto.getPortId());
			if( portIf != null ){
				dto.setConnectedItemId(portIf.getConnectedItemId());
				dto.setConnectedItemName(portIf.getConnectedItemName());
				dto.setConnectedPortId(portIf.getConnectedPortId());
				dto.setConnectedPortName(portIf.getConnectedPortName());
			}
		}
		return dto;
	}
	
	/*
	 * IN -> from RESTvi API client to server
	 * 
	 * This method generated lks values in DTO using xxxDesc fields because RESTAPI
	 * call send only lkpValue whiel server works mostly on lksId
	 */
	public void adaptPowerPortFromJson( PowerPortDTO dto, Errors errors ) {
		
		Long portId = dto.getPortId();
		Long itemId = dto.getItemId();
		
		if(dto.getPortSubClassLksDesc() != null && dto.getPortSubClassLksDesc().length() > 0 ) {
			LksData portSubClassLks = lksCache.getLksDataUsingLkpAndType(dto.getPortSubClassLksDesc(), SystemLookup.LkpType.PORT_SUBCLASS);
			if( portSubClassLks != null ){
				dto.setPortSubClassLksValueCode(portSubClassLks.getLkpValueCode());
				dto.getDirtyFlagMap().put("setPortSubClassLksValueCode", true);
			}else{
				Object[] errorArgs = { "type" };
				errors.reject("PortValidator.powerIncorrectFieldValue", errorArgs, "Invalid power port field value");
			}
		}
		
		// connector
		if( dto.getConnectorName() != null && dto.getConnectorName().length() > 0 ){

			ConnectorLkuData connLkuData = connectorLkuCache.getConnectorLkuData(dto.getConnectorName());
			
			if( null != connLkuData ){
				dto.setConnectorLkuId(connLkuData.getConnectorId());
				dto.getDirtyFlagMap().put("setConnectorLkuId", true);
			}else{
				Object[] errorArgs = { "connector" };
				errors.reject("PortValidator.powerIncorrectFieldValue", errorArgs, "Invalid power port connector field value");
			}
		}

		// phase
		if( dto.getPhaseLksDesc() != null && dto.getPhaseLksDesc().length() > 0 ) {
			
			LksData phaseLks = lksCache.getLksDataUsingLkpAndType(dto.getPhaseLksDesc(), SystemLookup.LkpType.PHASE_ID);
			
			if( null != phaseLks ){
				dto.setPhaseLksValueCode(phaseLks.getLkpValueCode());
				dto.getDirtyFlagMap().put("setPhaseLksValueCode", true);
			}else{
				Object[] errorArgs = { "phase" };
				errors.reject("PortValidator.powerIncorrectFieldValue", errorArgs, "Invalid power port field value");
			}
		}

		// volts
		if( dto.getVoltsLksDesc() != null && dto.getVoltsLksDesc().length() > 0 ) {
			
			LksData voltsLks = lksCache.getLksDataUsingLkpAndType(dto.getVoltsLksDesc(), SystemLookup.LkpType.VOLTS);
			
			if( null != voltsLks ){
				dto.setVoltsLksValueCode(voltsLks.getLkpValueCode());
				dto.getDirtyFlagMap().put("setVoltsLksValueCode", true);
			}else{
				Object[] errorArgs = { "volts" };
				errors.reject("PortValidator.powerIncorrectFieldValue", errorArgs, "Invalid power port field value");
			}
		}

		// color
		if( dto.getColorLkuDesc() != null && dto.getColorLkuDesc().length() > 0 ){
			List<LkuData>lkuList = userLookup.findByLkpValueAndTypeCaseInsensitive(dto.getColorLkuDesc(), GlobalConstants.color);
			if( lkuList != null && lkuList.size() == 1 ){
				LkuData l = lkuList.get(0);
				dto.setColorLkuId(l.getLkuId());
				dto.getDirtyFlagMap().put("setColorLkuId", true);
				dto.getDirtyFlagMap().put("setColorNumber", true);
			}else{
				Object[] errorArgs = { "colorCode" };
				errors.reject("PortValidator.powerIncorrectFieldValue", errorArgs, "Invalid power port field value");
			}
		}
		
		// power factor 
		if ( dto.getPowerFactor() != 0 ) {
			dto.getDirtyFlagMap().put("setPowerFactor", true);
		}
		
		// watts name plate
		if ( dto.getWattsNameplate() != 0 ) {
			dto.getDirtyFlagMap().put("setWattsNameplate", true);
		}
		
		// watts budget
		if ( dto.getWattsBudget() != 0 ) {
			dto.getDirtyFlagMap().put("setWattsBudget", true);
		}
		
		if (null != dto.getPsRedundancy() && dto.getPsRedundancy().length() > 0) {
			dto.getDirtyFlagMap().put("setPsRedundancy", true);
		}
		
		dto.getDirtyFlagMap().put("setConnector", dto.getDirtyFlagMap().get("setConnectorName"));
		dto.getDirtyFlagMap().put("setConnectorLkuId", dto.getDirtyFlagMap().get("setConnectorName"));
		
		//ensure that portId and itemId have correct values since REST API client may not send them
		dto.setPortId(portId);
		dto.getDirtyFlagMap().put("setPortId", true);
		dto.setItemId(itemId);
		dto.getDirtyFlagMap().put("setItemId", true);
		
		if (itemId != null && itemId > 0 && portId != null && portId > 0){
			handleExistingPort(dto, portId);
		}
		
	}

	private void handleExistingPort(PowerPortDTO dto, Long portId) {
		//Load the port
		PowerPort powerPort = powerPortDAO.read(portId);
		//Get a dto from the domain object
		List<Long> movePortIds = new ArrayList<Long>();
		movePortIds.add(portId);
		Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);
		PowerPortDTO existingPortDTO = PortsAdaptor.adaptPowerPortDomainToDTO(powerPort, portsAction);

		//Make sure that we have existing data filled up into the dto.
		try {
			for (PropertyDescriptor pd: Introspector.getBeanInfo(PowerPortDTO.class).getPropertyDescriptors()){
				if (pd.getReadMethod() != null && !"class".equals(pd.getName()) && pd.getWriteMethod() != null){
					Object existing = pd.getReadMethod().invoke(existingPortDTO);
					Boolean flag = dto.getDirtyFlagMap().get(pd.getWriteMethod().getName());
					if (flag == null)
						pd.getWriteMethod().invoke(dto,existing);
					else if (flag != null && !flag){
						pd.getWriteMethod().invoke(dto,existing);
					}
				}
			}
		} catch (IntrospectionException e) {
			if (getLog().isDebugEnabled())
				e.printStackTrace();
		} catch (IllegalArgumentException e) {
			if (getLog().isDebugEnabled())
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			if (getLog().isDebugEnabled())
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if (getLog().isDebugEnabled())
			e.printStackTrace();
		}
	}


}
