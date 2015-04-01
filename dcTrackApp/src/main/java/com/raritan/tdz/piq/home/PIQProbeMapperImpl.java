package com.raritan.tdz.piq.home;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPAddress;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemSNMP;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.domain.ProbePDULink;
import com.raritan.tdz.port.home.PortHome;

/**
 * PIQProbeMapper Default Implementation.
 * @author Andrew Cohen
 */
public class PIQProbeMapperImpl implements PIQProbeMapper {

	private final Logger log = Logger.getLogger("PIQProbeMapper");
	
	private ItemHome itemHome;
	private PortHome portHome;
	private SessionFactory sessionFactory;
	private Queue<AsyncTask> probeQueue;
	private int maxRequestQueueSize = 100;
	private PIQSyncPDUClient pduClient;
	private PIQProbeLookup probeLookup;
	private MessageSource messageSource;
	
	public PIQProbeMapperImpl(SessionFactory sessionFactory, 
			ItemHome itemHome,
			PortHome portHome,
			PIQSyncPDUClient pduClient,
			PIQProbeLookup probeLookup,
			MessageSource messageSource) {
		this.sessionFactory = sessionFactory;
		this.itemHome = itemHome;
		this.portHome = portHome;
		this.pduClient = pduClient;
		this.probeLookup = probeLookup;
		this.messageSource = messageSource;
		this.probeQueue = new ConcurrentLinkedQueue<PIQProbeMapperImpl.AsyncTask>();
	}
	
	@Required
	public void setMaxRequestQueueSize(int maxRequestQueueSize) {
		this.maxRequestQueueSize = maxRequestQueueSize;
	}
	
	@Override
	public long updateProbeRackPDU(long probeItemId, String ipAddress) throws RemoteDataAccessException {
		if (log.isDebugEnabled()) log.debug("Calling updateProbeRackPDU...");
		if (ipAddress == null) {
			if (log.isDebugEnabled()) {
				log.debug("No IP Address on updateProbeRackPDU call");
			}
			return -1;
		}
		
		Item probeItem = (Item)sessionFactory.getCurrentSession().get(Item.class, probeItemId);
		if (probeItem == null) {
			log.warn("updateProbeRackPDU: No probe found for item id = " + probeItemId);
			return -1;
		}
		
		Item probeRPDU = probeLookup.getDummyRackPDUForProbeItem( probeItem.getItemId() ); 
		
		if (probeRPDU == null) {
			probeRPDU = createProbeRackPDUItem( probeItem, ipAddress );
			
			if (probeRPDU != null) {
				addProbeRackPDUDataPort(probeItem.getItemId(), probeRPDU.getItemId());
			}
		}
		else {
			boolean updatedPort = updateProbeRackPDUIpAddress(probeRPDU, ipAddress);
			assert updatedPort;
			
			updateProbeRackPDUSnmp3(probeRPDU, probeItem.getItemSnmp());
			
			updateProbeRackPDUPiqID( probeRPDU, ipAddress );
		}
		
		return probeRPDU.getItemId();
	}
	
	@Override
	public void updateProbeRackPDUDataPort(long probeItemId, DataPort port, String ipAddress) throws RemoteDataAccessException {
		if (log.isDebugEnabled()) log.debug("Calling updateProbeRackPDUDataPort...");

		Item probeItem = (Item)sessionFactory.getCurrentSession().get(Item.class, probeItemId);
		if (probeItem == null) {
			log.warn("updateProbeRackPDUDataPort: No probe found for item id = " + probeItemId);
			return;
		}
		
		Item probeRPDU = probeLookup.getDummyRackPDUForProbeItem( probeItemId );
		
		if (probeRPDU != null) {
			DataPort probeDataPort = getDataPort( probeItem.getItemId() );
			if (probeDataPort != null) {
				DataPort pduDataPort = getDataPort( probeRPDU.getItemId() );
				// We only need to update the community string on probe RPDU data port
				pduDataPort.setCommunityString( probeDataPort.getCommunityString() );
				sessionFactory.getCurrentSession().merge( pduDataPort );
			}
		}
		else {
			// The probeRPDU should already exist, but if not, create it
			probeRPDU = createProbeRackPDUItem( probeItem, ipAddress );
			if (probeRPDU != null) {
				addProbeRackPDUDataPort(probeItem.getItemId(), probeRPDU.getItemId());
			}
		}
	}
	
	@Override
	public void queueProbeRequest(long probeItemId, String ipAddress) {
		if (log.isDebugEnabled()) {
			log.debug("Queueing probe request for id = " + probeItemId + ", ip = " + ipAddress);
		}
		
		probeQueue.add( new AsyncTask(probeItemId, ipAddress) );
		
		if (probeQueue.size() >= maxRequestQueueSize) {
			flushQueuedProbeRequests();
		}
	}
	
