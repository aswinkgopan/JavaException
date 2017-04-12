package com.code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;


public class ConnectAuditDB extends ValidateDB implements Runnable {

	Boolean ConnectionStatus = false;
	Connection connection = null;
	Button ConnectDbButton;
	Statement statement = null;
	ResultSet logsResultSet =null;
	FileOutputStream fos = null;
	String sql = null;
	public Label dbStatus;
	public Display display;
	ProgressBar progressBar;
	Button payloadProc;
	Button stopPayloadProc;
	String dbServiceName;
	String dbSchema; 
	String environment;
	String filepath;
	public static int payloadCount = 1;
	
	String DAY_OF_MONTH_NBR;
	String startDate;
	String toDate;	
	String EXCEPTION_TS;
	String SERVICE_NAME;
	String COUNTRY_CODE;
	String PROTOCOL_MSG_ID;
	String SOURCE_SYSTEM_NAME;
	String DEST_SYSTEM_NAME;
	String SRC_CMPNT_NAME;
	String SRC_SUB_CMPNT_NAME;
	String SRC_SERVER_NAME;
	String PROTOCOL_HDR_TXT;
	String PAYLOAD_MSG_TXT;
	String RESEND_MSG_IND;
	String DbSchema;
	
	boolean canFetchPayload = false;

		
	public boolean isCanFetchPayload() {
		return canFetchPayload;
	}

	public void setCanFetchPayload(boolean canFetchPayload) {
		this.canFetchPayload = canFetchPayload;
	}

	public ConnectAuditDB(Label dbLabel,Connection connection,ResultSet resultSet) {
		this.setConnection(connection);
		dbStatus = dbLabel;
		this.setLogsResultSet(resultSet);
	}
	
public ConnectAuditDB() {
		// TODO Auto-generated constructor stub
	}

