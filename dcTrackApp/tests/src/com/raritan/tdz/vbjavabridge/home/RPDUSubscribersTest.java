package com.raritan.tdz.vbjavabridge.home;

import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.math.BigInteger;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemSNMP;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.PortHome;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.subscribers.PDUDataPortSubscriber;
import com.raritan.tdz.vbjavabridge.subscribers.RPDUItemSubscriber;

public class RPDUSubscribersTest extends TestBase {

	private RPDUItemSubscriber rpduSubscriber;
	private PIQSyncPDUClient pduClient;
	private PDUDataPortSubscriber pduDataPortSubscriber;
	private LNHome lnHome;
	
	private final String pduName = "1A-RPDU-R";
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		rpduSubscriber = (RPDUItemSubscriber)ctx.getBean("rPDUItemSubscriber");
		pduClient = (PIQSyncPDUClient)ctx.getBean("piqSyncPDUClient");
		pduDataPortSubscriber = (PDUDataPortSubscriber)ctx.getBean("pduDataPortSubscriber");
		lnHome = (LNHome)ctx.getBean("listenNotifyHome");
		
		//This is to protect from lnEvent loop adding/deleting/modifying objects on 
		//PowerIQ during our testing of the subscribers:-)
		lnHome.setSuspend(true);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		//Make sure that you dont have any events pending
		//that way when you re-run this will not be picked up
		//by the lnevent loop to process them.
		deleteLnEvents();
		
