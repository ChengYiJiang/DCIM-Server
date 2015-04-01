package com.raritan.tdz.circuit.home;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;



import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitListDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.DataPortNodeDTO;
import com.raritan.tdz.circuit.dto.PatchCordDTO;
import com.raritan.tdz.circuit.dto.PowerCableDTO;
import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.circuit.dto.VirtualWireDTO;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortConnectorDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.validators.Validator;
import com.raritan.tdz.circuit.home.DataCircuitHome;

/**
 * Circuit business logic tests. Note that these tests depend upon circuits in the demo DB.
 * 
 * TODO: Add more tests for saving and deleting power circuits
 * 
 * @author Andrew Cohen
 */
public class CircuitTests extends TestBase {

	// Test circuit IDs from the demo DB
	private static final float[] PARTIAL_USED_DATA_CIRCUIT_IDS = new float[] { 11827.3F, 11875.3F };
	private static final float[] NON_PARTIAL_USED_DATA_CIRCUIT_IDS = new float[] { 1.3F, 5.3F, 9.3F };
	private static final float[] PARTIAL_USED_POWER_CIRCUIT_IDS = new float[] { 6400.2F, 8896.2F };
	private static final float[] NON_PARTIAL_USED_POWER_CIRCUIT_IDS = new float[] { 5376.2F, 5377.2F };
	
	private static final float[] DATA_CIRCUITS_WITH_SHARED_CONNS = new float[] { 11923.3F, 11924.3F };
	private static final float[] POWER_CIRCUITS_WITH_SHARED_CONNS = new float[] { 6566.2F, 8902.2F, 3301.2F };
	
	private static final float PROPOSED_POWER_CIRCUIT_ID_1 = 19.1F;
	private static final long ORIGINAL_POWER_CIRCUIT_ID_1 = 4143L;
	
	private CircuitPDHome home;
	private CircuitPDService service;
	private DataCircuitHome dataCircuitHome;
	UserInfo testUser;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		home = (CircuitPDHome)ctx.getBean("circuitPDHome");
		service = (CircuitPDService)ctx.getBean("circuitPDService");
		dataCircuitHome = (DataCircuitHome)ctx.getBean("dataCircuitHome");
		
