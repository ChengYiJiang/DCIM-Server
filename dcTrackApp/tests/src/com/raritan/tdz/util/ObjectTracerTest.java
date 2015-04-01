/**
 * 
 */
package com.raritan.tdz.util;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.lang.reflect.Field;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;

/**
 * @author prasanna
 *
 */
public class ObjectTracerTest {
	ObjectTracer oTracer;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		oTracer = new ObjectTracer();
		oTracer.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.raritan.tdz.util.ObjectTracer#traceObject(java.lang.Class, java.lang.String)}.
	 */

	@Test
	public void testTraceObject() {
		try {
			Field[] dtoFields = ItemSearchResultDTOImpl.class.getDeclaredFields();
			for (int i = 0; i < dtoFields.length; i++){
				oTracer.traceObject(MeItem.class, dtoFields[i].getName());
				oTracer.getObjectTrace();
				System.out.println("ObjectTrace result for " + dtoFields[i].getName() + ":\t\t" + oTracer.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	

	@Test
	public void testSerialNumber() {
		try {
			String property="serialNumber";
			ObjectTracer oTracer = new ObjectTracer();
			oTracer.traceObject(Item.class, property);
			assertTrue(oTracer.toString().contains(property));
			System.out.println("======= Object trace for " + property + "= " + oTracer.toString());

			property="itemName";
			oTracer.traceObject(Item.class, property);
			assertTrue(oTracer.toString().contains(property));
			System.out.println("======= Object trace for " + property + "= " + oTracer.toString());

			property="raritanAssetTag";
			oTracer.traceObject(Item.class, property);
			assertTrue(oTracer.toString().contains(property));
			System.out.println("======= Object trace for " + property + "= " + oTracer.toString());

			property="email";
			oTracer.traceObject(Item.class, property);
			oTracer.addHandler("itemAdminUser", new UserIdTracerHandler());
			assertTrue(oTracer.toString().contains(property));
			System.out.println("======= Object trace for " + property + "= " + oTracer.toString());

			property="userId";
			//ObjectTracer oTracer6 = new ObjectTracer();
			oTracer.traceObject(Item.class, property);
			oTracer.addHandler("itemAdminUser", new UserIdTracerHandler());
			assertTrue(oTracer.toString().contains(property));
			System.out.println("======= Object trace for " + property + "= " + oTracer.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}
}
