package com.raritan.tdz.item.rulesengine;

import org.springframework.util.Assert;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class FpduWiringCallback implements RemoteRefMethodCallback {
	private ItemDAO itemDAO;
	final String  CONSTANT_FPDU_INPUT_WIRING_OF_ITEM= "tiFPDUInputWiring";
	final String  CONSTANT_FPDU_OUTPUT_WIRING_OF_ITEM= "cmbFPDUOutputWiring";
	
	public FpduWiringCallback( ItemDAO itemDAO ){
		this.itemDAO = itemDAO;
	}
	
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {
		
		Long itemId = (Long)filterValue;
		Assert.isTrue(itemId > 0);
		Item item = itemDAO.getItem(itemId);
		if((item instanceof  MeItem) == false ) return;	
		
		MeItem fpduItem = (MeItem) item; 

		if( uiViewComponent.getUiId().equals(CONSTANT_FPDU_INPUT_WIRING_OF_ITEM)){
			fillInputWiringValue( fpduItem, uiViewComponent);
		}
		
		if ( uiViewComponent.getUiId().equals( CONSTANT_FPDU_OUTPUT_WIRING_OF_ITEM ) ) {
			fillOutputWiringValue( fpduItem, uiViewComponent);
		}
	}
	
	private void fillInputWiringValue( MeItem fpduItem, UiComponent uiViewComponent){
		if (fpduItem.getPhaseLookup() != null) {
			if( fpduItem.getPhaseLookup().getLkpValueCode() == SystemLookup.PhaseIdClass.THREE_DELTA ){
				uiViewComponent.getUiValueIdField().setValue("3-Wire + Ground");
			}else if( fpduItem.getPhaseLookup().getLkpValueCode() == SystemLookup.PhaseIdClass.THREE_WYE){
				uiViewComponent.getUiValueIdField().setValue("4-Wire + Ground");
			}
		}
	}
	
	private void fillOutputWiringValue( MeItem fpduItem, UiComponent uiViewComponent) {
		
		Long numOfPanels = itemDAO.getNumOfChildren(fpduItem);
		if (0 == numOfPanels) {
			uiViewComponent.getUiValueIdField().setValue(null);
			uiViewComponent.getUiValueIdField().setValueId(null);
			return;
		}
		
		Long numOfChildrenWithDeltaPhase = itemDAO.getNumOfChildWithDeltaPhase(fpduItem.getItemId());
		if (numOfChildrenWithDeltaPhase > 0) {
			uiViewComponent.getUiValueIdField().setValue("3-Wire + Ground");
			uiViewComponent.getUiValueIdField().setValueId(SystemLookup.PhaseIdClass.THREE_DELTA);
			return;
		}

		uiViewComponent.getUiValueIdField().setValue("4-Wire + Ground");
		uiViewComponent.getUiValueIdField().setValueId(SystemLookup.PhaseIdClass.THREE_WYE);
	}
	
}

