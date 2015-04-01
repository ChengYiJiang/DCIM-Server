package com.raritan.tdz.floormaps.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.*;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.floormaps.dto.CadHandleDTO;

import org.springframework.stereotype.Service;

@Service
public interface CadService {

    public CadHandleDTO getCadHandles(String locationid) throws DataAccessException;
    public int setCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException;
    public CadHandleDTO syncCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException;
    
    public boolean updateLocation(String locationid,String filePath) throws DataAccessException;
    public Map getParameters(String locationid) throws DataAccessException;
	public Map getPIQLocationInfo(String locationid) throws DataAccessException;
    
}
