/**
 * 
 */
package com.raritan.tdz.item.home;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchDtoToDomainTest extends TestBase{
	
	private DtoToDomainObjectTrace itemSearchResultDTOToItem;
	
	@BeforeMethod
	public void setUp() throws Throwable{
		super.setUp();
		itemSearchResultDTOToItem = (DtoToDomainObjectTrace)ctx.getBean("itemSearchResultDTOToItem");
	}
	

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getAllTrace()}.
	 */
	@Test
	public void testGetAllTrace() {
		AssertJUnit.assertTrue(itemSearchResultDTOToItem.getAllTrace().size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getAllTrace(int)}.
	 */
	@Test
	public void testGetAllTraceInt() {
		AssertJUnit.assertTrue(itemSearchResultDTOToItem.getAllTrace(1).size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getAllTrace(int, int)}.
	 */
	@Test
	public void testGetAllTraceIntInt() {
		AssertJUnit.assertFalse(itemSearchResultDTOToItem.getAllTrace(0,10).size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getAllLeafNodes()}.
	 */
	@Test
	public void testGetAllLeafNodes() {
		AssertJUnit.assertTrue(itemSearchResultDTOToItem.getAllLeafNodes().size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getAllFirstNodes()}.
	 */
	@Test
	public void testGetAllFirstNodes() {
		AssertJUnit.assertTrue(itemSearchResultDTOToItem.getAllFirstNodes().size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getAllTraceExcludeLeafNode()}.
	 */
	@Test
	public void testGetAllTraceExcludeLeafNode() {
		AssertJUnit.assertTrue(itemSearchResultDTOToItem.getAllTraceExcludeLeafNode().size() > 0);
	}

	/**
	 * Test method for {@link com.raritan.tdz.item.home.search.ItemSearchResultDTOToDomainTrace#getLeafToTraceMap()}.
	 */
	@Test
	public void testGetLeafToTraceMap() {
		Map<String, String> leafToTraceMap = itemSearchResultDTOToItem.getLeafToTraceMap();
		AssertJUnit.assertTrue(leafToTraceMap.size() > 0);
		
		
		
		Criteria criteria = session.createCriteria(Item.class);
		
		for (String alias: itemSearchResultDTOToItem.getAllTraceExcludeLeafNode()){
			criteria.createAlias(alias, alias.replace(".", "_"), Criteria.LEFT_JOIN);
			System.out.println(alias);
		}
		
		ProjectionList proList = Projections.projectionList();
		
		for (Field dtoObject: ItemSearchResultDTOImpl.class.getDeclaredFields()){
			String dtoObjectName = dtoObject.getName();
			String alias = itemSearchResultDTOToItem.getTraceExcludeLeafNode(dtoObjectName);
			String property = itemSearchResultDTOToItem.getLastNode(dtoObjectName);
			String lastNode = itemSearchResultDTOToItem.getLastNode(dtoObjectName);
			
			if (alias != null && !alias.equals(property)){
				property = alias.replace(".", "_") + "." + lastNode;
			}
			
			String proAlias = dtoObjectName;
			
			if (alias != null && alias.equals(dtoObjectName)){
				proAlias = "_" + dtoObjectName;
			} 
			
			if (property != null)
				proList.add(Projections.alias(Projections.property(property),proAlias));
		}
		
//		ProjectionList proList = Projections.projectionList();
//		for (String lastNode: itemSearchResultDTOToItem.getAllLeafNodes()){
//			String alias = leafToTraceMap.get(lastNode).replace(".","_");
//			String property = alias.equals(lastNode) ? lastNode : alias + "." + lastNode;
//			String proAlias = lastNode;
//			if (alias.equals(lastNode)){
//				proAlias = "_" + lastNode;
//			} else if (lastNode.equals("lkuValue")){
//				proAlias = alias.contains("_") ? alias.split("_")[1].replace("Lookup","LkuValue") : alias.replace("Lookup", "LkuValue");
//			} else if (lastNode.equals("lksData")){
//				proAlias = alias.contains("_") ? alias.split("_")[1].replace("Lookup","LksValue") : alias.replace("Lookup", "LksValue");
//			}
//			
//			proList.add(Projections.alias(Projections.property(property),proAlias));
//			System.out.println("Last Node " + lastNode + " alias " + leafToTraceMap.get(lastNode));
//		}
//		
		criteria.setProjection(proList);
		
		String searchKey = itemSearchResultDTOToItem.getTraceExcludeLeafNode("mfrName").replace(".", "_") + "." + "mfrName";
		
		criteria.add(Restrictions.ilike(searchKey,"Dell",MatchMode.ANYWHERE));
		criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List list = criteria.list();
		
		AssertJUnit.assertTrue(list.size() > 0);
		
		
	}

}
