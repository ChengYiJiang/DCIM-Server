package com.raritan.tdz.page.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemClone;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.FilterDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.user.dao.UserDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.UnitConverterIntf;
//temp by tonyshen

/**
 * 
 * @author Randy Chen
 */
public class PaginatedHomeItemListImpl extends PaginatedHomeBase implements PaginatedHome {
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired(required=true)
	private UserDAO userDao;

	ItemHome itemHome;
	ItemClone itemClone;
	
	public PaginatedHomeItemListImpl(SessionFactory sessionFactory ){
		super(sessionFactory);
		
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public void setItemClone(ItemClone itemClone) {
		this.itemClone = itemClone;
	}
	
	//temp by tonyshen
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void auditUpdateTest() {
		
		try {
			
			Session session = sessionFactory.getCurrentSession();	

			Query query = session.createQuery("from ItemServiceDetails where itemServiceDetailId=4972");
			List itemServiceDetailsList = query.list();
			ItemServiceDetails itemServiceDetails = (ItemServiceDetails) itemServiceDetailsList.get(0);
			session.delete(itemServiceDetails);
			
			query = session.createQuery("from Item where itemId=4972");
			List itemList = query.list();
			Item item = (Item) itemList.get(0);
			session.delete(item);

		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
	private ProjectionList parseCustomField(ProjectionList proList, String fieldName, boolean columnVisible, ColumnDTO custColumnDto){
		String aliasName=fieldName;
		String[] splitArr=fieldName.split("_");
		String lkuId=splitArr[1];
			
		String sql="";
		if(columnVisible) {
			String lkuIdSql = "";
			String[] lkuIdArr = custColumnDto.getCustomIds().split(",");
			if(custColumnDto!=null && custColumnDto.getCustomIds() !=null &&  lkuIdArr.length == 1){
				lkuIdSql = "="+lkuId;
				sql="(select attr_val from dct_custom_fields a where a.item_id=this_.item_id and a.attrib_name_lku_id"+lkuIdSql+" limit 1) as "+aliasName;
			//For the repeat custom code	
			}else{
				StringBuilder sbLkuIdSql=new StringBuilder("");
				sbLkuIdSql.append("in (");
				
				for(int idx=0; idx < lkuIdArr.length; idx++){
					sbLkuIdSql.append(lkuIdArr[idx]);
					
					if( idx != (lkuIdArr.length-1) ){//not the last record
						sbLkuIdSql.append(",");
					}
				}
				sbLkuIdSql.append(")");
								
				sql="(select attr_val from dct_custom_fields a where a.item_id=this_.item_id and a.attrib_name_lku_id " + sbLkuIdSql.toString() + " limit 1) as "+aliasName;
			}
				
		} else {
			sql="'' as "+aliasName;
		}
		
		Projection projection = Projections.sqlProjection(
			sql,
			new String[]{aliasName}, 
			new org.hibernate.type.Type[]{new StringType()}
		);
		proList.add(projection,aliasName);
		
		return proList;
	}
	
	private ProjectionList parseSlotLabel(ProjectionList proList, String fieldName, boolean columnVisible){
		
		//For Chassis slot label
		String sql="";
		
		if(columnVisible) {

			/*
			//CR50034 - ignore hard-code "bladechass10" alias
			sql="(select slot_label from dct_models_chassis_slots a,dct_models_chassis b,dct_items bladechassis "+
					"where "+
					"a.model_chassis_id=b.model_chassis_id and "+
					"a.slot_number=this_.slot_position and "+
					"b.model_id=bladechassis.model_id and this_2_.chassis_id=bladechassis.item_id and "+
					"this_.facing_lks_id=b.face_lks_id) "+
				") as "+fieldName;
			*/
			
			//CR52718 select slot_label or slot_position
			//If the slot label has been defined, display slot label otherwise, display slot position.
			sql=
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
				") as "+fieldName;

		} else {
			sql="'' as "+fieldName;
		}

		Projection projection = Projections.sqlProjection(
			sql,
			new String[]{fieldName}, 
			new org.hibernate.type.Type[]{new StringType()}
		);
		proList.add(projection,fieldName);
		
		return proList;
	}
	

	private ProjectionList parseRequestNumber(ProjectionList proList, String fieldName, boolean columnVisible){

		String itemRequestNumber="itemRequestNumber";

		String sql = "(SELECT r.requestno FROM tblrequest r " 
						+ "LEFT JOIN tblrequesthistory h ON r.id=h.requestid "
						+ "LEFT JOIN dct_lks_data l on h.stageid = l.lks_id "
						+ "WHERE h.current=true AND r.itemid=this_.item_id "
						+ "AND (r.requesttype != 'Disconnect' AND r.requesttype != 'Reconnect' AND r.requesttype != 'Connect') "
						+ "AND l.lks_id in ( 931, 932, 933, 934, 935, 936 )"
						+ "LIMIT 1) " +
						"AS " + itemRequestNumber;
								
		Projection projection = Projections.sqlProjection(
						sql,
						new String[]{itemRequestNumber}, 
						new org.hibernate.type.Type[]{new StringType()}
				);
		proList.add(projection,itemRequestNumber);
		
		return proList;
	}
	
	private ProjectionList parseRequestStage(ProjectionList proList, String fieldName, boolean columnVisible){
		
		String stageIdLkpValue="stageIdLkpValue";
		String sql= "(SELECT l.lkp_value FROM tblrequest r " 
						+ "LEFT JOIN tblrequesthistory h ON r.id=h.requestid "
						+ "LEFT JOIN dct_lks_data l on h.stageid = l.lks_id "
						+ "WHERE h.current=true AND r.itemid=this_.item_id "
						+ "AND (r.requesttype != 'Disconnect' AND r.requesttype != 'Reconnect' AND r.requesttype != 'Connect') "
						+ "AND l.lks_id in ( 931, 932, 933, 934, 935, 936 )"
						+ "LIMIT 1) " +
						"AS "+ stageIdLkpValue;
									
		Projection projection = Projections.sqlProjection(
						sql,
						new String[]{stageIdLkpValue}, 
						new org.hibernate.type.Type[]{new StringType()}
				);
		proList.add(projection,stageIdLkpValue);
		
		return proList;
	}

	private ProjectionList parsePIQMappingInfo(ProjectionList proList, String fieldName, boolean columnVisible){
		
		String aliasName="piqId";

		String sql = "(select " + 
							" (case when (piq_name_app_setting.setting_value is not null AND piq_name_app_setting.setting_value != '') or (piq_host_app_setting.setting_value is not null AND piq_host_app_setting.setting_value != '') then 'Integrated ' else '' end) + " +  
							" (case when (piq_name_app_setting.setting_value is not null AND piq_name_app_setting.setting_value != '') then '''' + piq_name_app_setting.setting_value + '''' + ' ' else '' end) +  " + 
							" (case when (piq_host_app_setting.setting_value is not null AND piq_host_app_setting.setting_value != '') then '''' + piq_host_app_setting.setting_value + '''' + ' ' else '' end)  " + 
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
							"AS "+ aliasName;

		
		Projection projection = Projections.sqlProjection(
						sql,
						new String[]{aliasName}, 
						new org.hibernate.type.Type[]{new StringType()}
				);
		proList.add(projection,aliasName);
		
		return proList;
	}
	
	private ProjectionList parseParentItem(ProjectionList proList, String fieldName, boolean columnVisible){
		//US1978 specific query for cabinet
		String aliasName="cabinetName";
		
		//CR51805 - Add Floor PDU (11) to show it's parent
		String sql="(CASE WHEN  this_.class_lks_id in (6,12,13) THEN  this_.item_name "+
               			"WHEN  this_.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_name from dct_items parentitem where this_.parent_item_id=parentitem.item_id) "+
							"ELSE null END) "+
							"as "+aliasName;
											
		Projection projection = Projections.sqlProjection(
			sql,
			new String[]{aliasName}, 
			new org.hibernate.type.Type[]{new StringType()}
		);
		proList.add(projection,aliasName);
		
		return proList;
	}
	
	private ProjectionList parseCabinetId(ProjectionList proList, String fieldName, boolean columnVisible){
		//US1978 specific query for cabinet
		String aliasName="cabinetId";
		
		String sql="(CASE WHEN  this_.class_lks_id in (6,12,13) THEN  this_.item_id "+
               			"WHEN  this_.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_id from dct_items parentitem where this_.parent_item_id=parentitem.item_id) "+
							"ELSE null END) "+
							"as "+aliasName;
											
		Projection projection = Projections.sqlProjection(
			sql,
			new String[]{aliasName}, 
			new org.hibernate.type.Type[]{new StringType()}
		);
		proList.add(projection,aliasName);
		
		return proList;
	}
	
	private ProjectionList parseParentCadHandle(ProjectionList proList, String fieldName, boolean columnVisible){
	
		//US1978 specific query for cabinet
		String aliasName="cadHandle";
		
		//Get CAD Handle directly from class:
		//Cabinet - 6
		//Floor PDU - 11
		//UPS - 12
		//CRAC - 13
		String sql="(CASE WHEN  this_.class_lks_id in (6,12,13) THEN  this_.cad_handle "+
               			"WHEN  this_.class_lks_id in (1,2,3,4,5,7,11) THEN (select cad_handle from dct_items parentitem where this_.parent_item_id=parentitem.item_id) "+
							"ELSE null END) "+
							"as "+aliasName;
											
		Projection projection = Projections.sqlProjection(
			sql,
			new String[]{aliasName}, 
			new org.hibernate.type.Type[]{new StringType()}
		);
		proList.add(projection,aliasName);
		
		return proList;
	}
	
	private ProjectionList parseBladeChassis(ProjectionList proList, String fieldName, boolean columnVisible){
		
		//US2102 specific query for bladeChassis
		String aliasName="chassis";
												
		String sql="(CASE WHEN  this_.subclass_lks_id in (104, 108) THEN  this_.item_name "+
               			"WHEN this_.subclass_lks_id in (105) THEN "+
               			"	("+
               					"select items.item_name  from dct_items_it chassisItem,dct_items items "+
                   				"where "+
                   				"this_.item_id=chassisItem.item_id and chassisItem.chassis_id=items.item_id"+
               			"	) "+

                        // CR59667 (Network Blade items') Chassis name is not displayed in the chassis column in the item list 
                        " WHEN this_.subclass_lks_id = 109 THEN this_.grouping_name " +

							"ELSE null END) "+
							"as "+ aliasName;
											
		Projection projection = Projections.sqlProjection(
			sql,
			new String[]{aliasName}, 
			new org.hibernate.type.Type[]{new StringType()}
		);
		proList.add(projection,aliasName);
		
		return proList;
	}
	
	private Criteria parseDomainObject(Criteria criteria, String fieldName, String domainType) throws ClassNotFoundException, HibernateException{
		
		for (String alias: getAliases("com.raritan.tdz.domain.CabinetItem",fieldName)){					
			if (!criteria.toString().contains(alias.replace(".","_"))){
				criteria.createAlias(alias, alias.replace(".","_"), Criteria.LEFT_JOIN);
				//log.info("0 createAlias="+alias+"  "+alias.replace(".","_"));
			}
		}
		
		for (String alias: getAliases("com.raritan.tdz.domain.ItItem",fieldName)){					
			if (!criteria.toString().contains(alias.replace(".","_"))){
				criteria.createAlias(alias, alias.replace(".","_"), Criteria.LEFT_JOIN);
				//log.info("1 createAlias="+alias+"  "+alias.replace(".","_"));
			}
		}
		
		for (String alias: getAliases("com.raritan.tdz.domain.MeItem",fieldName)){					
			if (!criteria.toString().contains(alias.replace(".","_"))){
				criteria.createAlias(alias, alias.replace(".","_"), Criteria.LEFT_JOIN);
				//log.info("2 createAlias="+alias+"  "+alias.replace(".","_"));
			}
		}
		
		for (String alias: getAliases(domainType,fieldName)){					
			if (!criteria.toString().contains(alias.replace(".","_"))){
				criteria.createAlias(alias, alias.replace(".","_"), Criteria.LEFT_JOIN);
				//log.info("3 createAlias="+alias+"  "+alias.replace(".","_"));
			}
		}
		
		return criteria;
	}
	
	private void parseAliasField(){
		
	}
	
	/** Update the default column array by setting column array. */
	private ColumnDTO[] updateVisible(ColumnDTO[] defColDtoArr, ColumnDTO[] colDtoArr){
				
		for(ColumnDTO defColumn : defColDtoArr) {
			for(ColumnDTO column : colDtoArr) {
				if(defColumn.getFieldName().equals(column.getFieldName()) && defColumn.getFieldLabel().equals(column.getFieldLabel()) ){
					defColumn.setVisible(column.isVisible());
										
					break;
				}
			}
		}
		
		return defColDtoArr;
	}
	
	/** This function is for the merged fields with same name but diff type. */
	private ColumnDTO[] updateCustomIds(ColumnDTO[] defColDtoArr, ColumnDTO[] colDtoArr){				
		
		for(ColumnDTO defColumn : defColDtoArr) {
			for(ColumnDTO column : colDtoArr) {
				if(defColumn.getFieldName().equals(column.getFieldName()) && defColumn.getFieldLabel().equals(column.getFieldLabel()) ){					
					
					column.setCustomIds(defColumn.getCustomIds());					
					break;
				}
			}
		}
		
		return colDtoArr;
	}
	
	/**
	 * We have a default column array generated from the column configure file and another column array generated from setting.
	 * Do the sync between the default column dto array and the setting column dto array, 
	 * 1. If the original default column array has more columns than the setting column array(ex. add several new columns in new release),
	 *    add the extra column dto to the setting column array.
	 * 2. If the original default column array has less columns, remove the extra column dto from the setting column dto array.
	 * 3. We have a new business rule about merging the custom fields, if the old setting still has the separated custom field with same name, 
	 *    remove the separated custom fields. NOTE: The new merged custom dto should be displayed in one column. 
	 * 
	 * @param defColumnDTOArr The column array from column configure file.
	 * @param settingColumnDTOArr The column array from saving setting
	 * @author kc.chen
	 * @since 3.1
	 */
	private ColumnDTO[] syncColumnArray(ColumnDTO[] defColumnDTOArr,ColumnDTO[] settingColumnDTOArr){
						
		if(defColumnDTOArr!= null && defColumnDTOArr.length>0 && settingColumnDTOArr!=null && settingColumnDTOArr.length>0){
			
			//Update the visible of default column DTO array from the criteria DTO array
			defColumnDTOArr = updateVisible(defColumnDTOArr ,settingColumnDTOArr);
			
			settingColumnDTOArr = updateCustomIds(defColumnDTOArr ,settingColumnDTOArr);
			
			settingColumnDTOArr = syncMoreColumnDto(defColumnDTOArr ,settingColumnDTOArr);
			
			settingColumnDTOArr = syncLessColumnDto(defColumnDTOArr ,settingColumnDTOArr);
			
			settingColumnDTOArr = removeCustomRepeatColumnDto(defColumnDTOArr ,settingColumnDTOArr);						
			
		}//end if defColumnDTOArr!= null
		return settingColumnDTOArr;
	}
	
	/**
	 * This function is for the old setting with repeat custom fields column. 
	 * These repeat custom fields(same name) will be just displayed one column. 
	 */
	private ColumnDTO[] removeCustomRepeatColumnDto(ColumnDTO[] defColumnDTOArr,ColumnDTO[] settingColumnDTOArr){
		ColumnDTO[] tmpSetColDtoArr = null;
		ColumnDTO tmpDefCol = null;
		ColumnDTO tmpSetCol = null , tmpRmSetCol;		
		ColumnDTO existedCol;
		int tmpRmSetColIdx = -1;
		int tmpSetColIdx = -1;
		boolean colExist = false;
		Map<Integer,ColumnDTO> existedSetColMap = new HashMap<Integer,ColumnDTO>();
		
		
		//List<ColumnDTO> removeColList = new ArrayList<ColumnDTO>();
		List<Integer> removeColList = new ArrayList<Integer>();
		
		
		for(int defIdx=0 ; defIdx < defColumnDTOArr.length ; defIdx++){
			tmpDefCol = defColumnDTOArr[defIdx];
			//It's custom field
			if(tmpDefCol!=null && tmpDefCol.getCustomIds()!=null && tmpDefCol.getCustomIds().length()>0){
				for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
					
					colExist = false;
					tmpSetCol = settingColumnDTOArr[setIdx];
					if(tmpDefCol.getFieldLabel().equals(tmpSetCol.getFieldLabel())){
						colExist = true;
						tmpSetColIdx = setIdx;
						existedSetColMap.put(setIdx, tmpDefCol);
						break;
					}
				}
				
				//Make sure the Column exists in setting, then mark the remove list
				if(colExist==true){					
					for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
						
						//skip the 1st match one
						if(setIdx == tmpSetColIdx)	continue;
						
						tmpSetCol = settingColumnDTOArr[setIdx];
						existedCol = existedSetColMap.get(tmpSetColIdx);
																			
						if( tmpSetCol!=null && existedCol!=null && tmpSetCol.getFieldLabel().equals(existedCol.getFieldLabel()) ){
							removeColList.add(setIdx);
						}	
					}
				}
			}
		}	
		
		boolean colNeedToRemove = false;
		
		if( removeColList!=null && removeColList.size()>0 ){
			tmpSetColDtoArr = new ColumnDTO[settingColumnDTOArr.length-removeColList.size()];
			
			int tmpNewSetColIdx = 0;
			
			for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
			
				tmpSetCol = settingColumnDTOArr[setIdx];
				
				for(int colIdx=0 ; colIdx < removeColList.size(); colIdx++ ){
					colNeedToRemove = false;
					tmpRmSetColIdx = removeColList.get(colIdx);
					
					//Current index is in the remove list
					if( tmpRmSetColIdx != -1 && tmpRmSetColIdx == setIdx){
						colNeedToRemove = true;
						tmpRmSetColIdx = -1;
						break;
					}
				}	
				
				//Not found in the remove list, add it.
				if( colNeedToRemove!=true ){
					tmpSetColDtoArr[tmpNewSetColIdx] = tmpSetCol;
					tmpNewSetColIdx++;
				}
			}
		}else{
			tmpSetColDtoArr = settingColumnDTOArr;
		}			
		
		return tmpSetColDtoArr;
	}
	
//	private ColumnDTO[] removeAllByName(String removedColumnName, ColumnDTO[] settingColumnDTOArr){
//		ColumnDTO tmpSetCol = null;
//		ColumnDTO[] tmpSettingColumnDtoArr = new ColumnDTO[settingColumnDTOArr.length-1];
//		int tmpIdx=0;
//		for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
//			tmpSetCol = settingColumnDTOArr[setIdx];
//			//Add the column dto, when the name doesn't equal the field name
//			if(removedColumnName!=null && !removedColumnName.equalsIgnoreCase(tmpSetCol.getFieldName())){
//				tmpSettingColumnDtoArr[tmpIdx] = tmpSetCol;
//				tmpIdx++;
//			}
//		}
//		
//		return tmpSettingColumnDtoArr;
//	}
	
