package com.raritan.tdz.dctimport.integration.transformers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dao.DataConnDAO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.DataPortNodeDTO;
import com.raritan.tdz.circuit.dto.WireNodeInterface;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.CircuitSearch;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.circuit.service.DataProc;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.DataConnImport;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortDTOBase;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.UserLookup;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.user.home.UserHome;
import com.raritan.tdz.util.GlobalUtils;

/**
 * Converter to get the CircuitDTO object for data circuit
 * @author Santo Rosario
 *
 */
public class DataConnBeanTransformer implements ImportBeanToParameter {
	private String uuid;

	@Autowired
	protected DataCircuitDAO dataCircuitDAO;

	@Autowired
	protected DataPortDAO dataPortDAO;

	@Autowired
	private UserLookupFinderDAO userLookupFinderDAO;

	@Autowired
	private CircuitPDHome circuitHome;

	@Autowired
	protected DataConnDAO dataConnDAO;

	@Autowired
	protected UserHome userHome;

	@Autowired
	protected DataCircuitHome dataCircuitHome;

	@Autowired 
	private CircuitSearch circuitSearch;

	public DataConnBeanTransformer(String uuid) {
		super();
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception {
		return null;
	}

	protected CircuitDTO newDataCircuitDtoFromDCImport(DataConnImport dcImport, Errors errors, UserInfo userInfo) throws DataAccessException, Exception {	
		//First port to add
		DataPort portStart = dataPortDAO.getPort(dcImport.getStartingItemLocation(), dcImport.getStartingItemName(), dcImport.getStartingPortName());

		if (null == portStart) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.rejectValue("sourcePort", "Import.DataConn.PortNotFound", errorArgs, "Cannot find source port");
			return null;
		}

		//Last port to add
		DataPort portEnd = dataPortDAO.getPort(dcImport.getEndingItemLocation(), dcImport.getEndingItemName(), dcImport.getEndingPortName());

		if (null == portEnd) {
			Object[] errorArgs = {  dcImport.getEndingItemName() + ":" + dcImport.getEndingPortName() };
			errors.rejectValue("sourcePort", "Import.DataConn.PortNotFound", errorArgs, "Cannot find source port");
			return null;
		}
		
		CircuitDTO circuit = new CircuitDTO();
		List<CircuitNodeInterface> nodeList = new ArrayList<CircuitNodeInterface>();
		
		circuit.setNodeList(nodeList);
		circuit.setStatusCode(SystemLookup.ItemStatus.PLANNED);
		circuit.setUserInfo(userInfo);
		
		//Add first node in circuits
		//Handle circuit with two nodes ending in a data panel
		if(portStart.isDataPanel()) {
			DataConnection conn = getPanelConnection(portStart, errors);
			
			if(errors.hasErrors()) { return null;	}
									
			addDataPanel(nodeList, conn, null, null, userInfo);
		}
		else {
			DataPortNodeDTO nodeDTO = DataProc.newDataPortNodeDTO(portStart);
			nodeList.add(nodeDTO);
		}

		//Add panel connections
		processDataPanels(circuit, dcImport, errors, portStart, portEnd);
		
		if(errors.hasErrors()) {
			return null;
		}
		
		//Add last node if circuit does not end in a data panel
		if(!portEnd.isDataPanel()) {
			//add last node in circuit
			DataPortNodeDTO nodeDTO = DataProc.newDataPortNodeDTO(portEnd);
			nodeList.add(nodeDTO);
		}
				
		DataProc.reverseCircuit(circuit);

		setCircuitInfo(circuit, dcImport);

		if (circuit.getProposeCircuitId() != null && circuit.getProposeCircuitId() > 0) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.reject("circuit.editInstalledWithExistingProposed", errorArgs, "proposed circuit exists");
			return null;
		}		

