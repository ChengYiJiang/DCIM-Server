package com.raritan.tdz.floormaps.service;

import java.io.*;
import java.util.*;
import java.net.*;


import org.apache.log4j.Logger;
import com.raritan.tdz.exception.*;
import com.raritan.tdz.floormaps.home.CadHome;
import com.raritan.tdz.floormaps.dto.CadHandleDTO;
import com.raritan.tdz.lookup.SystemLookup;

//import org.springframework.stereotype.Service;

//import org.springframework.beans.factory.annotation.Autowired;

public class CadServiceImpl implements CadService {

	private Logger log = Logger.getLogger(this.getClass());
    private CadHome cadhome;

	//Default constructor
	public CadServiceImpl(CadHome home) {
	    cadhome = home;
	}

    public CadHandleDTO getCadHandles(String locationid) throws DataAccessException {
        CadHandleDTO cadHandleDTO = null;
        cadHandleDTO = cadhome.getCadHandles(locationid);
        return cadHandleDTO;
    }
    
    public int setCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException{
        return cadhome.setCadHandles(cadHandleDTO);
    }
    
    public CadHandleDTO syncCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException{
    	return cadhome.syncCadHandles(cadHandleDTO);
    }
        
    public boolean updateLocation(String locationid,String filePath) throws DataAccessException {
    	return cadhome.updateLocation(locationid,filePath);
    }
    
    public Map getParameters(String locationid) throws DataAccessException {
    	return cadhome.getParameters(locationid);
    }
    
	public Map getPIQLocationInfo(String locationid) throws DataAccessException {
		return cadhome.getPIQLocationInfo(locationid);
	}
}
