/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.InstalledPDUWithoutIPException;
import com.raritan.tdz.piq.home.PIQProbeMapper;
import com.raritan.tdz.piq.home.PIQSensorCommon;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
public class IPTeamingSubscriberImpl extends LNSubscriberBase {

	private PIQSyncDeviceClient piqSyncItemClient = null;
	private PIQSyncPDUClient piqSyncPDUClient = null;
	private ItemHome itemHome = null;
	private PIQProbeMapper piqProbeMapper = null;
	@Autowired
	private PIQSyncDeviceClient piqSycItemClient;
	
	@Autowired
	private PortHome portHome = null;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private PIQSensorCommon piqSensorCommon;

	private Logger log = Logger.getLogger(this.getClass());
	
	private String tableName="\"tblipteaming\"";

	public IPTeamingSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome,
			PIQSyncDeviceClient piqSyncItemClient,
			PIQSyncPDUClient piqSyncPDUClient,
			ItemHome itemHome,
			PIQProbeMapper piqProbeMapper) {
		super(sessionFactory, lnHome);
		this.piqSyncItemClient = piqSyncItemClient;
		this.piqSyncPDUClient = piqSyncPDUClient;
		this.itemHome = itemHome;
		this.piqProbeMapper = piqProbeMapper;
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
		Long portId = Long.parseLong(event.getCustomField1());
		if (portId != null){
			DataPort dPort = (DataPort) session.load(DataPort.class, portId);
			String ip = getDataPortNetInfo(dPort);
			
			addItemOnPIQ(dPort.getItem(),dPort,ip, session);
		}
	}


	/**
	 * @param dPort
	 * @return
	 */
	private String getDataPortNetInfo(DataPort dPort) {
		List<String> ips = itemHome.getDataPortNetInfo(dPort.getItem().getItemId());
		String ip = null;
		if (ips != null && ips.size() > 0)
			ip = ips.get(0);
		return ip;
	}
	
	/**
	 * @param item
	 * @return
	 */
	private String getDataPortNetInfo(Item item) {
		List<String> ips = itemHome.getDataPortNetInfo(item.getItemId());
		String ip = null;
		if (ips != null && ips.size() > 0)
			ip = ips.get(0);
		return ip;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
	
		Long itemId = event.getTableRowId();
		if (event != null && itemId != null){
			try{
				Item item = (Item) session.load(Item.class, itemId);
				if (item == null) return;
				
				String ipAddress = event.getCustomField1();
				
				if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU && 
						piqSyncPDUClient.isPduIntegratedWithPIQ(ipAddress, item.getPiqId())) {
					deleteItemOnPIQ(session, item, item.getPiqId().toString());
				} else {
					deleteItItem(item, ipAddress);
				}
				
			}catch (org.hibernate.ObjectNotFoundException e){
				log.warn("processDeleteEvent: Item with id = " + itemId + "not found");
			} catch (HibernateException e) {
				log.warn("processDeleteEvent: Item with id = " + itemId + "not found");
			} catch (DataAccessException e) {
				log.warn("processDeleteEvent: Item with id = " + itemId + "not found");
			}
		}

	}
	
	private void deleteItItem(Item item,  String ip) throws RemoteDataAccessException {
		
		Integer powerRating = getPowerRating(item);
		piqSycItemClient.updateDevice(item,ip,powerRating, false);
			
		// If item is a probe, we need to delete its associated dummy Rack PDU since we are deleting the data port
		if (item.getClassLookup() != null && item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE) {
			// get dummy rack pdu for this probe item.
			Item probeRPDU = piqProbeMapper.getLookup().getDummyRackPDUForProbeItem( item.getItemId() );
			if (probeRPDU != null) {
				try {
					if (piqSyncPDUClient.isPduIntegratedWithPIQ(ip, probeRPDU.getPiqId())) {
						itemHome.deleteItem( probeRPDU.getItemId(), false, null );
					}
				} 
				catch (BusinessValidationException e) {
					log.error("Error deleting probe RPDU " + probeRPDU.getItemId(), e);
					e.printValidationErrors();
				}
				catch (Throwable e) {
					log.error("Error deleting probe RPDU " + probeRPDU.getItemId(), e);
				}
			}
		}
	}


	/**
	 * @param session
	 * @param item
	 * @param piqId
	 * @throws RemoteDataAccessException
	 * @throws HibernateException
	 * @throws DataAccessException 
	 */
	private void deleteItemOnPIQ(Session session, Item item, String piqId)
			throws RemoteDataAccessException, HibernateException, DataAccessException {
		if (item != null){
			if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU){
				piqSyncPDUClient.deletePDU(piqId);
				resetPIQID(session, item);
				resetPIQIdForPorts(item);
			} else if ((item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE)
					|| (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK)
					|| (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)){
//				piqSyncItemClient.deleteDevice(piqId);
//				resetPIQID(session, item);
				//Here we update the device since we are only changing the IPAddress to null
				//on PIQ
				Integer powerRating = getPowerRating(item);
				String ipAddress = getDataPortNetInfo(item);
				piqSyncItemClient.updateDevice(item, ipAddress, powerRating, false );
			}
		}
	}


	private void resetPIQIdForPorts(Item item) throws DataAccessException {
		powerPortDAO.resetPIQId(item);
		piqSensorCommon.deleteAllSensors(item, getErrorObject());
	}
	
	/**
	 * @param item
	 * @param session TODO
	 * @param session
	 * @param piqId
	 * @throws RemoteDataAccessException
	 * @throws HibernateException
	 */
	private void addItemOnPIQ(Item item, DataPort dataPort, String ipAddress, Session session)
			throws RemoteDataAccessException, HibernateException {
		if (item != null){
			if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU){
				try {
					piqSyncPDUClient.addRPDU(item, dataPort, ipAddress);
				}
				catch (InstalledPDUWithoutIPException e) {
					// We've already handled and logged this exception earlier.
					// We can ignore it here because it will not bubble up to initial sync.
				}
				
			} else if ((item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE)
					|| (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK)){
				//Here we update the device since the device should already exist on PIQ as this
				//is taken care by the item subscriber	
				Integer powerRating = getPowerRating(item);
				piqSyncItemClient.updateDevice(item, ipAddress, powerRating, false );
			} else if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE){
				updateProbe(item,ipAddress,session);
			}
		}
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
	 * @param session
	 * @param item
	 * @throws HibernateException
	 */
	private void resetPIQID(Session session, Item item)
			throws HibernateException {
		if (session != null){
			item.setPiqId(null);
			session.merge(item);
			session.flush();
		}
	}
	
	private String getPiqId(DataPort dPort){
		String piqId = null;
		
		if (dPort != null){
			Item item = dPort.getItem();
			if (item != null){
				Integer id = item.getPiqId();
				if (id != null) 
					piqId = id.toString();
			}
		}
		
		return piqId;
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
		DataPort dPort = getDataPort(session, ip);
		
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
				} else if (itemClass != null && (itemClass.getLkpValueCode() == SystemLookup.Class.DEVICE)
						|| (itemClass.getLkpValueCode() == SystemLookup.Class.NETWORK)
						|| (itemClass.getLkpValueCode() == SystemLookup.Class.PROBE))
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
		Integer powerRating = getPowerRating(item);
		piqSyncItemClient.updateDevice(item, ip, powerRating, false);
		
		// For probes, create a dummy rack PDU item in dcTrack
		if ((item.getClassLookup() != null) && (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)) {
			piqProbeMapper.updateProbeRackPDU(item.getItemId(), ip);
		}
	}

	/**
	 * @param ip
	 * @param dPort
	 * @throws RemoteDataAccessException
	 */
	private void updateRPDU(String ip, DataPort dPort)
			throws RemoteDataAccessException {
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
			//And then add the pdu item
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
	private DataPort getDataPort(Session session, String ipAddress){
		DataPort pduDataPort = null;
		
		
		String SQL_QUERY = " select dct_ports_data.port_data_id " + 
				" from dct_ports_data inner join tblipteaming on tblipteaming.portid = dct_ports_data.port_data_id " +
				" inner join tblipaddresses on tblipaddresses.id = tblipteaming.ipaddressid " +
				" where tblipaddresses.ipaddress=" + "'" + ipAddress + "'" +
				" order by tblipteaming.portid; ";
		
		SQLQuery query = session.createSQLQuery(SQL_QUERY);
		
		List portIds = query.list();
		
		if (portIds.size() > 0){
			BigInteger portId = (BigInteger) portIds.get(0);
			pduDataPort = (DataPort) session.load(DataPort.class, portId.longValue());
		}
		
		return pduDataPort;
	}
	

	/**
	 * @param item
	 * @return
	 */
	private Integer getPowerRating(Item item) {
		List<Integer> powerRatings = itemHome.getPowerRating(item.getItemId());
		Integer powerRating = null;
		if (powerRatings != null && powerRatings.size() > 0){
			powerRating = powerRatings.get(0);
		}
		return powerRating;
	}

	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, SensorPort.class.getName());
		return errors;
	}

}
