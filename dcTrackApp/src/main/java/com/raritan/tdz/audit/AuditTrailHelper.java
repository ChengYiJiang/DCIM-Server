package com.raritan.tdz.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.entity.EntityPersister;

import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.field.domain.Fields;
//import org.springframework.beans.factory.annotation.Autowired;

public class AuditTrailHelper {

	private static Logger log = Logger.getLogger( "AuditTrailListener" );

	//private EventSource session;
	private static Map<String, String> propertyMap;
	private static Map<String, String> dbColumnMap;
	private static Map<String, String> dbFieldIdMap;
	private static Map<String, String> displayNameMap;
	
	private static Map<Long, String> customFieldMap;
	
	public final static String DB_COLUMN_NAME="DB_COLUMN_NAME";
	public final static String DB_FIELD_ID="DB_FIELD_ID";
	public final static String DISPLAY_NAME="DISPLAY_NAME";
	public final static String VALUE="VALUE";

	public AuditTrailHelper() {
	
	}
    
    public AuditTrailHelper(Session session) {
    	 	
    	if ( propertyMap==null ) {
    		propertyMap=new HashMap<String, String>();
    		dbColumnMap=new HashMap<String, String>();
    		dbFieldIdMap=new HashMap<String, String>();
    		displayNameMap=new HashMap<String, String>();

			//<value>com.raritan.tdz.field.domain.FieldDetails</value>
    		Query query = session.createQuery("from Fields");
    		List<Fields> fieldsList = query.list();
    		for ( Fields fields : fieldsList ) {
    			
    			String entityAttributeName = fields.getEntityAttributeName();
    			if ( entityAttributeName!=null && !entityAttributeName.trim().equals("")) {
    				entityAttributeName = entityAttributeName.trim();
    				String[] entityAttributeNameParts = entityAttributeName.split("\\.");
    				if ( entityAttributeNameParts.length>1 ) {
    					propertyMap.put( entityAttributeNameParts[0], entityAttributeNameParts[1] );
    					log.info( ">> propertyMap.key   : " + entityAttributeNameParts[0] );
    					log.info( ">> propertyMap.value : " + entityAttributeNameParts[1] );
    				} else if ( entityAttributeNameParts.length>0 ) {
    					propertyMap.put( entityAttributeName, "" );
    					log.info( ">> propertyMap.key   : " + entityAttributeName );
    					log.info( ">> propertyMap.value : " + "" );
    				}
    				
    				if ( entityAttributeNameParts.length>0 ) {
    	    			String dbColumnName = fields.getDbColumnName();
    					dbColumnName = dbColumnName==null || dbColumnName.trim().equals("") ? null : dbColumnName;
    					if ( dbColumnName!=null ) {
            				dbColumnMap.put( entityAttributeNameParts[0], dbColumnName );
        					log.info( ">> dbColumnMap.key   : " + entityAttributeNameParts[0] );
        					log.info( ">> dbColumnMap.value : " + dbColumnName );
    					}
        				dbFieldIdMap.put( entityAttributeNameParts[0], fields.getFieldId().toString() );
        				displayNameMap.put( entityAttributeNameParts[0], fields.getDefaultName() );
    				}
    			}
    		}
    	}
    	
    	if ( customFieldMap==null ) {
    		customFieldMap=new HashMap<Long, String>();

			//<value>com.raritan.tdz.field.domain.FieldDetails</value>
    		Query query = session.createQuery("from LkuData where lkuTypeName='CUSTOM_FIELD'");
    		List<LkuData> dataList = query.list();
    		for ( LkuData lkuData : dataList ) {
    		
    			Long lkuId=lkuData.getLkuId();
    			String lkuValue=lkuData.getLkuValue();
    			
    			customFieldMap.put(lkuId,lkuValue);
    		
    		}
    	}
    	
    	log.info("customFieldMap="+customFieldMap);
    	
    }
    
