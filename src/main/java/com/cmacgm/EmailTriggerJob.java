package com.cmacgm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

@Component
@PropertySource("classpath:config.properties")
public class EmailTriggerJob {

	private Properties configProp = new Properties();
	private String pathToStore = "C:\\testing\\";
	private String startDate = "2017-10-16";
	private static String _dapFileName = "";

	@Scheduled(cron = "${cronExpressionHtml}")
	public void JupiterRecordAlert() throws SQLException {

		Connection connection = null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			configProp.load(in);
			startDate = configProp.getProperty("startDate");
			;
		} catch (IOException e) {
			System.out.println("IOException" + e);
			throw new RuntimeException("config.properties not loaded properly");
		}
		try {
			Util_Connection util_connection = new Util_Connection();
			connection = util_connection.GetConnection();
			Statement st = (Statement) connection.createStatement();

			StringBuilder buf = new StringBuilder();
			buf.append("<html><body><b>Job Last Run Time: </b>" + util_connection.getFormatDate() + "</br>");

			String dayWiseDate = "";
			String dailyIndexRate = null;
			ResultSet res = null;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			buf.append("<b>Monthly Wise Summary Report: </b></br> <table border='1'>" + "<tr>"
					+ "<th>Month</th><th>Total Index</th>" + "<th>Manual Index</th>" + "<th>Auto Index</th>"
					+ "<th>Draft Sent</th></tr>");
			Calendar calm = Calendar.getInstance();
			calm.set(2017, 8, 16); // October 16th 2017

			Calendar currentDate = Calendar.getInstance();
			currentDate.add(Calendar.MONTH, 0);
			while (!calm.getTime().after(currentDate.getTime())) {
				calm.add(Calendar.MONTH, 1);
				Date getmonth = calm.getTime();
				String monthDate = util_connection.getFormattedDate(getmonth);

				dailyIndexRate = " select total_index,manual_index,auto_index,draft_sent from ((select COUNT(IndStartTime) as total_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"+ monthDate	+ "', '%b') and IndStartTime IS NOT NULL ) as total_index, (select COUNT(IndStartTime) as manual_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
						+ monthDate
						+ "', '%b')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL ) as manual_index,(select COUNT(IndStartTime) as auto_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
						+ monthDate
						+ "', '%b')  and IndStartTime IS NOT NULL and IndStartUser='Service' and IndStartUser IS NOT NULL) as auto_index,(select COUNT(IndStartTime) as draft_sent FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%b')=DATE_FORMAT('"
						+ monthDate
						+ "', '%b')  and IndStartTime IS NOT NULL and status IS NOT NULL and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') ) as draft_sent)";

				res = st.executeQuery(dailyIndexRate);

				while (res.next()) {
					String total_index = res.getString("total_index");
					String manual_index = res.getString("manual_index");
					String auto_index = res.getString("auto_index");
					String draft_sent = res.getString("draft_sent");
					formatter = new SimpleDateFormat("MMMM");
					String monthName = formatter.format(getmonth);
					/*
					 * System.out.println("month_name:"+monthName);
					 * System.out.println("total_index " +total_index);
					 * System.out.println("manual_index " +manual_index);
					 * System.out.println("auto_index " +auto_index);
					 * System.out.println("draft_sent " +draft_sent);
					 */
					buf.append("<tr><td>").append(monthName).append("</td><td>").append(total_index).append("</td><td>")
							.append(manual_index).append("</td><td>").append(auto_index).append("</td><td>")
							.append(draft_sent).append("</td></tr>");
				}

			}
			buf.append("</table>");
			buf.append("</br><b>Monthly Wise Index Type Summary Report: </b></br> <table border='1'>" + "<tr>"
					+ "<th>Month</th><th>NSI</th>" + "<th>RSI</th>" + "<th>COR</th>"
					+ "<th>CORF</th><th>DAP</th><th>QRS</th><th>CRS</th><th>COM</th><th>CERT</th><th>TOTAL INDEX</th></tr>");
			int NSI_COUNT = 0, RSI_COUNT = 0, COR_COUNT = 0, CORF_COUNT = 0, DAP_COUNT = 0, QRS_COUNT = 0,
					CRS_COUNT = 0, COM_COUNT = 0, CERT_COUNT = 0,TOTAL_INDEX=0;

			Calendar currentDateTime = Calendar.getInstance();
			currentDateTime.add(Calendar.MONTH, 0);
			Calendar calmDate = Calendar.getInstance();
			calmDate.set(2017, 8, 16); // October 16th 2017
			while (!calmDate.getTime().after(currentDateTime.getTime())) {
				calmDate.add(Calendar.MONTH, 1);
				Date getmonth = calmDate.getTime();
			String		monthDate = util_connection.getFormattedDate(getmonth);

				dailyIndexRate = "select COUNT(IndStartTime) as total_index,count(index_type) as count,index_type as index_type from dtl_index where DATE_FORMAT(si_received, '%b')=DATE_FORMAT('"+ monthDate	+ "', '%b')  group by index_type";
				res = st.executeQuery(dailyIndexRate);
				String monthName ="";
				while (res.next()) {

					TOTAL_INDEX += Integer.parseInt(res.getString("total_index"));
						if (res.getString("index_type").equals("NSI"))
							NSI_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("RSI"))
							RSI_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("COR"))
							COR_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("CORF"))
							CORF_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("DAP"))
							DAP_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("QRS"))
							QRS_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("CRS"))
							CRS_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("COM"))
							COM_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("CERT"))
							CERT_COUNT += Integer.parseInt(res.getString("count"));				

						monthName=formatter.format(getmonth);
				}
					buf.append("<tr><td>").append(monthName).append("</td><td>").append(NSI_COUNT)
							.append("</td><td>").append(RSI_COUNT).append("</td><td>").append(COR_COUNT)
							.append("</td><td>").append(CORF_COUNT).append("</td><td>").append(DAP_COUNT)
							.append("</td><td>").append(QRS_COUNT).append("</td><td>").append(CRS_COUNT)
							.append("</td><td>").append(COM_COUNT).append("</td><td>").append(CERT_COUNT)
							.append("</td><td>").append(TOTAL_INDEX).append("</td></tr>");
					NSI_COUNT = 0;
					RSI_COUNT = 0;
					COR_COUNT = 0;
					CORF_COUNT = 0;
					DAP_COUNT = 0;
					QRS_COUNT = 0;
					CRS_COUNT = 0;
					COM_COUNT = 0;
					CERT_COUNT = 0;
					TOTAL_INDEX = 0;
				

				
			}
			buf.append("</table></br>");

			buf.append("</br><b>Weekly Wise Summary Report: </b></br> <table border='1'>" + "<tr>"
					+ "<th>Week</th><th>Total Index</th>" + "<th>Manual Index</th>" + "<th>Auto Index</th>"
					+ "<th>Draft Sent</th></tr>");
			int wtotal_index = 0;
			int wmanual_index = 0;
			int wauto_index = 0;
			int wdraft_sent = 0;
			int tempRate = 1;
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDateWise = formatter.parse(startDate);
			Date endDateWise = formatter.parse(util_connection.getFormattedDate(new Date()));
			Calendar startw = Calendar.getInstance();
			startw.setTime(startDateWise);
			Calendar endw = Calendar.getInstance();
			endw.setTime(endDateWise);
			endw.add(Calendar.DATE, 0);
			int WEEK_OF_YEAR = 0;
			for (Date date = startw.getTime(); startw.before(endw); startw.add(Calendar.DATE,
					1), date = startw.getTime()) {
				tempRate++;
				WEEK_OF_YEAR = startw.get(Calendar.WEEK_OF_YEAR);
		
			

				dayWiseDate = util_connection.getFormattedDate(date);
				dailyIndexRate = "select total_index,manual_index,auto_index,draft_sent from ((SELECT COUNT(IndStartTime) as total_index FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate + "' and IndStartTime IS NOT NULL) as total_index,"
						+ "(SELECT COUNT(IndStartTime)  as manual_index FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate
						+ "' and IndStartUser!='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL) as manual_index ,"
						+ "(SELECT COUNT(IndStartTime) as auto_index FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate
						+ "' and IndStartUser='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL) as auto_index,"
						+ "(SELECT COUNT(IndStartTime) as draft_sent FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate
						+ "' and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') and IndStartTime IS NOT NULL and status IS NOT NULL) as draft_sent )";

				res = st.executeQuery(dailyIndexRate);

				while (res.next()) {

					if (tempRate > 0 && tempRate <= 7) {
						wtotal_index += Integer.parseInt(res.getString("total_index"));
						wmanual_index += Integer.parseInt(res.getString("manual_index"));
						wauto_index += Integer.parseInt(res.getString("auto_index"));
						wdraft_sent += Integer.parseInt(res.getString("draft_sent"));
					}

					/*
					 * System.out.println(dayWiseDate); System.out.println(
					 * "wtotal_index " +wtotal_index); System.out.println(
					 * "wmanual_index " +wmanual_index); System.out.println(
					 * "wauto_index " +wauto_index); System.out.println(
					 * "wdraft_sent " +wdraft_sent);
					 */

				}
				int day_of_week = endw.get(Calendar.DAY_OF_WEEK)-1;
				int tempweek= endw.get(Calendar.WEEK_OF_YEAR);
					if (tempRate > 0 && tempRate == 7) {
				
					tempRate = 0;
					
					buf.append("<tr><td>").append(WEEK_OF_YEAR).append("</td><td>").append(wtotal_index)
							.append("</td><td>").append(wmanual_index).append("</td><td>").append(wauto_index)
							.append("</td><td>").append(wdraft_sent).append("</td></tr>");
					wtotal_index = 0;
					wmanual_index = 0;
					wauto_index = 0;
					wdraft_sent = 0;

				}		else if(WEEK_OF_YEAR==tempweek && tempRate==day_of_week){
					tempRate = 0;
						buf.append("<tr><td>").append(WEEK_OF_YEAR).append("</td><td>").append(wtotal_index)
							.append("</td><td>").append(wmanual_index).append("</td><td>").append(wauto_index)
							.append("</td><td>").append(wdraft_sent).append("</td></tr>");
					wtotal_index = 0;
					wmanual_index = 0;
					wauto_index = 0;
					wdraft_sent = 0;

				}
			}
			buf.append("</table></br>");

			buf.append("</br><b>Weekly Wise Index Type Summary Report: </b></br> <table border='1'>" + "<tr>"
					+ "<th>Week</th><th>NSI</th>" + "<th>RSI</th>" + "<th>COR</th>"
					+ "<th>CORF</th><th>DAP</th><th>QRS</th><th>CRS</th><th>COM</th><th>CERT</th><th>TOTAL INDEX</th></tr>");

		
			tempRate = 1;
			wtotal_index = 0;
			Calendar startindex = Calendar.getInstance();
			startindex.setTime(startDateWise);
		
			Calendar endindex = Calendar.getInstance();
			endindex.setTime(endDateWise);
			endindex.add(Calendar.DATE, 0);
			for (Date date = startindex.getTime(); startindex.before(endindex); startindex.add(Calendar.DATE,
					1), date = startindex.getTime()) {
				tempRate++;
				WEEK_OF_YEAR = startindex.get(Calendar.WEEK_OF_YEAR);
			
				dayWiseDate = util_connection.getFormattedDate(date);

				dailyIndexRate = "select COUNT(IndStartTime) as total_index,count(index_type) as count,index_type as index_type from dtl_index where DATE(si_received)='"
						+ dayWiseDate + "' group by index_type";
				res = st.executeQuery(dailyIndexRate);

				while (res.next()) {

					if (tempRate > 0 && tempRate <= 7) {
						wtotal_index += Integer.parseInt(res.getString("total_index"));
						if (res.getString("index_type").equals("NSI"))
							NSI_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("RSI"))
							RSI_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("COR"))
							COR_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("CORF"))
							CORF_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("DAP"))
							DAP_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("QRS"))
							QRS_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("CRS"))
							CRS_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("COM"))
							COM_COUNT += Integer.parseInt(res.getString("count"));
						else if (res.getString("index_type").equals("CERT"))
							CERT_COUNT += Integer.parseInt(res.getString("count"));

					}

				}
				int day_of_week = endindex.get(Calendar.DAY_OF_WEEK)-1;
				int tempweek= endw.get(Calendar.WEEK_OF_YEAR);
				if (tempRate > 0 && tempRate == 7) {
						 

					tempRate = 0;
					buf.append("<tr><td>").append(WEEK_OF_YEAR).append("</td><td>").append(NSI_COUNT)
							.append("</td><td>").append(RSI_COUNT).append("</td><td>").append(COR_COUNT)
							.append("</td><td>").append(CORF_COUNT).append("</td><td>").append(DAP_COUNT)
							.append("</td><td>").append(QRS_COUNT).append("</td><td>").append(CRS_COUNT)
							.append("</td><td>").append(COM_COUNT).append("</td><td>").append(CERT_COUNT)
							.append("</td><td>").append(wtotal_index).append("</td></tr>");
					NSI_COUNT = 0;
					RSI_COUNT = 0;
					COR_COUNT = 0;
					CORF_COUNT = 0;
					DAP_COUNT = 0;
					QRS_COUNT = 0;
					CRS_COUNT = 0;
					COM_COUNT = 0;
					CERT_COUNT = 0;
					wtotal_index = 0;

				}else if(WEEK_OF_YEAR==tempweek && tempRate==day_of_week){

					tempRate = 0;
					buf.append("<tr><td>").append(WEEK_OF_YEAR).append("</td><td>").append(NSI_COUNT)
							.append("</td><td>").append(RSI_COUNT).append("</td><td>").append(COR_COUNT)
							.append("</td><td>").append(CORF_COUNT).append("</td><td>").append(DAP_COUNT)
							.append("</td><td>").append(QRS_COUNT).append("</td><td>").append(CRS_COUNT)
							.append("</td><td>").append(COM_COUNT).append("</td><td>").append(CERT_COUNT)
							.append("</td><td>").append(wtotal_index).append("</td></tr>");
					NSI_COUNT = 0;
					RSI_COUNT = 0;
					COR_COUNT = 0;
					CORF_COUNT = 0;
					DAP_COUNT = 0;
					QRS_COUNT = 0;
					CRS_COUNT = 0;
					COM_COUNT = 0;
					CERT_COUNT = 0;
					wtotal_index = 0;
				}
			}
			buf.append("</table></br>");

			Calendar start = Calendar.getInstance();
			start.setTime(endDateWise);
			start.add(Calendar.DATE, -7);
			Calendar end = Calendar.getInstance();
			end.setTime(endDateWise);
			buf.append("<b>Day Wise Summary Report: </b></br> <table border='1'>" + "<tr>"
					+ "<th>Day</th><th>Total Index</th>" + "<th>Manual Index</th>" + "<th>Auto Index</th>"
					+ "<th>Draft Sent</th></tr>");
			for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
				dayWiseDate = util_connection.getFormattedDate(date);

				dailyIndexRate = "select total_index,manual_index,auto_index,draft_sent from ((SELECT COUNT(IndStartTime) as total_index FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate + "' and IndStartTime IS NOT NULL ) as total_index,"
						+ "(SELECT COUNT(IndStartTime)  as manual_index FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate
						+ "' and IndStartUser!='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL) as manual_index ,"
						+ "(SELECT COUNT(IndStartTime) as auto_index FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate
						+ "' and IndStartUser='Service' and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL) as auto_index,"
						+ "(SELECT COUNT(IndStartTime) as draft_sent FROM dtl_index where DATE(IndStartTime)='"
						+ dayWiseDate
						+ "' and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') and IndStartTime IS NOT NULL and status IS NOT NULL) as draft_sent) ";

				res = st.executeQuery(dailyIndexRate);
				while (res.next()) {
					String total_index = res.getString("total_index");
					String manual_index = res.getString("manual_index");
					String auto_index = res.getString("auto_index");
					String draft_sent = res.getString("draft_sent");

					buf.append("<tr><td>").append(dayWiseDate).append("</td><td>").append(total_index)
							.append("</td><td>").append(manual_index).append("</td><td>").append(auto_index)
							.append("</td><td>").append(draft_sent).append("</td></tr>");

					/*
					 * System.out.println(dayWiseDate); System.out.println(
					 * "total_index " +total_index); System.out.println(
					 * "manual_index " +manual_index); System.out.println(
					 * "auto_index " +auto_index); System.out.println(
					 * "draft_sent " +draft_sent);
					 */

				}
			}
			buf.append("</table>");
			Calendar startWeek = Calendar.getInstance();
			Calendar endWeek = Calendar.getInstance();
			startWeek.setTime(endDateWise);
			startWeek.add(Calendar.DATE, -7);
			endWeek.setTime(endDateWise);
			buf.append("</br><b>Day Wise Index Type Summary Report: </b></br> <table border='1'>" + "<tr>"
					+ "<th>Date</th><th>NSI</th>" + "<th>RSI</th>" + "<th>COR</th>"
					+ "<th>CORF</th><th>DAP</th><th>QRS</th><th>CRS</th><th>COM</th><th>CERT</th><th>TOTAL INDEX</th></tr>");
			for (Date date = startWeek.getTime(); startWeek.before(endWeek); startWeek.add(Calendar.DATE,
					1), date = startWeek.getTime()) {
				dayWiseDate = util_connection.getFormattedDate(date);

				dailyIndexRate = "select COUNT(IndStartTime) as total_index,count(index_type) as count,index_type as index_type from dtl_index where DATE(si_received)='"
						+ dayWiseDate + "' group by index_type";
				res = st.executeQuery(dailyIndexRate);
				NSI_COUNT = 0;
				RSI_COUNT = 0;
				COR_COUNT = 0;
				CORF_COUNT = 0;
				DAP_COUNT = 0;
				QRS_COUNT = 0;
				CRS_COUNT = 0;
				COM_COUNT = 0;
				CERT_COUNT = 0;
				wtotal_index = 0;
				while (res.next()) {

					wtotal_index += Integer.parseInt(res.getString("total_index"));
					if (res.getString("index_type").equals("NSI"))
						NSI_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("RSI"))
						RSI_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("COR"))
						COR_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("CORF"))
						CORF_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("DAP"))
						DAP_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("QRS"))
						QRS_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("CRS"))
						CRS_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("COM"))
						COM_COUNT += Integer.parseInt(res.getString("count"));
					else if (res.getString("index_type").equals("CERT"))
						CERT_COUNT += Integer.parseInt(res.getString("count"));
				}
				buf.append("<tr><td>").append(dayWiseDate).append("</td><td>").append(NSI_COUNT).append("</td><td>")
						.append(RSI_COUNT).append("</td><td>").append(COR_COUNT).append("</td><td>").append(CORF_COUNT)
						.append("</td><td>").append(DAP_COUNT).append("</td><td>").append(QRS_COUNT).append("</td><td>")
						.append(CRS_COUNT).append("</td><td>").append(COM_COUNT).append("</td><td>").append(CERT_COUNT)
						.append("</td><td>").append(wtotal_index).append("</td></tr>");
				NSI_COUNT = 0;
				RSI_COUNT = 0;
				COR_COUNT = 0;
				CORF_COUNT = 0;
				DAP_COUNT = 0;
				QRS_COUNT = 0;
				CRS_COUNT = 0;
				COM_COUNT = 0;
				CERT_COUNT = 0;
				wtotal_index = 0;

			}
			buf.append("</table>");
			/*
			 * dailyIndexRate =
			 * " select * from ((select COUNT(IndStartTime) as total_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
			 * + dateString +
			 * "', '%Y') and IndStartTime IS NOT NULL and IndStartUser IS NOT NULL and status IS NOT NULL) as total_index, (select COUNT(IndStartTime) as auto_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
			 * + dateString +
			 * "', '%Y')  and IndStartTime IS NOT NULL and IndStartUser='Service' and IndStartUser IS NOT NULL and status IS NOT NULL) as auto_index,(select COUNT(IndStartTime) as manual_index FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
			 * + dateString +
			 * "', '%Y')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL and status IS NOT NULL) as manual_index,(select COUNT(IndStartTime) as draft_sent FROM dtl_index  where   DATE_FORMAT(IndStartTime, '%Y')=DATE_FORMAT('"
			 * + dateString +
			 * "', '%Y')  and IndStartTime IS NOT NULL and IndStartUser!='Service' and IndStartUser IS NOT NULL and status IS NOT NULL and status IN ('DRAFT SENT','DRAFT SENT WITH QUERY') ) as draft_sent)"
			 * ;
			 * 
			 * res = st.executeQuery(dailyIndexRate);
			 * 
			 * while (res.next()) { String total_index =
			 * res.getString("total_index"); String manual_index =
			 * res.getString("manual_index"); String auto_index =
			 * res.getString("auto_index"); String draft_sent =
			 * res.getString("draft_sent"); Format formatter = new
			 * SimpleDateFormat("yyyy"); String year = formatter.format(new
			 * Date()); buf.append("<b>Year: " + year + "</b><table border='1'>"
			 * + "<tr>" + "<th>Total Index</th>" + "<th>Manual Index</th>" +
			 * "<th>Auto Index</th>" + "<th>Draft Sent</th></tr>");
			 * buf.append("<tr><td>").append(total_index).append("</td><td>").
			 * append(manual_index).append("</td><td>")
			 * .append(auto_index).append("</td><td>").append(draft_sent).append
			 * ("</td></tr></table>");
			 * 
			 * System.out.println("year:"+year); System.out.println(
			 * "total_index " +total_index); System.out.println( "manual_index "
			 * +manual_index); System.out.println( "auto_index " +auto_index);
			 * System.out.println("draft_sent " +draft_sent);
			 * 
			 * }
			 */
			buf.append("</body></html>");
			SendMail sendEmail = new SendMail();
			Thread.sleep(10);
			
			sendEmail.SendMail("Jupiter Record Count", buf.toString());

		}

		catch (SQLException s) {
			System.out.println("SQL code does not execute." + s);
		} catch (Exception e) {
			System.out.println("Exception" + e);

		} finally {
			connection.close();
		}

	}

	@Scheduled(cron = "${cronExpressionFrance}")
	public void JupiterRecordAttachment() throws SQLException {

		Connection connection = null;
		String dailyIndexRate = null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			configProp.load(in);
			pathToStore = configProp.getProperty("pathToStore");
		} catch (IOException e) {
			System.out.println("IOException" + e);
			throw new RuntimeException("config.properties not loaded properly");
		}
		try {
			Util_Connection util_connection = new Util_Connection();
			connection = util_connection.GetConnection();
			Statement st = (Statement) connection.createStatement();
			String yesterdayDate = "";
			String todayDate = "";
			ResultSet res = null;
			yesterdayDate = util_connection.formmatedDate();
			todayDate = util_connection.todayFormattedDate();
			yesterdayDate = yesterdayDate + " 06:00:00";
			todayDate = todayDate + " 05:59:59";
			dailyIndexRate = "Select * from dtl_query where id!='' and query_created_date between '" + yesterdayDate
					+ "'  AND '" + todayDate + "'";
			res = st.executeQuery(dailyIndexRate);

			Thread.sleep(10);
			writeQueryResult(res, "QueryCreated");

			dailyIndexRate = "Select * from dtl_index where id!='' and IndStartTime  between '" + yesterdayDate
					+ "' AND '" + todayDate + "'";
			res = st.executeQuery(dailyIndexRate);

			Thread.sleep(10);
			writeQueryResult(res, "IndexStarted");

			dailyIndexRate = "Select * from dtl_audit_errors where id!='' and error_marked_on between '" + yesterdayDate
					+ "'  AND '" + todayDate + "'";
			res = st.executeQuery(dailyIndexRate);

			Thread.sleep(10);
			writeQueryResult(res, "ErrorMarked");

			dailyIndexRate = "Select * from dtl_correction_query where id!='' and created_date between '"
					+ yesterdayDate + "'  AND '" + todayDate + "'";
			res = st.executeQuery(dailyIndexRate);

			Thread.sleep(10);
			writeQueryResult(res, "QueryCorrection");

			Thread.sleep(10);
			SendEmailAttachment("Yesterday Jupiter Daily Records Count Attached for Reference");

		}

		catch (SQLException s) {
			System.out.println("SQL code does not execute." + s);
		} catch (Exception e) {
			System.out.println("Exception" + e);
		} finally {
			connection.close();
		}

	}

	@Scheduled(cron = "${cronExpressionAuditReport}")
	public void JupiterRecordAttachmentMonday() throws SQLException {

		Connection connection = null;
		String dailyIndexRate = null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			configProp.load(in);
			pathToStore = configProp.getProperty("pathToStore");
		} catch (IOException e) {
			System.out.println("IOException" + e);
			throw new RuntimeException("config.properties not loaded properly");
		}
		try {
			Util_Connection util_connection = new Util_Connection();
			connection = util_connection.GetConnection();
			Statement st = (Statement) connection.createStatement();
			String yesterdayDate = "";
			String todayDate = "";
			String CurrDate = util_connection.getFormatDate();

			CurrDate = CurrDate.replace(":", "_");
			ResultSet res = null;
			todayDate = util_connection.todayFormattedDate();

			yesterdayDate = util_connection.getLastMondayDateddMMyyyy();
			yesterdayDate = yesterdayDate + " 06:31:00";
			todayDate = todayDate + " 06:30:00";
			dailyIndexRate = "select e.book_no as Booking_No, e.category as Error_Category,e.sub_category as Error_Sub_Category,e.error_person as Error_Addressed_By, e.error_marked_on as Error_Reported_Date, a.book_no,a.audit_start_user as Audited_By ,a.audit_end_user as Audit_Completed_By,a.audit_end_time as Audit_Completed_Time from dtl_audit_errors e, dtl_audit a where a.book_no=e.book_no and e.error_marked_on between '"
					+ yesterdayDate + "'  AND '" + todayDate + "' order by e.error_marked_on desc";
			res = st.executeQuery(dailyIndexRate);

			String content = "Audit Report From Date:" + yesterdayDate + " To Date:" + todayDate;
			_dapFileName = "Audit_Report_" + CurrDate;
			Thread.sleep(10);
			writeQueryResult(res, _dapFileName);
			Thread.sleep(10);
			SendScheduledReportMailwithAttachement("Audit_Report", content);

		}

		catch (SQLException s) {
			System.out.println("SQL code does not execute." + s);
		} catch (Exception e) {
			System.out.println("Exception" + e);
		} finally {
			connection.close();
		}

	}

	@Scheduled(cron = "${cronExpressionScheduledReports}")
	public void JupiterScheduledReportsWithAttachment() throws SQLException {

		Connection connection = null;
		String dailyIndexRate = null;
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			configProp.load(in);
			pathToStore = configProp.getProperty("pathToStore");
		} catch (IOException e) {
			System.out.println("IOException" + e);
			throw new RuntimeException("config.properties not loaded properly");
		}
		try {

			Util_Connection util_connection = new Util_Connection();
			connection = util_connection.GetConnection();
			Statement st = (Statement) connection.createStatement();
			String yesterdayDate = "";
			String lastthreeDayDate="";
			String todayDate = "";
			ResultSet res = null;
			String CurrDate = util_connection.getFormatDate();

			CurrDate = CurrDate.replace(":", "_");

			String currTime = util_connection.getCurrentTimeStamp();

			String[] HHmm = currTime.split(":");
			String HH = HHmm[0]; // 004
			String MM = HHmm[1];

			String _rpt1 = configProp.getProperty("scheduledReport1");
			String _rpt2 = configProp.getProperty("scheduledReport2");
			String _rpt3 = configProp.getProperty("scheduledReport3");
			String _rpt4 = configProp.getProperty("scheduledReport4");
			String _rpt5 = configProp.getProperty("scheduledReport5");
			String _rpt6 = configProp.getProperty("scheduledReport6");
		 //	String _rpt7 = configProp.getProperty("scheduledReport7");
			String _rptm6 = configProp.getProperty("scheduledReportm6");
			//String _rptm7 = configProp.getProperty("scheduledReportm7");

			String _rtp1StTime = configProp.getProperty("ScheduledRpt1StartTime");
			String _rtp1EndTime = configProp.getProperty("ScheduledRpt1EndTime");

			String _rtp2StTime = configProp.getProperty("ScheduledRpt2StartTime");
			String _rtp2EndTime = configProp.getProperty("ScheduledRpt2EndTime");

			String _rtp3StTime = configProp.getProperty("ScheduledRpt3StartTime");
			String _rtp3EndTime = configProp.getProperty("ScheduledRpt3EndTime");

			String _rtp4StTime = configProp.getProperty("ScheduledRpt4StartTime");
			String _rtp4EndTime = configProp.getProperty("ScheduledRpt4EndTime");

			String _rtp5StTime = configProp.getProperty("ScheduledRpt5StartTime");
			String _rtp5EndTime = configProp.getProperty("ScheduledRpt5EndTime");

			String _rtp6StTime = configProp.getProperty("ScheduledRpt6StartTime");
			String _rtp6EndTime = configProp.getProperty("ScheduledRpt6EndTime");

			//String _rtp7StTime = configProp.getProperty("ScheduledRpt7StartTime");
			//String _rtp7EndTime = configProp.getProperty("ScheduledRpt7EndTime");

			_dapFileName = "DAP_Report_" + CurrDate;
			String subject = "";
			String content = "";
			boolean status = false;
			yesterdayDate = util_connection.formmatedDate();
			lastthreeDayDate = util_connection.formattedLastThreeDayDate();
			todayDate = util_connection.todayFormattedDate();
			subject = "DAP REPORT";
			if (HH.equals(_rpt1) && MM.equals("00")) {
				status = true;
				yesterdayDate = yesterdayDate + " " + _rtp1StTime;
				todayDate = todayDate + " " + _rtp1EndTime;
				content = "DAP_REPORT From Date:" + yesterdayDate + " To Date:" + todayDate;
			} else if (HH.equals(_rpt2) && MM.equals("00")) {
				status = true;
				yesterdayDate = todayDate + " " + _rtp2StTime;
				todayDate = todayDate + " " + _rtp2EndTime;
				content = "DAP_REPORT From Date:" + yesterdayDate + " To Date:" + todayDate;
			} else if (HH.equals(_rpt3) && MM.equals("00")) {
				status = true;
				yesterdayDate = todayDate + " " + _rtp3StTime;
				todayDate = todayDate + " " + _rtp3EndTime;
				content = "DAP_REPORT From Date:" + yesterdayDate + " To Date:" + todayDate;
			} else if (HH.equals(_rpt4) && MM.equals("00")) {
				status = true;
				yesterdayDate = todayDate + " " + _rtp4StTime;
				todayDate = todayDate + " " + _rtp4EndTime;
				content = "DAP_REPORT From Date:" + yesterdayDate + " To Date:" + todayDate;
			} else if (HH.equals(_rpt5) && MM.equals("00")) {
				status = true;
				yesterdayDate = todayDate + " " + _rtp5StTime;
				todayDate = todayDate + " " + _rtp5EndTime;
				content = "DAP_REPORT From Date:" + yesterdayDate + " To Date:" + todayDate;
			} else if (HH.equals(_rpt6) && MM.equals(_rptm6)) {
				subject = "Log_History";
				_dapFileName = "Log_History" + CurrDate;
				lastthreeDayDate = lastthreeDayDate + " " + _rtp6StTime;
				todayDate = todayDate + " " + _rtp6EndTime;
				content = "Log_History From Date:" + lastthreeDayDate + " To Date:" + todayDate;
				dailyIndexRate = "select index_type,book_no,ssc_log,log_type, created_user,created_date from tbl_input_log_history where created_date between '"
						+ lastthreeDayDate + "'  AND '" + todayDate + "' order by created_date desc";
				res = st.executeQuery(dailyIndexRate);
				Thread.sleep(10);

				writeQueryResult(res, _dapFileName);

				Thread.sleep(10);
				// SendEmailAttachment("Current_Indexing_Status");
				SendScheduledReportMailwithAttachement(subject, content);
			}
				/*else if (HH.equals(_rpt7) && MM.equals(_rptm7)) {
				subject = "Log_History";
				_dapFileName = "Log_History" + CurrDate;
				yesterdayDate = todayDate + " " + _rtp7StTime;
				todayDate = todayDate + " " + _rtp7EndTime;
				content = "Log_History From Date:" + yesterdayDate + " To Date:" + todayDate;
				dailyIndexRate = "select index_type,book_no,ssc_log,log_type, created_user,created_date from tbl_input_log_history where created_date  between '"
						+ yesterdayDate + "'  AND '" + todayDate + "' order by created_date desc";
				res = st.executeQuery(dailyIndexRate);
				Thread.sleep(10);

				writeQueryResult(res, _dapFileName);

				Thread.sleep(10);
				// SendEmailAttachment("Current_Indexing_Status");
				SendScheduledReportMailwithAttachement(subject, content);
			}*/
			if (status) {
				dailyIndexRate = "Select index_type,book_no,country_id,country_name,brand,customer_name,agency_code,vessel_name,voyage_code,service_code,saildate,pol,pod,zone,sub_zone,edi,si_received,sla_cutoff_time,IndStartUser,IndStartTime,IndEndTime,status,hold_status,created_date,cntnr_lnkd,booking_status,bl_status,release_action"
						+ " from dtl_index where status = 'DRAFT-APPROVAL' and id!='' and IndStartTime  between '"
						+ yesterdayDate + "' AND '" + todayDate + "'";
				res = st.executeQuery(dailyIndexRate);
				Thread.sleep(10);

				writeQueryResult(res, _dapFileName);

				Thread.sleep(10);
				// SendEmailAttachment("Current_Indexing_Status");
				SendScheduledReportMailwithAttachement(subject, content);
			}
		}

		catch (SQLException s) {
			System.out.println("SQL code does not execute." + s);
		} catch (Exception e) {
			System.out.println("Exception" + e);
		} finally {
			connection.close();
		}

	}

	public void writeQueryResult(ResultSet rs, String fileName) throws IOException, SQLException {
		File pathToFileStore = new File(pathToStore);
		if (!pathToFileStore.exists())
			pathToFileStore.mkdir();

		File file1 = new File(pathToStore + fileName + ".xls");
		if (file1.exists())
			file1.delete();
		else
			file1.createNewFile();
		writeToOutputPath(rs, fileName, file1);
	}

	public void writeToOutputPath(ResultSet rs, String filename, File file) throws SQLException, IOException {
		try {
			WritableWorkbook Wworkbook = Workbook.createWorkbook(file);
			WritableSheet Wsheet = Wworkbook.createSheet(filename, 0);
			int colCount, col, row = 1;
			ResultSetMetaData rsmd = rs.getMetaData();
			colCount = rsmd.getColumnCount();
			Label label = new Label(0, 0, filename);
			Wsheet.mergeCells(0, 0, 4, 0);
			Wsheet.addCell(label);
			for (col = 0; col < colCount; col++) {
				label = new Label(col, row, rsmd.getColumnName(col + 1));
				Wsheet.addCell(label);
			}
			row++;

			while (rs.next()) {
				for (col = 0; col < colCount; col++) {
					label = new Label(col, row, rs.getString(col + 1));
					Wsheet.addCell(label);
				}
				row++;
			}

			Wworkbook.write();
			Wworkbook.close();

		} catch (Exception e) {
			System.out.println("Exception in file generation " + e);
		}

	}

	public void SendEmailAttachment(String content) throws IOException, SQLException {
		SendMail sendEmail = new SendMail();
		boolean filesend1 = false;
		boolean filesend2 = false;
		boolean filesend3 = false;
		boolean filesend4 = false;

		File file1 = new File(pathToStore + "QueryCreated.xls");
		if (file1.exists())
			filesend1 = true;
		else
			file1 = null;
		File file2 = new File(pathToStore + "IndexStarted.xls");
		if (file2.exists())
			filesend2 = true;
		else
			file2 = null;

		File file3 = new File(pathToStore + "ErrorMarked.xls");
		if (file3.exists())
			filesend3 = true;
		else
			file3 = null;
		File file4 = new File(pathToStore + "QueryCorrection.xls");
		if (file4.exists())
			filesend4 = true;
		else
			file4 = null;

		if (filesend1 || filesend2 || filesend3 || filesend4)
			sendEmail.SendMailwithattachement("Jupiter Record Day Count", content, file1, file2, file3, file4);
	}

	public void SendScheduledReportMailwithAttachement(String subject, String content)
			throws IOException, SQLException {
		SendMail sendEmail = new SendMail();
		boolean filesend1 = false;

		File file1 = new File(pathToStore + _dapFileName + ".xls");
		if (file1.exists())
			filesend1 = true;
		else
			file1 = null;

		if (filesend1) {
			sendEmail.SendScheduledMailwithAttachement(subject, content, file1);
		}
	}
}
