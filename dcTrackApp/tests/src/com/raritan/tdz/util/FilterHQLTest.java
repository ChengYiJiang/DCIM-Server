/**
 * 
 */
package com.raritan.tdz.util;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.rulesengine.Filter;
import com.raritan.tdz.rulesengine.FilterHQLImpl;

/**
 * @author prasanna
 *
 */
public class FilterHQLTest {
	Filter filter = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		filter = FilterHQLImpl.createFilter();
	}

	
	@Test
	public void testSimplePropertyValueBuilder(){
		filter.eq("itemId", 10L);
		Assert.assertEquals(filter.toSqlString(), " this_.itemId = 10");
	}
	
	@Test
	public void testSimplePropertyValueWithAlias(){
		filter.eq("tickets.itemId", 10L);
		Assert.assertEquals(filter.toSqlString(), " tickets.itemId = 10");
	}
	
	@Test
	public void testWithAndOperator(){
		filter.eq("itemId", 10L);
		Filter andPartFilter = FilterHQLImpl.createFilter();
		andPartFilter.eq("isModified", true);
		
		filter.and(andPartFilter);
		
		Assert.assertEquals(filter.toSqlString(), " this_.itemId = 10 and this_.isModified = true");
	}

	@Test
	public void testWithOrOperator(){
		filter.eq("itemId", 10L);
		Filter orPartFilter = FilterHQLImpl.createFilter();
		orPartFilter.eq("isModified", true);
		
		filter.or(orPartFilter);
		
		Assert.assertEquals(filter.toSqlString(), " this_.itemId = 10 or this_.isModified = true");
	}
	
	@Test
	public void testWithAndOrOperator(){
		filter.eq("itemId", 10L);
		Filter andPartFilter = FilterHQLImpl.createFilter();
		andPartFilter.eq("isModified", true);
		
		filter.and(andPartFilter);
		
		Filter orPartFilter = FilterHQLImpl.createFilter();
		orPartFilter.eq("isModified", true);
		
		filter.or(orPartFilter);
		
		Assert.assertEquals(filter.toSqlString(), " this_.itemId = 10 and this_.isModified = true or this_.isModified = true");
	}
	
	@Test
	public void testWithAndOrParenthesesOperator(){
		filter.eq("itemId", 10L);
		Filter andPartFilter = FilterHQLImpl.createFilter();
		andPartFilter.openParentheses().eq("isModified", true);
		
		filter.and(andPartFilter);
		
		Filter orPartFilter = FilterHQLImpl.createFilter();
		orPartFilter.eq("isModified", false);
		filter.or(orPartFilter).closeParentheses();
		
		Assert.assertEquals(filter.toSqlString(), " this_.itemId = 10 and ( this_.isModified = true or this_.isModified = false )");
	}
	
	@Test
	public void testWithInOperator(){
		List<Long> itemIds = new ArrayList<Long>(){{
			add(10L);
			add(11L);
			add(12L);
		}};
		
		filter.in("itemId", itemIds);
		
		Assert.assertEquals(filter.toSqlString(), " this_.itemId in ( 10, 11, 12 )");
	}

}
