package com.raritan.tdz.item.home;


import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.tests.TestBase;

/**
 * 
 */

/**
 * @author bozana
 *
 */
public class GetAllItemsTest extends TestBase {

	ItemHome itemHome;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemHome = (ItemHome)ctx.getBean("itemHome");
	}


	/**
	 * Test method for {@link com.raritan.tdz.item.home.ItemHomeImpl#getItemDetails(java.lang.Long)}.
	 * @throws Throwable 
	 */
	@Test
	public void testGetItemDetails() throws Throwable {
		
		Map<String, UiComponentDTO> componentDTOs = itemHome.getItemDetails(new Long(10), null /*FIXME: send user info */);
		System.out.println("#### size of DTO = " + componentDTOs.size());
		assertTrue(componentDTOs.size() > 0);
		
		for(UiComponentDTO dto:componentDTOs.values()){
			String val = null;
			Object dtoValue = dto.getUiValueIdField().getValue();
			Object fieldRef = dto.getUiValueIdField().getRemoteRef();
			if (dtoValue != null) val = dtoValue.toString();

			System.out.println(fieldRef + "\t\t\t" + dto.getUiId() + " = " + val);
			
			if( dto.getUiId().equals("cmbModel")){
				assertTrue(val.equals("42RU-TeraFrame Cabinet 42D"));
				System.out.println("model name is good!");
			}
			if( dto.getUiId().equals("tiClass")){
				assertTrue(val.equals("Cabinet"));
				System.out.println("class is good");
			}
		}
	}	
}


