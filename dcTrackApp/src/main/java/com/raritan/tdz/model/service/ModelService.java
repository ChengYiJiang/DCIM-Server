/**
 * 
 */
package com.raritan.tdz.model.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.model.dto.ModelDTO;
import com.raritan.tdz.model.dto.ModelDetailDTO;

/**
 * @author prasanna
 *
 */
public interface ModelService {
	
	/**
	 * Gets all the Make from models library as a name/value pair
	 * @return
	 */
	
	public List<ValueIdDTO> getAllMake();
	
	/**
	 * Gets all the model
	 * @param mfrId
	 * @return
	 */
	public List<ValueIdDTO> getAllModels();
	
	/**
	 * Gets all the models for a given make and item class.
	 * @param mfrId the manufacturer/make ID
	 * @param classValueCode the item class code
	 * @return a list of models as ValueIdDTO objects
	 */
	public List<ValueIdDTO> getModels(Long mfrId, List<Integer> includeClasses, List<Integer> excludeClasses);
	
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
	public Map<String, UiComponentDTO> getModelDetails(Long modelId)
			throws SecurityException, IllegalArgumentException, JAXBException, 
			NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, NoSuchFieldException, Throwable;

	/**
	 * Get List of ModelDTO for a given make
	 * @param mfrId the make/manufacturer ID
	 * @param includeClasses optional list of classes to filter the model list
	 * @param excludeClasses optional list of classes to exclude from the model list
	 * @return a list of modelDTOs
	 * @throws Throwable
	 */
	public List<ModelDTO> getAllModelsForMake(long mfrId, List<Integer> includeClasses, List<Integer> excludeClasses) throws Throwable;


    public ModelDetailDTO getModelDetailById(long modelId) throws Throwable;

    /**
     * Get all power port DTOs
     * @param modelId
     * @return
     * @throws Throwable
     */
	List<PowerPortDTO> getAllPowerPorts(Long modelId) throws Throwable;

	/**
	 * Get all data port DTOs
	 * @param modelId
	 * @return
	 * @throws Throwable
	 */
	List<DataPortDTO> getAllDataPorts(Long modelId) throws Throwable;

    /**
     * Get all passive models.
     * 
     * @return a list of modelDTOs
     * @throws Throwable
     * 
     * @see #getAllModelsForMake(long, List, List)
     */
    public List<ModelDTO> getPassiveModels() throws Throwable;
}