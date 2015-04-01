package com.raritan.tdz.move.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestHome;
import com.raritan.tdz.user.dao.UserDAO;

/**
 * process the move request(s) created in request bypass mode
 * @author bunty
 *
 */
public class RequestBypassMoveBehavior implements ItemSaveBehavior {

	private RequestHome requestHome;
	
	@Autowired(required=true)
	protected RequestDAO requestDAO;
	
	@Autowired(required=true)
	protected UserDAO userDAO;
	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	public RequestHome getRequestHome() {
		return requestHome;
	}

	public void setRequestHome(RequestHome requestHome) {
		this.requestHome = requestHome;
	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		Long moveItemId = item.getItemToMoveId();
		if (null == moveItemId || moveItemId <= 0) { //do nothing
			return;
		}
		
		Errors errors = (Errors) additionalArgs[0];

		Long rStage[] = {SystemLookup.RequestStage.REQUEST_ISSUED, 
				SystemLookup.RequestStage.REQUEST_UPDATED, SystemLookup.RequestStage.REQUEST_APPROVED,
				SystemLookup.RequestStage.REQUEST_REJECTED, SystemLookup.RequestStage.WORK_ORDER_ISSUED};

		List<Request> requests = requestDAO.getRequestForItem(moveItemId, rStage);

		if(requests == null || requests.size() == 0) return;
		
		// get request bypass setting in db for this session user, 
		// requestManager will skip to process for user who dont 
		// have permission
		Boolean requestBypassSetting = userDAO.getUserRequestByPassSetting(new Long(sessionUser.getUserId()));
		
		if (null == requestBypassSetting || !requestBypassSetting) return;
		
		requestHome.processRequests(sessionUser, requests, errors);
	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}

}
