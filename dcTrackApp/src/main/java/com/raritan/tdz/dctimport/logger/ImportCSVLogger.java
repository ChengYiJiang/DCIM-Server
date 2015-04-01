/**
 * 
 */
package com.raritan.tdz.dctimport.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dctimport.job.ImportStepResolveListener;
import com.raritan.tdz.dctimport.job.TransitionStep;

/**
 * @author prasanna
 * This will log the lines in a CSV file.
 */
public class ImportCSVLogger implements ImportLogger {
	
	private final Logger logger = Logger.getLogger("dctImport");
	
	private File csvFile;
	private String fileName;
	
	private final AtomicInteger lineNumber;
	
	private final Map<Integer,ImportLoggerVO> importLoggerVOMap =  new LinkedHashMap<Integer, ImportLoggerVO>();
	
	@Autowired
	private MessageSource messageSource; 
	
	@Autowired
	private ImportStepResolveListener importStepResolveListener;
	
	private final AtomicInteger errorAndWarningColumnNumber = new AtomicInteger();
	
	public ImportCSVLogger(AtomicInteger lineNumber){
		this.lineNumber = lineNumber;
	}
	
	@Override
	public void setFileName (String fileName) throws IOException{
		if (fileName != null && !fileName.isEmpty()){
			if (fileName.endsWith(".xls.csv")) {
				this.fileName = fileName.replace(".xls", "").substring(0,fileName.replace(".xls", "").lastIndexOf(".csv")) + "-error.csv";
			} else if (fileName.endsWith(".xlsx.csv")) {
				this.fileName = fileName.replace(".xlsx", "").substring(0,fileName.replace(".xlsx", "").lastIndexOf(".csv")) + "-error.csv";
			} else {
				this.fileName = fileName.substring(0,fileName.lastIndexOf(".csv")) + "-error.csv";
			}
			csvFile = new File(this.fileName);
			csvFile.createNewFile();
		} else {
			this.fileName = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.logger.ImportLogger#logLine(java.lang.String, int)
	 */
	@Override
	public void logLine(String line, int lineNumber) {
		if (csvFile == null) return;
		
		//If the header or line contains Errors and warnings, note the token number and remove that column
		//This is because we append the errors and warnings column at the end and we dont want too many
		//Errors and Warnings columns
		line = removeErrorsAndWarning(line);
		
		ImportLoggerVO loggerVO = getImportLoggerVO(lineNumber);
		loggerVO.setLine(line);
		loggerVO.setLineNumber(lineNumber);
		this.lineNumber.set(lineNumber);
	}





	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.logger.ImportLogger#logError(org.springframework.validation.Errors, int)
	 */
	@Override
	public void logError(Errors errors) {
		if (csvFile == null) return;
		ImportLoggerVO loggerVO = getImportLoggerVO(lineNumber.get());
		loggerVO.setErrors(errors);
		loggerVO.setLineNumber(lineNumber.get());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.logger.ImportLogger#logWarning(org.springframework.validation.Errors, int)
	 */
	@Override
	public void logWarning(Errors warnings) {
		if (csvFile == null) return;
		ImportLoggerVO loggerVO = getImportLoggerVO(lineNumber.get());
		loggerVO.setErrors(warnings);
		loggerVO.setLineNumber(lineNumber.get());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.logger.ImportLogger#commit()
	 */
	@Override
	public void commit() throws Exception {
		if (csvFile == null) return;
		
		if (!importLoggerVOMap.isEmpty()){
			String line = getAssembledLine();
			if (line != null && !line.isEmpty()){
				FileOutputStream outStream = new FileOutputStream(csvFile,true);
				String encoding = "UTF8";
			    OutputStreamWriter osw = new OutputStreamWriter(outStream, encoding);
				BufferedWriter bw = new BufferedWriter(osw);
				
				bw.write(line);
				bw.newLine();
				bw.flush();
				bw.close();
			}
			
			logger.warn("Writing the line: " + line);
			
			importLoggerVOMap.clear();
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.logger.ImportLogger#clean()
	 */
	@Override
	public void clean(boolean deleteFile) throws Exception{
		if (csvFile == null) return;
		if (deleteFile){
			csvFile.delete();
		}
		else {
			FileOutputStream outStream = new FileOutputStream(csvFile);
			String encoding = "UTF8";
		    OutputStreamWriter osw = new OutputStreamWriter(outStream, encoding);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write("");
			bw.flush();
			bw.close();
		}
	}

	
	private ImportLoggerVO getImportLoggerVO(int lineNumber) {
		if (importLoggerVOMap.get(lineNumber) == null)
			importLoggerVOMap.put(lineNumber, new ImportLoggerVO());
		return importLoggerVOMap.get(lineNumber);
	}
	
	private String getAssembledLine(){
		StringBuilder ret = new StringBuilder();
		
		if (!importLoggerVOMap.isEmpty()){
			for (Map.Entry<Integer, ImportLoggerVO> loggerVOEntry:importLoggerVOMap.entrySet()){
				boolean isImportStep = importStepResolveListener.getTransitionToStep().equals(TransitionStep.IMPORT_STEP);
				boolean isCommentLine = loggerVOEntry.getValue() != null && loggerVOEntry.getValue().getLine() != null ? Pattern.compile("^#.*|^,+",Pattern.CASE_INSENSITIVE).matcher(loggerVOEntry.getValue().getLine()).matches():false;
				
				//If this is import step, we need to make sure that we dont include lines that do not have any errors (comment line is an exception)
				if (isImportStep){
					if ((isCommentLine || 
							(loggerVOEntry.getValue() != null 
								&& (loggerVOEntry.getValue().getErrors().hasErrors() || loggerVOEntry.getValue().getWarnings().hasErrors()))))
						ret.append(getAssembledLineFromVO(loggerVOEntry.getValue()));
				}
				else
					ret.append(getAssembledLineFromVO(loggerVOEntry.getValue()));
			}
		}
		
		return ret.toString();
	}
	
	private String getAssembledLineFromVO(ImportLoggerVO loggerVO){
		if (loggerVO == null) return "";
		
		StringBuilder line = new StringBuilder();
		
		line.append(loggerVO.getLine());
		
		//If this is an header, then make sure we append the column for Errors and Warnings
		Pattern pattern = Pattern.compile("^#.*Operation.*,.*Object.*",Pattern.CASE_INSENSITIVE);
		if (pattern.matcher(line).matches()) {
			line.append(",Errors and Warnings");
		} else {
			line.append(",");
		}
		
		line.append(getAssembledErrors(loggerVO));
		
		line.append(getAssembledWarnings(loggerVO));
		
		return line.toString();
	}
	
	private String getAssembledErrors(ImportLoggerVO loggerVO){
		if (loggerVO == null) return "";
		
		StringBuilder errors = new StringBuilder();
		
		if (loggerVO.getErrors() != null && loggerVO.getErrors().hasErrors()){
			errors.append("\"");
			for (ObjectError error:loggerVO.getErrors().getAllErrors()){
				errors.append(messageSource.getMessage(error, Locale.getDefault()));
				errors.append(";");
			}
			errors.append("\"");
		}
		
		
		return errors.toString();
	}
	
	private String getAssembledWarnings(ImportLoggerVO loggerVO){
		if (loggerVO == null) return "";
		
		StringBuilder errors = new StringBuilder();
		
		if (loggerVO.getWarnings() != null && loggerVO.getWarnings().hasErrors()){
			errors.append("\"");
			for (ObjectError error:loggerVO.getErrors().getAllErrors()){
				errors.append(messageSource.getMessage(error, Locale.getDefault()));
				errors.append(";");
			}
			errors.append("\"");
		}	
		return errors.toString();
	}
	
	private String removeErrorsAndWarning(String line) {
		Pattern headerPattern = Pattern.compile("^#.*Operation.*,.*Object.*",Pattern.CASE_INSENSITIVE);
		Pattern errorsAndWarningsPattern = Pattern.compile(".*Errors\\s+And\\s+Warnings.*",Pattern.CASE_INSENSITIVE);
		if (headerPattern.matcher(line).matches() && errorsAndWarningsPattern.matcher(line).matches()){
			errorAndWarningColumnNumber.set(0);
			String[] tokens = line.split(",");
			int cnt = 0;
			for (String token:tokens){
				if (errorsAndWarningsPattern.matcher(token).matches()){
					errorAndWarningColumnNumber.set(cnt);
				}
				cnt++;
			}
		}
		
		
		//On any corresponding lines, remove the errors and warning from original line and then store
		if (errorAndWarningColumnNumber.get() > 0){
			DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
			FieldSet fieldSet = tokenizer.tokenize(line);
			
			String[] tokens = fieldSet.getValues();
			StringBuilder newLine = new StringBuilder();
			int cnt = 0;
			for (String token:tokens){
				if (errorAndWarningColumnNumber.get() != cnt){
					newLine.append(token);
					newLine.append(",");
				}
				cnt++;
			}
			
			line = newLine.toString().contains(",") ? newLine.substring(0,newLine.lastIndexOf(",")).toString():newLine.toString();
		}
		return line;
	}

	@Override
	public String getURL() {
		if (csvFile == null || fileName == null) return null;
		StringBuilder result = new StringBuilder();
		result.append("/dcTrackImport/");
		result.append(fileName.substring(fileName.lastIndexOf("/") + 1));
		
		return result.toString();
	}

	@Override
	public String getCurrentOriginalLine() {
		ImportLoggerVO loggerVO = getImportLoggerVO(lineNumber.get());
		return loggerVO.getLine();
	}

}