	@Override
	public synchronized void flushQueuedProbeRequests() {
		if (log.isDebugEnabled()) {
			log.debug("Flushing " + probeQueue.size() + " Probe Rack PDU requests");
		}
		
		while (!probeQueue.isEmpty()) {
			probeQueue.remove().run();
		}
		
		if (log.isDebugEnabled()) log.debug("Completed flushing probe request queue");
	}
	
	@Override
	public boolean deleteProbeRackPDUForProbeItemId(long probeItemId) {
		if (log.isDebugEnabled()) {
			log.debug("Deleting dummy rack PDU for probe with id = " + probeItemId);
		}
		
		boolean ret = false;
		
		// Lookup dummy rack PDU ID
		Item pdu = probeLookup.getDummyRackPDUForProbeItem( probeItemId );
		
		if (pdu != null) {
			try {
				itemHome.deleteItem( pdu.getItemId(), false, null );
				ret = true;
			}
			/*catch (DataAccessException e) {
				log.error("Error deleting dummy rack PDU with id = " + pdu.getItemId(), e);
				ret = false;
			}*/
			catch (BusinessValidationException e) {
				e.printValidationErrors();
				log.error("Error deleting dummy rack PDU with id = " + pdu.getItemId(), e);
				ret = false;
			}
			catch (Throwable e) {
				log.error("Error deleting dummy rack PDU with id = " + pdu.getItemId(), e);
				ret = false;
			}
		}
		
		return ret;
	}
	
	@Override
	public PIQProbeLookup getLookup() {
		return probeLookup;
	}
	
	@Override
	public void updateProbeRPDUCabinet(long probeItemId) {
		Session session = sessionFactory.getCurrentSession();
		Item probeRPDU = probeLookup.getDummyRackPDUForProbeItem( probeItemId );
		if (probeRPDU != null) {
			Item probe = (Item)session.get(Item.class, probeItemId);
			if (probe != null) {
				probeRPDU.setParentItem( probe.getParentItem() );
				session.merge( probeRPDU );
			}
		}
	}
	
	@Override
	@Scheduled(fixedDelay = 360000)
	public void deleteOrphanedProbePDULinks() {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.getNamedQuery("deleteOrphanedProbePDULinks");
		final int count = q.executeUpdate();
		if (count > 0) {
			log.info("Cleaned up " + count + " orphaned Probe PDU associations");
		}
	}
	
	@Override
	public boolean deleteUnmappedProbePDULink(long unmappedProbePiqId) {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createQuery("delete from ProbePDULink where unmappedProbePiqId = :unmappedProbePiqId");
		q.setLong("unmappedProbePiqId", unmappedProbePiqId);
		final int count = q.executeUpdate();
		if (count > 0) {
			return true;
		}
		return false;
	}
	
	//
	// Private methods
	//
	
