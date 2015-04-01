/**
 * 
 */
package com.raritan.tdz.dctimport.integration.routers;


/**
 * @author prasanna
 * This resolver will be used to route
 * to the right normalizer transformer - HeaderNormalizer vs
 * OperationObjectTypeNormalizer
 */
public interface ImportNormalizerResolver {
	public final static String HEADER_NORMALIZER = "HeaderNormalizer"; 
	public final static String OPERATION_OBJECT_TYPE_NORMALIZER = "OperationObjectTypeNormalizer";
	public final static String PASSTHROGUH_NORMALIZER = "PassThroughNormalizer";
	public static final String HEADER_PATTERN = "^#.*Operation.*Object.*";
	public static final String OPERATION_OBJECT_PATTERN = "^ADD.*|^EDIT.*|^DELETE.*|^UNMAP.*";
	
	/**
	 * This will resolve to the correct normalizer based
	 * on the line in the message
	 * @param line Line with which this needs to be resolved
	 * @return
	 */
	public String resolve(String line);
}
