/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

 import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQProbeMapper;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class ITItemDataPortSubscriber implements DataPortSubscriber{

	private PIQSyncDeviceClient piqSycItemClient = null;
	private SessionFactory sessionFactory = null;
	private ItemHome itemHome;
	private PIQProbeMapper piqProbeMapper = null;
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	private PIQSyncPDUClient piqSyncPDUClient = null;

	private Logger log = Logger.getLogger(this.getClass());
	
	ITItemDataPortSubscriber(){
		
	}
	
	public PIQSyncDeviceClient getPiqSycItemClient() {
		return piqSycItemClient;
	}

	public void setPiqSycItemClient(PIQSyncDeviceClient piqSycItemClient) {
		this.piqSycItemClient = piqSycItemClient;
	}

	/**
	 * @return the sessionFactory
	 */
	public final SessionFactory getSessionFactory() {
		return sessionFactory;
	}


	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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

	
	public final void setPiqProbeMapper(PIQProbeMapper piqProbeMapper) {
		this.piqProbeMapper = piqProbeMapper;
	}
	
	
	@Override
	public void handleInsertEvent(Session session, Item item, DataPort dPort) throws RemoteDataAccessException {
		if (piqSycItemClient != null && piqSycItemClient.isAppSettingsEnabled()){
			log.debug("Processing Insert PDU item");
			
			String ip = getDataPortNetInfo(dPort);
			
			Integer powerRating = getPowerRating(item);
			
			//Here we update the device since the device should already exist on PIQ as this
			//is taken care by the item subscriber			
			piqSycItemClient.updateDevice(item,ip,powerRating, false);
			
			// For probes, create a dummy rack PDU item in dcTrack
			if ((item.getClassLookup() != null) && (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)) {
				piqProbeMapper.updateProbeRackPDU(item.getItemId(), getDataPortNetInfo( dPort ));
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}



	@Override
	public void handleUpdateEvent(Session session, Item item, DataPort dPort) throws RemoteDataAccessException {
		if (piqSycItemClient != null){
			log.debug("Processing Update PDU item");
			String ip = getDataPortNetInfo(dPort);
			Integer powerRating = getPowerRating(item);
			piqSycItemClient.updateDevice(item,ip,powerRating, false);
			
			// CR 43742 - update data port on probe's associated dummy rack RPDU
			if (item.getClassLookup() != null && item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE) {
				piqProbeMapper.updateProbeRackPDUDataPort(item.getItemId(), dPort, ip);
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	public void handleDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		log.debug("Delete event is handled by IPTeamingSubscriberImpl->deleteEvent handler");
	}
	
	/**
	 * @param item
	 * @return
	 */
	private Integer getPowerRating(Item item) {
		return itemDAO.getEffectiveBudgetedWattsForAnItem(item.getItemId());
	}


	/**
	 * @param dPort
	 * @return
	 */
	private String getDataPortNetInfo(DataPort dPort) {
		//Get the IPAddress
		String ip = null;
		List<String> ips = itemHome.getDataPortNetInfo(dPort.getItem().getItemId());
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
	 * @param item
	 * @return
	 */
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

}
