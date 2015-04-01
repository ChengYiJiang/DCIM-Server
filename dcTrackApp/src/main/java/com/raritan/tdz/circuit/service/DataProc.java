package com.raritan.tdz.circuit.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.DataPortNodeDTO;
import com.raritan.tdz.circuit.dto.PatchCordDTO;
import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.circuit.dto.VirtualWireDTO;
import com.raritan.tdz.circuit.dto.WireNodeInterface;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.UnitConverterHelperImpl;
import com.raritan.tdz.util.UnitConverterImpl;
import com.raritan.tdz.views.DataPortObject;

public class DataProc {
	private static Logger appLogger = Logger.getLogger(CircuitPDServiceImpl.class);

	public static WireNodeInterface newWireNode(DataConnection conn, UserInfo userInfo){
		
		WireNodeInterface cord;
		
		DataPort source = conn.getSourceDataPort();
		DataPort dest = conn.getDestDataPort();
		
		if(conn.getConnectionType() != null && conn.getConnectionType().getLkpValueCode() == SystemLookup.LinkType.IMPLICIT){
			StructureCableDTO cable = new StructureCableDTO();
			
			if(source.getCableGradeLookup() != null){
				cable.setCableGradeLkuId(source.getCableGradeLookup().getLkuId());
				cable.setCableGradeLkuDesc(source.getCableGradeLookup().getLkuValue());
			}
			
			if(source.getMediaId() != null){
				cable.setMediaLksId(source.getMediaId().getLksId());
				cable.setMediaLksDesc(source.getMediaId().getLkpValue());
			}
			
			cord = cable;
		}
		else {
			if(source.isVirtual()  || source.isLogical()){
				cord = new VirtualWireDTO();
			}
			else{
				cord = new PatchCordDTO( conn.getConnectionCord(), userInfo );
			}			
		}
		
		cord.setNePortName(source.getPortName());
		
		boolean isPatchCord = (conn.getConnectionType() != null && 
				conn.getConnectionType().getLkpValueCode() == SystemLookup.LinkType.EXPLICIT) ? true : false; 
		
		cord.setPatchCord (isPatchCord);
		ConnectionCord connCord = conn.getConnectionCord();
		
		
		if (connCord != null) {
			Integer length = connCord.getCordLength();
			if (null != userInfo) {
				UnitConverterImpl lenghtConverter = new UnitConverterImpl(UnitConverterImpl.FEET_TO_METER);
				lenghtConverter.setUnitConverterHelper( new UnitConverterHelperImpl());
				Object len = lenghtConverter.convert(new Integer(connCord.getCordLength()), ((userInfo != null) ? userInfo.getUnits() : "1"));
				if (len instanceof Double) {
					length = ((Double) len).intValue();
				}
				else if (len instanceof Integer) {
					length = (Integer) len;
				}
				else if (len instanceof Float) {
					length = ((Float) len).intValue();
				}
			}

			cord.setCordLength( length );
			LkuData colorLkp = connCord.getColorLookup();
			if (colorLkp != null) {
				cord.setCordColor( colorLkp.getLkuId() );
			}
		}
		
		if (dest != null){
			cord.setFePortName(dest.getPortName());
		}
		
		return cord;
	}

