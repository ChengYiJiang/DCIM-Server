package com.raritan.tdz.model.factory;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.ModelMfrDetails;
import com.raritan.tdz.mfr.dao.ModelMfrDetailsDAO;
import com.raritan.tdz.model.dao.ModelDAO;

public class GenericModelFactoryImpl implements GenericModelFactory {

	@Autowired
	private ModelMfrDetailsDAO modelMfrDetailsDAO;
	
	@Autowired
	private ModelDAO modelDAO;
	
	@Autowired
	private LksCache lksCache;
	
	private Long modelClass;
	
	private String mounting;
	
	private String formFactor;
	
	
	
	public GenericModelFactoryImpl(Long modelClass, String mounting,
			String formFactor) {

		this.modelClass = modelClass;
		this.mounting = mounting;
		this.formFactor = formFactor;
	}

	@Override
	public ModelDetails getModel() {
		
		String modelName = getModelName();
		ModelDetails model = modelDAO.getModelByName(modelName);
		if (null != model) return model;
		
		return createModel();
	}
	
	private ModelDetails createModel() {
		
		ModelDetails model = new ModelDetails();
		ModelMfrDetails mfr = modelMfrDetailsDAO.getMfrByName("Generic");
		
		model.setModelName(getModelName());
		model.setSysModelMfrName(mfr.getMfrName());
		model.setModelMfrDetails(mfr);
		model.setClassLookup(lksCache.getLksDataUsingLkpCode(modelClass)); 
		model.setMounting(mounting);
		model.setFormFactor(formFactor);
		model.setCreationDate(new Timestamp(new Date().getTime()));
		model.setCreatedBy("dctrack");
		model.setRuHeight(1);

		modelDAO.create(model);
		
		return model;
	}
	
	private String getModelName() {
		LksData classLks = lksCache.getLksDataUsingLkpCode(modelClass);
		String modelName = "Generic " + classLks.getLkpValue();
		
		return modelName;
	}

}
