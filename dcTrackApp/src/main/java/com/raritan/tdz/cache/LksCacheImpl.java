package com.raritan.tdz.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;

public class LksCacheImpl implements LksCache {

	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;

	private Map<Long, LksData> portLkpValueCodeLksData = new HashMap<Long, LksData>();

	private Map<String, LksData> voltsLksData = new HashMap<String, LksData>();
	
	@Transactional
	@Override
	public LksData getLksDataUsingLkpCode(Long lkpValueCode) {
		
		LksData lksData = portLkpValueCodeLksData.get(lkpValueCode);
		if (null == lksData) {
			lksData = systemLookupFinderDAO.findByLkpValueCode(lkpValueCode).get(0);
			portLkpValueCodeLksData.put(lkpValueCode, lksData);
		}
		
		return lksData;
	}


	@Transactional
	@Override
	public LksData getLksDataUsingLkpAndType(String lkpValue, String type) {
		LksData voltLksData = voltsLksData.get(lkpValue + ":" + type);
		if (null == voltLksData) {
			List<LksData> voltsLksList =  systemLookupFinderDAO.findByLkpValueAndType(lkpValue, type);
			if (null != voltsLksList && voltsLksList.size() == 1) {
				voltLksData = voltsLksList.get(0);
			}
			voltsLksData.put(lkpValue + ":" + type, voltLksData);
		}

		return voltLksData;
	}

}
