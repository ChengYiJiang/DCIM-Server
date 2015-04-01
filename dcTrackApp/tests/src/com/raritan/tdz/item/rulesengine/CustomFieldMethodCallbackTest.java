package com.raritan.tdz.item.rulesengine;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;
import static org.testng.Assert.assertNotNull;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.field.service.FieldService;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.tests.TestBase;

public class CustomFieldMethodCallbackTest extends TestBase{
	private CustomFieldMethodCallback customFields = null;
    
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		customFields = (CustomFieldMethodCallback)ctx.getBean("customFieldMethodCallback");
		assertNotNull(customFields, "CustomFields object is null");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
  @Test
  public void fillValue() throws Throwable {
	  UiComponent uiViewComponent = new UiComponent();
	  uiViewComponent.setUiValueIdField(new UiValueIdField());
	  try {
		  customFields.fillValue(uiViewComponent, null, 63L /*item_id*/, null, null, null);
	  } catch (Exception e) {
		  e.printStackTrace();
		  fail("fillValue raised exception");
	  }
	  
  }

}
