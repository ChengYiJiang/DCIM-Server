package com.raritan.tdz.item.home;


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;



public class EffectivePowerTests extends TestBase {
	private ItemHome itemHome;
	final UserInfo gatekeeper = new UserInfo(1L, "1", "admin", "admin@localhost",
			"System", "Administrator", "941", "", "1", "en-US",
			"site_administrators",
			"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
			5256000, true);


	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemHome = (ItemHome) ctx.getBean("itemHome");
	}

	@AfterMethod
	public void tearDown() throws Throwable {

		super.tearDown();
	}

	@Test
	public final void testCabinetItemListEffectivePower() throws Throwable {
		Long cabinetId = 3L;
		List<Map<String,Object>> items = itemHome.getCabinetItemList(cabinetId);

		Assert.assertEquals(items.size() > 0, true);
		int numNonNullEffPowerVals = 0;
		for( Map<String, Object> i : items){
			Set<String> keys = i.keySet();
			for( String k : keys){
				if( k.equals("tiEffectivePower")) {
					Double effPowerId = (Double) i.get(k);
					if( effPowerId != null && effPowerId.doubleValue() > 0.0) ++numNonNullEffPowerVals;

				}
			}
		}
		Assert.assertEquals(numNonNullEffPowerVals > 0 , true);
	}

	@Test
	public final void testItemDetailsEffectivePower() throws Throwable {
		Long itemId = 57L;
		Map<String, UiComponentDTO> retval = itemHome.getItemDetails(itemId, gatekeeper);
		UiComponentDTO uiCmp1 = retval.get("tiEffectivePower");
		Assert.assertEquals(uiCmp1 != null , true);
		Assert.assertEquals(uiCmp1.getEditable().booleanValue(), false);
		System.out.println("effectivePower for " + itemId + " = " + (Double)uiCmp1.getUiValueIdField().getValue());
		List<PowerPortDTO> powerDTOList  = (List<PowerPortDTO>) retval.get("tabPowerPorts").getUiValueIdField().getValue();
		if( powerDTOList != null && powerDTOList.size() > 0){
			for(PowerPortDTO p :  powerDTOList){
				System.out.println("PP: itemName=" + p.getConnectedItemName() + ", circuitStatusValueCode=" + p.getCircuitStatusLksValueCode() +
						", circuitStatusLksValue=" + p.getCircuitStatusLksValue() );
				Assert.assertEquals(p.getCircuitStatusLksValueCode() == null ||
						p.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.INSTALLED ||
						p.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.POWERED_OFF ||
						p.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.PLANNED, true);			
			}
		}
	} 
	
	@Test
	public final void testDataPortsStatus() throws Throwable {
		Long itemId = 57L; //NJESX12
		Map<String, UiComponentDTO> retval = itemHome.getItemDetails(itemId, gatekeeper);
		
		List<DataPortDTO> dataDTOList  = (List<DataPortDTO>) retval.get("tabDataPorts").getUiValueIdField().getValue();
		if( dataDTOList != null && dataDTOList.size() > 0){
			for(DataPortDTO p :  dataDTOList){
				System.out.println("DP: itemName=" + p.getConnectedItemName() + ", circuitStatusValueCode=" + p.getCircuitStatusLksValueCode() +
						", circuitStatusLksValue=" + p.getCircuitStatusLksValue() );
				Assert.assertEquals(p.getCircuitStatusLksValueCode() == null ||
						p.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.INSTALLED ||
						p.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.POWERED_OFF ||
						p.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.PLANNED, true);			
			}
		}
	} 
}