	/** Remove the extra column dto from setting column dto(s), if they don't exist in default column dto.  */
	private ColumnDTO[] syncLessColumnDto(ColumnDTO[] defColumnDTOArr,ColumnDTO[] settingColumnDTOArr){
		boolean colExist = false;
		
		List<ColumnDTO> removeColList = new ArrayList<ColumnDTO>();
		ColumnDTO tmpDefCol = null;
		ColumnDTO tmpSetCol, tmpRmSetCol;
		
		//1st round check the original default dto column array existing in setting column dto array.
		for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
		
			tmpSetCol = settingColumnDTOArr[setIdx];
						
			for(int defIdx=0 ; defIdx < defColumnDTOArr.length; defIdx++){
				tmpDefCol = defColumnDTOArr[defIdx];
								
				if(tmpDefCol.getFieldLabel().equals(tmpSetCol.getFieldLabel())){
					colExist = true;
					break;
				}
			}
			
			if(colExist == false){
				removeColList.add(tmpSetCol);
			}
		}
		
		ColumnDTO[] tmpSetColDtoArr = null;
		List<ColumnDTO> tmpSetColDtoList = new ArrayList<ColumnDTO>();
		
		
		if( removeColList!=null && removeColList.size()>0 ){
			
			tmpSetColDtoArr = new ColumnDTO[settingColumnDTOArr.length - removeColList.size()];
			
//			for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
//				tmpSetColDtoArr[setIdx] = settingColumnDTOArr[setIdx];
//			}
			for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
				colExist = false;
				tmpSetCol = settingColumnDTOArr[setIdx];
			
				for(int colIdx=0 ; colIdx < removeColList.size(); colIdx++ ){
					tmpRmSetCol = removeColList.get(colIdx);
					
					if(tmpSetCol!=null && tmpRmSetCol!=null && tmpSetCol.getFieldLabel().equals(tmpRmSetCol.getFieldLabel())){						
						colExist = true;
					}
				}
				if(colExist==false){
					tmpSetColDtoList.add(tmpSetCol);
				}
			}	
			
