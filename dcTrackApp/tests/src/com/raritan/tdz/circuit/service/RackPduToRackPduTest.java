package com.raritan.tdz.circuit.service;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.PatchCordDTO;
import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.dto.VirtualWireDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.CircuitProc;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.tests.TestBase;

/**
 * Circuit business logic tests. Only add tests that create new circuits 
 * 
 * @author Santo Rosario
 */
public class RackPduToRackPduTest extends TestBase {

	private CircuitPDService service;
	private CircuitPDHome circuitHome;
	private ItemDAO itemDAO;
	private PowerPortDAO powerPortDAO;
	private PowerConnDAO powerConnDAO;	
	private PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		service = (CircuitPDService)ctx.getBean("circuitPDService");
		circuitHome = (CircuitPDHome)ctx.getBean("circuitPDHome");
		itemDAO = (ItemDAO)ctx.getBean("itemDAO");
		powerPortDAO = (PowerPortDAO)ctx.getBean("powerPortDAO");
		powerConnDAO = (PowerConnDAO)ctx.getBean("powerConnectionDAO");
		powerCircuitDAO = (PowerCircuitDAO)ctx.getBean("powerCircuitDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	

	@Test
	public final void testRpdusToOutlet() throws Throwable {
		for(int i=1; i<6; i++){
			createRpdusToOutlet(i);
		}
	}	

	public void createRpdusToOutlet(int numberRpdus) throws Throwable {
		System.out.println("\n\ncreateRpdusToOutlet using " + numberRpdus + " input cord(s).\n");
		
		CircuitDTO circuit = new CircuitDTO();
		List<CircuitNodeInterface> nodeList = this.getRpdusToOutletNodes(numberRpdus, false);
		
		circuit.setNodeList(nodeList);

		CircuitDTO newCircuit = this.service.saveCircuit(circuit);
		
		AssertJUnit.assertTrue(newCircuit.getNodeList().size() == nodeList.size());	
		
		System.out.println("\n\ncreateRpdusToOutlet using " + numberRpdus + " input cord(s).\n");
		for(CircuitNodeInterface n:newCircuit.getNodeList()){
			n.print();
		}
		
		CircuitCriteriaDTO circuitCriteria = new CircuitCriteriaDTO();
		circuitCriteria.setCircuitType(circuit.getCircuitType());
		circuitCriteria.setPortId(getFirstRpduInputPort(nodeList));

		List<CircuitDTO> circuitDTOs = service.viewCircuitByCriteria(circuitCriteria);
		
		AssertJUnit.assertTrue(circuitDTOs.size() == numberRpdus);  //should return two circuits
		
		//check circuit trace
		for(CircuitDTO cir:circuitDTOs){
			AssertJUnit.assertFalse(cir.getCircuitTrace() == null);
			
			if(cir.getSharedCircuitTrace() != null && cir.getSharedCircuitTrace().length() > 0){
				AssertJUnit.assertTrue(cir.getCircuitTrace().contains(cir.getSharedCircuitTrace()));
			}
		}
		deleteCircuit(circuitDTOs, nodeList);
		
		//Commit changes
		sf.getCurrentSession().flush();
	}	
	
	private List<CircuitNodeInterface> getOutletNodes(long startPortId) throws DataAccessException{
		List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
		PowerPortNodeDTO node;		
		
		CircuitProc circuitProc = new CircuitProc(this.sf.getCurrentSession());
		
		PowerConnection conn = (PowerConnection)circuitProc.getPortConnection(startPortId, SystemLookup.PortClass.POWER, true);
		
		while(conn != null){
			List<Long> movePortIds = new ArrayList<Long>();
			movePortIds.add(conn.getSourcePowerPort().getPortId());
			Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);

			node = (PowerPortNodeDTO)PortsAdaptor.adaptPowerPortDomainToDTO(conn.getSourcePowerPort(), true, portsAction);
			nodeList.add(node);	
			
			if(conn.getDestPort() != null){
				nodeList.add(PowerProc.newWireNode(conn, null));
			}
			
			conn = (PowerConnection)circuitProc.getPortConnection(conn.getDestPortId(), SystemLookup.PortClass.POWER, true);
		}
		
		return nodeList;
	}
	
