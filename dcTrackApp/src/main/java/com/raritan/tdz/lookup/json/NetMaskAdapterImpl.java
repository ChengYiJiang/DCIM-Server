package com.raritan.tdz.lookup.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.dao.NetMaskDAO;
import com.raritan.tdz.ip.domain.NetMask;

public class NetMaskAdapterImpl implements NetMaskAdapter {
	@Autowired(required=true)
	private NetMaskDAO netMaskDAO;

	private ResourceBundleMessageSource messageSource;

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getNetMaskByIdAPI(Long id)
			throws BusinessValidationException {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		List<NetMask> netMasksList = null;
		if( id != null ) netMasksList = netMaskDAO.getById(id);
		if( netMasksList.size() > 0 ){
			ret.put("netmasks", netMasksList);
		}
		return ret;	
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getNetMaskByMaskAPI(String mask)
			throws BusinessValidationException {
		Map<String, Object> ret = new HashMap<String, Object>();		
		List <NetMask> netMasksList = null;
		
		if( mask != null ) netMasksList = netMaskDAO.getByMask(mask);
		if( netMasksList.size() > 0 ){
			ret.put("netmasks", netMasksList);
		}
		return ret;
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getNetMaskByCidrAPI(Long cidr)
			throws BusinessValidationException {
		Map<String, Object> ret = new HashMap<String, Object>();		
		List <NetMask> netMasksList = null;
		
		if( cidr != null ) netMasksList = netMaskDAO.getByCidr(cidr);
		if( netMasksList.size() > 0 ){
			ret.put("netmasks", netMasksList);
		}
		return ret;
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getAllNetMasks() throws BusinessValidationException {
		Map<String, Object> ret = new HashMap<String, Object>();		
		List <NetMask> netMasksList = null;
		
		netMasksList = netMaskDAO.getAll();
		if( netMasksList.size() > 0 ){
			ret.put("netmasks", netMasksList);
		}
		return ret;
	}

}
