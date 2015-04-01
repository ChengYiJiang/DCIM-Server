/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author prasanna
 *
 */
public class ItemPropagateValueCallback implements RemoteRefMethodCallback {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef)
	 */
	@Override
	public void fillValue(UiComponent uiViewCompoent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {
		Item item = new Item();
		uiViewCompoent.getUiValueIdField().setValue(item.getPropagateFields());
	}

}
