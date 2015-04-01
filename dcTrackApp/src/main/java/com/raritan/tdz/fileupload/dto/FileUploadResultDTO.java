/**
 * 
 */
package com.raritan.tdz.fileupload.dto;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

/**
 * @author prasanna
 *
 */
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class FileUploadResultDTO {
	
	private List<ResultDTO> resultDTOs = new ArrayList<FileUploadResultDTO.ResultDTO>();
	
	
	@JsonProperty("results")
	public List<ResultDTO> getResultDTOs() {
		return resultDTOs;
	}

	public void addResult(String fileName, String originalFileName){
		ResultDTO resultDTO = new ResultDTO(fileName, originalFileName);
		resultDTOs.add(resultDTO);
	}

	public static class ResultDTO{
		private String fileName;
		private String originalFileName;
		
		public ResultDTO(String fileName, String originalFileName) {
			super();
			this.fileName = fileName;
			this.originalFileName = originalFileName;
		}
	
		
		@JsonProperty("newFileName")
		public String getFileName() {
			return fileName;
		}
		
	
		
		@JsonProperty("originalFileName")
		public String getOriginalFileName() {
			return originalFileName;
		}
	}

}
