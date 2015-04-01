/**
 * 
 */
package com.raritan.tdz.item.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author prasanna
 * This annotation is used to only indicate
 * the status that the item will be going into
 * for a specified request. This annotation 
 * can be used in validation code where we need
 * the new status that the item is going to be when
 * the request is created.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AllowableItemStatusForRequest {
	long[] statusLkpValueCodes();
}
