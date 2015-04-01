package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.factory.GenericModelFactory;

/**
 * updates power panel static data
 * @author bunty
 *
 */
public class PowerPanelVpcItemUpdateStaticData implements VPCItemUpdate {

	@Autowired
	private LksCache lksCache;
	
	@Autowired
	private VPCItemUpdateHelper vpcItemUpdateHelper;
	
	@Autowired
	private GenericModelFactory powerPanelGenericModelFactory;
	
	private Long itemClassLkp;
	
	private Long itemSubClassLkp;
	
	public Long getItemClassLkp() {
		return itemClassLkp;
	}

	public void setItemClassLkp(Long itemClassLkp) {
		this.itemClassLkp = itemClassLkp;
	}

	public Long getItemSubClassLkp() {
		return itemSubClassLkp;
	}

	public void setItemSubClassLkp(Long itemSubClassLkp) {
		this.itemSubClassLkp = itemSubClassLkp;
	}

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {

		// update model
		item.setModel(powerPanelGenericModelFactory.getModel());
		
		// update class
		vpcItemUpdateHelper.updateClass(item, itemClassLkp);
		
		// update subclass
		vpcItemUpdateHelper.updateSubClass(item, itemSubClassLkp);
		
		MeItem meItem = (MeItem) item;
		
		Long highVoltageLkpValueCode = (Long) additionalParams.get(VPCLookup.ParamsKey.HIGH_VOLTAGE_LKP);// vpcItemUpdateHelper.getHighVoltageLkp(locationId);
		Long lowVoltageLkpValueCode = (Long) additionalParams.get(VPCLookup.ParamsKey.LOW_VOLTAGE_LKP); // vpcItemUpdateHelper.getLowVoltageLkp(locationId);
		
		// set line voltage to high voltage
		meItem.setLineVolts(new Long(lksCache.getLksDataUsingLkpCode(highVoltageLkpValueCode).getLkpValue()).longValue()); // config data
		
		// set phase voltage to low voltage
		meItem.setPhaseVolts(new Long(lksCache.getLksDataUsingLkpCode(lowVoltageLkpValueCode).getLkpValue()).longValue()); // config data
		
		// set phase as 3-phase WYE
		meItem.setPhaseLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseIdClass.THREE_WYE));
		
		// set rating amps to MAX
		meItem.setRatingAmps(VPCLookup.DefaultValue.Current);
		
		// set # of poles to 6
		meItem.setPolesQty(VPCLookup.DefaultValue.polesQty);
		
		// set phase a color
		meItem.setPhaseAColorLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseColor.Black));

		// set phase b color
		meItem.setPhaseBColorLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseColor.Red));

		// set phase c color
		meItem.setPhaseCColorLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseColor.Blue));

	}

}
