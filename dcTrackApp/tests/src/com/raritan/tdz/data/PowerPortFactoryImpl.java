package com.raritan.tdz.data;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.powerchain.home.UpsBankPortFactoryImpl;
import com.raritan.tdz.util.GlobalUtils;


public class PowerPortFactoryImpl implements PowerPortFactory  {
	@Autowired
	SystemLookupFinderDAO systemLookupDAO;
	
	@Autowired
	UserLookupFinderDAO userLookupDAO;
	
	@Autowired
	ConnectorLookupFinderDAO connectorLookupDAO;
		
	private float defaultDeRating;
	
	@Override
	public float getDefaultDeRating() {
		return defaultDeRating;
	}

	@Override
	public void setDefaultDeRating(float defaultDeRating) {
		this.defaultDeRating = defaultDeRating;
	}

	private String defaultVolts;
	
	@Override
	public String getDefaultVolts() {
		return defaultVolts;
	}

	@Override
	public void setDefaultVolts(String defaultVolts) {
		this.defaultVolts = defaultVolts;
	}

	public PowerPortFactoryImpl() {
		defaultVolts = "208";
		defaultDeRating = 0.8f;
	}
	
	@Override
	public PowerPort createPortsForPanel(MeItem item, int quantity){
		LksData poleLksA = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.A).get(0);
		LksData poleLksB = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.B).get(0);
		LksData poleLksC = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.C).get(0);
		List<LksData> polesLookup = Arrays.asList(poleLksA, poleLksB, poleLksC);

		PowerPort firstPort = createPortsForItem(item, "Pole", SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, quantity, null, null, 0);
		int idx = 0;
		
		for(PowerPort p:item.getPowerPorts()){
			p.setPortName(String.valueOf(p.getSortOrder()));
			p.setPhaseLegsLookup(polesLookup.get(idx));
			p.setPolePhase(polesLookup.get(idx).getLkpValue());
			
			idx++;

			if(idx == 3) idx = 0;			
		}
		
		return firstPort;
	}

	@Override
	public PowerPort createPorts3PhaseWYEForItem(Item item, Long portSubClassValueCode, int quantity){
		return createPortsForItem(item, "3PhaseWPort", portSubClassValueCode, quantity, SystemLookup.PhaseIdClass.THREE_WYE, defaultVolts, 0);
	}
	
	@Override
	public PowerPort createPorts3PhaseDELTAForItem(Item item, Long portSubClassValueCode, int quantity){
		return createPortsForItem(item, "3PhaseDPort", portSubClassValueCode, quantity, SystemLookup.PhaseIdClass.THREE_DELTA, defaultVolts, 0);
	}
	
	@Override
	public PowerPort createPortsForItem(Item item, Long portSubClassValueCode, int quantity, int watts, int amps){
		PowerPort firstPort = createPortsForItem(item, "1Phase3WPort", portSubClassValueCode, quantity, SystemLookup.PhaseIdClass.SINGLE_3WIRE, defaultVolts, 0);

		for(PowerPort p:item.getPowerPorts()){
			p.setWattsNameplate(watts);
			p.setWattsBudget(Integer.valueOf(String.valueOf(watts * defaultDeRating)));
			p.setAmpsNameplate(amps);
			p.setAmpsBudget(Integer.valueOf(String.valueOf(amps * defaultDeRating)));
		}
		
		return firstPort;		
	}
	
	@Override
	public PowerPort createPortsPSForItem(Item item, int quantity, int watts){
		PowerPort firstPort = createPortsForItem(item, "PS", SystemLookup.PortSubClass.POWER_SUPPLY, quantity, SystemLookup.PhaseIdClass.SINGLE_3WIRE, defaultVolts, 0);
		
		for(PowerPort p:item.getPowerPorts()){
			p.setWattsNameplate(watts);
			p.setWattsBudget(Integer.valueOf(String.valueOf(watts * defaultDeRating)));
		}
		
		return firstPort;		
	}

	@Override
	public PowerPort createPortsRecpForItem(Item item, PowerPort inputCord, int quantity, int amps, Long phaseValueCode, String voltsValue){
		PowerPort firstPort = createPortsForItem(item, "Output", SystemLookup.PortSubClass.RACK_PDU_OUTPUT, quantity, phaseValueCode, defaultVolts, amps);
		
		for(PowerPort p:item.getPowerPorts()){
			p.setInputCordPort(inputCord);
		}
		
		return firstPort;		
	}
	
	@Override
	public void createFuseOnPort(PowerPort port, String fuseLkuValue, int amps){
		/* dct_lku_data
		900;"BREAKER_OR_FUSE";"";"Fuse A1";;0;1
		901;"BREAKER_OR_FUSE";"";"Fuse A2";;0;2
		*/		
		if(fuseLkuValue == null) fuseLkuValue = "Fuse A1";
		
		LkuData fuse = userLookupDAO.findByLkpValueAndTypeCaseInsensitive(fuseLkuValue, "BREAKER_OR_FUSE").get(0);
		
		port.setAmpsBudget(amps);
		port.setFuseLookup(fuse);
	}
	
	@Override
	public PowerPort createPortsForItem(Item item, String startPortName, Long portSubClassValueCode, int quantity, Long phaseValueCode, String voltsValue, int amps){
		if(phaseValueCode == null){
			phaseValueCode = SystemLookup.PhaseIdClass.SINGLE_3WIRE;
		}
		
		if(voltsValue == null){
			voltsValue = defaultVolts;
		}
		
		LksData volts = systemLookupDAO.findByLkpValueAndType(voltsValue, "VOLTS").get(0);
		LksData phase = systemLookupDAO.findByLkpValueCode(phaseValueCode).get(0);
		LksData portSubClassLookup = systemLookupDAO.findByLkpValueCode(portSubClassValueCode).get(0);
		ConnectorLkuData connectorLookup = connectorLookupDAO.findByNameCaseInsensitive("IEC-320-C13").get(0);

		PowerPort port = null;
		PowerPort firstPort = null;

		int portCount = item.getPowerPorts() == null ? 1 : item.getPowerPorts().size();
		
		for(int i = 0; i<quantity; i++){
			port = new PowerPort();
			port.setPortName(startPortName + i + portCount);
			port.setPortSubClassLookup(portSubClassLookup);
			port.setConnectorLookup(connectorLookup);
			port.setVoltsLookup(volts);
			port.setPhaseLookup(phase);
			port.setPowerFactor(1);
			port.setItem(item);
			port.setCreationDate(GlobalUtils.getCurrentDate());
			port.setUpdateDate(GlobalUtils.getCurrentDate());
			port.setSortOrder(i + portCount);
			port.setAmpsNameplate(amps);
			port.setAmpsBudget(amps * defaultDeRating);
			
			item.addPowerPort(port);

			if(firstPort == null){
				firstPort = port;
			}
		}

		return firstPort;
	}
	
}
