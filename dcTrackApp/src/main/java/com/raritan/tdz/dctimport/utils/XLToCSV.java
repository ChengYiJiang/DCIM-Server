/**
 * 
 */
package com.raritan.tdz.dctimport.utils;

/**
 * @author prasanna
 *
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.Resource;

import com.raritan.tdz.dctimport.integration.routers.ImportNormalizerResolver;
import com.raritan.tdz.dctimport.integration.transformers.HeaderNormalizer;
import com.raritan.tdz.dctimport.integration.transformers.OperationObjectTypeNormalizer;



public class XLToCSV {
	
	/**
	 * This converts an Excel spreadsheet into CSV using Apache POI library
	 * @param inResource - Excel Spreadsheet File resource
	 * @param outFilePath - CSV Spreadsheet File resource
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static void convert(Resource inResource, String outFilePath) throws IOException, InvalidFormatException {
		convertSheet(inResource,outFilePath,0, ",");
	}
	
	/**
	 * This converts an Excel spreadsheet into CSV using Apache POI library
	 * @param inResource - Excel Spreadsheet File resource
	 * @param outFilePath - CSV Spreadsheet File resource
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static void convertTemplateSheet(Resource inResource, String outFilePath, String sheetName) throws IOException, InvalidFormatException {
		Workbook workbook = WorkbookFactory.create(inResource.getFile());
		int sheetIndex = workbook.getSheetIndex(sheetName);
		convertSheet(inResource,outFilePath,sheetIndex, "|");
	}

	/**
	 * This converts an Excel spreadsheet into CSV using Apache POI library
	 * @param inResource - Excel Spreadsheet File resource
	 * @param outFilePath - CSV Spreadsheet File resource
	 * @param dilimiter TODO
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static void convertSheet(Resource inResource, String outFilePath, int sheetIndex, String dilimiter) throws IOException, InvalidFormatException {
		
		File outFile = createOutFile(outFilePath);
		FileOutputStream fos = new FileOutputStream(outFile);
		Workbook workbook = WorkbookFactory.create(inResource.getFile());
		
		workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		
		String encoding = "UTF8";
	    OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
		BufferedWriter bw = new BufferedWriter(osw);
		
		DataFormatter df = new DataFormatter();
		
		for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++){
			StringBuilder rowBuffer = new StringBuilder();
			Cell cell = null;
			Row row = null;
			row = sheet.getRow(rowIndex);
			
			if (row != null){
				for (int colIndex = row.getFirstCellNum(); colIndex < row.getLastCellNum(); colIndex++){
					cell = row.getCell(colIndex);
	
					if (!cell.toString().isEmpty()){
						String formattedCellValue = df.formatCellValue(cell);
						if (formattedCellValue.contains(dilimiter))
							rowBuffer.append("\"");
						rowBuffer.append(formattedCellValue);
						if (formattedCellValue.contains(dilimiter))
							rowBuffer.append("\"");
						rowBuffer.append(dilimiter);
					}
					else
						rowBuffer.append(dilimiter);
				}
			} else {
				rowBuffer.append("");
			}
			
			if (rowBuffer != null){
				String rowData = rowBuffer.toString().contains(",") ? rowBuffer.toString().substring(0, rowBuffer.toString().lastIndexOf(",")) : rowBuffer.toString();
				
				bw.write(rowData);
				bw.newLine();
			}
		}
		
		bw.flush();
		bw.close();
	}

	private static File createOutFile(String outputFilePath) throws IOException {
		File outFile = new File(outputFilePath);
		outFile.createNewFile();
		return outFile;
	}
	
	public static void applyCommaCorrection(String csvFilePath) throws IOException {
		File inFile = new File(csvFilePath);
		File outFile = new File(csvFilePath+".tmp");
		outFile.createNewFile();
		
		FileInputStream inputStream = new FileInputStream(inFile);
		FileOutputStream outputStream = new FileOutputStream(outFile);
		
		InputStreamReader isr = new InputStreamReader(inputStream);
		OutputStreamWriter osw = new OutputStreamWriter(outputStream);
		
		BufferedReader reader = new BufferedReader(isr);
		BufferedWriter writer = new BufferedWriter(osw);
		
		String rline = reader.readLine();
		int headerColumnCnt = 0;
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		while (rline != null){
			Pattern headerPattern = Pattern.compile(ImportNormalizerResolver.HEADER_PATTERN,Pattern.CASE_INSENSITIVE);
			Pattern operationPattern = Pattern.compile(ImportNormalizerResolver.OPERATION_OBJECT_PATTERN,Pattern.CASE_INSENSITIVE);
			if (headerPattern.matcher(rline).matches()){
				FieldSet fieldSet = tokenizer.tokenize(rline);
				headerColumnCnt = fieldSet.getFieldCount();
				writer.write(rline);
			} else if (operationPattern.matcher(rline).matches()){
				FieldSet fieldSet = tokenizer.tokenize(rline);
				int opnColumnsCnt = fieldSet.getFieldCount();
				StringBuilder outString = new StringBuilder(rline);
				if (opnColumnsCnt < headerColumnCnt){
					int diff = headerColumnCnt - opnColumnsCnt;
					for (int i = 0; i < diff; i++){
						outString.append(",");
					}
				}
				writer.write(outString.toString());
			} else {
				writer.write(rline);
			}
			writer.newLine();
			rline = reader.readLine();
		}
		
		writer.flush();
		isr.close();
		osw.close();
		
		Files.copy(Paths.get(csvFilePath+".tmp"), Paths.get(csvFilePath),StandardCopyOption.REPLACE_EXISTING);
		outFile.delete();
	}
}
