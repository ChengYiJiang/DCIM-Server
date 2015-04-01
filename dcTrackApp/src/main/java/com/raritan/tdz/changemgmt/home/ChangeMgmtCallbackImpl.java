package com.raritan.tdz.changemgmt.home;

import java.util.Date;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.user.home.UserHome;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Change Management workflow callback implementation.
 * 
 * @author Andrew Cohen
 * @version 3.0
 */
public class ChangeMgmtCallbackImpl implements ChangeMgmtCallback {

	private SessionFactory sessionFactory;
	private ItemHome itemHome;
	private UtilHome utilHome;
	
	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired
	private UserHome userHome;

	public ChangeMgmtCallbackImpl(SessionFactory sessionFactory, ItemHome itemHome, UtilHome utilHome) {
		this.sessionFactory = sessionFactory;
		this.itemHome = itemHome;
		this.utilHome = utilHome;
	}
	
	@Override
	public long createAddItemWorkOrder(long requestId, long itemId, Date dueOne) throws DataAccessException {
		long workOrderId = 0;
		Session session = sessionFactory.getCurrentSession();
		
		try {
			Request request = (Request)session.get(Request.class, requestId);
			if (request != null) {
				Item item = itemHome.viewItemEagerById(itemId);
				if (item != null) {
					requestDAO.createWorkOrder(request, userHome.getCurrentUserInfo());
					/*WorkOrder wo = new WorkOrder( );
					wo.setWorkOrderDueOn( new Timestamp( dueOne.getTime() ) );
					wo.setWorkOrderNumber("Add Item: '" + item.getItemName() + "'");
					wo.setWorkOrderTypeLookup( SystemLookup.getLksData(session, SystemLookup.WorkOrderType.DEVICES) );
					wo.setCompleted(false);
					wo.setArchived(false);
					session.save( wo );
					
					workOrderId = wo.getWorkOrderId();
					
					request.setWorkOrder( wo );*/
					session.refresh( request );
					
					workOrderId = request.getWorkOrder().getWorkOrderId();
					
					updateRequestStage(requestId, SystemLookup.RequestStage.WORK_ORDER_ISSUED);
				}
			}
		}
		catch (HibernateException e) {
			throw new DataAccessException(new ExceptionContext(e.getMessage(), this.getClass(), e));
		}
		
		return workOrderId;
	}

	@Override
	public void completeWorkOrder(long workOrderId) throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		try {
			WorkOrder wo = (WorkOrder)session.get(WorkOrder.class, workOrderId); 
			if (wo != null) {
				wo.setCompleted(true);
				session.merge( wo );
				
				// wo.getRequest() is broken - criteria query below is workaround for now
				//Request req = wo.getRequest();
				
				Criteria c = session.createCriteria(Request.class);
				c.add( Restrictions.eq("workOrderId", wo.getWorkOrderId()) );
				Request req = (Request)c.uniqueResult();
				if (req != null) {
					updateRequestStage(req.getRequestId(), SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
				}
			}
		}
		catch (HibernateException e) {
			throw new DataAccessException(new ExceptionContext(e.getMessage(), this.getClass(), e));
		}
	}

	@Override
	public void updateItemStatus(long itemId, long itemStatusLkpValueCode) throws DataAccessException {
		Item item = itemHome.viewItemEagerById( itemId );
		if (item != null) {
			LksData itemStatusLks = utilHome.viewSystemLookupByValueCode(itemStatusLkpValueCode);
			if (itemStatusLks != null) {
				item.setStatusLookup( itemStatusLks );
				itemHome.editItem( item );
			}
		}
	}
	
	// TODO: This use current Request/Request history domain later - may be updated in 3.0 to use new domain model.
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateRequestStage(long requestId, long requestStageLkpValueCode) throws DataAccessException {
		Session session = sessionFactory.getCurrentSession();
		Request req = (Request)session.get(Request.class, requestId);
		
		// TODO: BusinessValidationException if request not found? 
		if (req != null) {
			Set<RequestHistory> histories = req.getRequestHistories();
			for (RequestHistory h : histories) {
				h.setCurrent(false);
				session.merge( h );
			}
			
			RequestHistory hist = new RequestHistory();
			hist.setRequestDetail( req );
			hist.setCurrent(true);
			hist.setRequestedOn( new Date() );
			hist.setStageIdLookup( SystemLookup.getLksData(session, requestStageLkpValueCode));
			
			session.save( hist );
		}
	}
}
