package com.raritan.tdz.page.home;

import java.beans.PropertyDescriptor;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.Fields;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.page.domain.PglistSettings;
import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.ColumnGroupDTO;
import com.raritan.tdz.page.dto.FilterDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.dto.LookupOptionDTO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.ChassisTracerHandler;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * 
 * @author Randy Chen
 */
public class PaginatedHomeBase {

	private static Map defaultNameMap;

	public static final String COMMA="%2C";
	
	protected Logger log = Logger.getLogger( this.getClass() );
	protected SessionFactory sessionFactory;
	
	protected Map columnGroupMap;
	protected String columnAttributes;
	protected Map columnAttributeMap;
	protected Map columnDTOMap;
	protected Map fieldNameUidMap;
	protected Map uidFieldNameMap;
	protected Map fieldLabelMap;
	
	public static int MAX_LINES_PER_PAGE = 25;
	public static int DEFAULT_PAGE_NUMBER = 1;
	
	//CR49126 - for special six column mapping checking
	protected static Map<String,List> lookupMap=new HashMap<String,List>();

	protected RulesProcessor rulesProcessor;
	protected RemoteRef remoteReference;
	
	private static int DEF_DB_TIMEZONE_UNKNOW = 25;
	
	private static int DEF_DB_TIMEZONE = -4;
	
	private static int USER_MOCK = 0;
	
	private static int USER_NONE = -1;
	
	private int dbTimezoneOffset = DEF_DB_TIMEZONE_UNKNOW;
	
	private static String SPACE = " ";
	
	public static final String PAGE_TYPE_ITEMLIST = "itemList";
	
	//private String clientUtcTimeString="";
	
	private Map<String,String> clientUtcTimeMap=new HashMap<String,String>();
	
	public PaginatedHomeBase( SessionFactory sessionFactory ) {
		this.sessionFactory = sessionFactory;
		log.info("Init PaginatedHomeBase!");		
		
	}
	
	public void setClientUtcTimeString(String utc){
		UserInfo currentUser = FlexUserSessionContext.getUser();
		String userId = "";
		if(currentUser != null){
			userId = currentUser.getUserId();
		}else{ // for developing environment
			userId = "admin";
		}
		
		clientUtcTimeMap.put(userId, utc);
	}
	
	public String getClientUtcTimeString(){
		UserInfo currentUser = FlexUserSessionContext.getUser();
		String userId = "";
		if(currentUser != null){
			userId = currentUser.getUserId();
		}else{ // for developing environment default user id
			userId = "admin";
		}		
				
		return clientUtcTimeMap.get(userId);
	}
	
	/** Common function used for configuring the client utc time string.  */
	protected void configClientUtcTime(ListCriteriaDTO listCriteriaDTO){
		
		String clientUtcTimeString = (listCriteriaDTO==null) ? "" : listCriteriaDTO.getCurrentUtcTimeString();
		if(clientUtcTimeString!=null && clientUtcTimeString.length()>0)	{
			setClientUtcTimeString(clientUtcTimeString);			
		}else{
			//@TODO Give the application server time here, when client utc time string lost 
		}
		
		log.info("configClientUtcTime="+this.getClientUtcTimeString());
	}
	
	protected void setDbTimezoneOffset(){ 				
		
		if(dbTimezoneOffset == DEF_DB_TIMEZONE_UNKNOW){
			try{
				String sql = " SELECT date_part('hour' , age( now(), now() at time zone 'UTC' ) )"; // result -4
				Session session = sessionFactory.getCurrentSession();
											
				dbTimezoneOffset  = ((Double)session.createSQLQuery(sql).uniqueResult()).intValue();
								
				log.info("**setDbTimezoneOffset..ok");
			} catch (Exception dae) {				
				//log.error("setDbTimezoneOffset:",dae.getMessage());
				log.error("setDbTimezoneOffset:"+dae.getMessage());
				
				dbTimezoneOffset = DEF_DB_TIMEZONE;
				log.info("**setDbTimezoneOffset - Using default timezone.");
			}	 
		}
	}
	
	protected int getDbTimezoneOffset(){
		
		if(dbTimezoneOffset == DEF_DB_TIMEZONE_UNKNOW) 	setDbTimezoneOffset();
		
		return dbTimezoneOffset;
	}
	
	private ListCriteriaDTO addAnyNewColumns(ListCriteriaDTO storedListCriteriaDTO, String pageType) {
		// THIS IS A HACK TO ADD SHOW NEW COLUMNS ON UI WHEN USER HAD SAVED HIS PREFERENCE AND
		// THERE IS A NEW COLUMN ADDED in columns.xml FOR CIRCUIT LIST. 
		if (pageType.equals("circuitList") && storedListCriteriaDTO != null) {
			// user setting - saved in database
			List<ColumnDTO> userColumns = storedListCriteriaDTO.getColumns();
			// default settings - shows all columns including any new ones added
			List<ColumnDTO> defaultColumns = getColumnConfig(true, pageType);

			// add any news columns to users columns dto list
			for (ColumnDTO defaultColumn : defaultColumns) {
				boolean foundMatch = false;
				for (ColumnDTO userColumn : userColumns){
					if (userColumn.getUiComponentId().equals (defaultColumn.getUiComponentId()) ) {
						foundMatch = true; 
						break;
					}
				}
				
				if (!foundMatch) {
					int idx = defaultColumns.lastIndexOf(defaultColumn);
					userColumns.add(idx, defaultColumn);
				}
			}
			storedListCriteriaDTO.setColumns(userColumns);
		}
		return storedListCriteriaDTO; 
	}
	
	protected ListCriteriaDTO getListCriteriaDTO(ListCriteriaDTO listCriteriaDTO,String pageType) {		
		
		//save for fit rows per page
		//int fitLinesPerPage = 0;
		//int totalRows=0;
		int pageNumber = DEFAULT_PAGE_NUMBER;
		int maxLinesPerPage = MAX_LINES_PER_PAGE;
				
		log.info("input getListCriteriaDTO=["+listCriteriaDTO+"]");		
		
		boolean isFirstQuery= listCriteriaDTO != null ? listCriteriaDTO.isFirstQuery() : true;
		
		List<ColumnCriteriaDTO> columnCriteriaDTOList=null;
		List<ColumnDTO> columnDTOList=null;
				
		if(listCriteriaDTO!=null) {					
			
			/*
			if(listCriteriaDTO.getFitType()==ListCriteriaDTO.FIRST_LIST_QUERY){
				//this param is for the Fit's rows per page
				fitLinesPerPage = listCriteriaDTO.getMaxLinesPerPage();
			}
			*/
			
			pageNumber=listCriteriaDTO.getPageNumber();
			maxLinesPerPage=listCriteriaDTO.getMaxLinesPerPage();
			//log.info("getListCriteriaDTO--->listCriteriaDTO is not null! pageNumber="+pageNumber+" maxLinesPerPage="+maxLinesPerPage);
			
			columnCriteriaDTOList=listCriteriaDTO.getColumnCriteria();			
			columnDTOList=listCriteriaDTO.getColumns();
			
			//Fill up the rest of columnDTO attributes
			columnDTOList=updateColumnDTO(columnDTOList);
			
			//Prevent null value
			if(columnCriteriaDTOList==null) {
				columnCriteriaDTOList=new ArrayList<ColumnCriteriaDTO>();
		 		listCriteriaDTO.setColumnCriteria(columnCriteriaDTOList);
			}
			
		} else {			
			listCriteriaDTO = new ListCriteriaDTO();
			listCriteriaDTO.setPageNumber(pageNumber);
			listCriteriaDTO.setMaxLinesPerPage(maxLinesPerPage);
			
			columnCriteriaDTOList=new ArrayList<ColumnCriteriaDTO>();
			//columnDTOList=new ArrayList<ColumnDTO>();
			
			listCriteriaDTO.setColumnCriteria(columnCriteriaDTOList);
			listCriteriaDTO.setColumns(columnDTOList);
			
		}
				
		//log.info("listCriteriaDTO.getMaxLinesPerPage()="+listCriteriaDTO.getMaxLinesPerPage());
		//log.info("listCriteriaDTO.getFitType()="+listCriteriaDTO.getFitType());
				
		if(columnDTOList==null) {						
			//null -> get all columns and don't save user configuration
			log.info("**restore default columns");
			List<ColumnDTO> dtoList=getColumnConfig(true,pageType);
			listCriteriaDTO.setColumnCriteria(new ArrayList<ColumnCriteriaDTO>()); //empty ColumnCriteriaDTO dto
			listCriteriaDTO.setColumns(dtoList);
						
			listCriteriaDTO.setMaxLinesPerPage(maxLinesPerPage);
			listCriteriaDTO.setFitType(ListCriteriaDTO.FIT);
			
			try {
				saveUserConfig(listCriteriaDTO,pageType);				
			} catch (DataAccessException dae) {			
				log.error("saveUserConfig:",dae);
			}
			
			if("itemList".equals(pageType)) {
				syncCustomFields(dtoList);
			}
			
		} else if(columnDTOList.size()==0) {						
			log.info("**get default columns");
			//[] -> get default columns and save user configuration
			
			ListCriteriaDTO storedListCriteriaDTO=null;
			try {
				storedListCriteriaDTO=getUserConfig(pageType);
				log.info("**storedListCriteriaDTO="+storedListCriteriaDTO);
				// HACK HACK HACK
				// current design does not automatically show any new columns 
				// added to the circuit list. Hacking the code here to add new 
				// columns to the existing list before sending to the client. 
				// In current design the column settings are saved as  
				// serialized object in the database and hence not so flexible
				addAnyNewColumns( storedListCriteriaDTO, pageType);
			} catch (DataAccessException dae) {
				log.error("storedListCriteriaDTO:",dae);
				storedListCriteriaDTO=null;
			}			
			
			//If no user saved configuration 
			//get columnDTO from configuration file 
			if(storedListCriteriaDTO==null) {				
				List<ColumnDTO> dtoList=getColumnConfig(true,pageType);
	
				listCriteriaDTO.setColumns(dtoList);
				
				log.info("**set maxLinesPerPage="+maxLinesPerPage);
				log.info("**set setFitType="+ListCriteriaDTO.FIT);
				listCriteriaDTO.setMaxLinesPerPage(maxLinesPerPage);
				listCriteriaDTO.setFitType(ListCriteriaDTO.FIT);
				
				log.info("**setColumns dtoList="+dtoList.size());
				
				try {
					saveUserConfig(listCriteriaDTO,pageType);
					log.info("**saveUserConfig..ok");
				} catch (DataAccessException dae) {				
					log.error("saveUserConfig:",dae);
				}
				
			} else {
								
				listCriteriaDTO=storedListCriteriaDTO;
				log.info("**listCriteriaDTO=storedListCriteriaDTO");				
				log.info("**getMaxLinesPerPage="+listCriteriaDTO.getMaxLinesPerPage());
				log.info("**getFitType="+listCriteriaDTO.getFitType());				
			}
			
			if("itemList".equals(pageType)) {
				syncCustomFields(listCriteriaDTO.getColumns());
			}
			
		}else{
			log.info("getListCriteriaDTO---> columnDTOList != null! columnDTOList.size()="+columnDTOList.size());
		}
		
		//Sync up filterType of columnDTO - for add columns from client side
		listCriteriaDTO=updateDTOAttribute(listCriteriaDTO);
		
		//CR53063 Replace isFirstQuery attribute from client instead of server saved
		listCriteriaDTO.setFirstQuery(isFirstQuery);
		
		log.info("return getListCriteriaDTO=["+listCriteriaDTO+"]");				
		return listCriteriaDTO;
	}
		
	//getAlias method
	protected ArrayList<String> getAliases( String remoteType, String fieldName ) throws ClassNotFoundException {
	
		ArrayList<String> aliases = new ArrayList<String>();
		String traceStr = getFieldTrace( remoteType, fieldName );
		
		//log.info("fieldName="+fieldName+" traceStr="+traceStr+" remoteType="+remoteType);
		
		String aliasStr = traceStr.contains( "." ) ? traceStr.substring( 0, traceStr.lastIndexOf( "." ) ) : null;
		if ( aliasStr!=null ) {
			StringBuffer buffer = new StringBuffer();
			for ( String token: aliasStr.split( "\\." ) ) {
				buffer.append( token );
				aliases.add( buffer.toString() );
				buffer.append( "." );
			}
		}
		
		//log.info("aliasStr="+aliasStr+" remoteType="+remoteType);
		
		return aliases;
	}
	
	protected String getFieldTrace( String remoteType, String fieldname ) throws ClassNotFoundException {
	
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler( "^[a-z].*LkpValue", new LksDataValueTracerHandler() );
		objectTrace.addHandler( "^[a-z].*LkuValue", new LkuDataValueTracerHandler() );
		objectTrace.addHandler( "^[a-z].*LkpValueCode", new LksDataValueTracerHandler() );
		objectTrace.addHandler( "^[a-z].*LkuValueCode", new LkuDataValueTracerHandler() );
		objectTrace.addHandler( "itemAdminUser", new UserIdTracerHandler() );
		objectTrace.addHandler( "parentItem", new ParentTracerHandler() );
		
		objectTrace.addHandler( "upsBankItem", new ParentTracerHandler() );
		objectTrace.addHandler( "cracNwGrpItem", new ParentTracerHandler() );
		
		objectTrace.addHandler( "bladeChassis", new ChassisTracerHandler() );
		
		objectTrace.traceObject( Class.forName( remoteType ), fieldname );
		String trace = objectTrace.toString();

		return trace;
	
	}
	
