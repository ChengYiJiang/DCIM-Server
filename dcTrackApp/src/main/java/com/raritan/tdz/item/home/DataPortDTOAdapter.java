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
import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.util.GlobalConstants;



public class DataPortDTOAdapter extends DTOAdapterBase {
    @Autowired(required=true)
    private SystemLookupFinderDAO systemLookupDao;

    @Autowired(required=true)
    private UserLookupFinderDAO userLookup;

    @Autowired(required=true)
    private ConnectorLookupFinderDAO connectorLookupFinderDAO;

	@Autowired(required=true)
	private DataCircuitDAO dataCircuitDAO;
	
	@Autowired(required=true)
	private DataPortDAO dataPortDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;
    

	public DataPortDTO adaptDataPortToDTO(DataPort dp, long itemId ){
		List<Long> movePortIds = new ArrayList<Long>();
		movePortIds.add(dp.getPortId());
		Map<Long, LksData> portsAction = dataPortMoveDAO.getMovePortAction(movePortIds);
		DataPortDTO dto = PortsAdaptor.adaptDataPortDomainToDTO(dp, portsAction);
		
		HashMap<Long, PortInterface> portMap = dataCircuitDAO.getDestinationItemsForItem(itemId);

		if( portMap != null ){
			DataPortDTO portIf = (DataPortDTO)portMap.get(dto.getPortId());
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
	public void adaptDataPortFromJson( DataPortDTO dto, Errors errors ){
		
		Long portId = dto.getPortId();
		Long itemId = dto.getItemId();
		
		if(dto.getPortSubClassLksDesc() != null && dto.getPortSubClassLksDesc().length() > 0 ){
			List<LksData> lksList = systemLookupDao.findByLkpValueAndTypeCaseInsensitive(dto.getPortSubClassLksDesc(), SystemLookup.LkpType.PORT_SUBCLASS);
			if( lksList != null && lksList.size() == 1 ){
				LksData l=lksList.get(0);
				dto.setPortSubClassLksValueCode(l.getLkpValueCode());
				dto.getDirtyFlagMap().put("setPortSubClassLksValueCode", true);
			}else{
				Object[] errorArgs = { "Type" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
		if( dto.getMediaLksDesc() != null && dto.getMediaLksDesc().length() > 0 ){
			List<LksData>lksList = systemLookupDao.findByLkpValueAndTypeCaseInsensitive(dto.getMediaLksDesc(), SystemLookup.LkpType.MEDIA);
			if( lksList != null && lksList.size() == 1 ){
				LksData l=lksList.get(0);
				dto.setMediaLksValueCode(l.getLkpValueCode());
				dto.getDirtyFlagMap().put("setMediaLksValueCode", true);
			}else{
				Object[] errorArgs = { "Media" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
		
		if( dto.getSpeedLkuDesc() != null && dto.getSpeedLkuDesc().length() > 0 ){
			List<LkuData> lkuList = userLookup.findByLkpValueAndTypeCaseInsensitive(dto.getSpeedLkuDesc(), GlobalConstants.speed);
			
			if( lkuList != null && lkuList.size() == 1 ){
				LkuData l = lkuList.get(0);
				dto.setSpeedLkuId(l.getLkuId());
				dto.getDirtyFlagMap().put("setSpeedLkuId", true);
			}else{
				Object[] errorArgs = { "Data Rate" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
		
		if( dto.getProtocolLkuDesc() != null && dto.getProtocolLkuDesc().length() > 0 ){
			List<LkuData>lkuList = userLookup.findByLkpValueAndTypeCaseInsensitive(dto.getProtocolLkuDesc(), GlobalConstants.protocol);
			if( lkuList != null && lkuList.size() == 1 ){
				LkuData l = lkuList.get(0);
				dto.setProtocolLkuId(l.getLkuId());
				dto.getDirtyFlagMap().put("setProtocolLkuId", true);
			}else{
				Object[] errorArgs = { "Protocol" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
			
		if( dto.getVlanLkuDesc() != null && dto.getVlanLkuDesc().length() > 0 ){
			List<LkuData> lkuList = userLookup.findByLkpValueAndTypeCaseInsensitive(dto.getVlanLkuDesc(), GlobalConstants.vlan);
			if( lkuList != null && lkuList.size() == 1 ){
				LkuData l = lkuList.get(0);
				dto.setVlanLkuId(l.getLkuId());
				dto.getDirtyFlagMap().put("setVlanLkuId", true);
			}else{
				Object[] errorArgs = { "Grouping/VLAN" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
		
		if( dto.getColorLkuDesc() != null && dto.getColorLkuDesc().length() > 0 ){
			List<LkuData>lkuList = userLookup.findByLkpValueAndTypeCaseInsensitive(dto.getColorLkuDesc(), GlobalConstants.color);
			if( lkuList != null && lkuList.size() == 1 ){
				LkuData l = lkuList.get(0);
				dto.setColorLkuId(l.getLkuId());
				dto.getDirtyFlagMap().put("setColorLkuId", true);
			}else{
				Object[] errorArgs = { "Color Code" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
		
		dto.getDirtyFlagMap().put("setConnector", dto.getDirtyFlagMap().get("setConnectorName"));
		dto.getDirtyFlagMap().put("setConnectorLkuId", dto.getDirtyFlagMap().get("setConnectorName"));
		dto.getDirtyFlagMap().put("setMediaLksValueCode", dto.getDirtyFlagMap().get("setMediaLksDesc"));
		
		if( dto.getConnectorName() != null && dto.getConnectorName().length() > 0) {
			List<ConnectorLkuData>connectorList  = connectorLookupFinderDAO.findByNameCaseInsensitive(dto.getConnectorName());
			if( connectorList != null && connectorList.size() == 1 ){
				ConnectorLkuData connector = connectorList.get(0);
				dto.setConnectorLkuId(connector.getConnectorId());
			}else{
				Object[] errorArgs = { "Connector" };
				errors.reject("PortValidator.dataIncorrectFieldValue", errorArgs, "Invalid data port field value");
			}
		}
		
		if (dto.getIpAddressImport() != null) {
			dto.getDirtyFlagMap().put("setIpAddressImport", true);
		}
		if (dto.getProxyIndex() != null) {
			dto.getDirtyFlagMap().put("setProxyIndex", true);
		}
		
		//ensure that portId and itemId have corect values since REST API client may not send them
		dto.setPortId(portId);
		dto.getDirtyFlagMap().put("setPortId", true);
		dto.setItemId(itemId);
		dto.getDirtyFlagMap().put("setItemId", true);
		
		if (itemId != null && itemId > 0 && portId != null && portId > 0){
			handleExistingPort(dto, portId);
		}
		
	}

	private void handleExistingPort(DataPortDTO dto, Long portId) {
		//Load the port
		DataPort dataPort = dataPortDAO.read(portId);
		//Get a dto from the domain object
		List<Long> movePortIds = new ArrayList<Long>();
		movePortIds.add(portId);
		Map<Long, LksData> portsAction = dataPortMoveDAO.getMovePortAction(movePortIds);
		DataPortDTO existingPortDTO = PortsAdaptor.adaptDataPortDomainToDTO(dataPort, portsAction);

		//Make sure that we have existing data filled up into the dto.
		try {
			for (PropertyDescriptor pd: Introspector.getBeanInfo(DataPortDTO.class).getPropertyDescriptors()){
				if (pd.getReadMethod() != null && !"class".equals(pd.getName()) && pd.getWriteMethod() != null){
					Object existing = pd.getReadMethod().invoke(existingPortDTO);
					Boolean flag = dto.getDirtyFlagMap().get(pd.getWriteMethod().getName());
					if (flag == null || !flag){
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
