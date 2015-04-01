/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.InstalledPDUWithoutIPException;
import com.raritan.tdz.piq.home.PIQProbeMapper;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
public class IPAddressesSubscriberImpl extends LNSubscriberBase {

	private PIQSyncDeviceClient piqSyncItemClient = null;
	private PIQSyncPDUClient piqSyncPDUClient = null;
	private ItemHome itemHome = null;
	private PIQProbeMapper piqProbeMapper = null;
	private PortHome portHome = null;

	private Logger log = Logger.getLogger(this.getClass());
	
	private String tableName="\"tblipaddresses\"";

	public IPAddressesSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome, 
			PIQSyncDeviceClient piqSyncItemClient,
			PIQSyncPDUClient piqSyncPDUClient,
			ItemHome itemHome,
			PortHome portHome,
			PIQProbeMapper piqProbeMapper) {
		super(sessionFactory, lnHome);
		this.piqSyncItemClient = piqSyncItemClient;
		this.piqSyncPDUClient = piqSyncPDUClient;
		this.itemHome = itemHome;
		this.piqProbeMapper = piqProbeMapper;
		this.portHome = portHome;
	}


	/**
	 * @return the piqSycItemClient
	 */
	public final PIQSyncDeviceClient getPiqSycItemClient() {
		return piqSyncItemClient;
	}


	/**
	 * @param piqSycItemClient the piqSycItemClient to set
	 */
	public final void setPiqSycItemClient(PIQSyncDeviceClient piqSycItemClient) {
		this.piqSyncItemClient = piqSycItemClient;
	}


	/**
	 * @return the itemHome
	 */
	public final ItemHome getItemHome() {
		return itemHome;
	}


	/**
	 * @param itemHome the itemHome to set
	 */
	public final void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber#subscribe()
	 */
	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),tableName,this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),tableName,this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),tableName,this);
			session.close();
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processInsertEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		//We do not need to process the insert event since this is taken 
		//care by the data ports subscriber

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		//We do not need to process the delete event since this is taken 
		//care by the data ports subscriber

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processUpdateEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		
		//Get the IPAddress
		String ip = getIPAddress(session, event);
		
		// Get the DataPort given the IPAddress
		DataPort dPort = getDataPort(session, event);
		
		//Now process the update of either the device or RPDU. 
		//RackPDUs have to be specially handled since PIQ needs dcTrack to first
		//delete the PDU and then re-add with new IP
		if (dPort != null && ip != null)
		{
			Item item = dPort.getItem();
			if (item != null){
				LksData itemClass = item.getClassLookup();
				if (itemClass != null && itemClass.getLkpValueCode() == SystemLookup.Class.RACK_PDU){
					updateRPDU(ip, dPort);
				}
				else if (itemClass != null && itemClass.getLkpValueCode() == SystemLookup.Class.PROBE) {
					updateProbe(item, ip, session);
				}
				else if (itemClass != null && (itemClass.getLkpValueCode() == SystemLookup.Class.DEVICE)
						|| (itemClass.getLkpValueCode() == SystemLookup.Class.NETWORK))
				{
					updateDevice(item, ip, session);
				}
			}
		}
	}

	/**
	 * @param item
	 * @param ip
	 * @param session
	 * @throws RemoteDataAccessException
	 */
	private void updateDevice(Item item, String ip, Session session)
			throws RemoteDataAccessException {
		piqSyncItemClient.updateDevice(item, ip, getPowerRating(item.getItemId(),session), false);
	}
	
	/**
	 * Updates the IP Address for probe's associated dummy rack PDU.
	 * @param probe
	 * @param ip
	 * @param session
	 * @throws RemoteDataAccessException
	 */
	private void updateProbe(Item probe, String ip, Session session) throws RemoteDataAccessException {
		Item probeRPDU = piqProbeMapper.getLookup().getDummyRackPDUForProbeItem( probe.getItemId() );
		
		if (probeRPDU != null) {
			Collection<DataPort> dataPorts = null;
			try {
				dataPorts = portHome.viewDataPortsByCriteria(probeRPDU.getItemId(), SystemLookup.Class.RACK_PDU, null, null, null, -1, true);
				if (dataPorts != null && dataPorts.size() > 0) {
					
					// Handle situation where probe may have been unmapped - lookup the PDU by IP address in PIQ
					if (probeRPDU.getPiqId() == null) {
						Integer piqId = piqSyncPDUClient.lookupByIPAddress( ip );
						if (piqId != null) {
							probeRPDU.setPiqId( piqId );
							sessionFactory.getCurrentSession().merge( probeRPDU );
						}
					}
					
					updateRPDU(ip, dataPorts.iterator().next());
				}
			}
			catch (DataAccessException e) {
				log.error("Error retrieving data ports for probe dummy Rack PDU " + probe.getItemId());
			}
		}
		else {
			// We shouldn't hit this case on an update, but if there is no rack PDU,
			// create it with the updated IP Address
			piqProbeMapper.updateProbeRackPDU(probe.getItemId(), ip);
		}
	}

	/**
	 * @param ip
	 * @param dPort
	 * @throws RemoteDataAccessException
	 */
	private void updateRPDU(String ip, DataPort dPort)
			throws RemoteDataAccessException {
		
		// update ip address only when the item doesn't have proxyIndex and 
		// powerIQ support ipAddressChangeAPI.
		if ( piqSyncPDUClient.isProxiedItem(dPort.getItem()) == false &&
				piqSyncPDUClient.doesPIQSupportIpAddressChangeAPI() == true ) {
			piqSyncPDUClient.updateIpAddress(ip, dPort);
		} else {
			Integer pduId = dPort.getItem().getPiqId();
			if (pduId != null && pduId > 0){
				piqSyncPDUClient.deletePDU(pduId.toString());
				dPort.getItem().setPiqId(null);
				if (sessionFactory != null){
					Session session = sessionFactory.getCurrentSession();
					session.merge(dPort.getItem());
					session.flush();
				}
			}
			// And then add the pdu item
			try {
				piqSyncPDUClient.addRPDU(dPort.getItem(), dPort, ip);
			}
			catch (InstalledPDUWithoutIPException e) {
				// We've already handled and logged this exception earlier.
				// We can ignore it here because it will not bubble up to initial sync.
			}
		}
	}
	

	/**
	 * @param session
	 * @param event
	 * @throws HibernateException
	 * @return IPAddress
	 */
	private String getIPAddress(Session session, LNEvent event)
			throws HibernateException {
		//Get the IPAddress from the tblipaddresses table
		
		String ip = null;
		
		String SQL_QUERY = " select tblipaddresses.ipaddress from tblipaddresses " 
				+ " where tblipaddresses.id = " + "'" +  event.getTableRowId() + "';";
		
		SQLQuery query = session.createSQLQuery(SQL_QUERY);
		
		List<String> ips = query.list();
		
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
	
	/**
	 * @param session
	 * @param ipAddress
	 * @throws HibernateException
	 * @return Item
	 */
	private DataPort getDataPort(Session session, LNEvent event){
		DataPort pduDataPort = null;
		
		
		String SQL_QUERY = " select dct_ports_data.port_data_id " + 
				" from dct_ports_data inner join tblipteaming on tblipteaming.portid = dct_ports_data.port_data_id " +
				" inner join tblipaddresses on tblipaddresses.id = tblipteaming.ipaddressid " +
				//" where tblipaddresses.ipaddress=" + "'" + ipAddress + "'" +
				" where tblipaddresses.id = " + event.getTableRowId() +
				" order by tblipteaming.portid; ";
		
		SQLQuery query = session.createSQLQuery(SQL_QUERY);
		
		List portIds = query.list();
		
		if (portIds.size() > 0){
			BigInteger portId = (BigInteger) portIds.get(0);
			pduDataPort = (DataPort) session.load(DataPort.class, portId.longValue());
		}
		
		return pduDataPort;
	}
	
	
    private Integer getPowerRating(Long itemId, Session session){
    	Integer result = 0;
		Criteria criteria = session.createCriteria(PowerPort.class);
		
		criteria.createAlias("item","item").add(Restrictions.eq("item.itemId", itemId));
		
		List list = criteria.list();
		
		if (list.size() > 0){
			PowerPort powerport = (PowerPort) list.get(0);
		
			if (powerport != null){
				result = powerport.getWattsBudget();
			}
		}
		
    	return result;
    }


}
