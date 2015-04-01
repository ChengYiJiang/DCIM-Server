package com.raritan.tdz.port.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.GlobalUtils;

public class PowerPortObjectCollection extends PortObjectCollection implements ApplicationContextAware {

	/*@Autowired
	private PowerConnDAO powerConnectionDAO;*/

	private ApplicationContext applicationContext;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;
	
	private String powerSupplyCapacityValidator;
	
	@Autowired(required=true)
	PortCollectionHelper<PowerPort> powerPortObjectCollectionHelper;
	
	public PowerPortObjectCollection(IPortObjectFactory portObjectFactory) {
		super(portObjectFactory);
	}
	
	public String getPowerSupplyCapacityValidator() {
		return powerSupplyCapacityValidator;
	}

	public void setPowerSupplyCapacityValidator(String powerSupplyCapacityValidator) {
		this.powerSupplyCapacityValidator = powerSupplyCapacityValidator;
	}

	@Override
	public void init(Object itemObj, Errors errors) {
		setItem((Item)itemObj);
		ports = powerPortObjectCollectionHelper.init(itemObj, getPortObjectFactory(), errors);
	}
	
	@Override
	public List<Long> getDeleteIds() {
		// get deleted power port ids 
		return powerPortObjectCollectionHelper.getDeleteIds(item);
		
	}
		
	private void updateRedundancy() {
		/* Get redundancy Information */
		long itemClass = item.getClassLookup().getLkpValueCode();
		boolean updateRedundancyApply = (itemClass == SystemLookup.Class.DEVICE ||
											itemClass == SystemLookup.Class.NETWORK ||
											itemClass == SystemLookup.Class.PROBE) && 
											(item instanceof ItItem)  &&
											(null != getItem().getPowerPorts() && getItem().getPowerPorts().size() > 0);
		
		if (!updateRedundancyApply) {
			return;
		}
		// update item's redundancy
		ItItem itItem = (ItItem) itemDAO.getItem(item.getItemId());
		String psRedundancy =  itItem.getPsredundancy();
		if ((null == psRedundancy || psRedundancy.length() == 0 || psRedundancy.equals("null"))) {
			int psSize = ports.size();
			psRedundancy = (psSize >=2) ? "N + 1" : ((psSize ==1) ? "N" : null);
			itItem.setPsredundancy(psRedundancy);
			itemDAO.saveItem(itItem);
		}
		
		int lastIdx = psRedundancy != null ? psRedundancy.lastIndexOf("+") : 0; 
		int newR = lastIdx > 0 ? (GlobalUtils.isNumeric(psRedundancy.substring(lastIdx+1).trim()) ? (Integer.parseInt(psRedundancy.substring(lastIdx+1).trim())) : 0 ): 0; /* new num of redundant ports */
		int oldR = 0;
		
		// update ports redundancy
		Set<PowerPort> powerPortList = itItem.getPowerPorts();
		if (null == powerPortList) {
			return;
		}
		for (PowerPort pp: powerPortList) {
			if (pp.getIsRedundant()) {
				oldR++;
			}
		}

		
		int diffR = newR - oldR;

		// more redundant ports
		if (diffR > 0) {
			int redundancyAppliedCount = newR;
			for (PowerPort powerPort: powerPortList) {
				if (redundancyAppliedCount == 0) {
					break;
				}
				if (powerPort.getIsRedundant()) {
					if (!powerPort.getUsed()) { 
						redundancyAppliedCount--;
						continue;
					}
				}
				if (!powerPort.getUsed() && !powerPort.getIsRedundant()) {
					powerPort.setIsRedundant(true);
					redundancyAppliedCount--;
				}
			}
			if (redundancyAppliedCount > 0) {
				for (PowerPort powerPort: powerPortList) {
					if (redundancyAppliedCount == 0) {
						break;
					}
					if (!powerPort.getIsRedundant()) {
						powerPort.setIsRedundant(true);
						redundancyAppliedCount--;
					}
				}
			}
		}
		
		// less redundant ports
		else if (diffR < 0) {
			int nonRedundancyAppliedCount = -diffR;
			for (PowerPort powerPort: powerPortList) {
				if (nonRedundancyAppliedCount == 0) {
					break;
				}
				if (powerPort.getUsed() && powerPort.getIsRedundant()) {
					powerPort.setIsRedundant(false);
					nonRedundancyAppliedCount--;
				}
			}
			if (nonRedundancyAppliedCount > 0) {
				for (PowerPort powerPort: powerPortList) {
					if (nonRedundancyAppliedCount == 0) {
						break;
					}
					if (powerPort.getIsRedundant()) {
						powerPort.setIsRedundant(false);
						nonRedundancyAppliedCount--;
					}
				}
			}
		}
		itemDAO.saveItem(itItem);
		return;
	}