	@Transactional(readOnly=true)
	public List<LookupOptionDTO> getLookupOption( ListCriteriaDTO listCriteriaDTO, String pageType ) throws DataAccessException {
	
		log.info("getLookupOption...");
		
		listCriteriaDTO=updateDTOAttribute(listCriteriaDTO);
			
		ArrayList<LookupOptionDTO> lookupOptionAL = new ArrayList<LookupOptionDTO>();
		
		//if ( listCriteriaDTO==null ) return lookupOptionAL;
				
		//ColumnDTO[] columnDTOArray = listCriteriaDTO.getColumns();
		
		//No more criteria
		//ColumnDTO[] columnDTOArray = (ColumnDTO[])(listCriteriaDTO.getColumns()).toArray(new ColumnDTO[0]);
		
		//Get all filierable fields
		/*
		List columnDTOList=new ArrayList();
		 for (Iterator it = columnDTOMap.entrySet().iterator(); it.hasNext();) {
		 	ColumnDTO dto=(ColumnDTO)entry.getValue();
		 }
		 */
		 
		 Collection mapDTOs=columnDTOMap.values();
		 ColumnDTO[] columnDTOArray = (ColumnDTO[])mapDTOs.toArray(new ColumnDTO[0]);
		

		if ( columnDTOArray==null || columnDTOArray.length==0 ) return lookupOptionAL;
		
		HashMap<String, String> lksHashMap = getLksHashMap( pageType );
		HashMap<String, String> lkuHashMap = getLkuHashMap( pageType );
		Session session = this.sessionFactory.getCurrentSession();
		
		HashSet valuesHashSet = new HashSet();
		
		for ( int i=0; i<columnDTOArray.length; i++ ) {			
			valuesHashSet.clear();

			//log.info( "[ getLookupOption().lks.fieledName ]" + columnDTOArray[i].getFieldName() );
			//log.info( "[ getLookupOption().lks.fieledName.isFilterable ]" + columnDTOArray[i].isFilterable() );
			
			if ( !columnDTOArray[i].isFilterable() ) continue;
			
			String fieldName = columnDTOArray[i].getFieldName();
			String fieldLabel = columnDTOArray[i].getFieldLabel();
			//log.info( "[ getLookupOption().lks.fieledName ]" + fieldName );
			
			if ( lksHashMap.containsKey( fieldName ) ) {
				
				Criteria criteria = session.createCriteria( com.raritan.tdz.domain.LksData.class );
				criteria.add( Restrictions.eq( "this.lkpTypeName", lksHashMap.get( fieldName ) ) );
				criteria.addOrder( Order.asc( "this.lkpValue" ) );
				
				if ( "classLkpValue".equals( fieldName ) ) {
					criteria.add( 
						Restrictions.not( 
							Restrictions.in( "this.lkpValue", new String[] { "Blanking Plate", "CRAC Group", "UPS Bank", "Passive", "Perforated Tiles" } ) 
						) 
					);
				}
				
				// For item list we are not supposed to show completed, archived and abandoned requests
				if( "stageIdLkpValue".equals(fieldName) && pageType.equals("itemList")){
					criteria.add( 
							Restrictions.not( 
								Restrictions.in( "this.lkpValue", new String[] { "Request Complete", 
										"Request Archived", "Request Abandoned" } ) 
							) 
						);
				}
				//CR47920 remove "Hidden" and "To Be Removed"
				if ( "statusLkpValue".equals( fieldName ) ) {
					criteria.add( 
						Restrictions.not( 
							Restrictions.in( "this.lkpValue", new String[] { "Hidden", "To Be Removed" } ) 
						) 
					);
				}
				
				//CR48382 show "Data" and "Power" only; remove "Sensor"
				if ( "circuitTypeLkpValue".equals( fieldName ) ) {
					criteria.add( 
						Restrictions.not( 
							Restrictions.in( "this.lkpValue", new String[] { "Sensor" } ) 
						) 
					);
				}

				//CR48636 Circuits List Status - Planned / Installed / Powered-Off / To Be Removed
				if( "status".equals( fieldName ) && "circuitList".equalsIgnoreCase( pageType )) {
					criteria.add( 
						Restrictions.not( 
							//CR49123 removed Power-off status from the exclusion list below, since it's needed as a filter choice in the status column of the circuits list
							Restrictions.in( "this.lkpValue", new String[] { "Off-Site", "Hidden" , "Archived", "Storage" } )
						) 
					);
				}

				List<?> list = criteria.list();
				
				if ( list!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					for ( Object element : list ) {
						LksData lksData = (LksData)element;
						String lkpValue = lksData.getLkpValue();
						if ( !"ALL".equalsIgnoreCase( lkpValue ) ) {
							if ( !valuesHashSet.contains( lkpValue ) ) {
								values.add( lkpValue );
								codes.add( lkpValue );
								valuesHashSet.add( lkpValue );			
							}
						}
					}
					//lookupOptionDTO.setFieldName( fieldName );
					lookupOptionDTO.setFieldName( fieldLabel );
					
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
				}
			} else if ( lkuHashMap.containsKey( fieldName ) ) {	
				
				Criteria criteria = session.createCriteria( com.raritan.tdz.domain.LkuData.class );
				criteria.add( Restrictions.eq( "this.lkuTypeName", lkuHashMap.get( fieldName ) ) );
				criteria.addOrder( Order.asc( "this.lkuValue" ) );
				
				List<?> list = criteria.list();
				
				if ( list!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					for ( Object element : list ) {
						LkuData lkuData = (LkuData)element;
						String lkuValue = lkuData.getLkuValue();
						if ( !valuesHashSet.contains( lkuValue ) && !"ALL".equalsIgnoreCase( lkuValue ) ) {
							codes.add( lkuValue );
							values.add( lkuValue );
							valuesHashSet.add( lkuValue );
							//log.info( "[ getLookupOption().lku.lkuValue ]" + lkuValue );
						}
					}
					//lookupOptionDTO.setFieldName( fieldName );
					lookupOptionDTO.setFieldName( fieldLabel );
					
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
				}
			} else if ( "createdBy".equals( fieldName ) && "circuitList".equalsIgnoreCase( pageType ) ) {
				
				session = this.sessionFactory.getCurrentSession();
				Query q = session.getNamedQuery( "AllUsersList" );
				List<?> list = q.list();
				if ( list!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					HashSet<String> uhs = new HashSet<String>();
					for ( Object element : list ) {
						UserInfo userInfo = (UserInfo)element;
						String userName = userInfo.getUserName();
						if ( !uhs.contains( userName ) ) {
							codes.add( userName );
							values.add( userName );
							uhs.add( userName );
						}
						//log.info( "[ getLookupOption().lku.lkuValue ]" + userName );
					}
					
					Object[] objs = codes.toArray();
					codes = new ArrayList<String>();
					values = new ArrayList<String>();
					Arrays.sort( objs );
					for ( int idx=0; idx<objs.length; idx++ ) {
						codes.add( objs[idx].toString() );
						values.add( objs[idx].toString() );
					}
					//lookupOptionDTO.setFieldName( fieldName );
					lookupOptionDTO.setFieldName( fieldLabel );
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
				}
			} else if ( ( "locationCode".equals( fieldName ) && "circuitList".equalsIgnoreCase( pageType ) ) || ( "code".equals( fieldName ) && "itemList".equalsIgnoreCase( pageType ) ) ) {
				
				Criteria criteria = session.createCriteria( com.raritan.tdz.domain.DataCenterLocationDetails.class );
				criteria.createAlias( "componentTypeLookup","componentTypeLookup" );
				criteria.add( Restrictions.eq( "componentTypeLookup.lkpValueCode", SystemLookup.DcLocation.ROOM ) );
				criteria.addOrder( Order.asc( "this.code" ) );
				
				List<?> list = criteria.list();
				
				if ( list!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					for ( Object element : list ) {
						DataCenterLocationDetails locationDetails = (DataCenterLocationDetails)element;
						String code = locationDetails.getCode();
						if ( !valuesHashSet.contains( code ) ) {
							codes.add( code );
							values.add( code );
							valuesHashSet.add( code );
							//log.info( "[ getLookupOption().lku.lkuValue ]" + code );
						}
					}
					//lookupOptionDTO.setFieldName( fieldName );
					lookupOptionDTO.setFieldName( fieldLabel );
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
				}
				
			} else if ( "mountedRailLkpValue".equals( fieldName ) && "itemList".equalsIgnoreCase( pageType ) ) {
			
				//CR49126 - for special six column mapping checking
				List lookupList=new ArrayList();
				
				//Jan. 2,2013
				String inString="";

				if( "Rails Used".equals( fieldLabel ) ) {
					inString = SystemLookup.RailsUsed.FRONT+","+SystemLookup.RailsUsed.REAR+","+SystemLookup.RailsUsed.BOTH;
				} else if( "Cabinet Side".equals( fieldLabel ) ) {
					inString = SystemLookup.RailsUsed.LEFT_REAR+","+SystemLookup.RailsUsed.RIGHT_REAR;
				}
				
				String queryHQL="SELECT lksData.lkpTypeName, lksData.lkpValue FROM LksData lksData WHERE lksData.lkpValueCode IN ( "+inString+" ) ORDER BY lksData.lkpValueCode";
				log.info("queryHQL="+queryHQL);
				Query query = session.createQuery( queryHQL );
				
				List<?> results = query.list();
				if ( results!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					for ( Object element : results ) {
						Object[] cols = (Object[])element;
						String lkpValue = cols[1]==null ? "" : cols[1].toString();
						codes.add( lkpValue );
						values.add( lkpValue );
						
						lookupList.add(lkpValue);
						
					}

					lookupOptionDTO.setFieldName( fieldLabel );
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
					
					lookupMap.put(fieldLabel,lookupList);
					
				}
				
			} else if ( "facingLkpValue".equals( fieldName ) && "itemList".equalsIgnoreCase( pageType ) ) {		
			
				//CR49126 - for special six column mapping checking
				List lookupList=new ArrayList();
				
				//Jan. 2,2013
				String queryHQL="SELECT DISTINCT lksData.lkpTypeName, lksData.lkpValue FROM LksData lksData WHERE lksData.lkpTypeName IN ( :lkpTypeValue ) ORDER BY lksData.lkpTypeName, lksData.lkpValue";
				Query query = session.createQuery( queryHQL );

				if( "Front Faces".equals( fieldLabel ) ) {
					query.setString( "lkpTypeValue" , SystemLookup.LkpType.FACING );
				} else if( "Orientation".equals( fieldLabel ) ) {
					query.setString( "lkpTypeValue" , SystemLookup.LkpType.ORIENTATION );
				} else if( "Depth Position".equals( fieldLabel ) ) {
					query.setString( "lkpTypeValue" , SystemLookup.LkpType.ZERO_U );
				} else if( "Chassis Face".equals( fieldLabel ) ) {
					query.setString( "lkpTypeValue" , SystemLookup.LkpType.FACE );
				}
				
				List<?> results = query.list();
				if ( results!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					for ( Object element : results ) {
						Object[] cols = (Object[])element;
						String lkpValue = cols[1]==null ? "" : cols[1].toString();
						if ( !"ALL".equalsIgnoreCase( lkpValue ) ) {
							codes.add( lkpValue );
							values.add( lkpValue );
							//log.info( "[ getLookupOption().lks.lkpValue ]" + lkpValue );
						}
						
						lookupList.add(lkpValue);
					}
					
					//lookupOptionDTO.setFieldName( fieldName );
					lookupOptionDTO.setFieldName( fieldLabel );
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
					
					lookupMap.put(fieldLabel,lookupList);
				}

			} else if ( "subclassLkpValue".equals( fieldName ) && "itemList".equalsIgnoreCase( pageType ) ) {		
				
				//Jan. 7,2013
				//CR48079
				String queryHQL="SELECT DISTINCT lksData.lkpValueCode, lksData.lkpValue "+
										"FROM LksData lksData "+
										"INNER JOIN lksData.lksData as parent "+
										"WHERE lksData.lkpTypeName='"+SystemLookup.LkpType.SUBCLASS+"' and "+
										"parent.lkpValueCode NOT IN ( "+SystemLookup.Class.PASSIVE+" ) "+
										"ORDER BY lksData.lkpValueCode";				
										
				Query query = session.createQuery( queryHQL );
				
				List<?> results = query.list();
				if ( results!=null ) {
					LookupOptionDTO lookupOptionDTO = new LookupOptionDTO();
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					for ( Object element : results ) {
						Object[] cols = (Object[])element;
						String lkpValue = cols[1]==null ? "" : cols[1].toString();
						codes.add( lkpValue );
						values.add( lkpValue );
					}
					//lookupOptionDTO.setFieldName( fieldName );
					lookupOptionDTO.setFieldName( fieldLabel );
					lookupOptionDTO.setCode( codes );
					lookupOptionDTO.setValue( values );
					lookupOptionAL.add( lookupOptionDTO );
				}				

			} else {
				//log.info( "[ getLookupOption().otherFieledName ]" + fieldName );
			}
		}
        
		
		// followings are for debugging.
		
		LookupOptionDTO[] lookupOptionDTOArray = new LookupOptionDTO[0];
		if ( lookupOptionAL.size()>0 ) {
			lookupOptionDTOArray = new LookupOptionDTO[ lookupOptionAL.size() ];
			for ( int i=0; i<lookupOptionAL.size(); i++ )
				lookupOptionDTOArray[i] = (LookupOptionDTO)lookupOptionAL.get(i);
		}
		
		/*		
		log.info( "[ getLookupOption().LookupOptionDTO ]" + lookupOptionDTOArray);
		if ( lookupOptionDTOArray!=null && lookupOptionDTOArray.length>0 ) {
			log.info( "[ getLookupOption().LookupOptionDTO[].length ]" + lookupOptionDTOArray.length );
			if ( lookupOptionDTOArray[0].getCode()!=null && lookupOptionDTOArray[0].getCode().size()>0 ) {
				log.info( "[ getLookupOption().lookupOptionDTO[0].code ]" + lookupOptionDTOArray[0].getCode().size() );
				log.info( "[ getLookupOption().lookupOptionDTO[0].code ]" + lookupOptionDTOArray[0].getCode() );
				for ( String a : lookupOptionDTOArray[0].getCode() ) {
					log.info( "[ getLookupOption().lookupOptionDTO[0].code ]" + a  );
				}
			}
			if ( lookupOptionDTOArray[0].getValue()!=null && lookupOptionDTOArray[0].getValue().size()>0 ) {
				log.info( "[ getLookupOption().lookupOptionDTO[0].value ]" + lookupOptionDTOArray[0].getValue().size() );
				log.info( "[ getLookupOption().lookupOptionDTO[0].value ]" + lookupOptionDTOArray[0].getValue() );
				for ( String a : lookupOptionDTOArray[0].getValue() ) {
					log.info( "[ getLookupOption().lookupOptionDTO[0].value ]" + a  );
				}
			}
		}
		*/
		
		//log.info("lookupOptionAL="+lookupOptionAL);
		
		return lookupOptionAL;
	}
	
