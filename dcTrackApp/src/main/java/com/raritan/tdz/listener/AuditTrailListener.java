package com.raritan.tdz.listener;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.audit.AuditTrailHelper;
import com.raritan.tdz.audit.domain.AuditTrail;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.field.domain.Fields;
import com.raritan.tdz.session.FlexUserSessionContext;

public class AuditTrailListener implements PostUpdateEventListener {

    private static final long serialVersionUID = 3842219603961288450L;
    
    private static Logger log = Logger.getLogger( "AuditTrailListener" );
    
    @Transactional( propagation=Propagation.SUPPORTS )
    public void onPostUpdate( PostUpdateEvent event ) throws HibernateException {
        	
    	if ( isDisableAuditTrail() ) return;
    	
		EventSource _session = event.getSession();
		
		Session session = _session.getSessionFactory().openSession();
		
		try {
		
			_session.getPersistenceContext().setFlushing(true);
			//JB check this _session.getJDBCContext().getConnectionManager().flushBeginning();
			
		
			final Object entity = event.getEntity();
			final Object[] values = event.getState();	
			final Object[] oldState = event.getOldState();
			final EntityPersister persister = event.getPersister();
			
			if ( oldState==null )  return;
			
			int[] dirtyProperties = persister.findDirty( oldState, values,entity, _session );

	        // Collect data from the EntityPersister
	        final String[] propertyNames = persister.getPropertyNames();

	        // Logging of the Dirty Properties
	        if ( dirtyProperties!=null ) {
	        
	        	AuditTrailHelper helper = new AuditTrailHelper( session );
	        	String auditAction = "UPDATE";
				final Serializable recordId = persister.getIdentifier( entity, _session );
	        	String tableName = helper.getTableName( persister );
	        	
	        	log.info( "onPostUpdate >>> tableName : " + tableName );
	        	
	        	if ( tableName==null || 
	           		 tableName.trim().equalsIgnoreCase( "dct_audit_trail" ) || 
	           		 tableName.trim().equalsIgnoreCase( "dct_ports_power" ) || 
	           		 tableName.trim().equalsIgnoreCase( "dct_ports_data" ) || 
	           		 tableName.trim().equalsIgnoreCase( "dct_circuits_power" ) || 
	           		 tableName.trim().equalsIgnoreCase( "dct_circuits_data" )) return;

	        	String itemId = helper.getItemId(_session, persister, entity );

	        	log.info( "onPostUpdate >>> itemId : " + itemId );
	        	
	        	if ( itemId==null || itemId.trim().equals( "" ) )  return;
	        	
	            String userName = "";
	            UserInfo user = FlexUserSessionContext.getUser();

	            if ( user!=null ) {
	            	userName = user.getUserName();
	            }

	            for ( int i=0; i<dirtyProperties.length; i++ ) {
	                int dirtyIndex = dirtyProperties[ i ];
	                
	                try {

	                	String propertyName = propertyNames[ dirtyIndex ];
	                
						Object oldEntity = oldState[ dirtyIndex ];
						Object newEntity = values[ dirtyIndex ];
	                
						Map< ?, ? > oldMap = helper.getEntityValue( entity, oldEntity, propertyName );
						Map< ?, ? > newMap = helper.getEntityValue( entity, newEntity, propertyName );

						Object oldValue = oldMap.get( AuditTrailHelper.VALUE );
						Object newValue = newMap.get( AuditTrailHelper.VALUE );

						String _oldValue = oldValue==null ? "" : oldValue.toString().trim();
						String _newValue = newValue==null ? "" : newValue.toString().trim();
						
						if ( (_oldValue.equalsIgnoreCase( "(blank)" )||_oldValue.equals( "" )) && 
						     (_newValue.equalsIgnoreCase( "(blank)" )||_newValue.equals( "" )) ) {
							continue;
						}
						
						if ( _newValue.length()>500 ) {
							_newValue = _newValue.substring( 0, 500 );
						}
						
						String dbColumnName = oldMap.get( AuditTrailHelper.DB_COLUMN_NAME ).toString();
						String dbFieldId = oldMap.get( AuditTrailHelper.DB_FIELD_ID ).toString();
						String displayName = oldMap.get( AuditTrailHelper.DISPLAY_NAME ).toString();
	                
						AuditTrail auditTrail = new AuditTrail();
						auditTrail.setDbColumnName( dbColumnName );
						Fields field = new Fields();
						field.setFieldId( new Long( dbFieldId==null || dbFieldId.trim().equals( "" ) ? "0" : dbFieldId ) );
						auditTrail.setField( field );
						auditTrail.setDisplayName( displayName );
						auditTrail.setAuditAction( auditAction );
						String rid = recordId.toString();
						auditTrail.setRecordId( Long.parseLong( rid==null || rid.trim().equals( "" ) ? "0" : rid ) );
						auditTrail.setTableName( tableName );
						auditTrail.setOldValue( _oldValue );
						auditTrail.setNewValue( _newValue );
						auditTrail.setChangeBy( userName );
						auditTrail.setChangeDate( new Timestamp( new Date().getTime() ) );
						auditTrail.setItem( new Item( Long.parseLong( itemId==null || itemId.trim().equals( "" ) ? "0" : itemId ) ) );
						
						Query query;
						
						if ( "dct_item_details".equalsIgnoreCase( tableName ) ) {
							query = session.createQuery( "from Item where itemServiceDetails.itemServiceDetailId=" + itemId );//.setFlushMode( FlushMode.MANUAL );
						} else {
							query = session.createQuery( "from Item where itemId=" + itemId );//.setFlushMode( FlushMode.MANUAL );
						}
						
						List< ? > itemList = query.list();
						if ( itemList.size()>0 ) {
							Item item = (Item)((Item)itemList.get( 0 )).clone();
							auditTrail.setLocation( item.getDataCenterLocation() );
							auditTrail.setClassLookup( item.getClassLookup() );
						} 

						if ( auditTrail.getLocation()==null || auditTrail.getLocation().getDataCenterLocationId()==null ) {
							return;
						}
						
	            		session.save( auditTrail );
	            		session.flush();
	                } catch ( Exception e ) {
	                	log.info( "", e );
	                } 
	            }
	        }
		} finally {
			try {
				if ( session!=null ) {
					session.close();
				}
			} catch ( Exception ex ) {}
			
        	_session.getPersistenceContext().setFlushing(false);
        	//JB check this _session.getJDBCContext().getConnectionManager().flushEnding();
		}
        
    	log.info( "onPostUpdate >>> out ");
    }
    
