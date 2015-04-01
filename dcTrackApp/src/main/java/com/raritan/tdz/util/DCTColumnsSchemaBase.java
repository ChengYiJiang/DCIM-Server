/**
 * 
 */
package com.raritan.tdz.util;



import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.ColumnInfo;

import com.raritan.tdz.util.DCTColumnsSchema;

/**
 * @author bozana
 *
 */
public class DCTColumnsSchemaBase implements DCTColumnsSchema {
	
	//map of pairs: uiID, property length
	private Map<String, String> m_uiIDLenthMap;
	
	//map of pairs: sql 'table column', hibernate's 'entity property' 
	private Map<String, String> m_sqlColumnToHibPropertMap;
	
	private LinkedHashSet<RulesProcessor> rulesProcessors;
	
	@Autowired
	private FieldHome fieldHome;
	
	private static Logger log = Logger.getLogger("DCTColumnsSchemaBase");
	protected SessionFactory sessionFactory;
	
	public SessionFactory getSessionFactory(){ 
		return this.sessionFactory; 
	}
	
	public DCTColumnsSchemaBase(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	/**
	 *
	 * @return map containing pairs: uiID, max_length
	 * where format max_length is returned as a String whose format
	 * depends on data type according to the following rules:
	 * 		
	 * 		data type:           format:
	 * -----------------------------------------------
	 * 		bigint             "precision.radix.scale"
	 *      smallint           "precision.radix.scale"
	 *      numeric            "precision.radix.scale"
	 *      integer            "precision.radix.scale"
	 *      character varying  "max_length"
	 *      text               " "
	**/
	@Override
	public Map<String, String> getUiIdLenthMap(){
		return m_uiIDLenthMap;
	}

	/**
	 * 
	 * @param uiId 
	 * @return length of the property for specified uiId
	 */
	@Override
	public String getPropertyLength( String uiId ) {
		return (m_uiIDLenthMap==null) ? "0" : m_uiIDLenthMap.get(uiId);
	}

	@Override
	public void validate(String uiId, Double value, Errors errors) {
		//Get the length for the given uiId from schema
		String lengthStr = getPropertyLength(uiId);
		Object[] errorArgs = {value, fieldHome.getDefaultName(uiId)};
		
		if (lengthStr == null || !lengthStr.contains(".")){
			errors.reject("SchemaValidation.Double.incompatibleValue", errorArgs, "The given value for this field is incompatible");
			return;
		}
		
		
		//Get the lengths before and after decimal point from the schema
		String precisionStr = lengthStr.substring(0,lengthStr.indexOf("."));
		String scaleStr = lengthStr.substring(lengthStr.lastIndexOf(".") + 1);
		
		Integer lengthBeforeDecimal = precisionStr != null && scaleStr != null ? Integer.parseInt(precisionStr) - Integer.parseInt(scaleStr) : 0;
		Integer lengthAfterDecimal = scaleStr != null ? Integer.parseInt(scaleStr) : 0;
		
		if (lengthAfterDecimal <= 0){
			errors.reject("SchemaValidation.Double.incompatibleValue", errorArgs, "The given value for this field is incompatible");
			return;
		}
		
		StringBuffer pattern = new StringBuffer();
		
		pattern.append("^(\\d{0,");
		pattern.append(lengthBeforeDecimal);
		pattern.append("}(\\.\\d{1,");
		pattern.append(lengthAfterDecimal);
		pattern.append("}))+|(\\d{1,");
		pattern.append(lengthBeforeDecimal);
		pattern.append("}(\\d.\\d{0,");
		pattern.append(lengthAfterDecimal);
		pattern.append("})?)$");
		
		BigDecimal bd = new BigDecimal(value.toString());	
		
		//Now validate the given double value with the length 
		String valueStr = bd.toString();
		
		//With a regex we can easily look for if this is indeed a double value with correct number of max before decimal + after decimal
		if (value > 0.0 && !Pattern.compile(pattern.toString()).matcher(valueStr).matches()){
			Object[] args = {valueStr, fieldHome.getDefaultName(uiId),lengthBeforeDecimal,lengthAfterDecimal};
			errors.reject("SchemaValidation.Double.invalidValue", args, "The given value for this field is invalid");
		}
	}

	@Override
	public void validate(String uiId, Integer value, Errors errors) {
		//Get the length for the given uiId from schema
		String lengthStr = getPropertyLength(uiId);
		Object[] errorArgs = {value, fieldHome.getDefaultName(uiId)};
		
		if (lengthStr == null || !lengthStr.contains(".")){
			errors.reject("SchemaValidation.Integer.incompatibleValue", errorArgs, "The given value for this field is incompatible");
			return;
		}
		
		
		//Get the lengths before and after decimal point from the schema
		String precisionStr = lengthStr.substring(0,lengthStr.indexOf("."));
		String scaleStr = lengthStr.substring(lengthStr.lastIndexOf(".") + 1);
		
		Integer lengthBeforeDecimal = precisionStr != null && scaleStr != null ? Integer.parseInt(precisionStr) - Integer.parseInt(scaleStr) : 0;
		Integer lengthAfterDecimal = scaleStr != null ? Integer.parseInt(scaleStr) : 0;
		
		if (lengthAfterDecimal > 0){
			errors.reject("SchemaValidation.Integer.incompatibleValue", errorArgs, "The given value for this field is incompatible");
			return;
		}
		
		StringBuffer pattern = new StringBuffer();
		
		pattern.append("^(\\d{0,");
		pattern.append(lengthBeforeDecimal);
		pattern.append("}");
		pattern.append("+|(\\d{1,");
		pattern.append(lengthBeforeDecimal);
		pattern.append("})?)$");
		
		
		//Now validate the given double value with the length 
		String valueStr = value.toString();
		
		//With a regex we can easily look for if this is indeed a double value with correct number of max before decimal + after decimal
		if (value > 0 && !Pattern.compile(pattern.toString()).matcher(valueStr).matches()){
			Object[] args = {valueStr, fieldHome.getDefaultName(uiId),lengthBeforeDecimal};
			errors.reject("SchemaValidation.Integer.invalidValue", args, "The given value for this field is invalid");
		}
	}

	@Override
	public void validate(String uiId, String value, Errors errors) {
		//Get the length for the given uiId from schema
		String lengthStr = getPropertyLength(uiId);
		Object[] errorArgs = {value, fieldHome.getDefaultName(uiId)};
		
		//We can ignore strings with Text datatype
		if (lengthStr.equals(" ")) return;
		
		if (lengthStr == null || lengthStr.contains(".")){
			errors.reject("SchemaValidation.String.incompatibleValue", errorArgs, "The given value for this field is incompatible");
			return;
		}
	
		//Now validate the given double value with the length 
		String valueStr = value.toString();
		
		//With a regex we can easily look for if this is indeed a double value with correct number of max before decimal + after decimal
		if (valueStr.length() > Integer.parseInt(lengthStr)){
			String msgStr = truncateLongStrForMsg(valueStr);
			Object[] args = {msgStr, fieldHome.getDefaultName(uiId),lengthStr};
			errors.reject("SchemaValidation.String.invalidValue", args, "The given value for this field is invalid");
		}
		
	}
	
	private String truncateLongStrForMsg(String value) {
		String rval = value;
		if (value.length() > 50) {
			StringBuilder msg = new StringBuilder();
			msg.append(value.substring(0, 50));
			msg.append("...");
			rval = msg.toString();
		}
		return rval;
	}


	/**
	 * From hibernate obtain all properties for all entities and find
	 * corresponding table and column names. 
	 */
	private void createSQLColumnToHibPropertyMap(){
        m_sqlColumnToHibPropertMap = new HashMap<String,String>();
        
		//obtain metadata for all classes
        Map metaDataMap = sessionFactory.getAllClassMetadata();

        Iterator it = metaDataMap.entrySet().iterator();
        while (it.hasNext()) {
        	try{
        		Map.Entry pairs = (Map.Entry)it.next();
        		ClassMetadata classMetadata = (ClassMetadata)pairs.getValue();
        		AbstractEntityPersister y = (AbstractEntityPersister)classMetadata;
        	
        		for (int j = 0; j < y.getPropertyNames().length; j++) {
        			if(y.getPropertyColumnNames(j).length > 0){
        				//FIXME: There is a problem with TicketsStatus entity. Hibernate is
        				//for all columns mapped to ticketsStatus reporting the "ticketsStatus"
        				//as property. Need to find a fix for this.
        				for( int k=0; k< y.getPropertyColumnNames(j).length; k++ ){

        					String key = createKey(y.getTableName(), y.getPropertyColumnNames(j)[k]);
        					String value = createValue((String)pairs.getKey(), y.getPropertyNames()[j]);
        					if(log.isDebugEnabled()){
        						log.debug("Adding key,value: " + key + ", " + value);
        					}
        					m_sqlColumnToHibPropertMap.put(key, value);
        				}
        			}
        		}
        	}catch(HibernateException e){
        		log.error("Got hibernate exception, continue...");
				e.printStackTrace();
        	}
        }
	}
	
	
	private String createKey(String tableName, String columnName){
		StringBuffer myKey = new StringBuffer();

		//hibernate returns some SQL table/column names quoted, some not...
		//so we have to remove quotes in those that have them
		if(tableName.startsWith("\"") || tableName.endsWith("\"")){
			myKey.append(tableName.replaceAll("\"", ""));   				
		}else{
			myKey.append(tableName);
		}
		myKey.append(" ");
		if(columnName.startsWith("\"") ||columnName.endsWith("\"")){
			myKey.append(columnName.replaceAll("\"", ""));
		}else{
			myKey.append(columnName);	
		}
		return myKey.toString(); 
		
	}
	
	private String createValue(String entityName, String propertyName){
		StringBuffer myVal = new StringBuffer();
		
		myVal.append(entityName);
		myVal.append(" ");
		myVal.append(propertyName);
		return myVal.toString();
	}
	

	
	/**
	 * @return: max column length as a String whose format depends on data 
	 *  according to the following rules:
	 * 		
	 * 		data type:           format:
	 * -----------------------------------------------
	 * 		bigint             "precision.radix.scale"
	 *      smallint           "precision.radix.scale"
	 *      numeric            "precision.radix.scale"
	 *      integer            "precision.radix.scale"
	 *      character varying  "max_length"
	 *      text               " "
	 *      ------------------------------------------
	 *      
	 * 		For all other cases it returns null
	**/
	private String getColumnLength( ColumnInfo columnInfo ){
		StringBuffer length = new StringBuffer();

		if( columnInfo.getData_type().equals("bigint") ||
				columnInfo.getData_type().equals("smallint") ||
				columnInfo.getData_type().equals("integer") ||
				columnInfo.getData_type().equals("numeric")){
			
	        length.append(columnInfo.getNumeric_precision());
	        length.append(".");
	        length.append(columnInfo.getNumeric_precision_radix());
	        length.append(".");
	        length.append(columnInfo.getNumeric_scale());
		}else if( columnInfo.getData_type().equals("character varying")){
	        length.append(columnInfo.getCharacter_maximum_length());
		}else if( columnInfo.getData_type().equals("text")){
			length.append(" ");
		}

		if( length.length() > 0 ) return length.toString();
		else return null;
	}
	
	private String getTableAndColumnName( String tableName, String columnName){
		StringBuffer tableAndColumnName = new StringBuffer();
		tableAndColumnName.append(tableName);
		tableAndColumnName.append(" ");
		tableAndColumnName.append(columnName);
		return tableAndColumnName.toString();		
	}

	/**
	 * @param entityAndProperty - String in format: "entity property"
	 * @return uiID
	 * obtain uiID from entity and property
	 */
	private List<String> getUiId( String entityAndProperty, RulesProcessor rulesProcessor){
		List<String> uiIds = new ArrayList<String>();
		
		if(entityAndProperty != null ){
			String [] strArray = entityAndProperty.split(" ");
			String entity = strArray[0];
			String property = strArray[1];
			try{
				uiIds = rulesProcessor.getUiId(entity, property, true);
			}catch(Exception e){
				log.debug("## There is no uiId for (" + entity + ", " + property +") cannot add it to the map.");
			}
		}
		return uiIds;
	}

	/**
	 * Run SQL queary on INFORMATION_SCHEMA.COLUMNS and obtain 
	 * info about max column length for each column
	 * For each column obtain uiID and add pair (uiID, length) to the
	 * map
	 * 
	 */
	@Override
	@Transactional(readOnly = true)
	public void createUiIDPropertyLengthMap(){
	
		m_uiIDLenthMap = new HashMap<String, String>();
		
		createSQLColumnToHibPropertyMap();
		
		Session session = sessionFactory.getCurrentSession();
		String sql = "select table_name, column_name, data_type, " +
				"character_maximum_length, numeric_precision, numeric_precision_radix, numeric_scale " +
				"from INFORMATION_SCHEMA.COLUMNS where table_name like 'dct_%'";
		Query q = session.createSQLQuery(sql);
		
		Iterator i = q.setResultTransformer(Transformers.aliasToBean(ColumnInfo.class)).list().iterator();
		while(i.hasNext()){
			ColumnInfo columnInfo = (ColumnInfo)i.next();
	       	
        	String tableAndColumnName = getTableAndColumnName(columnInfo.getTable_name(), columnInfo.getColumn_name());
        	String entityAndProperty = m_sqlColumnToHibPropertMap.get(tableAndColumnName);
        	
        	String length = getColumnLength(columnInfo);
        	for( RulesProcessor rulesProcessor : rulesProcessors){
        		List<String> uiIds = getUiId(entityAndProperty, rulesProcessor);
        	
        		for(String uiId:uiIds){
        			if(log.isDebugEnabled()){
        				log.debug("tableAndColumnName=" + tableAndColumnName + ", entityAndProperty=" + entityAndProperty +
							", length=" + length + ", uiId=" + uiId);
        			}
	
					if( uiId != null && !uiId.isEmpty() && length != null){
						m_uiIDLenthMap.put(uiId, length);
					}
        		}
        	}
        }
		// FIXME:: HACK, HACK, HACK to avoid the ticket comments to work, 
		// this is not working because the ticket comments is in an embedded table in hibernate 
		m_uiIDLenthMap.put("tiTicketComments", " ");
	}

	public LinkedHashSet<RulesProcessor> getRulesProcessors() {
		return rulesProcessors;
	}

	public void setRulesProcessors(LinkedHashSet<RulesProcessor> rulesProcessors) {
		this.rulesProcessors = rulesProcessors;
	}

}
