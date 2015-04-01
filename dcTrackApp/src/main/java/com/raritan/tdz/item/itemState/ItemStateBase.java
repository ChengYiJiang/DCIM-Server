/**
 * 
 */
package com.raritan.tdz.item.itemState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.domain.Fields;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.validators.ItemObjectValidatorsFactory;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.CustomFieldsHelper;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * This is the common implementation across all states Any specific state
 * modifications should extend this class and implement their own changes.
 * 
 * @author prasanna
 * 
 */
public class ItemStateBase implements ItemState {

	protected RulesProcessor rulesProcessor;
	protected RemoteRef remoteRef;

	protected SessionFactory sessionFactory;
	protected FieldHome fieldHome;

	@Autowired
	protected ItemHome itemHome;

	protected Map<Long, RoleValidator> allowableTransitionStates;

	private RulesNodeEditability nodeEditability;

	private String domainClass;
	
	private MandatoryFieldStateValidator mandatoryFieldValidator = null;
	
	private ParentChildConstraintValidator pcValidator = null;
	
	@Autowired
	ItemObjectValidatorsFactory itemObjectValidatorsFactory;
	
	@Autowired
	private ItemDAO itemDAO;
	
	public ItemStateBase(RulesProcessor rulesProcessor, RemoteRef remoteRef,
			SessionFactory sessionFactory, FieldHome fieldHome,
			Map<Long, RoleValidator> allowableTransitionStates,
			RulesNodeEditability nodeEditability) {
		super();
		this.rulesProcessor = rulesProcessor;
		this.remoteRef = remoteRef;
		this.sessionFactory = sessionFactory;
		this.fieldHome = fieldHome;
		this.allowableTransitionStates = allowableTransitionStates;
		this.nodeEditability = nodeEditability;
	}

	public Map<Long, RoleValidator> getAllowableTransitionStates() {
		return allowableTransitionStates;
	}

	public void setAllowableTransitionStates(
			Map<Long, RoleValidator> allowableTransitionStates) {
		this.allowableTransitionStates = allowableTransitionStates;
	}

	public RulesNodeEditability getNodeEditability() {
		return nodeEditability;
	}

