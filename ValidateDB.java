package com.code;

import java.sql.Connection;
import java.sql.DriverManager;

public class ValidateDB implements Runnable {

	String dbUsername;
	String dbPassword;
	boolean connnectionStatus = false;
	
	public void run() 
	{
		this.validateDBConnection(this.getDbUsername(), this.getDbPassword());
	}

	public void validateDBConnection(String username, String password) {
		if (username != null && password != null)
		{
			try{	
				Class.forName("com.ibm.db2.jcc.DB2Driver");
				Connection connection = DriverManager.getConnection("jdbc:db2:DSNU", username,password);
				this.setConnnectionStatus(true);
				connection.close();				
			}catch(Exception e)
			{
				this.setConnnectionStatus(false);
			}
		}
		else
		{
			this.setConnnectionStatus(false);
		}
	
	}

	public String getDbPassword() {
		return dbPassword;
	}
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	public String getDbUsername() {
		return dbUsername;
	}
	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public boolean isConnnectionStatus() {
		return connnectionStatus;
	}

	public void setConnnectionStatus(boolean connnectionStatus) {
		this.connnectionStatus = connnectionStatus;
	}
	
}
