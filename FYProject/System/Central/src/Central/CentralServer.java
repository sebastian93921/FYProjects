/*
 * Music Player Server Side / Central Server
 * 
 * This is the Central Server which used to check the availability to each Media Server
 * And provide admission control to the system
 * All Media Server are started with 17221 Port Number
 * 
 * Bug Fix: Registration check user data folder - 24/02/2013
 * 
 * Build Date 22/11/2012
 * 
 * Sebastian Ko
 */
package Central;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import net.AudioDetails;
import net.AudioElement;


abstract class csd extends SysPrint{
    public final String version = "20/06/2013-1800 Central Update 1";
    public final String system  = "Central";
    public static int TotalAudio  = 0;
    
    //Default Output Command
    public final String ERR        = "ERR";
    public final String ACK        = "ACK";
    public final String NULL       = "NULL";
    public final String noUserID   = "NOID";
    
    /*
     * Initialize
     */
    public static LinkedList<AudioDetails> audioList = null;
    
    public static mediaServerDetails mediaServer[]   = null;
    public static int port                           = 0;
    public final String dataPath                    = ".\\data\\";
    public final String userData                    = ".\\userData\\";
    public final String userNameFolder              = dataPath+"usr\\";
    
    /*
     * Client Version Format
     * 0.0.0-0
     * ^ ^ ^ ^
     * | | | Build Test version
     * | | Minor version
     * | Major version
     * Major System version
     */
    public String clientVersion    = "0.0.0-1";    
    
    public static boolean shutdown = false;
    
    public static char code[] = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T',
                    'U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q',
                    'r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','~','!','@','#','$','%',
                    '^','&','*','(',')','_','+','=','-','`','[',']','{','}',';','\'',':','\"','<','>','?',',','.','/'
                    };
}


public class CentralServer extends csd{
    private ServerSocket serSocket                  = null;     //Central Server Socket
   
    public CentralServer(int Port){
        //Initialize
        port = Port;
    }
        
