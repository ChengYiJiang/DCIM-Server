package com.raritan.tdz.circuit.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.PatchCordDTO;
import com.raritan.tdz.circuit.dto.PowerCableDTO;
import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.dto.VirtualWireDTO;
import com.raritan.tdz.circuit.dto.WireNodeDTO;
import com.raritan.tdz.circuit.dto.WireNodeInterface;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.views.PowerPortObject;
import com.raritan.tdz.vpc.home.VPCHome;

public class PowerProc {
	private static Logger appLogger = Logger.getLogger(CircuitPDServiceImpl.class);
	
	public static WireNodeInterface newWireNode(PowerConnection conn, UserInfo userInfo){
		
		WireNodeInterface cord;
		
		PowerPort source = conn.getSourcePowerPort();
		PowerPort dest = conn.getDestPowerPort();
		
		if(source.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.RACK_PDU_OUTPUT){
			cord = new VirtualWireDTO();
			cord.setClickable(false);
			cord.setReadOnly(true);
		}
		else if(conn.isLinkTypeImplicit()){
			PowerCableDTO cable = new PowerCableDTO(source, conn.getConnectionCord());
			cord = cable;
		}
		else {
			cord = new PatchCordDTO( conn.getConnectionCord(), userInfo );
		}
		
		cord.setNePortName(source.getPortName());
		boolean inputCord = (source.getPortSubClassLookup() != null && 
				source.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.INPUT_CORD) ? true : false;
		cord.setInputCord (inputCord);
		
		if (dest != null){
			cord.setFePortName(dest.getPortName());
		}
		
		return cord;
	}
	
	public static PowerCircuit newPowerCircuitFromNodes(CircuitDTO circuit, ItemDAO itemDAO, VPCHome vpcHome, PowerPortDAO powerPortDAO){
			PowerCircuit cx = new PowerCircuit();			
			List<PowerConnection> connList = new ArrayList<PowerConnection>();
			PowerConnection conn = null;
			PowerConnection priorConn = null;
			PowerPort port = null;
			
			if(appLogger.isDebugEnabled()){
				appLogger.debug("\n\n++++++++++++++newPowerCircuitFromNodes BEGIN+++++++++++++++++\n");
			}
			
			cx.setUserInfo(circuit.getUserInfo());
			
			int nodeCount = circuit.getNodeList().size();			
			LksData connType = null;
			
			for(int i=0; i<nodeCount; i++){
				CircuitNodeInterface node = circuit.getNodeList().get(i);
				
				if(node instanceof PowerPortNodeDTO){
					PowerPortNodeDTO portNode = (PowerPortNodeDTO)node;
					
					if(appLogger.isDebugEnabled()){
						appLogger.debug(portNode.toString());
					}
										
					conn = new PowerConnection();					
					
					// if this node is a VPC Power outlet node and power outlet port do not exist, create a new power outlet port
					if (itemDAO.isVpcPowerOutlet(portNode.getItemId(), portNode.getLocationId()) && null == powerPortDAO.read(portNode.getPortId())) {
						
						port = vpcHome.createPowerOutletPort(portNode.getItemId(), priorConn.getSourcePortId(), null);
						portNode.setPortId(port.getPortId());
						// port = vpcHome.createPowerOutletPortAndConnection(priorConn.getDestPortId(), circuit.getLocationId(), circuit.getChainLabel(), null);
					}
					else {
						port = new PowerPort();
						port.setPortId(portNode.getPortId());
					}
					
					//only need port id, and isUsed
					port.setUsed(portNode.isUsed());
					conn.setSourcePowerPort(port);
					conn.setSortOrder(i+1);
					
					if((i + 1) < nodeCount){
						node = circuit.getNodeList().get(i + 1);
						
						if(node instanceof WireNodeDTO){
							WireNodeDTO wnode = (WireNodeDTO)node;
							
							if(appLogger.isDebugEnabled()){
								appLogger.debug(wnode.toString());
							}
							
							conn.setConnectionCord(CircuitProc.newConnCord(wnode, circuit.getUserInfo()));									
							connType = new LksData();
							
							if(wnode instanceof PowerCableDTO || wnode instanceof VirtualWireDTO ){
								connType.setLkpValueCode(SystemLookup.LinkType.IMPLICIT);
								connType.setLksId(501L);
							}
							else{								
								connType.setLkpValueCode(SystemLookup.LinkType.EXPLICIT);
								connType.setLksId(502L);
							}	
							conn.setConnectionType(connType);
						}
					}										
					
					if(priorConn != null){
						priorConn.setDestPowerPort(conn.getSourcePowerPort());
					}
					
					priorConn = conn;
					connList.add(conn);	
				}				
			}

			if(connList.size() > 0){
				cx.setStartConnection(connList.get(0));
				cx.setEndConnection(connList.get(connList.size()-1));
			}
			
			cx.setCircuitConnections(connList);
			cx.setPowerCircuitId(circuit.getCircuitUID().getCircuitDatabaseId());
			
			if(appLogger.isDebugEnabled()){
				appLogger.debug("\n\n++++++++++++++newPowerCircuitFromNodes END+++++++++++++++++\n");
			}
			
			return cx;		
	}
	
		
	
