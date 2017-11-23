package com.cmacgm.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cmacgm.Util_Connection;

public class Test {

	/*public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		Util_Connection util_connection = new Util_Connection();
		
		String yesterdayDate="";
		String todayDate="";
		        yesterdayDate=util_connection.formmatedDate();
				todayDate=util_connection.todayFormattedDate();
				System.out.println(yesterdayDate+" 06:00:00");
				System.out.println(todayDate+" 05:59:59");
		     Calendar cal = Calendar.getInstance();
		    cal.set(2017, 9, 06);  //January 30th 2000
	         int month = cal.get(Calendar.MONTH);
	         int day = cal.get(Calendar.DAY_OF_MONTH);
	        cal.clear();	      
	        Calendar todayDate = Calendar.getInstance();
	        int currentMonth = todayDate.get(Calendar.MONTH);
		        
	        for (int startmonth=month; month <= currentMonth; month++) {
	        	System.out.println(month);
	        	cal.set(2017,month, 06);  //January 30th 2000
	            Date getmonth = cal.getTime();
	        	Util_Connection util_connection = new Util_Connection();
	            System.out.println("getmonth=" + util_connection.getFormattedDate(getmonth));

	        }
		Util_Connection util_connection = new Util_Connection();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String startDate="2017-10-17";
		 Date startDateWise = formatter.parse(startDate);
		 Date endDateWise =formatter.parse(util_connection.getFormattedDate(new Date()));
		
		Calendar startw = Calendar.getInstance();
		 startw.setTime(startDateWise);
		 Calendar endw = Calendar.getInstance();
		 endw.setTime(endDateWise);
		 int tempw=0;
		 boolean status=false;
		 int tempRate=0;
	 for (Date date = startw.getTime(); startw.before(endw); startw.add(Calendar.DATE, 1), date = startw.getTime()) {
		 tempRate++;		
		 int WEEK_OF_YEAR = 1;						
		 WEEK_OF_YEAR= startw.get(Calendar.WEEK_OF_YEAR);
		 if(tempRate>0 && tempRate==7){
		     tempw=WEEK_OF_YEAR;
		     tempRate=0;
		     System.out.println(WEEK_OF_YEAR+"WEEK_OF_YEAR: "+status);
		  }
		
		
		 
		
		 start.setTime(startDateWise);
		 Calendar end = Calendar.getInstance();
		 end.setTime(endDateWise);
		 for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String dayWiseDate= util_connection.getFormattedDate(date);
			System.out.println(dayWiseDate);
		 }
	//}
		
		   Calendar cal = Calendar.getInstance();
		   cal.set(2017,8, 16); //October 16th 2017	              
		   Util_Connection util_connection = new Util_Connection();
           Calendar currentDate = Calendar.getInstance();	 
           currentDate.add(Calendar.MONTH, -1);  
           while (!cal.getTime().after(currentDate.getTime())) {	        	
        	 cal.add(Calendar.MONTH, 1);  
            Date getmonth = cal.getTime();
            String monthDate=util_connection.getFormattedDate(getmonth);
            System.out.println(monthDate);
           }*/
	//}

}
