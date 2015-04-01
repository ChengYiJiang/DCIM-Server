/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.jobs.PDUJobHandler;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class ITItemPowerPortSubscriber implements PowerPortSubscriber{

	private PIQSyncDeviceClient piqSycItemClient = null;
	private SessionFactory sessionFactory = null;
	private ItemHome itemHome;


	private Logger log = Logger.getLogger(this.getClass());
	
	ITItemPowerPortSubscriber(){
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


	@Override
	public void handleInsertEvent(Session session, Item item, PowerPort pPort) throws RemoteDataAccessException {
		if (piqSycItemClient != null && piqSycItemClient.isAppSettingsEnabled()){
			log.debug("Processing Insert PDU item");
			
			String ip = getDataPortNetInfo(pPort);
			//Here we update the device since the device should already exist on PIQ as this
			//is taken care by the item subscriber
			Integer powerRating = getPowerRating(item);
			piqSycItemClient.updateDevice(item,ip,powerRating, false);
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}



	@Override
	public void handleUpdateEvent(Session session, Item item, PowerPort pPort) throws RemoteDataAccessException {
		if (piqSycItemClient != null){
			log.debug("Processing Update PDU item");
			String ip = getDataPortNetInfo(pPort);
			Integer powerRating = getPowerRating(item);
			piqSycItemClient.updateDevice(item,ip,powerRating, false);
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
		if (piqSycItemClient != null){
			log.debug("Processing Update PDU item");
			
			//Get the item from the item id in the first custom field
			Long itemId = Long.parseLong( event.getCustomField1() );
			Item item = (Item) session.load(Item.class, itemId);
			
			String ip = getDataPortNetInfo(item);
			
			Integer powerRating = getPowerRating(item);
			piqSycItemClient.updateDevice(item,ip,powerRating, false);
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}
	
	

	/**
	 * @param pPort
	 * @return
	 */
	private String getDataPortNetInfo(PowerPort pPort) {
		//Get the IPAddress
		String ip = null;
		List<String> ips = itemHome.getDataPortNetInfo(pPort.getItem().getItemId());
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
	 * @param pPort
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

}
