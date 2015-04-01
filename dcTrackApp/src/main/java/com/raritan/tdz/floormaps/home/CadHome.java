package com.raritan.tdz.floormaps.home;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.*;
import com.raritan.tdz.floormaps.dto.CadHandleDTO;

/**
 * 
 * @author Brian Wang
 */
public interface CadHome {
		
	@Transactional(readOnly = true)
	public CadHandleDTO getCadHandles(String locationid) throws DataAccessException;

	@Transactional(readOnly = false)
	public int setCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException;
	
	@Transactional(readOnly = false)
    public boolean updateLocation(String locationid,String filePath) throws DataAccessException;

	@Transactional(readOnly = false)
	public CadHandleDTO syncCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException;
	
	@Transactional(readOnly = false)
    public void syncCadHandleByItem(Item item) throws DataAccessException;
	
	@Transactional(readOnly = true)
	public Map getParameters(String locationid) throws DataAccessException;

	@Transactional(readOnly = true)
	public Map getPIQLocationInfo(String locationid) throws DataAccessException;
	
}
