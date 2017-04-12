package com.code;


import java.io.*;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


/** JDK before version 7. */
public class ConnectFile {

	
	String storePath;
	ArrayList<String> dataList;
	private String path;
	private String dirName;
	
	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public ConnectFile(String path,ArrayList<String> dataString) {
		super();
		this.path = path;
		this.dataList = dataString;
	}
	
	public ConnectFile(String path) {
		super();
		this.path = path;
	}

	public ConnectFile() {
		super();
	}
	
	boolean isFileExist()
	{
        //use buffering
        File file = new File(this.path);
        return (file.exists());
	}
	
	boolean deleteFile()
	{
		if(this.isFileExist())
		{
			File file = new File(this.getPath());
			return (file.delete());
		}
		return false;
	}

	boolean isFileContentEmpty()
	{
		try{
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(this.path));
		if(reader.readLine() == null)
			return true;
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException d)
		{
			d.printStackTrace();
		}
		return false;
	}
	
	static public boolean deleteDirectory(File path)
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
	
	public static void trgDirCreate(String dirName)
	{
		final File homeDir = new File(System.getProperty("user.home"));
		final File path = new File(homeDir,dirName);
		if (!path.exists())
		{
			path.mkdir();
		}
		if (deleteDirectory(path))
		{
			path.mkdir();	
		}
	}
	
	
	public static String clobToString(java.sql.Clob data)
	{
	    final StringBuilder sb = new StringBuilder();
	 
	    try
	    {
	        final Reader         reader = data.getCharacterStream();
	        final BufferedReader br     = new BufferedReader(reader);
	 
	        int b;
	        while(-1 != (b = br.read()))
	        {
	            sb.append((char)b);
	        }
	 
	        br.close();
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace();
	    }
	    catch (IOException e)
	    {
	    	e.printStackTrace();
	    }
	 
	    return sb.toString();
	}

	public static void writingClobToFile(Clob data,Timestamp timeStampRq)
	{
		String timeStamp = timeStampRq.toString().replaceAll(" ","").replaceAll(".", "_");
		
		final File homeDir = new File(System.getProperty("user.home"));
		final File dir = new File(homeDir,"Payload");
		File file = new File(dir.getAbsolutePath()+"_"+timeStamp+".txt");
				FileOutputStream out;
				PrintStream p;
				try{
					if (!file.exists())
					{
						out = new FileOutputStream(file,true);
						p = new PrintStream(out);
						p.println(data.getSubString(1, (int)data.length()-1));
						p.close();
					}
				}catch(Exception e)
				{
				e.printStackTrace();
				}
	
	}
	
	public static void fileOperation(String servName, ArrayList<String> myList) throws IOException
	{	
		final File homeDir = new File(System.getProperty("user.home"));
		final File dir = new File(homeDir,"Payload");
		String targetFile = dir.getAbsolutePath()+"\\"+servName+".txt";
		File file = new File(targetFile);
		FileOutputStream out;
		PrintStream p;
		try{
			if (!file.exists())
			{
				out = new FileOutputStream(file,false);
				p = new PrintStream(out);
				for (int  i =0; i< myList.size() ;i++)
					p.println(myList.get(i));
				p.close();
			}
			else
				{
					out = new FileOutputStream(file,true);
					p = new PrintStream(out);
					for (int  i =0; i< myList.size() ;i++)
						p.println(myList.get(i));
					p.close();
				}
		   }catch(Exception e)
			{
				e.printStackTrace();
			}		
	}
	

	
	public static void getConsolidatedData(ResultSet resultSet,String servName,String dirName)
	{
		String payload_request_id =null;
		ArrayList<String>myList = new ArrayList<String>();
        trgDirCreate(dirName);
        try {
			while(resultSet.next())
			{
				java.sql.Clob payloadMsgTxt =resultSet.getClob(1);
				Timestamp timeStampRq = resultSet.getTimestamp(2);
				System.out.println(payload_request_id);
				myList.add(payload_request_id+"		"+timeStampRq);
				writingClobToFile(payloadMsgTxt, timeStampRq);
				fileOperation(servName, myList);
			}
		} catch (SQLException e) 
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}      
        System.out.println("OPERATION COMPLETE..");
	}
	
	boolean SerializeToFile()
	{
	    //serialize the List
	    //note the use of abstract base class references
	    try{
	        //use buffering
	         OutputStream file = new FileOutputStream(this.getPath());
	         OutputStream buffer = new BufferedOutputStream(file);
	         ObjectOutput output = new ObjectOutputStream(buffer);
	         try{
	           output.writeObject(this.dataList);
	         }
	         finally{
	           output.close();
	           buffer.close();
	           file.close();
	         } 
	       }  
	       catch(IOException ex){
	         ex.printStackTrace();
	         return false;
	       }
           return true;
	}

	@SuppressWarnings("unchecked")
	ArrayList<String> DeSerializeFomFile() throws IOException,ClassNotFoundException
	{
		ArrayList<String> recoveredList = null;
	    //serialize the List
	    //note the use of abstract base class references
	 		 	InputStream file = new FileInputStream(this.getPath());
	 		 	InputStream buffer = new BufferedInputStream(file);
	 		 	ObjectInput input = new ObjectInputStream (buffer);
	        //deserialize the List
	 		 	recoveredList = (ArrayList<String>) input.readObject();
	        //display its data
		        input.close();    	
		        buffer.close();
		        file.close();
	      return recoveredList;	      	
	}

	public ArrayList<String> getDataList() {
		return dataList;
	}

	public void setDataList(ArrayList<String> dataList) {
		this.dataList = dataList;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}