	private Item createProbeRackPDUItem(Item probeItem, String ipAddress) throws RemoteDataAccessException {
		log.debug("No associated Rack PDU item found for probe - will create one");
		
		Session session = sessionFactory.getCurrentSession();
		
		Item probeRPDU = new Item();
		probeRPDU.setDataCenterLocation( probeItem.getDataCenterLocation() );
		probeRPDU.setParentItem( probeItem.getParentItem() );
		probeRPDU.setItemName( probeItem.getItemName() + "-PROBE-RPDU");
		probeRPDU.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.RACK_PDU) );
		probeRPDU.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.HIDDEN) );
		probeRPDU.setModel( null );
		
		ItemServiceDetails probeRPDUDetail = new ItemServiceDetails();
		probeRPDUDetail.setDescription("Dummy Rack PDU for Probe PIQ Integration");
		probeRPDU.setItemServiceDetails( probeRPDUDetail );
		
		ItemSNMP itemSNMP = probeItem.getItemSnmp();
		if (itemSNMP != null) {
			ItemSNMP clonnedItemSNMP = (ItemSNMP)itemSNMP.clone();
			clonnedItemSNMP.setItem(probeRPDU);
			probeRPDU.setItemSnmp( clonnedItemSNMP );
		}
		
		boolean foundExistingPDU = false;
		Integer piqId = getPduPiqId( ipAddress );
		if (piqId != null) {
			if (log.isDebugEnabled()) {
				log.debug("Setting existing PIQ ID on probe Rack PDU item");
			}
			probeRPDU.setPiqId( piqId );
			foundExistingPDU = true;
		}
		
		try {
			itemHome.saveItem( probeRPDU );
			//session.save( new ProbePDULink(probeItem, probeRPDU) );
			saveOrUpdateProbeRPDULink( new ProbePDULink(probeItem, probeRPDU) );
		} 
		catch (DataAccessException e) {
			log.error("Error saving probe Rack PDU item", e);
			probeRPDU = null;
		}
		
		if (foundExistingPDU) {
			//log.error(">>>>> - createProbeRackPDUItem() Thread id: " + Thread.currentThread().getId() +
			//		"Session Ptr = " + Integer.toHexString(System.identityHashCode(session)));

			// Since we found an existing PDU in PIQ, call the postProcess method
			// to ensure power and sensor ports are linked.
			pduClient.postProcessAddPDU(probeItem, probeRPDU, piqId);
		}
		 
		return probeRPDU;
	}
	
	private Integer getPduPiqId(String ipAddress) throws RemoteDataAccessException {
		Integer piqId = pduClient.lookupByIPAddress( ipAddress );
		return piqId;
	}
	
	private void addProbeRackPDUDataPort(long probeItemId, long pduItemId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Item probeItem = (Item)session.get(Item.class, probeItemId);
			
			if (probeItem == null) {
				log.warn("Could not load probe Item with id = " + probeItemId);
				return;
			}
			
			Item pduItem = (Item)session.get(Item.class, pduItemId);
			if (pduItem == null) {
				log.warn("Could not load probe Rack PDU Item with id = " + pduItemId);
				return;
			}
			
			Collection<DataPort> dataPorts = portHome.viewDataPortsByCriteria(probeItemId, -1, null, null, null, -1, true);
			if (dataPorts != null && !dataPorts.isEmpty()) {
				
				for (DataPort dp : dataPorts) {
					DataPort pduDataPort = new DataPort();
					pduDataPort.setPortName( dp.getPortName() );
					pduDataPort.setItem( pduItem );
					pduDataPort.setCommunityString( dp.getCommunityString() );
					
					portHome.saveDataPort( pduDataPort );
					
					for (IPAddress ipAddress : dp.getIpAddresses()) {
						pduDataPort.addIpAddress( ipAddress );
						session.update( pduDataPort );
					}
				}
			}
		}
		catch (Throwable e) {
			log.error("Error adding data port to Probe Rack PDU item", e);
		}
	}
	
	private boolean updateProbeRackPDUIpAddress(Item probeRackPDUItem, String ipAddress) {
		if (log.isDebugEnabled()) log.debug("Called PIQProbeMapper.updateProbeRackPDUIpAddress");
		boolean updatedPort = false;
		
		try {
			Collection<DataPort> dataPorts = portHome.viewDataPortsByCriteria(probeRackPDUItem.getItemId(), 
					SystemLookup.Class.RACK_PDU,
					null, // port IDs
					null, // port Subclass
					null, // sort order
					-1,   // parent Item Id
					true  // connected
			);
			
			if (dataPorts != null && !dataPorts.isEmpty()) {
				boolean ipAssigned = false; /* flag that checks if ipAdress is already
											   assigned to one of the data port */
				for (DataPort dp : dataPorts) {
					if (updatedPort) break;
					
					Set<IPAddress> ipAddresses = dp.getIpAddresses();
					if (ipAddresses.isEmpty()) {
						  if (ipAssigned == true) addProbeRackPDUIPAddress(dp.getPortId(), ipAddress);
					}
					else {
						ipAssigned = false;
						for (IPAddress curIpAddr : ipAddresses) {
							String curIp = curIpAddr.getIpAddress();
							
							if (curIp != null && !curIp.equals(ipAddress)) {
								curIpAddr.setIpAddress( ipAddress );
								sessionFactory.getCurrentSession().update( curIpAddr );
								updatedPort = true;
								
								if (log.isDebugEnabled()) {
									log.debug("Updated probe Rack PDU '" + probeRackPDUItem.getItemName() +
											"' (id=" + probeRackPDUItem.getItemId() +
											") Ip Address from " + curIp + " to " + ipAddress);
								}
								
								break;
							}
						}
					}
				}
			}
		}
		catch (DataAccessException e) {
			log.error("", e);
		}
		
		if (!updatedPort) {
			if (log.isDebugEnabled()) {
				log.debug("Did not update Probe Rack PDU IP Address");
			}
		}
		
		return updatedPort;
	}

	private void addProbeRackPDUIPAddress(long dataPortId, String ipAddress) {
		try {
			Session session = sessionFactory.getCurrentSession();
			DataPort dp = portHome.viewDataPortsById( dataPortId );
			dp.addIpAddress(session, ipAddress);
			session.update( dp );
		}
		catch (Throwable e) {
			log.error("Error setting IP Address on Probe Rack PDU item data port", e);
		}
	}
	
	private void updateProbeRackPDUSnmp3(Item probeRackPDUItem, ItemSNMP updatedSNMP) {
		ItemSNMP currentSNMP = probeRackPDUItem.getItemSnmp();
		Session session = sessionFactory.getCurrentSession();
		
		if (currentSNMP != null && updatedSNMP == null) {
			session.delete( currentSNMP );
			if (log.isDebugEnabled()) {
				log.debug("Removed item SNMP3 record on probe rack PDU " + probeRackPDUItem.getItemId());
			}
		}
		else if (currentSNMP == null && updatedSNMP != null) {
			// Add new SNMP3 record to dummy rack PDU
			ItemSNMP newSNMP = (ItemSNMP)updatedSNMP.clone();
			newSNMP.setItem( probeRackPDUItem );
			newSNMP.setItemSNMPId( probeRackPDUItem.getItemId() );
			session.save( newSNMP );
			if (log.isDebugEnabled()) {
				log.debug("Added new item SNMP3 record on probe rack PDU " + probeRackPDUItem.getItemId());
			}
		}
		else if (currentSNMP != null && updatedSNMP != null) {
			// Update SNMP3 info
			currentSNMP.copyFrom( updatedSNMP );
			if (currentSNMP.getItemSNMPId() == null) {
				currentSNMP.setItemSNMPId( probeRackPDUItem.getItemId() );
				session.saveOrUpdate( currentSNMP );
			}
			else {
				session.update( currentSNMP );
			}
			if (log.isDebugEnabled()) {
				log.debug("Updated SNMP3 record on probe rack PDU " + probeRackPDUItem.getItemId());
			}
		}
	}
	
	/**
	 * Update the PIQ ID of the probe rack PDU if it is null. This is possible
	 * if the probe was previously "unmapped" from the PIQ import wizard.
	 * @param probeRPDU
	 * @param ipAddress
	 */
	private void updateProbeRackPDUPiqID(Item probeRPDU, String ipAddress) throws RemoteDataAccessException {
		if (probeRPDU.getPiqId() != null) return;
		
		if (log.isDebugEnabled()) {
			log.debug("PIQ ID is null on existing probe Rack PDU with IP Address " + ipAddress);
		}
		
		Integer piqId = getPduPiqId( ipAddress );
			
		if (piqId != null) {
			pduClient.postProcessAddPDU(null, probeRPDU, piqId);
		}
		else {
			log.warn("Failed to lookup dummy Rack PDU in power IQ with IP Address " + ipAddress);
		}
	}
	
	/**
	 * Find first data port on an item.
	 * @param itemId
	 * @return
	 */
	private DataPort getDataPort(long itemId) {
		DataPort dp = null;
		
		try {
			Collection<DataPort> dataPorts = portHome.viewDataPortsByCriteria(itemId, -1, null, null, null, -1, true);
			if (dataPorts != null) {
				for (DataPort dataPort : dataPorts) {
					Set<IPAddress> ipAddresses = dataPort.getIpAddresses();
					if (ipAddresses != null && !ipAddresses.isEmpty()) {
						dp = dataPort;
						break;
					}
				}
			}
		}
		catch (DataAccessException e) {
			log.error("", e);
		}
		
		return dp;
	}
	
	private void saveOrUpdateProbeRPDULink(ProbePDULink link) {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createQuery("from ProbePDULink where probeId = :probeId");
		q.setLong("probeId", link.getProbe().getItemId());
		//q.setLong("dummyRpduId", link.getDummyRPDU().getItemId());
		ProbePDULink existing = (ProbePDULink)q.uniqueResult();
		
		if (existing != null) {
			if (log.isDebugEnabled()) {
				log.debug("Existing probe " + existing.getProbeId() + ", changed dummy Rpdu from " +
					existing.getDummyRpduId() + " to " + link.getDummyRPDU().getItemId());
			}
			existing.setProbe( link.getProbe() );
			existing.setDummyRPDU( link.getDummyRPDU() );
			session.update( existing );
		}
		else {
			session.save( link );
		}
	}
	
	/**
	 * A wrapper for running the updateProbeRackPDU operation asynchronously.
	 */
	private class AsyncTask implements Runnable {
		private long probeItemId;
		private String ipAddress;
		
		AsyncTask(long probeItemId, String ipAddress) {
			this.probeItemId = probeItemId;
			this.ipAddress = ipAddress;
		}
		
		public void run() {
			try {
				updateProbeRackPDU(probeItemId, ipAddress);
			} 
			catch (RemoteDataAccessException e) {
				String msgData = messageSource.getMessage("remote.error",
						new Object[] { e.getUrl(), e.getMessage() },
						null
				);
				log.error( msgData );
			}
		}
	}
}
