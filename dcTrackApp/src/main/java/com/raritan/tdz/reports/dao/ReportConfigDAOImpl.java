package com.raritan.tdz.reports.dao;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.reports.domain.ReportConfig;

public class ReportConfigDAOImpl extends DaoImpl<ReportConfig> implements ReportConfigDAO {


	public ReportConfig loadReportConfig(long reportConfigId) {
		Session session = this.getNewSession();
		Criteria criteria = session.createCriteria(ReportConfig.class);
		criteria.add(Restrictions.eq("reportConfigId", reportConfigId));
		criteria.createAlias("reportConfig", "reportConfig", Criteria.LEFT_JOIN);
			
		ReportConfig result = (ReportConfig)criteria.uniqueResult();
		
		session.close();
		
		return result;
	}
		
	
}
