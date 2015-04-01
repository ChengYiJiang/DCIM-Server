/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;


/**
 * @author prasanna
 *
 */
public class PowerPortQuantityMethodCallback implements RemoteRefMethodCallback {

	@Autowired
	PowerPortDAO powerPortDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef, java.lang.Object)
	 */
	@Override
	public void fillValue(UiComponent uiViewCompoent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {
		// List<PowerPort> powerPorts = powerPortDAO.getPortsForItem((Long)filterValue);
		Long numOfPorts = powerPortDAO.getNumOfPortForItem((Long)filterValue);
		
		if (numOfPorts != null){
			uiViewCompoent.getUiValueIdField().setValue(numOfPorts);
			uiViewCompoent.getUiValueIdField().setValueId(numOfPorts);
		} else {
			uiViewCompoent.getUiValueIdField().setValue(0L);
			uiViewCompoent.getUiValueIdField().setValueId(0L);
		}
	}


}
