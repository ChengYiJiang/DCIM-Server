/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.listener.AuditTrailListener;
import com.raritan.tdz.port.home.IPortObjectCollection;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * This implementation of template will not perform the split
 * of valueIdDTO list and process a single item.
 * @author prasanna
 *
 */
public class ItemObjectTemplateSingle extends ItemObjectTemplateBase {	
	
	private Logger log = Logger.getLogger( this.getClass() );
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#saveItem(java.lang.Long, java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<String, UiComponentDTO> saveItem(Long itemId,
			List<ValueIdDTO> valueIdDTOList, UserInfo userInfo)
			throws BusinessValidationException,Throwable {
		
		//If it is an existing item, fetch that item from database
		//as this will be used by many business logic including changeModel
		Long existingItemId = itemId;
		captureItemData(existingItemId);
		String unit = null;
		String classLkpValue = (String) ValueIdDTOHolder.getCurrent().getValue("tiClass");
		final boolean isUpdate = (itemId != null && itemId > 0) ? true : false;
		
		try{
			//Extract units from userInfo
			if (userInfo != null) unit = userInfo.getUnits();
			
			//Make sure that we either create or load the item
			//Unfortunately webclient sends modelId as integer and REST client as Long. This will handle it.
			Long modelId =  null;
			Object cmbModel = ValueIdDTOHolder.getCurrent().getValue("cmbModel");
			if (cmbModel != null){
				modelId = cmbModel instanceof Integer?((Integer)cmbModel).longValue()
					: (Long)cmbModel;
			}
			Item item = itemDomainFactory.createOrLoad(itemId,modelId,classLkpValue != null && classLkpValue.contains("Virtual Machine"));
			
			if (isUpdate){
				//Change the model
				ModelDetails newModel = modelId != null ? modelDao.getModelById(modelId) : null;
				if (newModel != null)
					item.setModel(newModel);
			}

			//Convert the dtoList to item
			//IMPORTANT NOTE: Please put all the conversion including ports behind the
			//                itemDomainAdaptor. 
			item = (Item) itemDomainAdaptor.convert(item, valueIdDTOList, unit);
			
			if (isUpdate){
				onModelChanged(item);
				//OnLocationChange(item);
			}
		
			//Pre-Validate
			if (itemSaveBehaviors != null){
				for (ItemSaveBehavior itemSaveBehavior: itemSaveBehaviors){
					if (itemSaveBehavior.canSupportDomain(item.getClass().getName()))
						itemSaveBehavior.preValidateUpdate(item, userInfo);
				}
			}

			MapBindingResult errors = getErrorObject(item);
			Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo);

			// Pre Validate Updates to the domain object
			if (portObjectsList != null){
				for (String portObject: portObjectsList){
					Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
					IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portObject, item, errors);
					 if (portObjects != null) portObjects.preValidateUpdates(errors);
				}
			}

			//Validate. 
			//IMPORTANT NOTE: Please do not put any validation logic in ItemObjectTemplate
			//                All the validations MUST be taken care by the itemValidator.
			//				  Only exception is when we need to validate the DTOList itself
			//	              which will be taken care by the Domain Adaptors.
			if (itemValidator != null)
				itemValidator.validate(targetMap, errors);

			if (itemDomainAdaptor.getValidationErrors().hasErrors()) {
				errors.addAllErrors(itemDomainAdaptor.getValidationErrors());
			}
			
			if (errors.hasErrors()){
				//Include them into the BusinessValidationException
				//Throw this right away.
				throwBusinessValidationException(errors, "itemService.saveItem");
			}
			
			setAdditionalItemData(userInfo,item);
			
			//PreSave
			if (itemSaveBehaviors != null){
				for (ItemSaveBehavior itemSaveBehavior: itemSaveBehaviors){
					itemSaveBehavior.preSave(item, errors);
				}
			}
			
			// Pre Save for Ports.
			if (portObjectsList != null){
				for (String portObject: portObjectsList){
					Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
					IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portObject, item, errors);
					portObjects.preSave();
				}
			}

			// Delete invalid ports
			if (portObjectsList != null){
				for (String portObject: portObjectsList){
					Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
					IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portObject, item, errors);
					portObjects.deleteInvalidPorts(errors);
					portObjects.clearPortMoveData(errors);
				}
			}

			//Save the item
			itemId = saveItem(item,unit);
			
			//PostSave
			if (itemSaveBehaviors != null){
				for (ItemSaveBehavior itemSaveBehavior: itemSaveBehaviors){
					if (itemSaveBehavior.canSupportDomain(item.getClass().getName()))
						itemSaveBehavior.postSave(item, userInfo, errors);
				}
			}
			
			//Post Save for Ports.
			if (portObjectsList != null){
				for (String portObject: portObjectsList){
					Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
					IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portObject, item, errors);
					portObjects.postSave(userInfo, errors);
				}
			}

			if (null != itemFinalBehaviors) {
				for (ItemSaveBehavior itemFinalBehavior: itemFinalBehaviors) {
					itemFinalBehavior.postSave(item, userInfo, errors);
				}
			}
			
			//This is to validate double click on item save (CR Number: 49558)
			postValidateItem(item, userInfo, itemId > 0, errors);
			
			//CR52635
			if ( !isUpdate && !errors.hasErrors() ) {
				try {
					new AuditTrailListener().saveAuditDataForInsert( itemId, sessionFactory.getCurrentSession() );		
				} catch ( Exception ex ) {
					log.warn( "Exception from AuditTrailListener().saveAuditDataForInsert, itemId: " + itemId, ex );
				}
			}
			if (errors.hasErrors()){
				// if post operations have collected errors, inform the user
				throwBusinessValidationException(errors, "itemService.saveItem");
			}
		} finally {
		
			//Cleanup the valueIdDTOHolder for current thread
			//Note that this is set in the ItemObjectFactory implementation
			//during creation of "this" object.
			ValueIdDTOHolder.clearCurrent();
			
			clearCapturedItemData(existingItemId);
		}
		
		return getItemDetails(itemId, unit);
	}
}
