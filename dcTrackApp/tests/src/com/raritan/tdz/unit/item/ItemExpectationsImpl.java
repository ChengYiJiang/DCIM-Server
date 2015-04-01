package com.raritan.tdz.unit.item;

import java.util.List;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import com.raritan.tdz.item.dao.ItemDAO;


public class ItemExpectationsImpl implements ItemExpectations {
	@Autowired
	ItemDAO itemDAO;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetItemXX(org.jmock.Mockery, org.springframework.validation.Errors, java.lang.Long, java.lang.Object)
	 */
	@Override
	public void createGetItemXX(Mockery jmockContext, final Errors errors, final Long itemId, final Object retValue){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(retValue));
		}});
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetItem(org.jmock.Mockery, java.lang.Long, java.lang.Object)
	 */
	@Override
	public void createGetItem(Mockery jmockContext,  final Long itemId, final Object retValue){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(retValue));
		}});
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createReadItem(org.jmock.Mockery, java.lang.Long, java.lang.Object)
	 */
	@Override
	public void createReadItem(Mockery jmockContext,  final Long itemId, final Object retValue){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).read(with(itemId)); will(returnValue(retValue));
		}});
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetItemIdsToDelete(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createGetItemIdsToDelete(Mockery jmockContext,  final Long itemId, final List<Long> idsToDelete){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getItemIdsToDelete(with(itemId)); will(returnValue(idsToDelete));
		}});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createOneOfGetItemIdsToDelete(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createOneOfGetItemIdsToDelete(Mockery jmockContext,  final Long itemId, final List<Long> recList){
		jmockContext.checking(new Expectations() {{			
			oneOf(itemDAO).getItemIdsToDelete(with(itemId)); will(returnValue(recList));
		}});
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetItemsToDeleteInvalidStages(org.jmock.Mockery, java.util.List, java.util.List)
	 */
	@Override
	public void createGetItemsToDeleteInvalidStages(Mockery jmockContext,  final List<Long> idsList, final List<Long> recList){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getItemsToDeleteInvalidStages (with(idsList)); will(returnValue(recList));
		}});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetFreeStandingItemIdForItem(org.jmock.Mockery, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void createGetFreeStandingItemIdForItem(Mockery jmockContext,  final Long itemId, final Long retValue){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getFreeStandingItemIdForItem (with(itemId)); will(returnValue(retValue));
		}});
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetItemToDeleteConnected(org.jmock.Mockery, java.util.List, java.util.List)
	 */
	@Override
	public void createGetItemToDeleteConnected(Mockery jmockContext,  final List<Long> idsList, final List<String> recList){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getItemToDeleteConnected(with(idsList)); will(returnValue(recList));
		}});
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetFPDUItemToDeleteConnected(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createGetFPDUItemToDeleteConnected(Mockery jmockContext,  final Long itemId, final List<String> recList){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getFPDUItemToDeleteConnected(with(itemId)); will(returnValue(recList));
		}});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemExpectations#createGetPowerPanelItemToDeleteConnected(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createGetPowerPanelItemToDeleteConnected(Mockery jmockContext,  final Long itemId, final List<String> recList){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getPowerPanelItemToDeleteConnected (with(itemId)); will(returnValue(recList));
		}});
	}	
	
	@Override
	public void createGetItemName(Mockery jmockContext,  final Long itemId, final String retValue){
		jmockContext.checking(new Expectations() {{			
			allowing(itemDAO).getItemName (with(itemId)); will(returnValue(retValue));
		}});
	}	
}
