package clientSide.control;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.Booking;
import entities.GroupGuide;
import entities.Park;
import entities.ParkEmployee;
import entities.ParkManager;
import entities.ParkVisitor;
import entities.SystemUser;
import entities.Traveler;
import entities.ParkVisitor.VisitorType;

public class LoginController {
	
	
	
	
	
	/**
	 * This method gets userName and password of Traveler/GroupGuide
	 * @return instance of systemUser of the traveler/groupGuide or null if there is no user with this userName and password
	 * @throws CommunicationException 
	 */
	@SuppressWarnings("unused")
	public static SystemUser checkVisitorCredential(String userName ,String password) throws CommunicationException
	{
		//check if there is travelerUser with this userName&password: return Traveler if there is and null if not
		SystemUser systemUser=checkTravelerCredential(userName,password); 
		
		if(systemUser!=null)
		{
			return systemUser;
		}
		//check if there is groupGuideUser with this userName&password: return GruopGuide if there is and null if not
		else
		{
			systemUser=checkgroupGuideCredential(userName,password);
			return systemUser; 
		}
	}
	
	/**
	 * This method gets userName and password 
	 * @return instance of Traveler if there is travelerUser with this userName&password
	 * @throws CommunicationException 
	 */
	private static SystemUser checkTravelerCredential(String userName ,String password) throws CommunicationException
	{
		//check if there is travelerUser with this userName&password:
		
		//Creating a select query in order to check if such a travelerUser exists in the traveler table and get all his details
		Communication requestForTraveler = new Communication(CommunicationType.QUERY_REQUEST); 
		requestForTraveler.setQueryType(QueryType.SELECT);
		requestForTraveler.setTables(Arrays.asList("traveller"));
		requestForTraveler.setSelectColumns(Arrays.asList("*"));
		requestForTraveler.setWhereConditions(Arrays.asList("userName","password"), Arrays.asList("=", "AND","="),      
		  						Arrays.asList(userName,password));
		GoNatureClientUI.client.accept(requestForTraveler); //sending the query to the server that will connect to the DB
		
		if(requestForTraveler.getQueryResult())  //getQueryResult will return true if there is traveler user with this userName&password
		{
			//insert the travelerUser details to an ArrayList
			ArrayList<Object[]> travelerResult = requestForTraveler.getResultList(); 
			
			//insert relevant objects from the ArrayList to values
			String idNumber=(String)travelerResult.get(0)[0];
			String firstName=(String)travelerResult.get(1)[0];
			String lastName=(String)travelerResult.get(2)[0];
			String emailAddress=(String)travelerResult.get(3)[0];
			String phoneNumber=(String)travelerResult.get(4)[0];
			boolean isLoggedIn=(boolean)travelerResult.get(7)[0];
			
			//creating an instance of the traveler user and return it
			 Traveler travelerUser=new Traveler(idNumber,firstName,lastName,userName,password,
						emailAddress,phoneNumber,isLoggedIn,ParkVisitor.VisitorType.TRAVELER);
			return travelerUser;
		}
		else return null; //There is no travelerUser with this userName and password	

	}
	
	
	/**
	 * This method gets userName and password 
	 * @return instance of GroupGuide if there is groupGuideUser with this userName&password
	 * @throws CommunicationException 
	 */
	private static SystemUser checkgroupGuideCredential(String userName ,String password) throws CommunicationException
	{
		//Creating a select query in order to check if such a groupGuideUser exists in the group_guide table and get all his details
		Communication requestForGrupGuide = new Communication(CommunicationType.QUERY_REQUEST);
		requestForGrupGuide.setQueryType(QueryType.SELECT);
		requestForGrupGuide.setTables(Arrays.asList("group_guide"));
		requestForGrupGuide.setSelectColumns(Arrays.asList("*"));
		requestForGrupGuide.setWhereConditions(Arrays.asList("userName","password"), Arrays.asList("=", "AND","="),      
		  						Arrays.asList(userName,password));
		GoNatureClientUI.client.accept(requestForGrupGuide); //sending the query to the server that will connect to the DB
		
		if(requestForGrupGuide.getQueryResult()) //getQueryResult will return true if there is groupGuideUser with this userName&password
		{
			//insert the groupGuideUser details to an ArrayList
			ArrayList<Object[]> groupGuideResult = requestForGrupGuide.getResultList();
			
			//insert relevant objects from the ArrayList to values
			String idNumber=(String)groupGuideResult.get(0)[0];
			String firstName=(String)groupGuideResult.get(1)[0];
			String lastName=(String)groupGuideResult.get(2)[0];
			String emailAddress=(String)groupGuideResult.get(3)[0];
			String phoneNumber=(String)groupGuideResult.get(4)[0];
			boolean isLoggedIn=(boolean)groupGuideResult.get(7)[0];
			
			//creating an instance of the GroupGuideUser and return it
			 GroupGuide groupGuideUser=new GroupGuide(idNumber,firstName,lastName,userName,password,
						emailAddress,phoneNumber,isLoggedIn,ParkVisitor.VisitorType.GROUPGUIDE);
			return groupGuideUser;			
		}
		else {return null;} //There is no GroupGuideUser with this userName and password
	}
	
	
	
	
	
	
	
	
	/**
	 * This method gets userName and password of ParkManager/DepartmentManager/ParkEmployee
	 * @return instance of systemUser of the traveler/groupGuide or null if there is no user with this userName and password
	 * @throws CommunicationException 
	 */
	public static SystemUser checkEmployeeCredential(String userName ,String password) throws CommunicationException
	{
		return null;
	}
	
	
/*	private static SystemUser checkParkManagerCredential(String userName ,String password) throws CommunicationException
	{
		//check if there is ParkManagerUser with this userName&password:
		
		//Creating a query in order to check if such a ParkManagerUser exists in the park_manager table and get all his details
		Communication requestForParkManager = new Communication(CommunicationType.QUERY_REQUEST); 
		requestForParkManager.setQueryType(QueryType.SELECT);
		requestForParkManager.setTables(Arrays.asList("park_manager"));
		requestForParkManager.setSelectColumns(Arrays.asList("*"));
		requestForParkManager.setWhereConditions(Arrays.asList("userName","password"), Arrays.asList("=", "AND","="),      
		  						Arrays.asList(userName,password));
		GoNatureClientUI.client.accept(requestForParkManager); //sending the query to the server that will connect to the DB
		
		if(requestForParkManager.getQueryResult())  //getQueryResult will return true if there is ParkManagerUser with this userName&password
		{
			//insert the ParkManagerUser details to an ArrayList
			ArrayList<Object[]> travelerResult = requestForParkManager.getResultList(); 
			
			//insert relevant objects from the ArrayList to values
			String idNumber=(String)travelerResult.get(0)[0];
			String firstName=(String)travelerResult.get(1)[0];
			String lastName=(String)travelerResult.get(2)[0];
			String emailAddress=(String)travelerResult.get(3)[0];
			String phoneNumber=(String)travelerResult.get(4)[0];
			String managesPark=(String)travelerResult.get(5)[0];
			boolean isLoggedIn=(boolean)travelerResult.get(8)[0];
				
			////////////call create park///////
			//creating an instance of the traveler user and return it:
			
			ParkManager parkManagerUser=new ParkManager(idNumber,firstName,lastName,userName,password,
						emailAddress,phoneNumber,isLoggedIn,null,null);
			return parkManagerUser;
		}		
		return null;		
	}*/
	
	
	
	
	/*private static SystemUser createParkInstance(String managesPark) throws CommunicationException
	{
		//Creating a query in order to get details of specific park (using the park name)
		Communication requestForParkDetails = new Communication(CommunicationType.QUERY_REQUEST); 
		requestForParkDetails.setQueryType(QueryType.SELECT);
		requestForParkDetails.setTables(Arrays.asList("park"));
		requestForParkDetails.setSelectColumns(Arrays.asList("*"));
		requestForParkDetails.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),      
		  						Arrays.asList(managesPark));
		GoNatureClientUI.client.accept(requestForParkDetails); //sending the query to the server that will connect to the DB
		
		if(requestForParkDetails.getQueryResult())  //getQueryResult will return true if there is this park 
		{
			//insert the Park details to an ArrayList
			ArrayList<Object[]> travelerResult = requestForParkDetails.getResultList(); 
			
			//insert relevant objects from the ArrayList to values
			String parkID=(String)travelerResult.get(0)[0];
			String parkName=(String)travelerResult.get(1)[0];
			//String parkAddress=(String)travelerResult.get(2)[0] +" "+(String)travelerResult.get(3)[0];
			//ParkManager parkManager=/////////parkManagerId=(String)travelerResult.get(5)[0];
			//ArrayList<ParkEmployee> employees=///////String parkID
			//DepartmentManager departmentManager=/////(String)departmentManagerId=travelerResult.get(6)[0];
			
			int maximumVisitorsCapacity=(int)travelerResult.get(7)[0];
			int maximumOrderAmount=(int)travelerResult.get(8)[0];
			int currentCapacity=(int)travelerResult.get(10)[0];
			float maximumTimeLimit=(int)travelerResult.get(9)[0];
			
				
			////////////call create park///////
			//creating an instance of the traveler user and return it:
			
			//Park park=new ParkManager(idNumber,firstName,lastName,userName,password,
						//emailAddress,phoneNumber,isLoggedIn,null,null);
			//return ParkManagerUser;
		}		
		
		
		
		return null;
	}/*
	
	
	
	
	
	/**
	 * This method gets instance of SystemUser according to it's isLoggedIn field 
	 * @return True-if this SystemUser is already logged in,false-if not
	 */
	public static boolean checkAlreadyLoggedIn(SystemUser systemUser)
	{
		return systemUser.isLoggedIn();
	}

	/**
	 * This method gets instance of SystemUser 
	 * @return boolean true-if this SystemUser have a active booking, false-if not
	 */
	public static Booking checkIdInActiveBookings(String idNumber)
	{
		return null;
	}
	
	/**
	 * This method gets instance of SystemUser and update the date and time of the loggedIn in the DB
	 */
	public static void updateLastLoggedIn(SystemUser systemUser,LocalDate date,LocalTime time)
	{
		
	}

}
