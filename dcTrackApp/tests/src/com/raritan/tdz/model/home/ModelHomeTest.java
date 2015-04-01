package com.raritan.tdz.model.home;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;

import com.raritan.tdz.domain.ModelDataPorts;
import com.raritan.tdz.domain.ModelPorts;
import com.raritan.tdz.domain.ModelPowerPorts;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.model.dto.ModelDTO;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.tests.TestBase;

/**
 * 
 */

/**
 * @author prasanna
 *
 */
public class ModelHomeTest extends TestBase {

	ModelHome modelHome;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		modelHome = (ModelHome)ctx.getBean("modelHome");
	}

	/**
	 * Test method for {@link com.raritan.tdz.model.home.ModelHomeImpl#getAllMake()}.
	 */
	@Test
	public void testGetAllMake() {
		List<ValueIdDTO> makeValueIds = modelHome.getAllMakes();
		
		AssertJUnit.assertTrue (makeValueIds.size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.model.home.ModelHomeImpl#getAllModel(java.lang.Long)}.
	 */
	@Test
	public void testGetAllModel() {
		List<ValueIdDTO> makeValueIds = modelHome.getAllMakes();
		
		List<ValueIdDTO> modelValueIds = modelHome.getModels((Long)makeValueIds.get(0).getData(), null, null);
		AssertJUnit.assertTrue(modelValueIds.size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.model.home.ModelHomeImpl#getModelDetails(java.lang.Long)}.
	 * @throws Throwable 
	 */
	@Test
	public void testGetModelDetails() 
			throws Throwable {
		Map<String, UiComponentDTO> componentDTOs = modelHome.getModelDetails(new Long(1515), null);
		AssertJUnit.assertTrue(componentDTOs.size() > 0);
	}

	@Test
	public void testGetAllModelsForMake() {
		List<ValueIdDTO> makeValueIds = modelHome.getAllMakes();
		List<ModelDTO> modelList = modelHome.getAllModelsForMake((Long) makeValueIds.get(0).getData(), null, null);
		/* list is big so commenting out the printing data 
		System.out.println("model details for make " + (Long) makeValueIds.get(0).getData() + " = " + modelList.toString());
		*/

		// the Maufacturer's id = -1 get all models 
		modelList = modelHome.getAllModelsForMake(-1L, null, null);

		/* list is big so commenting out the printing data 
		System.out.println("model details for all makes = " + modelList.toString());
		*/
	}
	
	@Test
	public void testGetAllModelDataPorts() {
		long modelId = 1;
		try {
			List<DataPortDTO> dataPortDtos = modelHome.getAllDataPort(modelId);
			
			// verify dto 
			assertEquals((long)(dataPortDtos.get(0).getConnector().getConnectorId()), 14, "Invalid ConnecterId");
			assertEquals((String)dataPortDtos.get(0).getConnector().getConnectorName(), "RJ45", "Invalid ConnecterName");
			assertEquals((String)dataPortDtos.get(0).getMediaLksDesc(), "Twisted Pair", "Invalid mediaDesc");
			assertEquals((long)dataPortDtos.get(0).getMediaLksValueCode(), 7063, "Invalid mediaLksValueCode");
			assertEquals((String)dataPortDtos.get(0).getPortName(), "Eth01", "Invalid Port name");
			assertEquals((String)dataPortDtos.get(0).getProtocolLkuDesc(), "Ethernet/IP", "Invalid protocolDesc");
			assertEquals((long)dataPortDtos.get(0).getProtocolLkuId(), 1042, "Invalid protocoLkuId");
			assertEquals((String)dataPortDtos.get(0).getSpeedLkuDesc(), "10/100/1G Base-T", "Invalid speedLkuDesc");
			assertEquals((long)dataPortDtos.get(0).getSpeedLkuId(), 1030, "invalid speedLkuId");

			
			assertEquals((long)(dataPortDtos.get(1).getConnector().getConnectorId()), 14, "Invalid ConnecterId");
			assertEquals((String)dataPortDtos.get(1).getConnector().getConnectorName(), "RJ45", "Invalid ConnecterName");
			assertEquals((String)dataPortDtos.get(1).getMediaLksDesc(), "Twisted Pair", "Invalid mediaDesc");
			assertEquals((long)dataPortDtos.get(1).getMediaLksValueCode(), 7063, "Invalid mediaLksValueCode");
			assertEquals((String)dataPortDtos.get(1).getPortName(), "Eth02", "Invalid Port name");
			assertEquals((String)dataPortDtos.get(1).getProtocolLkuDesc(), "Ethernet/IP", "Invalid protocolDesc");
			assertEquals((long)dataPortDtos.get(1).getProtocolLkuId(), 1042, "Invalid protocoLkuId");
			assertEquals((String)dataPortDtos.get(1).getSpeedLkuDesc(), "10/100/1G Base-T", "Invalid speedLkuDesc");
			assertEquals((long)dataPortDtos.get(1).getSpeedLkuId(), 1030, "invalid speedLkuId");
			

			assertEquals((long)(dataPortDtos.get(2).getConnector().getConnectorId()), 14, "Invalid ConnecterId");
			assertEquals((String)dataPortDtos.get(2).getConnector().getConnectorName(), "RJ45", "Invalid ConnecterName");
			assertEquals((String)dataPortDtos.get(2).getMediaLksDesc(), "Twisted Pair", "Invalid mediaDesc");
			assertEquals((long)dataPortDtos.get(2).getMediaLksValueCode(), 7063, "Invalid mediaLksValueCode");
			assertEquals((String)dataPortDtos.get(2).getPortName(), "Eth03", "Invalid Port name");
			assertEquals((String)dataPortDtos.get(2).getProtocolLkuDesc(), "Ethernet/IP", "Invalid protocolDesc");
			assertEquals((long)dataPortDtos.get(2).getProtocolLkuId(), 1042, "Invalid protocoLkuId");
			assertEquals((String)dataPortDtos.get(2).getSpeedLkuDesc(), "10/100 Base-T", "Invalid speedLkuDesc");
			assertEquals((long)dataPortDtos.get(2).getSpeedLkuId(), 1029, "invalid speedLkuId");
		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetAllModelPowerPorts() {
		long modelId = 1;
		try {
			List<PowerPortDTO> powerPortDtos = modelHome.getAllPowerPort(modelId );
			
			assertEquals((long)(powerPortDtos.get(0).getConnector().getConnectorId()), 72, "Invalid ConnecterId");
			assertEquals((String)powerPortDtos.get(0).getConnector().getConnectorName(), "IEC-320-C14", "Invalid ConnecterName");
			assertEquals((long)powerPortDtos.get(0).getAmpsNameplate(), 0, "Invalid Amps Nameplate");
			assertEquals((long)powerPortDtos.get(0).getAmpsBudget(), 0, "Invalid Amps Nameplate");

			assertEquals((long)powerPortDtos.get(0).getWattsNameplate(), 400, "Invalid Watts Nameplate");
			assertEquals((long)powerPortDtos.get(0).getWattsBudget(), 240, "Invalid Watts Nameplate");
			assertEquals((long)powerPortDtos.get(0).getPhaseLksValueCode(), 7021, "Invalid PhaseksValueCode");
			assertEquals((String)powerPortDtos.get(0).getPhaseLksDesc(), "Single Phase (3-Wire)", "Invalid PhaseLksDesc");
			assertEquals((String)powerPortDtos.get(0).getPortName(), "PS1", "Invalid portName");
			assertEquals((double)powerPortDtos.get(0).getPowerFactor(), 1.0, "Invalid powerFactor");
			assertEquals((String)powerPortDtos.get(0).getVoltsLksDesc(), "120~240", "Invalid voltsLksDesc");
			assertEquals((long)powerPortDtos.get(0).getVoltsLksValueCode(), 0, "invalid voltsLksValueCode");
		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
	}
	
}
