/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import java.util.List;

import org.springframework.integration.Message;

/**
 * @author prasanna
 *
 */
public interface ExportColumnCriteriaTransformer {
	public Message<List<Object>> transform(Message<List<Object>> argList);
}
