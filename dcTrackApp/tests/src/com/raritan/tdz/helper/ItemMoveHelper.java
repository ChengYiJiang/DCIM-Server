package com.raritan.tdz.helper;

import java.util.LinkedList;
import java.util.List;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.SaveUtils;
import com.raritan.tdz.lookup.SystemLookup;

public class ItemMoveHelper extends TestHelperBase {

  	public ItemMoveHelper(ItemHome itemHome, UserInfo userInfo) {
  		
		super(itemHome, userInfo);
	}

	public List<ValueIdDTO> getItemSaveFields(String itemName, long classLksId, long statusLksId) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();

  		fields.addAll( SaveUtils.addItemField("cmbStatus", statusLksId));
  		fields.addAll( SaveUtils.addItemIdentityFields(itemName, null) );
  		
  		switch((int)classLksId){
  		case 1:
  			fields.addAll( SaveUtils.addItemHardwareFields(20L, 1L) ); //mfr_id=20, model_id=1
  	  		fields.addAll( SaveUtils.addRackablePlacementFields(
						1L,		 						// Site A
						10L,							// Cabinet 1H
						SystemLookup.RailsUsed.BOTH		// Both Rails Used
					));
  	  		fields.addAll( SaveUtils.addItemField("cmbUPosition", 2) );
  			
  			break;
  		case 6:
  			fields.addAll( SaveUtils.addItemHardwareFields(12L, 24L) ); //mfr_id=12, model_id=24
  			break;
  		default:
  			fields.addAll( SaveUtils.addItemHardwareFields(20L, 1L) ); //mfr_id=20, model_id=1
  			break;
  		}
  		
  		return fields;
  	}	

}
