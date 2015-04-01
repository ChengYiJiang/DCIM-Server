/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import com.raritan.tdz.dctimport.dto.DCTImport;

import static com.raritan.tdz.dctimport.utils.DCTImportUtil.*;

/**
 * @author prasanna
 *
 */
public class DCTImportTokenizer extends DelimitedLineTokenizer {
	
	private Class<? extends DCTImport> dctImportDomainClass;

	public Class<? extends DCTImport> getDctImportDomainClass() {
		return dctImportDomainClass;
	}

	public void setDctImportDomainClass(
			Class<? extends DCTImport> dctImportDomainClass) throws ClassNotFoundException {
		this.dctImportDomainClass = dctImportDomainClass;
		List<String> names = new ArrayList<String>();
		names.add("operation");
		names.add("objecttype");
		names.addAll(getNames(dctImportDomainClass));
		super.setNames(Arrays.copyOf(names.toArray(),names.toArray().length,String[].class));
	}
}
