/*****************************************************************************
 * Media System Kernel
 * To load the sperate system
 * System files must put in "app" folder in (class/jar) files
 * 
 * <-System MUST include : ->
 * 
 * Variable :
 *   package <System name>;
 *   public String system = "";
 *   private String cmd = "";
 * 
 * Method :
 *   public int start();
 *   public String getSystemName();
 *   public void setCommand(String cmd);
 *   <constructor>
 *
 * Sebastian Ko 04/11/2012
 ****************************************************************************/
 
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;

public class MSKernel{
	/*
	 * Initialize Part
	 */
	public Scanner s = new Scanner(System.in);			//normal mode
	public static Scanner ss = new Scanner(System.in);	//static mode
	
	public static String systemDir 	= "app";
	public static Class sysClass	= null;
	public static Object sysObj 	= null;
	public static Method sysMethod	= null;
	public static String sysName	= "";
	
	public static int error			= -1;
	
	/*Main System*/
	public static void main(String argv[]){
		System.out.print("System Loader 06112012-0159\n");
		System.out.print("--------------------------------------------------\n");
		System.out.print("[Kernel]Search for system files\n");
		
		File path = new File(System.getProperty("java.class.path"));
		System.out.print("[Kernel]System path - "+path.getAbsolutePath()+"\n");
        
		
		error = loadClass(systemDir);	//class loader
		
		if(error == 0)System.out.print("[Kernel]System exit normally\n");
		else if(error == 1)System.out.print("[Kernel]System exit with error\n");
		System.exit(0);
	}
	
	@SuppressWarnings("unchecked")
	public static int loadClass(String dir){
		File directory 	= new File(dir);
		File fileList[] = directory.listFiles();
		
		try{
			if(fileList.length == 0){
				System.out.println("[Error]No files in system folder!");
				return 1;
			}
			
			for(int i = 0 ; i < fileList.length ; i++){
				if(fileList[i].isFile())
				{
					System.out.print("[Kernel]Check file(s) : "+fileList[i].getName()+"\n");
					/*
					 * Class Check
					 * Loaded with jar files
					 * if Class include value in "public String system "
					 * start the class
					 */
					try{
						String getPath	= "app\\"+fileList[i].getName();
						File sysFile	= new File(getPath);
						
						/* .jar use */
						URL classPath[]	= new URL[]{new URL("file:app\\"+fileList[i].getName())};
						ClassLoader cl	= new URLClassLoader(classPath,ClassLoader.getSystemClassLoader());
						
						Class<?> loadedClass = null;
						try{
							/* .jar use */
							loadedClass	= Class.forName((fileList[i].getName()).replace(".jar","")+"."+(fileList[i].getName()).replace(".jar",""),true,cl);	//Link to class
						}catch(Exception ex){
							System.out.println("[Error]"+ex);
						}catch(NoClassDefFoundError exx){
							System.out.println("[Error]"+exx);
						}
						
						Object objClass		= loadedClass.newInstance();							//Initialize Class
						
						Method checkMethod = loadedClass.getDeclaredMethod("getSystemName");		//assign declared class method
						Object ans = checkMethod.invoke(objClass);									//invoke it
						
						if(!((String)ans).equals("")){
							sysClass	= loadedClass;
							sysObj		= objClass;
							sysMethod	= sysMethod;
							sysName 	= (String)ans;
						}
					}catch(Exception ex){
						System.out.println("[Error]"+ex);
					}
				}
			}
			
			/*
			 * Start services
			 */
			if(sysName.equals("")){
				System.out.print("[Kernel]No Services found. Program will exit.\n");
				return 0;
			}else{
				System.out.print("[Kernel]Services loaded : "+sysName+"\n");
				System.out.print("[Kernel]Ready to start "+sysName+" services...\n");
				
				try{
					new Thread(new Runnable(){
						public void run(){
							startServices();
						}
					}).start();
					
				}catch(Exception e){
					System.out.println("[Error]"+e);
				}
				
				while(true){
					if(error == 0)return 0;
				}
			}
			
		}catch(NullPointerException e){
			System.out.println("[Error]Folder \"app\" not found!");
			new File(systemDir).mkdir();
			return 1;
		}
	}
	
	
	
	
	/************************
	*Start Services
	************************/
	@SuppressWarnings("unchecked")
	public static void startServices(){
		int restart = 1; //services restart code
		do{
			if(restart == 1)
				try{
					restart 		= 0;
					sysMethod		= sysClass.getDeclaredMethod("start");
					Object respone 	= sysMethod.invoke(sysObj);
					
					/*COMMAND MODE*/
					do{
						System.out.print(">");
						String cmd	= ss.nextLine();
						
						sysMethod	= sysClass.getDeclaredMethod("setCommand",String.class);
						sysMethod.invoke(sysObj,cmd);
					}while(true);
				}catch(Exception e){
					System.out.println("[Error]"+e);
					System.out.print("[Error]Services down.\n[Kernel]System will restart automatically...\n");
					restart = 1;
				}
		}while(error != 1);
	}
}