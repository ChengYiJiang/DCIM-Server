package com.raritan.tdz.lookup.json;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


public class LookupAdapterImpl implements LookupAdapter {

	@Autowired(required=true)
	private UserLookupFinderDAO userLookup;

	private ResourceBundleMessageSource messageSource;

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getLkuByIdAPI(Long id) throws BusinessValidationException {
		Map<String, Object> ret = new HashMap<String, Object>();		
		List<LkuData> lkuDataList = userLookup.findById(id);

		if( lkuDataList.size() > 0 ){
			ret.put("lkus", lkuDataList);
		}

		return ret;
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getLkuByTypeAPI(String lkuType) throws BusinessValidationException {
		Map<String, Object> ret = new HashMap<String, Object>();		
		List <LkuData> lkuDataList = null;
		
		if( lkuType != null ) lkuDataList = userLookup.findByLkpType(lkuType);
		if( lkuDataList.size() > 0 ){
			ret.put("lkus", lkuDataList);
		}
		return ret;
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getLkuByValueAPI(String lkuValue) throws BusinessValidationException{

		Map<String, Object> ret = new HashMap<String, Object>();		
		List <LkuData> lkuDataList = null;

		if( lkuValue != null ) lkuDataList = userLookup.findByLkpValue(lkuValue);
		if( lkuDataList.size() > 0 ){
			ret.put("lkus", lkuDataList);
		}

		return ret;
	}

}
