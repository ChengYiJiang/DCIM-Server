/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
@Transactional
public class UnitTestItemDAOImpl implements UnitTestItemDAO {

	
	SessionFactory sf;
	
	@Override
	public void setSf(SessionFactory sf) {
		this.sf = sf;
	}

	@Autowired
	ItemHome itemHome;
	
	
	UnitTestItemDAOImpl(SessionFactory sf){
		this.sf = sf;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#createUnitTestLocation()
	 */
	@Override
	public DataCenterLocationDetails createUnitTestLocation() throws Throwable {
		DataCenterLocationDetails location = null;
		Session session = sf.getCurrentSession();
		String locationName = "UnitTestDC";
		location = getTestLocation(locationName);
		
		if(location == null){
			location = new DataCenterLocationDetails();
			location.setCode(locationName);
			location.setDcName(locationName);
			location.setArea(1000L);
			location.setComponentTypeLookup( SystemLookup.getLksData(session, SystemLookup.DcLocation.SITE) );
			session.save( location );
		}
		
		session.flush();
		
		return location;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#deleteUnitTestLocation(com.raritan.tdz.domain.DataCenterLocationDetails)
	 */
	@Override
	public void deleteUnitTestLocation(DataCenterLocationDetails location)
			throws Throwable {
		
		Session session = sf.getCurrentSession();
		if (location != null && session.isOpen()){
			session.delete(location);
			location = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#createCabinet(java.lang.Long, java.lang.String)
	 */
	@Override
	public Map<String, UiComponentDTO> createCabinet(Long locationId,
			String modelName, List<ValueIdDTO> additionalParams) throws ClassNotFoundException,
			BusinessValidationException, Throwable {
		//First create a cabinet
		List<ValueIdDTO> cabinetDTOList = createNewItemDTO("CAB-" + System.currentTimeMillis(), modelName );
		//Place the cabinet
		List<ValueIdDTO> cabinetPlacementDTOList = placeCabinetItem(locationId,"1","1","fasfads");
		
		cabinetDTOList.addAll(cabinetPlacementDTOList);
		
		replaceOrAdd(cabinetDTOList,additionalParams);
		
		//Save the cabinet
		Map<String, UiComponentDTO> cabinetDTOMap = itemHome.saveItem(new Long(-1), cabinetDTOList, getTestAdminUser());
		
		return cabinetDTOMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#createStackableItem(java.lang.Long, java.lang.Long, long, java.lang.String)
	 */
	@Override
	public Map<String, UiComponentDTO> createStackableItem(Long location,
			Long cabinetId, long uPosition, String modelName, List<ValueIdDTO> additionalParams)
			throws ClassNotFoundException, BusinessValidationException,
			Throwable {
		//Then create the stackable
		List<ValueIdDTO> newItemDTOList = createNewItemDTO("ITEM-STACKABLE-" + System.currentTimeMillis(),modelName);
		
		List<ValueIdDTO> itemPlacementDTOList = placeRackableItem(location,cabinetId,uPosition);
		
		newItemDTOList.addAll(itemPlacementDTOList);
		
		replaceOrAdd(newItemDTOList,additionalParams);
		
		//Then save it
		Map<String, UiComponentDTO> itemDTOMap = itemHome.saveItem(new Long(-1), newItemDTOList, getTestAdminUser());
		
		return itemDTOMap;
	}
	
	@Override
	public Map<String, UiComponentDTO> createStandardItem(Long location,
			Long cabinetId, long uPosition, String modelName,
			List<ValueIdDTO> additionalParams) throws ClassNotFoundException,
			BusinessValidationException, Throwable {
		//Then create the stackable
		List<ValueIdDTO> newItemDTOList = createNewItemDTO("ITEM-STANDARD-" + System.currentTimeMillis(),modelName);
		
		List<ValueIdDTO> itemPlacementDTOList = placeRackableItem(location,cabinetId,uPosition);
		
		newItemDTOList.addAll(itemPlacementDTOList);
		
		replaceOrAdd(newItemDTOList,additionalParams);
		
		//Then save it
		Map<String, UiComponentDTO> itemDTOMap = itemHome.saveItem(new Long(-1), newItemDTOList, getTestAdminUser());
		
		return itemDTOMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#getItemDTO(long)
	 */
	@Override
	public Map<String, UiComponentDTO> getItemDTO(long itemId) throws Throwable {
		return (itemHome.getItem(itemId, getTestAdminUser()));
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#updateItem(java.util.List)
	 */
	@Override
	public Map<String, UiComponentDTO> updateItem(
			List<ValueIdDTO> valueIdDTOList)
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {
		Long itemId = new Long(-1);
		Map<String,UiComponentDTO> itemDTOResult = new HashMap<String, UiComponentDTO>();
		
		for (ValueIdDTO dto:valueIdDTOList){
			if (dto.getLabel().equals("tiName")){
				itemId = (Long) dto.getData();
				break;
			}
		}
		
		if (itemId > 0)
			itemDTOResult =  (itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser()));
		
		return itemDTOResult;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.UnitTestItemDAO#deleteItem(long)
	 */
	@Override
	public void deleteItem(long itemId) throws BusinessValidationException, Throwable {
		if (itemId > 0){
			itemHome.deleteItem(itemId, true, getTestAdminUser());
		}

	}
	
	@Override
	public List<ValueIdDTO> getItemUpdateValueIdDTOList(Map<String,UiComponentDTO> itemDTO) throws Throwable {
		List<ValueIdDTO> list = new ArrayList<ValueIdDTO>();
		
		for (Map.Entry<String, UiComponentDTO> entry: itemDTO.entrySet()){
			String label = entry.getKey();
			UiComponentDTO value = entry.getValue();
			Object id = value.getUiValueIdField().getValueId();
			Object val = value.getUiValueIdField().getValue();
			
			ValueIdDTO vDto = new ValueIdDTO();
			vDto.setLabel(label);
			
			if (id != null){
				vDto.setData(id);
			} else {
				vDto.setData(val);
			}
			
			list.add(vDto);
		}
		
		return list;
	}
	
	

	
	@Override
	public Map<String, UiComponentDTO> updateValue(String label,
			Map<String, UiComponentDTO> origDTO, Object value, boolean changeId) {
		if (changeId){
			origDTO.get(label).getUiValueIdField().setValueId(value);
		} else {
			origDTO.get(label).getUiValueIdField().setValueId(null);
			origDTO.get(label).getUiValueIdField().setValue(value);
		}
		return origDTO;
	}
	
	
	@Override
	public ValueIdDTO createValueIdDTOObj(String label, Object data) {
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel(label);
		dto.setData(data);
		return dto;
	}
	
	
	@Override
	public ModelDetails getModel(String modelName) {
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.add(Restrictions.eq("modelName", modelName));
		
		ModelDetails modelDetails = (ModelDetails) criteria.uniqueResult();
		
		return modelDetails;
	}
	
	
	
	// --------------- private methods --------------
	
	private DataCenterLocationDetails getTestLocation(String locationName){		
		Criteria criteria;
		Session session = sf.getCurrentSession();
		
		try{						
			criteria = session.createCriteria(DataCenterLocationDetails.class);
			criteria.add(Restrictions.eq("dcName", locationName) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List list = criteria.list();
			
			if(list != null && list.size() > 0)
			{
				return (DataCenterLocationDetails)list.get(0);
			}
			
		}catch(HibernateException e){
			
			 e.printStackTrace();
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			e.printStackTrace();
		}
		return null;
	}
	
	//This will take care of either replacing or adding modifiedList to the original
	private void replaceOrAdd(List<ValueIdDTO> originalList, List<ValueIdDTO> modifiedList){
		if (modifiedList != null){
			for (ValueIdDTO modified:modifiedList){
				boolean found = false;
				for (ValueIdDTO original:originalList){
					if (modified.getLabel().equals(original.getLabel())){
						original.setData(modified.getData());
						found = true;
					}
				}
				//If it cannot be replaced, add it.
				if (found == false){
					originalList.add(modified);
				}
			}
		}
	}
	
	private List<ValueIdDTO> createNewItemDTO(String tiName, String modelName) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		long lkuId = -1;
		long userId = -1;
		
		ModelDetails modelDetails = getModel(modelName);
		valueIdDTOList.add(createValueIdDTOObj("cmbMake", modelDetails.getModelMfrDetails().getModelMfrDetailId() )); 
		valueIdDTOList.add(createValueIdDTOObj("cmbModel", modelDetails.getModelDetailId()));
		valueIdDTOList.add(createValueIdDTOObj("tiSerialNumber", "SN-" + System.currentTimeMillis()));
		valueIdDTOList.add(createValueIdDTOObj("tiAssetTag", "A-" + System.currentTimeMillis()));
		valueIdDTOList.add(createValueIdDTOObj("tieAssetTag", "E-" + System.currentTimeMillis()));
		//Identity Panel Data
		valueIdDTOList.add(createValueIdDTOObj("tiName", tiName));
		valueIdDTOList.add(createValueIdDTOObj("tiAlias", "HELLO THERE!"));
		
		if ( (lkuId = getLkuId("TYPE", modelDetails.getClassLookup().getLkpValueCode())) > 0)
			valueIdDTOList.add(createValueIdDTOObj("cmbType", lkuId));
		
		if ( (lkuId = getLkuId("FUNCTION", modelDetails.getClassLookup().getLkpValueCode())) > 0)
			valueIdDTOList.add(createValueIdDTOObj("cmbFunction", lkuId));
		
		if ((userId = getUserId("admin")) > 0)
			valueIdDTOList.add(createValueIdDTOObj("cmbSystemAdmin", userId));
		
		if ( (lkuId = getLkuId("TEAM", modelDetails.getClassLookup().getLkpValueCode())) > 0)
			valueIdDTOList.add(createValueIdDTOObj("cmbSystemAdminTeam", lkuId));
		
		if ( (lkuId = getLkuId("DEPARTMENT", modelDetails.getClassLookup().getLkpValueCode())) > 0)
			valueIdDTOList.add(createValueIdDTOObj("cmbCustomer", lkuId));
		
		Session session = sf.getCurrentSession();
		LksData statusLks = SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED);
		if (statusLks != null)
			valueIdDTOList.add(createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
	
		
		return valueIdDTOList;
 	}
	
	private List<ValueIdDTO> placeRackableItem(long locationId, Long cabinetId, long uPosition){
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		// Placement Panel Data for rackables
		valueIdDTOList.add(createValueIdDTOObj("cmbLocation", locationId));
		
		
		long railsUsedId = -1;
//		if ((railsUsedId = getLkpValueCode(SystemLookup.LkpType.RAILS_USED)) > 0)
//			valueIdDTOList.add(createValueIdDTOObj("radioRailsUsed", railsUsedId));
		valueIdDTOList.add(createValueIdDTOObj("cmbCabinet", cabinetId));
		valueIdDTOList.add(createValueIdDTOObj("cmbUPosition", uPosition));
		
		long orientation = -1;
		if ((orientation = getLkpValueCode(SystemLookup.LkpType.ORIENTATION)) > 0)
			valueIdDTOList.add(createValueIdDTOObj("cmbOrientation", orientation));
		
		
		valueIdDTOList.add(createValueIdDTOObj("tiLocationRef", "loc:" + System.currentTimeMillis()));
		
		return valueIdDTOList;
	}
	
	private List<ValueIdDTO> placeCabinetItem(long locationId, String rowLabel, String positionInRow, String locationRef){
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		long lkpValueCode = -1;
		// Placement Panel data for cabinets
		valueIdDTOList.add(createValueIdDTOObj("cmbLocation", locationId));
		valueIdDTOList.add(createValueIdDTOObj("cmbRowLabel", rowLabel));
		valueIdDTOList.add(createValueIdDTOObj("cmbRowPosition", positionInRow));
//		if ((lkpValueCode = getLkpValueCode(SystemLookup.LkpType.FACING)) > 0)
//			valueIdDTOList.add(createValueIdDTOObj("radioFrontFaces", lkpValueCode));
		valueIdDTOList.add(createValueIdDTOObj("tiLocationRef", locationRef));
		return valueIdDTOList;
	}
	



	private long getLkuId( String type, String lkuValue){
		long lkuId = -1;
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(LkuData.class);
		criteria.add(Restrictions.eq("lkuTypeName", type));
		criteria.add(Restrictions.eq("lkuValue", lkuValue));
		
		LkuData lkuData = (LkuData) criteria.uniqueResult();
		
		if (lkuData != null){
			lkuId = lkuData.getLkuId();
		}
		
		return lkuId;
	}
	
	private long getLkuId(String type, Long classLkpValueCode){
		long lkuId = -1;
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(LkuData.class);
		criteria.createAlias("lksData", "lksData");
		criteria.add(Restrictions.eq("lkuTypeName", type));
		criteria.add(Restrictions.eq("lksData.lkpValueCode", classLkpValueCode));
		
		List list = criteria.list();
		if (list.size() > 0){
			LkuData lkuData = (LkuData) list.get(0);
			lkuId = lkuData.getLkuId();
		}
		
		return lkuId;
	}
	
	private long getUserId(String userName){
		long userId = -1;
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(Restrictions.eq("userId", userName));
		
		List list = criteria.list();
		
		if (list.size() > 0)
			userId = ((Users) list.get(0)).getId();
		
		return userId;
	}
	
	private long getLkpValueCode(String lkpType){
		long lkpValueCode = -1;
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(LksData.class);
		criteria.add(Restrictions.eq("lkpTypeName", lkpType));
		
		List list = criteria.list();
		
		if (list.size() > 0)
			lkpValueCode = ((LksData) list.get(0)).getLkpValueCode();
		
		return lkpValueCode;
	}
	
	private UserInfo getTestAdminUser() {
		UserInfo user = new UserInfo();
		user.setUserName("unitTestAdmin");
		user.setUserId("1"); //This field is the users.id, not a string field
		user.setAccessLevelId( Integer.toString( UserAccessLevel.ADMIN.getAccessLevel() ) );
		return user;
	}



	

}
