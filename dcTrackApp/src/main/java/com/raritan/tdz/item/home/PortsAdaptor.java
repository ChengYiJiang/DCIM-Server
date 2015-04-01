package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.service.CircuitProc;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPAddress;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.DataPortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.PowerPortInterface;
import com.raritan.tdz.dto.SensorPortDTO;
import com.raritan.tdz.dto.SensorPortInterface;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.ip.dao.IPAddressDAO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.UnitIntf;

/**
 * Handles conversion of Port DTOs to Port Domain objects and vice versa.
 * @author Andrew Cohen
 */
public class PortsAdaptor {
	
	private static void setIpAddresses(Item item, DataPort port, DataPortInterface dataPortDTO, IPAddressDAO ipAddressDAO) {
		
		// Set the IP Address provided by the client for only the when moved item when it is new
		if (item.getItemToMoveId() == null || item.getItemToMoveId() <= 0 || item.getItemId() > 0) return; 
		
		String ipAddressStr = dataPortDTO.getIpAddress();
		
		if (null == ipAddressStr) return;
		
		List<String> ipAddressesStr = new ArrayList<String>(Arrays.asList(ipAddressStr.split(";")));
		
		if (null == ipAddressesStr) return;
		
		List<IPAddress> ipAddresses = ipAddressDAO.get(ipAddressesStr);
		
		Set<IPAddress> ipAddressesSet = new HashSet<IPAddress>(ipAddresses);
		
		port.setIpAddresses(ipAddressesSet);
		
	}
	
