package com.raritan.tdz.item.home.itemObject;


import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.home.FPDUBreakerToUPSBankBreakerUpdateActionHandler;
import com.raritan.tdz.powerchain.home.FloorPduBreakerPortToPanelBreakerUpdateConnectionActionHandler;
import com.raritan.tdz.powerchain.home.FloorPduBreakerPortUpdateValueActionHandler;
import com.raritan.tdz.powerchain.home.FloorPduDeleteConnectionActionHandler;


public class FloorPDUSaveBehavior implements ItemSaveBehavior {

	private static Logger log = Logger.getLogger(FloorPDUSaveBehavior.class);

	@Autowired(required=true)
	ItemDAO itemDAO;

	@Autowired
	ItemFinderDAO itemFinderDAO;

	@Autowired
	FPDUBreakerToUPSBankBreakerUpdateActionHandler floorPduBreakerPortToUpsUpdateConnectionActionHandler;

	@Autowired
	FloorPduBreakerPortToPanelBreakerUpdateConnectionActionHandler floorPduBreakerPortToPanelBreakerUpdateConnectionActionHandler;

	@Autowired
	FloorPduBreakerPortUpdateValueActionHandler floorPduBreakerPortUpdateValueActionHandler;

	@Autowired
	FloorPduDeleteConnectionActionHandler  floorPduDeleteConnectionActionHandler;

	@Autowired
	PowerPortDAO powerPortDAO;

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		Assert.isTrue( floorPduBreakerPortToUpsUpdateConnectionActionHandler  != null );

		MeItem fpdu = (MeItem)item;
		itemDAO.initPowerPortsAndConnectionsProxy(fpdu);
		MeItem tmpFpdu = (MeItem) item;
		MeItem upsBank = tmpFpdu.getUpsBankItem();
		MapBindingResult errors = (MapBindingResult)additionalArgs[0];

		// Associate a UPS Bank to the Floor PDU
		if( upsBank != null && fpdu.getBreakerPortId() == null) {
			fpdu.setLineVolts((long)upsBank.getRatingV());
			fpdu.setPhaseLookup(upsBank.getPhaseLookup()); //input wiring
			boolean validateConn = true;
			boolean migrationInProgress = false;

			itemDAO.initPowerPortsAndConnectionsProxy(upsBank);

			// ups bank's rating_v propagates to the fpdu. Power chain will further propagate it to ports
			floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(fpdu, upsBank, errors, validateConn, migrationInProgress);
			floorPduBreakerPortUpdateValueActionHandler.update(fpdu, errors);

			int count = itemDAO.setPanelUPSBankId(fpdu.getItemId(), upsBank.getItemId());
			log.debug("1. Total of " + count + " panels are updated with upsBank id " + upsBank.getItemId() );
		}

		// Associate a Branch Circuit Breaker to the Floor PDU
		else if ( fpdu.getBreakerPortId() != null ) {
			Long breakerPortId = fpdu.getBreakerPortId();
			PowerPort port = powerPortDAO.loadPort(breakerPortId);
			if (upsBank != null ) fpdu.setUpsBankItem(upsBank);
			// set fpdu volts and phase is assigned from branch circuit breaker port.
			if (port != null && port.getVoltsLookup() != null) fpdu.setLineVolts(new Double(port.getVoltsLookup().getLkpValue()));
			fpdu.setPhaseLookup(port.getPhaseLookup()); //input wiring

			if ( breakerPortId > 0 ) {
				boolean validateConn = true;
				boolean migrationInProgress = false;
				floorPduBreakerPortToPanelBreakerUpdateConnectionActionHandler.process(fpdu, breakerPortId, errors, validateConn, migrationInProgress);
				floorPduBreakerPortUpdateValueActionHandler.update(fpdu, errors);
			}
			if (null != upsBank) {
				int count = itemDAO.setPanelUPSBankId(fpdu.getItemId(), upsBank.getItemId());
				log.debug("2. Total of " + count + " panels are updated with upsBank id " + upsBank.getItemId() );
			} else {
				throw new IllegalArgumentException("Set UPS bank for this FloorPDU connection.");
			}
		}

		// delete all associations to the floor pdu
		else if (upsBank == null && fpdu.getBreakerPortId() == null) {
			// user has not selected ups or breaker for this FPDU, remove connection.
			floorPduDeleteConnectionActionHandler.process(fpdu, errors);
		}

	}

	private void updateLocalPowerPanelLocation(Item item){
		assert( SavedItemData.getCurrentItem() != null );
		Item origItem = SavedItemData.getCurrentItem().getSavedItem();
		if( origItem.getDataCenterLocation() != item.getDataCenterLocation()
				&& (item.getStatusLookup().getLkpValueCode().longValue() == SystemLookup.ItemStatus.PLANNED ||
				item.getStatusLookup().getLkpValueCode().longValue() == SystemLookup.ItemStatus.IN_STORAGE )){

			List<Item> childItems = itemFinderDAO.findChildItemsFromParentId(item.getItemId());
			if( childItems != null ) for( Item i : childItems ){
				if( item.getSubclassLookup() == null && i.getSubclassLookup() != null &&
						i.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.LOCAL ){
					i.setDataCenterLocation(item.getDataCenterLocation());
				}

				if( log.isDebugEnabled()){
					log.debug("  FPDU child: " + origItem.getItemId() + ", old location=" + origItem.getDataCenterLocation().getDataCenterLocationId()
							+ ", new location=" + i.getDataCenterLocation().getDataCenterLocationId());
				}
			}
		}		
	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {
		boolean isUpdate = SavedItemData.getCurrentItem() != null ? true : false;
		//If location changed, and item is in planned or storage state
		//propagate new location to local power panel
		if( isUpdate ) updateLocalPowerPanelLocation(item);

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(MeItem.class.getName())){
				canSupport = true;
			}
		}
		return canSupport;

	}

}
