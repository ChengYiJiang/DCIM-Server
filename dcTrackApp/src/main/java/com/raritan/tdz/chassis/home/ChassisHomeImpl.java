package com.raritan.tdz.chassis.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelChassis;
import com.raritan.tdz.domain.ModelChassisSlot;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.home.MoveHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalUtils;

public class ChassisHomeImpl implements ChassisHome {
	private static Logger log = Logger.getLogger("ChassisHome");

	//<-- beans injected via constructor 
	private SessionFactory sessionFactory;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private MoveHome moveHome;
	
	//<-- beans injected via set function 
	private ResourceBundleMessageSource messageSource;

	//<-- static constants
	private static final long BLADE_FULL = 0L;
	private static final long BLADE_FULL_DOUBLE = 1L;
	private static final long BLADE_HALF = 2L;
	private static final long BLADE_HALF_DOUBLE = 3L;
	private static final long BLADE_QUARTER = 4L;
	private static final int DEFUALT_NUM_OF_BAYS_IN_CHASSIS = 1;
	private static final int DEFUALT_MAX_SLOTS_IN_CHASSIS = 64;

	private static final long UPDATE_ALL_MODEL_CHASSIS_LAYOUT_IDENTIFIER = -20120823;

	private static final String LN_EVT_CHASSIS_MODEL_CHANGE = "chassis";
	private static final String LN_EVT_BLADE_MODEL_CHANGE = "blade";
	
	private static final String EV_PARAM_LOCATION = "Location";
	private static final String EV_PARAM_CABINET = "Cabinet";
	private static final String EV_PARAM_CHASSIS = "Chassis";
	private static final String EV_PARAM_BLADE = "Blade";
	private static final String EV_PARAM_SLOT = "Was in Slot";
	private static final String EV_PARAM_ERR_CAUSE = "Cause";
	
	private static final String ERR_CAUSE_INVALID_SLOT_ASSIGNMENT = "Invalid Slot Assignment";
	private static final String ERR_CAUSE_SLOT_CANNOT_HOUSE_BLADE = "Slot Cannot House Blade";
	private static final String ERR_CAUSE_CONFLICT_IN_SLOT_POSITION = "Conflict in Slot Position";
	private static final String ERR_CAUSE_CHASSIS_FACE_DELETED = "Rear of the chassis deleted, cannot house blade in the rear";
	private static final String ERR_CAUSE_MODEL_NOT_CHASSIS = "Item is no more a Chassis";
	
	private static final boolean updateToDatabase = true;
	
	//<-- constructor
	public ChassisHomeImpl(SessionFactory sessionFactory, ResourceBundleMessageSource messageSource) {
		this.sessionFactory = sessionFactory;
		this.messageSource = messageSource;
	}

	//<-- convinence class
	public class customModelChassisComparator implements Comparator<ModelChassisSlot> {
		@Override
		public int compare(ModelChassisSlot object1, ModelChassisSlot object2) {
			return object1.getSlotNumber() - object2.getSlotNumber();
	    }
	}

	public class customBladeItemComparator implements Comparator<ItItem> {
		@Override
		public int compare(ItItem object1, ItItem object2) {
			return (int) (object1.getSlotPosition() - object2.getSlotPosition());
	    }
	}

	public class customValueIdDTOComparator implements Comparator<ValueIdDTO> {
		@Override
		public int compare(ValueIdDTO object1, ValueIdDTO object2) {
			return (object1.getLabel().compareTo(object2.getLabel()));
	    }
	}
	
	public class SlotEventDetails {
		public long chassisItemId;
		public String bladeItemName;
		public String errCause;
		public long slotNumberBefore;
	}
	
	public class SlotReAssignmentInfo {
		public String layout;    
		public List<SlotEventDetails> slotEventDetailsList = new ArrayList<SlotEventDetails>();
	}

	public class SlotReAssignmentInfoFrontBack {
		String modelName;	// Name of chassis or blade model that causes layout changes. 
		SlotReAssignmentInfo layoutFront;
		SlotReAssignmentInfo layoutRear;    
	}
	
	//<-- business logic
	
	//TODO: Remove functions that are duplicate in itemHomeImpl.java

