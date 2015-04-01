/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author prasanna
 * This annotation will be used in the Import Domain to map the
 * header value with the field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Header {
	String value();
}
