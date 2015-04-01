package com.raritan.tdz.piq.integration;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;

public interface DataEnhancer {
	
	public List<String> enhance(List<Item> items) throws DataAccessException, RemoteDataAccessException, Exception;

}