			tmpSetColDtoArr = new ColumnDTO[tmpSetColDtoList.size()];
			
			for(int setColIdx=0 ; setColIdx<tmpSetColDtoList.size() ; setColIdx++ ){
				tmpSetColDtoArr[setColIdx] = tmpSetColDtoList.get(setColIdx);
			}
		}else{
			tmpSetColDtoArr = settingColumnDTOArr;
		}
		
		return tmpSetColDtoArr;
	}
	
	private ColumnDTO[] syncMoreColumnDto(ColumnDTO[] defColumnDTOArr,ColumnDTO[] settingColumnDTOArr){
		boolean colExist = false;
		
		List<ColumnDTO> addColList = new ArrayList<ColumnDTO>();
		ColumnDTO tmpDefCol;
		ColumnDTO tmpSetCol;
		
		//1st round check the original default dto column array existing in setting column dto array.
		for(int defIdx=0 ; defIdx < defColumnDTOArr.length; defIdx++){
			
			colExist = false;
			
			tmpDefCol = defColumnDTOArr[defIdx];
			
			for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
				
				tmpSetCol = settingColumnDTOArr[setIdx];
				
				if(tmpDefCol.getFieldLabel().equals(tmpSetCol.getFieldLabel())){
					colExist = true;
					break;
				}
			}
			
			if(colExist == false){
				addColList.add(tmpDefCol);
			}
		}
		
		ColumnDTO[] tmpSetColDtoArr = null;
		
		if( addColList!=null && addColList.size()>0 ){
			
			tmpSetColDtoArr = new ColumnDTO[settingColumnDTOArr.length + addColList.size()];
			
			for(int setIdx=0 ; setIdx < settingColumnDTOArr.length; setIdx++ ){
				tmpSetColDtoArr[setIdx] = settingColumnDTOArr[setIdx];
			}
			
			for(int colIdx=0 ; colIdx < addColList.size(); colIdx++ ){
				tmpSetCol = addColList.get(colIdx);
				
				tmpSetColDtoArr[settingColumnDTOArr.length + colIdx] = tmpSetCol;
			}
		}else{
			tmpSetColDtoArr = settingColumnDTOArr;
		}
		
		return tmpSetColDtoArr;
	}
	
	/**
	 * Get the the item page list.
	 *  
	 */
	public ListResultDTO getPageList(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
	
		ListResultDTO listResultDTO=new ListResultDTO();
		Map aliasMap=new HashMap();
		Map idProMap = new HashMap();
		
		int totalRows=0;
		
		configClientUtcTime(listCriteriaDTO);
		
		//get ListCriteriaDTO
		listCriteriaDTO=super.getListCriteriaDTO(listCriteriaDTO,pageType);
		
		int maxLinesPerPage=listCriteriaDTO.getMaxLinesPerPage() > 0 ? listCriteriaDTO.getMaxLinesPerPage() : ListCriteriaDTO.ROWS_PER_PAGE_50;
		int pageNumber=listCriteriaDTO.getPageNumber() > 0 ? listCriteriaDTO.getPageNumber() : 1;
		
		List<ColumnCriteriaDTO> columnCriteriaDTOList=listCriteriaDTO.getColumnCriteria();
		List<ColumnDTO> columnDTOList=listCriteriaDTO.getColumns();
				
		ColumnCriteriaDTO[] columnCriteriaDTOArr=(ColumnCriteriaDTO[])columnCriteriaDTOList.toArray(new ColumnCriteriaDTO[0]);
		ColumnDTO[] columnDTOArr=(ColumnDTO[])columnDTOList.toArray(new ColumnDTO[0]);

		listCriteriaDTO.setMaxLinesPerPage(maxLinesPerPage);
		listCriteriaDTO.setPageNumber(pageNumber);

		List list=new ArrayList();
		
		try {
		
			//Pre-process sorting for specific column
			columnCriteriaDTOArr = preProcessSorting(columnCriteriaDTOArr);
			
			log.info("checking columnCriteriaDTO..");
			for(ColumnCriteriaDTO dto : columnCriteriaDTOArr) {
				log.info("input ColumnCriteriaDTO.name="+dto.getName());
				log.info("input ColumnCriteriaDTO.toSort="+dto.isToSort());
				
				// To know which projection that should be used for itemId.
				if (dto.isToSort() || dto.getFilter() != null) {
					idProMap.put(dto.getName(), null);
				}
			}
		
			Session session = this.sessionFactory.getCurrentSession();
			
			String domainType="com.raritan.tdz.domain.Item";
			
			//First create a criteria  given an entity name such as com.raritan.tdz.domain.Item
			Criteria criteria = session.createCriteria(domainType);

			ProjectionList proList = Projections.projectionList();
			
			// The second criteria and projection list for detail data (with all projections)
			Criteria detailCriteria = session.createCriteria(domainType);			
			ProjectionList proListIdOnly = Projections.projectionList();
			if (idProMap.size() > 0)
				proListIdOnly.add(Projections.distinct(Projections.property("itemId")));
			else
				proListIdOnly.add(Projections.property("itemId"));		// This should not happen because there is always a default sort
			
			//Now for each of the columns that you want to display, create the alias and projections for each one of them
			
			//Get default column dto
			ColumnDTO[] defColumnDTOArr = getColumnConfigArray(true,PaginatedHomeBase.PAGE_TYPE_ITEMLIST);
									
			columnDTOArr = syncColumnArray(defColumnDTOArr, columnDTOArr);
			
			//#CR50034 Using the original default column dto to loop the list here
			//for(ColumnDTO column : columnDTOArr) {
			for(ColumnDTO column : defColumnDTOArr) {
				
				String fieldName=column.getFieldName();
				boolean visible=column.isVisible();
				log.info("fieldName="+fieldName + " fieldLabel=" + column.getFieldLabel());
				if(fieldName==null || "".equals(fieldName)) {
					continue;
				}
											
				//For custom field
				if(fieldName.indexOf("custom_")!=-1) {
					//proList = parseCustomField(proList, fieldName, visible);														
					proList = parseCustomField(proList, fieldName, visible, column);										
					
				} else if("slotLabel".equalsIgnoreCase(fieldName)) {
					proList = parseSlotLabel(proList, fieldName, visible);					
				} else if("parentItem".equalsIgnoreCase(fieldName)) {
					proList = parseParentItem(proList, fieldName, visible);																				
				} else if("CabinetId".equalsIgnoreCase(fieldName)) {
					proList = parseCabinetId(proList, fieldName, visible);										
				} else if("ParentCadHandle".equalsIgnoreCase(fieldName)) {
					proList = parseParentCadHandle(proList, fieldName, visible);
				} else if("bladeChassis".equalsIgnoreCase(fieldName)) {
					proList = parseBladeChassis(proList, fieldName, visible);	
				} else if("stageIdLkpValue".equalsIgnoreCase(fieldName)) {
					proList = parseRequestStage(proList, fieldName, visible);		
				} else if("piqId".equalsIgnoreCase(fieldName)) {
					proList = parsePIQMappingInfo(proList, fieldName, visible);
				} else if("ipAddresses".equalsIgnoreCase(fieldName)) {
				
					//US2212 Display IP Addresses in Items List
					String aliasName="addresses";
										
					String sql="(select array_to_string(array("+
					"	select addr.ipaddress "+
					"	from dct_items items, dct_ports_data ports, tblipteaming ipteam,tblipaddresses addr "+
					"	where "+
					"	items.item_id=ports.item_id and ports.port_data_id=ipteam.portid and ipteam.ipaddressid=addr.id and items.item_id=this_.item_id "+
					"),';')) "+
					"as "+aliasName;
														
					Projection projection = Projections.sqlProjection(
						sql,
						new String[]{aliasName}, 
						new org.hibernate.type.Type[]{new StringType()}
					);
					proList.add(projection,aliasName);
					
				} else if("itemRequestNumber".equalsIgnoreCase(fieldName)) {
						proList = parseRequestNumber(proList, fieldName, visible);
				} else {
					
					criteria = parseDomainObject(criteria, fieldName, domainType);
					detailCriteria = parseDomainObject(detailCriteria, fieldName, domainType);
					
					//Projections
					String traceStr = getFieldTrace("com.raritan.tdz.domain.CabinetItem",fieldName);
					//log.info("0 traceStr="+traceStr+" fieldName="+fieldName);
					if("".equals(traceStr)) {
						traceStr=getFieldTrace("com.raritan.tdz.domain.ItItem",fieldName);
						//log.info("1 traceStr="+traceStr);
					}
					if("".equals(traceStr)) {
						traceStr=getFieldTrace("com.raritan.tdz.domain.MeItem",fieldName);
						//log.info("2 traceStr="+traceStr);
					}
					if("".equals(traceStr)) {
						traceStr=getFieldTrace(domainType,fieldName);
						//log.info("3 traceStr="+traceStr);
					}
									
					String alias = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : traceStr;
					String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0, traceStr.lastIndexOf(".")).replace(".","_") : "this.";
					
					//log.info("aliasForProjection="+aliasForProjection+" alias="+alias);
					
					if("$HWD".equalsIgnoreCase(fieldName)) {
						
						Projection projection = Projections.sqlProjection(
							"(dim_h || ' x ' || dim_w || ' x ' || dim_d) as HWD",
							new String[]{"HWD"}, 
							new org.hibernate.type.Type[]{new StringType()}
						);
						
						proList.add(projection,"HWD");
					/*} else if("stageIdLkpValue".equalsIgnoreCase(fieldName)) {
						proList = parseRequestStage(proList, fieldName, visible);	*/		
					} else {
					
						if("itemId".equals(fieldName)) {
							//CR48287 - prevent duplicate data when paging
							proList.add(Projections.distinct(Projections.property(fieldName)));
						} else {
							proList.add(Projections.alias(Projections.property(aliasForProjection.toString() + alias), fieldName));
						}						
						//log.info("proList="+aliasForProjection.toString() + alias+"  "+alias);						
					}
					
					aliasMap.put(fieldName,aliasForProjection.toString() + alias);
					
				} 
				// Get last projection if it need to be used in the id projection
				String key = column.getFieldLabel();
				if (fieldName.indexOf("custom_")!=-1) {
					// Use field name if it is a custom field
					key = column.getFieldName();
				}
				if (idProMap.containsKey(key)) {
					Projection projection = proList.getProjection(proList.getLength() - 1);
					log.info("Add id projection " + projection.getAliases()[0] + " " + projection.toString());
					proListIdOnly.add(projection, projection.getAliases()[0]);
				}
				
			}//end for(ColumnDTO column : columnDTO)

			log.info("aliasMap="+aliasMap);
			log.info("proList="+proList);
			log.info("prodListIdOnly=" + proListIdOnly);

            // CR56523 Filter the Class and Mounting for the special six columns.
            // Search 'CR49126' in this file for the restrictions.
            boolean isRestrictedByTheFilter = false;
            if (columnCriteriaDTOArr != null && columnCriteriaDTOArr.length > 0) {
                for(ColumnCriteriaDTO columnCriteriaDto : columnCriteriaDTOArr) {
                    if ( columnCriteriaDto == null || columnCriteriaDto.getFilter() == null) {
                        continue;
                    }

                    if("Rails Used".equals(columnCriteriaDto.getName())) {
                        isRestrictedByTheFilter = true;

                        // Mounting
                        criteria.add(
                            Restrictions.disjunction()
                                .add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.RACKABLE + "%").ignoreCase())
                                .add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.NON_RACKABLE + "%").ignoreCase())
                                .add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.FREE_STANDING + "%").ignoreCase())
                        );

                        // Class
                        criteria.add(Restrictions.in("classLookup.lkpValueCode",
                            new Long[] {
                                SystemLookup.Class.DEVICE,
                                SystemLookup.Class.NETWORK,
                                SystemLookup.Class.PROBE,
                                SystemLookup.Class.DATA_PANEL,
                                SystemLookup.Class.RACK_PDU,
                                SystemLookup.Class.FLOOR_OUTLET
                            }
                        ));

                    } else if("Orientation".equals(columnCriteriaDto.getName())) {
                        isRestrictedByTheFilter = true;

                        // Mounting
                        criteria.add(
                                Restrictions.disjunction()
                                    .add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.RACKABLE + "%").ignoreCase())
                                    .add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.NON_RACKABLE + "%").ignoreCase())
                                    .add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.FREE_STANDING + "%").ignoreCase())
                            );

                        // Class
                        criteria.add(Restrictions.in("classLookup.lkpValueCode",
                            new Long[] {
                                SystemLookup.Class.DEVICE,
                                SystemLookup.Class.NETWORK,
                                SystemLookup.Class.PROBE,
                                SystemLookup.Class.DATA_PANEL,
                                SystemLookup.Class.RACK_PDU,
                                SystemLookup.Class.FLOOR_OUTLET
                            }
                        ));

                    } else if("Cabinet Side".equals(columnCriteriaDto.getName())) {
                        isRestrictedByTheFilter = true;

                        // Mounting
                        criteria.add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.ZERO_U + "%").ignoreCase());

                    } else if("Depth Position".equals(columnCriteriaDto.getName())) {
                        isRestrictedByTheFilter = true;

                        // Mounting
                        criteria.add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.ZERO_U + "%").ignoreCase());

                    } else if("Front Faces".equals(columnCriteriaDto.getName())) {
                        isRestrictedByTheFilter = true;

                        // Mounting
                        criteria.add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.FREE_STANDING + "%").ignoreCase());

                        // Class
                        criteria.add(Restrictions.in("classLookup.lkpValueCode",
                            new Long[] {
                                SystemLookup.Class.CABINET,
                                SystemLookup.Class.FLOOR_PDU,
                                SystemLookup.Class.UPS,
                                SystemLookup.Class.CRAC
                            }
                        ));

                    } else if("Chassis Face".equals(columnCriteriaDto.getName())) {
                        isRestrictedByTheFilter = true;

                        // Mounting
                        criteria.add(Restrictions.like("model.mounting", "%" + SystemLookup.Mounting.BLADE + "%").ignoreCase());
                    } else if("Name".equals(columnCriteriaDto.getName())) {
                    	columnCriteriaDto = replaceSqlLikeSign(columnCriteriaDto);
                    }
                }
            }//end if (columnCriteriaDTO != null && columnCriteriaDTO.length > 0) {

            if (!isRestrictedByTheFilter) {
				//All classes
				//Added classes on Oct.1,2012 by Randy Chen
				//CR46988 Add FLOOR_PDU,UPS,CRAC
				criteria.add(Restrictions.in("classLookup.lkpValueCode",
					new Long[]{
						SystemLookup.Class.CABINET,
						SystemLookup.Class.DEVICE,
						SystemLookup.Class.NETWORK,
						SystemLookup.Class.PROBE,
						SystemLookup.Class.DATA_PANEL,
						SystemLookup.Class.RACK_PDU,
						SystemLookup.Class.FLOOR_OUTLET,
						SystemLookup.Class.FLOOR_PDU,
						SystemLookup.Class.UPS,
						SystemLookup.Class.CRAC
					}
				));
            }

			//CR47920 Remove hidden items
			criteria.add(Restrictions.not(
				Restrictions.in("statusLookup.lkpValueCode",new Long[]{SystemLookup.ItemStatus.HIDDEN}
				)
			));
						
			criteria.add(Restrictions.gt("this.itemId",new Long(-1)));

			//From common applyCommonFilters			
			//Remove this condition 
			//Aug. 21,2012
			//criteria.add(Restrictions.ne("statusLookup.lkpValueCode", SystemLookup.ItemStatus.DELETED));
			
			//Loop for filter
			processFilter(domainType,criteria,columnCriteriaDTOArr,columnDTOArr,aliasMap,getClientUtcTimeString());
			
			//getTotalRows
			criteria.setProjection(Projections.rowCount());
			
			//CR53063 skipped when first query 
			if(listCriteriaDTO.isFirstQuery()==false){
				totalRows= ((Long)criteria.uniqueResult()).intValue();
			}
			
			log.info("totalRows="+totalRows);
			
			//Loop for sorting
			columnCriteriaDTOArr=processSorting(criteria,columnCriteriaDTOArr,columnDTOArr,aliasMap, detailCriteria);

			
			//maxLinesPerPage=-1 //-> fetch all data
			if(listCriteriaDTO.getFitType() == ListCriteriaDTO.ALL) {
				maxLinesPerPage=totalRows;
			}
			
			//Compute first result
			int firstResult=(pageNumber-1)*maxLinesPerPage;
						
			criteria.setFirstResult(firstResult);
			//criteria.setFetchSize(maxLinesPerPage); //CR48287 fixed here - don't use this method
			criteria.setMaxResults(maxLinesPerPage);
									
			//CR53063 skipped when first query 
			if(listCriteriaDTO.isFirstQuery()==false) {			

				if (listCriteriaDTO.getFitType() != ListCriteriaDTO.ALL) {
//					log.info("First criteria.list() to get id " + firstResult + " " + maxLinesPerPage);
					
					criteria.setProjection(proListIdOnly);
					List objs = criteria.list();
					List<Long> itemIds;
					
					if (proListIdOnly.getLength() > 1) {
						itemIds = new ArrayList();
						Object[] objArr;
						for (Object obj: objs) {
							objArr = (Object[])obj;
							Long id = (Long)objArr[0];
//							log.info("itemId " + id);
							itemIds.add(id);
						}
					} else {
						itemIds = (List<Long>)objs;
					}
					
					log.info("First criteria.list() result " + itemIds.size());
					
					if (itemIds.size() > 0)	{			
						detailCriteria.add(Restrictions.in("itemId", itemIds));
					
						detailCriteria.setProjection(proList);
						list = detailCriteria.list();
					}
				} else {
					criteria.setProjection(proList);
					list = criteria.list();
				}
			}
			log.info("Second criteria.list() result " + list.size());
			List itemRsList = postProcessResults(list,columnDTOArr,defColumnDTOArr);
			
			listCriteriaDTO.setColumnCriteria(Arrays.asList(columnCriteriaDTOArr));
			listCriteriaDTO.setColumns(Arrays.asList(columnDTOArr));	
						
			listCriteriaDTO=updateDTOAttributeForOutput(listCriteriaDTO);
			
			listResultDTO.setListCriteriaDTO(listCriteriaDTO);
			//listResultDTO.setValues(list);
			listResultDTO.setValues(itemRsList);
			
			listResultDTO.setTotalRows(totalRows);

			log.info("pageNumber="+pageNumber);
			log.info("firstResult="+firstResult);
			log.info("maxLinesPerPage="+maxLinesPerPage);
			log.info("listResultDTO="+listResultDTO);
			log.info("totalRows="+totalRows);
			log.info("list="+list.size());
			log.info("itemRsList="+itemRsList.size());
			log.info("defColumnDTOArr.length="+defColumnDTOArr.length);
			log.info("columnDTOArr.length="+columnDTOArr.length);
			
		}catch(ClassNotFoundException e){
		
			log.error("ClassNotFoundException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(QueryException e){
		
			log.error("QueryException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(HibernateException e){
		
			log.error("HibernateException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
		
			log.error("DataAccessException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			
		}

		return listResultDTO;
	}
	/** 
	 * Replace the sql like string sign '_' & '%'. 
	 * DCT-3694 Incorrect filtering in the item list using '_' and '%'
	 */
	private ColumnCriteriaDTO replaceSqlLikeSign(ColumnCriteriaDTO columnCriteriaDto){
		
		if(columnCriteriaDto!=null){
			FilterDTO filterDto = columnCriteriaDto.getFilter();
			if(filterDto!=null){
				String queryStr = filterDto.getEqual();
				StringBuilder sbNewQueryStr=new StringBuilder("");
				//Parse the like string
				if(queryStr != null && !"".equals(queryStr)){
					char[] queryCharArr = queryStr.toCharArray();
					if(queryCharArr!=null && queryCharArr.length>0){
						for(int idx=0; idx<queryCharArr.length;idx++){
							if(queryCharArr[idx] == '%' || queryCharArr[idx] == '_'){
								sbNewQueryStr.append("\\");
								sbNewQueryStr.append(queryCharArr[idx]);
							}else{
								sbNewQueryStr.append(queryCharArr[idx]);
							}
						}
					}										
					filterDto.setEqual(queryStr);
					columnCriteriaDto.setFilter(filterDto);
				}								
			}			
			
		}
		return columnCriteriaDto;
	}
	
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType) {
	
		ListResultDTO listResultDTO=null;
		
		List<ColumnDTO> columns=listCriteriaDTO.getColumns();
			
		try {
			listResultDTO=getPageList(listCriteriaDTO,pageType);
		} catch (DataAccessException dae) {
			log.error("",dae);
		} catch (Exception e) {
			log.error("Error from getPageList:",e);
		}
		
		return super.getValueList(listResultDTO,pageType);
	}
	
	/*
	public ColumnCriteriaDTO[] preProcessSorting(ColumnCriteriaDTO[] columnCriteriaDTO,ColumnDTO[] columnDTO) {
	
		log.info("preProcessSorting..columnCriteriaDTO.length="+columnCriteriaDTO.length);
			
		//Default sort for item list
		if ( columnCriteriaDTO==null || columnCriteriaDTO.length==0 ) {
		
			//US1028 and CR48032 default sort
			log.info("No column criteria->apply default sort");
			columnCriteriaDTO=new ColumnCriteriaDTO[1];
			ColumnCriteriaDTO dto = new ColumnCriteriaDTO();
			
			//Get the first visible column (from left)
			String firstVisableFeldLabel="";
			
			for(int i=0;i<columnDTO.length;i++) {
				ColumnDTO colDTO=columnDTO[i];
				
				if(colDTO.isVisible()) {
					firstVisableFeldLabel=colDTO.getFieldLabel();
					break;
				}
			}

			log.info("firstVisableFeldLabel="+firstVisableFeldLabel);
			
			dto.setName(firstVisableFeldLabel);
			dto.setToSort(true);
			dto.setSortDescending(false);
			
			columnCriteriaDTO[0]=dto;
		
		}
		
		return columnCriteriaDTO;
	}
	*/
	
	public ColumnCriteriaDTO[] preProcessSorting(ColumnCriteriaDTO[] columnCriteriaDTO) {
	
		if(isNoSorting(columnCriteriaDTO)) {
		
			//CR49208 - default sort should be "Name" column
			//US1208 #5 / US1029 #4
			ColumnCriteriaDTO dto = new ColumnCriteriaDTO();
			dto.setName("Name");
			dto.setToSort(true);
			dto.setSortDescending(false);
		
			columnCriteriaDTO=getDefaultSortColumnCriteriaDTO(columnCriteriaDTO,dto);
			log.info("Add default sorting Name");
		}
		
		return columnCriteriaDTO;
	}

	protected String getFieldNameByUid(String uiComponentId) {
	
		String fieldName=(String)uidFieldNameMap.get(uiComponentId);
	
		return fieldName;
	}
	
	@Transactional(readOnly = true)
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException{
		return super.getUserConfig(pageType);
	}
	
	@Transactional(readOnly = true)
	public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException{
		return super.getDefUserConfig(pageType);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return super.saveUserConfig(itemListCriteria, pageType);
	}
	

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return super.deleteUserConfig(itemListCriteria, pageType);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException{
		return super.resetUserConfig(itemListCriteria, pageType, fitRows);
	}
	
	/**
	 * Do some special modifications for client's display
	 * @param list - Criteria list
	 * @param columnDTO
	 */
	protected List postProcessResults(List itemRsList,ColumnDTO[] columnDTOArr, ColumnDTO[] defColumnDtoArr) {
	
		itemRsList = syncItemListResultsColumnOrder(itemRsList, columnDTOArr, defColumnDtoArr);
		
		int indexMounting=getFieldLabelIndex(columnDTOArr,"Mounting");
		int indexClass=getFieldLabelIndex(columnDTOArr,"Class");

		String unit="1";
		UserInfo userInfo = FlexUserSessionContext.getUser();
		if(userInfo!=null) {
			unit=userInfo.getUnits();
			long id=userInfo.getId();
			String userId=userInfo.getUserId();
			log.info("postProcessResults id="+id+" userId="+userId+" unit="+unit);								
		}
		
		//unit="2"; //SI debug
	
		int uPositionIndex = -1;
		int slotPositionIndex = -1;				
				
		for ( int colIdx = 0; colIdx < columnDTOArr.length; colIdx++ ) {
		
			String fieldName=columnDTOArr[colIdx].getFieldName();						
					
			if ( "uPosition".equalsIgnoreCase( fieldName ) ) {
				uPositionIndex = colIdx;
			}
			if ( "slotPosition".equalsIgnoreCase( fieldName ) ) {
				slotPositionIndex = colIdx;
			}
			
			String uiComponentId=columnDTOArr[colIdx].getUiComponentId();
			
			try {
			
				if(uiComponentId!=null && !"".equals(uiComponentId)) {
				
					//log.info("uiComponentId="+uiComponentId);
							
					String remoteRef = rulesProcessor.getRemoteRef(uiComponentId);
				
					//log.info("remoteRef="+remoteRef);
					
					//process for HWD
					if("tiDimension".equals(uiComponentId) || "$HWD".equals(fieldName)) {
					
						//Borrow tiRailWidth for inch to mm conversion
						UnitConverterIntf unitConverter = remoteReference.getRemoteRefUnitConverter("CONSTANT_RAIL_WIDTH_FOR_ITEM");
						//log.info("unitConverter="+unitConverter);
						
						String valObj = null;
						Object tmpObj = null;
						Object[] objArr;
						
						for(int criteriaIdx=0; criteriaIdx <itemRsList.size(); criteriaIdx++) {
							objArr=(Object[])itemRsList.get(criteriaIdx);														
							
							tmpObj = objArr[colIdx];
							
							if(tmpObj==null)
								continue;
								
							if (tmpObj instanceof String){
								valObj = (String)tmpObj;
							}else{								
								valObj = tmpObj.toString();
							}
							
							String[] valArr=valObj.split("x");
							
							if(valArr!= null && valArr.length==3){
							
								double height=Double.parseDouble(valArr[0]);
								double width=Double.parseDouble(valArr[1]);
								double depth=Double.parseDouble(valArr[2]);
								
								Object newHeight = unitConverter.convert(height, unit);
								Object newWidth = unitConverter.convert(width, unit);
								Object newDepth = unitConverter.convert(depth, unit);
								
								String newValue=newHeight+" x "+newWidth+" x "+newDepth;
								
								objArr[colIdx]=newValue;
							}else{
								log.warn("The index of item list result about $HWD composing is wrong.");
							}
							//log.debug("dim valObj="+valObj+" newValue="+newValue);							
						}
					
					} else {
					
						UnitConverterIntf unitConverter = remoteReference.getRemoteRefUnitConverter(remoteRef);
						//log.info("unitConverter="+unitConverter);
					
						if (unitConverter != null) {
							for(int criteriaIdx=0;criteriaIdx<itemRsList.size();criteriaIdx++) {
								Object[] objArr=(Object[])itemRsList.get(criteriaIdx);
								Object valObj=objArr[colIdx];
							
								Object newValue = unitConverter.convert(valObj, unit);
								objArr[colIdx]=newValue;
								//log.debug("valObj="+valObj+" newValue="+newValue);

							}
						
						}
					
					}
					
						
				}//end if(uiComponentId!=null && !"".equals(uiComponentId))							
				
			}catch(org.apache.commons.jxpath.JXPathNotFoundException e) {
				//log.info("Convert Bypass:"+e);
			}
			
			//CR49126 - for special six column mapping checking
			/*
			Reference
			Provider: Samer
			case WHEN dct_models.mounting In ('Rackable','Non-rackable','Free-Standing') AND dct_items.class_lks_id Not In (6,11,12,13) THEN rails.lkp_value END AS "RailsUsed",
			case WHEN dct_models.mounting In ('Rackable','Non-rackable','Free-Standing') AND dct_items.class_lks_id Not In (6,11,12,13) THEN face.lkp_value END AS "Orientation",
			case WHEN dct_models.mounting = 'ZeroU' THEN rails.lkp_value END AS "CabinetSide", case WHEN dct_models.mounting = 'ZeroU' THEN face.lkp_value END AS "DepthPosition",
			case WHEN dct_models.mounting = 'Free-Standing' AND dct_items.class_lks_id In (6,11,12,13) THEN face.lkp_value END AS "FrontFaces",
			case WHEN dct_models.mounting = 'Blade' THEN face.lkp_value END AS "ChassisFace",
			*/
			
			String fieldLabel=columnDTOArr[colIdx].getFieldLabel();
			List lookupList=(List)lookupMap.get(fieldLabel);
						
			if(lookupList!=null) {
				
				Object tmpObj = null;
				String valObj = null;				
				
				for(int j=0;j<itemRsList.size();j++) {//result list
					Object[] objArr=(Object[])itemRsList.get(j);										
					
					tmpObj = objArr[colIdx];
					
					//String valObj=(String)objArr[colIdx];  
					
					if(tmpObj != null){
						if (tmpObj instanceof String){								
							valObj = (String)tmpObj;
						}else{								
							valObj = tmpObj.toString();
						}
					}
						
					if(valObj==null)
						continue;

					if(!lookupList.contains(valObj)) {
						objArr[colIdx]=""; //set to empty
					}
					
					String mounting=(String)objArr[indexMounting];
					String className=(String)objArr[indexClass];
					
					if(mounting!=null && className!=null) {
					
						//log.info("fieldLabel="+fieldLabel+" mounting="+mounting+" className="+className);
						
						if("Rails Used".equals(fieldLabel) && 
							(mounting.equals(SystemLookup.Mounting.RACKABLE) ||
							mounting.equals(SystemLookup.Mounting.NON_RACKABLE) ||
							mounting.equals(SystemLookup.Mounting.FREE_STANDING))
							&&
							!(className.equals("Cabinet") ||
							className.equals("Floor PDU") ||
							className.equals("UPS") || 
							className.equals("CRAC"))) {
							
						} else if("Orientation".equals(fieldLabel) && 
							(mounting.equals(SystemLookup.Mounting.RACKABLE) ||
							mounting.equals(SystemLookup.Mounting.NON_RACKABLE) ||
							mounting.equals(SystemLookup.Mounting.FREE_STANDING))
							&&
							!(className.equals("Cabinet") ||
							className.equals("Floor PDU") ||
							className.equals("UPS") || 
							className.equals("CRAC"))) {
						
						} else if("Cabinet Side".equals(fieldLabel) && 
							mounting.equals(SystemLookup.Mounting.ZERO_U)) {
							
						} else if("Depth Position".equals(fieldLabel) && 
							mounting.equals(SystemLookup.Mounting.ZERO_U)) {
							
						} else if("Front Faces".equals(fieldLabel) && 
							mounting.equals(SystemLookup.Mounting.FREE_STANDING)
							&&
							(className.equals("Cabinet") ||
							className.equals("Floor PDU") ||
							className.equals("UPS") || 
							className.equals("CRAC"))) {
							
						} else if("Chassis Face".equals(fieldLabel) && 
							mounting.equals(SystemLookup.Mounting.BLADE)) {
						
						} else {
							objArr[colIdx]=""; //set to empty
						}
					
					}
					
				} //for j
				
			}
			
		}
	
		for ( Object obj : itemRsList ) {
			Object[] objarray = (Object[])obj;
			if ( uPositionIndex>-1 && objarray[uPositionIndex] != null ) {
				String tmp = objarray[uPositionIndex].toString();
				objarray[uPositionIndex] = tmp.equals( "-9" ) ? "" : ( tmp.equals( "-2" ) ? "Above" : ( tmp.equals( "-1" ) ? "Below" : tmp ) );
			}
			if ( slotPositionIndex>-1 && objarray[slotPositionIndex] != null ) {
				String tmp = objarray[slotPositionIndex].toString();
				objarray[slotPositionIndex] = tmp.equals( "-9" ) ? "" : tmp;
			}
			
		}
		
		return itemRsList;
				
	}	
	
	/** 
	 * Using the original default column dto would select the wrong order or some setting column dto which don't exist.
	 * Skip the column(s) that doesn't exist in settingColumnDTOArr and re-order the result list by setting column dto array.
	 */
	private List syncItemListResultsColumnOrder(List itemRsList,ColumnDTO[] settingColumnDTOArr, ColumnDTO[] defColumnDtoArr){
		
		List itemRtnList = new ArrayList();
		
		ColumnDTO settingColDto;
		ColumnDTO defColDto;
		
		Object[] objArr;
		Object[] newObjArr;
		int settingIdx=0;
		if(itemRsList!=null && itemRsList.size()>0){
			for ( Object objRs : itemRsList ) {
				if(objRs!=null){
					
					//The sequence of the array is the defColumnDtoArr's, re-order to settingColumnDTOArr's
					objArr = (Object[])objRs;
					
					if(objArr!=null && objArr.length>0){
												
						newObjArr = new Object[settingColumnDTOArr.length];
						
						for(int objIdx=0; objIdx<objArr.length; objIdx++){
							defColDto = defColumnDtoArr[objIdx];
							if(defColDto!=null){
								settingIdx = getSettingColumnDtoOrder(defColDto, settingColumnDTOArr);
								if(settingIdx != -1){// -1 = not found
									newObjArr[settingIdx] = objArr[objIdx];
								}
							}	
						}
						
						itemRtnList.add(newObjArr);
					}
				}	
			}
		}

		return itemRtnList;
	}
	
	/**
	 * Get the index from setting column dto array by default column dto.
	 * @param defColumn
	 * @param settingColumnDTOArr
	 * @return -1 - Not found
	 */
	private int getSettingColumnDtoOrder(ColumnDTO defColumn, ColumnDTO[] settingColumnDTOArr){
		ColumnDTO settingColDto;
		int settingColIndex = -1;
		for(int settingIdx = 0; settingIdx<settingColumnDTOArr.length; settingIdx++){
			settingColDto = settingColumnDTOArr[settingIdx];
			if(settingColDto!=null && defColumn!=null && settingColDto.getFieldLabel().equals(defColumn.getFieldLabel())){
				settingColIndex = settingIdx;
				break;
			}
		}
		
		return settingColIndex;
	}
	
	private int getFieldLabelIndex(ColumnDTO[] columnDTO,String checkFieldLabel) {
	
		for(int i=0;i<columnDTO.length;i++) {					
						
			ColumnDTO column=columnDTO[i];
			
			if(column==null) {
				continue;
			}
			
			String fieldLabel=column.getFieldLabel();
			if(fieldLabel.equalsIgnoreCase(checkFieldLabel)) {
				return i;
			}
			
		}
	
		return -1;
	}
	
	public String getItemActionMenuStatus( List<Long> itemIdList ) {
		return new ItemActionMenuStatus( sessionFactory, itemHome, itemClone, powerPortMoveDAO, userDao).getItemActionMenuStatus( itemIdList );
	}
}
