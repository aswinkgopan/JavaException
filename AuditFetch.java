package com.code;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Menu;

public class AuditFetch extends Thread{

	private ConnectFile file = null;  //  @jve:decl-index=0:
	private ConnectAuditDB db = null;  //  @jve:decl-index=0:
	private ValidateDB validateDB = null;
	private boolean saveMe = false;
	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="195,13"
	private TabFolder processTab = null;
	private Composite connectComposite = null;
	private Text serviceName = null;
	private Label Interface = null;
	private Button fetchAuditPayloadButton = null;
	private Button stopFetchAuditPayloadButton = null;
	private Thread payloadAuditProcess = null;
	private Composite connect = null;
	private Button[] auditCheckBoxes;
	private Text username = null;
	private Text password = null;
	private Label Username = null;
	private Label password_l = null;
	TableItem auditItem = null;
	boolean isAvailableAuditDetails = false;
	boolean isAvailableExceptionDetails = false;
	private Button connectDbButton = null;
	private Button cRemember = null;
	private Button ClearMeNowButton = null;
	private Label ClickToRemember = null;
	private Label ClickToErase = null;
	private ProgressBar progressBar = null;
	private Map<String,String> myAuditList = null;  //  @jve:decl-index=0:
	private Combo auditDBSchemaCombo = null;
	private Label DBSchema_l = null;
	String dbUsername;
	String dbPassword;
	private Label dbAuditStatus = null;
	private Table AuditEditable = null;
	
	final String[] myAuditColumnNames = {
			"COUNTRY_CODE",
			"PROTOCOL_MSG_ID",
			"DAY_OF_MONTH_NBR",
			"SRC_CMPNT_NAME",
			"SRC_SUB_CMPNT_NAME",
			"SRC_SERVER_NAME",
			"PAYLOAD_MSG_TXT",
			"SOURCE_SYSTEM_NAME",
			"FROM DATE",
			"TO DATE",
			"RESEND_MSG_IND",
			};

