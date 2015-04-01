/**
 * 
 */
package com.raritan.tdz.model.home;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.model.dto.ModelDTO;

/**
 * @author prasanna
 * This represents the Models library Home layer interface 
 * for all the business logic.
 */
public interface ModelHome  extends com.raritan.tdz.home.ModelHome{
	/**
	 * Gets all the Make from models library as a name/value pair
	 * @return
	 */
	
	public List<ValueIdDTO> getAllMakes();
	
	/**
	 * Gets all the models.
	 * @return
	 */
	public List<ValueIdDTO> getAllModels();
	
	/**
	 * Get models by criteria.
	 * @param mfrId optional make Id (if null, get models for all makes)
	 * @param includeClasses optional list of item classes to include
	 * @param excludeClasses optional list of item classes to exclude
	 * @return
	 */
	public List<ValueIdDTO> getModels(Long mfrId, List<Long> includeClasses, List<Long> excludeClasses);
	
	
	/**
	 * Gets the details of a model given model id.
	 * This will be based on what is defined in the UiView template
	 * @param modelId
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws JAXBException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws Throwable 
	 */
	public Map<String, UiComponentDTO> getModelDetails(Long modelId, UserInfo userInfo)
			throws SecurityException, IllegalArgumentException, JAXBException, 
			NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, NoSuchFieldException, Throwable;
	/**
	 * Create Model from ModelDetails
	 * @param ModelDetals
	 * @return
	 */
	public ModelDetails createModel(ModelDetails modelDetails);

	/**
	 * @param modelName
	 * @return modelId 
	 */
	public Long getModelIdByName(String modelName);

	/**
	 * Get all model details for the specified make. To be used as a data source with the Advanced DropDown on the client.
	 * @param mfrId the manufacturer/make id
	 * @param includeClasses optional list of included item classes to filter the model list
	 * @param excludeClasses optional list of excluded item classes to filter the model list
	 * @return list of modelDTOs
	 */
	public List<ModelDTO> getAllModelsForMake(long mfrId, List<Long> includeClasses, List<Long> excludeClasses);

	/**
	 * Get a list of data port for model id
	 * @param modelId - model id for which port information will be returned
	 */
	public List<DataPortDTO> getAllDataPort(long modelId) throws DataAccessException; 
	
	/**
	 * Get a list of  power port for model id
	 * @param modelId - model id for which port information will be returned
	 * @throws DataAccessException 
	 */
	public List<PowerPortDTO> getAllPowerPort(long modelId) throws DataAccessException;

	public ModelDetails getModelByName(String modelName);
	public ModelDetails createPhatomCabinetModel(ModelDetails itemModel); 
	public void deleteModel(ModelDetails model);

	public ModelDetails getModelById(Long modelId);

    /**
     * Get all passive models.
     * 
     * @return a list of modelDTOs
     *
     * @see #getAllModelsForMake(long, List, List)
     */
    public List<ModelDTO> getPassiveModels();
}