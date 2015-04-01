/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;

import com.raritan.dctrack.xsd.DcTrack;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

/**
 * @author prasanna
 *
 */
public class ItemUserRoleEditability implements RulesNodeEditability {

	//Note: This will be used
	//      just until we get some sort of a database table that could have this kind-of information
	// The string part is an xpath to the node and the boolean is true or false for editable/editabile
	Map<String, Boolean> editabilityMap;
	
	ItemUserRoleEditability(Map<String,Boolean> editabilityMap){
		this.editabilityMap = editabilityMap;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesNodeEditability#setNodeEditable(com.raritan.dctrack.xsd.DcTrack)
	 */
	@Override
	public void setNodeEditable(DcTrack dcTrack) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		//Go through the entire map and set the appropriate boolean values for the given xpath
		//Make sure you check the current setting in the dcTrack object and perform and operation
		//to get the right condition.
		JXPathContext jc = JXPathContext.newContext(dcTrack);
		
		for (Map.Entry<String, Boolean> entry : editabilityMap.entrySet()){
			String xPath = entry.getKey();
			Boolean value = entry.getValue();
			
			Class<?> xsdClass = jc.getValue(xPath).getClass();
			Method isEditableMethod = xsdClass.getMethod("isEditable", null);
			Method setEditableMethod = xsdClass.getMethod("setEditable", Boolean.class);
			
			Boolean existingValue = (Boolean)(isEditableMethod.invoke(jc.getValue(xPath), null));
			Boolean newValue = existingValue && value;
			setEditableMethod.invoke(jc.getValue(xPath), newValue);
		}

	}
	
	@Override
	public List<String> getEditableNodes() {
		List<String> xPaths = new ArrayList<String>();
		for (Map.Entry<String, Boolean> entry : editabilityMap.entrySet()){
			String xPath = entry.getKey();
			Boolean value = entry.getValue();
			
			if (value)
				xPaths.add(xPath);
		}
		
		return xPaths;
	}

	@Override
	public List<String> getNonEditableNodes() {
		List<String> xPaths = new ArrayList<String>();
		for (Map.Entry<String, Boolean> entry : editabilityMap.entrySet()){
			String xPath = entry.getKey();
			Boolean value = entry.getValue();
			
			if (!value)
				xPaths.add(xPath);
		}
		
		return xPaths;
	}

	@Override
	public void mergeNodeEditable(RulesNodeEditability mergeRulesNodeEditability) {
		List<String> mergeNonEditableNodes = mergeRulesNodeEditability.getNonEditableNodes();
		List<String> myEditableNodes = this.getEditableNodes();
		
		for (String nonEitableNode: mergeNonEditableNodes) {
			if (myEditableNodes.contains(nonEitableNode)) {
				editabilityMap.put(nonEitableNode, false);
			}
		}
	}

}
