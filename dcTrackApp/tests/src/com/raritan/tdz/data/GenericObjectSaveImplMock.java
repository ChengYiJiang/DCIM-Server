package com.raritan.tdz.data;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

public class GenericObjectSaveImplMock implements GenericObjectSave {
	@Autowired
	protected UnitTestDatabaseIdGenerator unitTestIdGenerator;

	private String lastRequestNo = null;
	
	@Override
	public Long save(Object mockObject) {
		Long id = unitTestIdGenerator.nextId();
		
		if(mockObject instanceof Item){
			((Item)mockObject).setItemId(id);
			return id;
		}

		if(mockObject instanceof Request){
			((Request)mockObject).setRequestId(id);
			return id;
		}

		if(mockObject instanceof ICircuitInfo){
			((ICircuitInfo)mockObject).setCircuitId(id);
			return id;
		}

		return id;
	}
	
	@Override
	public DataCenterLocationDetails getTestLocation()  {			
		DataCenterLocaleDetails x = new DataCenterLocaleDetails();
		x.setCountry("UNITED STATES");
		x.setDcLocaleDetailsId(100L);

		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setCode("UnitTestDC");
		location.setDcName("UnitTestDC");
		location.setArea(1000L);
		location.setDataCenterLocationId(100L);
		location.setDcLocaleDetails(x);
		
		return location;
	}

	@Override
	public String getNextRequestNo() {
    	if(lastRequestNo == null){
    		String date = java.util.Calendar.getInstance().getTime().toString();
    		lastRequestNo = date.substring(date.length() - 2) + "00001";
    	}
    	else{
    		lastRequestNo += 1;
    	}
    	
	    return lastRequestNo;
	}

	@Override
	public void update(Object dbObject) {
		// TODO Auto-generated method stub
		
	}
		
}
