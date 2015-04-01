package com.raritan.tdz.circuit.service;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
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
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortConnectorDTO;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * Circuit business logic tests. Only add tests that create new circuits 
 * 
 * @author Santo Rosario
 */
public class CircuitEditabilityTest extends TestBase {

	private CircuitPDService service;


	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		service = (CircuitPDService)ctx.getBean("circuitPDService");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}


/*	@Test
	public void testPartialCircuitEditability1(){
		Float circuitId = 5960.2001953125F; //2B-RPDU-R partial circuit
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitId(5960.2F);
		cCriteria.setCircuitType(20000L);
		cCriteria.setMaxLinesPerPage(-1);
		cCriteria.setPageNumber(1);
		cCriteria.setSecondPortSearched(false);
		cCriteria.setSkipPowerCalc(false);

		List<Float> circuitIdList = new ArrayList<Float>();
		circuitIdList.add(new Float(circuitId));

		try {
			String status = service.getCircuitButtonStatus(circuitIdList );

			System.out.println(status);
			//TODO: check that delete is set to false, state is 11 and disconenct is false

			List<CircuitDTO> list = service.viewCircuitByCriteria(cCriteria);

			for( CircuitDTO dto : list ){
				System.out.println("circuit - read-only: " + dto.isReadOnly());
				Assert.assertFalse(dto.isReadOnly());
				List<CircuitNodeInterface> nodes = dto.getNodeList();
				int i=0;
				for( CircuitNodeInterface node : nodes ){
					if(node instanceof PowerPortNodeDTO ){
						PowerPortNodeDTO powerPortNode = (PowerPortNodeDTO)node;
						System.out.println("       node - " + powerPortNode.getPortName() + " - readonly=" + powerPortNode.getReadOnly());

						//only second node of partial power circuit should be editable 
						if( i == 1 ){
							Assert.assertTrue(powerPortNode.getReadOnly() == false);
						}else{
							Assert.assertTrue(powerPortNode.getReadOnly() == true);
						}
						++i;
					}
				}
			}
		} catch (ServiceLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/

/*	@Test
	public void testFullCircuitEditability1(){
		Float circuitId = 6176.2001953125F; //CLARITY-03 circuit
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitId(6176.2F);
		cCriteria.setCircuitType(20000L);
		cCriteria.setMaxLinesPerPage(-1);
		cCriteria.setPageNumber(1);
		cCriteria.setSecondPortSearched(false);
		cCriteria.setSkipPowerCalc(false);

		List<Float> circuitIdList = new ArrayList<Float>();
		circuitIdList.add(new Float(circuitId));

		try {
			String status = service.getCircuitButtonStatus(circuitIdList );

			System.out.println(status);
			//TODO: check that delete is set to false, state is 11 and disconenct is false

			List<CircuitDTO> list = service.viewCircuitByCriteria(cCriteria);

			for( CircuitDTO dto : list ){
				System.out.println("circuit - read-only: " + dto.isReadOnly());
				Assert.assertFalse(dto.isReadOnly());
				List<CircuitNodeInterface> nodes = dto.getNodeList();
				int i=0;
				for( CircuitNodeInterface node : nodes ){
					if(node instanceof PowerPortNodeDTO ){
						PowerPortNodeDTO powerPortNode = (PowerPortNodeDTO)node;
						System.out.println("       node - " + powerPortNode.getPortName() + " - readonly=" + powerPortNode.getReadOnly());

						//only first and second node of a full power circuit should be editable 
						if( i == 0 || i == 1 ){
							Assert.assertTrue(powerPortNode.getReadOnly() == false);
						}else{
							Assert.assertTrue(powerPortNode.getReadOnly() == true);
						}
						++i;
					}
				}
			}
		} catch (ServiceLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
}