	private List<CircuitNodeInterface> getRpdusToOutletNodes(int numberRackPdus, boolean addRecpAtEnd) throws Throwable {
		List<CircuitNodeInterface> nodeList = null;
		PowerPortNodeDTO node;		
		PowerPort priorRecp = null;
		
		for(int i=1; i<=numberRackPdus; i++){
			//Create Input Cord
			Item rackPdu = this.getNewRackPdu();
			PowerPort inputCord = null;
			PowerPort recp = null;
			
			for(PowerPort port:rackPdu.getPowerPorts()){
				if(port.isInputCord()){
					inputCord = port;
					break;
				}
			}

			for(PowerPort port:rackPdu.getPowerPorts()){
				if(port.isRackPduOutlet()){
					recp = port;
					//Set output to same rating of input port
					recp.setVoltsLookup(inputCord.getVoltsLookup());
					recp.setConnectorLookup(inputCord.getConnectorLookup());
					recp.setPhaseLookup(inputCord.getPhaseLookup());
					recp.setAmpsBudget(inputCord.getAmpsBudget());
					recp.setAmpsNameplate(inputCord.getAmpsNameplate());
					
					this.powerPortDAO.update(recp);				
					
					break;
				}
			}
			
			
			if(i == 1){ //do this one time only
				//Create outlet
				Item outlet = this.getNewOutlet();
				PowerPort outletPort = null;
				
				for(PowerPort port:outlet.getPowerPorts()){
					outletPort = port;
					break;
				}
				
				//connect outlet port to an existing breaker 
				PowerConnection conn = connectOutletToBreaker(outletPort);
				
				//Commit changes
				sf.getCurrentSession().flush();
				
				nodeList = this.getOutletNodes(outletPort.getPortId());
			}
			
			if(priorRecp != null){
				//Add rack pdu rec to start
			
				nodeList.add(0, new VirtualWireDTO());  //wire between input cord and outlet
				
				List<Long> movePortIds = new ArrayList<Long>();
				movePortIds.add(priorRecp.getPortId());
				Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);
				node = (PowerPortNodeDTO)PortsAdaptor.adaptPowerPortDomainToDTO(priorRecp, true, portsAction);
				nodeList.add(0, node);
			}
			
			//Add input cord to start of circuit
			nodeList.add(0, new PatchCordDTO());  //wire between input cord and outlet

			List<Long> movePortIds = new ArrayList<Long>();
			movePortIds.add(inputCord.getPortId());
			Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);

			node = (PowerPortNodeDTO)PortsAdaptor.adaptPowerPortDomainToDTO(inputCord, true, portsAction);
			nodeList.add(0, node);
			
			priorRecp = recp;
			
			if(i == numberRackPdus && addRecpAtEnd){
				//Add rack pdu rec to start of circuit				
				nodeList.add(0, new VirtualWireDTO());  //wire between input cord and outlet
				
				node = (PowerPortNodeDTO)PortsAdaptor.adaptPowerPortDomainToDTO(recp, true, portsAction);
				nodeList.add(0, node);				
			}
		}
		
		return nodeList;		
	}	

	private void deleteCircuit(List<CircuitDTO> circuitDTOs, List<CircuitNodeInterface> nodeList) throws ServiceLayerException{

		if(nodeList == null || circuitDTOs == null) return;
	
		for(CircuitNodeInterface n:nodeList){
			if(n instanceof PowerPortNodeDTO){ 
				PowerPortNodeDTO node = (PowerPortNodeDTO)n;
				CircuitDTO c = getCircuitForPort(circuitDTOs, node.getPortId());
				
				if(c == null) continue;
				
				List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();
				CircuitCriteriaDTO rec = new CircuitCriteriaDTO();
				rec.setCircuitId(c.getCircuitId());
				rec.setCircuitType(c.getCircuitType());
				recList.add(rec);
				
				this.service.deleteCircuitByIds(recList);
				
				
			}
		}
	}

	private Item getNewRackPdu(){
		//clone rack pdu "1A-RPDU-L" with itemId 945 from demo db
		CloneItemDTO cloningCriteria = new CloneItemDTO(945);
		Long itemId =  itemDAO.cloneItem(cloningCriteria , "admin");
		
		Item item = itemDAO.loadItem(itemId);
		Item parent = new Item();
		parent.setItemId(3);
		item.setParentItem(parent);
		
		itemDAO.update(item);
		
		this.addTestItem(item);
		
		return item;
	}

	private Item getNewOutlet(){
		//clone floor outlet "PDU-2A/PB1:1,3" with itemId 627 from demo db
		CloneItemDTO cloningCriteria = new CloneItemDTO(627);
		Long itemId =  itemDAO.cloneItem(cloningCriteria , "admin");
		
		Item item = itemDAO.loadItem(itemId);
		
		this.addTestItem(item);
		
		return item;
	}
	
	private PowerConnection connectOutletToBreaker(PowerPort outlet){
		
		try {
			PowerPort breaker = powerPortDAO.loadPort(7886L); //Breaker Port 38,40 on PDU-2A Panel PB2
			
			this.session = sf.getCurrentSession();
			PowerConnection conn = new PowerConnection();
			conn.setSourcePowerPort(outlet);
			conn.setDestPowerPort(breaker);
			conn.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED));
			conn.setConnectionType(SystemLookup.getLksData(session, SystemLookup.LinkType.IMPLICIT));
			
			session.save(conn);
			
			return conn;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
		return null;
	}
	
	private CircuitDTO getCircuitForPort(List<CircuitDTO> recList, Long portId){
		PowerPortNodeDTO node;
		
		for(CircuitDTO r:recList){
			if(!(r.getNodeList().get(0)instanceof PowerPortNodeDTO)) continue;
			
			node = (PowerPortNodeDTO)r.getNodeList().get(0);
			
			if(portId.equals(node.getPortId())){
				return r;
			}
		}
		
		return null;
	}
	

	private Long getFirstRpduInputPort(List<CircuitNodeInterface> recList){
		PowerPortNodeDTO node;
		
		for(int i = recList.size()-1; i>=0; i--){
			if(!(recList.get(i) instanceof PowerPortNodeDTO)) continue;
			
			node = (PowerPortNodeDTO)recList.get(i);
			
			if(node.isInputCord()){
				return node.getPortId();
			}
		}
		
		return null;
	}
	
}


