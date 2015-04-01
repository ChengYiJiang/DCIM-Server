package com.raritan.tdz.data;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;

public class GenericObjectSaveImplDB implements GenericObjectSave {
	private SessionFactory sessionFactory;
	
	public GenericObjectSaveImplDB(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Long save(Object dbObject) {
		Session session = sessionFactory.getCurrentSession();
		
		Long id = (Long)session.save(dbObject);
		
		session.flush();
		
		return id;
	}

	@Override
	public void update(Object dbObject) {
		Session session = sessionFactory.getCurrentSession();
		
		session.update(dbObject);
		session.flush();
	}

	@Override
	public DataCenterLocationDetails getTestLocation()  {	
		Session session = sessionFactory.getCurrentSession();
		
		DataCenterLocaleDetails x = new DataCenterLocaleDetails();
		x.setCountry("UNITED STATES");

		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setCode("UnitTestDC");
		location.setDcName("UnitTestDC");
		location.setArea(1000L);
		location.setDcLocaleDetails(x);
		
		session.save(location);
		
		return location;
	}
	
	@Override
	public String getNextRequestNo() {
		String requestNo = getCurrentRequestNo();
		if (requestNo != null) {
			requestNo = String.valueOf( Long.valueOf(requestNo) + 1);
		}
		return requestNo;
	}
	
	private String getCurrentRequestNo() {
		String requestNo = "";
    	
    	Session session = sessionFactory.getCurrentSession();

        //Get the last request number
        Query q = session.createSQLQuery("select r.requestNo from tblRequest r where substring(r.requestNo from 1 for 2) = to_char(current_date,'YY')  order by 1 desc ");

        List recList = q.list();

        //format for request number is YY#####, where YY is year and # is a digit
	    if(recList != null && recList.size() > 0){
	    	String r = (String)recList.get(0);
	    	String t[] = r.split("-");
	    	
	    	requestNo = t[0];
	    }
	    else {
	    	String date = java.util.Calendar.getInstance().getTime().toString();
	    	requestNo = date.substring(date.length() - 2) + "00001";
	    }

	    //increment request number by 1
	    requestNo = String.valueOf((Long.valueOf(requestNo)));

	    return requestNo;
	}
	
}
