package com.raritan.tdz.item.home;

import java.util.List;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dto.CloneItemDTO;

public interface ItemClone {
	public boolean isClonable(Long classLkpVC, Long subClassLkpVC);

	public Long clone(CloneItemDTO recDTO, UserInfo userInfo, ItemHome itemHome)
			throws DataAccessException, BusinessValidationException;

	public List<CloneItemDTO> removeChildren(List<CloneItemDTO> recList)
			throws DataAccessException;
}
