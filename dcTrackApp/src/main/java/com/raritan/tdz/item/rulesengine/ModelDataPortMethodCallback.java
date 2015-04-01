/**
 * 
 */

package com.raritan.tdz.item.rulesengine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author basker
 *
 */

public class ModelDataPortMethodCallback implements RemoteRefMethodCallback {

	@Autowired
	private ModelHome modelHome;
	

	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {
		// set Data port DTO  
		if ((Long)filterValue > 0){
			List<DataPortDTO> dataPortDTOList = modelHome.getAllDataPort((Long)filterValue); 
			uiViewComponent.getUiValueIdField().setValue(dataPortDTOList);
		}
	}
}