	public void setNodeEditability(RulesNodeEditability nodeEditability) {
		this.nodeEditability = nodeEditability;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}

	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}

	public FieldHome getFieldHome() {
		return fieldHome;
	}

	public void setFieldHome(FieldHome fieldHome) {
		this.fieldHome = fieldHome;
	}

	public String getDomainClass() {
		return domainClass;
	}

	public void setDomainClass(String domainClass) {
		this.domainClass = domainClass;
	}

	public MandatoryFieldStateValidator getMandatoryFieldValidator() {
		return mandatoryFieldValidator;
	}

	public void setMandatoryFieldValidator(
			MandatoryFieldStateValidator mandatoryFieldValidator) {
		this.mandatoryFieldValidator = mandatoryFieldValidator;
	}
	
	public ParentChildConstraintValidator getPcValidator() {
		return pcValidator;
	}

	public void setPcValidator(ParentChildConstraintValidator pcValidator) {
		this.pcValidator = pcValidator;
	}
	
	public ItemObjectValidatorsFactory getItemObjectValidatorsFactory() {
		return itemObjectValidatorsFactory;
	}

	public void setItemObjectValidatorsFactory(
			ItemObjectValidatorsFactory itemObjectValidatorsFactory) {
		this.itemObjectValidatorsFactory = itemObjectValidatorsFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.raritan.tdz.item.itemState.ItemState#getEditability(com.raritan.tdz
	 * .domain.Item)
	 */
	@Override
	public RulesNodeEditability getEditability(Item item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate(Object target, Errors errors) {
		validateAllButReqFields(target, null, errors);
	
		try {
			validateRequired(target, errors, "ItemValidator.fieldRequired");
		}catch (ClassNotFoundException e) {
			// This should generally not occur. However, a generic message is
			// provided in the errors
			errors.reject("Some fields are required in this state");
		}catch (DataAccessException e) {
			// This should generally not occur. However, a generic message is
			// provided in the errors
			errors.reject("Some fields are required in this state");
		}
	}


	@Override
	public boolean canTransition(Item item) {
		boolean retval = false;
		if( item.getStatusLookup() != null){
			retval= allowableTransitionStates.containsKey(item.getStatusLookup()
					.getLkpValueCode());
		}
		return retval;
	}

	@Override
	public void onSave(Item item) throws DataAccessException,
			BusinessValidationException, ClassNotFoundException {
		// At this moment we do not have any common code for save
		// we may add it in the future.

	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.getName().equals(domainClass);
	}

	
	@Override
	public void validateMandatoryFields(Item item, Errors errors, Long newStatusLkpValueCode) throws DataAccessException, ClassNotFoundException {	
		if (mandatoryFieldValidator != null) {
			//First validate the required fields for the item state
			validateRequired(item,errors, "ItemValidator.fieldRequired.withItemName");
			mandatoryFieldValidator.validateMandatoryFields(item, newStatusLkpValueCode, errors, "request", null);
		}
	}
	
	protected void validateEditablity(Object target, Errors errors,
			RulesNodeEditability nodeEditablity) throws HibernateException,
			ClassNotFoundException {

		Session session = sessionFactory.getCurrentSession();

		Item item = (Item) target;
		// First get the list of nodes that cannot be edited
		List<String> xPaths = nodeEditablity.getNonEditableNodes();

		// For each of the non-editable nodes
		for (String xPath : xPaths) {
			// Get their corresponding uiID for the component
			String uiId = getUiId(xPath);

			// Get the remoteRef
			String remoteReference = rulesProcessor.getRemoteRef(uiId);

			String remoteType = remoteRef.getRemoteType(remoteReference);
			String remoteAlias = remoteRef.getRemoteAlias(remoteType,
					RemoteRefConstantProperty.FOR_VALUE);

			// Create the criteria using remoteRef
			Criteria criteria = getCriteria(session, remoteType, remoteAlias);

			// Get the data from the database for the given id
			criteria.add(Restrictions.eq("itemId", item.getItemId()));

			String dbValue = (String) criteria.uniqueResult();

			// Now look for any changes in the target item from the original DB
			// item
			// for the above remoteReference using JXPath
			JXPathContext jc = JXPathContext.newContext(item);
			String xpath = getFieldTrace(remoteType, remoteAlias).replace(".",
					"/");
			String clientValue = (String) jc.getValue(xpath);

			// If they are different, flag them as error
			if (!clientValue.equals(dbValue)) {
				Object[] errorArgs = { getUiFieldName(uiId) };
				errors.rejectValue(remoteAlias,
						"ItemValidator.fieldNotEditable", errorArgs,
						"Non editable field");
			}
		}
	}
	
	// Fields not required for VMs
	private List<String> vmNonRequiredFields = Arrays.asList("cmbMake", "cmbModel","tiSerialNumber","tiAssetTag","tieAssetTag");

	protected void validateRequired(Object target, Errors errors, String errorCode)
			throws DataAccessException, ClassNotFoundException {
		Item item = (Item) target;
		final long subClass = item.getSubclassLookup() != null ? item.getSubclassLookup().getLkpValueCode() : 0;

		// First get all the fields for the given classLkpValue obtained from
		// the item object
		List<FieldDetails> fieldDetailsList = fieldHome
				.getFieldDetailsList(item.getClassLookup().getLkpValueCode());

		// Loop through the fields to get the UiComponentId
		for (FieldDetails fieldDetail : fieldDetailsList) {
			if (fieldDetail.getIsRequiedAtSave() == true) {
				String uiComponentId = fieldDetail.getField()
						.getUiComponentId();
				
				String remoteReference = null;

				try {
					// Get the remoteRef for the UiComponentId
					remoteReference = rulesProcessor
							.getRemoteRef(uiComponentId);
				} catch (JXPathNotFoundException e) {
					continue;
				}

				String remoteType = remoteRef.getRemoteType(remoteReference);
				String remoteAlias = remoteRef.getRemoteAlias(remoteReference,
						RemoteRefConstantProperty.FOR_VALUE);
				
				// Special handling for custom fields
				if (CustomFieldsHelper.isCustomField(uiComponentId)) {
					if (!CustomFieldsHelper.validateRequired( item, fieldDetail.getField() )) {
						Object[] errorArgs = { fieldDetail.getField().getCustomLku().getLkuValue(), item.getItemName() };
						errors.rejectValue(remoteAlias,
								errorCode, errorArgs,
								"Required field");
					}
					continue;
				}
				
				// Special handling for VMs
				if (subClass == SystemLookup.SubClass.VIRTUAL_MACHINE) {
					if (vmNonRequiredFields.contains(uiComponentId)) continue;
				}

				if (remoteType == null || remoteAlias == null)
					continue;

				// Look at the item object to see if the field is filled
				// If not generate an error
				JXPathContext jc = JXPathContext.newContext(item);
				String xpath = getFieldTrace(remoteType, remoteAlias).replace(
						".", "/");
				if (xpath != null && !xpath.isEmpty()) {
					try {
						String clientValue = jc.getValue(xpath) != null ? jc
								.getValue(xpath).toString() : null;

						if (clientValue == null) {
							Object[] errorArgs = { getUiFieldName(uiComponentId), item.getItemName() };
							errors.rejectValue(remoteAlias,
									errorCode, errorArgs,
									"Required field");
						} else if (clientValue.isEmpty()) {
							Object[] errorArgs = { getUiFieldName(uiComponentId), item.getItemName() };
							errors.rejectValue(remoteAlias,
									errorCode, errorArgs,
									"Required field");
						}
					} catch (JXPathNotFoundException e) {
						Object[] errorArgs = { getUiFieldName(uiComponentId), item.getItemName() };
						errors.rejectValue(remoteAlias,
								errorCode, errorArgs,
								"Required field");
					}
				}
			}
		}
	}
	
	/**
	 * Clear all cabinet placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearCabinetPlacement(Item item){
		if (item instanceof CabinetItem) {
			CabinetItem cabinet = (CabinetItem)item;
			cabinet.setRowLabel(null);
			cabinet.setPositionInRow(0);
			cabinet.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.FrontFaces.NORTH));
		}
		
	}
	
	/**
	 * Clear all cabinet placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearDevNetFreeStandingPlacement(Item item){
		if ((item instanceof ItItem)&& item.getModel() != null
				&& item.getModel().getMounting() != null
				&& item.getModel().getMounting().equals(SystemLookup.Mounting.FREE_STANDING)){
			Item parentItem = itemDAO.initializeAndUnproxy(item.getParentItem());
			CabinetItem cabinet = null;
			if (parentItem instanceof CabinetItem) {
				cabinet = (CabinetItem) parentItem;
				cabinet.setRowLabel(null);
				cabinet.setPositionInRow(0);
				cabinet.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.FrontFaces.NORTH));
			}
			else {
				throw new RuntimeException("cannot access conatiner cabinet");
			}
		}
		
	}
	
	/**
	 * Clear all standard device placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearStandardPlacement(Item item){
		if ((item instanceof ItItem || item instanceof MeItem) 
				&& item.getModel() != null
				&& item.getModel().getMounting() != null
				&& item.getModel().getMounting().equals(SystemLookup.Mounting.RACKABLE) 
				&& (null == item.getSubclassLookup() || SystemLookup.SubClass.CONTAINER != item.getSubclassLookup().getLkpValueCode().longValue()) ) {
			
			item.setMountedRailLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.RailsUsed.BOTH ));
			item.setParentItem(null);
			item.setUPosition(-9);
			item.setFacingLookup(null);
		}
	}
	
	/**
	 * Clear all standard device placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearBladePlacement(Item item){
	
		if (item instanceof ItItem && item.getModel() != null
				&& item.getModel().getMounting() != null 
				&&  item.getModel().getMounting().equals(SystemLookup.Mounting.BLADE)){
			ItItem itItem = (ItItem)item;
			itItem.setParentItem(null);
			itItem.setBladeChassis(null);
			itItem.setSlotPosition(-9);
			itItem.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.ChassisFace.FRONT ));
		}
	}
	
	
	/**
	 * Clear all standard device placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearVMPlacement(Item item){
	
		if (item instanceof ItItem && item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)){
			ItItem itItem = (ItItem)item;
			itItem.setDataStore(null);
		}
	}
	
	/**
	 * Clear all non rackable placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 * @throws DataAccessException 
	 */
	protected void clearNonRackablePlacement(Item item) throws DataAccessException{
		if ((item instanceof ItItem || item instanceof MeItem)&& item.getModel() != null && item.getModel().getMounting() != null 
				&& item.getModel().getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)){
			if (item.getParentItem() != null && item.getMountedRailLookup() != null)
					itemHome.updateShelfPosition(item.getParentItem().getItemId(), item.getUPosition(), item.getMountedRailLookup().getLkpValueCode(), null, item);
			
			item.setParentItem(null);
			item.setMountedRailLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.RailsUsed.FRONT ));
			item.setUPosition(-9);
			item.setFacingLookup(null);
			
		}
	}
	
	/**
	 * Clear all zeroU placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearZeroUPlacement(Item item){
		if ((item instanceof ItItem || item instanceof MeItem) && item.getModel() != null && item.getModel().getMounting() != null
				&&	item.getModel().getMounting().equals(SystemLookup.Mounting.ZERO_U)){		
			item.setParentItem(null);
			item.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.ZeroUDepth.FRONT ));
			item.setMountedRailLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.RailsUsed.LEFT_REAR ));
			item.setUPosition(-9);
		}
	}
	
	/**
	 * Clear all PowerOutlet placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearPowerOutletPlacement(Item item) {
		if (item instanceof MeItem && item.getClassLookup() != null && item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_OUTLET)) {
			MeItem meItem = (MeItem)item;
			meItem.setParentItem(null);
			meItem.setPduPanelItem(null);
			meItem.setUpsBankItem(null);
		}
	}
	
	/**
	 * Clear all FloorPDU placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearFloorPDUPlacement(Item item){
		if (item instanceof MeItem && item.getClassLookup() != null && item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU)){
			MeItem meItem = (MeItem)item;
			meItem.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.FrontFaces.NORTH));
		}
	}
	
	/**
	 * Clear all UPS placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearUPSPlacement(Item item){
		if (item instanceof MeItem && item.getClassLookup() != null && item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.UPS)){
			MeItem meItem = (MeItem)item;
			meItem.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.FrontFaces.NORTH));
			meItem.setUpsBankItem(null);
		}
	}
	
	/**
	 * Clear all CRAC placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	protected void clearCRACPlacement(Item item){
		if (item instanceof MeItem && item.getClassLookup() != null 
				&& item.getClassLookup() != null && item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CRAC)){
			MeItem meItem = (MeItem)item;
			meItem.setFacingLookup(SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.FrontFaces.NORTH));
			meItem.setCracNwGrpItem(null);
		}
	}
	
	private String getUiFieldName(String uiComponentId) {
		String uiFieldName = "";

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Fields.class);
		criteria.setProjection(Projections.property("defaultName"));
		criteria.add(Restrictions.eq("uiComponentId", uiComponentId));

		uiFieldName = (String) criteria.uniqueResult();

		return uiFieldName;
	}

	
	
	private Criteria getCriteria(Session session, String remoteType,
			String remoteAlias) throws ClassNotFoundException {
		Criteria criteria = session.createCriteria(remoteType);

		ProjectionList proList = Projections.projectionList();
		if (remoteAlias != null) {
			for (String alias : getAliases(remoteType, remoteAlias)) {
				if (!criteria.toString().contains(alias.replace(".", "_"))) {
					criteria.createAlias(alias, alias.replace(".", "_"),
							Criteria.LEFT_JOIN);
				}
			}
		}

		if (remoteAlias != null && !remoteAlias.isEmpty()) {
			String traceStr = getFieldTrace(remoteType, remoteAlias);
			String aliasForValue = traceStr.contains(".") ? traceStr
					.substring(traceStr.lastIndexOf(".")) : remoteAlias;
			String aliasForProjection = traceStr.contains(".") ? traceStr
					.substring(0, traceStr.lastIndexOf(".")).replace(".", "_")
					: "";

			proList.add(Projections.alias(
					Projections.property(aliasForProjection.toString()
							+ aliasForValue), aliasForProjection));
		}

		criteria.setProjection(proList);

		return criteria;
	}

	private String getUiId(String xPath) {
		String uiId = xPath.substring(
				xPath.lastIndexOf("uiViewComponent[@uiId='"),
				xPath.lastIndexOf("']"));
		return uiId;
	}

	// TODO: This method is getting repeated everywhere we need alias. Need to
	// come up with a bean/static class
	// TODO: that does this.
	private ArrayList<String> getAliases(String remoteType, String fieldName)
			throws ClassNotFoundException {
		ArrayList<String> aliases = new ArrayList<String>();
		String traceStr = getFieldTrace(remoteType, fieldName);
		String aliasStr = traceStr.contains(".") ? traceStr.substring(0,
				traceStr.lastIndexOf(".")) : null;
		if (aliasStr != null) {
			StringBuffer buffer = new StringBuffer();
			for (String token : aliasStr.split("\\.")) {
				buffer.append(token);
				aliases.add(buffer.toString());
				buffer.append(".");
			}
		}
		return aliases;
	}

	private String getFieldTrace(String remoteType, String fieldName)
			throws ClassNotFoundException {
		// Create Alias
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkpValue",
				new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue",
				new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode",
				new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode",
				new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString();
		return trace;
	}

	@Override
	public List<Long> getAllowableStates() {
		List<Long> retval = new ArrayList<Long>(
				allowableTransitionStates.keySet());
		return retval;
	}

	@Override
	public boolean isTransitionPermittedForUser(Item item, Long newState,
			UserInfo userInfo) {
		RoleValidator roleValidator = allowableTransitionStates.get(newState);
		if (roleValidator != null)
			return roleValidator.canTransition(item, userInfo);
		else
			//The reason for returning true instead of false is because, the itemStateContext use this
			//to indicate that user does not have permission. In fact that is not true. The roleValidator
			//for the specified transition is not available since it is not allowed to go to that state.
			return true; 
	}

	@Override
	public void validateParentChildConstraint(Item item, Errors errors,
			Long newStatusLkpValueCode, String errorCodePrefix)
			throws DataAccessException, ClassNotFoundException {
		if (pcValidator != null){
			pcValidator.validateParentChildConstraint(item, newStatusLkpValueCode, errors, errorCodePrefix);
		}
		
	}

	@Override
	public void validateAllButReqFields(Object target, UserInfo userSession, Errors errors){
		Item item = (Item) target;
		try {
			validateEditablity(target, errors, nodeEditability);
			validateParentChildConstraint(item, errors, item.getStatusLookup().getLkpValueCode(), "ItemValidator.parentChildConstraint");
			if (mandatoryFieldValidator != null)
				mandatoryFieldValidator.validateMandatoryFields(item,  item.getStatusLookup().getLkpValueCode(), errors, "normal", null);
			validateAdditionalPropertiesOfAnItem(item, userSession, errors);
		} catch (HibernateException e) {
			// This should generally not occur. However, a generic message is
			// provided in the errors
			errors.reject("Some fields are not editable in this state");
		} catch (ClassNotFoundException e) {
			// This should generally not occur. However, a generic message is
			// provided in the errors
			errors.reject("Some fields are not editable in this state");
		} catch (DataAccessException e) {
			// This should generally not occur. However, a generic message is
			// provided in the errors
			errors.reject("Some fields are required in this state");
		}
		
	}
	
	/**
	 *  This function validates Item's Connections, requests, stage etc,. 
	 */
	protected void validateAdditionalPropertiesOfAnItem( Item item, UserInfo sessionUser, Errors errors) {
		/* look at derived class for specific implementation */
	}

}
