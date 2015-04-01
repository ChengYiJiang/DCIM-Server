package com.raritan.tdz.dctimport.integration.transformers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.dto.WireNodeInterface;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.circuit.service.PowerProc;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.PowerConnImport;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.UserLookup;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.settings.service.ApplicationSettingsService;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.vpc.home.VPCHome;

/**
 * Converter to get the CircuitDTO object for data circuit
 * @author Santo Rosario
 *
 */
public class PowerConnBeanTransformer implements ImportBeanToParameter {

	private String uuid;
		
	@Autowired
	protected PowerCircuitDAO powerCircuitDAO;
	
	@Autowired
	protected PowerPortDAO powerPortDAO;

	@Autowired
	private UserLookupFinderDAO userLookupFinderDAO;	
	
	@Autowired
	protected PowerConnDAO powerConnDAO;
	
	@Autowired
	private VPCHome vpcHome;

	@Autowired
	private LocationDAO locationDAO;
	
	@Autowired
	private ApplicationSettingsService appSettingService;
	
	@Autowired
	private CircuitPDService circuitPDService;
	
	public PowerConnBeanTransformer(String uuid) {
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
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception, ServiceLayerException {
		
		return null;
	}
	
	protected CircuitDTO newPowerCircuitDtoFromDCImport(PowerConnImport dcImport, Errors errors, UserInfo userInfo) throws DataAccessException, ServiceLayerException {
		CircuitDTO circuit = new CircuitDTO();
		List<CircuitNodeInterface> nodeList = new ArrayList<CircuitNodeInterface>();
		
		//First port to add
		PowerPort portStart = powerPortDAO.getPort(dcImport.getStartingItemLocation(), dcImport.getStartingItemName(), dcImport.getStartingPortName());
		
		if (null == portStart) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.rejectValue("sourcePort", "Import.PowerConn.PortNotFound", errorArgs, "Cannot find source port");
			return null;
		}


		if(dcImport.isVpcEndingPort()) {			
			circuit = createVpcCircuit(dcImport, portStart.getPortId(), errors);
			circuit.setUserInfo(userInfo);			
			return circuit;
		}
		
		//Last port to add
		PowerPort portEnd = powerPortDAO.getPort(dcImport.getEndingItemLocation(), dcImport.getEndingItemName(), dcImport.getEndingPortName());
		
		if (null == portEnd) {
			Object[] errorArgs = {  dcImport.getEndingItemName() + ":" + dcImport.getEndingPortName() };
			errors.rejectValue("sourcePort", "Import.PowerConn.PortNotFound", errorArgs, "Cannot find source port");
			return null;
		}		
		
		//Add first node in circuits
		PowerPortNodeDTO nodeDTO = (PowerPortNodeDTO) PortsAdaptor.adaptPowerPortDomainToDTO(portStart, true, null);
		nodeDTO.setUsed(false);
		nodeList.add(nodeDTO);

		//Create wire node
		ConnectionCord cord = getCord(dcImport, errors);
		WireNodeInterface wireDTO = createWireNode(portStart, portEnd, cord, userInfo);				
		nodeList.add(wireDTO);
		
		//add last node in circuit
		nodeDTO = (PowerPortNodeDTO) PortsAdaptor.adaptPowerPortDomainToDTO(portEnd, true, null);
		nodeDTO.setUsed(false);
		nodeList.add(nodeDTO);
		
		circuit.setNodeList(nodeList);

		setCircuitInfo(circuit, dcImport);

		if (circuit.getProposeCircuitId() != null && circuit.getProposeCircuitId() > 0) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.reject("circuit.editInstalledWithExistingProposed", errorArgs, "proposed circuit exists");
			return null;
		}		

		//Add partial circuit if exists
		completeCircuit(circuit, portEnd.getPortId());	
		
		circuit.setUserInfo(userInfo);

