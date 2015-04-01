package com.raritan.tdz.request.dao;

import java.sql.Timestamp;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * DAO to operate on the request history tblRequestHistory table
 * @author bunty
 *
 */
public class RequestHistoryDAOImpl extends DaoImpl<RequestHistory> implements RequestHistoryDAO  {

	@Override
	public RequestHistory createReqHist(Request request,
			long requestStageValueCode, UserInfo userInfo) {

		UserInfo user = userInfo;
		Session session = this.getSession();
		
		RequestHistory hist = new RequestHistory();
		hist.setCurrent(true);
		if (user != null) {
			hist.setRequestedBy(user.getUserName());
		}
        hist.setRequestedOn(new Timestamp(java.util.Calendar.getInstance().getTimeInMillis()));
        hist.setRequestDetail(request);	
        hist.setStageIdLookup(SystemLookup.getLksData(session, requestStageValueCode));

        create(hist);
        
        return hist;

	}
	
	@Override
	public void setRequestHistoryNotCurrent(Request request) {
		
		Criteria criteria = getCriteriaAgainstRequest(request);

		criteria.add(Restrictions.eq("current", true));
		
		RequestHistory history = (RequestHistory) criteria.uniqueResult();
		
		history.setCurrent(false);
		
		update(history);
		
	}
	
	private Criteria getCriteriaAgainstRequest(Request request) {

		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(RequestHistory.class);
		criteria.createAlias("requestDetail", "requestDetail");
		criteria.add(Restrictions.eq("requestDetail.requestId", request.getRequestId()));
		
		return criteria;
	}
	
	
	@Override
	public void setCurrentRequestHistoryComment(Request request, String comment) {

		Criteria criteria = getCriteriaAgainstRequest(request);

		criteria.add(Restrictions.eq("current", true));
		
		RequestHistory history = (RequestHistory) criteria.uniqueResult();
		
		if (null == history) return;
		
		history.setComment(comment);
		
		mergeOnly(history);
		
	}
	
	@Override
	public String getCurrentRequestHistoryComment(Request request) {

		Criteria criteria = getCriteriaAgainstRequest(request);

		criteria.add(Restrictions.eq("current", true));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("comment"), "comment");
		
		criteria.setProjection(proList);
		
		return (String) criteria.uniqueResult();

	}

	@Override
	public String getRequestStageLkpValue(Request request) {

		Criteria criteria = getCriteriaAgainstRequest(request);
		
		criteria.add(Restrictions.eq("current", true));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("stageIdLookup"), "requestStage");
		
		criteria.setProjection(proList);

		LksData requestStageLks = (LksData) criteria.uniqueResult();
		
		return requestStageLks.getLkpValue();

	}

}
