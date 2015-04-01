/**
 * 
 */
package com.raritan.tdz.location.dao;

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * Test PIQ deletion calls.
 * @author Andrew Cohen
 */
public class LocationDAOTest extends TestBase {

	private LocationDAO locationDao;
	private LocationFinderDAO locationFinderDAO;
	private LocationUpdateDAO locationUpdateDAO;

	@BeforeMethod
	public void setUp() throws Throwable {	
		super.setUp();
		locationDao = (LocationDAO)ctx.getBean("locationDAO");
		locationFinderDAO = (LocationFinderDAO)ctx.getBean("locationDAO");
		locationUpdateDAO = (LocationUpdateDAO)ctx.getBean("locationDAO");
	}
	
	@Test
	public void testFindLocation() throws Throwable{
		List<DataCenterLocationDetails> locationDetailList = locationFinderDAO.findById(1L);
		assertTrue(locationDetailList.size() == 1);
	}
	
	@Test
	public void testGetLocationByHierarchy() throws Throwable {
	 List<DataCenterLocationDetails> locationDetailList = locationFinderDAO.findLocationsByHierarchy(SystemLookup.DcLocation.ROOM);
	 assertTrue(locationDetailList.size() > 0);
	}
	
	@Test
	public void testUpdateLocationExceptCurrent() throws Throwable {
		//Find the current default site
		List<DataCenterLocationDetails> locationDetailListOrig = locationFinderDAO.findDefaultLocation();
		assertTrue(locationDetailListOrig.size() == 1);
		
		int resultId = locationUpdateDAO.updateDefaultSiteExcludeCurrent(false,locationDetailListOrig.get(0).getDataCenterLocationId());
		assertTrue(resultId > 0);
		
		List<DataCenterLocationDetails> locationDetailListNew = locationFinderDAO.findDefaultLocation();
		assertTrue(locationDetailListNew.size() == 1);
	}
}
