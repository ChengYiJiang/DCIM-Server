/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.raritan.dctrack.xsd.DcTrack;

/**
 * @author prasanna
 *
 */
public interface RulesNodeEditability {
	public void setNodeEditable(DcTrack dcTrack) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	public List<String> getEditableNodes();
	public List<String> getNonEditableNodes();
	public void mergeNodeEditable(RulesNodeEditability mergeRulesNodeEditability);
}
