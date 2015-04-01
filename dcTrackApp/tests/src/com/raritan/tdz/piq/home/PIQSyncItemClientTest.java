/**
 * 
 */
package com.raritan.tdz.piq.home;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.DataCenterLocationHome;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.jobs.PDUJobHandler;
import com.raritan.tdz.tests.TestBase;

/**
 * @author prasanna
 *
 */
public class PIQSyncItemClientTest extends TestBase {

	/**
	 * Test method for {@link com.raritan.tdz.piq.home.PIQSyncDeviceClientImpl#addRPDU(com.raritan.tdz.domain.Item, DataPort, String, PDUJobHandler)}.
	 */
	//@Test
	@Test
	public final void testAddPDU() throws Throwable {
		PIQSyncPDUClient client = (PIQSyncPDUClient)ctx.getBean("piqSyncPDUClient");
		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");

		DataCenterLocationHome dcHome = (DataCenterLocationHome)ctx.getBean("dataCenterLocationHome");
		List<DataCenterLocationDetails> locations = null;
		try {
			locations = dcHome.viewAll();
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<Long> statusValueCodes = new ArrayList<Long>();
		statusValueCodes.add(SystemLookup.ItemStatus.PLANNED);
		statusValueCodes.add(SystemLookup.ItemStatus.INSTALLED);
		
		
		try {
			if (locations != null && locations.size() > 0){
				for (DataCenterLocationDetails location: locations){
					Collection<Item> items = itemHome.getItemsForSite(location.getDataCenterLocationId(), SystemLookup.Class.RACK_PDU, statusValueCodes);
					for (Item item: items){
						if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU){
							List<String> ips = itemHome.getDataPortNetInfo(item.getItemId());
							String ipAddress = null;
							if ( ips.size() > 0)
								ipAddress = ips.get(0);
							List<DataPort> dataPorts = getDataPort(item);
							DataPort dataPort = null;
							if (dataPorts.size() > 0)
								dataPort = dataPorts.get(0);
							String job_id = client.addRPDU(item, dataPort, ipAddress);
							//assertNotNull(job_id);
							System.out.println("job id: " + job_id);
						}
					}
				}
			}
		} catch (RemoteDataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<DataPort> getDataPort(Item item) {
		List<DataPort> dataPorts = null;
		
		Criteria criteria = session.createCriteria(DataPort.class).add(Restrictions.eq("item", item));
		dataPorts = criteria.list();
		return dataPorts;
	}

	/**
	 * Test method for {@link com.raritan.tdz.piq.home.PIQSyncDeviceClientImpl#addDevice(com.raritan.tdz.domain.Item, String, Integer, boolean)}.
	 */
	//@Test
	@Test
	public final void testAddDevice() {
		
//		PIQSyncRackClient rackClient = (PIQSyncRackClient)ctx.getBean("piqSyncRackClient");
//		PIQSyncItemClient itemClient = (PIQSyncItemClient)ctx.getBean("piqSyncItemClient");
//		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");
//
//		DataCenterLocationHome dcHome = (DataCenterLocationHome)ctx.getBean("dataCenterLocationHome");
//		List<DataCenterLocationDetails> locations = null;
//		try {
//			locations = dcHome.viewAll();
//		} catch (DataAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		List<Long> statusValueCodes = new ArrayList<Long>();
//		statusValueCodes.add(SystemLookup.ItemStatus.PLANNED);
//		
//		
//		try {
//			if (locations != null && locations.size() > 0){
//				for (DataCenterLocationDetails location: locations){
//					Collection<Item> items = itemHome.getItemsForSite(location.getDataCenterLocationId(), SystemLookup.Class.DEVICE, statusValueCodes);
//					for (Item item: items){
//						if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE){
//							String ip = null;
//							List<String> ips = getDataPortNetInfo(item.getItemId());
//							if ( ips.size() > 0)
//								ip = ips.get(0);
//							String piq_id = itemClient.addITItem(item, ip, null);
//							assertNotNull(piq_id);
//						}
//					}
//				}
//			}
//		} catch (RemoteDataAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DataAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Assert.fail("Not working on PIQ");
	}

	/**
	 * Test method for {@link com.raritan.tdz.piq.home.PIQSyncDeviceClientImpl#isDeviceInSync(java.lang.String)}.
	 */
	//@Test
	@Test
	public final void testIsDeviceInSync() {
		Assert.fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.raritan.tdz.piq.home.PIQSyncDeviceClientImpl#addRack(com.raritan.tdz.domain.Item, boolean)}.
	 */
	//@Test
	@Test
	public final void testAddRack() {
		
		PIQSyncRackClient client = (PIQSyncRackClient)ctx.getBean("piqSyncRackClient");
		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");

		DataCenterLocationHome dcHome = (DataCenterLocationHome)ctx.getBean("dataCenterLocationHome");
		List<DataCenterLocationDetails> locations = null;
		try {
			locations = dcHome.viewAll();
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<Long> statusValueCodes = new ArrayList<Long>();
		statusValueCodes.add(SystemLookup.ItemStatus.PLANNED);
		
		
		try {
			if (locations != null && locations.size() > 0){
				for (DataCenterLocationDetails location: locations){
					Collection<Item> items = itemHome.getItemsForSite(location.getDataCenterLocationId(), SystemLookup.Class.CABINET, statusValueCodes);
					for (Item item: items){
						if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.CABINET){
							String piq_id = client.addRack(item, false);
							assertNotNull(piq_id);
							item.setPiqId(new Integer(piq_id));
							assertTrue("Did not receive PIQ_ID for location " + location.getDcName(), (piq_id != null && !piq_id.isEmpty() ));
						}
					}
				}
			}
		} catch (RemoteDataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	//@Test
	@Test
	public final void testUpdateRack(){
		
		PIQSyncRackClient client = (PIQSyncRackClient)ctx.getBean("piqSyncRackClient");
		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");

		DataCenterLocationHome dcHome = (DataCenterLocationHome)ctx.getBean("dataCenterLocationHome");
		List<DataCenterLocationDetails> locations = null;
		try {
			locations = dcHome.viewAll();
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<Long> statusValueCodes = new ArrayList<Long>();
		statusValueCodes.add(SystemLookup.ItemStatus.PLANNED);
		
		
		try {
			if (locations != null && locations.size() > 0){
				for (DataCenterLocationDetails location: locations){
					Collection<Item> items = itemHome.getItemsForSite(location.getDataCenterLocationId(), SystemLookup.Class.CABINET, statusValueCodes);
					for (Item item: items){
						if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.CABINET){
							String piq_id = client.updateRack(item, false);
							assertNotNull(piq_id);
							assertTrue("Did not receive PIQ_ID for location " + location.getDcName(), (piq_id != null && !piq_id.isEmpty() ));
						}
					}
				}
			}
		} catch (RemoteDataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
    private List<String> getDataPortNetInfo(Long itemId){
       List<String> netinfo = null;
        
     try{
        Session session = this.sf.getCurrentSession();    
        
        //These tables need to be move to dct_????
            String SQL_QUERY = " select tblipaddresses.ipaddress " + 
            "from tblipaddresses inner join tblipteaming on tblipaddresses.id = tblipteaming.ipaddressid " +
            "inner join tblnetworks on tblipaddresses.networkid = tblnetworks.id " +
            "inner join dct_ports_data on tblipteaming.portid = dct_ports_data.port_data_id " +
            "where dct_ports_data.item_id = " + itemId.toString() + " " +              
            "order by tblipteaming.portid ";
                          
               org.hibernate.SQLQuery query = session.createSQLQuery(SQL_QUERY);
               netinfo = query.list();
                                                        
        } catch (Exception e) {
               e.printStackTrace();
        }      
     
     return netinfo;
    }      
    
    private Integer getPowerRating(Long itemId){
    	Integer result = 0;
    	try{
    		Session session = this.sf.getCurrentSession();
    		Criteria criteria = session.createCriteria(PowerPort.class);
    		
    		criteria.createAlias("item","item").add(Restrictions.eq("item.itemId", itemId));
    		
    		List list = criteria.list();
    		
    		
    		
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	return result;
    }


}
