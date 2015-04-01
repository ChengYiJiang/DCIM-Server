/**
 * 
 */

package com.raritan.tdz.item.rulesengine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author basker
 *
 */

public class ModelPowerPortMethodCallback implements RemoteRefMethodCallback {
	
	@Autowired
	private ModelHome modelHome;
	
	
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {
		if ((Long)filterValue > 0){
			// set power port DTO  
			List<PowerPortDTO> powerPortDTOList = modelHome.getAllPowerPort((Long)filterValue); 
			uiViewComponent.getUiValueIdField().setValue(powerPortDTOList);
		}
	}
}
