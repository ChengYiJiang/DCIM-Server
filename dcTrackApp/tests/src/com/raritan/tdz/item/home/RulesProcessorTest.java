/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.tests.TestBase;

/**
 * @author prasanna
 *
 */
public class RulesProcessorTest extends TestBase {
	
	private RulesProcessor processor;
	private RemoteRef remoteReference;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		processor = (RulesProcessor) ctx.getBean("itemRulesProcessor");
		remoteReference = (RemoteRef) ctx.getBean("remoteRefItemScreen");
	}

	/**
	 * Test method for {@link com.raritan.tdz.rulesengine.RulesProcessorBase#getRemoteRef(java.lang.String)}.
	 */
	@Test
	public void testGetRemoteRef() {
		String uiId = "tiName";
		String remoteRef = processor.getRemoteRef(uiId);
		
		AssertJUnit.assertEquals(remoteRef, "CONSTANT_NAME_FOR_ITEM");
	}

	@Test
	public void testGetItemNameRemoteRef() {
		String uiId = "tiName";
		String remoteRef = processor.getRemoteRef(uiId);
		
		AssertJUnit.assertEquals(remoteRef, "CONSTANT_NAME_FOR_ITEM");
	
		String entity = remoteReference.getRemoteType(remoteRef);
		System.out.println("### entity = " + entity);
		AssertJUnit.assertEquals(entity, "com.raritan.tdz.domain.Item");
		
		RemoteRefConstantProperty prop = RemoteRef.RemoteRefConstantProperty.FOR_VALUE;
		String property = remoteReference.getRemoteAlias(remoteRef, prop);
		System.out.println("### property = " + property);
		AssertJUnit.assertEquals(property, "itemName");
	}
	
	@Test
	public void testGetUiId() throws ClassNotFoundException {
		List<String> uiIds = processor.getUiId("com.raritan.tdz.domain.Item", "itemName",true);
		AssertJUnit.assertTrue(uiIds.size() > 0);
		AssertJUnit.assertNotNull(uiIds.get(0));
	}
}
