/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * @author prasanna
 *
 */
public class StepResolverReader implements ItemReader<String> {

	@Override
	public String read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		// DO Nothing
		return null;
	}

}