	public static DataCircuit newDataCircuitFromNodes(CircuitDTO circuit,  DataCircuitHome circuitHome) throws DataAccessException, BusinessValidationException {
			DataCircuit cx = new DataCircuit();			
			List<DataConnection> connList = new ArrayList<DataConnection>();
			DataConnection conn = null;
			DataConnection priorConn = null;
			DataPort port = null;
			boolean doPanelCheck = true;
			
			if(appLogger.isDebugEnabled()){
				appLogger.debug("\n\n++++++++++++++newDataCircuitFromNodes BEGIN+++++++++++++++++\n");
			}
			
			cx.setUserInfo(circuit.getUserInfo());
			
			//Flip Circuit if needed
			reverseCircuit(circuit);
			
			//Handle circuit starting with a data panel that is in an existing circuit
			DataPortNodeDTO portNode = (DataPortNodeDTO)circuit.getNodeList().get(0);

			if(portNode.getItemClassLksValueCode() == SystemLookup.Class.DATA_PANEL){
				doPanelCheck = updateNodeList(circuit, circuitHome);
				cx.setSharedCircuitTrace(circuit.getSharedCircuitTrace());
			}
			
			List<CircuitNodeInterface> nodeList = circuit.getNodeList();			
			int nodeCount = nodeList.size();			
			LksData connType = null;
			
			for(int i=0; i<nodeCount; i++){
				CircuitNodeInterface node = nodeList.get(i);
				
				if(node instanceof DataPortNodeDTO){
					portNode = (DataPortNodeDTO)node;
					
					if(appLogger.isDebugEnabled()){
						appLogger.debug(portNode.toString());
					}
					
					conn = new DataConnection();					
					//only need port id and is_used fields
					port = new DataPort();
					port.setPortId(portNode.getPortId());	
					port.setUsed(portNode.isUsed());
					conn.setSourceDataPort(port);
					conn.setSortOrder(i+1);

					//default connection type is Explicit
					connType = new LksData();
					connType.setLkpValueCode(SystemLookup.LinkType.EXPLICIT);
					connType.setLksId(502L);

					if((i + 1) < nodeCount){
						WireNodeInterface wnode = (WireNodeInterface)circuit.getNodeList().get(i + 1);
						if(appLogger.isDebugEnabled()){
							appLogger.debug(wnode.toString());
						}
						
						conn.setConnectionCord(CircuitProc.newConnCord(wnode, circuit.getUserInfo()));	
						
						if(wnode instanceof StructureCableDTO){
							connType.setLkpValueCode(SystemLookup.LinkType.IMPLICIT);
							connType.setLksId(501L);
						}
					}		
					
					conn.setConnectionType(connType);						
					
					if(priorConn != null){
						priorConn.setDestDataPort(conn.getSourceDataPort());
					}
										
					connList.add(conn);	
					priorConn = conn;
					
					if(doPanelCheck && portNode.getItemClassLksValueCode() == SystemLookup.Class.DATA_PANEL){						
						if(i == 0){ //creating a circuit starting with a Data Panel
							//From GUI screen, the data panel does not have the second node to the NE or FE panel
							//for a new circuit
							DataConnection panelConn = circuitHome.getPanelToPanelConn(portNode.getPortId());
							
							if(panelConn != null){
								DataPort sPort = panelConn.getSourceDataPort(); 
								DataPort dPort = panelConn.getDestDataPort();
								
								if((i + 2) < nodeCount){
									DataPortNodeDTO nextPortNode = (DataPortNodeDTO)circuit.getNodeList().get(i + 2);
									
									if(sPort.getPortId().equals(portNode.getPortId()) && dPort.getPortId().equals(nextPortNode.getPortId()))
									{   //don't add
										continue;
									}
								}
								
								if(sPort.getPortId().equals(portNode.getPortId())){ //reverse order
									panelConn.setSourceDataPort(dPort);
									panelConn.setDestDataPort(sPort);
								}
								
								connList.add(0, panelConn);
							}
						}						
					}
				}				
			}
			
			if(connList.size() > 0){
				cx.setStartConnection(connList.get(0));
				cx.setEndConnection(connList.get(connList.size()-1));
			}
			
			cx.setCircuitConnections(connList);
			cx.setDataCircuitId( circuit.getCircuitUID().getCircuitDatabaseId() );
			
			if(appLogger.isDebugEnabled()){
				appLogger.debug("\n++++++++++++++newDataCircuitFromNodes END+++++++++++++++++\n");
			}
			
			return cx;
		}
	

