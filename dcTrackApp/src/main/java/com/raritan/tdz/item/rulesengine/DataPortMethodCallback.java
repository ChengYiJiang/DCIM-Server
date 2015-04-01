/**
 *
 */

package com.raritan.tdz.item.rulesengine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.DataPortDTOHelper;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
/**
 * @author Bunty Nasta
 *
 */
public class DataPortMethodCallback implements RemoteRefMethodCallback {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private DataPortDTOHelper dataPortDTOHelper;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {

		Item item = itemDAO.read((Long) filterValue); //(Item) session.get(Item.class, (Long) filterValue);

		List<DataPortDTO> dataPortDTOList = dataPortDTOHelper.getPortDTOList(item);
		
		uiViewComponent.getUiValueIdField().setValue(dataPortDTOList);
		
	}
	
}
