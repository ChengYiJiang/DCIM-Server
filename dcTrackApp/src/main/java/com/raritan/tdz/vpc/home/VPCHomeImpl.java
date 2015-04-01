/**
 *
 */
package com.raritan.tdz.vpc.home;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.ApplicationSetting;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.settings.dao.ApplicationSettingsDAO;
import com.raritan.tdz.vpc.factory.VPCCircuit;
import com.raritan.tdz.vpc.factory.VPCPowerChainFactory;

/**
 * @author prasanna
 *
 */
public class VPCHomeImpl implements VPCHome {

	@Autowired
	private VPCPowerChainFactory vpcAPowerChainFactory;

	@Autowired
	private VPCPowerChainFactory vpcBPowerChainFactory;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private ApplicationSettingsDAO applicationSettingsDAO;

	@Autowired
	private LksCache lksCache;

	@Autowired
	private VPCCircuit powerOutletVPCCircuit;

	@Autowired
	private ItemDAO itemDAO;
	
	@Resource(name="validationInformationCodes")
	protected List<String> validationInformationCodes;


	/* (non-Javadoc)
	 * @see com.raritan.tdz.vpc.home.VPCHome#createVPC(java.lang.Long)
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public void create(Long locationId, UserInfo userInfo) throws BusinessValidationException {

		Errors errors = getErrorsObject(this.getClass());
		vpcAPowerChainFactory.create(locationId, errors);
		vpcBPowerChainFactory.create(locationId, errors);
		updateSettingDefaultValue(locationId);

		if (errors.hasErrors()){
			BusinessValidationException.throwBusinessValidationException(errors,
					new HashMap<String,BusinessValidationException.WarningEnum>(),
					validationInformationCodes, messageSource, this.getClass(), null);
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vpc.home.VPCHome#deleteVPC(java.lang.Long)
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public void delete(Long locationId, UserInfo userInfo) throws ClassNotFoundException, BusinessValidationException, Throwable {
		Errors errors = getErrorsObject(this.getClass());

		vpcAPowerChainFactory.delete(locationId, userInfo, errors);
		vpcBPowerChainFactory.delete(locationId, userInfo, errors);
		deleteSetting(locationId);

		if (errors.hasErrors()){
			BusinessValidationException.throwBusinessValidationException(errors,
					new HashMap<String,BusinessValidationException.WarningEnum>(),
					validationInformationCodes, messageSource, this.getClass(), null);
		}

	}

	@Override
	public void update(Long locationId, UserInfo userInfo) throws ClassNotFoundException, BusinessValidationException, Throwable {

		delete(locationId, userInfo);

		create(locationId, userInfo);

	}

	private void updateSettingDefaultValue(Long locationId) {
		Map<Long, Boolean> appSettingSet = new HashMap<Long, Boolean>();
		appSettingSet.put(SystemLookup.ApplicationSettings.VPC_SETTINGS.ENABLED, false);

		for (Map.Entry<Long, Boolean> entrySet: appSettingSet.entrySet()) {

			Long appSettingLkpValueCode = entrySet.getKey();

			ApplicationSetting appSetting = applicationSettingsDAO.getAppSetting(appSettingLkpValueCode, locationId);
			if (null == appSetting) {
				appSetting = new ApplicationSetting();
				appSetting.setLksData(lksCache.getLksDataUsingLkpCode(appSettingLkpValueCode));
				appSetting.setLocationId(locationId);
				appSetting.setValue("false");
				applicationSettingsDAO.create(appSetting);
			}
		}

	}

	private void deleteSetting(Long locationId) {

		Map<Long, Boolean> appSettingSet = new HashMap<Long, Boolean>();
		appSettingSet.put(SystemLookup.ApplicationSettings.VPC_SETTINGS.ENABLED, false);

		for (Map.Entry<Long, Boolean> entrySet: appSettingSet.entrySet()) {

			Long appSettingLkpValueCode = entrySet.getKey();

			ApplicationSetting appSetting = applicationSettingsDAO.getAppSetting(appSettingLkpValueCode, locationId);
			if (null != appSetting) {
				applicationSettingsDAO.delete(appSetting);
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vpc.home.VPCHome#getVPCPartialCircuit(java.lang.Long, java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public PowerCircuit getVPCPartialCircuit(String vpcPath, Long locationId, Long inputPortId, UserInfo userInfo)
			throws BusinessValidationException, NumberFormatException, DataAccessException {

		Errors errors = getErrorsObject(this.getClass());

		PowerCircuit circuit = powerOutletVPCCircuit.create(inputPortId, locationId, vpcPath, errors, userInfo);

		// do not flush this transaction and return circuit
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

		circuit.setCircuitId(-1L);

		return circuit;
	}

	@Override
	public PowerCircuit createVPCCircuit(String vpcPath, Long locationId, Long inputPortId, UserInfo userInfo)
			throws BusinessValidationException, NumberFormatException, DataAccessException {

		Errors errors = getErrorsObject(this.getClass());

		return powerOutletVPCCircuit.create(inputPortId, locationId, vpcPath, errors, userInfo);

	}

	@Override
	public PowerPort createPowerOutletPortAndConnection(Long srcPortId, Long locationId, String vpcChain, Errors errors) {

		return powerOutletVPCCircuit.createPortAndConnection(srcPortId, locationId, vpcChain, errors);

	}

	@Override
	public PowerPort createPowerOutletPort(Long srcPortId, Long locationId, String vpcChain, Errors errors) {

		return powerOutletVPCCircuit.createPort(srcPortId, locationId, vpcChain, errors);

	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public PowerPort createPowerOutletPort(Long itemId, Long srcPortId, Errors errors) {

		MeItem item = (MeItem) itemDAO.loadItem(itemId);
		Long locationId = item.getDataCenterLocation().getDataCenterLocationId();
		String vpcChain = item.getChainLabel();

		if (null == errors) {
			errors = getErrorsObject(this.getClass());
		}

		return powerOutletVPCCircuit.createPortAndConnection(srcPortId, locationId, vpcChain, errors);

	}

	private void deletePowerPort(Item item) {

		boolean skipFirst = true;
		boolean update = false;

		Set<PowerPort> ppSet = item.getPowerPorts();
		if (ppSet.size() == 0 || ppSet.size() == 1) return;

		PowerPort pp = null;
		Iterator<PowerPort> itr = ppSet.iterator();
		while (itr.hasNext()) {
			pp = itr.next();
			if (null != pp.getPortId() && !pp.getUsed()) {
				if (skipFirst) {
					skipFirst = false;
				}
				else {
					itr.remove();
					update = true;
				}
			}
		}

		if (update) {
			itemDAO.update(item);
		}
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void clearPowerOutletPort() {

		List<Item> powerOutlets = itemDAO.vpcPowerOutlets();

		for (Item powerOutlet: powerOutlets) {

			// clearUnusedPorts(powerOutlet, powerOutlet.getPowerPorts());
			deletePowerPort(powerOutlet);

		}

	}

	private MapBindingResult getErrorsObject(Class<?> errorBindingClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, errorBindingClass.getName());
		return errors;
	}


}