    @Transactional( propagation=Propagation.SUPPORTS )
    public void saveAuditDataForInsert( Long itemId, Session session ) throws HibernateException {
 		
    	if ( isDisableAuditTrail() ) return;
    	
   	 	String auditAction = "INSERT";
   	 	String tableName = "dct_items";
   	 
		if ( itemId==null )  return;
		
		String userName = "";
		UserInfo user = FlexUserSessionContext.getUser();
        if( user!=null ) {
        	userName = user.getUserName();
        }

		try {
			AuditTrail auditTrail = new AuditTrail();
			auditTrail.setDbColumnName( "item_name" );
			Fields field = new Fields();
			field.setFieldId( new Long( 1 ) );
			auditTrail.setField( field );
			auditTrail.setDisplayName( "Name" );
			auditTrail.setAuditAction( auditAction );
			//String rid = recordId.toString();
			//auditTrail.setRecordId( Long.parseLong( rid==null || rid.trim().equals( "" ) ? "0" : rid ) );
			auditTrail.setRecordId( itemId );
			
			auditTrail.setTableName( tableName );
			auditTrail.setChangeBy( userName );
			auditTrail.setChangeDate( new Timestamp( new Date().getTime() ) );
			auditTrail.setItem( new Item( itemId ) );
			
			log.info( "[ onPostInsert ] itemId : " + itemId );
			
			Query query = session.createQuery( "from Item where itemId=" + itemId );//.setFlushMode( FlushMode.MANUAL );
			
			List<?> itemList = query.list();
			if ( itemList.size()>0 ) {
				Item item = (Item)((Item) itemList.get( 0 )).clone();
				auditTrail.setLocation( item.getDataCenterLocation() );
				auditTrail.setClassLookup( item.getClassLookup() );
				auditTrail.setNewValue( item.getItemName() );

				log.info( "[ onPostInsert ] itemList.size()>0..." );
				log.info( "[ onPostInsert ] item.getDataCenterLocation() : " + item.getDataCenterLocation() );
			} else {
				log.info( "[ onPostInsert ] itemList.size()<=0..." );
			}

			String queryString = 
					"INSERT INTO dct_audit_trail (" +
							"location_id, item_id, class_lks_id, db_column_name, field_id, display_name," + 
							"audit_action, record_id, table_name, old_value, new_value, change_by, change_date)" +
						"VALUES (" + 
							":location_id, :item_id, :class_lks_id, :db_column_name, :field_id, :display_name, " + 
							":audit_action, :record_id, :table_name, :old_value, :new_value, :change_by, :change_date)" ;
			if ( auditTrail.getLocation()==null || auditTrail.getLocation().getDataCenterLocationId()==null ) {
				return;
				//queryString = queryString.replaceAll( ":location_id,", "" );
				//queryString = queryString.replaceAll( "location_id,", "" );
			}
			
			if ( auditTrail.getClassLookup()==null || auditTrail.getClassLookup().getLksId()==null ) {
				queryString = queryString.replaceAll( ":class_lks_id,", "" );
				queryString = queryString.replaceAll( "class_lks_id,", "" );
			}

			query = session.createSQLQuery( queryString );
			
			if ( auditTrail.getLocation()!=null && auditTrail.getLocation().getDataCenterLocationId()!=null ) {
				query.setLong( "location_id", auditTrail.getLocation().getDataCenterLocationId() );
			}
			
			if ( auditTrail.getClassLookup()!=null && auditTrail.getClassLookup().getLksId()!=null ) {
				query.setLong( "class_lks_id", auditTrail.getClassLookup().getLksId() );
			}	
			
			query.setLong( "item_id", new Long( itemId ) );
			query.setString( "db_column_name", "item_name" );
			query.setLong( "field_id", new Long( 1 ) );
			query.setString( "display_name", "Name" );
			query.setString( "audit_action", auditAction );
			query.setLong( "record_id", auditTrail.getRecordId() );
			query.setString( "table_name", tableName );
			query.setString( "old_value", "" );
			query.setString( "new_value", auditTrail.getNewValue() );
			query.setString( "change_by", auditTrail.getChangeBy() );
			query.setTimestamp( "change_date", auditTrail.getChangeDate() );
			
			query.executeUpdate();
    		
		} catch ( Exception e ) {
        	log.info( "", e );
		}
    }
    