	@Override
	public IPortObject getDetachedPort(Long portId, Errors errors) {
		PowerPort pp = powerPortDAO.loadEvictedPort(portId);
		IPortObject po = getPortObjectFactory().getPortObject(pp, errors);
		return po;
		
	}

	@Override
	public void validateSortOrder(Errors errors) {
		List<String> nonUniquePortTypes = powerPortObjectCollectionHelper.getPortTypeOfNonUniqueSortOrder(item);
		
		for (String portType: nonUniquePortTypes) {
			Object errorArgs[]  = { portType };
			errors.rejectValue("tabPowerPorts", "PortValidator.powerPortSortOrderNotUnique", errorArgs, "Power Port order is not unique");
		}
	}
	
	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
		super.postSave(userInfo, errors);
		updateRedundancy();
	}
	
	private void validateRedundancy(Errors errors) {
		long itemClass = getItem().getClassLookup().getLkpValueCode();
		boolean updateRedundancyApply = (itemClass == SystemLookup.Class.DEVICE ||
											itemClass == SystemLookup.Class.NETWORK ||
											itemClass == SystemLookup.Class.PROBE) && 
											(item instanceof ItItem) &&
											(null != getItem().getPowerPorts() && getItem().getPowerPorts().size() > 0);
		
		if (!updateRedundancyApply) {
			return;
		}
		String psRedundancy =  ((ItItem) getItem()).getPsredundancy();
		if ((null == psRedundancy || psRedundancy.length() == 0 || psRedundancy.equals("null")) || !psRedundancy.trim().startsWith("N")) {
			Object errorArgs[]  = { };
			errors.rejectValue("tabPowerPorts", "PortValidator.invalidRedundancy", errorArgs, "Power Port redundancy is invalid");
			return;
		}
		int psSize = ports.size();
		int lastIdx = psRedundancy != null ? psRedundancy.lastIndexOf("+") : 0; 
		int newR = lastIdx > 0 ? (GlobalUtils.isNumeric(psRedundancy.substring(lastIdx+1).trim()) ? (Integer.parseInt(psRedundancy.substring(lastIdx+1).trim())) : 0 ): 0; /* new num of redundant ports */
		if (lastIdx < 0 && psRedundancy.trim().equals("N")) lastIdx = 0;
		if (newR >= psSize || lastIdx < 0) {
			Object errorArgs[]  = { };
			errors.rejectValue("tabPowerPorts", "PortValidator.invalidRedundancy", errorArgs, "Power Port redundancy is invalid");
			return;
		}
	}
	
	@Override
	public void validate(Errors errors) {
		super.validate(errors);
		
		// Validate redundancy
		validateRedundancy(errors);
		
		// Validate Power Capacity
		validatePowerCapacity(errors);
		
	}

	@Override
	protected void updateSortOrder(Errors errors) {
		powerPortObjectCollectionHelper.updateSortOrderByPortSubclass(item/*, itemDAO*/, errors);
	}

	@Override
	public void preValidateUpdates(Errors errors) {
		updateSortOrder(errors);
		for (IPortObject port: ports) {
			port.preValidateUpdates(errors);
		}
	}
	
	private void validatePowerCapacity(Errors errors) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(errors.getObjectName(), getItem());
		
		getValidator(powerSupplyCapacityValidator).validate(targetMap, errors);
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	private Validator getValidator(String validator) {
		Validator validatorObj = (Validator) applicationContext.getBean(validator);

		return validatorObj;
	}

}
