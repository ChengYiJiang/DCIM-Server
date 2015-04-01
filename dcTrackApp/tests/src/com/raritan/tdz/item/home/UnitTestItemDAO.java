/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 * This interface covers all the required DAO kind of functionality that 
 * we need for handling item creation to deleting an item.
 */
public interface UnitTestItemDAO {
	/**
	 * 
	 * @return
	 * @throws Throwable
	 */
	DataCenterLocationDetails createUnitTestLocation() throws Throwable;
	
	/**
	 * 
	 * @param location
	 * @throws Throwable
	 */
	void deleteUnitTestLocation(DataCenterLocationDetails location) throws Throwable;
	
	/**
	 * Creates Cabinent
	 * @param locationId
	 * @param modelName
	 * @param additionalParams - You can provide additional valueIdDTO (uiId/data).
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	Map<String, UiComponentDTO> createCabinet(Long locationId, String modelName, List<ValueIdDTO> additionalParams) 
			throws ClassNotFoundException,	BusinessValidationException, Throwable;
	
	
	
	/**
	 * 
	 * @param location
	 * @param cabinetId
	 * @param uPosition
	 * @param modelName
	 * @param additionalParams - You can provide additional valueIdDTO (uiId/data).
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	Map<String,UiComponentDTO> createStackableItem(Long location,
			Long cabinetId, long uPosition, String modelName, List<ValueIdDTO> additionalParams)
			throws ClassNotFoundException, BusinessValidationException,	Throwable;
	
	/**
	 * 
	 * @param location
	 * @param cabinetId
	 * @param uPosition
	 * @param modelName
	 * @param additionalParams - You can provide additional valueIdDTO (uiId/data).
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	Map<String,UiComponentDTO> createStandardItem(Long location,
			Long cabinetId, long uPosition, String modelName, List<ValueIdDTO> additionalParams)
			throws ClassNotFoundException, BusinessValidationException,	Throwable;
	
	/**
	 * 
	 * @param itemId
	 * @return
	 * @throws Throwable
	 */
	Map<String,UiComponentDTO> getItemDTO(long itemId) throws Throwable;
	
	/**
	 * 
	 * @param valueIdDTOList
	 * @return
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	Map<String,UiComponentDTO> updateItem(List<ValueIdDTO> valueIdDTOList)
			throws BusinessValidationException, ClassNotFoundException, Throwable;
	
	/**
	 * 
	 * @param itemId
	 * @throws BusinessValidationException 
	 * @throws DataAccessException 
	 * @throws Throwable 
	 */
	void deleteItem(long itemId) throws DataAccessException, BusinessValidationException, Throwable;
	
	/**
	 * 
	 * @param label
	 * @param origDTO
	 * @param value
	 * @param changeId
	 * @return
	 */
	Map<String,UiComponentDTO> updateValue(String label, Map<String,UiComponentDTO> origDTO, Object value, boolean changeId);
	
	/**
	 * 
	 * @param itemDTO
	 * @return
	 * @throws Throwable
	 */
	List<ValueIdDTO> getItemUpdateValueIdDTOList(Map<String,UiComponentDTO> itemDTO) throws Throwable;
	
	/**
	 * Creates valueIdDTO given a uiId as label and corresponding data.
	 * @param label
	 * @param data
	 * @return
	 */
	ValueIdDTO createValueIdDTOObj(String label, Object data);
	
	/**
	 * Given model get modelDetails from database.
	 * @param modelName
	 * @return
	 */
	public ModelDetails getModel(String modelName);

	public void setSf(SessionFactory sf);
	
}