	/**
	* Get lookup data by specific field
	*/
	public List<Map> getLookupData(String fieldName,String lkuTypeName,String pageType) {
		List lookupList=new ArrayList();
		
		Map lookupMap=new HashMap();
		
		Session session = this.sessionFactory.getCurrentSession();
		
		HashMap<String, String> lkuHashMap = getLkuHashMap( pageType );
	
		Criteria criteria = session.createCriteria( com.raritan.tdz.domain.LkuData.class );
		criteria.add( Restrictions.eq( "this.lkuTypeName", lkuHashMap.get( fieldName ) ) );
		criteria.addOrder( Order.asc( "this.lkuValue" ) );
		
		if(!"".equals(lkuTypeName)) {
			criteria.createAlias("lksData", "lksData");
			criteria.add( Restrictions.eq( "lksData.lkpValue", lkuTypeName ) );
		}
	
		List<?> list = criteria.list();
	
		if ( list!=null ) {

			for ( Object element : list ) {
				LkuData lkuData = (LkuData)element;
				String lkuValue = lkuData.getLkuValue();
				if ( !"ALL".equalsIgnoreCase( lkuValue ) ) {
				
					lookupMap=new HashMap();
					lookupMap.put("name",lkuValue);
					lookupMap.put("value",lkuValue);
				
					lookupList.add(lookupMap);
			
				}
			}

		}
	
		return lookupList;
	}
	
	private HashMap<String, String> getLksHashMap( String pageType ) {
		
		HashMap<String, String> hm = new HashMap<String, String>();

		if ( "itemList".equalsIgnoreCase( pageType ) ) {
			hm.put( "classLkpValue", SystemLookup.LkpType.CLASS );
			hm.put( "statusLkpValue", SystemLookup.LkpType.ITEM_STATUS );
			hm.put( "osiLayerLkpValue", SystemLookup.LkpType.OS_LAYER );
			hm.put( "originLkpValue", SystemLookup.LkpType.ITEM_ORIGIN ); //US1937 Add Origin column
			hm.put( "stageIdLkpValue", SystemLookup.LkpType.REQUEST_STAGE );
		} else if ( "circuitList".equalsIgnoreCase( pageType ) ) {
			hm.put( "circuitTypeLkpValue", SystemLookup.LkpType.PORT_CLASS );
			hm.put( "status", SystemLookup.LkpType.ITEM_STATUS );
			hm.put( "requestStage", SystemLookup.LkpType.REQUEST_STAGE ); //CR48628
		} 
		
		return hm;
	}
	
	private HashMap<String, String> getLkuHashMap( String pageType ) {
		
		HashMap<String, String> hm = new HashMap<String, String>();

		if ( "itemList".equalsIgnoreCase( pageType ) ) {
			hm.put( "purposeLkuValue", GlobalConstants.purposeLkuType );
			hm.put( "functionLkuValue", GlobalConstants.functionLkuType );
			hm.put( "departmentLkuValue", "DEPARTMENT" );
			hm.put( "slaProfileLkuValue", "SLA_PROFILE" );
			
			//New added - fetch add lookup
			hm.put( "osLkuValue", "OS" );
			hm.put( "domainLkuValue", "DOMAIN" );
			hm.put( "vmClusterLkuValue", "VM_CLUSTER" );
			hm.put( "itemAdminTeamLkuValue", "TEAM" );
			
			//For Cabinet grouping
			hm.put( "cabinetGrpLkuValue", "CABINET" );
			
		} else if ( "circuitList".equalsIgnoreCase( pageType ) ) {
			hm.put( "teamDesc", "TEAM" );
		} 
		
		return hm;
	}
	
	protected void processFilter( String domainType,
								  Criteria criteria,
								  ColumnCriteriaDTO[] columnCriteriaDTO,
								  ColumnDTO[] columnDTO,
								  Map aliasMap ,
								  String clientUtcNow) {
								  
		String unit="1";
		UserInfo userInfo = FlexUserSessionContext.getUser();
		if(userInfo!=null) {
			unit=userInfo.getUnits();
			log.info("processFilter id="+userInfo.getId()+" userId="+userInfo.getUserId()+" unit="+unit);
			long id=parseUserId(userInfo);
			String userId=String.valueOf(id);
			
		}
																								
		if ( columnCriteriaDTO!=null && columnCriteriaDTO.length>0 ) {
			//loop the criteria list to generate the hibernate criteria
			for ( int i=0; i<columnCriteriaDTO.length; i++ ) {					
			
				ColumnCriteriaDTO dto = columnCriteriaDTO[i];
								
				if ( dto==null ) {
					continue;
				}
				
				//get field name from label
				String label = dto.getName();
				String name = (String)fieldLabelMap.get(label);
				if(name==null) {
					name=label;
				}
	
				String fieldName = (String)aliasMap.get( name );
				
				if(name.indexOf("custom_")!=-1) {
					fieldName=name;
				}
				
				if("slotLabel".equals(name)) {
					fieldName=name;
				}
				
				if("parentItem".equals(name)) {
					fieldName=name;
				}
							
				if("bladeChassis".equals(name)) {
					fieldName=name;
				}
				
				if("ipAddresses".equals(name)) {
					fieldName=name;
				}
				
				if("itemRequestNumber".equals(name)){
					fieldName=name;
				}
				if( "stageIdLkpValue".equals(name)){
					fieldName=name;
				}
				if( "piqId".equals(name)){
					fieldName=name;
				}
								
				FilterDTO filterDTO = dto.getFilter();
						
				if ( filterDTO!=null) {
					int groupType = filterDTO.getGroupType();
					String greaterThan = filterDTO.getGreaterThan();
					String lessThan = filterDTO.getLessThan();
					String equal = filterDTO.getEqual();
					boolean isLookup = filterDTO.isLookup();
					String lookupCodes = filterDTO.getLookupCodes();
					
					int filterType = getFilterType(columnDTO,label );
					
					log.info( "-------------" );
					log.info( "name=" + name );
					log.info( "fieldName=[" + fieldName+"]" );
					log.info( "filterType=" + filterType );
					log.info( "equal=" + equal );
					log.info( "greaterThan=" + greaterThan );
					log.info( "lessThan=" + lessThan );
					log.info( "groupType=" + groupType );
					
					String uiComponentId=(String)fieldNameUidMap.get(name);
					if(uiComponentId!=null ) {
								
						try {
							String remoteRef = rulesProcessor.getRemoteRef(uiComponentId);
							UnitConverterIntf unitConverter = remoteReference.getRemoteRefUnitConverter(remoteRef);
																
							if(unitConverter!=null) {
								if(!"".equals(equal))
									equal = (unitConverter.normalize(equal, unit)).toString();
									
								if(!"".equals(greaterThan))
									greaterThan = (unitConverter.normalize(greaterThan, unit)).toString();
									
								if(!"".equals(lessThan))
									lessThan = (unitConverter.normalize(lessThan, unit)).toString();
							}
						
						} catch(org.apache.commons.jxpath.JXPathNotFoundException e) {
							//log.info("Normalize Bypass:"+e);
						}
					
					}
									
					if ( filterType==ColumnDTO.INTEGER || filterType==ColumnDTO.FLOAT) {
						setRestrictionsForNumericType(domainType, criteria, name, fieldName, filterDTO);
					} else if ( filterType==ColumnDTO.DATE) {						
						//criteria = parseDateCriteria(criteria, equal, greaterThan, lessThan, fieldName);	
						criteria = parseUtcDateCriteria(criteria, equal, greaterThan, lessThan, fieldName, getClientUtcTimeString());
					
					} else {					
						//for custom field
						if(fieldName.indexOf("custom_")!=-1) {							
							//Note: this is item view only
							criteria = processCustomFilter(criteria, equal, fieldName, columnDTO);
																									
						} else if("slotLabel".equals(fieldName)) {
							//for slot label
							//Note: this is item view only
							if ( equal!=null && !"".equals( equal ) ) {
								
								//CR50034 - ignore hard-code "bladechass10" alias
								//CR50420
								/*
								String sql="upper((select slot_label from dct_models_chassis_slots a,dct_models_chassis b,dct_items bladechassis "+
											"where "+
											"a.model_chassis_id=b.model_chassis_id and "+
											"a.slot_number=this_.slot_position and "+
											"b.model_id=bladechassis.model_id and this_2_.chassis_id=bladechassis.item_id and "+
											"this_.facing_lks_id=b.face_lks_id)) like '%"+equal.toUpperCase()+"%'";
								*/
											
								//CR52718 select slot_label or slot_position
								//If the slot label has been defined, display slot label otherwise, display slot position.
								String sql="upper("+
									"(case when "+
									"	(select slot_label "+
									"	from dct_models_chassis_slots a,dct_models_chassis b,dct_items bladechassis "+
									"	where "+
									"	a.model_chassis_id=b.model_chassis_id and "+
									"	a.slot_number=this_.slot_position and "+
									"	b.model_id=bladechassis.model_id and this_2_.chassis_id=bladechassis.item_id and "+
									"	this_.facing_lks_id=b.face_lks_id) is not null "+
									"then "+
									"	(select slot_label "+
									"	from dct_models_chassis_slots a,dct_models_chassis b,dct_items bladechassis "+
									"	where "+
									"	a.model_chassis_id=b.model_chassis_id and "+
									"	a.slot_number=this_.slot_position and "+
									"	b.model_id=bladechassis.model_id and this_2_.chassis_id=bladechassis.item_id and "+
									"	this_.facing_lks_id=b.face_lks_id) "+
									"else "+
									"	to_char( "+
									"		(case when this_.slot_position>0 then this_.slot_position else null end) "+
									"		,'FM999' "+
									"	) "+
										"end "+
									"))  like '%"+equal.toUpperCase()+"%'";
							
								criteria.add( Restrictions.sqlRestriction(sql));
							}
					
						} else {
							
							if ( equal!=null && !"".equals( equal ) ) {
														
								//US1978 for parentItem (Cabinet) column
								if("parentItem".equals(fieldName)) {
								
									//CR51805 - Add Floor PDU (11) to show it's parent
									String sql="upper((CASE WHEN  this_.class_lks_id in (6,12,13) THEN  this_.item_name "+
													"WHEN  this_.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_name from dct_items parentitem where this_.parent_item_id=parentitem.item_id) "+
														"ELSE null END)) "+
														"like '%"+equal.toUpperCase()+"%'";
														
									criteria.add( Restrictions.sqlRestriction(sql));
									
								} else if("bladeChassis".equals(fieldName)) {
																							
									//US2102 for bladeChassis column
									String sql="upper((CASE WHEN  this_.subclass_lks_id = 104 THEN  this_.item_name "+
													"WHEN this_.subclass_lks_id in (105) THEN "+
													"	("+
															"select items.item_name  from dct_items_it chassisItem,dct_items items "+
															"where "+
															"this_.item_id=chassisItem.item_id and chassisItem.chassis_id=items.item_id"+
													"	) "+
														"ELSE null END)) "+
														"like '%"+equal.toUpperCase()+"%'";
														
									criteria.add( Restrictions.sqlRestriction(sql));
								
								} else if("ipAddresses".equals(fieldName)) {
																							
									//US2212 Display IP Addresses in Items List
									String sql="(select array_to_string(array("+
									"	select addr.ipaddress "+
									"	from dct_items items, dct_ports_data ports, tblipteaming ipteam,tblipaddresses addr "+
									"	where "+
									"	items.item_id=ports.item_id and ports.port_data_id=ipteam.portid and ipteam.ipaddressid=addr.id and items.item_id=this_.item_id "+
									"),';')) "+
									"like '%"+equal+"%'";
														
									criteria.add( Restrictions.sqlRestriction(sql));
									
								} else if( "itemRequestNumber".equals(fieldName)){

									String sql = "(select r.requestno from tblrequest r " 
											+ "left join tblrequesthistory h on r.id=h.requestid "
											+ "left join dct_lks_data l on h.stageid = l.lks_id "
											+ "where h.current=true and r.itemid=this_.item_id "
											+ "and r.requesttype not in ('Disconnect', 'Reconnect', 'Connect') "
											+ "and l.lks_id in ( 931, 932, 933, 934, 935, 936 )) "
											+ "like '%"+equal+"%'";
									log.info("Request Number sql=" + sql);									
									criteria.add( Restrictions.sqlRestriction(sql));
								} else if( "piqId".equals(fieldName)) {

									String sql = "(select " + 
											" upper(case when (piq_name_app_setting.setting_value is not null AND piq_name_app_setting.setting_value != '') or (piq_host_app_setting.setting_value is not null AND piq_host_app_setting.setting_value != '') then 'Integrated ' else '' end) + " +  
											" upper(case when (piq_name_app_setting.setting_value is not null AND piq_name_app_setting.setting_value != '') then '''' + piq_name_app_setting.setting_value + '''' + ' ' else '' end) +  " + 
											" upper(case when (piq_host_app_setting.setting_value is not null AND piq_host_app_setting.setting_value != '') then '''' + piq_host_app_setting.setting_value + '''' + ' ' else '' end)  " + 
											" from dct_items item inner join dct_locations loc on loc.location_id = item.location_id  " + 
											
											" inner join dct_app_settings piq_name_app_setting  " + 
											" on ((piq_name_app_setting.app_setting_id = loc.piq_host_app_setting_id or " +  
											" piq_name_app_setting.parent_app_setting_id = loc.piq_host_app_setting_id)  " + 
											" and loc.piq_host_app_setting_id is not null and piq_name_app_setting.setting_lks_id = 1115) " +  
											
											" inner join dct_app_settings piq_host_app_setting  " + 
											" on ((piq_host_app_setting.app_setting_id = loc.piq_host_app_setting_id or " +  
											" piq_host_app_setting.parent_app_setting_id = loc.piq_host_app_setting_id)  " + 
											" and loc.piq_host_app_setting_id is not null and piq_host_app_setting.setting_lks_id = 1047) " +  
											
											" inner join dct_app_settings piq_enable_app_setting  " + 
											" on ((piq_enable_app_setting.app_setting_id = loc.piq_host_app_setting_id or " +  
											" piq_enable_app_setting.parent_app_setting_id = loc.piq_host_app_setting_id)  " + 
											" and loc.piq_host_app_setting_id is not null and piq_enable_app_setting.setting_lks_id = 1051) " +  
											
											" where item.piq_id IS NOT NULL and item.piq_id > 0  " + 
											" and item.item_id = this_.item_id  " + 
											" and piq_enable_app_setting.setting_value = 'true' " +  
											" LIMIT 1) " + 
											"like '%"+ equal.toUpperCase() + "%'";

									
									criteria.add( Restrictions.sqlRestriction(sql));
											
								} else {
									//Normal field
									criteria.add( Restrictions.like( fieldName, "%"+equal+"%" ).ignoreCase() );
								}
							}
						
						}
						
						if ( greaterThan!=null && !"".equals( greaterThan ) ) {
							criteria.add( Restrictions.gt( fieldName, greaterThan ).ignoreCase() );
						}
						if ( lessThan!=null && !"".equals( lessThan ) ) {
							criteria.add( Restrictions.lt( fieldName, lessThan ).ignoreCase() );
						}
					
					}
	
					log.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
					log.info( "isLookup:" + isLookup );
					log.info( "lookupCodes:" + lookupCodes );
					log.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
					
					if ( isLookup && lookupCodes!=null && !"".equals( lookupCodes.trim() ) ) {
						log.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
						log.info( lookupCodes );						
						log.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
												
						//CR51061 Replace splitter ',' to '%2C' 						
						String[] lkpCodes = lookupCodes.split(COMMA);
						
						Class fieldType = null;
						try {
							fieldType=getFieldType( Class.forName( domainType ), fieldName );
							
							if(fieldType==null)
								fieldType=getFieldType( Class.forName( "com.raritan.tdz.domain.CabinetItem" ), fieldName );
								
							if(fieldType==null)
								fieldType=getFieldType( Class.forName( "com.raritan.tdz.domain.ItItem" ), fieldName );

							if(fieldType==null)
								fieldType=getFieldType( Class.forName( "com.raritan.tdz.domain.MeItem" ), fieldName );

						} catch ( Exception e ) {
							log.info( e.toString() );
						}
						
						if( fieldType != null ) log.info( "for lookup fieldType=" + fieldType.getName() );
						//There is no relation from Item to the RequestStage, so we cannot use previous algorithm
						//to determine fieldType (projection done on Item class). Hence, stageIdLkpValue is treated separately
						if( fieldType == null && "stageIdLkpValue".equals(fieldName)){
							criteria.add( Restrictions.sqlRestriction(getStageIdFilterSqlString(lkpCodes)));
						}else if ( "java.lang.Long".equals( fieldType.getName() ) || "long".equals( fieldType.getName() ) ) {
							Long[] lkpCodesLong = new Long[lkpCodes.length];     
							for ( int j = 0; j<lkpCodesLong.length; j++ ) {     
								lkpCodesLong[j] = new Long( lkpCodes[j] );
							}							
							criteria.add( Restrictions.in( fieldName, lkpCodesLong ) );
						} else {
							criteria.add( Restrictions.in( fieldName, lkpCodes ) );
						}
					}
				}
			}
		}
	}


