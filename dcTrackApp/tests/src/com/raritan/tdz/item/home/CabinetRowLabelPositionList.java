/**
 * 
 */
package com.raritan.tdz.item.home;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.tests.TestBase;
import java.util.List;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */
public class CabinetRowLabelPositionList extends TestBase {
	
	//private static final String EVENT_SOURCE = "Unit Test";
	
	private ItemService itemService;
        
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		//itemHome = (ItemHome)ctx.getBean("itemHome");
                itemService = (ItemService)ctx.getBean("itemService");
		//disableListenNotify();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		//clearEvents();
		super.tearDown();
	}
	
	@Test
	public void testGetCabinetRowLabels() throws Throwable {
		System.out.println("in testGetCabinetRowLabels()");

                List<ValueIdDTO> locList = itemService.getAllLocations();
                
                for(ValueIdDTO loc:locList){
                    System.out.println("Location: " + loc.getLabel());
                    System.out.println("----------------------------------------------------------");
                    
                    List<ValueIdDTO> rowsList = itemService.getCabinetRowLabels((Long)loc.getData());
                    
                    for(ValueIdDTO row:rowsList){
                        System.out.println("\tRow Label: " + row.getLabel());
                        System.out.println("\t----------------------");                                                
                    }                                        
                }
	}
        

	@Test
	public void testGetCabinetPositionInRow() throws Throwable {
		System.out.println("in testGetCabinetPositionInRow()");

                List<ValueIdDTO> locList = itemService.getAllLocations();
                
                for(ValueIdDTO loc:locList){
                    System.out.println("Location: " + loc.getLabel());
                    System.out.println("----------------------------------------------------------");
                    
                    List<ValueIdDTO> rowsList = itemService.getCabinetRowLabels((Long)loc.getData());
                    
                    for(ValueIdDTO row:rowsList){
                        System.out.println("\tRow Label: " + row.getLabel());
                        System.out.println("\t----------------------");
                                                
                        List<ValueIdDTO> posList = itemService.getCabinetPositionInRows((Long)loc.getData(), row.getLabel());

                        for(ValueIdDTO pos:posList){
                            System.out.println("\tPosition In Row: " + pos.getLabel());
                        }                                        
                    }                                        
                }
	}  
	

	@Test
	public void testGetContractNumber() throws Throwable {
		System.out.println("in testGetContractNumber()");

                List<ValueIdDTO> locList = itemService.getAllLocations();
                
                for(ValueIdDTO loc:locList){
                    System.out.println("Location: " + loc.getLabel());
                    System.out.println("----------------------------------------------------------");
                    
                    List<ValueIdDTO> rowsList = itemService.getContractNumbers((Long)loc.getData());
                    
                    for(ValueIdDTO row:rowsList){
                        System.out.println("\tContract Number: " + row.getLabel());
                        System.out.println("\t----------------------");                                                
                    }                                        
                }
                
                List<ValueIdDTO> rowsList = itemService.getContractNumbers(null);
                
                for(ValueIdDTO row:rowsList){
                    System.out.println("\tContract Number: " + row.getLabel());
                    System.out.println("\t----------------------");                                                
                }                                        
                
	}


	@Test
	public void testGetProjectNumber() throws Throwable {
		System.out.println("in testGetProjectNumber()");

                List<ValueIdDTO> locList = itemService.getAllLocations();
                
                for(ValueIdDTO loc:locList){
                    System.out.println("Location: " + loc.getLabel());
                    System.out.println("----------------------------------------------------------");
                    
                    List<ValueIdDTO> rowsList = itemService.getProjectNumbers((Long)loc.getData());
                    
                    for(ValueIdDTO row:rowsList){
                        System.out.println("\tProject Number: " + row.getLabel());
                        System.out.println("\t----------------------");                                                
                    }                                        
                }
                
                List<ValueIdDTO> rowsList = itemService.getProjectNumbers(null);
                
                for(ValueIdDTO row:rowsList){
                    System.out.println("\tProject Number: " + row.getLabel());
                    System.out.println("\t----------------------");                                                
                }                                        
                
	}
   	
}