		return circuit;
	}

	
	protected ConnectionCord getCord(PowerConnImport dcImport,  Errors errors) {
		String color = "%";

		ConnectionCord cord = new ConnectionCord();
		
		if(dcImport.getCordColor() != null && dcImport.getCordColor().length() > 0) {
			color = dcImport.getCordColor().isEmpty() ? color : dcImport.getCordColor();
		}
		
		if(dcImport.getCordType() != null && dcImport.getCordType().isEmpty() == false) { 	
			List<LkuData> lkuType = userLookupFinderDAO.findByLkpValueLkpAttributeAndTypeCaseInsensitive(dcImport.getCordType(), color, UserLookup.LkuType.CORD);
			
			if(lkuType == null || lkuType.size() == 0) {
				Object[] errorArgs = { dcImport.getCordType() + (color.equals("%") ? "" : " with color " + color)  };
				errors.reject("Import.Circuit.CircuitCordTypeNotExist", errorArgs, "Cannot find cord type");
				return null;
			}
					
			cord.setCordLookup(lkuType.get(0));
		}
		
		if(dcImport.getCordId() != null && dcImport.getCordId().isEmpty() == false) {
			cord.setCordLabel(dcImport.getCordId());
		}
		
		String cordLen = dcImport.getCordLength();
		
		if(cordLen != null && cordLen.isEmpty() == false) {
			if(!GlobalUtils.isNumeric(cordLen )){
				Object[] errorArgs = {cordLen  };
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
	
	protected void setCircuitInfo(CircuitDTO circuit, PowerConnImport dcImport) throws DataAccessException {
		if(dcImport.getCircuitId() != null && dcImport.getCircuitId() > 0) {
			circuit.setCircuitId(dcImport.getCircuitId());
			circuit.setStatusCode(dcImport.getStatusLksValueCode());	
			
			Long id = dcImport.getCircuitId() == null ? null : dcImport.getCircuitId().longValue();
			circuit.setProposeCircuitId(powerCircuitDAO.getProposedCircuitId(id));
		}
		else {
			circuit.setStatusCode(SystemLookup.ItemStatus.PLANNED);
		}
		circuit.setOrigin(SystemLookup.ItemOrigen.IMPORT);
	}

	protected WireNodeInterface createWireNode(PowerPort sourcePort, PowerPort destPort, ConnectionCord cord, UserInfo userInfo) {
		//Create wire node
		PowerConnection conn = new PowerConnection();
		conn.setSourcePowerPort(sourcePort);
		conn.setConnectionCord(cord);		
		conn.setDestPowerPort(destPort);		
		WireNodeInterface wireDTO = PowerProc.newWireNode(conn, userInfo);
		
		return wireDTO;

	}

	protected void completeCircuit(CircuitDTO circuit, Long endPortId) throws DataAccessException {		
		String trace = powerCircuitDAO.getCircuitTrace(endPortId);
		
		if(trace == null || trace.trim().isEmpty()) return;
		
		String[] connIds = trace.split(",");
		UserInfo userInfo = circuit.getUserInfo();

		for(String connId:connIds) {
			if(connId.isEmpty()) continue;
			
			PowerConnection conn = powerConnDAO.getConn(Long.parseLong(connId));
			
			if(conn.getDestPowerPort() == null) break;

			WireNodeInterface wireDTO = PowerProc.newWireNode(conn, userInfo);
			PowerPortNodeDTO nodeDTO = (PowerPortNodeDTO) PortsAdaptor.adaptPowerPortDomainToDTO(conn.getDestPowerPort(), true, null);
				
			circuit.getNodeList().add(wireDTO);
			circuit.getNodeList().add(nodeDTO);
		}			
	}
	
	protected CircuitDTO createVpcCircuit(PowerConnImport dcImport, Long startPortId, Errors errors) throws ServiceLayerException {			
		Long locationId = locationDAO.getLocationIdByCode(dcImport.getEndingItemLocation());
			
		if (null == locationId) {
			Object[] errorArgs = {  dcImport.getEndingItemLocation() };
			errors.reject("Import.locationNotFound", errorArgs, "Cannot find location code");
			return null;
		}
		
		if(!appSettingService.isVPCEnabledForLocation(dcImport.getEndingItemLocation())) {
			Object[] errorArgs = {  dcImport.getEndingItemLocation() };
			errors.reject("PowerChain.vpcNotEnabled", errorArgs, "VPC not enabled for location code");
			return null;			
		}
		
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setStartPortId(startPortId);
		cCriteria.setVpcChainLabel(dcImport.getVpcLabel());
		cCriteria.setLocationId(locationId);
		cCriteria.setCircuitType(SystemLookup.PortClass.POWER);
		
		for(CircuitDTO cr:circuitPDService.viewCircuitByCriteria(cCriteria)) {
			cr.setCircuitId(null);
			setCircuitInfo(cr, dcImport);

			return cr;
		}	
		
		return null;
	}


}
