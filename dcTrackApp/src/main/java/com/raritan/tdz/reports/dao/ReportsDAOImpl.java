package com.raritan.tdz.reports.dao;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfig;

public class ReportsDAOImpl extends DaoImpl<Report> implements ReportsDAO {

	@Override
	public List<Report> getAllReports() {
		List<Report> retval = null;
		Session session = this.getSession();
		Query query = session.getNamedQuery("Reports.getAllReports");

		retval = (List<Report>) query.list();

		return retval;
	}
	@Override
	public List<Report> getAllReportsDetached() {
		List<Report> retval = null;
		Session newSession = this.getNewSession(); //attached
		Query query = newSession.getNamedQuery("Reports.getAllReports");

		retval = (List<Report>) query.list();
		closeNewSession(newSession); //detached
		return retval;
	}
	
	@Override
	public Report getReport(long reportId) {
		Report retval = null;
		Session session = this.getSession();
		Query query = session.getNamedQuery("Reports.getReport");
		query.setParameter("reportId", reportId);

		retval =  (Report) query.uniqueResult();
		return retval;
	}
	
	@Override
	public Report getReportDetached(long reportId) {
		Report retval = null;
		Session newSession = getNewSession(); //attached
		Query query = newSession.getNamedQuery("Reports.getReport");
		query.setParameter("reportId", reportId);

		retval =  (Report) query.uniqueResult();
		closeNewSession(newSession); //detached
		return retval;
	}
	
}