	@SuppressWarnings("unchecked")
	@Override
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId) throws DataAccessException {
		Session session = null;
		Collection<ItItem> itemList = null;
		try{
			session = this.sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(ItItem.class);
			criteria.createCriteria("bladeChassis").add(Restrictions.eq("itemId", chassisItemId));
			criteria.addOrder(Order.asc("itemName"));
			@SuppressWarnings("rawtypes")
			List list = criteria.list();

			assert(null != list);
			itemList = list;
		} catch(HibernateException e) {

			throw new DataAccessException(new ExceptionContext("Exception in trying to fetch Blade items for Chassis id " + chassisItemId, this.getClass(), e));

		} catch(org.springframework.dao.DataAccessException e) {

			throw new DataAccessException(new ExceptionContext("Exception in trying to fetch Blade items  for Chassis id " + chassisItemId, this.getClass(), e));
		}

		return itemList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId, Long faceLksValueCode) throws DataAccessException {
		Session session = null;
		Collection<ItItem> itemList = null;
		try{
			session = this.sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(ItItem.class);
//			criteria.createAlias("mountedRailLookup", "mountedRailLookup");
			criteria.createAlias("facingLookup", "facingLookup");
			
			criteria.createCriteria("bladeChassis").add(Restrictions.eq("itemId", chassisItemId));
			criteria.add(Restrictions.eq("facingLookup.lkpValueCode", faceLksValueCode));
/*			if (faceLksValueCode == SystemLookup.ChassisFace.FRONT) {
				criteria.add(Restrictions.or(
						Restrictions.eq("mountedRailLookup.lkpValueCode", SystemLookup.RailsUsed.FRONT), 
						Restrictions.eq("mountedRailLookup.lkpValueCode", SystemLookup.RailsUsed.BOTH)
						));
			} else if (faceLksValueCode == SystemLookup.ChassisFace.REAR) {
				criteria.add(Restrictions.or(
						Restrictions.eq("mountedRailLookup.lkpValueCode", SystemLookup.RailsUsed.REAR), 
						Restrictions.eq("mountedRailLookup.lkpValueCode", SystemLookup.RailsUsed.BOTH)
						));
			}
*/			
			@SuppressWarnings("rawtypes")
			List list = criteria.list();

			assert(null != list);
			itemList = list;
		} catch(HibernateException e) {

			throw new DataAccessException(new ExceptionContext("Exception in trying to fetch Blade items for Chassis id " + chassisItemId, this.getClass(), e));

		} catch(org.springframework.dao.DataAccessException e) {

			throw new DataAccessException(new ExceptionContext("Exception in trying to fetch Blade items  for Chassis id " + chassisItemId, this.getClass(), e));
		}

		return itemList;
	}

	
	@Override
	public List<ModelChassis> getAllBladeChassis()  throws DataAccessException {
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria modelCriteria = session.createCriteria(ModelChassis.class);
	
			modelCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			
			@SuppressWarnings("unchecked")
			List<ModelChassis> modelList = modelCriteria.list();
	
			return modelList;
		}
		catch(HibernateException e) {
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		} catch(org.springframework.dao.DataAccessException e) {

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
	}

	@Override
	public ItItem getItItemDomainObject(final Long itItemId) {
		Session session = this.sessionFactory.getCurrentSession();
		return (ItItem)session.get(ItItem.class, itItemId);

	}

	private LksData getLksData(long lksId) {
		Session session = this.sessionFactory.getCurrentSession();
		return (LksData)session.get(LksData.class, lksId);
	}
	
	@Override
	public void updateBladeFacing(long bladeItemId) {
		Session session = this.sessionFactory.getCurrentSession();
		ItItem bladeItem = (ItItem)session.get(ItItem.class, bladeItemId);
		boolean update = false;
		if (null != bladeItem) { 
			if (null != bladeItem.getMountedRailLookup()) {
				if (null == bladeItem.getFacingLookup() || 
						(null != bladeItem.getFacingLookup() && 
							bladeItem.getFacingLookup().getLkpValueCode() != SystemLookup.ChassisFace.FRONT &&
							bladeItem.getFacingLookup().getLkpValueCode() != SystemLookup.ChassisFace.REAR)) {
					if (bladeItem.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.FRONT || 
							bladeItem.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.BOTH) {
						LksData facingLookup = getLksData(191L);
						bladeItem.setFacingLookup(facingLookup);
					}
					else if (bladeItem.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.REAR) {
						LksData facingLookup = getLksData(192L);
						bladeItem.setFacingLookup(facingLookup);
					}
					else {
						LksData facingLookup = getLksData(191L);
						bladeItem.setFacingLookup(facingLookup);
					}
					update = true;
				}
				else if (null != bladeItem.getFacingLookup()) {
					if (bladeItem.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.FRONT &&
							bladeItem.getMountedRailLookup().getLkpValueCode() != SystemLookup.RailsUsed.FRONT) {
						// get the lks data using hibernate and then set
						LksData facingLookup = getLksData(251L);
						bladeItem.setMountedRailLookup(facingLookup);
					}
					else if (bladeItem.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.REAR && 
							bladeItem.getMountedRailLookup().getLkpValueCode() != SystemLookup.RailsUsed.REAR) {
						LksData facingLookup = getLksData(252L);
						bladeItem.setMountedRailLookup(facingLookup);
					}
					else {
						LksData facingLookup = getLksData(251L);
						bladeItem.setMountedRailLookup(facingLookup);
					}
					update = true;
				}
			}
			else if (null != bladeItem.getFacingLookup()) {
				if (bladeItem.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.FRONT) {
					// get the lks data using hibernate and then set
					LksData facingLookup = getLksData(251L);
					bladeItem.setMountedRailLookup(facingLookup);
				}
				else if (bladeItem.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.REAR) {
					LksData facingLookup = getLksData(252L);
					bladeItem.setMountedRailLookup(facingLookup);
				}
				else {
					LksData facingLookup = getLksData(251L);
					bladeItem.setMountedRailLookup(facingLookup);
				}
				update = true;
			}
		}
		if (update) {
			session.update(bladeItem);
			session.flush();
		}
	}
	
	private void updateAllBladeFacing() throws DataAccessException {
		Collection<Item> blades = getAllBladeItem();
		for (Item blade: blades) {
			if (null != blade) {
				updateBladeFacing(blade.getItemId());
			}
		}
	}
	
	private void updateChassisGroupName(long chassisItemId, String groupName) {
		Session session = this.sessionFactory.getCurrentSession();
		ItItem chassisItem = (ItItem)session.get(ItItem.class, chassisItemId);
		if (chassisItem.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK) {
			chassisItem.setGroupingName(groupName);
			session.update(chassisItem);
		}
	}
	
	@Override
	public void updateBladeGroupName(long bladeItemId, String groupName) {
		Session session = this.sessionFactory.getCurrentSession();
		ItItem bladeItem = (ItItem)session.get(ItItem.class, bladeItemId);
		if (bladeItem.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK) {
			bladeItem.setGroupingName(groupName);
			session.update(bladeItem);
		}
	}

	@Override
	public void updateChassisGroupName(long chassisId) throws DataAccessException {
		Collection<ItItem> blades = getAllBladesForChassis(chassisId);
		Session session = this.sessionFactory.getCurrentSession();
		ItItem chassisItem = (ItItem)session.get(ItItem.class, chassisId);
		
		if(null == chassisItem) return;
		String groupName = chassisItem.getItemName();

		updateChassisGroupName(chassisId, groupName);
		
		for (ItItem blade: blades) {
			updateBladeGroupName(blade.getItemId(), groupName);
		}
	}
	
	@Override
	public void updateAllChassisGroupName() throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("model.formFactor", "Chassis"));
		@SuppressWarnings("unchecked")
		List<Item> list = criteria.list();
		Collection<Item> chassisList = list;
		for (Item chassis: chassisList) {
			updateChassisGroupName(chassis.getItemId());
		}
	}
	
	@Override
	public void updateChassisLayout(long chassisId) throws DataAccessException {
		try {
			if (chassisId == UPDATE_ALL_MODEL_CHASSIS_LAYOUT_IDENTIFIER) {
				/*
				 * NOTE: handle special case to update all chassis model layout information.
				 *       updateAllChassisLayout() must be called only once.
				 */
				updateAllBladeFacing();
				updateAllChassisLayout();
				updateAllChassisGroupName();
				return;
			}
			/* parse the blades and prepare layouts for front and rear of chassis 
			 * mounted_rails_pos_lks_id: 251 = FRONT, 252 = REAR, 253 = ASSUME FRONT */
			Collection<ItItem> blades = getAllBladesForChassis(chassisId);
	
			/* Prepared layout (front and rear) is applied to this chassis 
			 * update dct_items_it.layout_chassis_front, dct_items_it.layout_chassis_rear */
			Session session = this.sessionFactory.getCurrentSession();
			ItItem chassisItem = (ItItem)session.get(ItItem.class, chassisId);
			
			if(null == chassisItem) return;
			
			ModelChassis modelChassisFront = null, modelChassisRear = null;
			if (null != chassisItem) { 
				long model_id = chassisItem.getModel().getModelDetailId();
				/* get bays and max slots from chassis table and is_reserved from slots table */ 
				
				/*modelChassisFront = getBladeChassis(model_id, SystemLookup.ChassisFace.FRONT);
				modelChassisRear = getBladeChassis(model_id, SystemLookup.ChassisFace.REAR);*/
				modelChassisFront = getBladeChassis(chassisItem.getModel(), SystemLookup.ChassisFace.FRONT);
				modelChassisRear = getBladeChassis(chassisItem.getModel(), SystemLookup.ChassisFace.REAR);
			}
			String layoutChassisFront = computeChassisItemLayout(modelChassisFront, blades, SystemLookup.ChassisFace.FRONT);   
			String layoutChassisRear = computeChassisItemLayout(modelChassisRear, blades, SystemLookup.ChassisFace.REAR);
			
			if (null != layoutChassisFront && null != chassisItem) {
				chassisItem.setLayoutChassisFront(layoutChassisFront);
			}
			if (null != layoutChassisRear && null != chassisItem) { 
				chassisItem.setLayoutChassisRear(layoutChassisRear);
			}
			if (null != chassisItem) {
				session.update(chassisItem);
				session.flush();
			}
		} catch(HibernateException e) {
	
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		} catch(org.springframework.dao.DataAccessException e) {
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
	}
	
	@Override
	public Map<Long, String> getAvailableSlotPositions(Long chassisId, long bladeModelId, long faceLksValueCode, long bladeId) throws DataAccessException {
		ModelDetails model = getModelDetails(bladeModelId);
		if (null != model) {
			String bladeFormFactorStr = model.getFormFactor();
			long bladeClassLkpValueCode = model.getClassLookup().getLkpValueCode();
			return getAvailableSlotPositions(chassisId, bladeFormFactorStr, bladeClassLkpValueCode, faceLksValueCode, bladeId);
		}
		else {
			return new HashMap<Long, String>();
		}
	}
	
	private boolean isBladeTypeAllowed(ModelChassis modelChassis, ModelDetails model, long classLkpValueCode) {
		boolean bladeTypeAllowed = false;
		/*if (null != modelChassis && null != modelChassis.getSlotTypeLksData()) {
			bladeTypeAllowed = (modelChassis.getSlotTypeLksData().getLkpValueCode() == classLkpValueCode);
		}
		else*/ 
		if (null != model && null != model.getClassLookup()) {
			bladeTypeAllowed = (SystemLookup.Class.DEVICE == model.getClassLookup().getLkpValueCode() ||
					model.getClassLookup().getLkpValueCode() == classLkpValueCode);
		}
		return bladeTypeAllowed;
	}
	
	@Override
	public Map<Long, String> getAvailableSlotPositions(Long chassisId, String bladeFormFactorStr, long bladeClassLksValueCode, long faceLksValueCode, long bladeId) throws DataAccessException {
		long bladeFormFactor = getFormFactorId(bladeFormFactorStr);
		/* get the model id and chassis layout information 
		 * model id is required to get the information from the models table */
		ItItem itItem = getItItemDomainObject(chassisId);
		/* get the model id from the itItem */
		/* get information from models table i.e. dct_model, dct_model_chassis and dct_models_chassis_slots */
		ModelChassis modelChassis = null;
		if (null != itItem) { 
			// modelChassis = getBladeChassis(itItem.getModel().getModelDetailId(), faceLksValueCode);
			modelChassis = getBladeChassis(itItem.getModel(), faceLksValueCode);
		}
		else {
			return new HashMap<Long, String>();
		}

		/*  
		 * check for is_allow_spanning => false: full size blades only 
		 * check for is_allow_double => false: do not allow double wide blade */
		Map<Long, String> availableSlots = null;
		if (isChassisFaceAllowed(modelChassis, faceLksValueCode) && isBladeAllowed(modelChassis, bladeFormFactor) 
				/*&& isBladeTypeAllowed(modelChassis, itItem.getModel(), bladeClassLksValueCode)*/) {
			Integer bladeWidth = getBladeWidth(modelChassis, bladeFormFactor);
			Integer bladeHeight = getBladeHeight(modelChassis, bladeFormFactor);
			String layout = (faceLksValueCode == SystemLookup.ChassisFace.FRONT) ? itItem.getLayoutChassisFront() : itItem.getLayoutChassisRear();
			/* handle currentSlotNumber */
			if (null == layout) {
				updateChassisLayout(chassisId);
				itItem = getItItemDomainObject(chassisId);
				layout = (faceLksValueCode == SystemLookup.ChassisFace.FRONT) ? itItem.getLayoutChassisFront() : itItem.getLayoutChassisRear();
			}
			
			/* update the layout with the reserved slot information 
			 * These steps may not be required if the trigger cause the layout to be updated immediately */
			char[] updatedReservedLayout = layout.toCharArray();
			updateReservedSlots(modelChassis, updatedReservedLayout);
			layout = new String(updatedReservedLayout);
			
			List<Long> bladeIds = moveHome.getExceptionItemIds(bladeId);
			
			// if (bladeId > 0) {
			for (Long exceptionBladeId: bladeIds) {
				ItItem exceptionBladeItem = getItItemDomainObject(exceptionBladeId);
				boolean facingMatch = bladeFacingMatch(exceptionBladeItem, faceLksValueCode);
				if (null != exceptionBladeItem) {
					long currentSlotNumber = exceptionBladeItem.getSlotPosition();
					String exceptionBladeFormFactorStr = bladeFormFactorStr;
					if (null != exceptionBladeItem.getModel() && 
							null != exceptionBladeItem.getModel().getFormFactor() && 
							facingMatch) {
						exceptionBladeFormFactorStr = exceptionBladeItem.getModel().getFormFactor();
						
					}
					boolean chassisMatch = true;
					if (null != exceptionBladeItem.getBladeChassis()) {
						chassisMatch = (exceptionBladeItem.getBladeChassis().getItemId() == chassisId);
					}
					if (facingMatch && chassisMatch) {
						long exceptionBladeFormFactor = getFormFactorId(exceptionBladeFormFactorStr);
	
						ArrayList<ModelChassisSlot> slots = getSlotNumbers(modelChassis, exceptionBladeFormFactor, currentSlotNumber);
						char[] manipuilatedLayout = layout.toCharArray();
						if (null != modelChassis) {
							for (ModelChassisSlot slot: slots) {
								if (slot.getSlotNumber() > 0) {
									manipuilatedLayout[slot.getSlotNumber() - 1] = '0';
								}
			 
							}
							layout = new String(manipuilatedLayout);
						}
						else {
							/* Assume default chassis config 1x64 and only full size blades allowed */
							if (BLADE_FULL == bladeFormFactor && currentSlotNumber <= DEFUALT_MAX_SLOTS_IN_CHASSIS && currentSlotNumber > 0) { 
								manipuilatedLayout[(int)currentSlotNumber - 1] = '0';
								layout = new String(manipuilatedLayout);
							}
						}
					}
				}
			}
			availableSlots = getAvailableBladeSlots(itItem, modelChassis, layout, bladeWidth, bladeHeight, bladeFormFactor);	
		}
		else {
			availableSlots = new HashMap<Long, String>();
		}
		itItem = null;
		modelChassis = null;
    	return availableSlots;
    	// TODO:: Raise hibernate exception
	}

	@Override
	public ArrayList<ModelChassisSlot> getSlotNumbers(ModelChassis modelChassis, Item blade) {
		/* calculate slot numbers */
		long formFactor = getFormFactorId(blade.getModel().getFormFactor());
		Long slotNumber = blade.getSlotPosition();
		return getSlotNumbers(modelChassis, formFactor, slotNumber);
	}
	
	private List<ItItem> sortBladeItemUsingSlot(List<ItItem> unsortedBladeItem) {
		Collections.sort(unsortedBladeItem, new customBladeItemComparator());
		return unsortedBladeItem;
	}

	private void updateCabinetAndLocationForBladesInChassis(ItItem chassisItem, long bladeItemId) {
		Session updateSession = this.sessionFactory.getCurrentSession();
		ItItem bladeItem = (ItItem)updateSession.get(ItItem.class, bladeItemId);
		bladeItem.setDataCenterLocation(chassisItem.getDataCenterLocation());
		bladeItem.setParentItem(chassisItem.getParentItem());
		updateSession.update(bladeItem);
		//updateSession.flush();
	}
	
	@Override
	public void updateCabinetAndLocationForBladesInChassis(long chassisItemId) {
		Collection<ItItem> blades = null;
		Session session = this.sessionFactory.getCurrentSession();
		ItItem chassisItem = (ItItem)session.get(ItItem.class, chassisItemId);
		if (null == chassisItem) {
			return;
		}
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createCriteria("bladeChassis").add(Restrictions.eq("itemId", chassisItemId));
		@SuppressWarnings("unchecked")
		List<ItItem> list = criteria.list();
		blades = list;
		for (ItItem blade: blades) {
			updateCabinetAndLocationForBladesInChassis(chassisItem, blade.getItemId());
		}
	}
	
	@Override
	public SlotReAssignmentInfoFrontBack updateSlotNumberForBladesInChassis(long chassisId) throws DataAccessException {
		/* update the slot numbers for all the blades 
		   using chassis definition, blades form factor */
		/* parse the blades and prepare layouts for front and rear of chassis 
		   mounted_rails_pos_lks_id: 251 = FRONT, 252 = REAR, 253 = ASSUME FRONT */
		/* Prepared layout (front and rear) is applied to this chassis 
		 * update dct_items_it.layout_chassis_front, dct_items_it.layout_chassis_rear */
		Session session = this.sessionFactory.getCurrentSession();
		ItItem chassisItem = (ItItem)session.get(ItItem.class, chassisId);
		assert(null != chassisItem);
		
		ModelChassis modelChassisFront = null, modelChassisRear = null;
		boolean ischassisModel = true;
		if (null != chassisItem) { 
			long model_id = chassisItem.getModel().getModelDetailId();
			ischassisModel = isChassisModel(model_id);
			/* get bays and max slots from chassis table and is_reserved from slots table */ 
			/*modelChassisFront = getBladeChassis(model_id, SystemLookup.ChassisFace.FRONT);
			modelChassisRear = getBladeChassis(model_id, SystemLookup.ChassisFace.REAR);*/
			modelChassisFront = getBladeChassis(chassisItem.getModel(), SystemLookup.ChassisFace.FRONT);
			modelChassisRear = getBladeChassis(chassisItem.getModel(), SystemLookup.ChassisFace.REAR);
		}
		else {
			// TODO:: Raise exception for invalid chassis
		}
		
		SlotReAssignmentInfo layoutFront = updateSlotNumberForBlade(modelChassisFront, chassisId, SystemLookup.ChassisFace.FRONT, updateToDatabase, ischassisModel);   
		SlotReAssignmentInfo layoutRear = updateSlotNumberForBlade(modelChassisRear, chassisId, SystemLookup.ChassisFace.REAR, updateToDatabase, ischassisModel);
		chassisItem.setLayoutChassisFront(layoutFront.layout);
		chassisItem.setLayoutChassisRear(layoutRear.layout);
		session.update(chassisItem);
		session.flush();

		SlotReAssignmentInfoFrontBack layoutFrontBack = new SlotReAssignmentInfoFrontBack();
		layoutFrontBack.layoutFront = layoutFront;
		layoutFrontBack.layoutRear = layoutRear;
		return layoutFrontBack;
		// TODO:: Raise add hibernate exception
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Map sortByKey(Map map) {
	    List list = new LinkedList(map.entrySet());
	    Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getKey())
	              .compareTo(((Map.Entry) (o2)).getKey());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	
	@SuppressWarnings("rawtypes")
	private Long getFirstKey(Map map) {
	    @SuppressWarnings({ "unchecked" })
		final Set<Entry<Long, String>> mapValues = map.entrySet();
	    final int maplength = mapValues.size();
	    if (maplength > 0) {
		    @SuppressWarnings("unchecked")
			final Entry<Long,String>[] test = new Entry[maplength];
		    mapValues.toArray(test);
		    return test[0].getKey();
	    }
	    else {
	    	return 0L;
	    }
	}

	@SuppressWarnings("rawtypes")
	private Long getLastKey(Map map) {
	    @SuppressWarnings({ "unchecked" })
		final Set<Entry<Long, String>> mapValues = map.entrySet();
	    final int maplength = mapValues.size();
	    if (maplength > 0) {
		    @SuppressWarnings("unchecked")
			final Entry<Long,String>[] test = new Entry[maplength];
		    mapValues.toArray(test);
		    return test[maplength - 1].getKey();
	    }
	    else {
	    	return 0L;
	    }
	}

	@SuppressWarnings("rawtypes")
	private Long getLastButOneKey(Map map) {
	    @SuppressWarnings({ "unchecked" })
		final Set<Entry<Long, String>> mapValues = map.entrySet();
	    final int maplength = mapValues.size();
	    if (maplength - 1 > 0) {
		    @SuppressWarnings("unchecked")
			final Entry<Long,String>[] test = new Entry[maplength];
		    mapValues.toArray(test);
		    return test[maplength - 2].getKey();
	    }
	    else {
	    	return -1L;
	    }
	}
	
	private long getBladeFacing(ItItem blade) {
		long facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
		if (null != blade.getFacingLookup()) {
			facingLkpValueCode = blade.getFacingLookup().getLkpValueCode();
		}
		else {
			long bladeRailsUsedLkpValueCode = -1;
			if (null != blade.getMountedRailLookup()) {
				bladeRailsUsedLkpValueCode = blade.getMountedRailLookup().getLkpValueCode();
			}
			/* The facing information for the blade is not updated in the database. 
			 * If updated use the  blade.getFacingLookup().getLkpValueCode() */
			if (SystemLookup.RailsUsed.FRONT == bladeRailsUsedLkpValueCode || 
				SystemLookup.RailsUsed.BOTH == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.FRONT;
			}
			else if (SystemLookup.RailsUsed.REAR == bladeRailsUsedLkpValueCode) {
				facingLkpValueCode = SystemLookup.ChassisFace.REAR;
			}
		}
		
		return facingLkpValueCode;

	}

	@Override
	public Map<Long, String> getSortedSlotNumber(Long bladeId) throws DataAccessException {
		ItItem blade = (ItItem) itemDAO.loadItem(bladeId);
		
		ItItem chassis = (ItItem) itemDAO.loadItem(blade.getBladeChassis().getItemId());
		
		ModelChassis modelChassis = null;
		if (null != chassis) {
			modelChassis = getBladeChassis(chassis.getModel().getModelDetailId(), getBladeFacing(blade));
		}
		
		Map<Long, String> sortedSlotNumbers = getSortedSlotNumber(modelChassis, blade);
		
		return sortedSlotNumbers;
	
	}
	
	@Override
	public Map<Long, String> getSortedSlotNumber(ModelChassis modelChassis, ItItem blade) {
		ArrayList<ModelChassisSlot> slots = getSlotNumbers(modelChassis, blade);
		Map<Long, String> slotNumbers = new HashMap<Long, String>();
		for (ModelChassisSlot slot: slots) {
			if (null != slot) {
				String label = slot.getSlotLabel();
				if (GlobalUtils.isNumeric(slot.getSlotLabel())) {
					label = StringUtils.leftPad(slot.getSlotLabel(), 2, '0');
				}
				slotNumbers.put(Integer.valueOf(slot.getSlotNumber()).longValue(), label /*slot.getSlotLabel()*/);
			}
		}
		@SuppressWarnings("unchecked")
		Map<Long, String> sortedSlotNumbers = sortByKey(slotNumbers);
		return sortedSlotNumbers;
	}
	
	@Override
	public int getAnchorSlot(ModelChassis modelChassis, ItItem blade) {
		int anchorSlot = -9;
/*		ArrayList<ModelChassisSlot> slots = getSlotNumbers(modelChassis, blade);
		Map<Long, String> slotNumbers = new HashMap<Long, String>();
		for (ModelChassisSlot slot: slots) {
			if (null != slot) {
				slotNumbers.put(Integer.valueOf(slot.getSlotNumber()).longValue(), slot.getSlotLabel());
			}
		}
		@SuppressWarnings("unchecked")
		Map<Long, String> sortedSlotNumbers = sortByKey(slotNumbers);*/
		ArrayList<ModelChassisSlot> slots = getSlotNumbers(modelChassis, blade);
		Map<Long, String> sortedSlotNumbers = getSortedSlotNumber(modelChassis, blade);
		Long firstSlotNumber = getFirstKey(sortedSlotNumbers);
		Long lastSlotNumber = getLastKey(sortedSlotNumbers);
		if (blade.getModel().getFormFactor().contains("Double")) {
			Long lastButOneSlotNumber = getLastButOneKey(sortedSlotNumbers);
			if (-1 != lastButOneSlotNumber) {
				lastSlotNumber = lastButOneSlotNumber;
			}
		}
		for (ModelChassisSlot slot: slots) {
			if (null != slot) {
				if (slot.isAnchor()) {
					/*TODO:: there could be more than one anchor slots in the list, get the correct slot number
					it will be the lowest, if not then the highest number*/
					if (firstSlotNumber == slot.getSlotNumber()) {
						// dto.setAnchorSlot(slot.getSlotNumber());
						anchorSlot = slot.getSlotNumber();
						break;
					}
					else if (lastSlotNumber == slot.getSlotNumber()) {
						// dto.setAnchorSlot(slot.getSlotNumber());
						anchorSlot = slot.getSlotNumber();
						break;
					} 
				}
			}
		}
		return anchorSlot;
	}
	
	
	//CJ did here..
	public int getMaxSlotsForChassis(ModelChassis modelChassis, long chassisItemId){
		int bays = DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		int maxSlots = DEFUALT_MAX_SLOTS_IN_CHASSIS;
		
		if (null != modelChassis) {
			bays = modelChassis.getBays();
			maxSlots = modelChassis.getSlotMax();
		}
		return maxSlots;
	}
	
	
	private SlotReAssignmentInfo updateSlotNumberForBlade(ModelChassis modelChassis, long chassisItemId, long chassisFaceLksId, boolean updateToDatabase, boolean isChassisModel) throws DataAccessException {
		int bays = DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		int maxSlots = DEFUALT_MAX_SLOTS_IN_CHASSIS;
		if (null != modelChassis) {
			bays = modelChassis.getBays();
			maxSlots = modelChassis.getSlotMax();
		}
		int slots_per_bay =  maxSlots / bays;
		char[] layout = new char[bays * slots_per_bay];
		Arrays.fill(layout, '0');
		
		SlotReAssignmentInfo slotReAssignmentInfo = new SlotReAssignmentInfo();
		
		if (null != modelChassis) {
			/* update layout as 1 for reserved slots */
			updateReservedSlots(modelChassis, layout);
		}
	
		boolean moveRearBladesToFront = false;
		if (null == modelChassis && SystemLookup.ChassisFace.REAR == chassisFaceLksId) {
			moveRearBladesToFront = true;
		}
		
		Collection<ItItem> blades = null;
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createCriteria("bladeChassis").add(Restrictions.eq("itemId", chassisItemId));
		@SuppressWarnings("unchecked")
		List<ItItem> list = criteria.list();
		/* sort the blade using slot numbers */
		blades = sortBladeItemUsingSlot(list);
		for (ItItem blade: blades) {
			boolean update = false;
			if (!isChassisModel) {
				long slotNumberBefore = blade.getSlotPosition();
				// Item model is changed to be not a chassis. 
				// update the slot number and item facing
				blade.setSlotPosition(-9L);
				// LksData facingLksData = SystemLookup.getLksData(session, SystemLookup.ChassisFace.FRONT);
				blade.setFacingLookup(null);
				blade.setBladeChassis(null);
				update = true;
				
				// set eventData
				SlotEventDetails slotEventDetails =	setChassisSlotsReAssignmentEventData(
						chassisItemId, blade.getItemName(), ERR_CAUSE_MODEL_NOT_CHASSIS, slotNumberBefore);
				slotReAssignmentInfo.slotEventDetailsList.add(slotEventDetails);
			}
			else {
				if (!bladeFacingMatch(blade, chassisFaceLksId)) {
					continue;
				}
	
				if (moveRearBladesToFront) {
					long slotNumberBefore = blade.getSlotPosition();
					// Chassis rear is been deleted, move all the blades in the rear to the front. 
					// update the slot number and item facing
					blade.setSlotPosition(-9L);
					LksData facingLksData = SystemLookup.getLksData(session, SystemLookup.ChassisFace.FRONT);
					update = true;
					blade.setFacingLookup(facingLksData);
					
					// set eventData
					SlotEventDetails slotEventDetails =	setChassisSlotsReAssignmentEventData(
							chassisItemId, blade.getItemName(), ERR_CAUSE_CHASSIS_FACE_DELETED, slotNumberBefore);
					slotReAssignmentInfo.slotEventDetailsList.add(slotEventDetails);
				}
				else {
					Long slotNumber = blade.getSlotPosition();
					if (slotNumber < 1) {
						continue;
					}
					
					
					if (slotNumber > maxSlots) {
						// Error condition: if the slot assigned cannot house the blade because the slot is not defined for the chassis model
						// get the previous slot # before resetting
						long slotNumberBefore = blade.getSlotPosition();
						blade.setSlotPosition(-9L);
						/* conflicting slot position */
						update = true;
		
						// set eventData
						SlotEventDetails slotEventDetails =	setChassisSlotsReAssignmentEventData(
								chassisItemId, blade.getItemName(), ERR_CAUSE_INVALID_SLOT_ASSIGNMENT, slotNumberBefore);
						slotReAssignmentInfo.slotEventDetailsList.add(slotEventDetails);
					}
					else {
						/* check if this blade can fit in this chassis.
						 * NO: 
						 * 		set slot number as NULL and update exception count
						 * YES: 
						 * 		continue to check. 
						 * blade will take slots already taken?
						 * YES: 
						 * 		set slot number as NULL and update exception count 
						 * NO:
						 * 		update the slot number */
						long formFactor = getFormFactorId(blade.getModel().getFormFactor());
						int bladeHeight = getBladeHeight(modelChassis, formFactor);
						if (false == isBladeAllowed(modelChassis, formFactor)) {
							// get the previous slot # before resetting
							long slotNumberBefore = blade.getSlotPosition();
							blade.setSlotPosition(-9L);
							/* Blade size not permitted */ 
							update = true;
							
							// set eventData
							SlotEventDetails slotEventDetails =	setChassisSlotsReAssignmentEventData(
									chassisItemId, blade.getItemName(), ERR_CAUSE_SLOT_CANNOT_HOUSE_BLADE, slotNumberBefore);
							slotReAssignmentInfo.slotEventDetailsList.add(slotEventDetails);
						}
						else {
							/* get slots this blade may occupy */
							ArrayList<ModelChassisSlot> slotPositions = getSlotNumbers(modelChassis, blade);
							boolean bladeFit = true;
							for (ModelChassisSlot slotPosition: slotPositions) {
								if (layout[slotPosition.getSlotNumber() - 1] == '1') {
									bladeFit = false;
									break;
								}
							}
							/* check for anchor slots */
							if (bladeFit && slotPositions.size() > 0) {
								int anchorSlot = getAnchorSlot(modelChassis, blade);
								for (ModelChassisSlot slotPosition: slotPositions) {
									layout[slotPosition.getSlotNumber() - 1] = '1';
									if ((1 == bladeHeight) || slotPosition.isAnchor()) {
										if (slotNumber > slotPosition.getSlotNumber()) {
											// blade.setSlotPosition(slotPosition.getSlotNumber());
											update = true;
										}
									}
								}
								if (update) {
									blade.setSlotPosition(anchorSlot);
								}
							}
							else {
								// get the previous slot # before resetting
								long slotNumberBefore = blade.getSlotPosition();
								blade.setSlotPosition(-9L);
								/* conflicting slot position */
								update = true;
								
								// setEventData
								SlotEventDetails slotEventDetails =	setChassisSlotsReAssignmentEventData(
										chassisItemId, blade.getItemName(), ERR_CAUSE_CONFLICT_IN_SLOT_POSITION, slotNumberBefore);
								slotReAssignmentInfo.slotEventDetailsList.add(slotEventDetails);
							}
							slotPositions = null;
						}
					}
				}
			}
			if (update && updateToDatabase) {
				Session updateSession = this.sessionFactory.getCurrentSession();
				ItItem bladeItem = (ItItem)updateSession.get(ItItem.class, blade.getItemId());
				bladeItem.setSlotPosition(blade.getSlotPosition());
				if (moveRearBladesToFront) {
					bladeItem.setFacingLookup(blade.getFacingLookup());
				}
				updateSession.update(bladeItem);
				updateSession.flush();
			}
		}
		if (!isChassisModel) {
			slotReAssignmentInfo.layout = new String();
		}
		else {
			slotReAssignmentInfo.layout = new String(layout);
		}
		return slotReAssignmentInfo;
	}

	private List<SlotReAssignmentInfoFrontBack> updateChassisDefinition(long chassisModelId) throws DataAccessException {
		/*
		 * parse through all the chassis that uses this model and update the 
		 * slot numbers of all blades in this chassis and then update the chassis
		 * layout front and rear
		 */
		SlotReAssignmentInfoFrontBack info = null;
		List<SlotReAssignmentInfoFrontBack> result = new ArrayList<SlotReAssignmentInfoFrontBack>();
		Session session = this.sessionFactory.getCurrentSession();
		ModelChassis chassisModel = (ModelChassis)session.get(ModelChassis.class, chassisModelId);
		if (null != chassisModel) {
			Collection<Item> chassisList = null;
			session = this.sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Item.class);
			criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("model.modelDetailId", chassisModel.getModel().getModelDetailId()));
			@SuppressWarnings("unchecked")
			List<Item> list = criteria.list();
			chassisList = list;
			for (Item chassis: chassisList) {
				info = updateSlotNumberForBladesInChassis(chassis.getItemId());
				result.add(info);
			}
		}
		return result;
	}
	
	@Override
	public void updateAllChassisLayout() throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("model.formFactor", "Chassis"));
		@SuppressWarnings("unchecked")
		List<Item> list = criteria.list();
		Collection<Item> chassisList = list;
		for (Item chassis: chassisList) {
			updateChassisLayout(chassis.getItemId());
		}
	}

	@SuppressWarnings("unchecked")
	private List<Long> excludeWhenMovedChassis() {
		Session session = this.sessionFactory.getCurrentSession();
		
		Criteria movedChassisCriteria = session.createCriteria(PowerPortMove.class);
		movedChassisCriteria.createAlias("moveItem", "moveItem");
		movedChassisCriteria.setProjection((Projections.projectionList()
				.add(Projections.property("moveItem.itemId"))));
		
		return movedChassisCriteria.list();
	}
	
	@Override
	public Collection<ItItem> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		criteria.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
    	
    	