	private static DataPort convertDataPortDTOToDomain(DataPort dataPort, Item item, DataPortInterface dataPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO, IPAddressDAO ipAddressDAO) throws DataAccessException {
		// DataPort dataPort = new DataPort();
		dataPort.setItem( item );
		dataPort.setIpAddress( dataPortDTO.getIpAddress() );
		setIpAddresses(item, dataPort, dataPortDTO, ipAddressDAO);
		dataPort.setIpv6Address( dataPortDTO.getIpv6Address() );
		dataPort.setPortName( dataPortDTO.getPortName() );
//		dataPort.setUsed( dataPortDTO.isUsed() );
		dataPort.setSortOrder( dataPortDTO.getSortOrder() );
		dataPort.setIsRedundant( dataPortDTO.isRedundant() );
		dataPort.setComments( dataPortDTO.getComments() );
		dataPort.setCommunityString( dataPortDTO.getCommunityString() );
		dataPort.setMacAddress( dataPortDTO.getMacAddress() );

		if( dataPortDTO.getPlacementX() == null){
			dataPort.setPlacementX(-1);
		}else{
			dataPort.setPlacementX( dataPortDTO.getPlacementX() );
		}
		if(dataPortDTO.getPlacementY() == null ){
			dataPort.setPlacementY(-1);
		}else{
			dataPort.setPlacementY( dataPortDTO.getPlacementY() );
		}

		Long portId = dataPortDTO.getPortId();
		if (portId != null) {
			dataPort.setPortId( dataPortDTO.getPortId() );
		}
		else {
			dataPort.setPortId( null );
		}

		Long portSubClassValueCode = dataPortDTO.getPortSubClassLksValueCode();
		if (portSubClassValueCode != null && portSubClassValueCode > 0) {
			dataPort.setPortSubClassLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, portSubClassValueCode) /*systemLookupFinderDAO.findByLkpValueCode(portSubClassValueCode).get(0)*/ );
		}
		else {
			dataPort.setPortSubClassLookup( null );
		}

		Long portStatusValueCode = dataPortDTO.getPortStatusLksValueCode();
		if (portStatusValueCode != null && portStatusValueCode > 0) {
			dataPort.setPortStatusLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, portStatusValueCode) /*systemLookupFinderDAO.findByLkpValueCode(portStatusValueCode).get(0)*/ );
		}
		else {
			dataPort.setPortStatusLookup( null );
		}

		Long connectorLkuId = dataPortDTO.getConnectorLkuId();
		if (connectorLkuId != null) {
			dataPort.setConnectorLookup( getConnectorLkuData(connectorLookupFinderDAO, connectorLkuId) /*utilHome.viewConnectorLookupById( connectorLkuId )*/ );
		}
		else {
			dataPort.setConnectorLookup(null);
		}

		Long colorLkuId = dataPortDTO.getColorLkuId();
		if (colorLkuId != null) {
			dataPort.setColorLookup( getLkuDataUsingLkuId(userLookupFinderDAO, colorLkuId) /*utilHome.viewUserLookupById( colorLkuId )*/ );
		}
		else {
			dataPort.setColorLookup(null);
		}

		Long faceLksValueCode = dataPortDTO.getFaceLksValueCode();
		if (faceLksValueCode != null && faceLksValueCode > 0) {
			dataPort.setFaceLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, faceLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(faceLksValueCode).get(0)*/ );
		}
		else {
			dataPort.setFaceLookup(null);
		}

		Long speedLkuId = dataPortDTO.getSpeedLkuId();
		if (speedLkuId != null) {
			dataPort.setSpeedId( getLkuDataUsingLkuId(userLookupFinderDAO, speedLkuId) /*utilHome.viewUserLookupById( speedLkuId )*/ );
		}
		else {
			dataPort.setSpeedId( null );
		}

		Long mediaLksValueCode = dataPortDTO.getMediaLksValueCode();
		if (mediaLksValueCode != null && mediaLksValueCode > 0) {
			dataPort.setMediaId( getLksDataUsingLkpCode(systemLookupFinderDAO, mediaLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(mediaLksValueCode).get(0)*/ );
		}
		else {
			dataPort.setMediaId( null );
		}

		Long protocolLkuId = dataPortDTO.getProtocolLkuId();
		if (protocolLkuId != null) {
			dataPort.setProtocolID( getLkuDataUsingLkuId(userLookupFinderDAO, protocolLkuId) /*utilHome.viewUserLookupById( protocolLkuId )*/ );
		}
		else {
			dataPort.setProtocolID( null );
		}

		Long vlanLkuId = dataPortDTO.getVlanLkuId();
		if (vlanLkuId != null) {
			dataPort.setVlanLookup( getLkuDataUsingLkuId(userLookupFinderDAO, vlanLkuId) /*utilHome.viewUserLookupById( vlanLkuId )*/ );
		}
		else {
			dataPort.setVlanLookup( null );
		}

		Long cableGradeLkuId = dataPortDTO.getCableGradeLkuId();
		if (cableGradeLkuId != null) {
			dataPort.setCableGradeLookup( getLkuDataUsingLkuId(userLookupFinderDAO, cableGradeLkuId) /*utilHome.viewUserLookupById( cableGradeLkuId )*/ );
		}
		else {
			dataPort.setCableGradeLookup( null );
		}

		DataPortInterface linkPort = dataPortDTO.getLinkPort();
		if (linkPort != null) {
			dataPort.setLinkId( linkPort.getPortId() );
		}
		else {
			dataPort.setLinkId( null );
		}
		
		dataPort.setMoveActionLkpValueCode( dataPortDTO.getMoveActionLkpValueCode() );
		
		dataPort.setIpAddressImport( dataPortDTO.getIpAddressImport() );
		
		dataPort.setProxyIndexImport( dataPortDTO.getProxyIndex() );

		return dataPort;
	}

	/**
	 * Converts a data port DTO to a data port domain object.
	 * @param dataPortDTO the dto
	 * @param itemHome item home
	 * @param utilHome util home
	 * @param systemLookupFinderDAO TODO
	 * @param userLookupFinderDAO TODO
	 * @param connectorLookupFinderDAO TODO
	 * @return a data port domain object
	 */
	public static DataPort adaptDataPortDTOToDomain(DataPortInterface dataPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO, IPAddressDAO ipAddressDAO) throws DataAccessException {
		//Item item = itemHome.viewItemEagerById( dataPortDTO.getItemId() );
		Item item = itemDAO.getItem(dataPortDTO.getItemId() );

		if (item == null) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EMPTY_INPUT_ITEM, PortsAdaptor.class, null));
		}
		DataPort dataPort = new DataPort();
		convertDataPortDTOToDomain(dataPort, item, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);

		return dataPort;
	}

	/**
	 * Converts a data port DTO to a data port domain object.
	 * @param item
	 * @param dataPortDTO the dto
	 * @param itemHome item home
	 * @param utilHome util home
	 * @param systemLookupFinderDAO TODO
	 * @param userLookupFinderDAO TODO
	 * @param connectorLookupFinderDAO TODO
	 * @param ipAddressDAO TODO
	 * @return a data port domain object
	 */
	public static DataPort adaptDataPortDTOToNewItemDomain(Item item, DataPortInterface dataPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO, IPAddressDAO ipAddressDAO) throws DataAccessException {
		DataPort dataPort = new DataPort();
		convertDataPortDTOToDomain(dataPort, item, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);

		return dataPort;
	}

	public static DataPort updateDataPortDTOToDomain(DataPort dataPort, DataPortInterface dataPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO, IPAddressDAO ipAddressDAO) throws DataAccessException {
		//Item item = itemHome.viewItemEagerById( dataPortDTO.getItemId() );
		Item item = itemDAO.getItem(dataPortDTO.getItemId() );

		if (item == null) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EMPTY_INPUT_ITEM, PortsAdaptor.class, null));
		}
		convertDataPortDTOToDomain(dataPort, item, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);

		return dataPort;
	}

	public static DataPortDTO adaptDataPortDomainToDTO(DataPort p, Map<Long, LksData> portsAction) {
		if(p == null) return null;

		DataPortDTO port = new DataPortDTO();
		for (IPAddress ip: p.getIpAddresses()) {
			if (null == port.getIpAddress() || port.getIpAddress().length() == 0) {
				port.setIpAddress(ip.getIpAddress() + "; ");
			}
			else {
				port.setIpAddress(port.getIpAddress() + ip.getIpAddress() + "; ");
			}
		}
		// port.setIpAddress(p.getIpAddress());
		port.setIpv6Address(p.getIpv6Address());
		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
		port.setMacAddress(p.getMacAddress());
		port.setPortId(p.getPortId());
		port.setPortName(p.getPortName());
		port.setSortOrder(p.getSortOrder());
		port.setUsed(p.getUsed());
		port.setPlacementX( p.getPlacementX() );
		port.setPlacementY( p.getPlacementY() );
		port.setComments( p.getComments() );
		port.setCommunityString(p.getCommunityString());
		
		if(p.getMediaId() != null){
			port.setMediaLksValueCode( p.getMediaId().getLkpValueCode() );
			port.setMediaLksDesc(p.getMediaId().getLkpValue());
		}

		if(p.getProtocolID() != null){
			port.setProtocolLkuId(p.getProtocolID().getLkuId());
			port.setProtocolLkuDesc(p.getProtocolID().getLkuValue());
		}

		if(p.getSpeedId() != null){
			port.setSpeedLkuId(p.getSpeedId().getLkuId());
			port.setSpeedLkuDesc(p.getSpeedId().getLkuValue());
		}

		if(p.getVlanLookup() != null){
			port.setVlanLkuId(p.getVlanLookup().getLkuId());
			port.setVlanLkuDesc(p.getVlanLookup().getLkuValue());
		}

		if(p.getColorLookup() != null){
			LkuData color = p.getColorLookup();
			port.setColorLkuId( color.getLkuId() );
			port.setColorLkuDesc(color.getLkuValue());
			port.setColorNumber(color.getLkuAttribute());
		}

		if(p.getConnectorLookup() != null){
			port.setConnectorLkuId( p.getConnectorLookup().getConnectorId() );
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		}

		if(p.getPortSubClassLookup() != null){
			port.setPortSubClassLksValueCode(p.getPortSubClassLookup().getLkpValueCode());
			port.setPortSubClassLksDesc(p.getPortSubClassLookup().getLkpValue());
		}

		if (p.getFaceLookup() != null) {
			port.setFaceLksValueCode(p.getFaceLookup().getLkpValueCode());
		}

		if (p.getCableGradeLookup() != null) {
			port.setCableGradeLkuDesc(p.getCableGradeLookup().getLkuValue());
			port.setCableGradeLkuId(p.getCableGradeLookup().getLkuId());
		}

		port.setConnectedItemId(-1L);
		port.setConnectedItemName(null/*connectedItemName*/);
		port.setConnectedPortId(-1L/*connectedPortId*/-1); 
		port.setConnectedPortName(null/*connectedPortName*/);  
		
		// Get the action from the move data table
		// LksData moveAction = dataPortMoveDAO.getMovePortAction(p.getPortId());
		LksData moveAction = null;
		if (null != portsAction) {
			moveAction = portsAction.get(p.getPortId());
		}
		if (null != moveAction) {
			Long moveActionLkpValueCode = moveAction.getLkpValueCode();
			port.setMoveActionLkpValueCode(moveActionLkpValueCode);
			if (!moveActionLkpValueCode.equals(SystemLookup.MoveAction.DONT_CONNECT)) {
				port.setConnectedItemName(moveAction.getLkpValue()); 
			}
		}
		else {
			port.setMoveActionLkpValueCode(null);
		}

		// Link Port?

		return port;
	}

	public static SensorPortDTO adaptSensorPortDomainToDTO(SensorPort p, ItemDAO itemDAO, UnitConverterIntf converter, UnitIntf unit, Object selectedUnit) {
		if(p == null) return null;

		SensorPortDTO port = new SensorPortDTO();

		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
		port.setPortId(p.getPortId());
		port.setPortName(p.getPortName());
		port.setSortOrder(p.getSortOrder());
		port.setUsed(p.getUsed());
		port.setPlacementX( p.getPlacementX() );
		port.setPlacementY( p.getPlacementY() );
		port.setComments( p.getComments() );

		if(p.getColorLookup() != null){
			LkuData color = p.getColorLookup();
			port.setColorLkuId( color.getLkuId() );
			port.setColorLkuDesc(color.getLkuValue());
			port.setColorNumber(color.getLkuAttribute());
		}

		if(p.getConnectorLookup() != null){
			port.setConnectorLkuId( p.getConnectorLookup().getConnectorId() );
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		}

		if(p.getPortSubClassLookup() != null){
			port.setPortSubClassLksValueCode(p.getPortSubClassLookup().getLkpValueCode());
			port.setPortSubClassLksDesc(p.getPortSubClassLookup().getLkpValue());
		}

		if (p.getFaceLookup() != null) {
			port.setFaceLksValueCode(p.getFaceLookup().getLkpValueCode());
		}

		port.setConnectedItemId(-1L); // TODO:: fill this data
		port.setConnectedItemName(null/*connectedItemName*/); // TODO:: fill this data
		port.setConnectedPortId(-1L/*connectedPortId*/-1); // TODO:: fill this data
		port.setConnectedPortName(null/*connectedPortName*/);  // TODO:: fill this data
		port.setCircuitStatusLksValueCode(-1L/*circuitStatusLksValueCode*/); //TODO: fill this data if needed
		port.setCircuitStatusLksValue(null/*circuitStatusLksValue*/);

		//sensor port very specific data
		Long cabinetId = p.getCabinetItemId();
		port.setIsInternal( cabinetId != null ? true : false );
		port.setCabinetId( cabinetId );
		String cabinetName = null;
		if( cabinetId != null ){
			cabinetName = itemDAO.getItemName(cabinetId);
		}
		port.setCabinetName(cabinetName);

		LksData cabLocation = p.getCabLocationLookup();
		if( cabLocation != null ){
			port.setCabLocLksValueCode(cabLocation.getLkpValueCode());
			port.setCabLocLksDesc(cabLocation.getLkpValue());
		}
		port.setXyzLocation(p.getXyzLocation());
		
		Double valueActual = p.getValueActual();
		String spUnit = p.getValueActualUnit();
		
		if (converter != null && selectedUnit != null && ! valueActual.isNaN() && valueActual > -1) {
			Object convertedValue  = converter.convert(p, selectedUnit.toString());
			if (convertedValue != null) valueActual = (Double) convertedValue;

			Object newUnit = unit.getUnit(p, selectedUnit.toString());
			if (newUnit != null) spUnit = (String) newUnit;

		}

		port.setReadingValue(valueActual);
		port.setReadingUnit(spUnit);
		port.setStatusValue(p.getStatusActual()); //for contact-closure, assetStrips and disconnected sensors

		return port;
	}


	private static PowerPort convertPowerPortDTOToDomain(PowerPort powerPort, Item item, PowerPortInterface powerPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		// PowerPort powerPort = new PowerPort();
		powerPort.setItem( item );
		powerPort.setPortName( powerPortDTO.getPortName() );
//		powerPort.setUsed( powerPortDTO.isUsed() );
		powerPort.setSortOrder( powerPortDTO.getSortOrder() );
		powerPort.setIsRedundant( powerPortDTO.isRedundant() );
		powerPort.setComments( powerPortDTO.getComments() );

		if( powerPortDTO.getPlacementX() == null){
			powerPort.setPlacementX(-1);
		}else{
			powerPort.setPlacementX( powerPortDTO.getPlacementX() );
		}
		if(powerPortDTO.getPlacementY() == null ){
			powerPort.setPlacementY(-1);
		}else{
			powerPort.setPlacementY( powerPortDTO.getPlacementY() );
		}

//		powerPort.setAmpsActual( powerPortDTO.getAmpsActual() );
//		powerPort.setAmpsActualA( powerPortDTO.getAmpsActualA() );
//		powerPort.setAmpsActualB( powerPortDTO.getAmpsActualB() );
//		powerPort.setAmpsActualC( powerPortDTO.getAmpsActualC() );
		powerPort.setAmpsBudget( powerPortDTO.getAmpsBudget() );
		powerPort.setAmpsNameplate( powerPortDTO.getAmpsNameplate() );
		// powerPort.setWattsActual( powerPortDTO.getWattsActual() );
		powerPort.setWattsBudget( Long.valueOf(powerPortDTO.getWattsBudget()).intValue() );
		powerPort.setWattsNameplate( Long.valueOf(powerPortDTO.getWattsNameplate()).intValue() );
		powerPort.setPowerFactor( powerPortDTO.getPowerFactor() );
		powerPort.setPolePhase( powerPortDTO.getPolePhase() );
		powerPort.setPiqId( powerPortDTO.getPiqId() );

		Long portId = powerPortDTO.getPortId();
		if (portId != null) {
			powerPort.setPortId( powerPortDTO.getPortId() );
		}
		else {
			powerPort.setPortId( null );
		}

		Long portSubClassValueCode = powerPortDTO.getPortSubClassLksValueCode();
		if (portSubClassValueCode != null && portSubClassValueCode > 0) {
			powerPort.setPortSubClassLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, portSubClassValueCode) /*systemLookupFinderDAO.findByLkpValueCode(portSubClassValueCode).get(0)*/ );
		}
		else {
			powerPort.setPortSubClassLookup( null );
		}

		Long portStatusValueCode = powerPortDTO.getPortStatusLksValueCode();
		if (portStatusValueCode != null && portStatusValueCode > 0) {
			powerPort.setPortStatusLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, portStatusValueCode) /*systemLookupFinderDAO.findByLkpValueCode(portStatusValueCode).get(0)*/ );
		}
		else {
			powerPort.setPortStatusLookup( null );
		}

		Long connectorLkuId = powerPortDTO.getConnectorLkuId();
		if (connectorLkuId != null) {
			powerPort.setConnectorLookup( getConnectorLkuData(connectorLookupFinderDAO, connectorLkuId) /*utilHome.viewConnectorLookupById( connectorLkuId )*/ );
		}
		else {
			powerPort.setConnectorLookup( null );
		}

		Long colorLkuId = powerPortDTO.getColorLkuId();
		if (colorLkuId != null) {
			powerPort.setColorLookup( getLkuDataUsingLkuId(userLookupFinderDAO, colorLkuId) /*utilHome.viewUserLookupById( colorLkuId )*/ );
		}
		else {
			powerPort.setColorLookup( null );
		}

		Long faceLksValueCode = powerPortDTO.getFaceLksValueCode();
		if (faceLksValueCode != null && faceLksValueCode > 0) {
			powerPort.setFaceLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, faceLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(faceLksValueCode).get(0)*/ );
		}
		else {
			powerPort.setFaceLookup( null );
		}

		PowerPortInterface breakerPort = powerPortDTO.getBreakerPort();
		if (breakerPort != null) {
			powerPort.setBreakerPort( adaptPowerPortDTOToDomain(breakerPort, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO) );
		}
		else {
			powerPort.setBreakerPort( null );
		}

		Long fuseLkuId = powerPortDTO.getFuseLkuId();
		if (fuseLkuId != null) {
			powerPort.setFuseLookup( getLkuDataUsingLkuId(userLookupFinderDAO, fuseLkuId) /*utilHome.viewUserLookupById( fuseLkuId )*/ );
		}
		else {
			powerPort.setFuseLookup( null );
		}

		Long polesLksValueCode = powerPortDTO.getPolesLksValueCode();
		if (polesLksValueCode != null && polesLksValueCode > 0) {
			powerPort.setPolesLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, polesLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(polesLksValueCode).get(0)*/ );
		}
		else {
			powerPort.setPolesLookup( null );
		}

		Long phaseLksValueCode = powerPortDTO.getPhaseLksValueCode();
		if (phaseLksValueCode != null && phaseLksValueCode > 0) {
			powerPort.setPhaseLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, phaseLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(phaseLksValueCode).get(0)*/ );
		}
		else {
			powerPort.setPhaseLookup( null );
		}

		Long phaseLegsLksValueCode = powerPortDTO.getPhaseLegsLksValueCode();
		if (phaseLegsLksValueCode != null && phaseLegsLksValueCode > 0) {
			powerPort.setPhaseLegsLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, phaseLegsLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(phaseLegsLksValueCode).get(0)*/ );
		}
		else {
			powerPort.setPhaseLegsLookup( null );
		}

		Long cableGradeLkuId = powerPortDTO.getCableGradeLkuId();
		if (cableGradeLkuId != null) {
			powerPort.setCableGradeLookup( getLkuDataUsingLkuId(userLookupFinderDAO, cableGradeLkuId) /*utilHome.viewUserLookupById( cableGradeLkuId)*/ );
		}
		else {
			powerPort.setCableGradeLookup( null );
		}

		Long voltsLksValueCode = powerPortDTO.getVoltsLksValueCode();
		if (voltsLksValueCode != null) {
			powerPort.setVoltsLookup( getLksDataUsingLkpCodeAndType(utilHome, voltsLksValueCode) /*utilHome.viewSystemLookupByValueCode( voltsLksValueCode,  SystemLookup.LkpType.VOLTS )*/ ); 
		}
		else {
			powerPort.setVoltsLookup( null );
		}

		Long inputCordPortId = powerPortDTO.getInputCordPortId();
		if (inputCordPortId != null) {
			powerPort.setInputCordPort( utilHome.getPowerPort(inputCordPortId) );
		}
		else {
			powerPort.setInputCordPort( null );
		}

		Long buswayItemId = powerPortDTO.getBuswayItemId();
		if (buswayItemId != null && buswayItemId > 0) {
			powerPort.setBuswayItem( itemDAO.getItem( buswayItemId ) );
		}
		else {
			powerPort.setBuswayItem( null );
		}
		
		powerPort.setMoveActionLkpValueCode(powerPortDTO.getMoveActionLkpValueCode());

		return powerPort;
	}

	/**
	 * Converts a data port DTO to a data port domain object.
	 * @param powerPortDTO
	 * @param itemHome
	 * @param utilHome
	 * @param systemLookupFinderDAO TODO
	 * @param userLookupFinderDAO TODO
	 * @param connectorLookupFinderDAO TODO
	 * @return
	 * @throws DataAccessException
	 */
	public static PowerPort adaptPowerPortDTOToDomain(PowerPortInterface powerPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		//Item item = itemHome.viewItemEagerById( powerPortDTO.getItemId() );
		Item item = itemDAO.getItem( powerPortDTO.getItemId() );

		if (item == null) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EMPTY_INPUT_ITEM, PortsAdaptor.class, null));
		}
		PowerPort powerPort = new PowerPort();
		convertPowerPortDTOToDomain(powerPort, item, powerPortDTO, null, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
		return powerPort;
	}

	/**
	 * Converts a data port DTO to a data port domain object.
	 * @param item
	 * @param powerPortDTO
	 * @param itemHome
	 * @param utilHome
	 * @param systemLookupFinderDAO TODO
	 * @param userLookupFinderDAO TODO
	 * @param connectorLookupFinderDAO TODO
	 * @return
	 * @throws DataAccessException
	 */
	public static PowerPort adaptPowerPortDTOToNewItemDomain(Item item, PowerPortInterface powerPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		PowerPort powerPort = new PowerPort();
		convertPowerPortDTOToDomain(powerPort, item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
		return powerPort;
	}

	public static PowerPort updatePowerPortDTOToDomain(PowerPort powerPort, PowerPortInterface powerPortDTO, ItemDAO itemDAO, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		Item item = itemDAO.getItem( powerPortDTO.getItemId() );
		

		if (item == null) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EMPTY_INPUT_ITEM, PortsAdaptor.class, null));
		}
		convertPowerPortDTOToDomain(powerPort, item, powerPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);

		return powerPort;
	}

	public static PowerPortDTO adaptPowerPortDomainToDTO(PowerPort p, Map<Long, LksData> portsAction){
		return adaptPowerPortDomainToDTO(p,  false, portsAction);
	}

	public static PowerPortDTO adaptPowerPortDomainToDTO(PowerPort p,  boolean createPowerPortNodeDTO, Map<Long, LksData> portsAction){
		if(p == null){
			return null;
		}

		PowerPortDTO port;

		if(createPowerPortNodeDTO){
			port = new PowerPortNodeDTO();
		}
		else{
			port = new PowerPortDTO();
		}

		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
		port.setItemClassLksValueCode(p.getItem().getClassLookup().getLkpValueCode());
		if (p.getItem().getSubclassLookup() != null)
			port.setItemSubClassLksValueCode(p.getItem().getSubclassLookup().getLkpValueCode());
		port.setPortId(p.getPortId());
		port.setPortName(p.getPortName());
		port.setSortOrder(p.getSortOrder());
		port.setUsed(p.getUsed());
		port.setAmpsActual(p.getAmpsActual());
		port.setAmpsActualA(p.getAmpsActualA());
		port.setAmpsActualB(p.getAmpsActualB());
		port.setAmpsActualC(p.getAmpsActualC());
		port.setAmpsBudget(p.getAmpsBudget());
		port.setAmpsNameplate(p.getAmpsNameplate());
		port.setPowerFactor(p.getPowerFactor());
		port.setPowerFactorActual(p.getPowerFactorActual());
		port.setRedundant(p.getIsRedundant());
		port.setWattsActual(p.getWattsActual());
		port.setWattsBudget(p.getWattsBudget());
		port.setWattsNameplate(p.getWattsNameplate());
		port.setPlacementX(p.getPlacementX());
		port.setPlacementY(p.getPlacementY());
		port.setComments( p.getComments() );
		
		long itemClass = p.getItem().getClassLookup().getLkpValueCode();
		boolean updateRedundancyApply = (itemClass == SystemLookup.Class.DEVICE ||
											itemClass == SystemLookup.Class.NETWORK ||
											itemClass == SystemLookup.Class.PROBE) && 
											(p.getItem() instanceof ItItem) &&
											(null != p.getItem().getPowerPorts() && p.getItem().getPowerPorts().size() > 0);
		
		if (updateRedundancyApply) {
			ItItem itItem = (ItItem) p.getItem();
			port.setPsRedundancy(itItem.getPsredundancy());
		}

		if(p.getCableGradeLookup() != null) {
			port.setCableGradeLkuId(p.getCableGradeLookup().getLkuId());
			port.setCableGradeLkuDesc(p.getCableGradeLookup().getLkuValue());
		}

		if(p.getPhaseLegsLookup() != null) {
			port.setPhaseLegsLksValueCode(p.getPhaseLegsLookup().getLkpValueCode());
			port.setPhaseLegsLksDesc(p.getPhaseLegsLookup().getLkpValue());
		}

		if(p.getFuseLookup() != null){
			port.setFuseLkuId(p.getFuseLookup().getLkuId());
			port.setFuseLkuDesc(p.getFuseLookup().getLkuValue());
			port.setBreakerName(p.getFuseLookup().getLkuValue());
			port.setBreakerTotalWatts(p.getFuseMaxWatts());
		}

		if(p.getPhaseLookup() != null) {
			port.setPhaseLksValueCode(p.getPhaseLookup().getLkpValueCode());
			port.setPhaseLksDesc(p.getPhaseLookup().getLkpValue());
		}

		if(p.getColorLookup() != null) {
			port.setColorLkuId(p.getColorLookup().getLkuId());
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
			port.setColorNumber( p.getColorLookup().getLkuAttribute() );
		}

		if(p.getConnectorLookup() != null) {
			port.setConnectorLkuId(p.getConnectorLookup().getConnectorId());
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
			port.setPolesLksDesc(p.getConnectorLookup().getConnectorName());
		}

		if (p.getPortSubClassLookup() != null) {
			port.setPortSubClassLksValueCode( p.getPortSubClassLookup().getLkpValueCode() );
			port.setPortSubClassLksDesc(p.getPortSubClassLookup().getLkpValue());
		}

		if (p.getPortStatusLookup() != null) {
			port.setPortStatusLksValueCode( p.getPortStatusLookup().getLkpValueCode() );
		}

		if (p.getVoltsLookup() != null) {
			port.setVoltsLksDesc(p.getVoltsLookup().getLkpValue());
			port.setVoltsLksValueCode(p.getVoltsLookup().getLkpValueCode());
		}

		if (p.getFaceLookup() != null) {
			port.setFaceLksValueCode( p.getFaceLookup().getLkpValueCode() );
		}

		if (p.getInputCordPort() != null && p.getInputCordPort().getPortId() != null) {
			port.setInputCordPortId( p.getInputCordPort().getPortId() );
		}

		if (p.getBuswayItem() != null) {
			port.setBuswayItemId(p.getBuswayItem().getItemId());
		}

		if (p.getPolePhase() != null) {
			port.setPolePhase(p.getPolePhase());
		}

		if (p.getPiqId() != null) {
			port.setPiqId(p.getPiqId());
		}

		if(p.getBreakerPort() != null){
			PowerPort breakerPort = p.getBreakerPort();
			port.setBreakerName(breakerPort.getPortName());
			port.setBreakerAmpsMax(breakerPort.getAmpsNameplate());
			port.setBreakerAmpsRated(GlobalUtils.formatNumberTo0Dec(breakerPort.getAmpsNameplate() * breakerPort.deRatingFactor));
			port.setBreakerTotalWatts(breakerPort.getMaxWatts());
		}

		port.setFreeWatts(p.getMaxWatts());
		port.setTotalWatts(p.getMaxWatts());

		long portSubClass = p.getPortSubClassLookup() != null ? p.getPortSubClassLookup().getLkpValueCode() : 0;

		if(portSubClass == SystemLookup.PortSubClass.RACK_PDU_OUTPUT){
			if (!Double.isNaN(p.getAmpsNameplate())) {
			    port.setAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * p.upRatingFactor));
			    port.setAmpsRated(p.getAmpsNameplate());
			}

			if(p.getFuseLookup() != null){
				port.setBreakerName(p.getFuseLookup().getLkuValue());
				if (!Double.isNaN(p.getAmpsBudget())) {
					port.setBreakerAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsBudget() * p.upRatingFactor));
					port.setBreakerAmpsRated(p.getAmpsBudget());
				}
				port.setBreakerTotalWatts(p.getFuseMaxWatts());
			}
		}else if(portSubClass == SystemLookup.PortSubClass.INPUT_CORD){
			if (!Double.isNaN(p.getAmpsNameplate())) {
				port.setAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * p.upRatingFactor));
				port.setAmpsRated(p.getAmpsNameplate());
			}
		}else if(port.getItemClassLksValueCode() == SystemLookup.Class.FLOOR_OUTLET ){
			if (!Double.isNaN(p.getAmpsNameplate())) {
				port.setAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * p.upRatingFactor));
				port.setAmpsRated(p.getAmpsNameplate());
			}
		}else if(portSubClass == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER){
			if (!Double.isNaN(p.getAmpsNameplate())) {
				port.setAmpsMax(p.getAmpsNameplate());
				port.setAmpsRated(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * p.deRatingFactor));
			}

			Double total = GlobalUtils.formatNumberTo0Dec(p.getMaxWatts() * p.deRatingFactor);
			port.setTotalWatts(total.longValue());
		}else if(portSubClass == SystemLookup.PortSubClass.PANEL_BREAKER || portSubClass == SystemLookup.PortSubClass.PDU_INPUT_BREAKER
				|| portSubClass == SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER){
			if (!Double.isNaN(p.getAmpsNameplate())) {
				port.setAmpsMax(p.getAmpsNameplate());
				port.setAmpsRated(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * p.deRatingFactor));
			}

			Double totalWatts = 0.0;
			
			if (isThreePhase(port.getPhaseLksValueCode())){
				//totalWatts = itemPB.getLineVolts() * node.getAmpsRated() * Math.sqrt(3);
				if( port.getVoltsLksDesc() != null){
					totalWatts = Double.valueOf(port.getVoltsLksDesc()) * port.getAmpsRated() * Math.sqrt(3);
				}
			} else {
				if( port.getVoltsLksDesc() != null ){
					totalWatts = Double.valueOf(port.getVoltsLksDesc()) * port.getAmpsRated();
				}
			}
			
			port.setTotalWatts(totalWatts.longValue());
		}

		port.setConnectedItemId(-1L);
		port.setConnectedItemName(null/*connectedItemName*/);
		port.setConnectedPortId(-1L/*connectedPortId*/-1);
		port.setConnectedPortName(null/*connectedPortName*/); 
		port.setCircuitStatusLksValueCode(-1L /*circuitStatusLksValueCode */); 
		port.setCircuitStatusLksValue(null /*circuitStatusLksValue */);
		
		LksData moveAction = null;
		if (null != portsAction) {
			moveAction = portsAction.get(p.getPortId());
		}
		// LksData moveAction = powerPortMoveDAO.getMovePortAction(p.getPortId());
		if (null != moveAction) {
			Long moveActionLkpValueCode = moveAction.getLkpValueCode();
			port.setMoveActionLkpValueCode(moveActionLkpValueCode);
			if (!moveActionLkpValueCode.equals(SystemLookup.MoveAction.DONT_CONNECT)) {
				port.setConnectedItemName(moveAction.getLkpValue());
			}
		}
		else {
			port.setMoveActionLkpValueCode(null);
		}

		return port;
	}
	
	public static SensorPort adaptSensorPortDTOToNewItemDomain(Item item,
			SensorPortDTO sensorPortDTO,
			com.raritan.tdz.item.home.ItemHome itemHome, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		SensorPort sensorPort = new SensorPort();
		convertSensorPortDTOToDomain(sensorPort, item, sensorPortDTO, itemHome, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);

		return sensorPort;
	}

	private static SensorPort convertSensorPortDTOToDomain(SensorPort sensorPort, Item item,
			SensorPortInterface sensorPortDTO, ItemHome itemHome, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		sensorPort.setItem( item );
		sensorPort.setPortName( sensorPortDTO.getPortName() );
		sensorPort.setSortOrder( sensorPortDTO.getSortOrder() );
		sensorPort.setComments( sensorPortDTO.getComments() );
		//We should not be setting actual value and its unit coming from the web client to the server as these fields are read-only
		//These values are always set by the powerIQ integration module.
//		if (Double.valueOf(sensorPortDTO.getReadingValue()).equals(Double.NaN)){
//			sensorPort.setValueActual( -1 );
//		} else {
//			sensorPort.setValueActual( sensorPortDTO.getReadingValue());
//		}
//		sensorPort.setValueActualUnit(sensorPortDTO.getReadingUnit());

		Long portId = sensorPortDTO.getPortId();
		if (portId != null) {
			sensorPort.setPortId( sensorPortDTO.getPortId() );
		}
		else {
			sensorPort.setPortId( null );
		}

		Long portSubClassValueCode = sensorPortDTO.getPortSubClassLksValueCode();
		if (portSubClassValueCode != null && portSubClassValueCode > 0) {
			sensorPort.setPortSubClassLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, portSubClassValueCode) /*systemLookupFinderDAO.findByLkpValueCode(portSubClassValueCode).get(0)*/ );
		}
		else {
			sensorPort.setPortSubClassLookup( null );
		}

		Long portStatusValueCode = sensorPortDTO.getPortStatusLksValueCode();
		if (portStatusValueCode != null && portStatusValueCode > 0) {
			sensorPort.setPortStatusLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, portStatusValueCode) /*systemLookupFinderDAO.findByLkpValueCode(portStatusValueCode).get(0)*/ );
		}
		else {
			sensorPort.setPortStatusLookup( null );
		}

		Long connectorLkuId = sensorPortDTO.getConnectorLkuId();
		if (connectorLkuId != null) {
			sensorPort.setConnectorLookup( getConnectorLkuData(connectorLookupFinderDAO, connectorLkuId) /*utilHome.viewConnectorLookupById( connectorLkuId )*/ );
		}
		else {
			sensorPort.setConnectorLookup(null);
		}

		Long colorLkuId = sensorPortDTO.getColorLkuId();
		if (colorLkuId != null) {
			sensorPort.setColorLookup( getLkuDataUsingLkuId(userLookupFinderDAO, colorLkuId) /*utilHome.viewUserLookupById( colorLkuId )*/ );
		}
		else {
			sensorPort.setColorLookup(null);
		}

		Long faceLksValueCode = sensorPortDTO.getFaceLksValueCode();
		if (faceLksValueCode != null && faceLksValueCode > 0) {
			sensorPort.setFaceLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, faceLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(faceLksValueCode).get(0)*/ );
		}
		else {
			sensorPort.setFaceLookup(null);
		}
		sensorPort.setIsInternal( sensorPortDTO.isInternal() );
		
		Long cabinetId = sensorPortDTO.getCabinetId();
		if( cabinetId == null || cabinetId == 0 ){
			sensorPort.setCabinetItemId(null);
			sensorPort.setCabinetItem(null);
			sensorPort.setCabLocationLookup(null);
		}else{
			sensorPort.setCabinetItemId(sensorPortDTO.getCabinetId());
			Item cabinet = itemHome.viewItemEagerById( sensorPortDTO.getCabinetId());
			sensorPort.setCabinetItem(cabinet);
		}

		Long positionLksValueCode = sensorPortDTO.getCabLocLksValueCode();
		if( positionLksValueCode != null && positionLksValueCode > 0) {
			sensorPort.setCabLocationLookup( getLksDataUsingLkpCode(systemLookupFinderDAO, positionLksValueCode) /*systemLookupFinderDAO.findByLkpValueCode(positionLksValueCode).get(0)*/ );
		}

		sensorPort.setXyzLocation(sensorPortDTO.getXyzLocation());
		
		return sensorPort;
	}

	public static SensorPort updateSensorPortDTOToDomain(SensorPort sensorPort,
			SensorPortDTO sensorPortDTO,
			com.raritan.tdz.item.home.ItemHome itemHome, UtilHome utilHome, SystemLookupFinderDAO systemLookupFinderDAO, UserLookupFinderDAO userLookupFinderDAO, ConnectorLookupFinderDAO connectorLookupFinderDAO) throws DataAccessException {
		Item item = itemHome.viewItemEagerById( sensorPortDTO.getItemId() );

		if (item == null) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EMPTY_INPUT_ITEM, PortsAdaptor.class, null));
		}
		convertSensorPortDTOToDomain(sensorPort, item, sensorPortDTO, itemHome, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);

		return sensorPort;
	}
	
	public static boolean isThreePhase(Long phaseLksValueCode) {
		boolean threePhase = false;

		if (null != phaseLksValueCode && (phaseLksValueCode == SystemLookup.PhaseIdClass.THREE_DELTA
				|| phaseLksValueCode == SystemLookup.PhaseIdClass.THREE_WYE)) {
			threePhase = true;
		}

		return threePhase;
	}

	private static Map<Long, LksData> portLkpValueCodeLksData = new HashMap<Long, LksData>();
	
	public static LksData getLksDataUsingLkpCode(SystemLookupFinderDAO systemLookupFinderDAO, Long lkpValueCode) {
		
		if (lkpValueCode <=0) return null;
		
		LksData lksData = portLkpValueCodeLksData.get(lkpValueCode);
		if (null == lksData) {
			lksData = systemLookupFinderDAO.findByLkpValueCode(lkpValueCode).get(0);
			portLkpValueCodeLksData.put(lkpValueCode, lksData);
		}
		
		return lksData;
	}

	private static Map<Long, LkuData> lkuIdLkuData = new HashMap<Long, LkuData>();
	
	public static LkuData getLkuDataUsingLkuId(UserLookupFinderDAO userLookupFinderDAO, Long lkuId) {
		
		if (lkuId <= 0) return null;
		
		LkuData lkuData = lkuIdLkuData.get(lkuId);
		if (null == lkuData) {
			lkuData = userLookupFinderDAO.findById(lkuId).get(0);
			lkuIdLkuData.put(lkuId, lkuData);
		}
		
		return lkuData;
	}

	private static Map<Long, ConnectorLkuData> connectorLkuData = new HashMap<Long, ConnectorLkuData>();
	
	public static ConnectorLkuData getConnectorLkuData(ConnectorLookupFinderDAO connectorLookupFinderDAO, Long connectorLkuId) {
		
		if (connectorLkuId <= 0) return null;
		
		ConnectorLkuData connLkuData = connectorLkuData.get(connectorLkuId);
		if (null == connLkuData) {
			connLkuData = connectorLookupFinderDAO.findById(connectorLkuId).get(0);
			connectorLkuData.put(connectorLkuId, connLkuData);
		}
		
		return connLkuData;
	}
	
	private static Map<Long, LksData> voltsLksData = new HashMap<Long, LksData>();
	
	public static LksData getLksDataUsingLkpCodeAndType(UtilHome utilHome, Long lkpValueCode) throws DataAccessException {
		
		if (lkpValueCode < 0) return null;
		
		LksData voltLksData = voltsLksData.get(lkpValueCode);
		if (null == voltLksData) {
			voltLksData = utilHome.viewSystemLookupByValueCode( lkpValueCode,  SystemLookup.LkpType.VOLTS );
			voltsLksData.put(lkpValueCode, voltLksData);
		}

		return voltLksData;
	}
	
	
	public static DataPortDTO adaptTicketDataPortDomainToDTO(TicketPortsData p) {
		if(p == null) return null;

		DataPortDTO port = new DataPortDTO();
		
		port.setItemId(p.getTicketFields().getItemId());
		port.setItemName(p.getTicketFields().getItemName());
		port.setMacAddress(p.getMacAddress());
		port.setPortName(p.getPortName());
		
		if(p.getMediaId() != null){
			port.setMediaLksValueCode( p.getMediaId().getLkpValueCode() );
			port.setMediaLksDesc(p.getMediaId().getLkpValue());
		} else {
			port.setMediaLksDesc(p.getMedia());
		}

		if(p.getProtocolID() != null){
			port.setProtocolLkuId(p.getProtocolID().getLkuId());
			port.setProtocolLkuDesc(p.getProtocolID().getLkuValue());
		} else {
			port.setProtocolLkuDesc(p.getProtocol());
		}

		if(p.getSpeedId() != null){
			port.setSpeedLkuId(p.getSpeedId().getLkuId());
			port.setSpeedLkuDesc(p.getSpeedId().getLkuValue());
		} else {
			port.setSpeedLkuDesc(p.getSpeed());
		}

		if(p.getVlanLookup() != null){
			port.setVlanLkuId(p.getVlanLookup().getLkuId());
			port.setVlanLkuDesc(p.getVlanLookup().getLkuValue());
		} else {
			port.setVlanLkuDesc(p.getVlan());
		}

		if(p.getColorLookup() != null){
			LkuData color = p.getColorLookup();
			port.setColorLkuId( color.getLkuId() );
			port.setColorLkuDesc(color.getLkuValue());
			port.setColorNumber(color.getLkuAttribute());
		} else {
			port.setColorLkuDesc(p.getColor());
		}

		if(p.getConnectorLookup() != null){
			port.setConnectorLkuId( p.getConnectorLookup().getConnectorId() );
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		} else {
			port.setConnectorName(p.getConnector());
		}

		if(p.getPortSubclassId() != null){
			port.setPortSubClassLksValueCode(p.getPortSubclassId().getLkpValueCode());
			port.setPortSubClassLksDesc(p.getPortSubclassId().getLkpValue());
		} else {
			port.setPortSubClassLksDesc(p.getPortType());
		}

		port.setConnectedItemId(-1L);
		port.setConnectedItemName(null/*connectedItemName*/);
		port.setConnectedPortId(-1L/*connectedPortId*/-1); 
		port.setConnectedPortName(null/*connectedPortName*/);  
		

		return port;
	}
	
	public static PowerPortDTO adaptTicketPowerPortDomainToDTO(TicketPortsPower p, int index){
		if(p == null){
			return null;
		}

		PowerPortDTO port = new PowerPortDTO();

		port.setItemId(p.getTicketFields().getItemId());
		port.setItemName(p.getTicketFields().getItemName());
		port.setPortName(p.getPortNamePrefix() + index);
		
		port.setPowerFactor(p.getPowerFactor());
		port.setWattsBudget(p.getWattsBudget());
		port.setWattsNameplate(p.getWattsNamePlate());
		port.setPhaseLksDesc(p.getPhase());
		port.setConnectorName(p.getConnector());
		port.setColorLkuDesc(p.getColor());
		

		if(p.getColorLookup() != null) {
			port.setColorLkuId(p.getColorLookup().getLkuId());
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
			port.setColorNumber( p.getColorLookup().getLkuAttribute() );
		}

		if(p.getConnectorLookup() != null) {
			port.setConnectorLkuId(p.getConnectorLookup().getConnectorId());
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
			port.setPolesLksDesc(p.getConnectorLookup().getConnectorName());
		}

		if (p.getPortSubclassId() != null) {
			port.setPortSubClassLksValueCode( p.getPortSubclassId().getLkpValueCode() );
			port.setPortSubClassLksDesc(p.getPortSubclassId().getLkpValue());
		} else {
			port.setPortSubClassLksDesc(p.getPortType());
		}


		if (p.getVoltsId() != null) {
			port.setVoltsLksDesc(p.getVoltsId().getLkpValue());
			port.setVoltsLksValueCode(p.getVoltsId().getLkpValueCode());
		} else {
			port.setVoltsLksDesc(p.getVolts());
		}

	

		port.setConnectedItemId(-1L);
		port.setConnectedItemName(null/*connectedItemName*/);
		port.setConnectedPortId(-1L/*connectedPortId*/-1);
		port.setConnectedPortName(null/*connectedPortName*/); 
		

		return port;
	}

}
