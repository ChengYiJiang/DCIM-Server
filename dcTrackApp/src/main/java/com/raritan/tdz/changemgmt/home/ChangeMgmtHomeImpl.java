package com.raritan.tdz.changemgmt.home;


import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.changemgmt.home.workflow.WorkflowHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;

/**
 * Change Management business implementation.
 * @author Andrew Cohen
 * @version 3.0
 */
public class ChangeMgmtHomeImpl implements ChangeMgmtHome {

	private WorkflowHome workflow;
	private SessionFactory sessionFactory;
	
	// TODO: Remove when new tables and hibernate domain layer
	// for Requests/Work Orders is available.
	@Autowired(required=true)
	private ChangeMgmtHome26 changeMgmt26;
	
	public ChangeMgmtHomeImpl(SessionFactory sessionFactory, WorkflowHome workflow) {
		this.sessionFactory = sessionFactory;
		this.workflow = workflow;
	}
	
	@Override
	public Request addNewItem(Item item, UserInfo user) throws DataAccessException, SystemException {
		// TODO: BusinessValidation for item and user
		if (item != null && user != null) {
			
			// TODO: Replace changeMgmt26 when new Request/WorkOrder domain is available
			long requestId = changeMgmt26.createRequest(item.getItemId(), "New Item: " + item.getItemName(), "dct_items", "Item");
			
			String processId = workflow.startNewItemWorkflow(user, item, requestId);
			
			// Link the Request to the workflow process ID 
			//Session session = sessionFactory.getCurrentSession();
			Request request = (Request)sessionFactory.getCurrentSession().get(Request.class, requestId);
			//req.setWorkflowProcessId( processId ); 
			//session.merge( req );
			
			return request;
		}
		
		return null;
	}
	
	@Override
	public List<Request> getRequests() {
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(Request.class);
		c.addOrder( Order.desc("dueOn") );
		c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return c.list();
	}

	@Override
	public List<WorkOrder> getWorkOrders() {
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria( WorkOrder.class );
		c.addOrder( Order.desc("workOrderDueOn") );
		c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		return c.list();
	}
}
