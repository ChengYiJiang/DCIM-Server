/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;
import org.springframework.core.io.ClassPathResource;

import com.raritan.dctrack.xsd.AltUiValueIdFieldMap;
import com.raritan.dctrack.xsd.DcTrack;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiView;
import com.raritan.dctrack.xsd.UiViewPanel;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.util.ObjectTracer;

/**
 * @author prasanna
 *
 */
public abstract class RulesProcessorBase implements RulesProcessor {

	protected DcTrack dcTrack;
	protected String rulesTemplateFileName;
	
	protected Map<String, RulesQryMgr> qryMgrsForXPath = new LinkedHashMap<String, RulesQryMgr>();
	
	protected RemoteRef remoteReference;
	
	protected Map<String, RemoteRefMethodCallback> callbacks = new LinkedHashMap<String,RemoteRefMethodCallback>();
	protected Map<String, RemoteRefMethodCallbackUsingFilter> callbacksUsingFilter = new LinkedHashMap<String,RemoteRefMethodCallbackUsingFilter>();
	
	protected SessionFactory sessionFactory;
	
	
	private static final StringBuffer xsltForPanel = new StringBuffer();
	
	private static final String altDataKeyDefault = "main";
	
	static
	{
		xsltForPanel.append("<?xml version='1.0' encoding='UTF-8'?>");
		xsltForPanel.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">");
		xsltForPanel.append("<xsl:output method='xml' indent='yes'/>");
	
		xsltForPanel.append("<xsl:template match='node()|@*'>");
		xsltForPanel.append("   <xsl:copy>");
		xsltForPanel.append("      <xsl:apply-templates select='node()|@*'/>");
		xsltForPanel.append("   </xsl:copy>");
		xsltForPanel.append("</xsl:template>");
		
		xsltForPanel.append("<xsl:template match=\"*[@uiId!=%s]\">");
		xsltForPanel.append("<xsl:if test=\"position()!=last()-1\">");
		xsltForPanel.append("<xsl:copy-of select=\"//*[@uiId=%s]\"/>");
		xsltForPanel.append("</xsl:if>");
		xsltForPanel.append("</xsl:template>");

		xsltForPanel.append("");
		xsltForPanel.append("");
		xsltForPanel.append("");
		xsltForPanel.append("");
		xsltForPanel.append("");
		xsltForPanel.append("");
		xsltForPanel.append("");
		