    public Map getEntityValue(Object entity,Object dirtyObject,String propertyKey) {
    	Map returnMap=new HashMap();
    	
    	Object value=null;
    
    	if(dirtyObject!=null) 
	    	log.info("dirtyObject="+dirtyObject.getClass());
	    	
	    	log.info("entity="+entity.getClass());
	    	
    	log.info("propertyKey="+propertyKey);
    	try {
    	
    		//Specific process for custom field
			if(entity instanceof com.raritan.tdz.domain.CustomItemDetails && 
				"attrValue".equals(propertyKey)) {
				
				Long lkuId=((CustomItemDetails)entity).getCustomAttrNameLookup().getLkuId();
				
				String displayName=(String)customFieldMap.get(lkuId);
				
				value = (dirtyObject!=null) ? dirtyObject.toString() : "(blank)";
				
				String dbColumnName="attr_val";
				
				returnMap.put(DB_COLUMN_NAME,dbColumnName);
				returnMap.put(DB_FIELD_ID,lkuId.toString());
				returnMap.put(DISPLAY_NAME,displayName);
    		
    		} else {
    	
				String propertyName=(String)propertyMap.get(propertyKey);
				log.info("propertyName="+propertyName);
				if(propertyName==null) {
					throw new Exception("property not found:"+propertyKey);
				}
					
				if("".equals(propertyName)) {
				
					if("used".equals(propertyKey)) {
						value = ((Boolean)dirtyObject).booleanValue() ? "Connected:" : "Not Connected";
					} else {
						value = (dirtyObject!=null) ? dirtyObject.toString() : "(blank)";
					}
					
					
				} else {

					//CR48602 Deal with userName property - firstName + lastName
					if(dirtyObject!=null &&
						dirtyObject instanceof com.raritan.tdz.domain.cmn.Users &&
						"userName".equalsIgnoreCase(propertyName)) {
						Users users=(Users)dirtyObject;
						if(users!=null) {
							value=users.getFirstName()+" "+users.getLastName();
						}
					
					} else {
						value = (dirtyObject!=null) ? BeanUtils.getProperty(dirtyObject,propertyName) : "(blank)";
					}
					
				}
				
				String dbColumns=(String)dbColumnMap.get(propertyKey);
				if(dbColumns==null) {
					throw new Exception();
				}
				
				log.info("dbColumns="+dbColumns);
				String[] dbColumn=dbColumns.split("\\.");
				String dbTableName=dbColumn[0];
				String dbColumnName=dbColumn[1];
				
				String dbFieldId=dbFieldIdMap.get(propertyKey);
				String displayName=displayNameMap.get(propertyKey);
				
				returnMap.put(DB_COLUMN_NAME,dbColumnName);
				returnMap.put(DB_FIELD_ID,dbFieldId);
				returnMap.put(DISPLAY_NAME,displayName);
				
			}

    	} catch(Exception e) {
    		//log.error(e.toString());
    		value=null;
    	}
    	
    	returnMap.put(VALUE,value);
    
    	return returnMap;
    }

	public String getItemId(EventSource session,EntityPersister persister,Object entity) {
		String itemId="";
		
		try {
			Class<?> mappedClass=persister.getMappedClass(); //getMappedClass(EntityMode.POJO);
			if (mappedClass.equals(com.raritan.tdz.domain.ItemServiceDetails.class) ||
				mappedClass.equals(com.raritan.tdz.domain.CabinetItem.class) ||
				mappedClass.equals(com.raritan.tdz.domain.MeItem.class) ||
				mappedClass.equals(com.raritan.tdz.domain.ItItem.class) ||
				mappedClass.equals(com.raritan.tdz.domain.Item.class)
			) {
				final Serializable id = persister.getIdentifier(entity,session);
				itemId=id.toString();
				
			} else if(mappedClass.equals(com.raritan.tdz.domain.DataPort.class) ||
				mappedClass.equals(com.raritan.tdz.domain.CustomItemDetails.class) 
			) {
				itemId = (entity!=null) ? BeanUtils.getProperty(entity,"item.itemId") : "";
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return itemId;
	}
	
	public String getTableName(EntityPersister persister) {
	
		String tableName="";
	
		Class<?> mappedClass=persister.getMappedClass(); //getMappedClass(EntityMode.POJO);
		
		log.info("persister.getMappedClass="+mappedClass);
		
		Class<?> superClass=null;
		
		superClass=mappedClass.getSuperclass();
		
		if (superClass.equals(java.lang.Object.class)) {
			superClass=mappedClass;
		}
		
		log.info("superClass="+superClass);
		
		Table table = superClass.getAnnotation(Table.class);
		tableName = table.name();
		
		tableName=tableName.replace("`","");
		
		log.info("tableName="+tableName);
	
		return tableName;
	}

}