    public void saveAuditDataForDelete(Item item, Session session ) throws HibernateException {
    	
    	if ( isDisableAuditTrail() ) return;
    	
   	 	String auditAction = "DELETE";
		String tableName = "dct_items";
		
		if ( item == null )  return;
		
		String userName = "";
		UserInfo user = FlexUserSessionContext.getUser();
        if( user!=null ) {
        	userName = user.getUserName();
        }

		try {
			AuditTrail auditTrail = new AuditTrail();
			auditTrail.setDbColumnName( "item_name" );
			Fields field = new Fields();
			field.setFieldId( new Long( 1 ) );
			auditTrail.setField( field );
			auditTrail.setDisplayName( "Name" );
			auditTrail.setAuditAction( auditAction );
			auditTrail.setRecordId( item.getItemId() );
			auditTrail.setTableName( tableName );
			auditTrail.setChangeBy( userName );
			auditTrail.setChangeDate( new Timestamp( new Date().getTime() ) );
			auditTrail.setItem( new Item( item.getItemId() ) );
			auditTrail.setLocation( item.getDataCenterLocation() );
			auditTrail.setClassLookup( item.getClassLookup() );
			auditTrail.setNewValue( item.getItemName() );
		
			String queryString = "INSERT INTO dct_audit_trail (" +
					"location_id, item_id, class_lks_id, db_column_name, field_id, display_name," + 
					"audit_action, record_id, table_name, old_value, new_value, change_by, change_date)" +
				"VALUES (" + 
					":location_id, :item_id, :class_lks_id, :db_column_name, :field_id, :display_name, " + 
					":audit_action, :record_id, :table_name, :old_value, :new_value, :change_by, :change_date)";
			
			if ( auditTrail.getLocation()==null || auditTrail.getLocation().getDataCenterLocationId()==null ) {
				return;
				//queryString = queryString.replaceAll( ":location_id,", "" );
				//queryString = queryString.replaceAll( "location_id,", "" );
			}
			
			if ( auditTrail.getClassLookup()==null || auditTrail.getClassLookup().getLksId()==null ) {
				queryString = queryString.replaceAll( ":class_lks_id,", "" );
				queryString = queryString.replaceAll( "class_lks_id,", "" );
			}
			
			Query query = session.createSQLQuery( queryString );
			
			if ( auditTrail.getLocation()!=null && auditTrail.getLocation().getDataCenterLocationId()!=null ) {
				query.setLong( "location_id", auditTrail.getLocation().getDataCenterLocationId() );
			}
			
			if ( auditTrail.getClassLookup()!=null && auditTrail.getClassLookup().getLksId()!=null ) {
				query.setLong( "class_lks_id", auditTrail.getClassLookup().getLksId() );
			}

			query.setLong( "item_id", item.getItemId() );
			query.setString( "db_column_name", "item_name" );
			query.setLong( "field_id", new Long( 1 ) );
			
			query.setString( "display_name", "Name" );
			query.setString( "audit_action", auditAction );
			query.setLong( "record_id", item.getItemId() );
			query.setString( "table_name", tableName );
			query.setString( "old_value", auditTrail.getNewValue() );
			query.setString( "new_value", "" );
			query.setString( "change_by", auditTrail.getChangeBy() );
			query.setTimestamp( "change_date", auditTrail.getChangeDate() );

			query.executeUpdate();			
		} catch ( Exception e ) {
        	log.info( "", e );
		}
    }
        
    public boolean isDisableAuditTrail() {
    	boolean disableAuditTrail=false;
    
    	try {
    		disableAuditTrail = Boolean.parseBoolean(System.getProperty("dcTrack.disableAuditTrail"));
    	} catch( Exception e ) {
    	}
    
    	return disableAuditTrail;
    }
}