	public void disconnectAll()
	{
		Connection connection = this.getConnection();
		Statement statement = this.getStatement();
		try {
			statement.close();		
			connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}
	
	public void fetchPayload()
	{
		this.doUpdate(this.getDisplay(), this.getDbStatus(), "Executing Query - Please Wait !!");
		boolean isEmptyResultSet = true;
		ResultSet resultSet = this.getLogsResultSet();
		try{
			trgDirCreate();
			while (resultSet.next()) {
				isEmptyResultSet = false;
				doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 5);
				Clob payLoad = resultSet.getClob(18);
				String protocol = resultSet.getString(6).trim();
				String tempCmpnt =null;
				String subCmpnt = resultSet.getString(10).trim();
				tempCmpnt = subCmpnt.substring(subCmpnt.length()-10, subCmpnt.length()-5 );
				subCmpnt = protocol +"_"+ tempCmpnt;
				BufferedReader br = new BufferedReader(payLoad.getCharacterStream());
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line=br.readLine())!=null) {
					 sb.append(line);
					}
				String data = sb.toString();
				this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 7);
				this.doUpdate(this.getDisplay(), this.getDbStatus(), "Payload Count :"+new Integer(payloadCount).toString());
				Thread.sleep(100);
				if (data.length() > 2) {
				this.writingClobToFile(data, payloadCount++,subCmpnt);
				this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 8);
				}
				
			}
			if (isEmptyResultSet)
			{
				this.doUpdate(this.getDisplay(), this.getDbStatus(), "Empty Resultset , Check Values Please");
				Thread.sleep(2000);
				this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 0);
				this.doUpdateButton(this.getDisplay(), this.getPayloadProc(), this.getStopPayloadProc());
			}
			else{
				Runtime.getRuntime().exec("explorer.exe "+this.getFilepath());
			}
			resultSet.close();
			}catch (InterruptedException e) {
				try {
					Runtime.getRuntime().exec("explorer.exe "+this.getFilepath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				this.doUpdate(this.getDisplay(), this.getDbStatus(), "Query Interupted");
				this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 0);
				this.doUpdateButton(this.getDisplay(), this.getPayloadProc(), this.getStopPayloadProc());
				try {
					resultSet.close();
					this.doUpdateButton(this.getDisplay(), this.getPayloadProc(), this.getStopPayloadProc());
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}catch (IOException e) {
				this.doUpdate(this.getDisplay(), this.getDbStatus(), "IO Exception");
				this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 0);
				this.doUpdateButton(this.getDisplay(), this.getPayloadProc(), this.getStopPayloadProc());
			}catch (SQLException e) {
				e.printStackTrace();
				this.doUpdate(this.getDisplay(), this.getDbStatus(), "Check the values please !!");
				this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 0);
				this.doUpdateButton(this.getDisplay(), this.getPayloadProc(), this.getStopPayloadProc());
			}

	}
	
	
	public void getResultSet()
	{
		String envSchema = null;
		try {
		
			int countValues = 0;
			Connection connection = this.getConnection();
			statement = connection.createStatement();
			if (this.getDbSchema().contains("QA"))
				envSchema = "j2eai";
			else if (this.getDbSchema().contains("FUT"))
				envSchema = "x8eai";
			else
				envSchema = "dpeai";
			
			String sSQL = "SELECT * from "+envSchema+".eai_exception where service_name like '%"+this.getSERVICE_NAME()+"%' ";
			if (this.getCOUNTRY_CODE() != null) 
			{
				sSQL 		+= " AND country_code in ('"+this.getCOUNTRY_CODE()+"') " ;
				countValues++;
			}
			if (this.getPROTOCOL_MSG_ID() != null) 
			{
				sSQL 	+= " AND protocol_msg_id like '%"+this.getPROTOCOL_MSG_ID()+"%' ";
				countValues++;
			}
			if (this.getSRC_CMPNT_NAME() != null)  
			{
				sSQL		+= " AND SRC_CMPNT_NAME like  '%"+this.getSRC_CMPNT_NAME() +"%' ";
				countValues++;
			}
			if (this.getSRC_SUB_CMPNT_NAME() != null)
			{
				sSQL	+= " AND SRC_SUB_CMPNT_NAME like '%"+this.getSRC_SUB_CMPNT_NAME()+"%' ";
				countValues++;
			}
			if (this.getSRC_SERVER_NAME() != null) 
			{
				sSQL 	+= " AND SRC_SERVER_NAME like '%"+this.getSRC_SERVER_NAME()+"%' ";
				countValues++;
			}
			if (this.getRESEND_MSG_IND() != null) 
			{
				sSQL 		+= " AND RESEND_MSG_IND like '%"+this.getRESEND_MSG_IND()+"%' ";
				countValues++;
			}
			if (this.getDAY_OF_MONTH_NBR() != null) 
			{
				if (this.getDAY_OF_MONTH_NBR().length() > 1 && this.getDAY_OF_MONTH_NBR().contains(","))
				sSQL 			+= " AND DAY_OF_MONTH_NBR in ("+this.getDAY_OF_MONTH_NBR()+") ";
				else
					if (this.getDAY_OF_MONTH_NBR().length() == 1)
						sSQL 	+= " AND DAY_OF_MONTH_NBR = "+this.getDAY_OF_MONTH_NBR();
				countValues++;
			}
			if (this.getPAYLOAD_MSG_TXT() != null)
			{
				sSQL 	+= " AND payload_msg_txt like '%"+this.getPAYLOAD_MSG_TXT()+"%' ";
				countValues++;
			}
			if (this.getStartDate() != null && this.toDate != null) 
			{			
				sSQL += " AND TIMESTAMP (exception_ts) BETWEEN TIMESTAMP ('"+this.getStartDate()+"') AND TIMESTAMP ('"+this.getToDate()+"') ";
				countValues++;
			}
			
			this.setStatement(statement);
			System.out.println(sSQL);
			this.setLogsResultSet(statement.executeQuery(sSQL));
			if (countValues > 0)
				this.setCanFetchPayload(true);
			else
				this.setCanFetchPayload(false);		
			
		} catch (SQLException e) {
			this.dbStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
			this.dbStatus.setText("Check Your Values - SQL Error");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
	}
	
	public void makeConnection()  {
		String envSchema =null;
		try {
			if (this.getDbSchema().contains("QA") || this.getDbSchema().contains("FUT"))
				envSchema = "DSNV";
			else if (this.getDbSchema().contains("DSNU"))
				envSchema = "DSNU";
			else if (this.getDbSchema().contains("GM4P"))
				envSchema = "GM4P";
			Connection connection;
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			connection = DriverManager.getConnection("jdbc:db2:"+envSchema,this.getDbUsername(),this.getDbPassword());
			this.setConnection(connection);
			this.setConnectionStatus(true);
		} catch (Exception e) {
			doUpdate(this.getDisplay(), this.getDbStatus(), "Cannot Connect to DB");
		}
	}

	public void trgDirCreate()
	{
		final File homeDir = new File(System.getProperty("user.home"));
		final File path = new File(homeDir,"Payload_Exception");
		if (!path.exists())
		{
			path.mkdir();
		}
		if (this.deleteDirectory(path))
		{
			path.mkdir();	
		}
	}
 
	public boolean deleteDirectory(File path)
	{
	if (path.exists())
	{
		File[] files = path.listFiles();
		for(int i = 0;i<files.length;i++)
			if (files[i].isDirectory())
				deleteDirectory(files[i]);
			else if (files[i].delete());			
	}
	return path.delete();
	}
		
	public void writingClobToFile(String data,int count,String appendName)
	{
		BufferedWriter out = null;
		final File homeDir = new File(System.getProperty("user.home"));
		final File dir = new File(homeDir,"Payload_Exception");
		this.setFilepath(dir.getPath());
		try{
			File file = new File(dir.getAbsolutePath()+"/"+this.getSERVICE_NAME()+"_"+appendName+"_"+count+".txt");
			System.out.println(file.toString());
				out = new BufferedWriter(new FileWriter(file));
				out.write(data);
			}catch(Exception e)
			{
				e.printStackTrace();
			}finally{
				try {
					out.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					this.doUpdate(this.getDisplay(), this.getDbStatus(), "Cannot Write to File");
				}
			}
	
	}
	
	
	public void validateDBConnection(String username, String password) {
		if (username != null && password != null)
		{
			try{	
				Class.forName("com.ibm.db2.jcc.DB2Driver");
				Connection connection = DriverManager.getConnection("jdbc:db2:DSNU", username,password);
				this.setConnectionStatus(true);
				connection.close();
				
			}catch(Exception e)
			{
				this.setConnectionStatus(false);
			}
		}
		else
		{
			this.setConnectionStatus(false);
		}
	
	}
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getDbSchema() {
		return dbSchema;
	}
	public void setDbSchema(String dbSchema) {
		this.dbSchema = dbSchema;
	}

	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public ResultSet getLogsResultSet() {
		return logsResultSet;
	}
	public void setLogsResultSet(ResultSet logsResultSet) {
		this.logsResultSet = logsResultSet;
	}

	public FileOutputStream getFos() {
		return fos;
	}


	public void setFos(FileOutputStream fos) {
		this.fos = fos;
	}


	public String getSql() {
		return sql;
	}


	public void setSql(String sql) {
		this.sql = sql;
	}


	public Statement getStatement() {
		return statement;
	}


	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public Boolean getConnectionStatus() {
		return ConnectionStatus;
	}

	public void setConnectionStatus(Boolean connectionStatus) {
		ConnectionStatus = connectionStatus;
	}

	public void doUpdate(final Display display,final Label dbStatus, final String value )
	{
		display.asyncExec(new Runnable(){
			public void run()
			{
				if (!dbStatus.isDisposed())
					dbStatus.setText(value);
					dbStatus.getParent().layout();
			}
		});
	}

	public void doUpdateProgressBar(final Display display,final ProgressBar bar, final int value )
	{
		display.asyncExec(new Runnable(){
			public void run()
			{
				if (!bar.isDisposed())
					bar.setSelection(value);
					bar.getParent().layout();
			}
		});
	}
	
	public void doUpdateButton(final Display display,final Button start, final Button stop )
	{
		display.asyncExec(new Runnable(){
			public void run()
			{
				stop.setVisible(false);
				start.setVisible(true);
			}
		});
	}
	
	public void doUpdateConnectDbButton(final Display display,final Button Connect)
	{
		display.asyncExec(new Runnable(){
			public void run()
			{
				Connect.setEnabled(true);
			}
		});
	}
	
	public void run() {
		this.fetchPayload();
		this.disconnectAll();
		this.doUpdateProgressBar(this.getDisplay(), this.getProgressBar(), 0);
		this.doUpdateButton(this.getDisplay(), this.getPayloadProc(), this.getStopPayloadProc());
		this.doUpdate(this.getDisplay(), this.getDbStatus(), "");
		this.doUpdateConnectDbButton(this.getDisplay(), this.getConnectDbButton());
		payloadCount = 1;
	}

	public Label getDbStatus() {
		return dbStatus;
	}

	public void setDbStatus(Label dbStatus) {
		this.dbStatus = dbStatus;
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public Button getPayloadProc() {
		return payloadProc;
	}

	public void setPayloadProc(Button payloadProc) {
		this.payloadProc = payloadProc;
	}

	public Button getStopPayloadProc() {
		return stopPayloadProc;
	}

	public void setStopPayloadProc(Button stopPayloadProc) {
		this.stopPayloadProc = stopPayloadProc;
	}

	public Button getConnectDbButton() {
		return ConnectDbButton;
	}

	public void setConnectDbButton(Button connectDbButton) {
		ConnectDbButton = connectDbButton;
	}

	public String getCOUNTRY_CODE() {
		return COUNTRY_CODE;
	}

	public void setCOUNTRY_CODE(String country_code) {
		COUNTRY_CODE = country_code;
	}

	public String getDAY_OF_MONTH_NBR() {
		return DAY_OF_MONTH_NBR;
	}

	public void setDAY_OF_MONTH_NBR(String day_of_month_nbr) {
		DAY_OF_MONTH_NBR = day_of_month_nbr;
	}

	public String getDEST_SYSTEM_NAME() {
		return DEST_SYSTEM_NAME;
	}

	public void setDEST_SYSTEM_NAME(String dest_system_name) {
		DEST_SYSTEM_NAME = dest_system_name;
	}

	public String getEXCEPTION_TS() {
		return EXCEPTION_TS;
	}

	public void setEXCEPTION_TS(String exception_ts) {
		EXCEPTION_TS = exception_ts;
	}

	public String getPAYLOAD_MSG_TXT() {
		return PAYLOAD_MSG_TXT;
	}

	public void setPAYLOAD_MSG_TXT(String payload_msg_txt) {
		PAYLOAD_MSG_TXT = payload_msg_txt;
	}

	public String getPROTOCOL_HDR_TXT() {
		return PROTOCOL_HDR_TXT;
	}

	public void setPROTOCOL_HDR_TXT(String protocol_hdr_txt) {
		PROTOCOL_HDR_TXT = protocol_hdr_txt;
	}

	public String getPROTOCOL_MSG_ID() {
		return PROTOCOL_MSG_ID;
	}

	public void setPROTOCOL_MSG_ID(String protocol_msg_id) {
		PROTOCOL_MSG_ID = protocol_msg_id;
	}

	public String getRESEND_MSG_IND() {
		return RESEND_MSG_IND;
	}

	public void setRESEND_MSG_IND(String resend_msg_ind) {
		RESEND_MSG_IND = resend_msg_ind;
	}

	public String getSERVICE_NAME() {
		return SERVICE_NAME;
	}

	public void setSERVICE_NAME(String service_name) {
		SERVICE_NAME = service_name;
	}

	public String getSOURCE_SYSTEM_NAME() {
		return SOURCE_SYSTEM_NAME;
	}

	public void setSOURCE_SYSTEM_NAME(String source_system_name) {
		SOURCE_SYSTEM_NAME = source_system_name;
	}

	public String getSRC_CMPNT_NAME() {
		return SRC_CMPNT_NAME;
	}

	public void setSRC_CMPNT_NAME(String src_cmpnt_name) {
		SRC_CMPNT_NAME = src_cmpnt_name;
	}

	public String getSRC_SERVER_NAME() {
		return SRC_SERVER_NAME;
	}

	public void setSRC_SERVER_NAME(String src_server_name) {
		SRC_SERVER_NAME = src_server_name;
	}

	public String getSRC_SUB_CMPNT_NAME() {
		return SRC_SUB_CMPNT_NAME;
	}

	public void setSRC_SUB_CMPNT_NAME(String src_sub_cmpnt_name) {
		SRC_SUB_CMPNT_NAME = src_sub_cmpnt_name;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}	
}