		public static boolean updateNodeList(CircuitDTO circuit,  DataCircuitHome circuitHome) throws DataAccessException, BusinessValidationException {
			List<CircuitNodeInterface> nodeList = circuit.getNodeList();
			List<CircuitNodeInterface> newNodeList = null;
			int nodeCount = nodeList.size();			
			boolean doPanelCheck = true;
			
			if(circuit.isOriginImport()) return false;
			
			//Handle circuit starting with a data panel that is in an existing circuit
			DataPortNodeDTO portNode = (DataPortNodeDTO)nodeList.get(0);
			DataConnection panelConn = circuitHome.getPanelToPanelConn(portNode.getPortId());
						
			if(panelConn != null){
				DataPort sPort = panelConn.getSourceDataPort(); 
				DataPort dPort = panelConn.getDestDataPort();				

				if(sPort.getUsed() && dPort.getUsed()){ //circuit is connected
					return doPanelCheck;
				}
				
				if(sPort.getUsed() || dPort.getUsed()){
					//panel is part of an existing connection, cannot be changed
					//this handle when a partial circuit was built using panels, and user
					//want to connect the NE or FE of panel
					CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
					cCriteria.setConnectionId(panelConn.getDataConnectionId());					
					List<DataCircuit> circuitList = circuitHome.viewDataCircuitByCriteria(cCriteria);
					
					for(DataCircuit partialCircuit:circuitList){
						if(partialCircuit.getShareStartConnId() != 0){
							continue; //This is not a shared circuit
						}
						
						long cid1 = circuit.getCircuitUID().getCircuitDatabaseId();
						long cid2 = partialCircuit.getCircuitUID().getCircuitDatabaseId();
						
						if(cid1 == cid2){
							continue;
						}
						
						if(portNode.getPortId().equals(partialCircuit.getFirstPortId())){
							newNodeList = new ArrayList<CircuitNodeInterface>();
							
							for(int x = nodeCount-1; x > 0; x--){ //don't add first node
								newNodeList.add(nodeList.get(x));									
							}
							
							for(DataConnection oldConn:partialCircuit.getCircuitConnections()){
								DataPortNodeDTO newNode = new DataPortNodeDTO();
								newNode.setPortId(oldConn.getSourceDataPort().getPortId());
								newNode.setUsed(oldConn.getSourceDataPort().getUsed());
								newNode.setPortSubClassLksValueCode(oldConn.getSourceDataPort().getPortSubClassLookup().getLkpValueCode());
								newNode.setItemClassLksValueCode(oldConn.getSourceDataPort().getItem().getClassLookup().getLkpValueCode());
								newNodeList.add(newNode);
								
								if(newNode.getPortId() != partialCircuit.getLastPortId()){
									newNodeList.add(newWireNode(oldConn, circuit.getUserInfo()));
								}
							}
							circuit.setNodeList(newNodeList);
							circuit.setSharedCircuitTrace(partialCircuit.getCircuitTrace());														
						}
						else if(portNode.getPortId().equals(partialCircuit.getLastPortId())){
							//Add nodes to current partial circuit
							newNodeList = new ArrayList<CircuitNodeInterface>();
							
							for(DataConnection oldConn:partialCircuit.getCircuitConnections()){
								DataPortNodeDTO newNode = new DataPortNodeDTO();
								newNode.setPortId(oldConn.getSourceDataPort().getPortId());
								newNode.setUsed(oldConn.getSourceDataPort().getUsed());
								newNode.setPortSubClassLksValueCode(oldConn.getSourceDataPort().getPortSubClassLookup().getLkpValueCode());
								newNode.setItemClassLksValueCode(oldConn.getSourceDataPort().getItem().getClassLookup().getLkpValueCode());
								newNodeList.add(newNode);
								
								if(newNode.getPortId() != partialCircuit.getLastPortId()){
									newNodeList.add(newWireNode(oldConn, circuit.getUserInfo()));
								}								
							}
																
							for(int x = 1; x<nodeCount; x++){ //don't add first node
								newNodeList.add(nodeList.get(x));									
							}
							
							circuit.setCircuitId( partialCircuit.getCircuitUID().floatValue() );
							circuit.setNodeList(newNodeList);
						}
					}						
				}
			}
			
			if(newNodeList != null && newNodeList.size() > 0) doPanelCheck = false;
			
			return doPanelCheck;
		}
	
		
	public static DataPortNodeDTO newDataPortNodeDTO(DataPort p){
		if(p == null){
			return null;
		}
		
		DataPortNodeDTO port = new DataPortNodeDTO();
		
		port.setIpAddress(p.getIpAddress());
		port.setIpv6Address(p.getIpv6Address());
		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
		port.setMacAddress(p.getMacAddress());
		port.setPortId(p.getPortId());
		port.setPortName(p.getPortName());
		port.setSortOrder(p.getSortOrder());
		port.setUsed(p.getUsed());
		port.setItemClassLksValueCode(p.getItem().getClassLookup().getLkpValueCode());
		
		if(p.getMediaId() != null){
			port.setMediaLksDesc(p.getMediaId().getLkpValue());
		}
				
		if(p.getProtocolID() != null){
			port.setProtocolLkuDesc(p.getProtocolID().getLkuValue());
		}		
		
		if(p.getSpeedId() != null){
			port.setSpeedLkuDesc(p.getSpeedId().getLkuValue());
		}
				
		if(p.getVlanLookup() != null){
			port.setVlanLkuDesc(p.getVlanLookup().getLkuValue());
		}
		if(p.getColorLookup() != null){
			LkuData color = p.getColorLookup();
			port.setColorLkuDesc(color.getLkuValue());
			port.setColorNumber(color.getLkuAttribute());
		}
		
		if(p.getConnectorLookup() != null){
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		}
		
		if(p.getPortSubClassLookup() != null){
			port.setPortSubClassLksValueCode(p.getPortSubClassLookup().getLkpValueCode());
		}
				
		return port;
	}

