package com.raritan.tdz.page;


import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.FilterDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.home.PaginatedHome;
import com.raritan.tdz.session.FlexUserSessionContext;

import flex.messaging.util.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PaginatedListTest extends TestBase{
		private PaginatedHome paginatedHome;
		private ListCriteriaDTO initialListCriteria;
		final static private String pageType = "itemList";
		
		@Override
	    @BeforeMethod
	    public void setUp() throws Throwable {
	    	super.setUp();
	    	//NOTE: Override default mock UserId since PaginatedList is using it and it assumes
	    	//that value is a number (though in String type) 
	    	FlexUserSessionContext.setMockUserId( "1" );
	    	paginatedHome = (PaginatedHome)ctx.getBean("itemListPaginatedHome");
	    	createListCriteria();
	    	System.out.println("after having bean");
	    	
	    }

	    private ListCriteriaDTO createLaterListCriteria(ListCriteriaDTO previousListCriteria) {
	    	ListCriteriaDTO laterListCriteria = new ListCriteriaDTO();
	    	laterListCriteria.setFirstQuery(false);
	    	laterListCriteria.setFitType(0);
	    	laterListCriteria.setMaxLinesPerPage(12);;
	    	laterListCriteria.setPageNumber(0);
	    	laterListCriteria.setUserAddColumn(false);
	    	laterListCriteria.setColumnCriteria(previousListCriteria.getColumnCriteria());
	    	laterListCriteria.setColumns(previousListCriteria.getColumns());
	    	laterListCriteria.setCurrentUtcTimeString("2/7/2014 9:20:39 -0500");
			return laterListCriteria;
		}

	    private ListCriteriaDTO createReqNumberFilterinCriteria(ListCriteriaDTO previousListCriteria) {
	    	ListCriteriaDTO filterCriteria = new ListCriteriaDTO();
	    	filterCriteria.setFirstQuery(false);
	    	filterCriteria.setFitType(0);
	    	filterCriteria.setMaxLinesPerPage(12);;
	    	filterCriteria.setPageNumber(1);
	    	filterCriteria.setUserAddColumn(false);
	    	List<ColumnCriteriaDTO> columnCriteria = new ArrayList<ColumnCriteriaDTO>();
	    	ColumnCriteriaDTO c1 = new 	ColumnCriteriaDTO();
	    	c1.setFilter(null);
	    	c1.setName("Name");
	    	c1.setSortDescending(false);
	    	c1.setToSort(true);
	    	c1.visible = false;
	    	columnCriteria.add(c1);
	    	ColumnCriteriaDTO c2 = new 	ColumnCriteriaDTO();
	    	FilterDTO filter = new FilterDTO();
	    	filter.setEqual("00");
	    	filter.setGreaterThan(null);
	    	filter.setGroupType(0);
	    	filter.setLessThan(null);
	    	filter.setLookupCodes(null);
	    	c2.setFilter(filter);
	    	c2.setName("Request Number");
	    	c2.setSortDescending(false);
	    	c2.setToSort(true);
	    	c2.visible = false;
	    	columnCriteria.add(c2);
	    	
	    	filterCriteria.setColumnCriteria(columnCriteria);
	   
	    	filterCriteria.setColumns(previousListCriteria.getColumns());
	    	filterCriteria.setCurrentUtcTimeString("2/7/2014 9:20:39 -0500");
			return filterCriteria;
		}
	    
	    
	    private ListCriteriaDTO createReqStageFilterinCriteria(ListCriteriaDTO previousListCriteria) {
	    	ListCriteriaDTO filterCriteria = new ListCriteriaDTO();
	    	filterCriteria.setFirstQuery(false);
	    	filterCriteria.setFitType(0);
	    	filterCriteria.setMaxLinesPerPage(12);;
	    	filterCriteria.setPageNumber(1);
	    	filterCriteria.setUserAddColumn(false);
	    	List<ColumnCriteriaDTO> columnCriteria = new ArrayList<ColumnCriteriaDTO>();
	    	ColumnCriteriaDTO c1 = new 	ColumnCriteriaDTO();
	    	c1.setFilter(null);
	    	c1.setName("Name");
	    	c1.setSortDescending(false);
	    	c1.setToSort(true);
	    	c1.visible = false;
	    	columnCriteria.add(c1);
	    	ColumnCriteriaDTO c2 = new 	ColumnCriteriaDTO();
	    	FilterDTO filter1 = new FilterDTO();
	    	filter1.setEqual("00");
	    	filter1.setGreaterThan(null);
	    	filter1.setGroupType(0);
	    	filter1.setLessThan(null);
	    	filter1.setIsLookup(false);
	    	filter1.setLookupCodes(null);
	    	c2.setFilter(filter1);
	    	c2.setName("Request Number");
	    	c2.setSortDescending(false);
	    	c2.setToSort(true);
	    	c2.visible = false;
	    	columnCriteria.add(c2);
	    	
	    	ColumnCriteriaDTO c3 = new 	ColumnCriteriaDTO();
	    	FilterDTO filter2 = new FilterDTO();
	    	filter2.setEqual("");
	    	filter2.setGreaterThan(null);
	    	filter2.setGroupType(0);
	    	filter2.setLessThan(null);
	    	filter2.setIsLookup(true);
	    	filter2.setLookupCodes("Request Issued");
	    	c3.setFilter(filter2);
	    	c3.setName("Request Stage");
	    	c3.setSortDescending(false);
	    	c3.setToSort(false);
	    	c3.visible = false;
	    	columnCriteria.add(c3);
	    	
	    	filterCriteria.setColumnCriteria(columnCriteria);
	   
	    	filterCriteria.setColumns(previousListCriteria.getColumns());
	    	filterCriteria.setCurrentUtcTimeString("2/7/2014 9:22:39 -0500");
			return filterCriteria;
		}
		private void createListCriteria() {
			initialListCriteria = new ListCriteriaDTO();
			initialListCriteria.setFirstQuery(true);
			initialListCriteria.setFitType(0);
			initialListCriteria.setMaxLinesPerPage(0);
			initialListCriteria.setPageNumber(0);
			initialListCriteria.setUserAddColumn(false);
			initialListCriteria.setColumnCriteria(new ArrayList<ColumnCriteriaDTO>());
			initialListCriteria.setColumns(new ArrayList<ColumnDTO>());
			initialListCriteria.setCurrentUtcTimeString("2/7/2014 8:20:39 -0500");		
		}

		@AfterMethod
	    public void tearDown() throws Throwable {
	    	super.tearDown();
	    }
	    
	    
	    @Test
	    public void testRequestNumberAndStagePresenceAndPosition() throws ClassNotFoundException, DataAccessException{
	    	System.out.println("==== TEST: testRequestNumberAndStagePresenceAndPosition()");
	    	try {
				ListResultDTO results = paginatedHome.getPageList(initialListCriteria, pageType);
				assertNotNull(results);
				System.out.println("results=" + results.toString());
				List<ColumnDTO> columns = results.getListCriteriaDTO().getColumns();
				assertTrue(columns.size() > 0 );
				Long[] order = new Long[3];
				int i=0;
				for( ColumnDTO dto : columns ){
					
					System.out.println("-- fieldLabel = " + dto.getFieldLabel());
					if( dto.getFieldLabel().equals("Request Number")) order[0] = new Long(i);
					if( dto.getFieldLabel().equals("Request Stage")) order[1] = new Long(i);
					if( dto.getFieldLabel().equals("Created On")) order[2] = new Long(i);
					++i;
				}
				assertTrue(order[0] != 0 );
				assertTrue(order[1] != 0 && order[0] < order[1]);
				assertTrue(order[2] != null && order[1] < order[2]);
				assertTrue( order[0] + 1 == order[1]);
				assertTrue( order[1] + 1 == order[2]);
			} catch (DataAccessException e) {
				e.printStackTrace();
				throw e;
			}
	    }
	    
	    private void checkFields( ListResultDTO results ){
			assertNotNull(results);
			System.out.println("results=" + results.toString());
			List<ColumnDTO> columns = results.getListCriteriaDTO().getColumns();
			assertTrue(columns.size() > 0 );
			Long[] order = new Long[3];
			int i=0;
			for( ColumnDTO dto : columns ){
				
				System.out.println("-- fieldLabel = " + dto.getFieldLabel());
				if( dto.getFieldLabel().equals("Request Number")) order[0] = new Long(i);
				if( dto.getFieldLabel().equals("Request Stage")) order[1] = new Long(i);
				if( dto.getFieldLabel().equals("Created On")) order[2] = new Long(i);
				++i;
			}
			assertTrue(order[0] != 0 );
			assertTrue(order[1] != 0 && order[0] < order[1]);
			assertTrue(order[2] != null && order[1] < order[2]);
			assertTrue( order[0] + 1 == order[1]);
			assertTrue( order[1] + 1 == order[2]);
	    }
	    
	    
	    @Test
	    public void testGetItems() throws ClassNotFoundException, DataAccessException{
	    	System.out.println("==== TEST: testGetItems()" );
	    	try {
	    		ListResultDTO results = paginatedHome.getPageList(initialListCriteria, pageType);
				assertNotNull(results);
				
				results = paginatedHome.getPageList(createLaterListCriteria(results.getListCriteriaDTO()), pageType);
				checkFields( results );
				List<Object[]> values = results.getValues();
				assertNotNull( values );
				assertTrue(values.size() > 0 );
			} catch (DataAccessException e) {
				e.printStackTrace();
				throw e;
			}
	    }
	    
	    @Test
	    public void testRequestNumberFiltering() throws ClassNotFoundException, DataAccessException{
	    	System.out.println("==== TEST: testRequestNumberFiltering()" );
	    	try {
	    		ListResultDTO results = paginatedHome.getPageList(initialListCriteria, pageType);
				assertNotNull(results);
				int requestNumberCol = -1;
				List<ColumnDTO> columns = results.getListCriteriaDTO().getColumns();
				int numCoumns = columns.size();
				for( int c=0; c< numCoumns; c++){
					if( columns.get(c).getFieldLabel().equals("Request Number")) {
						requestNumberCol=c;
						break;
					}
				}
				results = paginatedHome.getPageList(createReqNumberFilterinCriteria(results.getListCriteriaDTO()), pageType);
				checkFields( results );
				List<Object[]> values = results.getValues();
				assertNotNull( values );
				assertTrue(values.size() > 0 );
				
				long numRaws = values.size();
				System.out.println("--- Request Number filtering results: ---");
				for ( int i=0; i< numRaws; i++ ){
					Object[] objs = values.get(i);
					int numCols = objs.length;
					for (int j =0; j < numCols; j++ ){
						System.out.print((objs[j]==null ? "null" : objs[j].toString()) + ", ");
						if( j == requestNumberCol ){
							assertTrue(objs[j] != null );
						}
					}
					System.out.println();
				}

			} catch (DataAccessException e) {
				e.printStackTrace();
				throw e;
			}
	    }
	    
	    @Test
	    public void testRequestStageFiltering() throws ClassNotFoundException, DataAccessException{
	    	System.out.println("==== TEST: testRequestStageFiltering()" );
	    	try {
	    		ListResultDTO results = paginatedHome.getPageList(initialListCriteria, pageType);
				assertNotNull(results);
				int requestStage = -1;
				List<ColumnDTO> columns = results.getListCriteriaDTO().getColumns();
				int numCoumns = columns.size();
				for( int c=0; c< numCoumns; c++){
					if( columns.get(c).getFieldLabel().equals("Request Stage")) {
						requestStage=c;
						break;
					}
				}
				results = paginatedHome.getPageList(createReqStageFilterinCriteria(results.getListCriteriaDTO()), pageType);
				checkFields( results );
				List<Object[]> values = results.getValues();
				assertNotNull( values );
				assertTrue(values.size() > 0 );
				
				long numRaws = values.size();
				System.out.println("--- Request Stage filtering results: ---");
				for ( int i=0; i< numRaws; i++ ){
					Object[] objs = values.get(i);
					int numCols = objs.length;
					for (int j =0; j < numCols; j++ ){
						System.out.print((objs[j]==null ? "null" : objs[j].toString()) + ", ");
						if( j == requestStage ){
							assertTrue(objs[j] != null );
						}
					}
					System.out.println();
				}

			} catch (DataAccessException e) {
				e.printStackTrace();
				throw e;
			}
	    }
	}


