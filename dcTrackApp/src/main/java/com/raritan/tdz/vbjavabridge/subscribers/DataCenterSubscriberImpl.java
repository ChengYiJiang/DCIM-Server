package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQSyncLocationClient;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;
import com.raritan.tdz.vpc.home.VPCHome;

/**
 * @author prasanna
 *
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
public class DataCenterSubscriberImpl extends LNSubscriberBase implements ItemWriter<DataCenterLocationDetails>  {


	private PIQSyncLocationClient piqSyncLocationClient;
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private VPCHome vpcHome;
	
	@Autowired
	private LocationDAO locationDAO;

	public DataCenterSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome, PIQSyncLocationClient piqSyncLocationClient) {
		super(sessionFactory, lnHome);
		this.piqSyncLocationClient = piqSyncLocationClient;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, DataCenterLocationDetails.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, DataCenterLocationDetails.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, DataCenterLocationDetails.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);
			
			session.close();
		}
	}

	/**
	 * @param session
	 * @param event
	 * @return
	 * @throws HibernateException
	 */
	private DataCenterLocationDetails getDataCenterLocationDetails(Session session, LNEvent event)
			throws HibernateException {
		//There is a big assumption here. The table row id provided in the event is of an data port :-)
		//Since we subscribe for all item events here, we should be okay.
		DataCenterLocationDetails dc = (DataCenterLocationDetails) session.load(DataCenterLocationDetails.class, event.getTableRowId());
		return dc;
	}
	

	@Override
	protected void processInsertEvent(Session session, LNEvent event) throws RemoteDataAccessException, Exception {
		try {
			// session.clear();
			DataCenterLocationDetails location = getDataCenterLocationDetails(session,event);
			if (location != null) {
				addLocation(session,location);
			}
		}catch (HibernateException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		try {
			String dataCenterId = event.getCustomField1();
			piqSyncLocationClient.deleteDataCenter(dataCenterId);
		}catch (HibernateException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event) throws RemoteDataAccessException, Exception {
		try {
			DataCenterLocationDetails location = getDataCenterLocationDetails(session,event);
			if (location != null) {
				updateLocation(session, location);
			}
		}catch (ObjectNotFoundException e){
			// Note: When we import DC via 'DC Import'we insert and update record which causes both 
			// insert and update event.
			// Therefore, when we delete location , due to error, while processing 'processInsertEvent', 
			// the subsequent execution of 'processUpdateEvent' may throw error indication
			// that row does not exist. 
			if (log.isDebugEnabled())
					e.printStackTrace();
		}
	}

	@Override
	public void write(List<? extends DataCenterLocationDetails> locations)
			throws Exception {
		log.debug("In PIQSyncLocationClientImpl write method: List Size is : " + locations.size());
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			for (DataCenterLocationDetails location:locations){
				try {
					if (!piqSyncLocationClient.isLocationInSync(location.getPiqId())){ 
						addLocation(session, location);
					} else {
						// Get locationType from details 
						piqSyncLocationClient.updateDataCenter(location); 
					}
				} catch (RemoteDataAccessException e) {
					if (log.isDebugEnabled())
						e.printStackTrace();
					//If for some reason PIQ does not contain location information, try to add one
					ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum)e.getExceptionContext().getExceptionItem( ExceptionContext.APPLICATIONCODEENUM);
					if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
					{
						addLocation(session,location);
					} else{
						throw new Exception(e);
					}
				}
			}
			session.flush();
		}
		
	}

	private String addLocation(Session session, 
			DataCenterLocationDetails location) 
			throws Exception {
		String piqId = null;
		try {
			piqId = piqSyncLocationClient.addDataCenter(location);
		}
		catch (RemoteDataAccessException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			throw new Exception(e);
		}
		
		if (piqSyncLocationClient.isAppSettingsEnabled() && location.getPiqExternalKey() != null &&
				!location.getPiqExternalKey().isEmpty() && piqId == null) {
			// site not found on piq and cannot be mapped
			// no need to retain this newly added site in dcTrack.
			// Commenting the code because location object cannot
			// be deleted at this point of time, it gives error.
			session.refresh(location);
			//TODO: In future we need to check if the location has any items before performing the delete operation.
			//      This is currently validated by CV.
			// delete all VPC items and then delete the location.
			try {
				vpcHome.delete(location.getDataCenterLocationId(), null);
			} catch (Throwable e) {

				throw new Exception("Cannot delete VPC item for location" + location.getCode());
			}
			
			// delete all items in this location
			locationDAO.deleteLocationAndItems(location.getDataCenterLocationId(), null);
			
			// session.delete(location);
			session.flush();
			
		} else {
			location.setPiqId(piqId);
			session.merge(location);
		}
		
		return piqId;
	}
	
	private String updateLocation(Session session, 
			DataCenterLocationDetails location) 
			throws Exception {
		String piqId = null;
		try {
			// update uses piqId for all update operation.
			// When user is trying to map an existing datacenter in 
			// dcTrack to that in PowerIQ via 'DC import', an external key
			// is set for mapping, and DB triggers update event. 
			// In this case, check if external key is present and 
			// there is NO powerIQ id only then map the site.
			if (location != null && location.getPiqExternalKey() != null &&
					location.getPiqId() == null) {
				piqId = piqSyncLocationClient.mapByExternalKey(location);
				if (piqId != null) {
					location.setPiqId(piqId);
					session.merge(location);
				}
			}
			else {
				piqSyncLocationClient.updateDataCenter(location);
				session.merge(location);
			}
		} catch (RemoteDataAccessException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			throw new Exception(e);
		}
		return piqId;
	}
}
