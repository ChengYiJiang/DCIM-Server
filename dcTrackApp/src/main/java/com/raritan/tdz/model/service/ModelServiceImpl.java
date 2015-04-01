/**
 * 
 */
package com.raritan.tdz.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.model.dto.ModelDTO;
import com.raritan.tdz.model.dto.ModelDetailDTO;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.model.home.ModelPortAdapter;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.ModelPorts;


/**
 * @author prasanna
 *
 */
public class ModelServiceImpl extends com.raritan.tdz.service.ModelServiceImpl implements ModelService {
	private ModelHome modelHome;
	
	public ModelServiceImpl(ModelHome modelHome){
		super(modelHome);
		this.modelHome = modelHome;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.model.service.ModelService#getAllMake()
	 */
	@Override
	public List<ValueIdDTO> getAllMake() {
		return modelHome.getAllMakes();
	}

	@Override
	public List<ValueIdDTO> getAllModels() {
		List<ValueIdDTO> models = modelHome.getAllModels();
		
		//Add the virtual machine to the top of the list
		ValueIdDTO specialValueId = new ValueIdDTO();
		specialValueId.setData(9999999L);
		specialValueId.setLabel("Virtual Machine");
		
		models.set(0, specialValueId);
		
		
		return models;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.model.service.ModelService#getAllModel(java.lang.Long)
	 */
	@Override
	public List<ValueIdDTO> getModels(Long mfrId, List<Integer> includeClasses, List<Integer> excludeClasses) {
		List<ValueIdDTO> models = modelHome.getModels(
				mfrId,
				prepareLongListCriteria( includeClasses ),
				prepareLongListCriteria( excludeClasses)
		);
		return models;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.model.service.ModelService#getModelDetails(java.lang.Long)
	 */
	@Override
	public Map<String, UiComponentDTO> getModelDetails(Long modelId)
			throws Throwable {
		return modelHome.getModelDetails(modelId, FlexUserSessionContext.getUser());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.model.service.ModelService#getModelDetails(java.lang.Long)
	 */
	@Override
	public List<ModelDTO> getAllModelsForMake(long mfrId, List<Integer> includeClasses, List<Integer> excludeClasses) 
			throws Throwable {
		return modelHome.getAllModelsForMake(
				mfrId,
				prepareLongListCriteria( includeClasses ),
				prepareLongListCriteria( excludeClasses )
		);
	}
	

	private List<Long> prepareLongListCriteria(List<Integer> values) {
		if (values == null) return null;
		List<Long> list = new ArrayList<Long>( values.size() );
		for (Integer i : values) {
			list.add( i.longValue() );
		}
		return list;
	}


    public ModelDetailDTO getModelDetailById(long modelId) throws Throwable {
        ModelDetails modelDetails = modelHome.getModelById(modelId);

        if (modelDetails == null)
            return null;

        ModelDetailDTO modelDetailDTO = new ModelDetailDTO(
            modelId,
            modelDetails.getModelName(),
            modelDetails.getRuHeight(),
            modelDetails.getClassLookup().getLksId(),
            modelDetails.getMounting(),
            modelDetails.getFormFactor(),
            modelDetails.getFrontImage(),
            modelDetails.getRearImage());
        return modelDetailDTO;
    }
    
    /**
     * Get all the power ports DTO 
     * @param modelId
     * @return
     * @throws ServiceLayerException
     */
	@Override
	public List<PowerPortDTO> getAllPowerPorts(Long modelId)
			throws Throwable {
		
		return modelHome.getAllPowerPort(modelId);
	}

	@Override
	public List<DataPortDTO> getAllDataPorts(Long modelId)
			throws Throwable {
		
		return modelHome.getAllDataPort(modelId);
	}

    @Override
    public List<ModelDTO> getPassiveModels() throws Throwable {
        return modelHome.getPassiveModels();
    }
}