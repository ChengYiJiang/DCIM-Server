/**
 * 
 */
package com.raritan.tdz.model.home;

import com.raritan.tdz.domain.ModelDataPorts;
import com.raritan.tdz.domain.ModelPowerPorts;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortConnectorDTO;
import com.raritan.tdz.dto.PowerPortDTO;

/**
 * Adaptor class for adapting DTO <-> Domain 
 * @author basker
 *
 */
public class ModelPortAdapter {

	public static DataPortDTO  adaptModelPortsDomainToDataPortDTO (ModelDataPorts p) {

		DataPortDTO port = new DataPortDTO();
		
		// set name
		if (p.getPortName() != null) {
			port.setPortName(p.getPortName());
		}

		// set connector
		PortConnectorDTO pcDto = new PortConnectorDTO();
		if (p.getConnectorLookup() != null && pcDto != null) {
			pcDto.setConnectorId(p.getConnectorLookup().getConnectorId());
			pcDto.setConnectorName(p.getConnectorLookup().getConnectorName());
			port.setConnector(pcDto);
			port.setConnectorLkuId(p.getConnectorLookup().getConnectorId());
		}
		
		// set media 
		if (p.getMediaLookup() != null) {
			port.setMediaLksValueCode(p.getMediaLookup().getLkpValueCode());
			port.setMediaLksDesc(p.getMediaLookup().getLkpValue());
		}
		
		//set protocol
		if (p.getProtocolLookup() != null) {
			port.setProtocolLkuId(p.getProtocolLookup().getLkuId());
			port.setProtocolLkuDesc(p.getProtocolLookup().getLkuValue());
		}
		
		//set speed
		if (p.getSpeedLookup() != null) {
			port.setSpeedLkuId(p.getSpeedLookup().getLkuId());
			port.setSpeedLkuDesc(p.getSpeedLookup().getLkuValue());
		}

		if (p.getPortSubclassLookup() != null) {
			port.setPortSubClassLksValueCode(p.getPortSubclassLookup().getLkpValueCode());
			port.setPortSubClassLksDesc(p.getPortSubclassLookup().getLkpValue());
		}
		
		if (p.getColorLookup() != null) {
			port.setColorLkuId(p.getColorLookup().getLkuId());
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
			port.setColorNumber(p.getColorLookup().getLkuAttribute());
		}
		
		if (p.getVlanLookup() != null) {
			port.setVlanLkuDesc(p.getVlanLookup().getLkuValue());
			port.setVlanLkuId(p.getVlanLookup().getLkuId());
		}

		port.setPlacementX(p.getPlacementX());
		port.setPlacementY(p.getPlacementY());
		if (null != p.getFaceLookup()) {
			port.setFaceLksValueCode(p.getFaceLookup().getLkpValueCode());
		}
		
		port.setRedundant(p.isRedundant());
		
		port.setSortOrder(p.getSortOrder());
		
		return port;
	}

	public static PowerPortDTO  adaptModelPortsDomainToPowerPortDTO (ModelPowerPorts p, Long classLkpValueCode) {
		
		PowerPortDTO port = new PowerPortDTO();
		
		// set name
		if (p.getPortName() != null) {
			port.setPortName(p.getPortName());
		}

		port.setSortOrder(p.getSortOrder());
		
		// set connector
		PortConnectorDTO pcDto = new PortConnectorDTO();
		if (p.getConnectorLookup() != null && pcDto != null) {
			pcDto.setConnectorId(p.getConnectorLookup().getConnectorId());
			pcDto.setConnectorName(p.getConnectorLookup().getConnectorName());
			port.setConnector(pcDto);
			port.setConnectorLkuId(p.getConnectorLookup().getConnectorId());
		}
		
		// set phase
		if (p.getPhaseLookup() != null) {
			port.setPhaseLksValueCode(p.getPhaseLookup().getLkpValueCode());
			port.setPhaseLksDesc(p.getPhaseLookup().getLkpValue());
		}

		// set volts	
		if (p.getVoltsLookup() != null) {
			port.setVoltsLksValueCode(p.getVoltsLookup().getLkpValueCode());
			port.setVoltsLksDesc(p.getVoltsLookup().getLkpValue());
		}
		
		// set power factor
		port.setPowerFactor(p.getPowerFactor());
		
		// set watts 
		port.setWattsNameplate(p.getWattsNameplate());
		port.setWattsBudget((0 == p.getWattsBudget()) ? p.getWattsNameplate() * 60 / 100 : p.getWattsBudget());
		
		// set amps
		double nameplateAmps = p.getAmpsNameplate();
		port.setAmpsNameplate(nameplateAmps);
		port.setAmpsBudget(p.getAmpsBudget());
		port.setAmpsRated(nameplateAmps);
		
		// set Breaker / Fuse
		if (p.getFuseLookup() != null) {
			port.setFuseLkuId(p.getFuseLookup().getLkuId());
			port.setFuseLkuDesc(p.getFuseLookup().getLkuValue());
			port.setBreakerName(p.getFuseLookup().getLkuValue());
			// Breaker Amps is stored in amps budget
			port.setBreakerAmpsRated(p.getAmpsBudget());
		}
		
		// input chord power port model id
		if (p.getInputCordPort() != null && p.getInputCordPort().getModelPowerPortId() != null) {
			port.setInputCordModelPowerPortId(p.getInputCordPort().getModelPowerPortId());
		}
		
		// subclass to differentiate between inputcord and outlets
		if (p.getPortSubclassLookup() != null) {
			port.setPortSubClassLksValueCode(p.getPortSubclassLookup().getLkpValueCode());
		}
		
		// may require for mapping inlet/outlet
		if (p.getModelPowerPortId() != null) {
			port.setModelPowerPortId(p.getModelPowerPortId());
		}
		
		if (p.getColorLookup() != null) {
			port.setColorLkuId(p.getColorLookup().getLkuId());
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
			port.setColorNumber(p.getColorLookup().getLkuAttribute());
		}
		
		port.setPlacementX(p.getPlacementX());
		port.setPlacementY(p.getPlacementY());
		if (null != p.getFaceLookup()) {
			port.setFaceLksValueCode(p.getFaceLookup().getLkpValueCode());
		}

		if (null != p.getPhaseLegsLookup()) {
			port.setPhaseLegsLksValueCode(p.getPhaseLegsLookup().getLkpValueCode());
			port.setPhaseLegsLksDesc(p.getPhaseLegsLookup().getLkpValue());
		}
		
		port.setRedundant(p.isRedundant());

		double defaultActualAmpValue = -1;
		port.setAmpsActual(defaultActualAmpValue);
		port.setAmpsActualA(defaultActualAmpValue);
		port.setAmpsActualB(defaultActualAmpValue);
		port.setAmpsActualC(defaultActualAmpValue);
		
		port.setSortOrder(p.getSortOrder());
		
		return port;
	}

}