	public static DataPortObject newDataPortObject(DataPort p){
		if(p == null){
			return null;
		}
		
		DataPortObject port = new DataPortObject();
		
		port.setGroupingVlanTag(p.getGroupingVlanTag());
		port.setIpAddress(p.getIpAddress());
		port.setIpv6Address(p.getIpv6Address());
		port.setItemId(p.getItem().getItemId());
		port.setItemName(p.getItem().getItemName());
		port.setMacAddress(p.getMacAddress());
		port.setPortId(p.getPortId());
		port.setPortName(p.getPortName());
		port.setSortOrder(p.getSortOrder());
		port.setUsed(p.getUsed());
		port.setItemClassLksValueCode(p.getItem().getClassLookup().getLkpValueCode());
		
		if(p.getMediaId() != null){
			port.setMediaLksDesc(p.getMediaId().getLkpValue());
		}
				
		if(p.getProtocolID() != null){
			port.setProtocolLkuDesc(p.getProtocolID().getLkuValue());
		}		
		
		if(p.getSpeedId() != null){
			port.setSpeedLkuDesc(p.getSpeedId().getLkuValue());
		}
				
		if(p.getVlanLookup() != null){
			port.setVlanLkuDesc(p.getVlanLookup().getLkuValue());
		}
		if(p.getColorLookup() != null){
			port.setColorLkuDesc(p.getColorLookup().getLkuValue());
			port.setColorNumber(p.getColorLookup().getLkuAttribute());
		}
		
		if(p.getConnectorLookup() != null){
			port.setConnectorName(p.getConnectorLookup().getConnectorName());
			port.setConnector(CircuitProc.newPortConnectorDTO(p.getConnectorLookup()));
		}
		
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
	
	public static void reverseCircuit(CircuitDTO circuit){
		int lastIndex = circuit.getNodeList().size() - 1;
		
		//Handle circuit starting with a data panel that is in an existing circuit
		DataPortNodeDTO startNode = (DataPortNodeDTO)circuit.getNodeList().get(0);
		DataPortNodeDTO endNode = (DataPortNodeDTO)circuit.getNodeList().get(lastIndex);
		
		if(startNode.getItemClassLksValueCode() == SystemLookup.Class.DATA_PANEL && endNode.getItemClassLksValueCode() != SystemLookup.Class.NETWORK){
			java.util.Collections.reverse(circuit.getNodeList());
			return;
		}
		
		if(startNode.getItemClassLksValueCode() == SystemLookup.Class.NETWORK && endNode.getItemClassLksValueCode() != SystemLookup.Class.NETWORK){
			java.util.Collections.reverse(circuit.getNodeList());
			return;
		}
	}
	
}