		return circuit;
	}

	protected DataConnection getPanelConnection(String locationCode, String panelName, String portName, Errors errors) throws DataAccessException {

		if(locationCode == null || locationCode.isEmpty()) return null;
		if(panelName == null || panelName.isEmpty()) return null;
		if(portName == null || portName.isEmpty()) return null;
		
		DataPort port = dataPortDAO.getPort(locationCode, panelName, portName);

		if (null == port) {
			Object[] errorArgs = { panelName + ":" + portName };
			errors.rejectValue("sourcePort", "Import.DataConn.PortNotFound", errorArgs, "Cannot find panel port");
			return null;
		}
		
		if(!port.isDataPanel()) {
			Object errorArgs[] = new Object[] { port.getDisplayName() };
			errors.reject("dataProc.badCircuitMiddleNode", errorArgs, "Data circuit cans only have data panels between start and end nodes.");
			return null;
		}
		
		//If is a data panel, return connection object
		DataConnection conn = getPanelConnection(port, errors);

		return conn;
	}


	protected ConnectionCord getCord(DataConnImport dcImport, int i, Errors errors) {
		List<String> cordTypeList = dcImport.getCordTypeList();
		List<String> cordLabelList = dcImport.getCordIdList();
		List<String> cordColorList = dcImport.getCordColorList();
		List<String> cordLengthList = dcImport.getCordLengthList();
		String color = "%";

		ConnectionCord cord = new ConnectionCord();

		if(cordColorList.size() > 0 && i < cordColorList.size()) {
			color = cordColorList.get(i).isEmpty() ? color : cordColorList.get(i);
		}

		if(cordTypeList.size() > 0 && i < cordTypeList.size() && cordTypeList.get(i).isEmpty() == false) {
			List<LkuData> lkuType = userLookupFinderDAO.findByLkpValueLkpAttributeAndTypeCaseInsensitive(cordTypeList.get(i), color, UserLookup.LkuType.CORD);
	
			if(lkuType == null || lkuType.size() == 0) {
				Object[] errorArgs = { cordTypeList.get(i)  + (color.equals("%") ? "" : " with color " + color) };
				errors.reject("Import.Circuit.CircuitCordTypeNotExist", errorArgs, "Cannot find cord type");
				return null;
			}
	
			cord.setCordLookup(lkuType.get(0));
		}
		
		if((cordLabelList.size() > 0 && i < cordLabelList.size())) {
			cord.setCordLabel(cordLabelList.get(i));
		}

		
		if((cordLengthList.size() > 0 && i < cordLengthList.size()) && cordLengthList.get(i).isEmpty() == false) {
			String cordLen = cordLengthList.get(i);
			
			if(!GlobalUtils.isNumeric(cordLen)){
				Object[] errorArgs = { cordLen };
				errors.reject("Import.Circuit.CircuitInvalidCordLength", errorArgs, "invalid cord length value");
				return null;				
			}
			long len = Long.parseLong(cordLen);
			
			if(len < 0 || len > 100000) {
				Object[] errorArgs = { cordLen };
				errors.reject("Import.Circuit.CircuitInvalidCordLength", errorArgs, "invalid cord length value");
				return null;								
			}
			cord.setCordLength(Integer.parseInt(cordLen));
		}

		return cord;
	}

	protected void setCircuitInfo(CircuitDTO circuit, DataConnImport dcImport) throws DataAccessException {
		if(dcImport.getCircuitId() != null && dcImport.getCircuitId() > 0) {
			circuit.setCircuitId(dcImport.getCircuitId());
			circuit.setStatusCode(dcImport.getStatusLksValueCode());	
			
			Long id = dcImport.getCircuitId() == null ? null : dcImport.getCircuitId().longValue();
			circuit.setProposeCircuitId(dataCircuitDAO.getProposedCircuitId(id));
		}
		
		//Add partial circuit if exists
		completeCircuit(circuit);
		circuit.setOrigin(SystemLookup.ItemOrigen.IMPORT);
	}
	
	protected WireNodeInterface createWireNode(DataPort sourcePort, DataPort destPort, ConnectionCord cord, UserInfo userInfo) {
		//Create wire node
		DataConnection conn = new DataConnection();
		conn.setSourceDataPort(sourcePort);
		conn.setConnectionCord(cord);
		conn.setDestDataPort(destPort);
		WireNodeInterface wireDTO = DataProc.newWireNode(conn, userInfo);

		return wireDTO;

	}

	protected void completeCircuit(CircuitDTO circuit) throws DataAccessException {
		String trace = dataCircuitDAO.getCircuitTrace(circuit.getEndPortId());
		
		if(trace == null || trace.trim().isEmpty()) return;
		
		String[] connIds = trace.split(",");
		UserInfo userInfo = circuit.getUserInfo();

		for(String connId:connIds) {
			if(connId.isEmpty()) continue;
			
			DataConnection conn = dataConnDAO.getConn(Long.parseLong(connId));
			
			if(conn.getDestDataPort() == null) break;
			
			WireNodeInterface wireDTO = DataProc.newWireNode(conn, userInfo);
			DataPortNodeDTO nodeDTO = DataProc.newDataPortNodeDTO(conn.getDestDataPort());
				
			circuit.getNodeList().add(wireDTO);
			circuit.getNodeList().add(nodeDTO);
		}			
	}

	protected void addDataPanel(List<CircuitNodeInterface> nodeList, DataConnection conn, DataConnection priorConn,  ConnectionCord cord, UserInfo userInfo) throws DataAccessException, Exception {
		DataPortNodeDTO nodeDTO;
		WireNodeInterface wireDTO;
		
		//Create wire node
		if(priorConn != null) {
			wireDTO = createWireNode(priorConn.getDestDataPort(), conn.getSourceDataPort(), cord, userInfo);
			nodeList.add(wireDTO);
		}

		//add panel near end node
		nodeDTO = DataProc.newDataPortNodeDTO(conn.getSourceDataPort());
		nodeList.add(nodeDTO);

		//Internal wire of panel to panel
		wireDTO = DataProc.newWireNode(conn, userInfo);
		nodeList.add(wireDTO);

		//add panel far end node
		nodeDTO = DataProc.newDataPortNodeDTO(conn.getDestDataPort());
		nodeList.add(nodeDTO);
	}

	protected DataConnection getPanelConnection(DataPort port, Errors errors) throws DataAccessException {
		//If is a data panel, return connection object
		DataConnection conn = dataCircuitHome.getPanelToPanelConn(port.getPortId());

		if (null == conn) {
			Object[] errorArgs = { port.getDisplayName() };
			errors.rejectValue("sourcePort", "Import.DataConn.PortNotFound", errorArgs, "Cannot find panel connection");
			return null;
		}

		if(port.getPortId().equals(conn.getSourcePortId()) == false) {
			//reverse connection
			DataConnection newConn = new DataConnection();
			newConn.setSourceDataPort(conn.getDestDataPort());
			newConn.setDestDataPort(conn.getSourceDataPort());
			newConn.setConnectionCord(conn.getConnectionCord());
			newConn.setConnectionType(conn.getConnectionType());

			return newConn;
		}

		return conn;
	}

	protected int processDataPanels(CircuitDTO circuit, DataConnImport dcImport, Errors errors, DataPort portStart, DataPort portEnd) throws DataAccessException, Exception {
		List<String> panelLocList = dcImport.getPanelLocationList();
		List<String> panelNameList = dcImport.getPanelNameList();
		List<String> panelPortNameList = dcImport.getPanelPortNameList();
		List<CircuitNodeInterface> nodeList = circuit.getNodeList();
		DataConnection priorConn = new DataConnection();
		int cordIndex = 0;
		UserInfo userInfo = circuit.getUserInfo();
		
		priorConn.setSourceDataPort(portStart);
		priorConn.setDestDataPort(portStart);

		//Add panel connections
		for(int i=0; i<panelLocList.size(); i++) {
			DataConnection conn = getPanelConnection(panelLocList.get(i), panelNameList.get(i), panelPortNameList.get(i), errors);

			if(errors.hasErrors()) { return cordIndex;	}

			ConnectionCord cord = getCord(dcImport, cordIndex, errors);

			addDataPanel(nodeList, conn, priorConn, cord, userInfo);

			priorConn = conn;
			cordIndex++;
		}
		
		//Handle circuit with two nodes ending in a data panel
		if(portEnd.isDataPanel()) {
			DataConnection conn = getPanelConnection(portEnd, errors);
			
			if(errors.hasErrors()) { return cordIndex;	}
			
			ConnectionCord cord = getCord(dcImport, cordIndex, errors);
						
			addDataPanel(nodeList, conn, priorConn, cord, userInfo);
		}
		else { //circuit does not end in a data panel
			//Create wire node
			ConnectionCord cord = getCord(dcImport, cordIndex, errors);
			WireNodeInterface wireDTO = createWireNode(priorConn.getDestDataPort(), portEnd, cord, userInfo);
			nodeList.add(wireDTO);
		}
		
		return cordIndex;
	}

	protected void setNodeUsedFlag(CircuitDTO circuit, String oldTrace) throws DataAccessException {
		String connId;
		PortDTOBase node;
		boolean isUsed = true;
		
		if(circuit == null) return;
		
		if(oldTrace == null) isUsed = false; //new circuit
		
		for(Object obj:circuit.getNodeList()) {
			if(obj instanceof PortDTOBase) {
				node = (PortDTOBase)obj;
				node.setUsed(isUsed);
				
				DataConnection conn = dataConnDAO.getPortConnection(node.getPortId(), true);

				if(conn != null && node.getItemClassLksValueCode() == SystemLookup.Class.DATA_PANEL) {
					//data panel for must be the source and the destination of two different connections in order for the is_used flag to be true
					conn = dataConnDAO.getPortConnection(node.getPortId(), false);
				}
				
				if(conn == null) {
					node.setUsed(false);
					continue;
				}
				else {
					node.setUsed(true);
				}
				
				if(oldTrace != null) {
					connId = "," + String.valueOf(conn.getDataConnectionId()) + ",";
					
					if(oldTrace.indexOf(connId) == -1) {
						//connection is not part of the existing circuit
						node.setUsed(false);
					}
				}
			}
		}
	}
	
}
