/**
 * 
 */
package com.raritan.tdz.rulesengine;



import com.raritan.dctrack.xsd.UiComponent;
import java.util.Map;


/**
 * @author prasanna
 * This will be used when an external method need to be called to fill the
 * Lookup/value fields
 */
public interface RemoteRefMethodCallbackUsingFilter {
	/**
	 * This will fill in the data. The additonalData may contain any additonal data
	 * required to process the call back. For example this may include a database ID.
	 * The implementation should know pre-hand as to what this additonal data is and
	 * cast it to required object appropriately. 
	 * @param uiViewCompoent
	 * @param filterField TODO
	 * @param filterValue TODO
	 * @param operator TODO
	 * @param remoteRef TODO
	 * @param additionalArgs TODO
	 */
	public void fillValue(UiComponent uiViewCompoent, Map<String, Filter> filterMap, RemoteRef remoteRef, Object additionalArgs) throws Throwable;
}