	private void setRestrictionsForNumericType(String domainType,
			Criteria criteria, String name, String fieldName, FilterDTO filterDTO) {
		String greaterThan = filterDTO.getGreaterThan();
		String lessThan = filterDTO.getLessThan();
		String equal = filterDTO.getEqual();
		Class fieldType = null;

		try {
			fieldType=getFieldType( Class.forName( domainType ), fieldName );

			if(fieldType==null)
				fieldType=getFieldType( Class.forName( "com.raritan.tdz.domain.CabinetItem" ), fieldName );

			if(fieldType==null)
				fieldType=getFieldType( Class.forName( "com.raritan.tdz.domain.ItItem" ), fieldName );

			if(fieldType==null)
				fieldType=getFieldType( Class.forName( "com.raritan.tdz.domain.MeItem" ), fieldName );

		} catch ( Exception e ) {
			log.info( e.toString() );
		}

		if(fieldType==null) {
			fieldType=java.lang.Integer.class;
		}


		log.info("fieldType from getFieldType():"+fieldType);
		log.info("domainType:"+domainType);
		log.info("fieldName:"+fieldName);


		if ( equal!=null && !"".equals( equal ) ) {
			try {
				double equalD = Double.parseDouble( equal );

				if ( "java.lang.Integer".equals( fieldType.getName() ) || "int".equals( fieldType.getName() ) ) {

					log.info( "equal INTEGER->=" + equal );
					criteria.add( Restrictions.eq( fieldName, new Integer( (int)equalD ) ) );

				} else if ( "java.lang.Long".equals( fieldType.getName() ) || "long".equals( fieldType.getName() ) ) {

					log.info( "equal LONG->=" + equal );
					criteria.add( Restrictions.eq( fieldName, new Long( (long)equalD ) ) );

				} else if ( "java.lang.Double".equals( fieldType.getName() ) || "double".equals( fieldType.getName() ) ) {

					log.info( "equal DOUBLE->=" + equal );
					criteria.add( Restrictions.eq( fieldName, new Double( equalD ) ) );
				}
			} catch ( NumberFormatException nfe) {
				log.error( "Bad format:" + equal, nfe );
			}
		}

		if ( greaterThan!=null && !"".equals( greaterThan ) ) {
			try {
				double greaterThanD = Double.parseDouble(greaterThan );
				if ( "java.lang.Integer".equals( fieldType.getName() ) || "int".equals( fieldType.getName() ) ) {

					log.info( "greaterThan INTEGER->=" + greaterThan );
					criteria.add( Restrictions.gt( fieldName, new Integer( (int)greaterThanD ) ) );

				} else if ( "java.lang.Long".equals( fieldType.getName() ) || "long".equals( fieldType.getName() ) ) {

					log.info( "greaterThan LONG->=" + greaterThan );
					criteria.add( Restrictions.gt( fieldName, new Long( (long)greaterThanD ) ) );

				} else if ( "java.lang.Double".equals( fieldType.getName() ) || "double".equals( fieldType.getName() ) ) {

					log.info( "greaterThan DOUBLE->=" + greaterThan );
					criteria.add( Restrictions.gt( fieldName, new Double( greaterThanD ) ) );
				}
			} catch ( NumberFormatException nfe) {
				log.error( "Bad format:" + equal, nfe );
			}
		}

		if ( lessThan!=null && !"".equals( lessThan ) ) {
			try {
				double lessThanD = Double.parseDouble(lessThan );
				if ( "java.lang.Integer".equals( fieldType.getName() ) || "int".equals( fieldType.getName() ) ) {

					log.info( "lessThan INTEGER->=" + lessThan );
					criteria.add( Restrictions.lt( fieldName, new Integer( (int)lessThanD ) ) );

				} else if ( "java.lang.Long".equals( fieldType.getName() ) || "long".equals( fieldType.getName() ) ) {

					log.info( "lessThan LONG->=" + lessThan );
					criteria.add( Restrictions.lt( fieldName, new Long( (long)lessThanD ) ) );

				} else if ( "java.lang.Double".equals( fieldType.getName() ) || "double".equals( fieldType.getName() ) ) {

					log.info( "lessThan DOUBLE->=" + lessThan );
					criteria.add( Restrictions.lt( fieldName, new Double( lessThanD ) ) );
				}
			} catch ( NumberFormatException nfe) {
				log.error( "Bad format:" + equal, nfe );
			}
		}
	}
	
	private String getStageIdFilterSqlString( String [] lkpCodes ){
		StringBuilder sql = new StringBuilder();
		sql.append("(select l.lkp_value from tblrequest r ");
		sql.append("left join tblrequesthistory h on r.id=h.requestid ");
		sql.append("left join dct_lks_data l on h.stageid = l.lks_id ");
		sql.append("where r.itemid=this_.item_id and h.current=true ");
		sql.append("and r.requesttype not in ('Disconnect', 'Reconnect','Connect') ");
		sql.append("and l.lkp_value in (");
		for( int l=0; l<lkpCodes.length; l++){
			sql.append("'");
			sql.append(lkpCodes[l]);
			sql.append("'");
			if( l< lkpCodes.length -1 ) sql.append(",");
		}
		sql.append(")) is not null");
		log.info("Request Stage=" + sql.toString());
		return sql.toString();
	}
	
	private Criteria processCustomFilter(Criteria criteria, String equalCond, String fieldName, ColumnDTO[] columnDTOArr){
		
		if ( equalCond!=null && !"".equals( equalCond ) ) {
			String[] splitArr=fieldName.split("_");
			String lkuId=splitArr[1];
			//check the custom field is merged fields or not
			String[] tmpCustArr;
			String tmpLkuId;
			
			String tmpFieldName;
			
			boolean hasCustomIds = false;
			
			String customIds ="";
			
			String sql = "";
			
			for(int idx=0; idx < columnDTOArr.length; idx++){
								
				tmpFieldName = columnDTOArr[idx].getFieldName();
				tmpCustArr = tmpFieldName.split("_");
				
				if( tmpCustArr!= null && tmpCustArr.length == 2 ){
					tmpLkuId = tmpCustArr[1];
					if(lkuId.equals(tmpLkuId)){
						hasCustomIds = true;					
						customIds = columnDTOArr[idx].getCustomIds();
						break;
					}
				}	
			}
			
			if(hasCustomIds){
				String[] lkuIdArr = customIds.split(",");
				
				String sqlCond="";
				
				if(lkuIdArr!= null && lkuIdArr.length>0){
				
					StringBuilder sbLkuIdSql=new StringBuilder("");
					sbLkuIdSql.append("in (");
					
					for(int idx=0; idx < lkuIdArr.length; idx++){
						sbLkuIdSql.append(lkuIdArr[idx]);
						
						if( idx != (lkuIdArr.length-1) ){//not the last record
							sbLkuIdSql.append(",");
						}
					}
					sbLkuIdSql.append(")");
					
					sqlCond = sbLkuIdSql.toString();
				}else{
					sqlCond = "="+lkuId;
				}
				
				sql="upper((select attr_val from dct_custom_fields a where "+
						"a.item_id=this_.item_id and "+
						"a.attrib_name_lku_id " + sqlCond + " limit 1)) like '%"+equalCond.toUpperCase()+"%'";
				
			}else{
				sql="upper((select attr_val from dct_custom_fields a where "+
								"a.item_id=this_.item_id and "+
								"a.attrib_name_lku_id="+lkuId+" limit 1)) like '%"+equalCond.toUpperCase()+"%'";
				
			}	
			criteria.add( Restrictions.sqlRestriction(sql));
		}//end if ( equalCond!=null && !"".equals( equalCond ) )
		
		return criteria;
	}
	
	/** Original non-UTC parsing function. */
	private Criteria parseDateCriteria(Criteria criteria, String equal, String greaterThan, String lessThan, String fieldName) {
		//08/29/2012
		SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
		
		if ( equal!=null && !"".equals( equal ) && !"MM/DD/YYYY".equals( equal ) ) {
		
			try {
				Date startDateTime = sdf.parse( equal + " 00:00:00" );
				Date endDateTime = sdf.parse( equal + " 23:59:59" );
				criteria.add( Restrictions.between( fieldName, startDateTime, endDateTime ) );
			} catch ( ParseException pe ) {
				log.error( "Bad format:" + equal, pe );
			}
		}
		
		if ( greaterThan!=null && !"".equals( greaterThan ) && !"MM/DD/YYYY".equals( greaterThan ) ) {
		
			try {
				Date endDateTime = sdf.parse( greaterThan + " 23:59:59" );
				criteria.add( Restrictions.gt( fieldName, endDateTime ) );
			} catch ( ParseException pe ) {
				log.error( "Bad format:" + greaterThan, pe );
			}
		}
		
		if ( lessThan!=null && !"".equals( lessThan ) && !"MM/DD/YYYY".equals( lessThan ) ) {
			try {
				Date startDateTime = sdf.parse( lessThan + " 00:00:00" );
				criteria.add( Restrictions.lt( fieldName, startDateTime ) );
			} catch ( ParseException pe) {
				log.error( "Bad format:" + greaterThan, pe );
			}
		}
		return criteria;
	}