		xsltForPanel.append("</xsl:stylesheet>");
	}
	
	
	public RemoteRef getRemoteReference() {
		return remoteReference;
	}

	public void setRemoteReference(RemoteRef remoteReference) {
		this.remoteReference = remoteReference;
	}

	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	public String getRulesTemplateFileName() {
		return rulesTemplateFileName;
	}

	public void setRulesTemplateFileName(String rulesTemplateFileName) {
		this.rulesTemplateFileName = rulesTemplateFileName;
	}

	public void init() throws HibernateException, JAXBException, ClassNotFoundException, IOException{
		configure(rulesTemplateFileName);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#configure(java.lang.String)
	 */
	@Override
	public void configure(String fileName) throws JAXBException, HibernateException, ClassNotFoundException, IOException {
		
		if (fileName != null)
			rulesTemplateFileName = fileName;
		
		RulesQryMgr qryMgr = new RulesQryMgrHQLImpl(remoteReference);
		
		
		try {
			parseTemplate();
			UiView uiView = getUiView();
			
			for (UiViewPanel panel: uiView.getUiViewPanel()){
				RulesQryMgr qryMgrForPanel = new RulesQryMgrHQLImpl(remoteReference);
				for (UiComponent uiComponent: panel.getUiViewComponents().getUiViewComponent()){
					String remoteRef = uiComponent.getUiValueIdField().getRemoteRef();
					if (remoteReference.getRemoteRefMethodCallback(remoteRef) != null){
						callbacks.put(uiComponent.getUiId(), remoteReference.getRemoteRefMethodCallback(remoteRef));
					} else {
						//qryMgr is for the view 
						qryMgr.createCriteria(uiComponent.getUiValueIdField().getRemoteRef(), 
								getXPathComponent(uiView.getUiId(), panel.getUiId(), uiComponent.getUiId()), altDataKeyDefault);
						
						qryMgr.addProjection(uiComponent.getUiValueIdField().getRemoteRef(), 
								getXPathComponent(uiView.getUiId(), panel.getUiId(), uiComponent.getUiId()), altDataKeyDefault);
						
						
						//qryMgrFor Panel is for the panel criteria.
						//The reason for this second qry just for panel is that client could ask data just 
						//for the panel and we do not want to include unnecessary projections.
						qryMgrForPanel.createCriteria(uiComponent.getUiValueIdField().getRemoteRef(), 
								getXPathComponent(uiView.getUiId(), panel.getUiId(), uiComponent.getUiId()), altDataKeyDefault);
						
						qryMgrForPanel.addProjection(uiComponent.getUiValueIdField().getRemoteRef(), 
								getXPathComponent(uiView.getUiId(), panel.getUiId(), uiComponent.getUiId()), altDataKeyDefault);
					}
					
					//Loop through the alternate data list and add criteria and projection for that as well
					for (AltUiValueIdFieldMap altField: uiComponent.getAltUiValueIdFieldMap()){
						String remoteRefForAlt = altField.getUiValueIdField().getRemoteRef();
						if (remoteReference.getRemoteRefMethodCallbackUsingFilter(remoteRefForAlt) != null){
							callbacksUsingFilter.put(uiComponent.getUiId(), remoteReference.getRemoteRefMethodCallbackUsingFilter(remoteRefForAlt));
						}else{
							qryMgr.createCriteria(remoteRefForAlt,
									getXPathComponentAltList(uiView.getUiId(),panel.getUiId(),uiComponent.getUiId(),altField.getId()), altField.getId());
							
							qryMgr.addProjection(remoteRefForAlt, 
									getXPathComponentAltList(uiView.getUiId(),panel.getUiId(),uiComponent.getUiId(),altField.getId()), altField.getId());
							
							qryMgrForPanel.createCriteria(remoteRefForAlt,
									getXPathComponentAltList(uiView.getUiId(),panel.getUiId(),uiComponent.getUiId(),altField.getId()), altField.getId());
							
							qryMgrForPanel.addProjection(remoteRefForAlt, 
									getXPathComponentAltList(uiView.getUiId(),panel.getUiId(),uiComponent.getUiId(),altField.getId()), altField.getId());
						}
					}
				}
				
				qryMgrsForXPath.put(getXPathPanel(uiView.getUiId(), panel.getUiId()),qryMgrForPanel);
			}
			
			qryMgrsForXPath.put(getXPathView(uiView.getUiId()),qryMgr);
			
		} catch (JAXBException e){
			//log it and throw that to upper layer
			throw e;
		}

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public UiView getData(String xPath,  String filterField, Object filterValue, String operator, Type type, String unit ) 
			throws Throwable {
		synchronized(this){
			clearData();
			fetchData(xPath, filterField, filterValue, operator, type, unit);
		}
		return getUiView();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public UiView getData(String xPath,  Map<String, Filter> filterMap, String unit ) 
			throws Throwable {
		synchronized(this){
			clearData();
			fetchData(xPath, filterMap, unit);
		}
		return getUiView();
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public UiView getData(String xPath,  Map<String, Filter> filterMap,  String filterField, Object filterValue, String operator, Type type, String unit ) 
			throws Throwable {
		synchronized(this){
			clearData();
		    fetchData(xPath, filterMap, unit);
		    
		    //Let us execute the old way of callbacks
		    executeCallback(filterField, filterValue, operator, unit);
			
			String columnName = getHibernateColumnName(getUiView().getRemoteRef(),filterField);
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("{alias}.");
			sql.append(columnName);
			sql.append(operator);
			sql.append("?");
	
			Criterion criterion = Restrictions.sqlRestriction(sql.toString(), filterValue, type);
			
			//Also the applicability is based on the old way of creating the Restrictions object
			applyEditability(criterion);
			
			postProcess();
			
			//This is to ensure that hibernate criteria is re-initialized next time.
			qryMgrsForXPath.get(xPath).clearAll();
		}
		return getUiView();
	}

	@Override
	public RulesNodeEditability getEditabilityInfo(String xPath,  Map<String, Filter> filterMap,  String filterField, Object filterValue, String operator, Type type, String unit) throws ClassNotFoundException, SecurityException, NoSuchFieldException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		String columnName = getHibernateColumnName(getUiView().getRemoteRef(),filterField);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("{alias}.");
		sql.append(columnName);
		sql.append(operator);
		sql.append("?");

		Criterion criterion = Restrictions.sqlRestriction(sql.toString(), filterValue, type);
		
		//Also the applicability is based on the old way of creating the Restrictions object
		return getEditability(criterion);

	}
	
	@Override
	public List<String> getUiId(String entityName, String propertyName, boolean ignoreIdProperty) throws ClassNotFoundException{
		ArrayList<String> uiIds = new ArrayList<String>();
		
		List<String> propertyKeys = remoteReference.getKey(propertyName);
		
		for (String propertyKey:propertyKeys){
			if (!(propertyKey != null && propertyKey.contains(RemoteRef.RemoteRefConstantProperty.FOR_ID.toString()) && ignoreIdProperty == true)){
				uiIds.add(getUiId(entityName,propertyName,propertyKey));
			}
		}
		
		return uiIds;
	}


	
	private void clearData() throws JAXBException, IOException {
//		JXPathContext jc = JXPathContext.newContext(getUiView());
//		List<UiValueIdField> list = jc.selectNodes("uiViewPanel/uiViewComponents/uiViewComponent/uiValueIdField");
//		
//		for (UiValueIdField valueId : list){
//			valueId.setValue(null);
//			valueId.setValueId(0);
//		}
		parseTemplate();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public String getXMLData(String xPath,  String filterField, Object filterValue, String operator, Type type, String unit ) 
			throws Throwable {
		fetchData(xPath, filterField, filterValue, operator, type, unit);
		return getXMLOutput(xPath);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public String getXMLData(String xPath, Map<String,Filter> filterMap,String filterField, Object filterValue, String operator, Type type, String unit ) 
			throws Throwable {
		fetchData(xPath, filterMap, unit);
	    
	    //Let us execute the old way of callbacks
	    executeCallback(filterField, filterValue, operator, unit);
		
		String columnName = getHibernateColumnName(getUiView().getRemoteRef(),filterField);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("{alias}.");
		sql.append(columnName);
		sql.append(operator);
		sql.append("?");

		Criterion criterion = Restrictions.sqlRestriction(sql.toString(), filterValue, type);
		
		//Also the applicability is based on the old way of creating the Restrictions object
		applyEditability(criterion);
		
		postProcess();
		
		//This is to ensure that hibernate criteria is re-initialized next time.
		qryMgrsForXPath.get(xPath).clearAll();
		return getXMLOutput(xPath);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public String getXMLData(String xPath, Map<String,Filter> filterMap, String unit ) 
			throws Throwable {
		fetchData(xPath, filterMap, unit);
		return getXMLOutput(xPath);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#getXMLData(java.lang.String)
	 */
	@Override
	public String getRemoteRef(String uiId){
		StringBuffer xPath = new StringBuffer();
		xPath.append("uiViewPanel/uiViewComponents/uiViewComponent[@uiId=");
		xPath.append("'");
		xPath.append(uiId);
		xPath.append("']");
		JXPathContext jc = JXPathContext.newContext(getUiView());
		UiComponent uiComponent = (UiComponent) jc.getValue(xPath.toString());
		return uiComponent.getUiValueIdField().getRemoteRef();
		
	}
	@Override
	public String getRemoteName(String uiId){
		StringBuffer xPath = new StringBuffer();
		xPath.append("uiViewPanel/uiViewComponents/uiViewComponent[@uiId=");
		xPath.append("'");
		xPath.append(uiId);
		xPath.append("']");
		JXPathContext jc = JXPathContext.newContext(getUiView());
		UiComponent uiComponent = (UiComponent) jc.getValue(xPath.toString());
		
		return uiComponent.getRemoteName();
	}
	
	@Override
	public RemoteRefMethodCallback getRemoteMethodCallback(String uiId){
		return callbacks.get(uiId);
	}
	

	@Override
	public List<String> getValidUiId(Long classMountingFormfactorUniqueValue) {
		List<String> uiIdList = new ArrayList<String>();
		JXPathContext jc = JXPathContext.newContext(getUiView());
		
		if (classMountingFormfactorUniqueValue != null){
			String uniqueValue = classMountingFormfactorUniqueValue.toString();

			StringBuffer xPath = new StringBuffer();
			xPath.append("uiViewPanel/uiViewComponents/uiViewComponent[contains(concat(',',@classMountingFormFactorValue,''),'");
			xPath.append(uniqueValue);
			xPath.append("')]");
			@SuppressWarnings("unchecked")
			List<UiComponent> uiComponents = jc.selectNodes(xPath.toString());
			for (UiComponent uiComponent:uiComponents){
				uiIdList.add(uiComponent.getUiId());
			}
		} else {
			StringBuffer xPath = new StringBuffer();
			xPath.append("uiViewPanel/uiViewComponents/uiViewComponent");
			@SuppressWarnings("unchecked")
			List<UiComponent> uiComponents = jc.selectNodes(xPath.toString());
			for (UiComponent uiComponent:uiComponents){
				uiIdList.add(uiComponent.getUiId());
			}
		}
		
		StringBuffer xPath = new StringBuffer();
		xPath.append("uiViewPanel/uiViewComponents/uiViewComponent[not(@classMountingFormFactorValue)]");
		
		List<UiComponent> uiComponents = jc.selectNodes(xPath.toString());
		for (UiComponent uiComponent:uiComponents){
			uiIdList.add(uiComponent.getUiId());
		}
		
		return uiIdList;
	}


	@Override
	public boolean isValidUiIdForUniqueValue(String uiId,
			Long classMountingFormfactorUniqueValue) {
		
		return getValidUiId(classMountingFormfactorUniqueValue).contains(uiId);
	}
	
	
	@Override
	public List<UiComponent> getComponentsWithDefaults(Long classMountingFormfactorUniqueValue){
		if (classMountingFormfactorUniqueValue != null){
			String uniqueValue = classMountingFormfactorUniqueValue.toString();
			JXPathContext jc = JXPathContext.newContext(getUiView());
			StringBuffer xPath = new StringBuffer();
			xPath.append("uiViewPanel/uiViewComponents/uiViewComponent[contains(concat(',',@classMountingFormFactorValue,''),'");
			xPath.append(uniqueValue);
			xPath.append("') and (./uiValueIdField/uiDefaultValue/valueId or ./uiValueIdField/uiDefaultValue/value)]");
			@SuppressWarnings("unchecked")
			List<UiComponent> componentsWithDefalt = jc.selectNodes(xPath.toString());
			
		
		return componentsWithDefalt;
		} else {
			return new ArrayList<UiComponent>();
		}
		
	}
	
	@Override
	public List<UiComponent> getAllComponentsWithDefaultValues(){
		JXPathContext jc = JXPathContext.newContext(getUiView());
		StringBuffer xPath = new StringBuffer();
		xPath.append("uiViewPanel/uiViewComponents/uiViewComponent[descendant::uiDefaultValue]");
		@SuppressWarnings("unchecked")
		List<UiComponent> componentsWithDefalt = jc.selectNodes(xPath.toString());
			
		return componentsWithDefalt;
	}
	
	
	private String getUiId(String entityName, String propertyName, String propertyKey) throws ClassNotFoundException{
		String uiId = null;
	
		if (propertyKey != null){
			String remoteReferenceStr = propertyKey.substring(0, propertyKey.indexOf("."));
			
			String remoteType = remoteReference.getRemoteType(remoteReferenceStr);
			
			if (!isPropertyRelatedToRemoteType(entityName, propertyName, remoteType))
				return uiId;
			
			UiComponent uiComponent = null;
			JXPathContext jc = JXPathContext.newContext(getUiView());
			uiComponent = (UiComponent) jc.getValue("uiViewPanel/uiViewComponents/uiViewComponent/uiValueIdField[@remoteRef='" + remoteReferenceStr + "']/..");
			
			if (uiComponent != null){
				uiId = uiComponent.getUiId();
			}
		}
		
		return uiId;
	}

	private boolean isPropertyRelatedToRemoteType(String entityName, String propertyName, String remoteType)
			throws ClassNotFoundException {
		ObjectTracer ot = new ObjectTracer();
		List<Field> fields = ot.traceObject(Class.forName(remoteType), propertyName);
		
		boolean foundType = false;
		for (Field field:fields){
			if (field.getDeclaringClass().getName().equals(entityName)){
				foundType = true;
			}
		}
		return foundType;
	}
	

	
	//Get screen specific view
	protected abstract UiView getUiView();
	
	//Apply the editability of the nodes.
	protected abstract void applyEditability(Criterion criterion)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException;
	
	protected void applyEditability(Map<String, Filter> filterMap)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException{
		
	}
	
	protected abstract RulesNodeEditability getEditability(Criterion criterion) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	
	protected void parseTemplate() throws JAXBException, IOException {
		ClassPathResource classpathResource = new ClassPathResource(rulesTemplateFileName);
		File rulesTemplateFile = classpathResource.getFile();
		JAXBContext jc = JAXBContext.newInstance("com.raritan.dctrack.xsd");
		Unmarshaller unMarshaller = jc.createUnmarshaller();
		unMarshaller.setProperty("com.sun.xml.bind.ObjectFactory", new ObjectFactoryEx());
		dcTrack = (DcTrack) unMarshaller.unmarshal(rulesTemplateFile);
	}
	
	protected String getXPathView(String uiViewId) {
		StringBuffer xPathBuffer = new StringBuffer();
		xPathBuffer.append("uiView");
		xPathBuffer.append("[@uiId='");
		xPathBuffer.append(uiViewId);
		xPathBuffer.append("']/");
		return xPathBuffer.toString();
	}
	
	protected String getXPathPanel(String uiViewId, String uiViewPanelId) {
		StringBuffer xPathBuffer = new StringBuffer();
		xPathBuffer.append(getXPathView(uiViewId));
		xPathBuffer.append("uiViewPanel");
		xPathBuffer.append("[@uiId='");
		xPathBuffer.append(uiViewPanelId);
		xPathBuffer.append("']/");
		return xPathBuffer.toString();
	}
	
	protected String getXPathComponent(String uiViewId, String uiViewPanelId, String uiViewComponentId) {
		StringBuffer xPathBuffer = new StringBuffer();
		xPathBuffer.append(getXPathPanel(uiViewId, uiViewPanelId));
		xPathBuffer.append("uiViewComponents/");
		xPathBuffer.append("uiViewComponent");
		xPathBuffer.append("[@uiId='");
		xPathBuffer.append(uiViewComponentId);
		xPathBuffer.append("']");
		xPathBuffer.append("/");
		return xPathBuffer.toString();
	}
	
	protected String getXPathComponentAltList(String uiViewId, String uiViewPanelId, String uiViewComponentId, String altDataId) {
		StringBuffer xPathBuffer = new StringBuffer();
		xPathBuffer.append(getXPathPanel(uiViewId, uiViewPanelId));
		xPathBuffer.append("uiViewComponents/");
		xPathBuffer.append("uiViewComponent");
		xPathBuffer.append("[@uiId='");
		xPathBuffer.append(uiViewComponentId);
		xPathBuffer.append("']");
		xPathBuffer.append("/");
		xPathBuffer.append("altUiValueIdFieldMap");
		xPathBuffer.append("[@id='");
		xPathBuffer.append(altDataId);
		xPathBuffer.append("']");
		xPathBuffer.append("/");
		return xPathBuffer.toString();
	}

	private String getHibernateColumnName(String remoteRef, String name) throws ClassNotFoundException, SecurityException, NoSuchFieldException{
		
		String remoteType = remoteReference.getRemoteType(remoteRef);
		Class<?> remoteTypeClass = Class.forName(remoteType);
		return getHibernateColumnName(remoteTypeClass, name);
	}

	private String getHibernateColumnName(Class<?> remoteTypeClass,
			String name) throws NoSuchFieldException {
		String columnName = null;
		Field field = remoteTypeClass.getDeclaredField(name);
		Annotation[] annotations = field.getDeclaredAnnotations();
		for (Annotation annotation :annotations){
			if (annotation instanceof Column){
				Column columnAnnotation = (Column) annotation;
				columnName = columnAnnotation.name();
				
				if (columnName != null && !columnName.isEmpty()){
					columnName = columnName.substring(1,columnName.lastIndexOf("`"));
				}
			}
		}
		
		
		return columnName;
	}

	private String getXMLOutput(String xPath) throws JAXBException {
		
		JAXBContext jc = JAXBContext.newInstance("com.raritan.dctrack.xsd");
		StringWriter stringWriter = new StringWriter();

		if (xPath.contains("uiViewPanel")){
			fillXMLDataForPanel(xPath, jc, stringWriter);
		} else {
			fillXMLDataForView(jc, stringWriter);
		}
		
		return stringWriter.toString();
	}

	private void fillXMLDataForView(JAXBContext jc, StringWriter stringWriter)
			throws JAXBException, PropertyException {
		Marshaller m = jc.createMarshaller();		
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
//		NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
//			
//			@Override
//			public String getPreferredPrefix(String arg0, String arg1, boolean arg2) {
//				if (arg0.equals("http://dctrack.raritan.com/xsd"))
//					return "dcTrack";
//				else
//					return null;
//			}
//		};
//		m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", mapper);
		m.marshal(dcTrack, stringWriter);
	}

	private void fillXMLDataForPanel(String xPath, JAXBContext jc,
			StringWriter stringWriter) throws JAXBException,
			TransformerFactoryConfigurationError {
		JAXBSource source = new JAXBSource(jc, dcTrack);
		
		//Get the view's uiId and panel's uiId from xpath string
		List<String> uiIds = getUiIds(xPath);

		
		TransformerFactory tf = TransformerFactory.newInstance();
		
		try {
			String formattedString = String.format(xsltForPanel.toString(), uiIds.get(0), uiIds.get(1));
			Transformer t = tf.newTransformer(new StreamSource(new StringReader(formattedString)));
			t.transform(source, new StreamResult(stringWriter));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fetchData(String xPath, String filterField,
			Object filterValue, String operator, Type type, String unit)
			throws Throwable {
		executeCallback(filterField, filterValue, operator, unit);
		if (qryMgrsForXPath.get(xPath) != null){
			qryMgrsForXPath.get(xPath).addFilter(getUiView().getRemoteRef(), filterField, filterValue, operator);
		}
		fetchData(xPath,unit);
		
		String columnName = getHibernateColumnName(getUiView().getRemoteRef(),filterField);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("{alias}.");
		sql.append(columnName);
		sql.append(operator);
		sql.append("?");

		Criterion criterion = Restrictions.sqlRestriction(sql.toString(), filterValue, type);
		
		applyEditability(criterion);
		
		postProcess();
		
		//This is to ensure that hibernate criteria is re-initialized next time.
		qryMgrsForXPath.get(xPath).clearAll();
	}
	
	private void fetchData(String xPath, Map<String, Filter> filterMap, String unit)
			throws Throwable {
		executeCallback(filterMap, unit);
		if (qryMgrsForXPath.get(xPath) != null){
			qryMgrsForXPath.get(xPath).addFilter(filterMap);
		}
		fetchData(xPath,unit);
				
		applyEditability(filterMap);
		
		postProcess();
		
		//This is to ensure that hibernate criteria is re-initialized next time.
		qryMgrsForXPath.get(xPath).clearAll();
	}
	
	
	private void fetchData(String xPath,String unit)
			throws Throwable {
		Session session = sessionFactory.getCurrentSession();

		//First clear all the editability and lockable status
		//CR Number: 50760. The clearEditability is no longer required since we do this already during clearData called before fetch.
		//This will reduce the time taken to clear them.
		//clearEditability();
		//executeCallback(filterField, filterValue, operator, unit);
		if (qryMgrsForXPath.get(xPath) != null){
			//qryMgrsForXPath.get(xPath).addFilter(getUiView().getRemoteRef(), filterField, filterValue, operator);
			List<Map<String,?>> dataList = qryMgrsForXPath.get(xPath).getData(session);
			
			for (Map<String,?> map : dataList){
				for (Map.Entry<String, ?> entry : map.entrySet()){
					String key = entry.getKey();
					Object value = entry.getValue();
					
					key = key.replace("_SL_", "/");
					key = key.replace("_LB_", "[");
					key = key.replace("_RB_", "]");
					key = key.replace("_A_", "@");
					key = key.replace("_Q_", "'");
					key = key.replace("_EQ_", "=");
					
					JXPathContext jxPathContext = JXPathContext.newContext(dcTrack.getUiViews());
					jxPathContext.setValue(key, value);
				}
			}
	

		}
		
	
	}


	private void postProcess() {
		//Set the value and valueId to be the same when we have only value.
		JXPathContext jc = JXPathContext.newContext(getUiView());
		List<UiComponentDTO> componentList = jc.selectNodes("uiViewPanel/uiViewComponents/uiViewComponent");
		
		for (UiComponentDTO uiViewComponent : componentList) {
			if (uiViewComponent.getUiValueIdField().getValue() != null && uiViewComponent.getUiValueIdField().getValueId() == null){
				uiViewComponent.getUiValueIdField().setValueId(uiViewComponent.getUiValueIdField().getValue());
			}
		}
	}

	private void clearEditability() throws IOException, JAXBException {
		
		//First read the template to get the original values for editability
		StringBuffer xPath = new StringBuffer();
		xPath.append("//");
		xPath.append(getXPathView(getUiView().getUiId()));
		xPath.append("/uiViewPanel/uiViewComponents/uiViewComponent");
		
		ClassPathResource classpathResource = new ClassPathResource(rulesTemplateFileName);
		File rulesTemplateFile = classpathResource.getFile();
		JAXBContext jc = JAXBContext.newInstance("com.raritan.dctrack.xsd");
		Unmarshaller unMarshaller = jc.createUnmarshaller();
		unMarshaller.setProperty("com.sun.xml.bind.ObjectFactory", new ObjectFactoryEx());
		DcTrack dcTrackTemp = (DcTrack) unMarshaller.unmarshal(rulesTemplateFile);
		
		//Set them here.
		JXPathContext jcTemp = JXPathContext.newContext(dcTrackTemp);
		List<UiComponentDTO> uiComponentList = jcTemp.selectNodes(xPath.toString());
		

		JXPathContext jcOrig = JXPathContext.newContext(dcTrack);
		
		for (UiComponentDTO node: uiComponentList){
			StringBuffer xpath = new StringBuffer();
			xpath.append("//");
			xpath.append(getXPathView(getUiView().getUiId()));
			xpath.append("/uiViewPanel/uiViewComponents/uiViewComponent[@uiId='");
			xpath.append(node.getUiId());
			xpath.append("']/editable");
			
			jcOrig.setValue(xpath.toString(), node.getEditable());
		}
		
	}

	private void executeCallback(String filterField,
			Object filterValue, String operator, String unit) throws Throwable {
		for (Map.Entry<String, RemoteRefMethodCallback> entry : callbacks.entrySet()){
			String uiComponentId = entry.getKey();
			JXPathContext jc = JXPathContext.newContext(dcTrack);
			
			StringBuffer xpath = new StringBuffer();
			xpath.append("//");
			xpath.append(getXPathView(getUiView().getUiId()));
			xpath.append("/uiViewPanel/uiViewComponents/uiViewComponent[@uiId='");
			xpath.append(uiComponentId);
			xpath.append("']");
			UiComponent key = (UiComponent) jc.getValue(xpath.toString());
			RemoteRefMethodCallback callback = entry.getValue();
			
			callback.fillValue(key, filterField, filterValue, operator, remoteReference, unit);
		}
		
	}
	
	private void executeCallback(Map<String, Filter> filterMap, String unit) throws Throwable {
		for (Map.Entry<String, RemoteRefMethodCallbackUsingFilter> entry : callbacksUsingFilter.entrySet()){
			String uiComponentId = entry.getKey();
			JXPathContext jc = JXPathContext.newContext(dcTrack);
			
			StringBuffer xpath = new StringBuffer();
			xpath.append("//");
			xpath.append(getXPathView(getUiView().getUiId()));
			xpath.append("/uiViewPanel/uiViewComponents/uiViewComponent[@uiId='");
			xpath.append(uiComponentId);
			xpath.append("']");
			UiComponent key = (UiComponent) jc.getValue(xpath.toString());
			RemoteRefMethodCallbackUsingFilter callback = entry.getValue();
			
			callback.fillValue(key, filterMap, remoteReference, unit);
		}
		
	}

	private List<String> getUiIds(String xPath) {
		String[] paths = xPath.split("@uiId=");
		List<String> uiIds = new ArrayList<String>();
		for (String path: paths){
			if (path.contains("']")){
				String[] ids = path.split("'\\].*"); 
				uiIds.add(ids[0]+"'");
			}
		}
		return uiIds;
	}
}
