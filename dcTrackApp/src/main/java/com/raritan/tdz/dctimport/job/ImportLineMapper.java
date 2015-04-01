/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.AbstractLineTokenizer;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.IncorrectTokenCountException;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.DCTImportHeaderMapCache;
import com.raritan.tdz.dctimport.integration.exceptions.HeaderNotFoundException;
import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.dctimport.integration.exceptions.IncorrectHeaderException;
import com.raritan.tdz.dctimport.logger.ImportLogger;


/**
 * @author prasanna
 *
 */
public class ImportLineMapper<T> implements LineMapper<T>, InitializingBean {
	
	private PatternMatchingCompositeLineTokenizer tokenizer = new PatternMatchingCompositeLineTokenizer();

	private PatternMatcher<FieldSetMapper<T>> patternMatcher;
	
	private Map<String,FieldSetMapper<T>> fieldSetMappers = new HashMap<String, FieldSetMapper<T>>();
	private Map<String,LineTokenizer> tokenizers = new HashMap<String, LineTokenizer>();
	private Map<String,DCTImport> importDTOs = new HashMap<String, DCTImport>();
	
	private Errors importLineMapperErrors;
	
	private ImportErrorHandler importErrorHandler;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ImportLogger importLogger;
	
	@Resource(name="importHeaderMapCache")
	private Map<String, DCTImportHeaderMapCache> importHeaderMapCache;
	


	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.tokenizer.afterPropertiesSet();
		Assert.isTrue(this.patternMatcher != null, "The 'fieldSetMappers' property must be non-empty");
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.item.file.LineMapper#mapLine(java.lang.String, int)
	 */
	@Override
	public T mapLine(String line, int lineNumber) throws Exception {
		
		importLineMapperErrors = new MapBindingResult(new HashMap<String, String>(), "importValidationErrors");
		
		//Since the heading row also happens to be a comment line, we cannot
		//use the comment property on the line reader. So, we ignore them here
		//and just process the header.
		String tokens[] = {"Ignore Line"};
		T mappedFieldSet = (T) new DefaultFieldSet(tokens);
		
		
		if (line.matches("^#.*|^,+|^\"#.*") && !line.matches("operation.object") || line == null || line.isEmpty())
			return mappedFieldSet;
		
		//Setup Names
		try {
			setupNames(line);
		} catch (IncorrectHeaderException e){
			if (importLineMapperErrors != null){
				String[] currentLineTokens = importLogger != null && importLogger.getCurrentOriginalLine() != null ?importLogger.getCurrentOriginalLine().split(","):null;
				String objectType = currentLineTokens != null && currentLineTokens.length > 2 ? currentLineTokens[1]: "Unknown";
				Object[] errorArgs = {objectType,e.getHeaderName()};
				importLineMapperErrors.reject("Import.incorrectHeaders",errorArgs,"Incorrect Header detected: " + lineNumber);
			}
			e.printStackTrace();
		} catch (HeaderNotFoundException e){
			if (importLineMapperErrors != null){
				importLineMapperErrors.reject("Import.headerNotFound",null,"No Header found " + lineNumber);
			}
			e.printStackTrace();
		}
		
		//If not process data rows and map them.
		tokens[0] = "Error Line";
		mappedFieldSet = (T) new DefaultFieldSet(tokens);
		
		mappedFieldSet = processDataRow(line, lineNumber, mappedFieldSet);
		
		if (importLineMapperErrors.hasErrors())
			importErrorHandler.handleLineErrors(importLineMapperErrors);
		
		return mappedFieldSet;
	}

	/**
	 * Process the data rows and map them to the beans.
	 * @param line
	 * @param lineNumber
	 * @param mappedFieldSet
	 * @return
	 */
	private T processDataRow(String line, int lineNumber, T mappedFieldSet) {
		
		try {
			mappedFieldSet = patternMatcher.match(line).mapFieldSet(this.tokenizer.tokenize(line));
		} catch (IncorrectTokenCountException e){
			if (importLineMapperErrors != null){
				Object[] errorArgs = {line};
				importLineMapperErrors.reject("Import.token.error",errorArgs,"There is a token error in line: " + lineNumber);
			}
			e.printStackTrace();
		} catch (BindException e) {
			// TODO:: We could use the existing error code which is in the BindException and apply a property in our exception properties file 
			// to display what we want. However, this is cumbersome. So, may be this is okay for now.
			if (importLineMapperErrors != null){
				for (ObjectError error: e.getAllErrors()) {
					processFieldBindError(line, lineNumber, error);
				}
			}
			e.printStackTrace();
		} catch (IllegalStateException e){
			if (importLineMapperErrors != null){
				Object[] errorArgs = {line,e.getMessage()};
				importLineMapperErrors.reject("Import.file.error",errorArgs,"Invalid File");
			}
			//mappedFieldSet = null;
		} 
		return mappedFieldSet;
	}

