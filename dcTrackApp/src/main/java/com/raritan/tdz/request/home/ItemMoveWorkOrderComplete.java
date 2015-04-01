package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.request.dao.RequestDAO;

/**
 * 
 * @author bunty
 *
 */

public class ItemMoveWorkOrderComplete implements RequestStageHelper {

	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws DataAccessException {

		// Complete the move operation
		requestDAO.itemMoveWorkOrderComplete(request, userInfo);

		// Archive cabinet elevation information
		requestDAO.cabinetElevationArchived(request, userInfo);
		
	}

}