	public static PowerPortDTO newPowerPortDTO(PowerPort p){
		if(p == null){
			return null;
		}
		
		PowerPortDTO port = new PowerPortDTO();
		
		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
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
		
		if(p.getCableGradeLookup() != null){
			port.setCableGradeLkuDesc(p.getCableGradeLookup().getLkuValue());
		}
				
		if(p.getPhaseLegsLookup() != null){
			port.setPhaseLegsLksDesc(p.getPhaseLegsLookup().getLkpValue());
		}		
		
		if(p.getFuseLookup() != null){
			port.setFuseLkuDesc(p.getFuseLookup().getLkuValue());
		}
				
		if(p.getPhaseLookup() != null){
			port.setPhaseLksDesc(p.getPhaseLookup().getLkpValue());
		}
		
		if(p.getColorLookup() != null){
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
		}
		
		if(p.getConnectorLookup() != null){
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		}
		
		if(p.getBreakerPort() != null){
			port.setBreakerName(p.getBreakerPort().getPortName());
		}
		
		long portSubClass = p.getPortSubClassLookup().getLkpValueCode();
		
		if(portSubClass == SystemLookup.PortSubClass.RACK_PDU_OUTPUT){
			port.setAmpsMax(p.getAmpsNameplate() * 1.25);  //need to change ????? USA default
			port.setAmpsRated(p.getAmpsNameplate());
			
			if(p.getFuseLookup() != null){
				port.setBreakerAmpsMax(p.getAmpsBudget() * 1.25); //need to change ????? USA default
				port.setBreakerAmpsRated(p.getAmpsBudget());
			}
			
		}else if(portSubClass == SystemLookup.PortSubClass.INPUT_CORD){
			port.setAmpsMax(p.getAmpsNameplate() * 1.25);  //need to change ????? USA default
			port.setAmpsRated(p.getAmpsNameplate());			
		}else if(portSubClass == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER){
			port.setAmpsMax(p.getAmpsNameplate()); 
			port.setAmpsRated(p.getAmpsNameplate() * 0.80);  //need to change ?????	USA default	
		}
		
		if(p.getPortSubClassLookup() != null){
			port.setPortSubClassLksValueCode(p.getPortSubClassLookup().getLkpValueCode());
		}
		
		if(p.getVoltsLookup() != null){
			port.setVoltsLksDesc(p.getVoltsLookup().getLkpValue());
			port.setVoltsLksValueCode(p.getVoltsLookup().getLkpValueCode());
		}
		
		return port;
	}	
	
