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
public class PortNamePrefixMethodCallback implements RemoteRefMethodCallback {

	@Autowired
	PowerPortDAO powerPortDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef, java.lang.Object)
	 */
	@Override
	public void fillValue(UiComponent uiViewCompoent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {
/*		List<PowerPort> powerPorts = powerPortDAO.getPortsForItem((Long)filterValue);
		List<String> portNames = new ArrayList<String>();
		
		for (PowerPort powerPort:powerPorts){
			portNames.add(powerPort.getPortName());
		}*/
		
		List<String> portNames = powerPortDAO.getPortsNameForItem((Long) filterValue);
		
		String commonPrefix = getCommonPrefix(portNames);
		
		uiViewCompoent.getUiValueIdField().setValue(commonPrefix);
		uiViewCompoent.getUiValueIdField().setValueId(commonPrefix);
	}
	
	// returns the length of the longest common prefix of all strings in the given array 
	private String getCommonPrefix(List<String> strings) {
		String commonPrefix = null;
		List<String> namePrefixes = new LinkedList<String>();
		Boolean namePrefixUpdated = null;
		for (String string: strings){
			List<String> splitStr = parse(string);
			if (null == splitStr || splitStr.size() == 0) {
				commonPrefix = null;
				return commonPrefix;
			}
			commonPrefix = splitStr.get(0);
			if (null == namePrefixUpdated) {
				namePrefixes.add(commonPrefix);
				namePrefixUpdated = true;
			}
			if (!namePrefixes.contains(splitStr.get(0))) {
				commonPrefix = null;
			}
		}
		
		return commonPrefix;
	}
	
	// private static final Pattern VALID_PATTERN = Pattern.compile("[0-9]+|[A-Z]+");
	private static final Pattern VALID_PATTERN = Pattern.compile("\\d+|\\D+");

	private List<String> parse(String toParse) {
	    List<String> chunks = new LinkedList<String>();
	    Matcher matcher = VALID_PATTERN.matcher(toParse);
	    while (matcher.find()) {
	        chunks.add( matcher.group() );
	    }
	    return chunks;
	}

}