	private Menu menuBar = null;
	public void run(Display display)
	{
		for(int i=0;i< 10;i++)
		{
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){				
			}
			display.asyncExec(new Runnable() {
				public void run()
				{
				if(progressBar.isDisposed()) return;
				progressBar.setSelection(progressBar.getSelection() + 1);
				}
			});
		}
	}
	private void createProcessTab() {
		processTab = new TabFolder(sShell, SWT.NONE);
		createConnect();
		createConnectComposite();
		progressBar.setMinimum(0);
		progressBar.setMaximum(8);
		processTab.setBounds(new Rectangle(10, 24, 323, 435));
		TabItem tabItem = new TabItem(processTab, SWT.NONE);
		tabItem.setText("Exception Payload");
		tabItem.setControl(connectComposite);
		TabItem tabItem2 = new TabItem(processTab, SWT.NONE);
		tabItem2.setText("Connect to DB");
		tabItem2.setControl(connect);
		file.setPath(new File("C://Windows//BrokerDBConf.ser").getAbsolutePath());
		center(sShell);
		dbAuditStatus.setVisible(true);
		if (file.isFileExist() && !(file.isFileContentEmpty()))
		{
			db.setConnectionStatus(true);
			ArrayList<String>inList = new ArrayList<String>();
			try {
				inList = file.DeSerializeFomFile();
				dbUsername = inList.get(0);
				dbPassword = inList.get(1);
				validateDB.setDbUsername(dbUsername);
				validateDB.setDbPassword(dbPassword);
				db.setDbUsername(dbUsername);
				db.setDbPassword(dbPassword);
				System.out.println(dbUsername +" "+dbPassword);
			}
				catch (IOException e1) {
					saveMe = false;
					file.deleteFile();		
				} catch (ClassNotFoundException e1) {
					file.deleteFile();
				}		
		}
		else
		{
			dbAuditStatus.setText("Please provide DB Credentials");
			dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		}
	}

	/**
	 * This method initializes connectComposite	
	 *
	 */
	private void createConnectComposite() {
		
		connectComposite = new Composite(processTab, SWT.NONE);
		connectComposite.setLayout(null);
		serviceName = new Text(connectComposite, SWT.BORDER);
		serviceName.setBounds(new Rectangle(92, 25, 213, 22));
		Interface = new Label(connectComposite, SWT.NONE);
		Interface.setBounds(new Rectangle(5, 30, 86, 16));
		Interface.setFont(new Font(Display.getDefault(), "Calibri", 10, SWT.NORMAL));
		Interface.setText("Interface Name");
		fetchAuditPayloadButton = new Button(connectComposite, SWT.NONE);
		fetchAuditPayloadButton.setBounds(new Rectangle(7, 353, 298, 21));
		fetchAuditPayloadButton.setText("Fetch Payload");
		db.setPayloadProc(fetchAuditPayloadButton);
		fetchAuditPayloadButton.setEnabled(false);
		fetchAuditPayloadButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {   
			
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {    
				
			if (serviceName != null && auditDBSchemaCombo.getText().length() > 1 )
			{
				final TableItem [] item = AuditEditable.getItems();
				if (auditCheckBoxes != null)
				{
					myAuditList = new HashMap<String, String>();
					for (int t =0 ; t < myAuditColumnNames.length ; t++)
					{
						final int sel = t;
						if (auditCheckBoxes[t].getSelection() == true)
						{
							myAuditList.put(item[sel].getText(0),item[sel].getText(1));
						}
					}
				}

				if (!(myAuditList.isEmpty()))
				{
						for(Map.Entry<String,String> entry : myAuditList.entrySet())
						{
							String key = entry.getKey();
							String values = entry.getValue();
							setVariablesAudit(key, values);
							System.out.println(key +" : "+values);
						}
						progressBar.setSelection(2);
						isAvailableAuditDetails = true;
						auditPayloadProcess();
						clearAuditALL();
						if (auditCheckBoxes != null)
						{
							for (int t = 0; t < myAuditColumnNames.length; t++) {
								if (auditCheckBoxes[t].getSelection())
									auditCheckBoxes[t].setSelection(false);
							}
						}
						
				}else
				{
					dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
					dbAuditStatus.setText("Kindly provide the details of the Interface");
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
					dbAuditStatus.setText("Connected To DB");
				}
			}else
			{
				dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
				dbAuditStatus.setText("Kindly provide the details of the Interface");
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
				dbAuditStatus.setText("Connected To DB");
			}
			}
		
		});
		
		
		progressBar = new ProgressBar(connectComposite, SWT.NONE);
		progressBar.setBounds(new Rectangle(8, 382, 296, 16));
		createAuditSchemaCombo();
		DBSchema_l = new Label(connectComposite, SWT.LEFT);
		DBSchema_l.setBounds(new Rectangle(5, 60, 75, 16));
		DBSchema_l.setFont(new Font(Display.getDefault(), "Calibri", 10, SWT.NORMAL));
		DBSchema_l.setText("DB Schema");
		
		AuditEditable = new Table(connectComposite, SWT.BORDER| SWT.MULTI );
		AuditEditable.setLinesVisible(true);
		AuditEditable.setHeaderVisible(true);
		AuditEditable.setFont(new Font(Display.getDefault(), "Calibri", 8, SWT.NORMAL));
		AuditEditable.setBounds(new Rectangle(8, 92, 297, 253));
		db.setProgressBar(progressBar);

		stopFetchAuditPayloadButton = new Button(connectComposite, SWT.NONE);
		stopFetchAuditPayloadButton.setBounds(new Rectangle(7, 353, 298, 21));
		stopFetchAuditPayloadButton.setVisible(false);
		db.setStopPayloadProc(stopFetchAuditPayloadButton);
		stopFetchAuditPayloadButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (payloadAuditProcess.isAlive())
				{
					payloadAuditProcess.interrupt();
					db.disconnectAll();
					if (payloadAuditProcess.isInterrupted())
					{
					System.out.println("Payload Thread Interupted");
					fetchAuditPayloadButton.setVisible(true);
					stopFetchAuditPayloadButton.setVisible(false);
					}
					
				}
			}
		});
		
		TableColumn column1 = new TableColumn(AuditEditable,SWT.LEFT);
		column1.setWidth(120);
		column1.setResizable(true);
		column1.setText("Table Columns");
		TableColumn column2 = new TableColumn(AuditEditable,SWT.LEFT);
		column2.setWidth(250);
		column2.setResizable(true);
		column2.setText("                 Fill Values Please              ");
		TableColumn column3 = new TableColumn(AuditEditable,SWT.LEFT);
		column3.setWidth(5);
		column3.setResizable(true);
		column3.setText("");
				
		for(String temp : myAuditColumnNames)
		{
			auditItem = new TableItem(AuditEditable,SWT.NONE);
			auditItem.setText(new String[] { temp });
		}
		
		final TableItem [] item = AuditEditable.getItems();
		auditCheckBoxes = new Button[myAuditColumnNames.length];  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String myPresentDate = dateFormat.format(date);
		
		for(int i=0;i< item.length ;i++)
		{
			TableEditor editor = new TableEditor(AuditEditable);
			editor.grabHorizontal=true;
			
			auditCheckBoxes[i] = new Button(AuditEditable,SWT.CHECK);
			if (item[i].getText().contains("DATE"))
			{
                item[i].setText(1, myPresentDate+"-00.00.00.000000");
    			editor.setEditor(auditCheckBoxes[i], item[i], 2);    
			}
			else
				editor.setEditor(auditCheckBoxes[i], item[i], 2);
		}
		
		column1.pack();
		column2.pack();
		column3.pack();
						
		final TableEditor editor = new TableEditor(AuditEditable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		AuditEditable.addListener(SWT.MouseDown, new Listener() {
	      public void handleEvent(Event event) {
	        Rectangle clientArea = AuditEditable.getClientArea();
	        Point pt = new Point(event.x, event.y);
	        int index = AuditEditable.getTopIndex();
	        while (index < AuditEditable.getItemCount()) 
	        {
	          boolean visible = false;
	          final TableItem item = AuditEditable.getItem(index);
	          for (int i = 0; i < AuditEditable.getColumnCount(); i++) {
	            Rectangle rect = item.getBounds(1);
	            if (rect.contains(pt)) {
	              final int column = 1;
	              final Text text = new Text(AuditEditable, SWT.NONE);
	              Listener textListener = new Listener() {
	                public void handleEvent(final Event e) {
	                  switch (e.type) {
	                  case SWT.FocusOut:
	                    item.setText(column, text.getText());
	                    text.dispose();
	                    break;
	                  case SWT.Traverse:
	                    switch (e.detail) {
	                    case SWT.TRAVERSE_RETURN:
	                      item
	                          .setText(column, text
	                              .getText());
	                    //FALL THROUGH
	                    case SWT.TRAVERSE_ESCAPE:
	                      text.dispose();
	                      e.doit = false;
	                    }
	                    break;
	                  }
	                }
	              };
	              text.addListener(SWT.FocusOut, textListener);
	              text.addListener(SWT.Traverse, textListener);
	              editor.setEditor(text, item, 1);
	              text.setText(item.getText(1));
	              text.selectAll();
	              text.setFocus();
	              return;
	            }
	            if (!visible && rect.intersects(clientArea)) {
	              visible = true;
	            }
	          }
	          if (!visible)
	            return;
	          index++;
	        }
	     }
	    });
	}
	
	void clearAuditALL()
	{
		myAuditList.clear();
		db.setPAYLOAD_MSG_TXT(null);
		db.setCOUNTRY_CODE(null);
		db.setStartDate(null);
		db.setToDate(null);
		db.setPROTOCOL_MSG_ID(null);
		db.setSRC_CMPNT_NAME(null);
		db.setSRC_SUB_CMPNT_NAME(null);
		db.setSRC_SERVER_NAME(null);
		db.setDAY_OF_MONTH_NBR(null);
		db.setRESEND_MSG_IND(null);
		db.setSOURCE_SYSTEM_NAME(null);
		System.out.println(myAuditList.isEmpty() ? "All Audit Values Appended- Clearing Map :TRUE"  : "All Values Appended- Clearing Map :FALSE");

	}
	  
	private void setVariablesAudit(String key, String value)
	{
		if (key.contains("PAYLOAD_MSG_TXT"))
		{
			db.setPAYLOAD_MSG_TXT(value.trim());
		}else
			if (key.contains("COUNTRY"))
			{
				db.setCOUNTRY_CODE(value.trim());
			}else
				if (key.contains("FROM DATE"))
				{
					db.setStartDate(value.trim());
				}else
					if (key.contains("TO DATE"))
					{
						db.setToDate(value.trim());
					}else
						if (key.contains("PROTOCOL"))
						{
							db.setPROTOCOL_MSG_ID(value.trim());
						}else
								if (key.contains("SRC_CMPNT_NAME"))
								{
									db.setSRC_CMPNT_NAME(value.trim());
								}else
									if (key.contains("SRC_SUB_CMPNT_NAME"))
									{
										db.setSRC_SUB_CMPNT_NAME(value.trim());
									}else
										if (key.contains("SRC_SERVER_NAME"))
										{
											db.setSRC_SERVER_NAME(value.trim());
										}else
											if (key.contains("DAY_OF_MONTH_NBR"))
												{
												db.setDAY_OF_MONTH_NBR(value.trim());
												}else
													if (key.contains("RESEND_MSG_IND"))
														{
														db.setRESEND_MSG_IND(value.trim());
														}
													else
														if (key.contains("SOURCE_SYSTEM_NAME"))
														{
															db.setSOURCE_SYSTEM_NAME(value.trim());
														}
	
	}
	
	private void auditPayloadProcess()
	{
		if (isAvailableAuditDetails)
		{
			System.out.println(serviceName.getText());
			db.setSERVICE_NAME(serviceName.getText());
			progressBar.setSelection(3);		
			db.makeConnection();
			System.out.println("Audit - Acquired Values and Made connection !");
			progressBar.setSelection(4);
			db.getResultSet();
			System.out.println("Audit - ResultSet Fetched !");
			connectDbButton.setEnabled(false);
			if (db.getConnectionStatus() && db.isCanFetchPayload())
			{
				stopFetchAuditPayloadButton.setVisible(true);
				stopFetchAuditPayloadButton.setText("Stop");
				fetchAuditPayloadButton.setVisible(false);
				payloadAuditProcess = new Thread(db);
				System.out.println("Audit - Starting Thread !");
				payloadAuditProcess.start();
			}
			else
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dbAuditStatus.setText("Empty Resultset / Please check values");
				dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
				progressBar.setSelection(0);	
			}
		}
	}
	
	/**
	 * This method initializes connect	
	 *
	 */
	private void createConnect() {
		connect = new Composite(processTab, SWT.NONE);
		connect.setLayout(null);
		username = new Text(connect, SWT.BORDER);
		username.setBounds(new Rectangle(110, 25, 171, 26));
		username.addListener(SWT.Verify, new Listener(){
			
			public void handleEvent(Event e)
			{
				Text source = (Text)e.widget;
				String oldString = source.getText();
				String textString =  oldString.substring(0, e.start) + 	e.text + oldString.substring(e.end);								
				dbUsername = textString;
			}
		});
		password = new Text(connect, SWT.BORDER | SWT.PASSWORD);
		password.setBounds(new Rectangle(110, 60, 171, 26));
		password.setEchoChar('*');
		password.addListener(SWT.Verify, new Listener(){
			
			public void handleEvent(Event e)
			{
				Text source = (Text)e.widget;
				String oldString = source.getText();
				String textString =  oldString.substring(0, e.start) + 	e.text + oldString.substring(e.end);								
				dbPassword = textString;
			}
		});
		
		Username = new Label(connect, SWT.CENTER);
		Username.setBounds(new Rectangle(5, 30, 66, 16));
		Username.setText("Username");
		password_l = new Label(connect, SWT.CENTER);
		password_l.setBounds(new Rectangle(5, 65, 66, 16));
		password_l.setText("Password");
		connectDbButton = new Button(connect, SWT.NONE);
		connectDbButton.setBounds(new Rectangle(35, 110, 221, 25));
		connectDbButton.setText("Connect");
		db.setConnectDbButton(connectDbButton);
		connectDbButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				if ( e.type == SWT.Selection )
				{
					if ( saveMe && username.getText() != null && password.getText() != null  )
					{					
						file.deleteFile();
						ArrayList<String>list = new ArrayList<String>();
						list.add(dbUsername);
						list.add(dbPassword);	
						validateDB.validateDBConnection(dbUsername, dbPassword);
						if (validateDB.isConnnectionStatus())
						{
							validateDB.setDbUsername(dbUsername);
							validateDB.setDbPassword(dbPassword);
							db.setDbUsername(dbUsername);
							db.setDbPassword(dbPassword);
							fetchAuditPayloadButton.setEnabled(true);
							file.setDataList(list);
							file.SerializeToFile();		
							dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
							dbAuditStatus.setText("Connected To DB");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
							dbAuditStatus.setText("Connected To DB");
						}else
						{
							file.deleteFile();
							fetchAuditPayloadButton.setEnabled(false);
							dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
							dbAuditStatus.setText("Enter Valid DB Credentials");
						}				
					}		
				}
			}		
		});

		cRemember = new Button(connect, SWT.CHECK);
		cRemember.setBounds(new Rectangle(5, 155, 16, 16));
		cRemember.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				saveMe = true;
			}
		});
		ClickToRemember = new Label(connect, SWT.NONE);
		ClickToRemember.setBounds(new Rectangle(30, 155, 251, 16));
		ClickToRemember.setText("Check To Remember");
		
		ClearMeNowButton = new Button(connect, SWT.CHECK);
		ClearMeNowButton.setBounds(new Rectangle(5, 155, 16, 16));
		ClickToErase = new Label(connect, SWT.NONE);
		ClickToErase.setBounds(new Rectangle(30, 155, 251, 16));
		ClickToErase.setText("Clear Me");
	}

	/**
	 * This method initializes schema	
	 *
	 */
	private void createAuditSchemaCombo() {
		auditDBSchemaCombo = new Combo(connectComposite, SWT.NONE);
		auditDBSchemaCombo.setBounds(new Rectangle(92, 55, 213, 21));
		auditDBSchemaCombo.add("");
		auditDBSchemaCombo.add("GM4P");
		auditDBSchemaCombo.add("DSNU");
		auditDBSchemaCombo.add("QA");
		auditDBSchemaCombo.add("FUT");
		
		auditDBSchemaCombo.addSelectionListener(new SelectionListener(){
			
			public void widgetSelected(SelectionEvent arg0) {
				int selection = auditDBSchemaCombo.getSelectionIndex();
				String dbSchema = auditDBSchemaCombo.getItem(selection);
				if (dbSchema.length() > 1)
					db.setDbSchema(dbSchema);
			}		
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}			
		});

	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
		 * for the correct SWT library path in order to run with the SWT dlls. 
		 * The dlls are located in the SWT plugin jar.  
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		
		Thread myMainThread = new Thread(new Runnable(){
			
			public void run(){
				
				Display display = Display.getDefault();
				AuditFetch thisClass = new AuditFetch();
				thisClass.createSShell();
				
				thisClass.sShell.open();
				
				while (!thisClass.sShell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
				display.dispose();
				
			}
		});
		
		myMainThread.start();
	}
	
	/**
	 * This method places the shell in the center of the screen
	 */
	public static void center(Shell shell)
	{
		Rectangle bds = shell.getDisplay().getBounds();
		Point p = shell.getSize();
		int nLeft = (bds.width - p.x) /2 ;
		int nTop = (bds.height - p.y) /2;	
		shell.setBounds(nLeft, nTop, p.x, p.y);	
	}
	
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		center(sShell);
		sShell.setText("Exception Payload Fetcher");
		menuBar = new Menu(sShell, SWT.BAR);
		dbAuditStatus = new Label(sShell, SWT.NONE);
		dbAuditStatus.setBounds(new Rectangle(10, 470, 321, 19));
		dbAuditStatus.setVisible(false);
		file = new ConnectFile();	
		db= new ConnectAuditDB();
		db.setDisplay(Display.getCurrent());
		db.setDbStatus(dbAuditStatus);
		validateDB = new ValidateDB();
		createProcessTab();
		sShell.setMenuBar(menuBar);
		sShell.setSize(new Point(350, 535));
		sShell.setLayout(null);	
		Thread validateDBThread = new Thread(validateDB);
		validateDBThread.start();
		System.out.println(validateDB.isConnnectionStatus());
		try {
			validateDBThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (validateDB.isConnnectionStatus() && dbUsername != null && dbPassword != null)
		{
			fetchAuditPayloadButton.setEnabled(true);
			cRemember.setVisible(false);
			ClickToRemember.setVisible(false);
			ClearMeNowButton.setVisible(true);
			ClickToErase.setVisible(true);
			dbAuditStatus.setVisible(true);
			dbAuditStatus.setText("Connected To DB : "+validateDB.getDbUsername());		
			dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));					
			username.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			password.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			
			ClearMeNowButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				@Override
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					dbAuditStatus.setVisible(false);
					username.setText("");
					password.setText("");
					fetchAuditPayloadButton.setEnabled(false);
					ClearMeNowButton.setVisible(false);
					ClickToErase.setVisible(false);
					cRemember.setVisible(true);
					ClickToRemember.setVisible(true);
					username.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					password.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));					
				}
			});
		}else
		{
			dbAuditStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));	
			dbAuditStatus.setText("Enter Valid DB Credentials");			
		}
		}

}