    public int startServer(){
        sysPrint("Builded date : "+version);
        
        sysPrint("Checking Server States");
        getMediaServerList();
        
        sysPrint("Checking Data Path");
        if(new File(this.dataPath).exists())
            if(new File(this.dataPath).isDirectory());
            else
                new File(this.dataPath).mkdir();
        else
            new File(this.dataPath).mkdir();
        
        sysPrint("Checking Users Path");
        if(new File(this.userData).exists())
            if(new File(this.userData).isDirectory());
            else
                new File(this.userData).mkdir();
        else
            new File(this.userData).mkdir();
        
        
        sysPrint("Checking Total Audio ID");
        try{
            BufferedReader tempIDFileReader = new BufferedReader(new FileReader(dataPath+"tid.sav"));
            TotalAudio = Integer.parseInt(tempIDFileReader.readLine());
            tempIDFileReader.close();
        }catch(Exception e){
            errPrint("Fail to read audio ID file");
            TotalAudio = 1;
        }
        
        sysPrint("Reading Audio List");
        try{
            ObjectInputStream audioListFile = new ObjectInputStream(new FileInputStream(dataPath+"tal.dat"));
            this.audioList                  = (LinkedList<AudioDetails>)audioListFile.readObject();
        }catch(Exception e){
            errPrint("Fail to read audio list");
            audioList                       = new LinkedList<AudioDetails>();
        }
        
        sysPrint("Admin Command Start");
        adminMode();
        
        sysPrint("Central Server Start");
        try {
            serSocket = new ServerSocket(port);
            sysPrint("Starting in Port : "+port);
            
            while(true){
                /*
                 * Start new thread
                 */
                if(!shutdown){
                    Socket newConnection = serSocket.accept();
                    new CServerConnections(newConnection).start();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    
    public static void main(String argv[]){
        System.out.print(
                "=================================\n"+
                "    Central Server BETA\n"+
                "=================================\n"
                );
        new CentralServer(17220).startServer();
    }
    
    private void getMediaServerList(){
        List<mediaServerDetails> serverList = new LinkedList<mediaServerDetails>();
        this.mediaServer                    = null;
        
        try {
            /*
             * Read Server List File
             */
            BufferedReader in = new BufferedReader(new FileReader("mediaList.txt"));
            String getLine = "";
            try {
                String Locat = null;  //Set Location of Server
                
                if(in.readLine().equals(":MSLHeader")){
                    while((getLine = in.readLine()) != null){
                        //Comment Line
                        if(getLine.startsWith("//") || getLine.equals("\n"));
                        //Client version
                        else if(getLine.startsWith(":ClientVersion")){
                            clientVersion   = in.readLine();
                        //Location Set
                        }else if(getLine.startsWith("::")){
                            Locat = getLine.replaceAll("::","");
                        //Server Set
                        }else if(Locat != null){
                            String serverID[] = getLine.split("#");
                            try{
                                serverList.add(new mediaServerDetails(serverID[0],Locat,Integer.parseInt(serverID[1]),this.port));
                            }catch(Exception e){
                                //ERROR with this list - DO nothing
                            }
                        }
                    }
                }
                
                in.close(); //Finaly clean the memory
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            serverList.add(new mediaServerDetails("127.0.0.1","Loopback",17221,this.port));
            try{
                PrintWriter confOut = new PrintWriter(new FileWriter("mediaList.txt",false));
            
                confOut.println(":MSLHeader");
                confOut.println("//MSLHeader = Media Server List Header");
                confOut.println();
                confOut.println("//::<Location>");
                confOut.println("//eg ::HK ::Loopback");
                confOut.println();
                confOut.println("//::Loopback means the server is running within the same server with Central Server");
                confOut.println("//<ip>#<port number>");
                confOut.println();
                confOut.println("::Loopback");
                confOut.println("127.0.0.1#17221");
                confOut.println();
                confOut.println("::HK");
                confOut.println("//Null Value");
                confOut.println();
                confOut.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        //Save to the array
        mediaServer = new mediaServerDetails[serverList.size()];
        for(int i = 0 ; i < mediaServer.length;i++)
            mediaServer[i] = serverList.get(i);
        
        /*
         * Check available server
         */
        int currentAva = 0;
        for(int i = 0 ; i < mediaServer.length;i++){
            if(mediaServer[i].available()){
                sysPrint("Media Server Available - "+mediaServer[i].getIP()+" # "+mediaServer[i].getLocation());
                currentAva++;
            }
        }
        sysPrint("Available Server : "+currentAva);
    }
    
    
    /* #####################################################################################
     * Command Mode for admin
     * Command
     * 1/ReloadList - reload server list
     * 2/Shutdown   - Shutdown Server
     * #####################################################################################
     */
    private void adminMode(){
        new Thread(new Runnable(){
            public void run(){
                Scanner sc = new Scanner(System.in);
                String cmd = "";
                while(true){
                    /*
                     * Admin Command
                     */
                    System.out.print("");
                    cmd = sc.nextLine();
                    
                    if(cmd.equals("")){                     //==================================================================================
                        errPrint("Wrong input");
                    }else if(cmd.equals("ReloadList")){     //==================================================================================
                        getMediaServerList();
                    }else if(cmd.equals("Shutdown")){       //==================================================================================
                        sysPrint("Server shutting down in 10s...");
                        shutdown = true;
                        try {
                            /*
                             * Save Last SongID
                             */
                            PrintWriter tidWriter = new PrintWriter(new FileWriter(dataPath+"tid.sav", false));  
                            tidWriter.print(TotalAudio);
                            tidWriter.close();

                            /*
                             * Save Total Audio List to File
                             */
                            ObjectOutputStream totalAudioList = new ObjectOutputStream ( new FileOutputStream(dataPath+"tal.dat",false));
                            totalAudioList.writeObject(audioList);
                            totalAudioList.close();
                        } catch (Exception ex) {}
                        try{Thread.sleep(10000);} catch (Exception ex) {}
                        System.exit(0);
                    }else{                                  //==================================================================================
                        errPrint("Unknown Command");
                        //Unknown Command
                    }
                }
            }
        }).start();
    }
}



class CServerConnections extends csd{
    private Socket connection  = null;     //Media Server Socket
    
    public CServerConnections(Socket getSocket){
        connection = getSocket;
    }
    
    public void run(){
        boolean wrongCmd        = true;
        
        /*
         * User details
         */
        String userID = null;   //Save userID for further function
        
        ObjectInputStream in    = null;
        try {
            ObjectOutputStream out  = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            
            sysPrint("Client Connected IP > "+connection.getInetAddress());
            
            while(true){
                String Command = (String)in.readObject();
                /* ===========================================================================================
                 * Command MODE
                 * Media Server Command handling
                 * ===========================================================================================
                 */
                if(Command.startsWith("M")){
                    wrongCmd    = false;
                    switch(Integer.parseInt(Command.replaceAll("M",""))){
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Get Total Audio Number
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 1: 
                            out.writeObject(TotalAudio);
                            TotalAudio++;
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Upload Song Information
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 2:
                            out.writeObject("#AD");
                            try{
                                AudioDetails tempDetails = (AudioDetails)in.readObject();
                                boolean check   = false;
                                int doubledAudio= -1;
                                int pass        = 0;    //If pass > 2 fail checking
                                sysPrint("Total Audio Number > "+audioList.size());
                                for(int i = 0;i < audioList.size();i++){
                                    try{
                                        if(audioList.get(i).getTagTitle().equals(tempDetails.getTagTitle()))pass++;
                                        if(audioList.get(i).getTagTrack().equals(tempDetails.getTagTrack()))pass++;
                                        if(audioList.get(i).getTagArtist().equals(tempDetails.getTagArtist()))pass++;
                                        if(audioList.get(i).getTagAlbum().equals(tempDetails.getTagAlbum()))pass++;
                                        if(!(audioList.get(i).getDurationInSec() == tempDetails.getDurationInSec()))pass = 0;   //Set if coming from different file
                                    }catch(Exception e){/*DO NOTHING*/}
                                    if(pass > 3 && tempDetails.getPerm() == AudioDetails.NET_PUBLIC){
                                        errPrint("Repeated Value Number > "+pass);
                                        doubledAudio    = i;
                                        check           = false;
                                        break;
                                    }
                                    check   = true;
                                    pass    = 0;
                                }
                                if(check || audioList.size() == 0){
                                    audioList.add(tempDetails);
                                    out.writeObject(ACK);
                                    sysPrint("Save New Audio > "+audioList.get(audioList.size()-1).getTagTitle());
                                }else{
                                    out.writeObject("#Repeated - ID:"+audioList.get(doubledAudio).getSongID());
                                    TotalAudio--;
                                }
                            }catch(Exception e){
                                out.writeObject(ERR);
                                e.printStackTrace();
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Ask For Song Path with ID
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 3:
                            try{
                                out.writeObject("#GetID");
                                int getSongID       = (Integer)in.readObject();
                                
                                boolean haveAudio   = false;
                                
                                sysPrint("Passing Audio Path");
                                for(int i = 0;i < audioList.size(); i++){
                                    if(audioList.get(i).getSongID() == getSongID){
                                        sysPrint("Complete");
                                        out.writeObject(audioList.get(i).getPath());
                                        haveAudio = true;
                                        
                                        break;
                                    }
                                }
                                if(!haveAudio){
                                    errPrint("Pass Fail");
                                    out.writeObject(NULL);
                                }
                            }catch(Exception e){
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Ask available Media Server
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 4:
                            try{
                                sysPrint("Check available Media Server");
                                out.writeObject("#Port");
                                String ownIP    = connection.getInetAddress().toString().replaceAll("/", "");
                                int ownPort     = (Integer)in.readObject();
                                
                                boolean ava = false, noFile = true;
                                for(int i = 0 ; i < mediaServer.length;i++){
                                    if(!(mediaServer[i].getIP().equals(ownIP)) || mediaServer[i].getPort() != ownPort){
                                        if(mediaServer[i].available()){
                                            sysPrint("Pass ip to requested Server");
                                            out.writeObject("#IP "+mediaServer[i].getIP());
                                            if(in.readObject().equals("ACK")){
                                                out.writeObject("#Port "+mediaServer[i].getPort());
                                            }else
                                                break;      //ERROR
                                            ava = true;     //Checked

                                            if(in.readObject().equals(ACK)){   //ACK/NULL
                                                noFile = false;
                                                break;
                                            }else
                                                noFile = true;
                                        }
                                    }
                                }
                                if(!ava){
                                    errPrint("Fail to check Media Server");
                                    out.writeObject(ERR);
                                }else if(noFile){
                                    errPrint("No submitted file in Media Server(s)");
                                    out.writeObject(NULL);
                                }else
                                    out.writeObject(ACK);
                            }catch(Exception e){
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Receive User's Uploaded Song ID
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 5:
                            out.writeObject("#UID/SGID");
                            String uploadedUID  = null;
                            int uploadedID      = -1;
                            try{
                                String uploadSG[]   = ((String)in.readObject()).split("/");
                                uploadedUID         = uploadSG[0];
                                uploadedID          = Integer.parseInt(uploadSG[1]);
                                
                                out.writeObject(ACK);
                            }catch(Exception e){
                                e.printStackTrace();
                                out.writeObject(ERR);
                            }
                            
                            try{
                                if(uploadedUID == null)throw new Exception();
                                
                                //Check dir
                                if(new File(this.userData+"\\"+uploadedUID).exists()){
                                    if(new File(this.userData+"\\"+uploadedUID).isDirectory());
                                    else
                                        new File(this.userData+"\\"+uploadedUID).mkdir();
                                }else
                                    new File(this.userData+"\\"+uploadedUID).mkdir();
                                
                                LinkedList<AudioDetails> UPList = new LinkedList<AudioDetails>();
                                
                                if(new File(this.userData+"\\"+uploadedUID+"\\"+"upl.dat").exists()){     //Uploaded List
                                    ObjectInputStream readUpFile = new ObjectInputStream(new FileInputStream(this.userData+"\\"+uploadedUID+"\\"+"upl.dat"));
                                    UPList                      = (LinkedList<AudioDetails>)readUpFile.readObject();
                                    readUpFile.close();
                                }
                                
                                boolean haveAudio   = false;
                                boolean sameID      = false;
                                for(int j = 0 ; j < UPList.size();j++){
                                    if(UPList.get(j).getSongID() == uploadedID){
                                        errPrint("Found Same ID in User's List");
                                        sameID = true;
                                        
                                        break;
                                    }
                                }
                                       
                                for(int i = 0;i < audioList.size(); i++){
                                    if(audioList.get(i).getSongID() == uploadedID){
                                        sysPrint("Found Uploaded Audio Data");
                                        
                                        if(!sameID)UPList.add(audioList.get(i));
                                        
                                        haveAudio = true;
                                        break;
                                    }
                                }
                                
                                
                                if(haveAudio){
                                    ObjectOutputStream upFile = new ObjectOutputStream ( new FileOutputStream(this.userData+"\\"+uploadedUID+"\\"+"upl.dat",false));
                                    upFile.writeObject(UPList);
                                    upFile.close();
                                    
                                    sysPrint("User ID : "+uploadedUID+" Uploaded List >");
                                    for(int i = 0 ; i < UPList.size();i++){
                                        sysPrint(">"+UPList.get(i).getTagTitle());
                                    }
                                }else
                                    throw new Exception();
                            }catch(Exception e){
                                e.printStackTrace();
                                errPrint("Fail to SAVE user's new Upload ID");
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Not the group of command.
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        default:
                            wrongCmd    = true;
                    }
                }
                
                
                
                /* ===========================================================================================
                 * Command MODE
                 * Client Command handling
                 * ===========================================================================================
                 */
                if(Command.startsWith("X")){
                    wrongCmd    = false;
                    switch(Integer.parseInt(Command.replaceAll("X",""))){
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Client Login
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 1: 
                            /*
                             * Login
                             * Format:
                             * 1/ Name
                             * 2/ Out
                             * 3/ Pass
                             * 4/ Out ID
                             * 
                             * user file:
                             * 1 byte = header 0xAD
                             * 1 byte = assigning position A/B/C/D/E/F/G
                             * 1 integer 0x00000000
                             * 
                             * Assigning position
                             * A = 0x00
                             * B = 0x01
                             * C = 0x02
                             * D = 0x03
                             * E = 0x04
                             * F = 0x05
                             * F = 0x06
                             */
                            try{
                                int res           = -1; //Login response
                                String tempID     = ""; //Put user ID
                                
                                out.writeObject("#Nam");
                                String logName = (String)in.readObject();

                                out.writeObject("#Pas");
                                String logPass = (String)in.readObject();

                                
                                if(new File(userNameFolder+logName+".dat").exists()){
                                    FileInputStream readUser = new FileInputStream(new File(this.userNameFolder+logName+".dat"));
                                    
                                    byte[] readBuffer = new byte[6];
                                    readUser.read(readBuffer);
                                    readUser.close();
                                    if(!new String(readBuffer,0,1).equals("x"))throw new Exception("Read user data fail > "+logName+".dat");
                                    
                                    switch(readBuffer[1]){
                                        case 0x00:tempID+="A";
                                            break;
                                        case 0x01:tempID+="B";
                                            break;
                                        case 0x02:tempID+="C";
                                            break;
                                        case 0x03:tempID+="D";
                                            break;
                                        case 0x04:tempID+="E";
                                            break;
                                        case 0x05:tempID+="F";
                                            break;
                                        case 0x06:tempID+="G";
                                            break;
                                    }
                                    int tempNum     = (readBuffer[2] << (8*3))&0xFF000000 | 
                                                    (readBuffer[3] << (8*2))&0x00FF0000 | 
                                                    (readBuffer[4] << (8*1))&0x0000FF00 |
                                                    (readBuffer[5] << (8*0))&0x000000FF;
                                    
                                    if(tempNum%11 == 0){
                                        tempID += new Integer(tempNum/11).toString();
                                        
                                        if(new File(userData+tempID+"\\").isDirectory()){
                                            if(new File(userData+tempID+"\\ps.dat").exists()){
                                                res = checkPass(tempID,logPass);
                                            }else
                                                throw new Exception("Fail to read user data");
                                        }else
                                            throw new Exception("Wrong User Folder ID > "+tempID);
                                    }else
                                        throw new Exception("Wrong User ID > "+logName+".dat");
                                }else
                                    throw new Exception("No this user found > "+logName);
                                /*
                                 * Testing purpose
                                 */
                                if(res == -1){
                                    throw new Exception("Login Fail");
                                }
                                
                                userID  = tempID;
                                out.writeObject(userID);
                            }catch(Exception e){
                                errPrint(e.getMessage());
                                
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Version confirm
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 2:
                            out.writeObject(this.clientVersion);
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Passing media server to user
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 3:
                            boolean ava = false;
                            for(int i = 0 ; i < mediaServer.length;i++){
                                if(mediaServer[i].available()){
                                    out.writeObject("#IP "+mediaServer[i].getIP());
                                    if(in.readObject().equals("ACK")){
                                        out.writeObject("#Port "+mediaServer[i].getPort());
                                    }else
                                        break;      //ERROR
                                    ava = true;     //Checked
                                    break;
                                }
                            }
                            if(!ava)out.writeObject(ERR); //Print ERROR
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Getting AudioDetails
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 4:
                            try{
                                out.writeObject("#ADID");
                                int getSongID       = (Integer)in.readObject();

                                boolean haveAudio   = false;

                                sysPrint("Passing Audio Details");
                                for(int i = 0;i < audioList.size(); i++){
                                    if(audioList.get(i).getSongID() == getSongID){
                                        sysPrint("Audio Found");
                                        out.writeObject(audioList.get(i));
                                        haveAudio = true;

                                        break;
                                    }
                                }
                                if(!haveAudio){
                                    errPrint("Pass Fail");
                                    out.writeObject(NULL);
                                }
                            }catch(Exception e){
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Receive User's Library
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 5:
                            if(userID != null){
                                //Check dir
                                if(new File(this.userData+"\\"+userID).exists()){
                                    if(new File(this.userData+"\\"+userID).isDirectory());
                                    else
                                        new File(this.userData+"\\"+userID).mkdir();
                                }else
                                    new File(this.userData+"\\"+userID).mkdir();

                                out.writeObject("#MSL");    //Music Library
                                try{
                                    LinkedList<AudioElement> MSList = (LinkedList<AudioElement>)in.readObject();
                                    /*
                                     * Save Music Library to File
                                     */
                                    ObjectOutputStream musicLibraryFile = new ObjectOutputStream ( new FileOutputStream(this.userData+"\\"+userID+"\\"+"msl.dat",false));
                                    musicLibraryFile.writeObject(MSList);
                                    musicLibraryFile.close();
                                    
                                    out.writeObject(ACK);
                                }catch(Exception e){
                                    e.printStackTrace();
                                    out.writeObject(ERR);
                                }
                            }else
                                out.writeObject(noUserID);
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Send User's Library
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 6:
                            if(userID != null){
                                //Check dir
                                if(new File(this.userData+"\\"+userID).exists()){
                                    if(new File(this.userData+"\\"+userID).isDirectory()){
                                        try{
                                            ObjectInputStream musicLibraryFile = new ObjectInputStream(new FileInputStream(this.userData+"\\"+userID+"\\"+"msl.dat"));
                                            out.writeObject((LinkedList<AudioElement>)musicLibraryFile.readObject());
                                            musicLibraryFile.close();
                                        }catch(Exception e){
                                            out.writeObject(new LinkedList<AudioElement>());
                                        }
                                    }else
                                        out.writeObject(new LinkedList<AudioElement>());
                                }else
                                    out.writeObject(new LinkedList<AudioElement>());
                            }else
                                out.writeObject(noUserID);
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Send User's Uploaded List
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 7:
                            if(userID != null){
                                //Check dir
                                if(new File(this.userData+"\\"+userID).exists()){
                                    if(new File(this.userData+"\\"+userID).isDirectory()){
                                        try{
                                            ObjectInputStream uploadFile = new ObjectInputStream(new FileInputStream(this.userData+"\\"+userID+"\\"+"upl.dat"));
                                            LinkedList<AudioDetails> tempUpload = (LinkedList<AudioDetails>)uploadFile.readObject();
                                            uploadFile.close();
                                            
                                            for(int i = 0 ; i <tempUpload.size();i++){
                                                out.writeObject(tempUpload.get(i));
                                                if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                            }
                                            out.writeObject(ACK);
                                        }catch(Exception e){
                                            e.printStackTrace();
                                            out.writeObject(ACK);
                                        }
                                    }else
                                        out.writeObject(ACK);
                                }else
                                    out.writeObject(ACK);
                            }else
                                out.writeObject(noUserID);
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Searching from uploaded audio (by name)
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 8:
                            sysPrint("User's searching by name");
                            out.writeObject("#SearchName");
                            try{
                                String searchName = (String)in.readObject();//User's input name
                                
                                for(int i = 0 ; i < audioList.size();i++){
                                    if(audioList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                    if(audioList.get(i).getTagTitle().toUpperCase().equals(searchName.toUpperCase())){
                                        out.writeObject(audioList.get(i));
                                        sysPrint(">"+audioList.get(i).getTagTitle());
                                        if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                    }
                                }
                                /*Retry*/
                                for(int i = 0; i < audioList.size();i++){
                                    if(audioList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                    if(audioList.get(i).getTagTitle().toUpperCase().contains(searchName.toUpperCase())){
                                        out.writeObject(audioList.get(i));
                                        sysPrint(">"+audioList.get(i).getTagTitle());
                                        if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                    }
                                }
                                /**/
                                out.writeObject(ACK);   //No more result
                            }catch(Exception e){
                                e.printStackTrace();
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Searching from uploaded audio (by album)
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 9:
                            sysPrint("User's searching by album");
                            out.writeObject("#SearchAlbum");
                            try{
                                String searchAlbum = (String)in.readObject();//User's input name
                                
                                for(int i = 0 ; i < audioList.size();i++){
                                    if(audioList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                    if(audioList.get(i).getTagAlbum().toUpperCase().equals(searchAlbum.toUpperCase())){
                                        out.writeObject(audioList.get(i));
                                        sysPrint(">"+audioList.get(i).getTagTitle());
                                        if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                    }
                                }
                                /*Retry*/
                                for(int i = 0; i < audioList.size();i++){
                                    if(audioList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                    if(audioList.get(i).getTagAlbum().toUpperCase().contains(searchAlbum.toUpperCase())){
                                        out.writeObject(audioList.get(i));
                                        sysPrint(">"+audioList.get(i).getTagTitle());
                                        if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                    }
                                }
                                /**/
                                out.writeObject(ACK);   //No more result
                            }catch(Exception e){
                                e.printStackTrace();
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Searching from uploaded audio (by artist)
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 10:
                            sysPrint("User's searching by artist");
                            out.writeObject("#SearchArtist");
                            try{
                                String searchArtist = (String)in.readObject();//User's input name
                                
                                for(int i = 0 ; i < audioList.size();i++){
                                    if(audioList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                    if(audioList.get(i).getTagArtist().toUpperCase().equals(searchArtist.toUpperCase())){
                                        out.writeObject(audioList.get(i));
                                        sysPrint(">"+audioList.get(i).getTagTitle()+"/"+audioList.get(i).getTagArtist());
                                        if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                    }
                                }
                                /*Retry*/
                                for(int i = 0; i < audioList.size();i++){
                                    if(audioList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                    if(audioList.get(i).getTagArtist().toUpperCase().contains(searchArtist.toUpperCase())){
                                        out.writeObject(audioList.get(i));
                                        sysPrint(">"+audioList.get(i).getTagTitle()+"/"+audioList.get(i).getTagArtist());
                                        if(!in.readObject().equals(ACK))throw new Exception("Connection Error");
                                    }
                                }
                                /**/
                                out.writeObject(ACK);   //No more result
                            }catch(Exception e){
                                e.printStackTrace();
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Registration
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 11:
                            out.writeObject("#LogNam");
                            String loginName = (String)in.readObject();
                            
                            out.writeObject("#LogPas");
                            String loginPass = (String)in.readObject();
                            
                            int res = createAccount(loginName,loginPass);
                            if(res == 0)out.writeObject(ACK);
                            else
                                out.writeObject(ERR);
                            
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Not the group of command
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        default:
                            wrongCmd    = true;
                    }
                }
                
                /*
                 * Finally
                 * No command was match
                 */
                if(wrongCmd){
                    out.writeObject("WNG");
                }
                
                //Reset used stream memory
                out.reset();
            }
        }catch(SocketException ex){
            //Close Connection
        }catch (EOFException ex){
            //Close Connection exception
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try{
            connection.close();
            
            /*
             * Save Last SongID
             */
            PrintWriter tidWriter = new PrintWriter(new FileWriter(dataPath+"tid.sav", false));  
            tidWriter.print(this.TotalAudio);
            tidWriter.close();
            
            /*
             * Save Total Audio List to File
             */
            ObjectOutputStream totalAudioList = new ObjectOutputStream ( new FileOutputStream(dataPath+"tal.dat",false));
            totalAudioList.writeObject(audioList);
            totalAudioList.close();
        }catch(Exception e){/*DO NOTHING- Close socket*/}
    }
    
    private int checkPass(String id,String pass) throws FileNotFoundException, IOException{
        
        FileInputStream readB = new FileInputStream(new File(userData+id+"\\ps.dat"));
        byte[] buffer         = new byte[readB.available()];
        readB.read(buffer);
        
        String tempPass = "";
        for(int i = 0 ; i < buffer.length ; i++){
            tempPass += code[(buffer[i] << (8*0))&0xFF];
        }
        
        if(tempPass.equals(pass)){
            sysPrint("Login Complete ID > "+id);
            return 0;
        }else
            return -1;
    }
    
    private int createAccount(String acc,String pass)throws Exception{
        try{
            if(!new File(userNameFolder).exists() || !new File(userNameFolder).isDirectory())new File(userNameFolder).mkdir();
            
            if(new File(userNameFolder+acc+".dat").exists())throw new Exception("User was exists");

            FileOutputStream fout = new FileOutputStream(new File(userNameFolder+acc+".dat"));
            fout.write("x".getBytes());

            while(true){
                int randNum = (int)(Math.random()*6 + 1); //Number 1-6
                String tempPos  = "";

                char pos = 'A';
                switch(randNum){
                    case 0:
                    case 1:
                        pos='B';
                        break;
                    case 2:
                        pos='C';
                        break;
                    case 3:
                        pos='D';
                        break;
                    case 4:
                        pos='E';
                        break;
                    case 5:
                        pos='F';
                        break;
                    case 6:
                        pos='G';
                        break;
                }
                int tempID = new Integer((int)(Math.random()*100000 + 1));
                tempPos = pos + new Integer(tempID).toString();
                if(!new File(userData+tempPos+"\\").exists()){
                    sysPrint("Create new Account > "+tempPos);
                    new File(userData+tempPos+"\\").mkdir();
                    
                    switch(pos){
                        case 'A':fout.write(0x00);
                            break;
                        case 'B':fout.write(0x01);
                            break;
                        case 'C':fout.write(0x02);
                            break;
                        case 'D':fout.write(0x03);
                            break;
                        case 'E':fout.write(0x04);
                            break;
                        case 'F':fout.write(0x05);
                            break;
                        case 'G':fout.write(0x06);
                            break;
                        
                    }
                    
                    fout.write(intToBytes(tempID*11));
                    fout.close();
                    
                    fout = new FileOutputStream(new File(userData+tempPos+"\\ps.dat"));
                    char passData[] = pass.toCharArray();
                    for(int i = 0 ; i < passData.length ; i++){
                        for(int j = 0;j < code.length ; j++){
                            if(code[j] == passData[i]){
                                fout.write((byte)j);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            fout.close();

            return 0;
        }catch(Exception e){
            errPrint(e.getMessage());
            return -1;
        }
    }
    
    
    public static final byte[] intToBytes(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}

