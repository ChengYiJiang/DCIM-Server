package com.raritan.tdz.move.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;

public interface ItemMoveHelper {

	/**
	 * get the errors when making a move request on the parent and children has pending requests
	 * @param itemId - parent item
	 * @param errors
	 * @return different errors object, it will not update the 'errors' object passed 
	 */
	public Errors getChildrenRequestErrors(Long itemId, Errors errors);

	/**
	 * get the errors when making a move request on the children and parent has pending requests
	 * @param item
	 * @param errors
	 * @return different errors object, it will not update the 'errors' object passed
	 */
	public Errors getParentRequestErrors(Item item, Errors errors);

	/**
	 * informs if the cabinet is changed for the item
	 * @param item
	 * @return
	 */
	public boolean isCabinetChanged(Item item);

	/**
	 * return errors when placing in moving cabinet
	 * @param item
	 * @param refErrors
	 * @param getErrorUnconditionally 
	 * @return
	 */
	public Errors getPlacementInMoveCabinetError(Item item, Errors refErrors, boolean getErrorUnconditionally);

	/**
	 * informs of the chassis placement is changed
	 * @param item
	 * @return
	 */
	public boolean isChassisChanged(Item item);

	/**
	 * return errors when placing in moving chassis
	 * @param item
	 * @param refErrors
	 * @param getErrorUnconditionally 
	 * @return
	 */
	public Errors getPlacementInMoveChassisError(Item item, Errors refErrors, boolean getErrorUnconditionally);

	/**
	 * check if the chassis is changed for the moving blade
	 * @param item
	 * @return
	 */
	public boolean isMovingBladeChassisChanged(Item item);

	/**
	 * get the list of reservation in the children item for a given parent
	 * @param parentItemId
	 * @param errors
	 * @return
	 */
	public Errors getChildrenReservationErrors(Long parentItemId, Errors errors);

}