		lnHome.setSuspend(false);
		super.tearDown();
	}

	private void deleteLnEvents() {
		if (sf != null){
			Session session = sf.getCurrentSession();
			
			Query query = session.createQuery("delete from LNEvent");
			
			query.executeUpdate();
		}
		
	}

	@Test
	public void addProxyIndex() throws RemoteDataAccessException {
		//fail("Not yet implemented");
		
		
		//First update the proxy index of a rack PDU
		if (sf != null){
			
			
			Session session = sf.getCurrentSession();
			Item item = getItem(session, pduName);
			
			//Let us first delete the item on Power IQ if it exists
			if (item.getPiqId() != null){
				if (pduClient.isRPDUInSync(item.getPiqId().toString())){
					pduClient.deletePDU(item.getPiqId().toString());
				}
			}
			
			String ip = getDataPortNetInfo(item);
			DataPort dPort = getDataPort(ip, session);
			
			assertNotNull(ip);
			AssertJUnit.assertNotNull(dPort);
			AssertJUnit.assertFalse(ip.isEmpty());
			
			//Change it back and call again
			item.setGroupingNumber("10");
			pduDataPortSubscriber.handleInsertEvent(session, item, dPort);
			AssertJUnit.assertTrue(pduClient.isProxyIndexInSync(item));
		}
	}


	
	@Test
	public void updateProxyIndex() throws RemoteDataAccessException {
		//fail("Not yet implemented");
		
		
		//First update the proxy index of a rack PDU
		if (sf != null){
			
			
			Session session = sf.getCurrentSession();
			Item item = getItem(session, pduName);
		
			String groupingNumber = item.getGroupingNumber();
			
			//Call the subscriber's update method
			item.setGroupingNumber("8");
			rpduSubscriber.handleUpdateEvent(session, item, new LNEvent());
			AssertJUnit.assertTrue(pduClient.isProxyIndexInSync(item));
			
			//Change it back and call again
			item.setGroupingNumber(groupingNumber);
			rpduSubscriber.handleUpdateEvent(session, item, new LNEvent());
			AssertJUnit.assertTrue(pduClient.isProxyIndexInSync(item));
		}
	}
	
	@Test
	public void removeProxyIndex() throws RemoteDataAccessException {
//		fail("Not yet implemented");
		
		
		//First update the proxy index of a rack PDU
		if (sf != null){
			
			
			Session session = sf.getCurrentSession();
			Item item = getItem(session, pduName);
		
			String groupingNumber = item.getGroupingNumber();
			
			//Call the subscriber's update method
			item.setGroupingNumber(null);
			rpduSubscriber.handleUpdateEvent(session, item, new LNEvent());
			AssertJUnit.assertFalse(pduClient.isProxyIndexInSync(item));
		}
	}
	
	
	@Test
	public void updatePXCredentials() throws RemoteDataAccessException {
		if (sf != null){
			Session session = sf.getCurrentSession();
			Item item = getItem(session, pduName);
			if (item.getItemSnmp() == null){
				item.setItemSnmp(new ItemSNMP());
			}
			item.getItemSnmp().setPxUserName("tom");
			item.getItemSnmp().setPxPassword("jerry");
			
			rpduSubscriber.handleUpdateEvent(session, item, new LNEvent());
			
			//Unfortunately, there is no way to verify if this is set on power IQ
			//since the get operation never gives us the ipmi_username:-(
			//We have to manually check this on the PowerIQ user interface!
		}
	}
	
	@Test
	public void updateSNMPv3Credentials() throws RemoteDataAccessException {
		if (sf != null){
			Session session = sf.getCurrentSession();
			Item item = getItem(session, pduName);
			
			if (item.getItemSnmp() == null){
				item.setItemSnmp(new ItemSNMP());
			}
			
			item.getItemSnmp().setSnmp3Enabled(true);
			item.getItemSnmp().setSnmp3AuthLevel("authPriv");
			item.getItemSnmp().setSnmp3AuthPasskey("raritan123");
			item.getItemSnmp().setSnmp3AuthProtocol("MD5");
			item.getItemSnmp().setSnmp3PrivPasskey("Raritan123$");
			item.getItemSnmp().setSnmp3PrivProtocol("AES");
			item.getItemSnmp().setSnmp3User("admin");
			
			rpduSubscriber.handleUpdateEvent(session, item, new LNEvent());
			
			//Unfortunately, there is no way to verify if this is set on power IQ
			//since the get operation never gives us the ipmi_username:-(
			//We have to manually check this on the PowerIQ user interface!
		}
	}
	
	@Test
	public void updateSNMPv3CredentialsWithWrongAuthPasskey(){
		if (sf != null){
			Session session = sf.getCurrentSession();
			Item item = getItem(session, pduName);
			
			if (item.getItemSnmp() == null){
				item.setItemSnmp(new ItemSNMP());
			}
			
			item.getItemSnmp().setSnmp3Enabled(true);
			item.getItemSnmp().setSnmp3AuthLevel("authPriv");
			
			//PowerIQ does not accept authpasskey to be lower than 8 characters. This will test if 
			//we fail properly.
			item.getItemSnmp().setSnmp3AuthPasskey("rarita");
			item.getItemSnmp().setSnmp3AuthProtocol("MD5");
			item.getItemSnmp().setSnmp3PrivPasskey("Raritan123$");
			item.getItemSnmp().setSnmp3PrivProtocol("AES");
			item.getItemSnmp().setSnmp3User("admin");
			
			try {
			
				rpduSubscriber.handleUpdateEvent(session, item, new LNEvent());
			} catch (RemoteDataAccessException e){
				//Catch the exception since we know it should fail.
			}
			
			//Unfortunately, there is no way to verify if this is set on power IQ
			//since the get operation never gives us the ipmi_username:-(
			//We have to manually check this on the PowerIQ user interface!
		}
	}
	
	private String getDataPortNetInfo(Item item) {
		//Get the IPAddress
		String ip = null;
		List<String> ips = itemHome.getDataPortNetInfo(item.getItemId());
		if (ips != null && ips.size() > 0)
		{
			for (String ipaddress: ips){
				if (ipaddress != null){
					ip = ips.get(0);
					break;
				}
			}
		}
		return ip;
	}
	
	private DataPort getDataPort(String ip, Session session){
		DataPort dPort = null;
		
		if (ip != null && !ip.isEmpty() && session != null){
			String queryStr = "select dct_ports_data.port_data_id from dct_ports_data inner join tblipteaming on tblipteaming.portid = dct_ports_data.port_data_id " +
					 " inner join tblipaddresses on tblipaddresses.id = tblipteaming.ipaddressid "
					 + " where tblipaddresses.ipaddress = '" + ip + "' order by tblipteaming.ipaddressid";
			Query query = session.createSQLQuery(queryStr);
			
			List dPorts = query.list();
			BigInteger portId = (BigInteger)dPorts.get(0);
			dPort = (DataPort) session.load(DataPort.class, portId.longValue());
		}
		
		return dPort;
	}
	
	
	private Item getItem(Session session, String itemName) {
		Item item = null;
		//TODO: This is now hard coded to a specific rack pdu and must have an associated IPAddress. This rackpdu is found
		//      in dcTrack demo database.
		Criteria criteria = session.createCriteria(Item.class).add(Restrictions.eq("itemName", itemName));
		List list = criteria.list();
		item = (Item)list.get(0);
		return item;
	}

}
