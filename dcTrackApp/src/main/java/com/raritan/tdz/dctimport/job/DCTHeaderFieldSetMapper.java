/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * @author prasanna
 *
 */
public class DCTHeaderFieldSetMapper  implements FieldSetMapper<FieldSet> {
	private String originalHeader;
	
	private String header;
	
	private boolean isUsed = false;
	
	private final Map<String,String> originalHeaderMap = new LinkedHashMap<String, String>();
	
	
	public String getHeader() {
		return header;
	}

	

	public boolean isUsed() {
		return isUsed;
	}



	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	


	public String getOriginalHeader() {
		return originalHeader;
	}



	public void setOriginalHeader(String originalHeader) {
		this.originalHeader = originalHeader;
	}

	public String getOriginalHeader(String modifiedHeader){
		return originalHeaderMap.get(modifiedHeader);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.item.file.FieldSetMapper#mapLine(org.springframework
	 * .batch.io.file.FieldSet)
	 */
    @Override
	public FieldSet mapFieldSet(FieldSet fs) {
    	isUsed = false;
    	//header = fs.toString().replaceAll("\\[|\\]", "");
    	StringBuilder headerBuilder = new StringBuilder();
    	String[] names = fs.getValues();
    	for (String name:names){
    		if (name.contains(","))
    			headerBuilder.append("\"");
    		headerBuilder.append(name.replaceAll("\\[|\\]", ""));
    		if (name.contains(","))
    			headerBuilder.append("\"");
    		headerBuilder.append(",");
    	}
    	header = headerBuilder.toString().contains(",") ? headerBuilder.substring(0,headerBuilder.lastIndexOf(",")) : headerBuilder.toString();
    	setupOriginalHeaderMap();
		return fs;
	}
    
    private void setupOriginalHeaderMap(){
    	DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    	String[] originalHeaderCols = tokenizer.tokenize(originalHeader).getValues();
    	FieldSet fieldSet = tokenizer.tokenize(header);
    	String[] headerCols = fieldSet.getValues();
    	
    	if (originalHeaderCols.length == headerCols.length){
    		int cnt = 0;
    		for (String headerCol:headerCols){
    			originalHeaderMap.put(headerCol, originalHeaderCols[cnt++]);
    		}
    	}
    }



	public Map<String, String> getOriginalHeaderMap() {
		return originalHeaderMap;
	}
}