	public static PowerPortObject newPowerPortObject(PowerPort p, PowerCircuitHome circuitHome){
		if(p == null){
			return null;
		}
		
		PowerPortObject port = new PowerPortObject();
		
		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
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
		port.setItemClassLksValueCode(p.getItem().getClassLookup().getLkpValueCode());
		
		if(p.getCableGradeLookup() != null){
			port.setCableGradeLkuDesc(p.getCableGradeLookup().getLkuValue());
		}
				
		if(p.getPhaseLegsLookup() != null){
			port.setPhaseLegsLksDesc(p.getPhaseLegsLookup().getLkpValue());
		}	
		
		if(p.getFuseLookup() != null){
			port.setFuseLkuDesc(p.getFuseLookup().getLkuValue());
		}
								
		if(p.getPhaseLookup() != null){
			port.setPhaseLksDesc(p.getPhaseLookup().getLkpValue());
		}
		
		if(p.getColorLookup() != null){
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
			port.setColorNumber(p.getColorLookup().getLkuAttribute());
		}
		
		if(p.getConnectorLookup() != null){
			port.setConnectorName(p.getConnectorLookup().getConnectorName());
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		}
		
		if(p.getVoltsLookup() != null){
			port.setVoltsLksDesc(p.getVoltsLookup().getLkpValue());
			port.setVoltsLksValueCode(p.getVoltsLookup().getLkpValueCode());			
		}
		
						
		long portSubClass = p.getPortSubClassLookup().getLkpValueCode();
		double upRatedFactor = p.upRatingFactor; 
		long usedWatts = 0;
		
		if(p.getBreakerPort() != null){
			PowerPort breakerPort = p.getBreakerPort();			
			port.setBreakerName(breakerPort.getPortName());
			port.setBreakerAmpsMax(breakerPort.getAmpsNameplate());
			port.setBreakerAmpsRated(GlobalUtils.formatNumberTo0Dec(breakerPort.getAmpsNameplate() * breakerPort.deRatingFactor));
			
			long breakerUsedWatts = circuitHome.getPowerPortUsedWatts(breakerPort.getPortId(), null);
			port.setBreakerUsedWatts(breakerUsedWatts);
			port.setBreakerTotalWatts(breakerPort.getMaxWatts());
		}
		
		if(portSubClass == SystemLookup.PortSubClass.RACK_PDU_OUTPUT){			
		    port.setAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * upRatedFactor));  
			port.setAmpsRated(p.getAmpsNameplate());
			
			if(p.getFuseLookup() != null){
				usedWatts = circuitHome.getPowerPortUsedWatts(p.getInputCordPortId(), p.getFuseLookup().getLkuId());
				
				port.setBreakerName(p.getFuseLookup().getLkuValue());
				port.setBreakerAmpsMax(p.getAmpsBudget() * upRatedFactor); 
				port.setBreakerAmpsRated(p.getAmpsBudget());	
				port.setBreakerTotalWatts(p.getFuseMaxWatts());
				port.setBreakerUsedWatts(usedWatts);
			}
			
			if(p.getUsed()){ //no need to get used watts if port is not connected
				usedWatts = circuitHome.getPowerPortUsedWatts(p.getPortId(), null);
			}
			else{
				usedWatts = 0;
			}
			
		}else if(portSubClass == SystemLookup.PortSubClass.INPUT_CORD){
			port.setAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * upRatedFactor));  
			port.setAmpsRated(p.getAmpsNameplate());			

			if(p.getUsed()){ //no need to get used watts if port is not connected
				usedWatts = circuitHome.getPowerPortUsedWatts(p.getPortId(), null);
			}
			else{
				usedWatts = 0;
			}
		}else if(port.getItemClassLksValueCode() == SystemLookup.Class.FLOOR_OUTLET ){
			port.setAmpsMax(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * upRatedFactor)); 
			port.setAmpsRated(p.getAmpsNameplate());   	
			
			usedWatts = circuitHome.getPowerPortUsedWatts(p.getPortId(), null);
		}else if(portSubClass == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER){
			port.setAmpsMax(p.getAmpsNameplate()); 
			port.setAmpsRated(GlobalUtils.formatNumberTo0Dec(p.getAmpsNameplate() * p.deRatingFactor));
			
			usedWatts = circuitHome.getPowerPortUsedWatts(p.getPortId(), null);
		}
		
		port.setUsedWatts(usedWatts);
		port.setFreeWatts(p.getMaxWatts() - usedWatts);
		port.setTotalWatts(p.getMaxWatts());

		if(p.getPortSubClassLookup() != null){
			port.setPortSubClassLksValueCode(p.getPortSubClassLookup().getLkpValueCode());
		}
		

		if(p.getFaceLookup() != null){
			port.setFaceValueCode(p.getFaceLookup().getLkpValueCode());
		}
		
		port.setPlacementX(p.getPlacementX());
		port.setPlacementY(p.getPlacementY());
		
		return port;
	}	
		
}
