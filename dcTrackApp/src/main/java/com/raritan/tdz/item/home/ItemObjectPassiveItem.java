package com.raritan.tdz.item.home;

import java.util.List;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @see ItemObject
 */
public interface ItemObjectPassiveItem {

    /**
     * Save a passive item.
     * 
     * @param itemId
     * @param dtoList
     *          ValueIdDTO list
     * @param userInfo
     * @return item id
     * @throws ClassNotFoundException
     * @throws BusinessValidationException
     * @throws Throwable
     */
    public Long saveItem(Long itemId,
            List<ValueIdDTO> dtoList, UserInfo userInfo)
            throws ClassNotFoundException, BusinessValidationException,
            Throwable;

    /**
     * Delete a passive item.
     * 
     * @param itemId
     * @throws Throwable
     */
    public void deleteItem(Long itemId) throws Throwable;
}