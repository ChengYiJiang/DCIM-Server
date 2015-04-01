package com.raritan.tdz.model.dao;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.model.dto.ModelDTO;

public interface ModelDAO  extends Dao<ModelDetails>{

	/**
	 * Get a model from database using mode id and lazy load it
	 * @param modelId - model Id of model to be loaded
	 * @return ModelDetails object 
	 */
	public ModelDetails loadModel(Long modelId) throws DataAccessException;
	
	/**
	 * Get a list of blade models from database that match a given make id
	 * @param makeId - make Id of for models to be loaded
	 * @return List of ModelDetails objects 
	 */
	public List<ModelDetails> getAllBladeModelForMake(long makeId);

	/**
	 * Get a list of blade models from database that match a given make id and an item class
	 * @param makeId - make Id of for models to be loaded
	 * @param classLkpValueCode - Class value code 
	 * @return List of ModelDetails objects 
	 */
	public List<ModelDetails> getAllBladeModelForMake(long makeId, long classLkpValueCode);

	/**
	 * Get a list of all models from database
	 * @param none
	 * @return List of ValueIdDTO objects 
	 */
	public List<ValueIdDTO> getAllModels();

	/**
	 * Get a list of models from database that match a given make id
	 * @param makeId - make Id of for models to be loaded
	 * @param includeClasses - List of item classes to return
	 * @param excludeClasses - List of item classes to exclude from results
	 * @return List of ValueIdDTO objects 
	 */
	public List<ValueIdDTO> getModels(Long makeId, List<Long> includeClasses, List<Long> excludeClasses);

	/**
	 * Get a list of models from database that match a given make id
	 * @param makeId - make Id of for models to be loaded
	 * @param includeClasses - List of item classes to return
	 * @param excludeClasses - List of item classes to exclude from results
	 * @return List of ModelDTO objects 
	 */
	public List<ModelDTO> getAllModelsForMake(long makeId, List<Long> includeClasses, List<Long> excludeClasses);

	/**
	 * Get model Id for a given model name
	 * @param modelName - name of model to find Id 
	 * @return Id of model 
	 */
	public Long getModelIdByName(String modelName);

	/**
	 * Get model object for a given model name
	 * @param modelName - name of model to find Id 
	 * @return A ModelDetails object 
	 */
	public ModelDetails getModelByName(String modelName);

	/**
	 * Get model object for a given model Id
	 * @param modelId - id of model 
	 * @return A ModelDetails object 
	 */
	public ModelDetails getModelById(Long modelId);

	/**
	 * Get the class value code for a given model id
	 * @param modelId - id of model
	 * @return class value code
	 */
	public Long getClassLkpValueCode(long modelId);
	
	
	public Map<String, Object> getModelMountingAndRuHeight(long modelId);
	
	public Map<String, Object> getModelMountingAndRuHeight(long modelId, List<String> projections);

}