	private void processFieldBindError(String line, int lineNumber, ObjectError error) {
		
		DCTHeaderFieldSetMapper headerFieldSetMapper = (DCTHeaderFieldSetMapper) fieldSetMappers.get("operation*");
		Map<String, String> headerMap = (null != headerFieldSetMapper) ? headerFieldSetMapper.getOriginalHeaderMap() : new HashMap<String, String>();
		
		if (error instanceof FieldError && error.getCode().equals("typeMismatch")) {
			
			FieldError fieldError = (FieldError) error;
			
			String field = fieldError.getField();
			String fieldHeader = getFieldHeader(line, field);
			String fieldHeaderUserFriendly = headerMap.get(fieldHeader);
			
			Object[] errorArgs = { (null != fieldHeaderUserFriendly) ? fieldHeaderUserFriendly : fieldHeader, fieldError.getRejectedValue(), (null != fieldHeaderUserFriendly) ? fieldHeaderUserFriendly : fieldHeader };
			importLineMapperErrors.reject("ItemValidator.invalidValue",errorArgs,"Field " + ((null != fieldHeaderUserFriendly) ? fieldHeaderUserFriendly : fieldHeader) + " has invalid value " + fieldError.getRejectedValue());
		}
		else {

			Object[] errorArgs = {line,messageSource.getMessage(error, Locale.getDefault())};
			importLineMapperErrors.reject("Import.bind.error",errorArgs,"There is a bind error in line: " + lineNumber);
		}

	}
	
	private String getFieldHeader(String line, String field) {
		
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		
		String[] part = tokenizer.tokenize(line).getValues();
		
		if (part.length < 2) return field;
		
		String key = part[0] + "," + part[1];
		
		DCTImportHeaderMapCache headerCache = importHeaderMapCache.get(key);
		
		if (null == headerCache) return field;
		
		Map<String, String> headerMap = headerCache.getReverseHeaderMap();
		
		if (null == headerMap) return field;
		
		String displayField = headerMap.get(field);
		
		if (null == displayField) return field;
		
		return displayField;
	}
	
	/**
	 * Process header row if it is one so that we can capture the header before processing data
	 * Please note that this assumes the header row always comes before the data row. If not, 
	 * errors are generated.
	 * <p><b>Algorithm:</b></p>
	 * <ol>
	 * <li> The header will be actually captured by the DCTHeaderFieldSetMapper when it is encountered</li>
	 * <li>When a processing line such as "ADD,LOCATION" is encountered, 
	 *    try to apply the header we read before into the names for the mapper for that operation</li>
	 * <li>Also, the isUsed flag is set on the header so that until we encounter another header row, 
	 *    the current header is applied.</li>
	 * <ol>
	 * 
	 * @param line
	 * @throws IncorrectHeaderException 
	 * @throws Exception
	 */
	private void setupNames(String line) throws IncorrectHeaderException,HeaderNotFoundException {
		//Get the field set mapper
		DCTHeaderFieldSetMapper headerFieldSetMapper = (DCTHeaderFieldSetMapper) fieldSetMappers.get("operation*");
		
		//Check to see if the header is already in use by the current row that is getting processed.Then the names
		//are already set.
		if (headerFieldSetMapper != null && !headerFieldSetMapper.isUsed()){
			
			//Get the header string out of the the header fieldset mapper 
			String header = headerFieldSetMapper.getHeader();
			String originalHeader = headerFieldSetMapper.getOriginalHeader();
			
			//Loop through the importDomains configured so that for this line we can setup the names
			for (Map.Entry<String, DCTImport> entry:importDTOs.entrySet()){
				String key = entry.getKey();
				DCTImport importDTO = entry.getValue();
				//We need to get the tokenizer since we need to setup the names on the tokenizer.
				if (tokenizers.get(key) instanceof AbstractLineTokenizer){
					AbstractLineTokenizer dctImportTokenizer = (AbstractLineTokenizer) tokenizers.get(key);
					//If the line contains the key for this domain, we found the exact tokenizer
					if (dctImportTokenizer != null && line.contains(key.replaceAll("\\*", ""))){
						List<String> names = importDTO.getNames(header, originalHeader);
						
						//Set up the names. This is based on the original names that are configured in the domain, but 
						//will take care of missing columns and the order.
						dctImportTokenizer.setNames(Arrays.copyOf(names.toArray(), names.toArray().length,String[].class));
						headerFieldSetMapper.setUsed(true);
						break;
					}
				}
			}
		}
	}
	
	public void setTokenizers(Map<String, LineTokenizer> tokenizers) {
		this.tokenizer.setTokenizers(tokenizers);
		this.tokenizers = tokenizers;
	}

	public void setFieldSetMappers(Map<String, FieldSetMapper<T>> fieldSetMappers) {
		Assert.isTrue(!fieldSetMappers.isEmpty(), "The 'fieldSetMappers' property must be non-empty");
		this.patternMatcher = new PatternMatcher<FieldSetMapper<T>>(fieldSetMappers);
		this.fieldSetMappers = fieldSetMappers;
	}

	public void setImportDTOs(Map<String, DCTImport> importDTOs) {
		this.importDTOs = importDTOs;
	}

	public ImportErrorHandler getImportErrorHandler() {
		return importErrorHandler;
	}

	public void setImportErrorHandler(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}
	
	
}