		testUser = this.getTestAdminUser();
		testUser.setRequestBypass(true);
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}


	@Test
	public final void testViewCircuitByCriteria() throws Throwable {
		CircuitCriteriaDTO criteria = new CircuitCriteriaDTO();
		criteria.setCircuitId( 1.1F );
		assertUniqueDataCircuitResult( dataCircuitHome.viewDataCircuitByCriteria( criteria ) );
	}
	
	@Test
	public final void testViewCircuitListForChassis() throws Throwable {
		CircuitCriteriaDTO criteria = new CircuitCriteriaDTO();
		criteria.setItemId( 340L ); // Chassis item - NJA01A (Site A)
		assertUniqueDataCircuitViewListResult( home.viewCircuitPDList( criteria ) );
	}
	
	@Test
	public final void testViewCircuitListForStack() throws Throwable {
		CircuitCriteriaDTO criteria = new CircuitCriteriaDTO();
		criteria.setItemId( 1002L ); // Network Stack item - NJAoffice-01 (Site A) 
		assertUniqueDataCircuitViewListResult( home.viewCircuitPDList( criteria ) );
	}
	
	@Test
	public final void testDetectPartialCircuitInUse() throws Throwable {
		for (float dataCircuitId : PARTIAL_USED_DATA_CIRCUIT_IDS) {
			// Test home
			AssertJUnit.assertTrue( home.isPartialCircuitInUse( getCircuitViewData(dataCircuitId) ) );
			// Test service for viewCircuitByCircuitCriteria
			CircuitDTO circuit = getCircuitByCircuitIdCriteria(dataCircuitId);
			AssertJUnit.assertTrue( circuit.isPartialCircuitInUse() );
		}
		for (float dataCircuitId : NON_PARTIAL_USED_DATA_CIRCUIT_IDS) {
			// Test home
			AssertJUnit.assertFalse( home.isPartialCircuitInUse( getCircuitViewData(dataCircuitId) ) );
			// Test service for viewCircuitByCircuitCriteria
			CircuitDTO circuit = getCircuitByCircuitIdCriteria(dataCircuitId);
			AssertJUnit.assertFalse( circuit.isPartialCircuitInUse() );
		}
		for (float dataCircuitId : PARTIAL_USED_POWER_CIRCUIT_IDS) {
			// Test home
			AssertJUnit.assertTrue( home.isPartialCircuitInUse( getCircuitViewData(dataCircuitId) ) );
			// Test service for viewCircuitByCircuitCriteria
			CircuitDTO circuit = getCircuitByCircuitIdCriteria(dataCircuitId);
			AssertJUnit.assertTrue( circuit.isPartialCircuitInUse() );
		}
		for (float dataCircuitId : NON_PARTIAL_USED_POWER_CIRCUIT_IDS) {
			// Test home
			AssertJUnit.assertFalse( home.isPartialCircuitInUse( getCircuitViewData(dataCircuitId) ) );
			// Test service for viewCircuitByCircuitCriteria
			CircuitDTO circuit = getCircuitByCircuitIdCriteria(dataCircuitId);
			AssertJUnit.assertFalse( circuit.isPartialCircuitInUse() );
		}
	}
	
	@Test
	public final void testSharedCircuitNodesAreReadOnly() throws Throwable {
		
		for (float powerCircuitId : POWER_CIRCUITS_WITH_SHARED_CONNS) {
			CircuitDTO circuit = getCircuitByCircuitIdCriteria( powerCircuitId );
			String sharedTrace = circuit.getSharedCircuitTrace();
			assertNotNull( sharedTrace, "sharedTrace was not Null" ); 
			Set<Long> connIds = getCircuitTraceIds( sharedTrace );
			for (CircuitNodeInterface node : circuit.getNodeList()) {
				if (connIds.contains( node.getId() )) {
					AssertJUnit.assertTrue( node.getReadOnly() );
				}
			}
		}
		
		for (float dataCircuitId : DATA_CIRCUITS_WITH_SHARED_CONNS) {
			CircuitDTO circuit = getCircuitByCircuitIdCriteria( dataCircuitId );
			String sharedTrace = circuit.getSharedCircuitTrace();
			assertNotNull( sharedTrace, "sharedTrace was not Null" ); 
			Set<Long> connIds = getCircuitTraceIds( sharedTrace );
			for (CircuitNodeInterface node : circuit.getNodeList()) {
				if (connIds.contains( node.getId() )) {
					AssertJUnit.assertTrue( node.getReadOnly() );
				}
			}
		}
	}
	
	
	@Test
	public final void testSavePartialConnection() throws Throwable {
		long circuitId = -1;
		
		try {
			CircuitDTO c = new CircuitDTO();
			c.setCircuitId( 0F );
			c.setImplicit( false );
			c.setUserInfo(testUser);
			
			List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
			
			DataPortNodeDTO n1 = new DataPortNodeDTO();
			n1.setMediaLksDesc("Twisted Pair");
			n1.setItemId( 546L );
			n1.setPortId( 3188L );
			n1.setPortSubClassLksValueCode( 30002L );
			n1.setItemClassLksValueCode( 0L );
			n1.setLocationId( 40L );
			n1.setUsed( false );
			
			PortConnectorDTO c1 = new PortConnectorDTO();
			c1.setConnectorId( 14L );
			c1.setConnectorName("RJ45");
			n1.setConnector( c1 );
			nodeList.add( n1 );
			
			StructureCableDTO n2 = new StructureCableDTO();
			n2.setMediaLksId( 61L );
			n2.setCableGradeLkuId( 996L );
			nodeList.add( n2 );
			
			DataPortNodeDTO n3 = new DataPortNodeDTO();
			n3.setMediaLksDesc("Twisted Pair");
			n3.setPortId( 3187L );
			n3.setPortSubClassLksValueCode( 30002L );
			n3.setLocationId( 1L );
			n3.setUsed( false );
			n3.setItemClassLksValueCode( 1500L );
			PortConnectorDTO c2 = new PortConnectorDTO();
			c2.setConnectorId( 14L );
			c2.setConnectorName("RJ45");
			n3.setConnector( c2 );
			nodeList.add( n3 );
			
			PatchCordDTO n4 = new PatchCordDTO();
			n4.setFePortName("A0050");
			n4.setNePortName("P01");
			nodeList.add( n4 );
			
			DataPortNodeDTO n5 = new DataPortNodeDTO();
			n5.setPortId( 48435L );
			n5.setItemClassLksValueCode( 1300L );
			n5.setPortSubClassLksValueCode( 30001L );
			n5.setPortName("P01");
			n5.setLocationId( 6L );
			PortConnectorDTO c3 = new PortConnectorDTO();
			c3.setConnectorId( 14L );
			c3.setConnectorName("RJ45");
			n5.setConnector( c3 );
			nodeList.add( n5 );
			
			c.setNodeList( nodeList );
			CircuitDTO circuit = service.saveCircuit( c );
			sf.getCurrentSession().flush();
			
			assertValidCircuitDTO( circuit );
			
			CircuitUID uid = circuit.getCircuitUID();
			circuitId = uid.getCircuitDatabaseId();
		}
		finally {
			if (circuitId > 0) {
				List<Long> ids = new LinkedList<Long>();
				ids.add( circuitId );
				dataCircuitHome.deleteDataCircuitByIds(ids, false);
			}
		}
	}
	
	@Test
	public final void testCircuitListHasUniqueItems() throws Throwable {
		CircuitCriteriaDTO c = new CircuitCriteriaDTO();
		c.setItemId( 504L );
		c.setEndWithClassCode( 1300L );
		c.setStartWithClassCode( 1300L );
		c.setCircuitType( 30000L );
		
//		List<CircuitViewData> l1 = home.viewCircuitPDList( c );
//		for (CircuitViewData l : l1) {
//			System.out.println(l.getStartItemName() + "/" + l.getStartPortName());
//		}
		
		List<CircuitListDTO> list = service.viewCircuitPDList( c );
		for (CircuitListDTO l : list) {
			System.out.println(l.getStartItemName() + "___" + l.getStartPortName());
		}
	}
	
	@Test
	public final void testGetOriginalFromProposed() throws Throwable {
		ICircuitInfo proposed = null;
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitType( SystemLookup.PortClass.POWER );
		cCriteria.setCircuitId( PROPOSED_POWER_CIRCUIT_ID_1 );
		List<? extends ICircuitInfo> circuits = home.viewCircuitByCriteria( cCriteria );
		if (circuits != null && !circuits.isEmpty()) {
			proposed = circuits.get(0);
		}
		
		AssertJUnit.assertNotNull(proposed);
		
		ICircuitInfo orig = home.getOriginalCircuitForProposed( 
				new CircuitUID( PROPOSED_POWER_CIRCUIT_ID_1 ).getCircuitDatabaseId(),
				SystemLookup.PortClass.POWER
		);
		
		AssertJUnit.assertNotNull(orig);
		AssertJUnit.assertEquals(ORIGINAL_POWER_CIRCUIT_ID_1, orig.getCircuitId());
	}
	

	//
	// Private methods
	//
	private CircuitDTO getCircuitByCircuitIdCriteria(float circuitId) throws Throwable {
		CircuitCriteriaDTO c = new CircuitCriteriaDTO();
		CircuitUID uid = new CircuitUID( circuitId ); 
		c.setCircuitId( circuitId );
		c.setCircuitType( uid.isDataCircuit() ? SystemLookup.PortClass.DATA : SystemLookup.PortClass.POWER );
		List<CircuitDTO> circuits = service.viewCircuitByCriteria( c );
		AssertJUnit.assertNotNull( circuits );
		AssertJUnit.assertEquals(1, circuits.size());
		return circuits.get(0);
	}
	
	private CircuitViewData getCircuitViewData(float circuitId) throws Throwable {
		CircuitCriteriaDTO c = new CircuitCriteriaDTO();
		c.setCircuitId( circuitId );
		return home.getCircuitViewData( c );
	}
	
	private Set<Long> getCircuitTraceIds(String circuitTrace) throws Throwable {
		Set<Long> ids = new HashSet<Long>();
		for (String connId : circuitTrace.split(",\\s*")) {
			if (!connId.isEmpty()) {
				ids.add( Long.parseLong( connId ) );
			}
		}
		return ids;
	}
	
	private void assertUniqueDataCircuitResult(List<DataCircuit> dataCircuits) {
		assertNotNull( dataCircuits );
		assertTrue( dataCircuits.size() > 0 ); // Could have multiple circuits for item
		
		DataCircuit dc = dataCircuits.get(0);
		assertNotNull( dc );
	}
	
	private void assertUniqueDataCircuitViewListResult(List<CircuitViewData> viewData) {
		assertNotNull( viewData );
		assertTrue( viewData.size() > 0 ); // Could have multiple circuits for item
		
		CircuitViewData cv = viewData.get(0);
		assertNotNull( cv );
	}
	
	private void assertValidCircuitDTO(CircuitDTO circuit) {
		assertNotNull( circuit );
		assertTrue( circuit.getCircuitUID().getCircuitDatabaseId() > 0 );
	}
	
	//@Test
	public final void testCreateFullPowerCircuit() throws Throwable {
		CircuitDTO circuit = new CircuitDTO();

		List<Long> portList = new ArrayList<Long>();		
		portList.add(5800L);//server power supply port
		portList.add(4796L);//outlet port of RPDU
		portList.add(4779L);//Input port of RPDU
		portList.add(2261L);//floor outlet 
		portList.add(2067L);//breaker port
				
		List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
		PowerPortNodeDTO node;		
				
		for(Long portId:portList){
			node = new PowerPortNodeDTO();
			node.setPortId(portId);
			node.setUsed(true);
			nodeList.add(node);
			
			if(portId.longValue() == 4796){      //outlet port of RPDU
				nodeList.add(new VirtualWireDTO());
			}
			else if(portId.longValue() == 2261){  //floor outlet 
				nodeList.add(new PowerCableDTO());
			}
			else if(portId.longValue() != 2067){  //last port, breaker port
				nodeList.add(new PatchCordDTO());
			}			
		}
		circuit.setNodeList(nodeList);
		
		this.service.saveCircuit(circuit);			
	}
}


