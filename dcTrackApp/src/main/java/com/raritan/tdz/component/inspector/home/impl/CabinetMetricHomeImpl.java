package com.raritan.tdz.component.inspector.home.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.util.Power3Phase;
import com.raritan.tdz.circuit.util.PowerCalc;
import com.raritan.tdz.component.inspector.dao.CabinetMetricDao;
import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.component.inspector.dto.CabinetMetricPowerFeedAmpereDto;
import com.raritan.tdz.component.inspector.dto.CabinetMetricPowerFeedDto;
import com.raritan.tdz.component.inspector.home.CabinetMetricHome;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.HstPortsData;
import com.raritan.tdz.domain.HstPortsDataDetails;
import com.raritan.tdz.domain.HstPortsPower;
import com.raritan.tdz.domain.HstPortsPowerDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.UnitConverterImpl;
import com.raritan.tdz.util.UnitConverterLookup;

/**
 * Implementation of CabinetMetricHome.
 */
public class CabinetMetricHomeImpl implements CabinetMetricHome {
    private static Logger log = Logger.getLogger(CabinetMetricHomeImpl.class);

    @Autowired
    private CabinetMetricDao cabinetMetricDao;

    @Autowired
    PowerCalc powerCalc;

    @Autowired
    PowerCircuitDAO powerCircuitDAO;

    @Autowired
    ItemDAO itemDAO;

    @Autowired
    private UnitConverterImpl poundToKgUnitConverter;

    @Override
    @Transactional
    public CabinetMetricDto getCabinetMetrics(long cabinetId, String unit_str) throws Throwable {
        CabinetMetricDto cab = new CabinetMetricDto();

        Long unit = UnitConverterLookup.US_UNIT;
        if (unit_str != null) {
        	unit = Long.parseLong(unit_str);
        } else {
        	unit_str = "1";
        }

        cab.setCabinetId(cabinetId);
        cab.setUnit(unit);

        Item cabinet = itemDAO.loadItem(cabinetId);
        if (cabinet == null) {
            log.error("Can't find item. id = " + cabinetId);
            return cab;
        }
        cab.setCabinetName(cabinet.getItemName());

        cab.setBudgetedPower(cabinetMetricDao.getCabinetBudgetedPower(cabinetId)); // unit = W
        cab.setHeatOutput(cab.getBudgetedPower() / getHeatFactor(unit)); // budgetedPower = n W, heatOutput = n / getHeatFactor(unit) (kW or BTU/hr)

        Double totalWeightInLB = cabinetMetricDao.getCabinetTotalWeight(cabinetId);
        Double totalWeightAfterUnitConversion = (Double)poundToKgUnitConverter.convert(totalWeightInLB, unit_str);
        cab.setTotalWeight(totalWeightAfterUnitConversion);

        cab.setItemsInCabinet(cabinetMetricDao.getCabinetItemCount(cabinetId));

        CabinetMetricDto tempDto = cabinetMetricDao.getCabinetRUInfo(cabinetId);
        cab.setTotalRUs(tempDto.getTotalRUs());
        cab.setAvailableRUs(tempDto.getAvailableRUs());
        cab.setLargestContiguousRUs(tempDto.getLargestContiguousRUs());

        setAvailDataPortMetrics(cab, cabinetId);
        setAvailPowerPortMetrics(cab, cabinetId);

        processRPDUMetric(cabinet, cab);
        processOutletMetric(cabinet, cab);

        List<String> connects = new ArrayList<String>();
        List<CabinetMetricPowerFeedDto> powerFeeds = new ArrayList<CabinetMetricPowerFeedDto>();
        for (CabinetMetricPowerFeedDto powerFeed : cab.getPowerFeeds()) {

            if (CabinetMetricPowerFeedDto.TYPE_RACK_PDU.equals(powerFeed.getType())) {
                // The connections connected to Rack PDUs.
                connects.add(powerFeed.getConnectTo());
            }

            if (CabinetMetricPowerFeedDto.TYPE_DIRECT_CONNECTION.equals(powerFeed.getType())) {
                if (connects.contains(powerFeed.getName())) {
                    // Do not show the sub-panel of direct connections connected to Rack PDUs.
                    continue;
                }
            }

            powerFeeds.add(powerFeed);
        }

        cab.setPowerFeeds(powerFeeds);

        return cab;
    }

	@Override
	public double getCabinetBudgetedPower(Long cabinetId) {
		return cabinetMetricDao.getCabinetBudgetedPower(cabinetId);
	}

	@Override
	public double getCabinetHeatOutput(Long cabinetId) {
		return cabinetMetricDao.getCabinetHeatOutput(cabinetId);
	}

