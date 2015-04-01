package com.raritan.tdz.settings.service;

import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.settings.dto.PiqSettingDTO;

public interface PIQInfoService {
	public PiqSettingDTO getPIQVersion(PiqSettingDTO piqSettingDTO) throws RemoteDataAccessException;
}
