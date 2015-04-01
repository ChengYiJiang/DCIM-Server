package com.raritan.tdz.item.home.itemObject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.home.ItemHome;

public class ItemNameGeneratorImpl implements ItemNameGenerator {

	private List<Long> supportStates;
	
	@Autowired
	protected ItemHome itemHome;
	
	public List<Long> getSupportStates() {
		return supportStates;
	}

	public void setSupportStates(List<Long> supportStates) {
		this.supportStates = supportStates;
	}

	private boolean stateSupported(long state) {
		for (Long supportState: supportStates) {
			if (supportState.longValue() == state) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean shouldGenerateName(Item item) {

		boolean itemStateValidForNameGeneration = (item.getStatusLookup() != null && null != supportStates && 
				stateSupported(item.getStatusLookup().getLkpValueCode().longValue()));
		
		boolean itemNameValidForNameGeneration = (item.getItemName() == null || item.getItemName().isEmpty());
		
		return (itemStateValidForNameGeneration && itemNameValidForNameGeneration);
		
	}

	@Override
	public void generateName(Item item) {
		if (!shouldGenerateName(item)) return;
		
		if (itemHome != null && (item.getItemName() == null || item.getItemName().isEmpty()) ) {
			String generatedName = "";
			try {
				generatedName = itemHome.getGeneratedStorageItemName().toUpperCase();
				item.setItemName(generatedName);
			} catch (Throwable ex) {
				// Cannot generate name, let the validation for the required field raise the exception
				/*Object[] errorArgs = { };
				errors.rejectValue("tiName", "ItemValidator.fieldRequired.cannotGeneratorName", errorArgs, "Name cannot be generated.");*/
			}
			
		}

	}

}