/*		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
 		Criterion parentCond = Restrictions.eq("parentItem.itemId", cabinetItemId);
		Criterion classCond = Restrictions.eq("classLookup.lkpValueCode", bladeTypeLkpValueCode);
		Criterion formFactorCond = Restrictions.eq("model.formFactor", "Chassis");
		Criterion modelChassisTypeCond = Restrictions.in("model.modelDetailId", getModelChassisModelIdForClass(bladeTypeLkpValueCode));
		criteria.add(Restrictions.or(Restrictions.and(Restrictions.and(classCond, parentCond), formFactorCond), modelChassisTypeCond));*/

		criteria.add(Restrictions.eq("parentItem.itemId", cabinetItemId));
		criteria.add(Restrictions.eq("model.formFactor", "Chassis"));
		if (SystemLookup.Class.DEVICE == bladeTypeLkpValueCode) {
			criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("classLookup.lkpValueCode", bladeTypeLkpValueCode));
		}

		criteria.add(Restrictions.and(Restrictions.isNotNull("statusLookup"),
                Restrictions.ne("statusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED)
                ));

		List<Long> whenMovedItems = excludeWhenMovedChassis();
		if (null != whenMovedItems && whenMovedItems.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("itemId", whenMovedItems)));
		}
		
		criteria.addOrder(Order.asc("itemName"));
		@SuppressWarnings("unchecked")
		List<ItItem> list = criteria.list();
		Collection<ItItem> chassisList = list;
		return chassisList;
	}

	@Override
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeModelId, long bladeId) throws DataAccessException { 
		List<ValueIdDTO> availableChassis = new ArrayList<ValueIdDTO>();
		
		ModelDetails model =  getModelDetails(bladeModelId);
		if (null == model || !model.getMounting().equals("Blade")) {
			return availableChassis;
		}
		String modelFormFactor = model.getFormFactor();
		long bladeClassLkpValueCode = model.getClassLookup().getLkpValueCode();
		ItItem exceptionBladeItem = getItItemDomainObject(new Long(bladeId));
		long exceptionBladeChassisItemId = -1;
		if (null != exceptionBladeItem && null != exceptionBladeItem.getBladeChassis()) {
			exceptionBladeChassisItemId = exceptionBladeItem.getBladeChassis().getItemId();
		}
		Collection<ItItem> chassisList = getAllChassisInCabinet(cabinetItemId, model.getClassLookup().getLkpValueCode());
		List<Long> chassisItemIdList = new ArrayList<Long>();
		for (ItItem chassis: chassisList) {
			long exceptionBladeId = -1;
			long chassisItemId = chassis.getItemId();
			String chassisItemName = chassis.getItemName();
			
			if (exceptionBladeChassisItemId == chassisItemId) {
				exceptionBladeId = exceptionBladeItem.getItemId();
			}
			if (chassisItemIdList.contains(chassisItemId) || chassisItemId == bladeId) {
				continue;
			}
			boolean bladeAllowedInChassisRear = isBladeAllowedinChassisRear(chassisItemId, bladeModelId, bladeId);
			boolean bladeAllowedInChassisFront = isBladeAllowedinChassisFront(chassisItemId, bladeModelId, bladeId);
			Map<Long, String> slotsFront = getAvailableSlotPositions(chassisItemId, modelFormFactor, bladeClassLkpValueCode, SystemLookup.ChassisFace.FRONT, exceptionBladeId);
			Map<Long, String> slotsRear = getAvailableSlotPositions(chassisItemId, modelFormFactor, bladeClassLkpValueCode, SystemLookup.ChassisFace.REAR, exceptionBladeId);
			/* if anytime the requirement changes to "if chassis have no slots for a given blade, then do not list the chassis"
			 if ((null != slotsFront && !slotsFront.isEmpty()) || 
				(null != slotsRear && !slotsRear.isEmpty())) {
				ValueIdDTO dto = new ValueIdDTO();
				dto.setData(chassisItemId);
				dto.setLabel(chassisItemName);
				availableChassis.add(dto);
				chassisItemIdList.add(chassisItemId);
			}*/
			/* if the blade is allowed in a chassis if the definition allows it, then use this code */
			boolean noSlotsAvailableInFront = (null == slotsFront) || (slotsFront.isEmpty()); 
			boolean noSlotsAvailableInRear = (null == slotsRear) || (slotsRear.isEmpty());
			if (bladeAllowedInChassisRear || bladeAllowedInChassisFront) {
				ValueIdDTO dto = new ValueIdDTO();
				dto.setData(chassisItemId);
				if (noSlotsAvailableInFront && noSlotsAvailableInRear) {
					chassisItemName += " (No Slots)";
				}
				dto.setLabel(chassisItemName);
				availableChassis.add(dto);
				chassisItemIdList.add(chassisItemId);
			}
		}
		return availableChassis;
	}
	
	@Override
	public ChassisItemDTO getChassisInfo(long chassisItemId, long faceLksValueCode)  throws DataAccessException {
		try {
			Item item = getItemInfo(chassisItemId);
			ChassisItemDTO dto = null;
			if (null != item && null != item.getModel()) {
				// ModelChassis chassisModel = getBladeChassis(item.getModel().getModelDetailId(), faceLksValueCode);
				ModelChassis chassisModel = getBladeChassis(item.getModel(), faceLksValueCode);
				if (null != chassisModel) {
					dto = new ChassisItemDTO(item.getItemId(), item.getItemName(), 
											chassisModel.getBays(), chassisModel.getSlotMax(), 
											chassisModel.isAllowDouble(), chassisModel.isAllowSpanning(),
                                            chassisModel.isSlotVertical());
				}
				// Chassis default is only for the Chassis Front
				else if (SystemLookup.ChassisFace.FRONT == faceLksValueCode) {
					dto = new ChassisItemDTO(item.getItemId(), item.getItemName(), 
							1, 64, false, false, false);
				}
				else {
					return dto;
				}

                // fill other info
                if (null != item.getModel()) {
                    dto.setModelId(item.getModel().getModelDetailId());
                    dto.setModelName(item.getModel().getModelName());
                    dto.setMounting(item.getModel().getMounting());
                }
                if (null != item.getClassLookup()) {
                    dto.setItemClass(item.getClassLookup().getLkpValueCode());
                    dto.setItemClassName(item.getClassLookup().getLkpValue());
                }
                if (null != item.getSubclassLookup()) {
                    dto.setItemSubClass(item.getSubclassLookup().getLkpValueCode());
                    dto.setItemSubClassName(item.getSubclassLookup().getLkpValue());
                }
			}
			return dto;
		}
		catch(Throwable t){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
		}
	}
	
	@Override
	public List<ChassisSlotDTO> getChassisSlotDetails(Long chassisId, Long faceLksValueCode) throws DataAccessException, BusinessValidationException {
		List<ChassisSlotDTO> slotDtos = new ArrayList<ChassisSlotDTO>();
		
		ModelChassis modelChassis = getModelChassisForChassisIdandFacing (chassisId, faceLksValueCode );
		
		// get all baldes on chassis with chassisId and facing in faceLksValueCode 
		Collection<ItItem> blades = getAllBladesForChassis(chassisId, faceLksValueCode);

		// get slots sorted by slot number
		ArrayList<ModelChassisSlot> sortedSlots = null;
		if (null != modelChassis) {
			sortedSlots = sortChassisSlot(modelChassis.getModelChassisSlot());
		}

		if (!isChassisFaceAllowed(modelChassis, faceLksValueCode)) {
			return slotDtos;
		}
		// Not all of the chassis has model chassis	added by brian.wang
		if (null == modelChassis || null == sortedSlots) {
			slotDtos = getDefaultSlotDetails(slotDtos);
		}
		else {
			// initialize slotDto for all slots above
			for (ModelChassisSlot slot: sortedSlots) {
				ChassisSlotDTO slotDto = new ChassisSlotDTO();
				slotDto.setId(slot.getModelsChassisSlotId());
				slotDto.setNumber(slot.getSlotNumber());
				slotDto.setLabel(slot.getSlotLabel());
				slotDto.setIsAnchorSlot(slot.isAnchor());
				slotDto.setIsReservedSlot(slot.isReserved());
				// no blade id here
				// no blade name here
				slotDtos.add(slotDto);
			}
		}
		
		// for slots that have blade update blade id and name
		for (ItItem blade: blades) {
			ArrayList<ModelChassisSlot> bladeSlots = getSlotNumbers(modelChassis, blade);
				// set what slots are occupied by this blade
			for (ModelChassisSlot slot: bladeSlots) {
				int dtoIdx = slot.getSlotNumber() - 1;
				
				if(dtoIdx < 0 || dtoIdx >= slotDtos.size()) continue;
				
				slotDtos.get(dtoIdx).setBladeId(blade.getItemId());
				slotDtos.get(dtoIdx).setBladeName(blade.getItemName());
				log.debug("Slot number with Blade = " + slot.getSlotNumber());
			}
		}		
		
		return slotDtos;
	}

	private Collection<Item> getAllBladeItem() throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("model.mounting", "Blade"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("unchecked")
		List<Item> list = criteria.list();
		return list;
	}
	
	@Override
	public Collection<ItItem> getAllBladesForItem(long itemId) throws DataAccessException {
		Collection<ItItem> items = null;
 		long chassisItemId = itemId;
		Session session = this.sessionFactory.getCurrentSession();
		ItItem item = (ItItem)session.get(ItItem.class, itemId);
		if (item != null && item.getModel() != null && item.getModel().getFormFactor().equals("Chassis")) {
			items = getAllBladesForChassis(chassisItemId);
		}
		else if (item != null && item.getModel() != null && item.getModel().getMounting().equals("Blade")) {
			if (null != item.getBladeChassis()) {
				items = getAllBladesForChassis(item.getBladeChassis().getItemId());
			}
			else {
				items = new ArrayList<ItItem>();
			}
		}
		else {
			items = new ArrayList<ItItem>();
		}
		return items;
	}
	
	@Override
	public List<ValueIdDTO> getAllAvailableCabinetForBladeModel(long locationId, long bladeModelId, long bladeId) throws DataAccessException {
		List<ValueIdDTO> availableCabinets = new ArrayList<ValueIdDTO>();
		
		ModelDetails model =  getModelDetails(bladeModelId);
		if (null == model || !model.getMounting().equals("Blade")) {
			return availableCabinets;
		}
		String modelFormFactor = model.getFormFactor();
		long bladeClassLkpValueCode = model.getClassLookup().getLkpValueCode();
		ItItem exceptionBladeItem = getItItemDomainObject(bladeId);
		long exceptionBladeChassisItemId = -1;
		if (null != exceptionBladeItem && null != exceptionBladeItem.getBladeChassis()) {
			exceptionBladeChassisItemId = exceptionBladeItem.getBladeChassis().getItemId();
		}
		
		List<Object[]> chassisList =  getAllChassis(locationId, model.getClassLookup().getLkpValueCode());
		List<Long> cabinetItemIdList = new ArrayList<Long>();
		for (Object[] chassis: chassisList) {
			long exceptionBladeId = -1;
			if (null == chassis[0] || null == chassis[1] || null == chassis[2]) {
				continue;
			}
			long chassisItemId = (Long)chassis[0];
			long parentItemId = (Long)chassis[1];
			String parentItemName = (String)chassis[2];
			
			if (exceptionBladeChassisItemId == chassisItemId) {
				exceptionBladeId = exceptionBladeItem.getItemId();
			}
			if (cabinetItemIdList.contains(parentItemId) || chassisItemId == bladeId) {
				continue;
			}
			boolean bladeAllowedInChassisRear = isBladeAllowedinChassisRear(chassisItemId, bladeModelId, bladeId);
			boolean bladeAllowedInChassisFront = isBladeAllowedinChassisFront(chassisItemId, bladeModelId, bladeId);
			/* -- uncomment this code if the requirement changes to "display all cabinets that have chassis with AVAILABLE SLOTS for a given blade"
			Map<Long, String> slotsFront = getAvailableSlotPositions(chassisItemId, modelFormFactor, bladeClassLkpValueCode, SystemLookup.ChassisFace.FRONT, exceptionBladeId);
			Map<Long, String> slotsRear = getAvailableSlotPositions(chassisItemId, modelFormFactor, bladeClassLkpValueCode, SystemLookup.ChassisFace.REAR, exceptionBladeId);
			if ((null != slotsFront && !slotsFront.isEmpty()) || 
				(null != slotsRear && !slotsRear.isEmpty())) {
				ValueIdDTO dto = new ValueIdDTO();
				dto.setData(parentItemId);
				dto.setLabel(parentItemName);
				availableCabinets.add(dto);
				cabinetItemIdList.add(parentItemId);
			}*/
			if (bladeAllowedInChassisRear || bladeAllowedInChassisFront) {
				ValueIdDTO dto = new ValueIdDTO();
				dto.setData(parentItemId);
				dto.setLabel(parentItemName);
				availableCabinets.add(dto);
				cabinetItemIdList.add(parentItemId);
			}
		}
		// Sort the cabinet list
		Collections.sort(availableCabinets, new customValueIdDTOComparator());
		return availableCabinets;
	}

	//<-- private functions
	
	@SuppressWarnings("unchecked")
	private List<Long> getModelChassisModelIdForClass(long classLkpValueCode) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelChassis.class);
		criteria.createAlias("slotTypeLksData", "slotTypeLksData", Criteria.LEFT_JOIN);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("model.modelDetailId"))));
		criteria.add(Restrictions.eq("slotTypeLksData.lkpValueCode", classLkpValueCode));
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	private List<Object[]> getAllChassis(long locationId, long bladeTypeLkpValueCode) { 
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("dataCenterLocation","dataCenterLocation", Criteria.LEFT_JOIN);
		criteria.createAlias("parentItem.statusLookup", "parentStatusLookup", Criteria.LEFT_JOIN);

		/*
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		Criterion classCond = Restrictions.eq("classLookup.lkpValueCode", bladeTypeLkpValueCode);
		Criterion locationCond = Restrictions.eq("dataCenterLocation.dataCenterLocationId", locationId);
		Criterion formFactorCond = Restrictions.eq("model.formFactor", "Chassis");
		Criterion modelChassisTypeCond = Restrictions.in("model.modelDetailId", getModelChassisModelIdForClass(bladeTypeLkpValueCode));
		criteria.add(Restrictions.or(Restrictions.and(Restrictions.and(classCond, locationCond), formFactorCond), modelChassisTypeCond));
		*/
		
		if (SystemLookup.Class.DEVICE == bladeTypeLkpValueCode) {
			criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("classLookup.lkpValueCode", bladeTypeLkpValueCode));
		}
		
		List<Long> whenMovedItems = excludeWhenMovedChassis();
		if (null != whenMovedItems && whenMovedItems.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("itemId", whenMovedItems)));
		}

		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("itemId"))
				.add(Projections.property("parentItem.itemId"))
				.add(Projections.property("parentItem.itemName"))));

    	criteria.add(Restrictions.eq("dataCenterLocation.dataCenterLocationId", locationId));
		criteria.add(Restrictions.eq("model.formFactor", "Chassis"));
		
		criteria.add(Restrictions.and(Restrictions.isNotNull("parentItem.statusLookup"),     
                Restrictions.ne("parentStatusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED)
                ));
		
		criteria.addOrder(Order.asc("itemName"));
		
		return criteria.list();
	}

	private ModelChassis getModelChassisForChassisIdandFacing (Long chassisId, Long faceLksValueCode) throws DataAccessException, BusinessValidationException {
		// get chassis item;
		ItItem chassis = getItItemDomainObject(chassisId);
		if (chassis == null || chassis.getModel() == null) {
			// throwBusinessValidationException("BladesValidation.invalidChassis", null, null);
			return null;
		}
		
		// get the chassis detail based on facing (SystemLookup.ChassisFace.FRONT/REAR).
		// return getBladeChassis(chassis.getModel().getModelDetailId(), faceLksValueCode);
		return getBladeChassis(chassis.getModel(), faceLksValueCode);
	}
	
	private List<ChassisSlotDTO> getDefaultSlotDetails(List<ChassisSlotDTO> slotDtos) {
		for (Integer slot = 1; slot <= DEFUALT_MAX_SLOTS_IN_CHASSIS; slot++) {
			ChassisSlotDTO slotDto = new ChassisSlotDTO();
			slotDto.setId(slot.longValue());
			slotDto.setNumber(slot);
			slotDto.setLabel(slot.toString());
			slotDto.setIsAnchorSlot(false);
			slotDto.setIsReservedSlot(false);
			// no blade id here
			// no blade name here
			slotDtos.add(slotDto);
		}
		return slotDtos;
	}

	private void throwBusinessValidationException(String code, Object[] args, Locale locale ) throws BusinessValidationException {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		String msg = messageSource.getMessage(code, args, locale);
		e.addValidationError( msg );
		e.addValidationError( code, msg);
		throw e;
	}

	@Override
	public long getBladeFaceLookUpLks(ItItem blade) {
		if (null != blade.getFacingLookup()) {
			return blade.getFacingLookup().getLkpValueCode();
		}
		else if ( null != blade.getMountedRailLookup()) {
			if  (blade.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.FRONT || 
					blade.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.BOTH) { 
				return SystemLookup.ChassisFace.FRONT;
			}
			if (blade.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.REAR) { 
				return SystemLookup.ChassisFace.REAR;
			}
		}
		return -1;
	}
	
	private boolean bladeFacingMatch(ItItem blade, long chassisFaceLksId) {
		if (null != blade.getFacingLookup()) { 
			if (chassisFaceLksId == blade.getFacingLookup().getLkpValueCode()) {
				return true;
			}
		}
		else if ( null != blade.getMountedRailLookup()) {
			if  ((blade.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.FRONT || 
					blade.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.BOTH) && 
					chassisFaceLksId == SystemLookup.ChassisFace.FRONT) {
				return true;
			}
			if (blade.getMountedRailLookup().getLkpValueCode() == SystemLookup.RailsUsed.REAR && 
					chassisFaceLksId == SystemLookup.ChassisFace.REAR) {
				return true;
			}
		}
		return false;
	}

	private boolean isChassisFaceAllowed(ModelChassis modelChassis,
			long faceLksValueCode) {
		
		// Network Chassis now have a REAR slots
		/*if ( modelChassis != null && null != modelChassis.getModel() && null != modelChassis.getModel().getClassLookup() &&
			modelChassis.getModel().getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK &&
			faceLksValueCode == SystemLookup.ChassisFace.REAR) {
			return false;
		}
		else*/
		// Default Chassis do not have slots in REAR 
		if ( null == modelChassis && faceLksValueCode == SystemLookup.ChassisFace.REAR) {
			return false;
		}
		return true;
	}
	
	private ModelChassisSlot getDefaultModelChassisSlot(Integer slotNumber) {
		ModelChassisSlot modelChassisSlot = new ModelChassisSlot();
		modelChassisSlot.setAnchor(false);
		modelChassisSlot.setReserved(false);
		modelChassisSlot.setSlotLabel(slotNumber.toString());
		modelChassisSlot.setSlotNumber(slotNumber);
		return modelChassisSlot;
	}
	
	private ModelChassisSlot getModelChassisSlot(ModelChassis modelChassis, int slotNumber) {
		if (null != modelChassis) {
			for (ModelChassisSlot slot: modelChassis.getModelChassisSlot()) {
				if (slot.getSlotNumber() == slotNumber) {
					return slot;
				}
			}
		}
		else {
			return getDefaultModelChassisSlot(slotNumber);
		}
		return null;
	}
	
	private ArrayList<ModelChassisSlot> getSlotNumbers(ModelChassis modelChassis, long formFactor, Long slotNumber) {
		if (false == isBladeAllowed(modelChassis, formFactor)) {
			ArrayList<ModelChassisSlot> slots = new ArrayList<ModelChassisSlot>();
			ModelChassisSlot slot = new ModelChassisSlot();
			slot.setSlotLabel("Imcompatible Blade Form Factor");
			slot.setSlotNumber(slotNumber.intValue());
			slot.setModelChassis(modelChassis);
			slot.setModelsChassisSlotId(0L);
			slots.add(slot);
			return slots;
		}
		
		int bladeWidth = getBladeWidth(modelChassis, formFactor);
		int bladeHeight = getBladeHeight(modelChassis, formFactor);
		int bays = DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		int maxSlots = DEFUALT_MAX_SLOTS_IN_CHASSIS;
		if (null != modelChassis) {
			bays = modelChassis.getBays();
			maxSlots = modelChassis.getSlotMax();
		}
		if (slotNumber > maxSlots || slotNumber <= 0 /*|| null == modelChassis*/) {
			ArrayList<ModelChassisSlot> slots = new ArrayList<ModelChassisSlot>();
			return slots;
		}
		int slots_per_bay =  maxSlots / bays;
		int slotIdx = ((slotNumber.intValue()  - 1) % slots_per_bay); 
		int bayIdx = (slotNumber.intValue() - 1) / slots_per_bay;
		int bayIncr = ((bayIdx % 2) == 0) ? 1 : -1;
		ArrayList<ModelChassisSlot> slots = new ArrayList<ModelChassisSlot>(bladeWidth * bladeHeight);
		int slots_checked = 0;
		int slots_to_be_checked = bladeWidth * bladeHeight;
		boolean skipAnchorCheck = (bladeHeight == 1) /*|| (bays == bladeHeight) */ || (null == modelChassis);
		
		for (int slot = slotIdx; slot < slotIdx + bladeWidth && slot < slots_per_bay; slot++) {
			for (int bay = bayIdx; ((bayIncr == -1) ? (bay > bayIdx - bladeHeight) : (bay < bayIdx + bladeHeight)) && 
					bay < bays && bay >= 0; 
					bay += bayIncr) {
				// Add the model chassis slot of the default item
				slots.add(getModelChassisSlot(modelChassis, bay * slots_per_bay + slot + 1));
				slots_checked++;
			}
		}

		if (slots_to_be_checked == slots_checked) {
			Integer slotPosition = bayIdx * slots_per_bay + slotIdx; //slotIdx;
			Integer otherEndSlotPosition = (bayIncr == 1) ? slotIdx + ( (bladeHeight - 1) + bayIdx) * slots_per_bay : 
															slotIdx + (-(bladeHeight - 1) + bayIdx) * slots_per_bay;
			if (skipAnchorCheck ||  
					getModelChassisSlot(modelChassis, slotPosition + 1).isAnchor() || 
					getModelChassisSlot(modelChassis, otherEndSlotPosition + 1).isAnchor()) {
			}
			else {
				slots.clear();
			}
		}
		else {
			slots.clear();
			/*for (int runningSlot = 0; runningSlot < maxSlots; runningSlot++) {
				bayIncr = 1;
				Integer otherEndSlot = runningSlot + (bladeHeight - 1) * slots_per_bay;
				if (otherEndSlot >= maxSlots) {
					continue;
				}
				if (otherEndSlot.intValue() == (slotNumber - 1)) {
					slots.clear();
					slotNumber = (long) runningSlot;
					slotIdx = ((slotNumber.intValue()  - 1) % slots_per_bay); 
					bayIdx = (slotNumber.intValue() - 1) / slots_per_bay;
					for (int slot = slotIdx; slot < slotIdx + bladeWidth && slot < slots_per_bay; slot++) {
						for (int bay = bayIdx; bay < bayIdx + bladeHeight && bay < bays && bay >= 0; bay += bayIncr) {
							// Add the model chassis slot of the default item
							slots.add(getModelChassisSlot(modelChassis, bay * slots_per_bay + slot + 1));
							slots_checked++;
						}
					}

					if (slots_to_be_checked == slots_checked) {
						Integer slotPosition = bayIdx * slots_per_bay + slotIdx; //slotIdx;
						Integer otherEndSlotPosition = slotIdx + ((bladeHeight - 1) + bayIdx) * slots_per_bay;
						if (skipAnchorCheck ||  
								getModelChassisSlot(modelChassis, slotPosition + 1).isAnchor() || 
								getModelChassisSlot(modelChassis, otherEndSlotPosition + 1).isAnchor()) {
						}
						else {
							slots.clear();
						}
					}
					else {
						// slots.clear();
					}
					break;
				}
			}*/
		}

		return slots;
	}
	

	
	private ArrayList<ModelChassisSlot> sortChassisSlot(Set<ModelChassisSlot> unsortedChassisSlots) {
		ArrayList<ModelChassisSlot> list = new ArrayList<ModelChassisSlot>(unsortedChassisSlots);
		Collections.sort(list, new customModelChassisComparator());
		return list;
	}

	
	
	private Map<Long, String> getAvailableBladeSlots(ItItem itItem,
			ModelChassis modelChassis, String layout, int bladeWidth, int bladeHeight, 
			long bladeFormFactor) {
		/* modelChassis table can be null if the chassis is from 2.6. This table was introduced
		 * in 3.0. For chassis from 2.6, assume the following 
		 * anchor = false
		 * spanning = false 
		 * bays = 1
		 * # slots = 64 */ 
		Map<Long, String> availableSlots = new HashMap<Long, String>();
		/* bayIdx is used to iterate through all the bays in the chassis 
		 * chassisSlotIdx is used to iterate through all the slots in a given bay
		 * bladeSlotIdx is used to iterate through all the slots starting the chassisSlotIdx for availability check
		 * bladeBayIdx is used to iterate through all the bay starting the bayIdx for availability check 
		 * bayIdxIncr is used to skip bays that need not be check, for example a half size blade in a 4 bay chassis should skip the odd numbered bays */
		int bayIdx, chassisSlotIdx, bladeSlotIdx, bladeBayIdx, bayIdxIncr = (BLADE_HALF_DOUBLE == bladeFormFactor || BLADE_HALF == bladeFormFactor) ? bladeHeight : 1;
		int bays = (null != modelChassis) ? modelChassis.getBays() : DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		int slots_per_bay = (null != modelChassis) ? modelChassis.getSlotMax() / bays : DEFUALT_MAX_SLOTS_IN_CHASSIS / DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		int slots_to_be_checked = bladeWidth * bladeHeight;
		boolean skipAnchorCheck = (bladeHeight == 1) /*|| (bays == bladeHeight)*/;

		if (null == layout) {
			return availableSlots;
		}
		ArrayList<ModelChassisSlot> sortedChassisSlots = null;
		if (null != modelChassis) {
			sortedChassisSlots = sortChassisSlot(modelChassis.getModelChassisSlot());
		}
		if (null == sortedChassisSlots) {
			skipAnchorCheck = true;
		}
 		for (bayIdx = 0; bayIdx < bays; bayIdx += bayIdxIncr) { // i++
 			// Integer j_idx = i * slots_per_bay;
 			int j_idx = 0;
 			for (chassisSlotIdx = j_idx; chassisSlotIdx < j_idx + slots_per_bay; chassisSlotIdx++) {
 				int k_idx = chassisSlotIdx;
 				boolean available = true;
 				int slots_checked = 0;
 				for (bladeSlotIdx = k_idx; bladeSlotIdx < k_idx + bladeWidth && bladeSlotIdx < slots_per_bay; bladeSlotIdx++) {
 					for (bladeBayIdx = bayIdx; 
 							bladeBayIdx < bayIdx + bladeHeight && bladeBayIdx < bays &&
 							(bladeBayIdx * (slots_per_bay) + bladeSlotIdx) < layout.length() &&
 							(bladeBayIdx * (slots_per_bay) + bladeSlotIdx) >= 0; 
 							bladeBayIdx++) {
 						available &= (layout.charAt(bladeBayIdx * (slots_per_bay) + bladeSlotIdx) == '0');
 						slots_checked++;
 					}
 				}
 				 
 				if (slots_to_be_checked == slots_checked &&	available) {
 					Integer slotPosition = k_idx + bayIdx * slots_per_bay; //k_idx;
 					Integer otherEndSlotPosition = k_idx + (bayIdx + bladeHeight - 1) * slots_per_bay;
 					if (skipAnchorCheck || 
 							(slotPosition >= 0 && slotPosition < sortedChassisSlots.size() && sortedChassisSlots.get(slotPosition).isAnchor())) {
 						// If slot number is required by the client return sortedChassisSlots.get(k_idx).getSlotNumber()
 						Integer slotPos = (slotPosition + 1);
 						availableSlots.put(
 								(null != sortedChassisSlots) ? sortedChassisSlots.get(slotPosition).getSlotNumber() : slotPos.longValue(), 
 								(null != sortedChassisSlots) ? sortedChassisSlots.get(slotPosition).getSlotLabel() : slotPos.toString());
 						// availableSlots.add((null != sortedChassisSlots) ? sortedChassisSlots.get(slotPosition).getSlotLabel() : slotPosition.toString());
 					}
 					// the other corner (index) can be an anchor
 					else if (skipAnchorCheck || 
 							(otherEndSlotPosition >= 0 && otherEndSlotPosition < sortedChassisSlots.size() && sortedChassisSlots.get(otherEndSlotPosition).isAnchor())) {
 						Integer slotPos = (otherEndSlotPosition + 1);
 						// If slot number is required by the client return sortedChassisSlots.get(k_idx).getSlotNumber()
 						availableSlots.put((null != sortedChassisSlots) ? sortedChassisSlots.get(otherEndSlotPosition).getSlotNumber() : slotPos.longValue(), 
 								(null != sortedChassisSlots) ? sortedChassisSlots.get(otherEndSlotPosition).getSlotLabel() : slotPos.toString());
 						// availableSlots.add((null != sortedChassisSlots) ? sortedChassisSlots.get(otherEndSlotPosition).getSlotLabel() : otherEndSlotPosition.toString());
 					}
 				}
 			}
		}
		return availableSlots;
	}
	
	private void updateReservedSlots(ModelChassis modelChassis, char[] layout) {
		if(modelChassis != null && layout != null){
			int layoutSize = layout.length;
			for (ModelChassisSlot slot: modelChassis.getModelChassisSlot()) {
				if (slot.isReserved() && (slot.getSlotNumber() - 1) < layoutSize) {
					layout[slot.getSlotNumber() - 1] = '1';
				}
			}
		}
	}
	
	private String computeChassisItemLayout(ModelChassis modelChassis, Collection<ItItem> blades, long chassisFaceLksId) {
		int bays = DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		int maxSlots = DEFUALT_MAX_SLOTS_IN_CHASSIS;
		if (null != modelChassis) {
			bays = modelChassis.getBays();
			maxSlots = modelChassis.getSlotMax();
		}
		int slots_per_bay =  maxSlots / bays;
		char[] layout = new char[bays * slots_per_bay];
		Arrays.fill(layout, '0');
		
		if (null != modelChassis) {
			/* update layout as 1 for reserved slots */
			updateReservedSlots(modelChassis, layout);
		}
		
		if (null == blades) {
			return new String(layout);
		}
		for (ItItem blade: blades) {
			Long slotNumber = blade.getSlotPosition();
			
			if (slotNumber >= 1 && slotNumber <= maxSlots && bladeFacingMatch(blade, chassisFaceLksId)) 
			{
				if (blade.getModel() == null) {
					continue;
				}
				long formFactor = getFormFactorId(blade.getModel().getFormFactor());
				int bladeWidth = getBladeWidth(modelChassis, formFactor);
				int bladeHeight = getBladeHeight(modelChassis, formFactor);
				int slotIdx = ((slotNumber.intValue()  - 1) % slots_per_bay); 
				int bayIdx = (slotNumber.intValue() - 1) / slots_per_bay;
				int bayIncr = ((bayIdx % 2) == 0) ? 1 : -1;
				int slots_checked = 0;
				int slots_to_be_checked = bladeWidth * bladeHeight;
				ArrayList<Integer> slotAssigned = new ArrayList<Integer>();  
				
				for (int slot = slotIdx; slot < slotIdx + bladeWidth && slot < slots_per_bay; slot++) {
					for (int bay = bayIdx; ((bayIncr == -1) ? (bay > bayIdx - bladeHeight) : (bay < bayIdx + bladeHeight)) && 
							bay < bays && bay >= 0; 
							bay += bayIncr) {
						// layout[bay * slots_per_bay + slot] = '1';
						slotAssigned.add(bay * slots_per_bay + slot);
						slots_checked++;
					}
/*					for (int bay = bayIdx; bay < bayIdx + bladeHeight && bay < bays && bay >= 0; bay += bayIncr) {
						layout[bay * slots_per_bay + slot] = '1';
						slots_checked++;
					}*/
				}
				if (slots_to_be_checked == slots_checked) {
					for (Integer layoutSlotIdx : slotAssigned) {
						layout[layoutSlotIdx] = '1';
					}
				} /*else {
					bayIncr = 1;
					for (int runningSlot = 0; runningSlot < maxSlots; runningSlot++) {
						Integer otherEndSlot = runningSlot + (bladeHeight - 1) * slots_per_bay;
						if (otherEndSlot >= maxSlots) {
							continue;
						}
						if (otherEndSlot.intValue() == (slotNumber - 1)) {
							slotNumber = (long) runningSlot;
							slotIdx = ((slotNumber.intValue()  - 1) % slots_per_bay); 
							bayIdx = (slotNumber.intValue() - 1) / slots_per_bay;
							for (int slot = slotIdx; slot < slotIdx + bladeWidth && slot < slots_per_bay; slot++) {
								for (int bay = bayIdx; bay < bayIdx + bladeHeight && bay < bays && bay >= 0; bay += bayIncr) {
									layout[bay * slots_per_bay + slot] = '1';
									slots_checked++;
								}
							}
							break;
						}
					}
				}*/
			}
		}
		return new String(layout);
	}
	

	
	private long getFormFactorId(String bladeFormFactorStr) {
		if (null == bladeFormFactorStr) {
			return -1;
		}
		if (bladeFormFactorStr.equals("Full")) {
			return BLADE_FULL;
		}
		if (bladeFormFactorStr.equals("Full-Double")) {
			return BLADE_FULL_DOUBLE;
		}
		if (bladeFormFactorStr.equals("Half")) {
			return BLADE_HALF;
		}
		if (bladeFormFactorStr.equals("Half-Double")) {
			return BLADE_HALF_DOUBLE;
		}
		if (bladeFormFactorStr.equals("Quarter")) {
			return BLADE_QUARTER;
		}
		return -1;
	}

	private int getBladeHeight(ModelChassis modelChassis,
			long bladeFormFactor) {
		Integer bladeHeight = 0;
		/* default */
		if (null == modelChassis) {
			return DEFUALT_NUM_OF_BAYS_IN_CHASSIS;
		}
		if (BLADE_FULL_DOUBLE == bladeFormFactor || BLADE_FULL == bladeFormFactor) {
			if (modelChassis.isAllowSpanning()) {
				bladeHeight = modelChassis.getBays();
			}
			else {
				bladeHeight = 1;
			}
		}
		else if (BLADE_HALF == bladeFormFactor || BLADE_HALF_DOUBLE == bladeFormFactor) {
			bladeHeight = modelChassis.getBays() / 2;
		}
		else if (BLADE_QUARTER == bladeFormFactor) {
			bladeHeight = modelChassis.getBays() / 4;
		}
		return bladeHeight;
	}

	private int getBladeWidth(ModelChassis modelChassis,
			long bladeFormFactor) {
		Integer bladeWidth = 0;
		if (BLADE_FULL == bladeFormFactor || BLADE_HALF == bladeFormFactor || BLADE_QUARTER == bladeFormFactor) {
			bladeWidth = 1;
		}
		else if (BLADE_FULL_DOUBLE == bladeFormFactor || BLADE_HALF_DOUBLE == bladeFormFactor) {
			bladeWidth = 2;
		}
		return bladeWidth;
	}

	private boolean isBladeAllowed(ModelChassis modelChassis, long bladeFormFactorType) {
		boolean bladeAllowed = true;
		
		if (bladeFormFactorType != BLADE_FULL && 
				bladeFormFactorType != BLADE_FULL_DOUBLE &&
				bladeFormFactorType != BLADE_HALF &&
				bladeFormFactorType != BLADE_HALF_DOUBLE &&
				bladeFormFactorType != BLADE_QUARTER) {
			return false;
		}
		/* when modelChassis is null, assume full size blades are allowed */
		if (null == modelChassis) { 
			return (BLADE_FULL == bladeFormFactorType);
		}
		
		/*  
		 * check for is_allow_spanning => false: full size blades only 
		 * check for is_allow_double => false: do not allow double wide blade 
		 * FULL size blades are allowed for all chassis 
		 * DOUBLE size blades are allowed for all chassis 
		 * HALF size blades are allow for chassis having # of bays that are multiple of 2 
		 * QUARTER size blades are allow for chassis having # of bays that are multiple of 4 
		 * */
		if (!modelChassis.isAllowSpanning()) { 
			if (BLADE_FULL_DOUBLE == bladeFormFactorType || BLADE_FULL == bladeFormFactorType) {
				bladeAllowed &= true;
			}
			else {
				bladeAllowed &= false;
			}
		}
		if (!modelChassis.isAllowDouble()) { 
			if (BLADE_FULL_DOUBLE == bladeFormFactorType || BLADE_HALF_DOUBLE == bladeFormFactorType) {
				bladeAllowed &= false;
			}
			else {
				bladeAllowed &= true;
			}
		}
		if (((modelChassis.getBays() % 2) != 0) && 
				(BLADE_HALF_DOUBLE == bladeFormFactorType || BLADE_HALF == bladeFormFactorType)) {
			bladeAllowed &= false;
		}
		if (((modelChassis.getBays() % 4) != 0) && BLADE_QUARTER == bladeFormFactorType) {
			bladeAllowed &= false;
		}
		return bladeAllowed;
	}
	
	private ModelDetails getModelDetails(long modelId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria modelCriteria = session.createCriteria(ModelDetails.class);
		
		modelCriteria.add(Restrictions.eq("modelDetailId", modelId));
	
		ModelDetails model = (ModelDetails) modelCriteria.uniqueResult();
		return model;
	}
	
	private Item getItemInfo(Long itemId) throws DataAccessException {
		Item item = null;
		try {
			Session session = this.sessionFactory.getCurrentSession();
			Criteria itItemCriteria = session.createCriteria(Item.class);
			
			itItemCriteria.add(Restrictions.eq("itemId", itemId));
		
			item = (Item) itItemCriteria.uniqueResult();
		}
		catch(HibernateException e){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		return item;
	}

	private ModelChassis getBladeChassis(ModelDetails model, long faceLkpValueCode) throws DataAccessException {
		if (null == model) {
			return null;
		}
		Set<ModelChassis> mcList = model.getModelChassis();
		for (ModelChassis mc: mcList) {
			if (null != mc.getFaceLks() && null != mc.getFaceLks().getLkpValueCode() && mc.getFaceLks().getLkpValueCode().longValue() == faceLkpValueCode) {
				return mc;
			}
		}
		return null;
	}
	
	@Override
	public ModelChassis getBladeChassis(Long model_id, long faceLksValueCode) throws DataAccessException {
		try {
			
			//model_id = 1013L;
			//faceLksValueCode = 192L;
			
			Session session = this.sessionFactory.getCurrentSession();
			Criteria modelCriteria = session.createCriteria(ModelChassis.class);
	
			modelCriteria.createAlias("model", "model", Criteria.LEFT_JOIN);
			modelCriteria.createAlias("faceLksData", "faceLksData", Criteria.LEFT_JOIN);
	
			modelCriteria.add(Restrictions.eq("model.modelDetailId", model_id));
			modelCriteria.add(Restrictions.eq("faceLksData.lkpValueCode", faceLksValueCode));
			
			modelCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			
			/* Test only code, remove after testing done */
			@SuppressWarnings("unchecked")
			// if this is not 1, we got problems
			List<ModelChassis> modelList = modelCriteria.list();
			assert(modelList.size() == 1 || modelList.size() == 0);
	
			ModelChassis modelChassis = (ModelChassis) modelCriteria.uniqueResult();
			return modelChassis;
		}
		catch(HibernateException e) {
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		} catch(org.springframework.dao.DataAccessException e) {

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
	}

	@Override
	public boolean isChassisRearDefined(long chassisItemId) throws DataAccessException {
		if (log.isDebugEnabled()) log.debug("==============================================isChassisRearDefined begin======================================================");
		ItItem item = getItItemDomainObject(chassisItemId);
		ModelChassis modelChassis = null;
		if (null != item && null != item.getModel()) {
			// modelChassis = getBladeChassis(item.getModel().getModelDetailId(), SystemLookup.ChassisFace.REAR);
			modelChassis = getBladeChassis(item.getModel(), SystemLookup.ChassisFace.REAR);
			//modelChassis = getBladeChassis(item.getModel().getModelDetailId(), 192);
		}
		boolean bln = (null != modelChassis);
		if (log.isDebugEnabled()) log.debug("====================================================================================================");
		if(item!=null && item.getModel()!=null){
			if (log.isDebugEnabled()) log.debug("chassisItemId="+ chassisItemId +" item.getModel().getModelDetailId()="+ item.getModel().getModelDetailId() +" isChassisRearDefined="+bln);
		}else{
			if (log.isDebugEnabled()) log.debug("Everything is null!");
		}
		if (log.isDebugEnabled()) log.debug("====================================================================================================");
		return bln;
	}

	private boolean isBladeAllowedinChassisFace(long chassisItemId, long bladeModelId, long faceLkpValueCode, long bladeItemId) throws DataAccessException {
		
		/* --- uncomment this code if anytime the requirement change to say that if there are no slot available then blade is not allowed.
		Map<Long, String> slotsAvailable = getAvailableSlotPositions(chassisItemId, bladeModelId, faceLkpValueCode, bladeItemId);
		return (slotsAvailable.size() > 0);
		*/

		/* if the chassis definition do not allow blade to be placed and nothing to do with available slots then use this code */
		ItItem item = getItItemDomainObject(chassisItemId);
		ModelChassis modelChassis = null;
		ModelDetails model =  getModelDetails(bladeModelId);
		if (null == model || !model.getMounting().equals("Blade")) {
			return false;
		}
		String bladeFormFactorStr = model.getFormFactor();
		long bladeFormFactor = getFormFactorId(bladeFormFactorStr);

		if (null != item && null != item.getModel()) {
			// modelChassis = getBladeChassis(item.getModel().getModelDetailId(), faceLkpValueCode);
			modelChassis = getBladeChassis(item.getModel(), faceLkpValueCode);
		}
		return (/*null != modelChassis && */isChassisFaceAllowed(modelChassis, faceLkpValueCode) && isBladeAllowed(modelChassis, bladeFormFactor));
	}

	@Override
	public boolean isBladeAllowedinChassisRear(long chassisItemId, long bladeModelId, long bladeItemId) throws DataAccessException {
		return isBladeAllowedinChassisFace(chassisItemId, bladeModelId, SystemLookup.ChassisFace.REAR, bladeItemId);
	}

	@Override
	public boolean isBladeAllowedinChassisFront(long chassisItemId, long bladeModelId, long bladeItemId) throws DataAccessException {
		return isBladeAllowedinChassisFace(chassisItemId, bladeModelId, SystemLookup.ChassisFace.FRONT, bladeItemId);
	}

	public SlotReAssignmentInfo validateChassisModelChange(long chassisItemId, long modelId) throws DataAccessException {
		SlotReAssignmentInfo slotInfoFront = validateChassisModelChange(chassisItemId, modelId, SystemLookup.ChassisFace.FRONT);
		SlotReAssignmentInfo slotInfoRear = validateChassisModelChange(chassisItemId, modelId, SystemLookup.ChassisFace.REAR);
		slotInfoFront.slotEventDetailsList.addAll(slotInfoRear.slotEventDetailsList); // ( + slotInfoRear)
		return slotInfoFront;
	}

	private boolean isChassisModel(long modelId) {
		ModelDetails modelDetails = getModelDetails(modelId);
		return modelDetails.getFormFactor().equals(SystemLookup.FormFactor.CHASSIS);
	}
	
	private SlotReAssignmentInfo validateChassisModelChange(long chassisItemId, long modelId, long chassisFace) throws DataAccessException {
		ModelChassis modelChassis = getBladeChassis(modelId, chassisFace);
		
		SlotReAssignmentInfo slotInfo = updateSlotNumberForBlade(modelChassis, chassisItemId, chassisFace, !updateToDatabase, isChassisModel(modelId));
		return slotInfo;
	}
	
	private void setChassisOrBladeModelDefinitionChangeEvent(List<List<SlotReAssignmentInfoFrontBack>> list, 
			ModelDetails model, String lnEvtStr ) throws DataAccessException {

		for (List<SlotReAssignmentInfoFrontBack> infoList: list) {
			for (SlotReAssignmentInfoFrontBack info: infoList) {
				//JB set event for each 
				for (SlotEventDetails details: info.layoutFront.slotEventDetailsList) {
					setChassisSlotsReAssignmentEvent(details, model.getModelName(), lnEvtStr);
				}
				for (SlotEventDetails details: info.layoutRear.slotEventDetailsList) {
					setChassisSlotsReAssignmentEvent(details, model.getModelName(), lnEvtStr);
				}
			}
		}
	}
	
	// blade model changed, so update all chassis that got affected.
	//@Override
	private void updateBladeDefinition(long bladeModelId) throws DataAccessException {
		ModelDetails model =  getModelDetails(bladeModelId);
		// Get list of ModelChassisId of Chassis items that houses blades with bladeModelId.
		List<Long> modelChassisIdList = getAllChassisModelForBladeModel(bladeModelId);
		for (Long modelChassisId : modelChassisIdList) {
			List<List<SlotReAssignmentInfoFrontBack>> list = updateChassisDefinitionUsingModelId(modelChassisId);
			setChassisOrBladeModelDefinitionChangeEvent(list,  model, LN_EVT_BLADE_MODEL_CHANGE );
		}
	}
	
	// This function is called by dct_lnevent subscriber when chassisModel or bladeModel property is changed.
	@Override
	public void updateModelDefinition(long modelId) throws DataAccessException {
		ModelDetails model =  getModelDetails(modelId);
		String mounting = model.getMounting();
		String formFactor = model.getFormFactor();
		if (mounting.equals(SystemLookup.Mounting.BLADE)) {
			updateBladeDefinition(modelId);
		}
		else if (formFactor.equals(SystemLookup.FormFactor.CHASSIS)) {
			List<List<SlotReAssignmentInfoFrontBack>> list = updateChassisDefinitionUsingModelId(modelId);
			setChassisOrBladeModelDefinitionChangeEvent(list, model, LN_EVT_CHASSIS_MODEL_CHANGE);
		}
	}

	@Override
	public List<ModelDetails> getAllBladeModelForMake(long makeId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("modelMfrDetails", "modelMfrDetails", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", makeId));
		criteria.add(Restrictions.eq("mounting", "Blade"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("modelDetailId"));
		@SuppressWarnings("unchecked")
		List<ModelDetails> list = criteria.list();
		return list;
	}

	@Override
	public List<ModelDetails> getAllBladeModelForMake(long makeId, long classLkpValueCode) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("modelMfrDetails", "modelMfrDetails", Criteria.LEFT_JOIN);
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", makeId));
		criteria.add(Restrictions.eq("mounting", "Blade"));
		criteria.add(Restrictions.eq("classLookup.lkpValueCode", classLkpValueCode));
		criteria.add(Restrictions.eq("formFactor", SystemLookup.FormFactor.FULL));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("modelDetailId"));
		@SuppressWarnings("unchecked")
		List<ModelDetails> list = criteria.list();
		return list;
	}
	
	@Override
	public String getBladeSlotLabel(ItItem bladeItem) throws DataAccessException, BusinessValidationException {
		Long slotNumber = bladeItem.getSlotPosition();
		if (null != bladeItem && null != bladeItem.getBladeChassis() && null != bladeItem.getFacingLookup()) {
			ModelChassis modelChassis = getModelChassisForChassisIdandFacing (bladeItem.getBladeChassis().getItemId(), bladeItem.getFacingLookup().getLkpValueCode());
			if (null != modelChassis) {
				ArrayList<ModelChassisSlot> list = new ArrayList<ModelChassisSlot>(modelChassis.getModelChassisSlot());
				for (ModelChassisSlot slot: list) {
					if (slot.getSlotNumber() == slotNumber) {
						return slot.getSlotLabel();
					}
				}
			}
		}
		return slotNumber.toString(); 
	}

	@Override
	public Integer getChassisSlotNumber(Long chassisId, long chassisFace, String slotLabel) throws DataAccessException, BusinessValidationException {
		if (null == chassisId || null == slotLabel) {
			return -1;
		}
		if (slotLabel.equals("-9")) {
			return -9;
		}
		ModelChassis modelChassis = getModelChassisForChassisIdandFacing (chassisId, chassisFace);
		if (null != modelChassis) {
			ArrayList<ModelChassisSlot> list = new ArrayList<ModelChassisSlot>(modelChassis.getModelChassisSlot());
			for (ModelChassisSlot slot: list) {
				if (slot.getSlotLabel().equals(slotLabel)) {
					return slot.getSlotNumber();
				}
			}
		}else if( chassisFace == SystemLookup.ChassisFace.FRONT){
			//If there is no chassis define, we use default one, but that one works only for the front facing.
			//For rear facing it does not
			List<ChassisSlotDTO> slotDtos = new ArrayList<ChassisSlotDTO>();
			List<ChassisSlotDTO> defaultChassisInfo = getDefaultSlotDetails(slotDtos);

			for( ChassisSlotDTO dto : defaultChassisInfo ){
				if( dto.getLabel().equals(slotLabel)) return dto.getNumber();
			}
		}
		return -1;
	}
	
	@Override
	public String getChassisSlotLabel(Long chassisId, long chassisFace, Integer slotNumber) throws DataAccessException, BusinessValidationException {
		if (null == chassisId || null == slotNumber) {
			return "";
		}
		if (slotNumber <= 0) {
			return "";
		}
		ModelChassis modelChassis = getModelChassisForChassisIdandFacing (chassisId, chassisFace);
		if (null != modelChassis) {
			ArrayList<ModelChassisSlot> list = new ArrayList<ModelChassisSlot>(modelChassis.getModelChassisSlot());
			for (ModelChassisSlot slot: list) {
				if (slot.getSlotNumber() == slotNumber) {
					return slot.getSlotLabel();
				}
			}
		} else if( chassisFace == SystemLookup.ChassisFace.FRONT) {
			//If there is no chassis define, we use default one, but that one works only for the front facing.
			//For rear facing it does not
			List<ChassisSlotDTO> slotDtos = new ArrayList<ChassisSlotDTO>();
			List<ChassisSlotDTO> defaultChassisInfo = getDefaultSlotDetails(slotDtos);

			for( ChassisSlotDTO dto : defaultChassisInfo ){
				if( dto.getNumber() == slotNumber) return dto.getLabel();
			}
		}
		return "";
	}
	//<-- private functions
	
	private List<Long> getAllChassisModelForBladeModel(long bladeModelId) {
		List<Long> chassisItemIdList = 
				getAllChassisItemIdForBladeModelId(bladeModelId);
		
		if (chassisItemIdList.isEmpty()) {
			return chassisItemIdList;
		}
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("model.modelDetailId"))));
		criteria.add(Restrictions.in("itemId", chassisItemIdList));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("unchecked")
		List<Long> list = criteria.list();
		return list;
	}
		
	private List<Long> getAllChassisItemIdForBladeModelId(long bladeModelId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ItItem.class);
		
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.createAlias("bladeChassis", "bladeChassis", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("model.modelDetailId", bladeModelId));
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("bladeChassis.itemId"))));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("unchecked")
		List<Long> list = criteria.list();
		return list;
	}

	private List<ModelChassis> getModelChassis(long modelId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelChassis.class);
		criteria.createAlias("model", "model", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("model.modelDetailId", modelId));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("unchecked")
		List<ModelChassis> list = criteria.list();
		return list;
	}

	private List<List<SlotReAssignmentInfoFrontBack>> updateChassisDefinitionUsingModelId(long modelId) throws DataAccessException {
		List<List<SlotReAssignmentInfoFrontBack>> result = new ArrayList<List<SlotReAssignmentInfoFrontBack>>();
		List<ModelChassis> chassisModelList = getModelChassis(modelId);
		for (ModelChassis chassisModel : chassisModelList) {
			List<SlotReAssignmentInfoFrontBack> info = updateChassisDefinition(chassisModel.getModelChassisId());
			result.add(info);
		}
		return result;
	}

	private SlotEventDetails setChassisSlotsReAssignmentEventData(long chassisItemId, 
			String bladeItemName, String cause, long slotNumberBefore) {
		SlotEventDetails slotEventDetails = new SlotEventDetails();
		slotEventDetails.bladeItemName = bladeItemName;
		slotEventDetails.chassisItemId = chassisItemId;
		slotEventDetails.errCause = (null == cause) ? "Invalid Slot Assignment" : cause;
		slotEventDetails.slotNumberBefore = slotNumberBefore;
		return slotEventDetails;
	}
	
	private String getEventSummary(String bladeItemName, String chassisItemName,
			String BladeOrChassisModel, String evtCauseStr ) {
		StringBuffer evtSummary = new StringBuffer();
		evtSummary.append("'").append(bladeItemName).append("'")
			.append(" could not be reassigned to a slot in '")
			.append(chassisItemName)
			.append("' due to a change in either the blade or chassis model attributes.");
		return evtSummary.toString();
	}
	
	private void setChassisSlotsReAssignmentEvent(SlotEventDetails details,
			String bladeOrChassisModelName, String evtCauseStr) throws DataAccessException { 
		if (details.slotNumberBefore <= 0) {
			return;
		}
		Event ev = null;
		Session session = null;
		String EVENT_SOURCE = "dcTrack";
		String chassisItemName = "unknown";
		String location = "unknown";
		String cabinet = "unknown";
		
		try {
			session = this.sessionFactory.getCurrentSession();
			ItItem item = (ItItem)session.get(ItItem.class, details.chassisItemId);
			if (item != null) {
				location = item.getDataCenterLocation().getDcName();
				cabinet = item.getParentItem().getItemName();
				chassisItemName = item.getItemName();
			}
			String evtSummary = getEventSummary(details.bladeItemName, chassisItemName,
					bladeOrChassisModelName, evtCauseStr);

			session = sessionFactory.getCurrentSession();
			Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
			// Create the asset event with default severity level
			ev = Event.createEvent(session, createdAt, Event.EventType.CHASSIS_SLOT_REASSIGNMENT, EVENT_SOURCE);
			ev.setSeverity(session, EventSeverity.WARNING);
			// set warnign level
			ev.setSummary(evtSummary.toString());
			ev.addParam(EV_PARAM_LOCATION, location);
			ev.addParam(EV_PARAM_CABINET, cabinet);
			ev.addParam(EV_PARAM_CHASSIS, chassisItemName);
			ev.addParam(EV_PARAM_BLADE, details.bladeItemName);
			ev.addParam(EV_PARAM_SLOT, (details.slotNumberBefore <= 0) ? "" : (new Long(details.slotNumberBefore).toString()));
			ev.addParam(EV_PARAM_ERR_CAUSE, details.errCause);
			session.save(ev);
			//FIXME: JB remove flush
			session.flush();
		}
		catch (HibernateException ex) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_CREATION_FAILED, this.getClass(), null));
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_CREATION_FAILED, this.getClass(), null));
		}
	}
	

}
