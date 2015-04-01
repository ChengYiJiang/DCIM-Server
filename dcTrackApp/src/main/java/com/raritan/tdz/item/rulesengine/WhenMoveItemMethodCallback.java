package com.raritan.tdz.item.rulesengine;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class WhenMoveItemMethodCallback implements RemoteRefMethodCallback {

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiData, java.util.Map, com.raritan.tdz.rulesengine.RemoteRefAttributes)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable {
		
		Long whenMovedItemId = powerPortMoveDAO.getWhenMovedItemId((Long)filterValue);
		
		uiViewComponent.getUiValueIdField().setValue(whenMovedItemId);
		
	}

}