	@Override
	public double getCabinetTotalWeight(Long cabinetId) {
		return cabinetMetricDao.getCabinetTotalWeight(cabinetId);
	}

	@Override
	public long getCabinetItemCount(Long cabinetId) {
		return cabinetMetricDao.getCabinetItemCount(cabinetId);
	}

	@Override
	public int getCabinetTotalAvailableRUs(Long cabinetId) {
		return cabinetMetricDao.getCabinetTotalAvailableRUs(cabinetId);
	}

	@Override
	public int getCabinetTotalRUs(Long cabinetId) {
		return cabinetMetricDao.getCabinetTotalRUs(cabinetId);
	}

	@Override
	public int getCabinetTotalUsedRUs(Long cabinetId) {
		return cabinetMetricDao.getCabinetTotalUsedRUs(cabinetId);
	}

	@Override
	public int getCabinetLargestContigRU(Long cabinetId) {
		return cabinetMetricDao.getCabinetLargestContigRU(cabinetId);
	}	
	
    @Override
    public List<PowerPortMetric> processRPDUMetric(Item cabinet, CabinetMetricDto cabDto) throws Throwable{
    	List<PowerPortMetric> recList = new  ArrayList<PowerPortMetric>();
    	
    	//Process Rack PDU Metrics
    	List<Item> rackList = itemDAO.getChildrenItemsOfClass(cabinet, SystemLookup.Class.RACK_PDU);
    	
    	for(Item item:rackList){
    		if(item.getPowerPorts() == null) continue;    		
    		
    		HashMap<Long, PortInterface> connectedMap = powerCircuitDAO.getDestinationItemsForItem(item.getItemId());
    		
    		for(PowerPort port:item.getPowerPorts()){
    			if(!port.isInputCord()) continue;
    			
    			CabinetMetricPowerFeedDto portDto = new CabinetMetricPowerFeedDto();
    			
        		setPowerPortMetric(portDto, item, port);
    			setConnectedToMetric(port.getPortId(), portDto, connectedMap);
    			
    			cabDto.addPowerFeed(portDto);
    		}
    	}
    	
    	return recList;
    } 

    @Override
    public List<PowerPortMetric> processOutletMetric(Item cabinet, CabinetMetricDto cabDto) throws Throwable{
    	List<PowerPortMetric> recList = new  ArrayList<PowerPortMetric>();
    	
    	//Process Power Outlet Metrics
    	List<Item> outletList = itemDAO.getChildrenItemsOfClass(cabinet, SystemLookup.Class.FLOOR_OUTLET);
    	
    	for(Item item:outletList){
    		if(item.getPowerPorts() == null) continue;
    		
    		HashMap<Long, PortInterface> connectedMap = powerCircuitDAO.getDestinationItemsForItem(item.getItemId());
    		
    		//Process Input Cord
    		for(PowerPort port:item.getPowerPorts()){
    			if(!port.isOutlet()) continue;

    			CabinetMetricPowerFeedDto portDto = new CabinetMetricPowerFeedDto();
    			
        		setPowerPortMetric(portDto, item, port);
    			setConnectedToMetric(port.getPortId(), portDto, connectedMap);
    			
    			cabDto.addPowerFeed(portDto);
    		}
    	}
    	
    	return recList;
    }  

    private void setPowerPortMetric(CabinetMetricPowerFeedDto feed, Item item, PowerPort port){		 
		 feed.setName(item.getItemName());
		 
		 if(item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.RACK_PDU)){
			 feed.setType(CabinetMetricPowerFeedDto.TYPE_RACK_PDU);
		 }
		 else{
			 feed.setType(CabinetMetricPowerFeedDto.TYPE_DIRECT_CONNECTION);

            // Show the port name in the title of direct connection sub-panels.
            feed.setName(feed.getName() + ":" + port.getPortName());
		 }
		 
		 feed.setInlet(port.getPortName());
         
		 Power3Phase calc = powerCalc.get3PhaseNodeSum(port);
		 Power3Phase meas = powerCalc.get3PhaseNodeSumMeasured(port);
		
		 //currentRatedB => Sum of Budget Amps for Power Supplies connected to Rack PDU
		 CabinetMetricPowerFeedDto tempFeed = new CabinetMetricPowerFeedDto();
		 
		 //L1
		 tempFeed.addAmpere(GlobalUtils.formatNumberToInt(port.getAmpsNameplate()), GlobalUtils.formatNumberToInt(calc.currentRatedA), GlobalUtils.formatNumberToInt(meas.currentMaxA), 1);
								
