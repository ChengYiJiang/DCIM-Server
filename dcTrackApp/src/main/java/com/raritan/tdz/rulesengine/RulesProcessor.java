/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.type.Type;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiView;

/**
 * @author prasanna
 * Rules processor that handles the rules-engine processing on the server
 * side.
 */
public interface RulesProcessor {
	/**
	 * Configure the rules processor given the template name
	 * @param fileName
	 * @throws JAXBException
	 * @throws HibernateException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void configure(String fileName) throws JAXBException, HibernateException, ClassNotFoundException, IOException;
	
	/**
	 * Get the xmlData output that needs to be sent to the client.
	 * @param xPath
	 * @param filterField
	 * @param filterValue
	 * @param operator
	 * @param type
	 * @return
	 * @throws Throwable 
	 */
	public String getXMLData(String xPath, String filterField, Object filterValue, String operator, Type type, String unit) 
			throws Throwable;
	
	/**
	 * Get the data in UiView that will be used as a DTO sent to the client.
	 * @param xPath
	 * @param filterField
	 * @param filterValue
	 * @param operator
	 * @param type
	 * @return
	 * @throws Throwable 
	 */
	public UiView getData(String xPath, String filterField, Object filterValue, String operator, Type type, String unit ) 
			throws Throwable;
	
	/**
	 * Get the xmlData output that needs to be sent to the client.
	 * @param xPath
	 * @param filterMap
	 * @param unit
	 * @return
	 * @throws Throwable
	 */
	public String getXMLData(String xPath, Map<String,Filter> filterMap, String unit) 
			throws Throwable;
	
	/**
	 * Get the data in UiView that will be used as a DTO sent to the client.
	 * @param xPath
	 * @param filterMap
	 * @param unit
	 * @return
	 * @throws Throwable
	 */
	public UiView getData(String xPath, Map<String, Filter> filterMap, String unit ) 
			throws Throwable;
	
	/**
	 * Get the xmlData output that needs to be sent to the client.
	 * <p><em>Please use this only when you need to call older callback interface and apply editibility functionality</em></p>
	 * @param xPath
	 * @param filterMap
	 * @param filterField
	 * @param filterValue
	 * @param operator
	 * @param type
	 * @param unit
	 * @return
	 * @throws Throwable
	 */
	public String getXMLData(String xPath, Map<String,Filter> filterMap, String filterField, Object filterValue, String operator, Type type, String unit) 
			throws Throwable;
	
	/**
	 * Get the data in UiView that will be used as a DTO sent to the client.
	 * <p><em>Please use this only when you need to call older callback interface and apply editibility functionality</em></p>
	 * @param xPath
	 * @param filterMap
	 * @param filterField
	 * @param filterValue
	 * @param operator
	 * @param type
	 * @param unit
	 * @return
	 * @throws Throwable
	 */
	
	public UiView getData(String xPath, Map<String, Filter> filterMap, String filterField, Object filterValue, String operator, Type type, String unit ) 
			throws Throwable;

	/**
	 * Get the remote reference given a uiId
	 * @param uiId
	 * @return
	 */
	public String getRemoteRef(String uiId);
	

	
	/**
	 * Gets the uiId given an entityName and property name.
	 * <p>This is a helper method for client (of this object)
	 * to get the uiId given Hibernate entityName and propertyName</p>
	 * <p> Ignores the .id property if the ignoreIdProperty is set to true.
	 * @param entityName
	 * @param propertyName
	 * @param ignoreIdProperty
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public List<String> getUiId(String entityName, String propertyName,boolean ignoreIdProperty) throws ClassNotFoundException;

	/**
	 * Get the remote name given the uiId
	 * @param uiId
	 * @return
	 */
	public String getRemoteName(String uiId);
	
	/**
	 * Get the remote call back interface given the uiId
	 * @param uiId
	 * @return
	 */
	public RemoteRefMethodCallback getRemoteMethodCallback(String uiId);
	
	/**
	 * Given classMountingFormFactorUniqueValue return back the list of UiIds
	 * allowed for this combination.
	 * @param classMountingFormfactorUniqueValue
	 * @return
	 */
	public List<String> getValidUiId(Long classMountingFormfactorUniqueValue);
	
	/**
	 * Checks to see if the given uiId is associated with classMountingFormfactorUniqueValue
	 * @param uiId
	 * @param classMountingFormfactorUniqueValue
	 * @return
	 */
	public boolean isValidUiIdForUniqueValue(String uiId, Long classMountingFormfactorUniqueValue);
	
	/**
	 * Gets the uiComponent that has default values given the unique value.
	 * @param classMountingFormfactorUniqueValue
	 * @return
	 */
	public List<UiComponent> getComponentsWithDefaults(Long classMountingFormfactorUniqueValue);
	
	/**
	 * Get uiComponent that has default values
	 * @return
	 */
	public List<UiComponent> getAllComponentsWithDefaultValues();

	/**
	 * get the editability information
	 * @param xPath
	 * @param filterMap
	 * @param filterField
	 * @param filterValue
	 * @param operator
	 * @param type
	 * @param unit
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public RulesNodeEditability getEditabilityInfo(String xPath,
			Map<String, Filter> filterMap, String filterField,
			Object filterValue, String operator, Type type, String unit)
			throws ClassNotFoundException, SecurityException,
			NoSuchFieldException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException;
}