	/**
	 * Parsing the client UTC time string.
	 * 1. Count client side offset.
	 * 2. Count database server side offset.
	 * 3. Using the new time string to query.
	 * #CR52156 DE694 - The cloning sequence is 1. Getting client time -> 2. Saving item time -> 3. Using the time criteria to query the new cloning item, 
	 * 					so this needs to correct the criteria time(especially minute, +5 or -5 to let the results appear). 
	 * @see          <a href="http://java.sun.com/docs/books/tutorial/i18n/format/simpleDateFormat.html">Java Tutorial</a>
	 */
	private Criteria parseUtcDateCriteria(Criteria criteria, String equal, String greaterThan, String lessThan, String fieldName, String clientUtcNow) {
		
		SimpleDateFormat sdf = new SimpleDateFormat( "M/d/yyyy HH:mm:ss Z" );
		
		int dbTimezoneOffset = this.getDbTimezoneOffset();		
				
		int clientTimezoneOffset = getClientTimezoneOffset(clientUtcNow);
				
		TimeZone t = TimeZone.getTimeZone("GMT"+String.valueOf(dbTimezoneOffset));
		
		Calendar cal = Calendar.getInstance(t);
		
		Date clientUtcDate = cal.getTime(); //avoid null
		int clientUtcHour = 0;
		int clientUtcMin = 0;
		int clientUtcSec = 0;
		
		try{
			clientUtcDate = sdf.parse(clientUtcNow);
			
			cal.setTime(clientUtcDate);
			
			clientUtcHour = cal.get(Calendar.HOUR_OF_DAY);			
			clientUtcMin = cal.get(Calendar.MINUTE);			
			clientUtcSec = cal.get(Calendar.SECOND);															
		}catch(ParseException pe){			
			log.error(pe);
		}						
		
		SimpleDateFormat sdfMdy = new SimpleDateFormat( "M/d/yyyy" );				
		
		if ( equal!=null && !"".equals( equal ) && !"MM/DD/YYYY".equals( equal ) ) {		
			try {				
				//get equal date time and set client hour/min/sec
				Date equalDate = sdfMdy.parse(equal);
				cal.setTime(equalDate);
				cal.set(Calendar.HOUR_OF_DAY,clientUtcHour);
				
				//time diff
				//cal.set(Calendar.MINUTE, (clientUtcMin - 5) );
				cal.set(Calendar.MINUTE, clientUtcMin );
				
				cal.set(Calendar.SECOND,clientUtcSec);
				
				cal.add(Calendar.HOUR_OF_DAY, -( clientTimezoneOffset - dbTimezoneOffset ) ); 
				
				log.info("clientTimezoneOffset - dbTimezoneOffset --->"+ (-( clientTimezoneOffset - dbTimezoneOffset )) );
								
				equalDate = cal.getTime();
																				
				cal.add(Calendar.HOUR_OF_DAY, -12); //-12
				
				cal.add(Calendar.MINUTE , -5);
				Date startDateTime = cal.getTime();
				
				log.info("startDateTime="+startDateTime);				
								
				cal.setTime(equalDate);
				cal.add(Calendar.HOUR_OF_DAY, +12); //+12
				
				cal.add(Calendar.MINUTE , 5);
				Date endDateTime = cal.getTime();
				log.info("endDateTime="+endDateTime);								
				
				criteria.add( Restrictions.between( fieldName, startDateTime, endDateTime ) );								
				
				log.info("startDateTime=" + startDateTime.toString() + "-endDateTime=" + endDateTime.toString());				
			} catch ( ParseException pe ) {
				log.error( "Bad format:" + equal, pe );
			} catch (Exception e){
				log.error( "Error when parsing equal date string -->" + equal, e );
			}
		}				
		
		
		
		if ( greaterThan!=null && !"".equals( greaterThan ) && !"MM/DD/YYYY".equals( greaterThan ) ) {
		
			try {
				log.info("greaterThan="+greaterThan);
				//Date endDateTime = sdf.parse( greaterThan );
				Date greaterThanDateTime = sdfMdy.parse(greaterThan);
				cal.setTime(greaterThanDateTime);
				
				cal.add(Calendar.HOUR_OF_DAY, -( clientTimezoneOffset - dbTimezoneOffset ) );
				
				cal.set(Calendar.HOUR_OF_DAY,clientUtcHour);
				
				cal.set(Calendar.MINUTE, (clientUtcMin-5) );
				
				cal.set(Calendar.SECOND,clientUtcSec);
				
				greaterThanDateTime = cal.getTime();								
				
				criteria.add( Restrictions.gt( fieldName, greaterThanDateTime ) );
				log.info("greaterThanDateTime=" + greaterThanDateTime.toString());																
							
			} catch ( ParseException pe ) {
				log.error( "Bad format:" + greaterThan, pe );
			}
		}				
		
		if ( lessThan!=null && !"".equals( lessThan ) && !"MM/DD/YYYY".equals( lessThan ) ) {
			try {
				log.info("lessThan="+lessThan);
				Date lessThanDateTime = sdfMdy.parse( lessThan );
				
				//count the hours between client and database tomezone first
				cal.add(Calendar.HOUR_OF_DAY, -( clientTimezoneOffset - dbTimezoneOffset ) );
				
				cal.setTime(lessThanDateTime);
				cal.set(Calendar.HOUR_OF_DAY,clientUtcHour);
								
				cal.set(Calendar.MINUTE, (clientUtcMin+5) );
				
				cal.set(Calendar.SECOND,clientUtcSec);
				
				lessThanDateTime = cal.getTime();				
				
				criteria.add( Restrictions.lt( fieldName, lessThanDateTime ) );
				log.info("lessThanDateTime=" + lessThanDateTime.toString());
				
			} catch ( ParseException pe) {
				log.error( "Bad format:" + greaterThan, pe );
			}
		}
		return criteria;
	}
	
	private int getClientTimezoneOffset(String clientTimeString){
		DecimalFormat df = new DecimalFormat("+#;-#");		
		int hours=0;		
		
		if( clientTimeString!=null && clientTimeString.trim().length() > 0 ){
			
			try{
				String[] arr = clientTimeString.split(SPACE);								
				// Parsing the string like '+0800'
				hours = (df.parse(arr[arr.length-1])).intValue() / 100;
				
			}catch(Exception e){//using db timezone				
				hours = getDbTimezoneOffset();
				log.info("parseUtcDateCriteria clientTimeString exception."+e.getMessage());
			}
		}else{			
			hours = getDbTimezoneOffset();
		}
		return hours;
	}
	
	
	
	
	protected ColumnCriteriaDTO[] processSorting( Criteria criteria,
								   ColumnCriteriaDTO[] columnCriteriaDTO,
								   ColumnDTO[] columnDTO,
								   Map aliasMap, Criteria secondCriteria ) {
											
		for ( int i=0; i<columnCriteriaDTO.length; i++ ) {					
		
			ColumnCriteriaDTO dto = columnCriteriaDTO[i];
			
			if ( dto==null ) {
				continue;
			}
			
			//get field name from label
			String label = dto.getName();
			String name = (String)fieldLabelMap.get(label);
			if(name==null) {
				name=label;
			}
			boolean toSort = dto.isToSort();
			
			log.info( "sort column name=" + name );
			log.info( "sort column toSort=" + toSort );
			
			if ( toSort ) {
				
				boolean sortDescending = dto.isSortDescending();
				
				String fieldName = (String)aliasMap.get( name );
				
				if(name.indexOf("custom_")!=-1) {
					fieldName=name;
				}
				
				if("slotLabel".equals(name)) {
					fieldName=name;
				}
				
				if("parentItem".equals(name)) {
					fieldName="cabinetName";
				}
				
				if("bladeChassis".equals(name)) {
					fieldName="chassis";
				}
				
				if("ipAddresses".equals(name)) {
					fieldName="addresses";
				}
				
				if("itemRequestNumber".equals(name)) {
					fieldName=name;
				}
				if("stageIdLkpValue".equals(name)) {
					fieldName=name;
				}
				if("piqId".equals(name)) {
					fieldName=name;
				}
				log.info( "order ... " + fieldName );
				
				Order order;
				if ( sortDescending ) {
					order = Order.desc( fieldName );
				} else {
					order = Order.asc( fieldName );
				}
				criteria.addOrder(order);
				if (secondCriteria != null)
					secondCriteria.addOrder(order);
			
			}
			

		}

		return columnCriteriaDTO;
	}
	
	public int getFilterType( ColumnDTO[] columnDTO, String checkFieldLabel ) {
		
		int filterType = 0;
		
		for ( ColumnDTO column : columnDTO ) {
			String fieldLabel = column.getFieldLabel();
			if ( fieldLabel.equals( checkFieldLabel ) ) {
				filterType = column.getFilterType();
				break;
			}	
		}
			
		return filterType;
	}
	
	public Class getFieldType( Class clazz, String fieldName ) {
		Class fieldType = null;
		
		try {
			if ( fieldName.indexOf( "this." )!=-1 ) {
				String propertyName = fieldName.replace( "this.", "" );
								
				PropertyDescriptor descriptor = new PropertyDescriptor( propertyName, clazz );
				fieldType = descriptor.getPropertyType();
				
			} else if ( fieldName.indexOf( "_" )!=-1 ) {
			
				String[] classNProperty = fieldName.split( "_" );
				String className = classNProperty[0];
				String propertyName = classNProperty[1];
				
				PropertyDescriptor descriptor  = new PropertyDescriptor( className, clazz );
				Class domainClass = descriptor.getPropertyType();
				
				log.info( "2 className=" + className );
				log.info( "2 propertyName=" + propertyName );
				log.info( "2 domainClass=" + domainClass );
				
				fieldType = this.getFieldType( domainClass, propertyName );
				
			} else if ( fieldName.indexOf( "." )!=-1 ) {
			
				String[] classNProperty = fieldName.split( "\\." );
				String className = classNProperty[0];
				String propertyName = classNProperty[1];
			
				PropertyDescriptor descriptor = new PropertyDescriptor( className, clazz );
				Class domainClass = descriptor.getPropertyType();
				
				log.info( "3 className=" + className );
				log.info( "3 propertyName=" + propertyName );
				log.info( "3 domainClass=" + domainClass );
	
				descriptor = new PropertyDescriptor( propertyName, domainClass );
				fieldType = descriptor.getPropertyType();
			}
		} catch ( Exception e ) {
			log.info( e.toString() );
		}
	
		return fieldType;
	}
	
	public String[] getFieldLabels(ColumnDTO[] columnDTO) {
		String[] fieldLabels=new String[columnDTO.length];
		
		defaultNameMap=getDefaultFieldName();
		
		log.info("defaultNameMap="+defaultNameMap);
		
		for(int i=0;i<columnDTO.length;i++) {
		
			String name=columnDTO[i].getFieldName();
			String label=columnDTO[i].getFieldLabel();
		
			//log.info("name="+name+" label="+label);
			
			int index=name.indexOf("LkuValue");
			if(index != -1) {
				name=name.substring(0,index)+"Lookup.lkuValue";
			}
			index=name.indexOf("LkpValue");
			if(index != -1) {
				name=name.substring(0,index)+"Lookup.lkpValue";
			}
		
			String fieldLabel=(String)defaultNameMap.get(name);
			
			if(fieldLabel==null) {
				fieldLabel=label;
			}
			
			fieldLabels[i]=fieldLabel;
			
			log.info("name="+name+" fieldLabels="+fieldLabels[i]);
		
		}
	
		return fieldLabels;
	}
	
	public Map getDefaultFieldName() {
	
		//if ( defaultNameMap==null ) {
		
			defaultNameMap=new HashMap<String, String>();
			
			Session session = this.sessionFactory.getCurrentSession();
		
			//<value>com.raritan.tdz.field.domain.FieldDetails</value>
    		Query query = session.createQuery("from Fields");
    		List<Fields> fieldsList = query.list();
    		for ( Fields fields : fieldsList ) {
    			//log.info( ">> DefaultName : " + fields.getDefaultName() );
    			String entityAttributeName = fields.getEntityAttributeName();
    			if ( entityAttributeName!=null && !entityAttributeName.trim().equals("")) {
    				entityAttributeName = entityAttributeName.trim();
    				String[] entityAttributeNameParts = entityAttributeName.split("\\.");
    				
    				if ( entityAttributeNameParts.length==1 ) {
        				defaultNameMap.put( entityAttributeName, fields.getDefaultName() );
    				} else if ( entityAttributeNameParts.length==2 ) {
    					if("lkpValue".equals(entityAttributeNameParts[1]) || "lkuValue".equals(entityAttributeNameParts[1])) {
    						defaultNameMap.put( entityAttributeName, fields.getDefaultName() );
    					} else if("itemAdminUser".equals(entityAttributeNameParts[0])) {
    						defaultNameMap.put( entityAttributeNameParts[0], fields.getDefaultName() );
    					} else {
    						defaultNameMap.put( entityAttributeNameParts[1], fields.getDefaultName() );
    					}
    				}
    				
    				
    			}
    		}
    	//}
	
		return defaultNameMap;
	}
	
