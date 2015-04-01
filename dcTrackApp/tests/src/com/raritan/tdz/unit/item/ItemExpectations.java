package com.raritan.tdz.unit.item;

import java.util.List;

import org.jmock.Mockery;
import org.springframework.validation.Errors;

public interface ItemExpectations {

	public abstract void createGetItemXX(Mockery jmockContext, Errors errors,
			Long itemId, Object retValue);

	public abstract void createGetItem(Mockery jmockContext, Long itemId,
			Object retValue);

	public abstract void createReadItem(Mockery jmockContext, Long itemId,
			Object retValue);

	public abstract void createGetItemIdsToDelete(Mockery jmockContext,
			Long itemId, List<Long> idsToDelete);

	public abstract void createOneOfGetItemIdsToDelete(Mockery jmockContext,
			Long itemId, List<Long> recList);

	public abstract void createGetItemsToDeleteInvalidStages(
			Mockery jmockContext, List<Long> idsList, List<Long> recList);

	public abstract void createGetFreeStandingItemIdForItem(
			Mockery jmockContext, Long itemId, Long retValue);

	public abstract void createGetItemToDeleteConnected(Mockery jmockContext,
			List<Long> idsList, List<String> recList);

	public abstract void createGetFPDUItemToDeleteConnected(
			Mockery jmockContext, Long itemId, List<String> recList);

	public abstract void createGetPowerPanelItemToDeleteConnected(
			Mockery jmockContext, Long itemId, List<String> recList);

	public void createGetItemName(Mockery jmockContext, Long itemId, String retValue);

}