		//L2
		 tempFeed.addAmpere(GlobalUtils.formatNumberToInt(port.getAmpsNameplate()), GlobalUtils.formatNumberToInt(calc.currentRatedB), GlobalUtils.formatNumberToInt(meas.currentMaxB), 2);		 
		
		//L3
		 tempFeed.addAmpere(GlobalUtils.formatNumberToInt(port.getAmpsNameplate()), GlobalUtils.formatNumberToInt(calc.currentRatedC), GlobalUtils.formatNumberToInt(meas.currentMaxC), 3);
		 
		 if(port.isSinglePhaseVoltage() == false) {
			 feed.setAmperes(tempFeed.getAmperes());
			 return;
		 }
		 
		 for(CabinetMetricPowerFeedAmpereDto x:tempFeed.getAmperes()) {
			 if(x.getBudgeted() > 0) {
				 feed.addAmpere(x);
				 break;
			 }
		 }
		 
		 if(feed.getAmperes().size() == 0) {
			feed.addAmpere(tempFeed.getAmperes().get(0));
		 }
		 
    } 
      
    private void setConnectedToMetric(Long portId, CabinetMetricPowerFeedDto portDto, HashMap<Long, PortInterface> connected){
		if(connected != null && connected.containsKey(portId)){
			PowerPortDTO x = (PowerPortDTO)connected.get(portId);
			portDto.setConnectTo(x.getConnectedItemName() + ":" + x.getConnectedPortName());
			
			CircuitUID cid = new CircuitUID(x.getConnectedCircuitId());
			
			Long circuitId = cid.getCircuitDatabaseId();
			
			PowerCircuit circuit = powerCircuitDAO.getPowerCircuit(circuitId);
			
			//Power Source is the last node in the circuit
			portDto.setSource(circuit.getEndConnection().getSourceItem().getItemName());

            for (PowerConnection conn : circuit.getCircuitConnections()) {
                PowerPort port = conn.getSourcePowerPort();
                if (port != null && port.isBranchCircuitBreaker()) {
                    portDto.addBreakerPole(Integer.parseInt(port.getConnectorLookup().getAttribute()));
                }
            }
		}		
    }

	private double getAreaFactor(long unit){
		double factor;
		double cUnitSqftToSqM = 0.09290304;
		
		if (unit == UnitConverterLookup.US_UNIT) {
			factor = 1.0;
		}
		else{
			factor = cUnitSqftToSqM;
		}
		
		return factor;
	}

	private double getHeatFactor(long unit){
		double factor;
		double cUnitBTUToKW = 0.0002930711;
		
		if (unit == UnitConverterLookup.US_UNIT) {
            // W -> BTU/hr
            // n W = n/(1000*0.0002930711) BTU/hr
			factor = cUnitBTUToKW * 1000.0;
		}
		else{
            // W -> kW
            // n W = n/1000 kW
		   factor = 1000.0;
		}
		
		return factor;			
	}

	private void setAvailDataPortMetrics(CabinetMetricDto cab, Long cabinetId) {
		List<HstPortsData> dataList = cabinetMetricDao.getHstPortData(cabinetId);
		int coax = 0, coaxT = 0;
		int copper = 0, copperT = 0;
		int fiber = 0, fiberT = 0;
		
		for(HstPortsData port:dataList){
			for(HstPortsDataDetails d:port.getHstPortsDataDetails()){
				LksData media = d.getMediaLookup();
				
				if(media.getLkpValueCode().equals(SystemLookup.MediaType.COAX)){
					coax += d.getFreePort();
					coaxT += d.getTotalPort();
					
				}
				else if(media.getLkpValueCode().equals(SystemLookup.MediaType.TWISTED_PAIR)){
					copper += d.getFreePort();
					copperT += d.getTotalPort();
				}
				else{
					fiber += d.getFreePort();
					fiberT += d.getTotalPort();
				}
			}
		}
		
		cab.setAvailablePortsCoax(coax);
		cab.setTotalPortsCoax(coaxT);
		cab.setAvailablePortsCopper(copper);
		cab.setTotalPortsCopper(copperT);
		cab.setAvailablePortsFiber(fiber);
		cab.setTotalPortsFiber(fiberT);
    }

	private void setAvailPowerPortMetrics(CabinetMetricDto cab, Long cabinetId) {
		List<HstPortsPower> recList = cabinetMetricDao.getHstPortPower(cabinetId);
		int free = 0;
		int total = 0;
		
		for(HstPortsPower port:recList){
			for(HstPortsPowerDetails d:port.getHstPortsPowerDetails()){
				free += d.getFreePort();
				total += d.getTotalPort();
			}
		}
		cab.setAvailablePortsPower(free);
		cab.setTotalPortsPower(total);
    }	
}