	/**
	* Get the user's configuration by page type. The page type could be item or circuit. 
	* @param ListCriteriaDTO if it's null and server will retrieve from configure to generate a default ListCriteriaDTO.
	*/
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException{
		log.info("getUserConfig pageType="+pageType);
		UserInfo userInfo = FlexUserSessionContext.getUser();
		ListCriteriaDTO dto = null;
		Session session = null;
		
		if(userInfo==null){
			log.error("FlexUserSessionContext.getUser() is null!");
			return null;
		}else{
			try{
  				PglistSettings pgListSettings = new PglistSettings();
				pgListSettings.setUserId( Long.parseLong(userInfo.getUserId()) );
				pgListSettings.setListName(pageType);
				pgListSettings.setIsActive(true);
				pgListSettings.setIsSystemDefault(false);
				session = this.sessionFactory.getCurrentSession();
				Object setting = session.createCriteria(PglistSettings.class)
					    .add( Example.create(pgListSettings) )
						.uniqueResult();
				
				if(setting == null){
					log.info("PglistSettings is null!");
					dto = null;
				}else{			
					log.info("PglistSettings is not null!");
					dto = ((PglistSettings)setting).getListCriteriaDTO();										
				}
			} catch ( NonUniqueResultException e ) {
				dto = null;
			} catch ( HibernateException e ) {		
				log.error( "HibernateException:", e );
			} catch ( org.springframework.dao.DataAccessException e ) {
				log.error( "DataAccessException:", e );
				e.printStackTrace();
			} catch ( Exception e ) {
				log.error( "Exception:", e );
				e.printStackTrace();
			}
		}
		
		log.info("Doing getUserConfig! user="+userInfo.getUserName()+" id="+userInfo.getUserId());		
		//return (dto==null) ? getDefaultListCriteriaDTO(pageType) : dto;
		return dto;

	}
	
	/**
	* Get the default configuration of the user first, if it doesn't exist, return the global default.
	*/
	public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException{
		UserInfo userInfo = FlexUserSessionContext.getUser();
		
		if(userInfo!=null)
			log.info("Doing getUserConfig! user="+userInfo.getUserName()+" id="+userInfo.getUserId());
		
		ListCriteriaDTO dto = new ListCriteriaDTO(); 
		try{
			Session session = this.sessionFactory.getCurrentSession();
			
			//Query query = session.createQuery( "select c from tb c where c.userId = :userId" );
			//query.setString( "name", userInfo.getUserId() );
			
			//Object obj = query.uniqueResult();
			//dto = (ListCriteriaDTO)obj ;
			if(thisListCriteria != null)	dto = thisListCriteria;
		} catch ( NonUniqueResultException e ) {	
			//log.error( "NonUniqueResultException:", e );	
		} catch ( HibernateException e ) {		
			log.error( "HibernateException:", e );
		} catch ( org.springframework.dao.DataAccessException e ) {
			log.error( "DataAccessException:", e );
			e.printStackTrace();
		} catch ( Exception e ) {
			log.error( "Exception:", e );
			e.printStackTrace();
		}
		return dto;
	}
	
	//for test 
	private ListCriteriaDTO thisListCriteria = null;
	
	/** Save the configuration by user. */
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{		
		
		UserInfo userInfo = null;		        
        Session session = null;		
		PglistSettings settings = null; 
		
		try{			
			userInfo = FlexUserSessionContext.getUser();
			
			log.info("userInfo="+userInfo);
			
			/*Dump saved data
			List dtos=itemListCriteria.getColumns();
			log.info("columnDTO.size()="+dtos.size());
			for(int i=0;i<dtos.size();i++) {
				ColumnDTO dto=(ColumnDTO)dtos.get(i);
				log.info(i+" name="+dto.getFieldName()+" "+dto.getFieldLabel());
			}
			*/
			
			if(userInfo!=null){
				log.info("PaginatedHomeBase saveUserConfig userInfo id="+userInfo.getUserId()+" name="+userInfo.getUserName());
				log.info("ListCriteriaDTO="+itemListCriteria);
				log.info("ListCriteriaDTO ListCriteria.getFitType()-->="+itemListCriteria.getFitType());
				
				boolean isSystemDefault = false;
				boolean isActive = true;
				long userId = parseUserId(userInfo);				
				
				boolean dbIsSystemDefault = false , dbIsActive = true;
				
				//settings = new PglistSettings(pageType, userInfo.getUserId(), isSystemDefault, isActive, itemListCriteria);
				settings = new PglistSettings(pageType, String.valueOf(userId), isSystemDefault, isActive, itemListCriteria);
				
				PglistSettings settingsCriteria = new PglistSettings();
				settingsCriteria.setListName(pageType);
				settingsCriteria.setUserId(userId);
				settingsCriteria.setIsActive(dbIsActive);
				settingsCriteria.setIsSystemDefault(dbIsSystemDefault);
				
				session = this.sessionFactory.getCurrentSession();			
				Object srvSettings = session.createCriteria(PglistSettings.class).add(Example.create(settingsCriteria)).uniqueResult();
											
				if(srvSettings == null){					
					session.save(settings);
					log.info("session.save(pgSet)");
				}else{
					PglistSettings pgSet = (PglistSettings) srvSettings;
					pgSet.setListCriteriaDTO(itemListCriteria);
					pgSet.parseJasonString(itemListCriteria);					
					session.update(pgSet);
					log.info("session.update(pgSet)");
				}								
			}else{
				log.error("PaginatedHomeBase saveUserConfig userInfo is null!");
			}																
		} catch ( NonUniqueResultException e ) {	
			log.error( "NonUniqueResultException:", e );
		} catch ( HibernateException e ) {
			log.error( "HibernateException:", e );
		} catch ( org.springframework.dao.DataAccessException e ) {			
			log.error( "DataAccessException:", e );
			e.printStackTrace();
		} catch ( Exception e ) {			
			log.error( "Exception:", e );
			e.printStackTrace();
		}finally{			
		}
		return 1;
	}
	
	/** Delete the configuration by user. */
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{				
		UserInfo userInfo = null;		        
        Session session = null;		
		PglistSettings settings = null; 
		try{			
			userInfo = FlexUserSessionContext.getUser();
			if(userInfo!=null){								
				boolean isSystemDefault = false;
				boolean isActive = true;
				long userId = parseUserId(userInfo);
				boolean dbIsSystemDefault = false , dbIsActive = true;
				
				settings = new PglistSettings(pageType, String.valueOf(userId), isSystemDefault, isActive, itemListCriteria);
				
				PglistSettings settingsCriteria = new PglistSettings();
				settingsCriteria.setListName(pageType);
				settingsCriteria.setUserId(userId);
				settingsCriteria.setIsActive(dbIsActive);
				settingsCriteria.setIsSystemDefault(dbIsSystemDefault);
				
				session = this.sessionFactory.getCurrentSession();			
				Object srvSettings = session.createCriteria(PglistSettings.class).add(Example.create(settingsCriteria)).uniqueResult();		
				session.delete(srvSettings);									
			}else{
				log.error("PaginatedHomeBase saveUserConfig userInfo is null!");
			}																
		} catch ( NonUniqueResultException e ) {	
			log.error( "NonUniqueResultException:", e );
		} catch ( HibernateException e ) {
			log.error( "HibernateException:", e );
		} catch ( org.springframework.dao.DataAccessException e ) {			
			log.error( "DataAccessException:", e );
			e.printStackTrace();
		} catch ( Exception e ) {			
			log.error( "Exception:", e );
			e.printStackTrace();
		}finally{			
		}
		return 1;
	}
	
	/** Handle the test and unknown situation.  */
	private long parseUserId( UserInfo userInfo ){
		long userId = 0;
		if(userInfo != null && userInfo.getUserId()!=null && userInfo.getUserId().length()>0){
			if( "mockuser".equals(userInfo.getUserId().toLowerCase()) ){
				userId = USER_MOCK;
			}else{
				userId = Long.parseLong(userInfo.getUserId());
			}
		}else{
			userId = USER_NONE;
		}
		return userId;
	}
	
	/** Reset the configuration by user. And use the default settings as the page list query condition. */
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException{
		log.info("resetUserConfig begin");
		UserInfo userInfo = null;		        
        //Session session = null;
        ListCriteriaDTO newCriteriaDto = null;
		try{
			userInfo = FlexUserSessionContext.getUser();
			if(userInfo!=null){
				//delete current config
				deleteUserConfig(itemListCriteria,pageType);
				log.info("resetUserConfig deleteUserConfig-done!");
				//get default config
				newCriteriaDto = getDefaultListCriteriaDTO(pageType,fitRows);
				log.info("resetUserConfig getDefaultListCriteriaDTO"); 				
				
				saveUserConfig(newCriteriaDto,pageType);
				log.info("resetUserConfig saveUserConfig-done!");
			}	
			log.info("resetUserConfig return newCriteriaDto-->"+ ( (newCriteriaDto==null) ? "" : newCriteriaDto.toString() ) );
		}catch(Exception e){
			log.error( "Exception:", e );
			e.printStackTrace();
			//ANY EXCEPTION, STILL GIVE A DEFAULT ListCriteriaDTO
			newCriteriaDto = getDefaultListCriteriaDTO(pageType,fitRows);
		}				
		return newCriteriaDto;
	}
	
	private ListCriteriaDTO getDefaultListCriteriaDTO(String pageType,int fitRows){
		
		ListCriteriaDTO listCriteriaDTO = new ListCriteriaDTO();
		
		listCriteriaDTO.setPageNumber(DEFAULT_PAGE_NUMBER);
		listCriteriaDTO.setMaxLinesPerPage(fitRows);
		List<ColumnDTO> dtoList=getColumnConfig(true,pageType);
		log.info("dtoList size=" + dtoList!=null?0:dtoList.size() );
		listCriteriaDTO.setColumnCriteria(new ArrayList<ColumnCriteriaDTO>()); //empty ColumnCriteriaDTO dto
		listCriteriaDTO.setColumns(dtoList);		
		//log.info("dtoList 1-->"+dtoList.get(1));
		return listCriteriaDTO;
	}
	
	public void setColumnGroupMap(Map map) {
		columnGroupMap=map;
		log.info("setColumnGroupMap columnGroupMap="+columnGroupMap);
	}
	
	public void setColumnAttributes(String columnAttrs) {
		columnAttributes=columnAttrs.trim();
		log.info("setColumnAttributes...");
		columnAttributeMap=new HashMap();
		columnDTOMap=new LinkedHashMap();
		fieldNameUidMap=new HashMap();
		uidFieldNameMap=new HashMap();
		fieldLabelMap=new HashMap();
		
		String[] columnAttributesArray=columnAttributes.split("\n");
		
		log.info("columnAttributesArray.length="+columnAttributesArray.length);
		
		try {
			for(int i=0;i<columnAttributesArray.length;i++) {
			
				String attrs=columnAttributesArray[i];
								
				String[] attrArr=attrs.split(",");
		
				String fieldId=attrArr[0].trim();
				String fieldName=attrArr[1].trim();
				String uiComponentId=attrArr[2].trim();
				String fieldLabel=attrArr[3].trim();
				boolean defaultColumn=Boolean.parseBoolean((String)attrArr[4]);
				boolean fixedColumn=Boolean.parseBoolean((String)attrArr[5]);
				int width=Integer.parseInt((String)attrArr[6]);
				boolean sortable=Boolean.parseBoolean((String)attrArr[7]);
				boolean filterable=Boolean.parseBoolean((String)attrArr[8]);
				int filterType=Integer.parseInt((String)attrArr[9]);
				String format=attrArr[10].trim();
				boolean visible=Boolean.parseBoolean((String)attrArr[11]);
				
				Map attrMap=new HashMap();
				attrMap.put("fieldId",fieldId);
				attrMap.put("fieldName",fieldName);
				attrMap.put("fieldLabel",fieldLabel);
				attrMap.put("uiComponentId",uiComponentId);
				attrMap.put("defaultColumn",defaultColumn);
				attrMap.put("fixedColumn",fixedColumn);
				attrMap.put("width",width);
				attrMap.put("sortable",sortable);
				attrMap.put("filterable",filterable);
				attrMap.put("filterType",filterType);
				attrMap.put("format",format);
				attrMap.put("visible",visible);
				
				columnAttributeMap.put(fieldId,attrMap);
				
				ColumnDTO dto=new ColumnDTO();
				dto.setFieldName(fieldName);
				dto.setFieldLabel(fieldLabel);
				dto.setUiComponentId(uiComponentId);
				dto.setDefaultColumn(defaultColumn);
				dto.setSortable(sortable);
				dto.setFilterable(filterable);
				dto.setVisible(visible);
				dto.setFilterType(filterType);
				dto.setWidth(width);
				dto.setFormat(format);
				
				//columnDTOMap.put(fieldName,dto);
				columnDTOMap.put(fieldLabel,dto);
				
				fieldNameUidMap.put(fieldName,uiComponentId);
				uidFieldNameMap.put(uiComponentId,fieldName);
				
				fieldLabelMap.put(fieldLabel,fieldName);
				
			}
		} catch(Exception e) {
			log.error("",e);
		}
	}

	
	public Map getColumnGroup(String pageType) throws DataAccessException{
	
		log.info("homeBase getColumnGroup");
	
	    Map groupMap = new LinkedHashMap();
	    for (Iterator it = columnGroupMap.entrySet().iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        
	        String groupStr=(String)entry.getKey();
	        List groupList=(List)entry.getValue();
	        
			String[] nameArr=groupStr.split(":");
			String groupKey=nameArr[0]+":"+nameArr[1];
			String groupName=nameArr[1];
	    
	    	List attrList=new ArrayList();
	        
	        for(int i=0;i<groupList.size();i++) {
				String fieldId=(String)groupList.get(i);

				Map attrMap=(Map)columnAttributeMap.get(fieldId);

	        	if(attrMap!=null) {
	        	
	        		String fieldName=(String)attrMap.get("fieldName");
	        		String fieldLabel=(String)attrMap.get("fieldLabel");
	        		String uiComponentId=(String)attrMap.get("uiComponentId");
	        		boolean fixedColumn=(Boolean)attrMap.get("fixedColumn");
	        		
	        		if(!"".equals(fieldName) && !"".equals(uiComponentId)) {
						ColumnGroupDTO dto=new ColumnGroupDTO();
						dto.setGroupName(groupName);
						//dto.setFieldName(fieldName);
						dto.setFieldName(fieldLabel);
						dto.setFieldLabel(fieldLabel);
						dto.setFixedColumn(fixedColumn);
						dto.setCustomField(false);
						attrList.add(dto);
					}
	        	
	        	}
	        	
	        	groupMap.put(groupKey,attrList);
	        	
	        }

		}
			        
		if("itemList".equalsIgnoreCase(pageType)) {
			//for custom fields
			Session session = this.sessionFactory.getCurrentSession();
			
			for (Iterator it = columnGroupMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry)it.next();	        
				String groupStr=(String)entry.getKey();
				
				String[] nameArr=groupStr.split(":");
				String groupKey=nameArr[0]+":"+nameArr[1];
				String groupName=nameArr[1];
				String classCode=nameArr[2];
				
				List attrList=(List)groupMap.get(groupKey);
							
				String classCriteria="";
				
				if("ALL".equalsIgnoreCase(classCode)) {
					classCriteria="a.lksData=null";
				} else {
					classCriteria="a.lksData.lkpValueCode="+classCode;
				}
				
				String hql="from LkuData a "+
									"where lkuTypeName='CUSTOM_FIELD' "+
									"and "+classCriteria+" order by a.lkuValue";
				
				Query query = session.createQuery(hql);
	
				List<LkuData> dataList = query.list();
				for ( LkuData lkuData : dataList ) {
				
					Long lkuId=lkuData.getLkuId();
					
					LksData lksData=lkuData.getLksData();
					Long lkpValueCode=new Long(0);
					
					if(lksData!=null) {
						lkpValueCode=lksData.getLkpValueCode();
					}
					
					String fieldName="custom_"+lkuId;
					String fieldLabel=lkuData.getLkuValue();
					
					ColumnGroupDTO dto=new ColumnGroupDTO();
					dto.setGroupName(groupName);
					//dto.setFieldName(fieldName);
					dto.setFieldName(fieldLabel);
					dto.setFieldLabel(fieldLabel);
					dto.setFixedColumn(false);
					dto.setCustomField(true);
					dto.setCustomIds(String.valueOf(lkuId));
					attrList.add(dto);
				}
				
				groupMap.put(groupKey,attrList);
				
			}//end for (Iterator it = columnGroupMap.entrySet().iterator(); it.hasNext();)
			
			groupMap = postProcessColumnGroup(groupMap);
			//re-organize the group map
		}
		
		log.info("groupMap.size()="+groupMap.size());
	
		return groupMap;
	}
	
	/** 
	 * 1. Get and check the repeat column group 
	 * 2. Remove the repeat
	 * 3. Create the column group DTO list
	 * 4. Add back to the Common column group list
	 * @author kc.chen 
	 */
	private Map postProcessColumnGroup(Map groupMap){
		Map newGroupMap = new LinkedHashMap();
		Map rtnGroupMap = new LinkedHashMap();
		
		List newAttrList=new ArrayList();
				
		Set<String> repeatColSet = generateColumnGroupFilterSet();				
							
		newGroupMap = removeRepeatDataFromColumnGroup(repeatColSet, groupMap);
			
		List<ColumnGroupDTO> colGrpDtoList = createRepeatColumnDtoListFromSet(repeatColSet);
			
		rtnGroupMap = addRepeatColumnToCommonGroupMap(colGrpDtoList, newGroupMap);
				
		//Now, using the repeat column list to remove from the column group, add 1 to common
		return rtnGroupMap;
	}
	
	private Map addRepeatColumnToCommonGroupMap(List<ColumnGroupDTO> colGrpDtoList,Map oldGroupMap){
		Map rtMap = new LinkedHashMap();
		
		Map newGroupMap = new LinkedHashMap();
		//List newAttrList=new ArrayList();
		
		String lkuId="", lkuValue="", lksId="", lkpValue="";
		String strSet;
		String[] strSetArr;
						
		if(colGrpDtoList!=null && colGrpDtoList.size()>0){
			
			Iterator it = oldGroupMap.entrySet().iterator();
			while ( it.hasNext() ) {
				Map.Entry entry = (Map.Entry)it.next();	        
				String groupStr=(String)entry.getKey();
				
				//A:Common (all classes)
				String[] nameArr=groupStr.split(":");
				//String groupKey=nameArr[0]+":"+nameArr[1];
				String groupName=nameArr[1];
				//String classCode=nameArr[2];
										
				if(groupName!=null && groupName.equals("Common (all classes)")){
					List<ColumnGroupDTO> attrList=(List)oldGroupMap.get(groupStr);						
					//newAttrList=new ArrayList();
					//Loop each group's list
					for ( ColumnGroupDTO colGroupDto : colGrpDtoList ) {
						attrList.add(colGroupDto);
					}//end for
					oldGroupMap.put(groupStr, attrList);
				}
				
			}//end while
		}//end if
		
		return oldGroupMap;
	}
	
	private List<ColumnGroupDTO> createRepeatColumnDtoListFromSet(Set<String> repeatColSet){
		
		Map<String,ColumnGroupDTO> rsMap = new HashMap<String,ColumnGroupDTO>();
		List<ColumnGroupDTO> colGrpDtoList = new ArrayList<ColumnGroupDTO>();
		ColumnGroupDTO dto,tmpDto;
		
		if(repeatColSet!=null){
			Iterator<String> itorSet = repeatColSet.iterator();
			String strSet = "";
			String[] strSetArr;
			String lkuId, lkuValue, lksId, lkpValue;
																							
			while( itorSet.hasNext() ){
				strSet = itorSet.next();
				
				if(strSet!=null && strSet.length()>0){
					strSetArr = strSet.split(COMMA);
					if( strSetArr.length == 4 ){
						lkuId = strSetArr[0];
						lkuValue = strSetArr[1];
						lksId = strSetArr[2];
						lkpValue = strSetArr[3];
						
						dto=new ColumnGroupDTO();
						dto.setGroupName(lkpValue); 					
						dto.setFieldName(lkuValue);
						dto.setFieldLabel(lkuValue);	
						dto.setCustomIds(lkuId);
						dto.setFixedColumn(false);
						dto.setCustomField(true);		
						
						if(rsMap.containsKey(lkuValue)){//Using the lkuValue as the key, if they have the same name, the results will filter the same dto.
							tmpDto = rsMap.get(lkuValue);
							tmpDto.setCustomIds(tmpDto.getCustomIds()+","+dto.getCustomIds());
							
							rsMap.put(lkuValue, tmpDto);
						}else{
							rsMap.put(lkuValue, dto);
						}
					}
				}
			}//end while
		}	
		
		//Extract the DTO from map, then add to the list
		Map.Entry entry;
		String custValueKey;
		ColumnGroupDTO grpDto;
		
		Iterator itorMap = rsMap.entrySet().iterator();
		while(itorMap.hasNext()){
			entry = (Map.Entry)itorMap.next();
			custValueKey = (String)entry.getKey();
			grpDto = (ColumnGroupDTO)entry.getValue();
			
			colGrpDtoList.add(grpDto);
		}
				
		return colGrpDtoList;
	}
	
	/** Get the repeat column group string list from DB */
	private Set<String> generateColumnGroupFilterSet(){
		
		Session session = this.sessionFactory.getCurrentSession();
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select u.lku_id, u.lku_value, u.lks_id, s.lkp_value ");
		sb.append(" from dct_lku_data as u , dct_lks_data as s ");
		sb.append(" where u.lku_type_name='CUSTOM_FIELD' ");
		sb.append(" and u.lks_id = s.lks_id ");
		sb.append(" order by u.lku_value ");
		
		SQLQuery query = session.createSQLQuery(sb.toString());			
		
		List<Object[]> resultList = query.list();
		
		List<String> repeatColList = new ArrayList();
		
		Set<String> colSet = new HashSet<String>();
		
		if(resultList!=null && resultList.size()>0){
			String lkuId, lkuValue, lksId, lkpValue;
			String oldLkuId="", oldLksId="", oldLkpValue="";
			String oldLkuValue="";
			StringBuilder sbCol;
			//1st round 
			for ( Object[] recordArr : resultList ) {
				lkuId = recordArr[0].toString();				
				lkuValue = recordArr[1].toString();
				lksId = recordArr[2].toString();
				lkpValue = recordArr[3].toString();
				
				if(oldLkuValue.equals(lkuValue)){
					sbCol = new StringBuilder();
					sbCol.append(lkuId).append(COMMA).append(lkuValue).append(COMMA).append(lksId).append(COMMA).append(lkpValue);
					colSet.add(sbCol.toString());
					sbCol = new StringBuilder();
					sbCol.append(oldLkuId).append(COMMA).append(oldLkuValue).append(COMMA).append(oldLksId).append(COMMA).append(oldLkpValue);
					colSet.add(sbCol.toString());
				}else{
					oldLkuId = lkuId;				
					oldLkuValue = lkuValue;
					oldLksId = lksId;
					oldLkpValue = lkpValue;
				}								
			}
						
		}
		return colSet;
	}
	
	private Map removeRepeatDataFromColumnGroup(Set<String> repeatColSet, Map groupMap){
		
		Map newGroupMap = new LinkedHashMap();
		List newAttrList=new ArrayList();
		
		String lkuId="", lkuValue="", lksId="", lkpValue="";
		String strSet;
		String[] strSetArr;
																
		//Remove from the column group map
		Iterator it = columnGroupMap.entrySet().iterator();
		while ( it.hasNext() ) {
			Map.Entry entry = (Map.Entry)it.next();	        
			String groupStr=(String)entry.getKey();
			
			String[] nameArr=groupStr.split(":");
			String groupKey=nameArr[0]+":"+nameArr[1];
			String groupName=nameArr[1];
			String classCode=nameArr[2];
			
			List<ColumnGroupDTO> attrList=(List)groupMap.get(groupKey);		
			newAttrList=new ArrayList();
			
			//Loop each group's list
			for ( ColumnGroupDTO colGroupDto : attrList ) {
				
				boolean blnEqual = false;
				
				if(repeatColSet!=null){
					//Loop each repeat column dto
					Iterator<String> itorSet = repeatColSet.iterator();
					while( itorSet.hasNext() ){
						strSet = itorSet.next();
						
						if(strSet!=null && strSet.length()>0){
							strSetArr = strSet.split(COMMA);
							if( strSetArr.length == 4 ){
								lkuId = strSetArr[0];
								lkuValue = strSetArr[1];
								lksId = strSetArr[2];
								lkpValue = strSetArr[3];
								
								if(lkuId.equals(colGroupDto.getCustomIds())){
									blnEqual = true;
									break;
								}
							}
						}					
					}//end while
				}
					
				if(blnEqual){//remove
					log.info("Remove column group-->"+colGroupDto.toString());
				}else{
					newAttrList.add(colGroupDto);
				}
			}//end for
			newGroupMap.put(groupKey, newAttrList);
		}//end outer while

		return newGroupMap;
	}

	
	private Map addRepeatColumnToGroupMap(Set<String> repeatColSet, Map groupMap){
		Map newGroupMap = new LinkedHashMap();
		List newAttrList=new ArrayList();
		
		String lkuId="", lkuValue="", lksId="", lkpValue="";
		String strSet;
		String[] strSetArr;
																
		//Remove from the column group map
		Iterator it = columnGroupMap.entrySet().iterator();
		while ( it.hasNext() ) {
			Map.Entry entry = (Map.Entry)it.next();	        
			String groupStr=(String)entry.getKey();
			
			String[] nameArr=groupStr.split(":");
			String groupKey=nameArr[0]+":"+nameArr[1];
			String groupName=nameArr[1];
			String classCode=nameArr[2];
			
			List<ColumnGroupDTO> attrList=(List)groupMap.get(groupKey);		
			newAttrList=new ArrayList();
			
			if(groupKey.equals("Common")){
				
				
				//Loop each group's list
				for ( ColumnGroupDTO colGroupDto : attrList ) {
					
					boolean blnEqual = false;
					
					//Loop each repeat column dto
					Iterator<String> itorSet = repeatColSet.iterator();
					while( itorSet.hasNext() ){
						strSet = itorSet.next();
						
						if(strSet!=null && strSet.length()>0){
							strSetArr = strSet.split(COMMA);
							if( strSetArr.length == 4 ){
								lkuId = strSetArr[0];
								lkuValue = strSetArr[1];
								lksId = strSetArr[2];
								lkpValue = strSetArr[3];
								
								if(lkuId.equals(colGroupDto.getCustomIds())){
									blnEqual = true;
									break;
								}
							}
						}					
					}//end while
					
					if(blnEqual){//remove
						log.info("Remove column group-->"+colGroupDto.toString());
					}else{
						newAttrList.add(colGroupDto);
					}
				}//end for
			}
			
			
			
			
			newGroupMap.put(groupKey, newAttrList);
		}//end outer while

		return newGroupMap;

	}
	
	//Fill up the rest of columnDTO attributes
	public List<ColumnDTO> updateColumnDTO(List<ColumnDTO> columnDTOList) {
	
		log.info("updateColumnDTO()...");
	
		if(columnDTOList==null)
			return null;

		for(int i=0;i< columnDTOList.size() ;i ++) {
			ColumnDTO dto=(ColumnDTO)columnDTOList.get(i);
			
			if(dto!=null) {
				//String fieldName=dto.getFieldName();
				//ColumnDTO newDTO=(ColumnDTO)columnDTOMap.get(fieldName);
				String fieldLabel=dto.getFieldLabel();
				ColumnDTO newDTO=(ColumnDTO)columnDTOMap.get(fieldLabel);
			
				if(newDTO != null) {
				
					if(dto.getFieldName()==null || "".equals(dto.getFieldName())) {
						dto.setFieldName(newDTO.getFieldName());
					}
					if(dto.getFieldLabel()==null || "".equals(dto.getFieldLabel())) {
						dto.setFieldLabel(newDTO.getFieldLabel());
					}
					if(dto.getUiComponentId()==null || "".equals(dto.getUiComponentId())) {
						dto.setUiComponentId(newDTO.getUiComponentId());
					}
					
					dto.setDefaultColumn(newDTO.isDefaultColumn());
					dto.setSortable(newDTO.isSortable());
					dto.setFilterable(newDTO.isFilterable());
					dto.setFilterType(newDTO.getFilterType());
					
					if(dto.getWidth()==-1) {
						dto.setWidth(newDTO.getWidth());
					}
					
					dto.setFormat(newDTO.getFormat());

					columnDTOList.set(i,dto);
				}
			}
		}
	
		return columnDTOList;
	}
	
	//Sync up filterType of columnDTO - for add columns from client side
	public ListCriteriaDTO updateDTOAttribute(ListCriteriaDTO listCriteriaDTO) {
		log.info("updateDTOAttribute()...");
		
		if(listCriteriaDTO==null) {
			return null;
		}
				
		List<ColumnDTO> columnDTOList=listCriteriaDTO.getColumns();
	
		if(columnDTOList!=null) {

			for(int i=0;i< columnDTOList.size() ;i ++) {
				ColumnDTO dto=(ColumnDTO)columnDTOList.get(i);
				
				if(dto!=null) {
					
					String fieldLabel=dto.getFieldLabel();
					
					ColumnDTO newDTO=(ColumnDTO)columnDTOMap.get(fieldLabel);
				
					if(newDTO != null) {
					
						//Replace fieldLabel with fieldName
						dto.setFieldName(newDTO.getFieldName());
					
						dto.setFilterable(newDTO.isFilterable());
						dto.setFilterType(newDTO.getFilterType());
						columnDTOList.set(i,dto);
						//log.info("getFieldName="+dto.getFieldName()+" filterType="+dto.getFilterType()+" filterable="+dto.isFilterable());
					}
				}
			}
			
			listCriteriaDTO.setColumns(columnDTOList);
			
		}
		
		List<ColumnCriteriaDTO> criteriaDTOList=listCriteriaDTO.getColumnCriteria();
	
		return listCriteriaDTO;
	}

	/** 
	 * Update the column list of the ListCriteriaDTO by using the field label retrieving from the column DTO map. 
	 */
	public ListCriteriaDTO updateDTOAttributeForOutput(ListCriteriaDTO listCriteriaDTO) {
		log.info("updateDTOAttributeForOutput()...");
		
		if(listCriteriaDTO==null) {
			return null;
		}
		
		List<ColumnDTO> columnDTOList=listCriteriaDTO.getColumns();
	
		if(columnDTOList!=null) {

			for(int i=0;i< columnDTOList.size() ;i ++) {
				ColumnDTO dto=(ColumnDTO)columnDTOList.get(i);
				
				if(dto!=null) {
					
					String fieldLabel=dto.getFieldLabel();
					ColumnDTO newDTO=(ColumnDTO)columnDTOMap.get(fieldLabel);
				
					if(newDTO != null) {
						//Replace fieldName with fieldLabel
						dto.setFieldName(newDTO.getFieldLabel());
					
					}
				}
			}
			
			listCriteriaDTO.setColumns(columnDTOList);
			
		}
	
		return listCriteriaDTO;
	}
	
	protected ColumnDTO[] getColumnConfigArray(boolean getDefault,String pageType) {
		List<ColumnDTO> dtoList = getColumnConfig(getDefault, pageType);
		//ColumnDTO[] columnDtoArr = (ColumnDTO[])dtoList.toArray();
		ColumnDTO[] columnDtoArr = null;
		
		if(dtoList!=null && dtoList.size()>0){
			columnDtoArr = new ColumnDTO[dtoList.size()];
			
			for(int idx=0; idx < dtoList.size(); idx++){
				columnDtoArr[idx] = dtoList.get(idx);
			}
			
		}
				
		return columnDtoArr;
	}
	
	protected List<ColumnDTO> getColumnConfig(boolean getDefault,String pageType) {
		
		//Pick default columns from columnDTOMap
		List<ColumnDTO> dtoList=new ArrayList();
		
	    for (Iterator it = columnDTOMap.entrySet().iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        
	        String fieldLabel=(String)entry.getKey();
	        ColumnDTO newDTO=(ColumnDTO)entry.getValue();
	        
	        ColumnDTO dto=(ColumnDTO)newDTO.clone();
	        
	        String fieldName=newDTO.getFieldName();
	        	        
	        if(fieldName==null || "".equals(fieldName)) {
	        	continue;
	        }
	        
	        if(getDefault) {
	        	if(dto.isDefaultColumn()) {
	        		 dtoList.add(dto);
	        	}
	        } else {
	        	dtoList.add(dto);
	        }   
	    }

		//if("itemList".equals(pageType)) {
		if(PAGE_TYPE_ITEMLIST.equals(pageType)){
		
			List<ColumnDTO> newDTOList=getCustomFields();
			
			//Get all custom fields 
			dtoList.addAll(newDTOList);
			
		}

		return dtoList;
	}
	
	public List<ColumnDTO> getCustomFields() {
		List<ColumnDTO> dtoList=new ArrayList<ColumnDTO>();

		//Get all custom fields
		Session session = this.sessionFactory.getCurrentSession();
		
		String hql="from LkuData a "+
							"where lkuTypeName='CUSTOM_FIELD' order by a.lkuValue";
							
		Query query = session.createQuery(hql);

		List<LkuData> dataList = query.list();
		for ( LkuData lkuData : dataList ) {
		
			Long lkuId=lkuData.getLkuId();
			
			LksData lksData=lkuData.getLksData();
			Long lkpValueCode=new Long(0);
			
			if(lksData!=null) {
				lkpValueCode=lksData.getLkpValueCode();
			}
			
			String fieldName="custom_"+lkuId;
			String fieldLabel=lkuData.getLkuValue();
			
			ColumnDTO dto=new ColumnDTO();
			dto.setFieldName(fieldName);
			dto.setFieldLabel(fieldLabel);
			dto.setUiComponentId("tiCustomField");
			dto.setDefaultColumn(true);
			dto.setSortable(true);
			dto.setFilterable(true);
			dto.setVisible(false);
			dto.setFilterType(ColumnDTO.TEXT);
			dto.setWidth(100);
			dto.setFormat("");
			dto.setCustomIds(lkuId.toString());
			
			dtoList.add(dto);
		}
	
		//CR50554
		dtoList = processLkuValue(dtoList);
		
		return dtoList;
	}

	/**
	 * Merge the same lku_data and update the customIds(Format will be like "id1,id2").
	 * 
	 * @param originalLksDto Original Lks column DTO list from DB.
	 * @return processed Lks column DTO list
	 * @author kc.chen
	 */
	private List<ColumnDTO> processLkuValue(List<ColumnDTO> originalLksDtoList){
		List<ColumnDTO> comparedLksColDtoList;
		List<ColumnDTO> processedLksDtoList = new ArrayList<ColumnDTO>();
		
		if(originalLksDtoList!=null && originalLksDtoList.size()>0){
			//comparedLksColDtoList = (List<ColumnDTO>) ((ArrayList<ColumnDTO>)originalLksDtoList ).clone();			
			
			for(ColumnDTO originalColDto:originalLksDtoList){				
				//processedLksDtoList = checkRepeatLku(originalColDto ,processedLksDtoList);	
				
				boolean repeatLku = false;
				for(ColumnDTO currDto:processedLksDtoList){
					//fieldLabel:Lku_value
					if(currDto.getFieldLabel().equals(originalColDto.getFieldLabel())){
						//update the CustomIds for the sql command
						processedLksDtoList.remove(currDto);
						//ID will never use the ',' as part of the value. That's OK by using the ',' as the split sign.
						currDto.setCustomIds(currDto.getCustomIds()+","+originalColDto.getCustomIds());
						
						processedLksDtoList.add(currDto);
						
						repeatLku = true;
						break;
					}
				}
				
				if(repeatLku == false){
					processedLksDtoList.add(originalColDto);
				}
			}
		}
										
		return processedLksDtoList;
	}				
	
	
	protected List<Map> getValueList(ListResultDTO listResultDTO,String pageType) {
		List resultList=new ArrayList();
		
		if(listResultDTO==null)
			return resultList;
	
		ListCriteriaDTO listCriteriaDTO=listResultDTO.getListCriteriaDTO();
		List<Object[]> valueList=listResultDTO.getValues();
		
		//log.info("valueList="+valueList);
		
		List<ColumnDTO> columns=listCriteriaDTO.getColumns();
		
		//Get default column dto
		ColumnDTO[] defColumnDTOArr = getColumnConfigArray(true,PaginatedHomeBase.PAGE_TYPE_ITEMLIST);
		
				
		if(valueList!=null && columns!=null) {
			for(Object[] valueArray : valueList) {
						
				if(valueArray.length != defColumnDTOArr.length)
					break;
				
				Map map=new LinkedHashMap();
				
				for(int index=0;index<valueArray.length;index++)  {
								
					//ColumnDTO columnDTO=(ColumnDTO)columns.get(index);
					ColumnDTO defColumnDTO=defColumnDTOArr[index];
					String defFieldLabel=defColumnDTO.getFieldLabel();
					
					for(ColumnDTO columnDTO : columns) {
						String fieldLabel=columnDTO.getFieldLabel();
						
						if(defFieldLabel.equals(fieldLabel)) {
							String value=valueArray[index]==null ? "" : valueArray[index].toString();
							map.put(fieldLabel,value);	
						}
					}
		
				}
			
				resultList.add(map);
			
			}
		
		}
		
		return resultList;
	}
	
	public List<ColumnDTO> syncCustomFields(List<ColumnDTO> columnDTOs) {
	
		log.info("syncCustomFields...columnDTOs.size()="+columnDTOs.size());
	
		Session session = this.sessionFactory.getCurrentSession();
		
		boolean hasChanged=false;
		
		List<ColumnDTO> newDTOs=getCustomFields();
	
		//Check for new column
		for(ColumnDTO newDTO : newDTOs) {
			String newFieldName=newDTO.getFieldName();
			boolean isExist=false;
			for(ColumnDTO currrentDTO:columnDTOs) {
				String currentFieldName=currrentDTO.getFieldName();
				
				if(currentFieldName.indexOf("custom_")==-1) {
					continue;
				}
				
				if(currentFieldName.equals(newFieldName)) {
					isExist=true;
					
					//Sync fieldName
					//New -> current field label
					String newFieldLabel=newDTO.getFieldLabel();
					currrentDTO.setFieldLabel(newFieldLabel);
					break;
				}
			}
			//If is not existing. Add new column
			if(!isExist) {
				columnDTOs.add(newDTO);
				hasChanged=true;
			}
		}
		
		//Check for deleted column
		Iterator<ColumnDTO> iter = columnDTOs.iterator();
		while(iter.hasNext()){
			ColumnDTO currrentDTO=iter.next();			
			String currentFieldName=currrentDTO.getFieldName();
			
			if(currentFieldName.indexOf("custom_")==-1) {
				continue;
			}
			
			boolean isExist=false;
			for(ColumnDTO newDTO : newDTOs) {
				String newFieldName=newDTO.getFieldName();
				if(currentFieldName.equals(newFieldName)) {
					isExist=true;
					//log.info("isExist="+isExist);
					break;
				}
			}
			
			//If is not existing. Remove column
			if(!isExist) {
				iter.remove();
				hasChanged=true;
			}
			
		}
		
		log.info("hasChanged="+hasChanged);
		log.info("columnDTOs.size()="+columnDTOs.size());

		return columnDTOs;
	}
	
	protected boolean isNoSorting(ColumnCriteriaDTO[] dtos) {	
		boolean check=true;
	
		if(dtos==null) {
			check=true;
		} else {
			for(ColumnCriteriaDTO dto : dtos) {
				if(dto.isToSort()) {
					check=false;
					break;
				}
			}
		}
		
		log.info("isNoSorting="+check);
		
		return check;
	}
	
	protected ColumnCriteriaDTO[] getDefaultSortColumnCriteriaDTO(ColumnCriteriaDTO[] dtos,ColumnCriteriaDTO newDTO) {
		//Purpose - keep filter when restore to default
	
		if(dtos==null) {
			dtos=new ColumnCriteriaDTO[1];
			dtos[0]=newDTO;
		} else {
			List dtosList=Arrays.asList(dtos);
			List newList = new ArrayList(dtosList);
			newList.add(newDTO);
			dtos=(ColumnCriteriaDTO[])newList.toArray(new ColumnCriteriaDTO[0]);
		}
		
		return dtos;
	}
	
	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}
	
	public RemoteRef getRemoteReference() {
		return remoteReference;
	}

	public void setRemoteReference(RemoteRef remoteReference) {
		this.remoteReference = remoteReference;
	}
}
