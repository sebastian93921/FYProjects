/**
 * Media Server Side
 * 
 * Media server MUST define a MAX_CLIENT number for connection controlling 
 * to the client.
 * 
 * The server port normally is using 17221 port.
 * Testing purpose are using 17222 port.
 * 
 * Build Date 23/11/2012
 * 
 * Sebastian Ko
 */

/*
 * For Audio MSP Header
 * MUST ONLY Have 60byte(NOT including extended header)
 * which is 
 * MSP + 0x00 Header
 * SGID + songID - 8byte
 * SPRT + sample rate - 8byte
 * BPSP + bits per sample - 8byte
 * CHNL + channels - 8byte
 * ISSN + signed - 8byte (boolean 1/0)
 * ISBE + isBigEndian - 8byte (boolean 1/0)
 * 
 * Sebastian Ko 19/01/2013
 */

package Media;

import MSWPlayer.MSWPlayer;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Scanner;
import net.AudioDetails;


/*
 * Media Server 
 */
abstract class msd extends SysPrint{
    public final String version = "20/06/2013-1800 Media Update 4";
    public String system        = "Media";
    public static int TotalAudio  = 0;
    
    //Default Output Command
    public final String ERR        = "ERR";
    public final String ACK        = "ACK";
    public final String NULL       = "NULL";
    public final String noUserID   = "NOID";
    
    /*
     * Initialize
     */
    public final int DEFAULT_NET_BUFFERSIZE = 4096; //Internet sending size
    public final int MAX_CLIENT      = 50;       //Define MAX Client handling
    public static int port            = 0;        //Media Server port
    public static int clientOnLine      = 0;        //Client ON LINE number

    public static String CentralIP      = null;     //Central Server IP
    public static int CentralPort       = 0;        //Central Server port
    
    /*
     * Audio
     */
    public final String songPath   = ".\\sng\\"; //Users Song Path
    public final String tempPath   = ".\\tmp\\"; //Temp File Path
    
    public static boolean shutdown  = false;
}


public class MediaServer extends msd{

    private ServerSocket serSocket  = null;     //Media Server Socket
    
    
    public MediaServer(int Port){
        //Initialize
        port = Port;
    }
        
    public int startServer(){
        sysPrint("Builded date : "+version);
        
        sysPrint("Admin Command Start");
        adminMode();
        
        sysPrint("Checking Song Path");
        if(new File(this.songPath).exists());
        else
            new File(this.songPath).mkdir();
        
        File tempFolder = new File(this.tempPath);
        if(tempFolder.exists()){
            deleteDirectory(tempFolder);
            tempFolder.mkdir();
        }else
            tempFolder.mkdir();
        
        sysPrint("Media Server Start");
        
        try{
            serSocket = new ServerSocket(port);
            sysPrint("Starting in Port : "+port);
            
            while(true){
                /*
                 * Start new thread
                 */
                if(!shutdown){
                    Socket newConnection = serSocket.accept();
                    new MServerConnections(newConnection).start();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    
    public static void main(String argv[]){
        System.out.print(
                "=================================\n"+
                "    Media Server BETA\n"+
                "=================================\n"
                );
        
        if(argv.length == 0){
            new MediaServer(17221).startServer();
        }else if(argv[0].startsWith("-port:"))
            new MediaServer(Integer.parseInt(argv[0].replaceAll("-port:",""))).startServer();
    }
	
    
    /* #####################################################################################
     * Admin Mode
     * Command Mode for admin
     * Command
     * 1/Shutdown   - Shutdown Server
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
                    }else if(cmd.equals("Shutdown")){       //==================================================================================
                        sysPrint("Server shutting down in 10s...");
                        shutdown = true;
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
    
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
}




class MServerConnections extends msd{
    private Socket connection  = null;     //Media Server Socket
    
    public MServerConnections(Socket getSocket){
        connection = getSocket;
    }
    
    public void run(){
        boolean wrongCmd    = true;
        
        /*
         * User details
         */
        String userID = null;   //Save userID for further function
        
        try {
            ObjectOutputStream out  = new ObjectOutputStream(connection.getOutputStream());
            ObjectInputStream in    = new ObjectInputStream(connection.getInputStream());
            
            sysPrint("Client Connected IP > "+connection.getInetAddress());
            
            while(true){
                String Command = (String)in.readObject();
                /*
                 * Command MODE
                 * Central Server Command handling
                 */
                if(Command.startsWith("C")){
                    wrongCmd    = false;
                    switch(Integer.parseInt(Command.replaceAll("C",""))){
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Check availability
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 1:
                            if(this.clientOnLine < this.MAX_CLIENT){
                                out.writeObject("K01");
                            }else
                                out.writeObject(ERR);
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Provide Central Server Port
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 2: 
                            out.writeObject("#Port");
                            try{
                                this.CentralPort    = Integer.parseInt((String)in.readObject());
                                out.writeObject("K02");
                                this.CentralIP      = connection.getInetAddress().toString().replace("/", "");
                            }catch(Exception e){
                                out.writeObject(ERR);
                            }
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Check if file exist
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 3:
                            out.writeObject("#CKID");
                            try{
                                int songID      = (Integer)in.readObject();
                                String songPath = null;

                                boolean haveSong    = false;

                                sysPrint("Request From other Media Server : Getting Audio Path..");
                                //Ask Server for Song Path
                                try{
                                    Socket requestSocket    = new Socket(this.CentralIP,this.CentralPort);
                                    ObjectOutputStream Cout = new ObjectOutputStream(requestSocket.getOutputStream());
                                    ObjectInputStream Cin   = new ObjectInputStream(requestSocket.getInputStream());

                                    Cout.writeObject("M03");
                                    if(Cin.readObject().equals("#GetID")){
                                        Cout.writeObject(songID);

                                        String res = (String)Cin.readObject();
                                        if(res.equals(NULL)){
                                            out.writeObject("#WNGID");
                                            break;
                                        }else if(res.startsWith("MSP:")){
                                            haveSong    = true;
                                            songPath    = res.replace("MSP:","");
                                        }else{
                                            out.writeObject(ERR);
                                            break;
                                        }
                                    }else{
                                        requestSocket.close();
                                        out.writeObject(ERR);
                                        break;
                                    }
                                    requestSocket.close();
                                }catch(Exception e){
                                    out.writeObject(ERR);
                                    e.printStackTrace();
                                    break;
                                }

                                /*
                                 * Check is File exist
                                 */
                                sysPrint("Check Requesting Files");
                                if(haveSong){
                                    File listMyAudio[] = new File(this.songPath).listFiles();

                                    /*
                                     * Getting Audio
                                     */
                                    boolean checkFile   = false;
                                    for(int i = 0 ; i < listMyAudio.length; i++){
                                        if(listMyAudio[i].getName().equals(songPath)){
                                            out.writeObject(ACK);
                                            in.readObject();    //NULL INPUT
                                            
                                            checkFile = true;
                                            FileInputStream getSong = new FileInputStream(new File(listMyAudio[i].getPath()));
                                            /*
                                             * Check file
                                             * 60 Byte Header
                                             */
                                            byte tempHeader[] = new byte[60];
                                            getSong.read(tempHeader, 0, 60);
                                            getSong.close();

                                            if(new String(tempHeader,0,3).equals("MSP")){
                                                int tempID = (tempHeader[8] << (8*3))&0xFF000000 | 
                                                            (tempHeader[9] << (8*2))&0x00FF0000 | 
                                                            (tempHeader[10] << (8*1))&0x0000FF00 |
                                                            (tempHeader[11] << (8*0))&0x000000FF;

                                                if(tempID == songID){
                                                    /*
                                                     * Downloading
                                                     * Change to byte array sending
                                                     * 1024byte per packet
                                                     */
                                                    sysPrint("Audio downloading from IP > "+connection.getInetAddress());
                                                    FileInputStream getAudioFile = new FileInputStream(new File(listMyAudio[i].getPath()));

                                                    byte[] tempBuffer   = new byte[DEFAULT_NET_BUFFERSIZE];
                                                    do{
                                                        if(!(getAudioFile.read(tempBuffer) == -1)){
                                                            out.write(tempBuffer);
                                                        }else{
                                                            sysPrint("Download Complete");
                                                            fillBytes(tempBuffer,"ACK");
                                                            
                                                            out.write(tempBuffer);
                                                            break;
                                                        }

                                                    }while(true);

                                                    getAudioFile.close();
                                                }else{
                                                    out.writeObject(ERR);   //Wrong ID
                                                    break;
                                                } 
                                            }else{
                                                out.writeObject(ERR);       //No Header
                                                break;
                                            }
                                        }
                                    }
                                    /*
                                     * Process if NO This Audio
                                     */
                                    if(!checkFile){
                                        errPrint("No requesting file found");
                                        out.writeObject(NULL);
                                        break;
                                    }
                                }
                            }catch(Exception e){
                                out.writeObject(ERR);
                                e.printStackTrace();
                            }
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
                 * Command MODE
                 * Client command handling
                 */
                if(Command.startsWith("X")){
                    wrongCmd    = false;
                    switch(Integer.parseInt(Command.replaceAll("X",""))){
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Receive User ID
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 50:
                            out.writeObject("#ID");
                            userID  = (String)in.readObject();
                            out.writeObject(ACK);
                            clientOnLine++;             //Add clientNum
                            break;
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Song Upload
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 51:
                            if(userID != null){
                                try{                                                                //---------Media Header MSP Header
                                    out.writeObject("#Type");
                                    String format   = (String)in.readObject();                      //---------Media Type 1 #Type
                                    if(format.equals("ERR"))throw new Exception();

                                    out.writeObject("#Size");                                       //---------Media Type 2 #Size
                                    long audioSize  = (Long)in.readObject();

                                    out.writeObject("#Buffer");                                     
                                    int bufferSize  = (Integer)in.readObject();

                                    out.writeObject("#Details");                                    //---------Media Type 3 #Details
                                    AudioDetails tempDetails    = (AudioDetails)in.readObject();

                                                                                                    //---------Media Footer MSF#
                                    out.writeObject("#Bytes");
                                    try{
                                        Socket requestSocket    = new Socket(this.CentralIP,this.CentralPort);
                                        ObjectOutputStream Cout = new ObjectOutputStream(requestSocket.getOutputStream());
                                        ObjectInputStream Cin   = new ObjectInputStream(requestSocket.getInputStream());

                                        Cout.writeObject("M01");
                                        TotalAudio = (Integer)Cin.readObject();

                                        requestSocket.close();
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                    int SongID  = TotalAudio;

                                    /*
                                     * Add Header to File
                                     */
                                    FileOutputStream tempAudio = new FileOutputStream(new File(this.tempPath+SongID+".msw"),false);
                                    /*
                                     * Total 4x14 = 56byte Header
                                     */
                                    tempAudio.write("MSP".getBytes());
                                    tempAudio.write(0x00);                         //Version of MSP
                                    tempAudio.write("SGID".getBytes());
                                    tempAudio.write(intToBytes(SongID));              //value
                                    tempAudio.write("SPRT".getBytes());
                                    tempAudio.write(floatToBytes(tempDetails.getSampleRate()));
                                    tempAudio.write("BPSP".getBytes());
                                    tempAudio.write(intToBytes(tempDetails.getBitPerSample()));
                                    tempAudio.write("CHNL".getBytes());
                                    tempAudio.write(intToBytes(tempDetails.getChannels()));
                                    tempAudio.write("ISSN".getBytes());
                                    tempAudio.write(intToBytes(tempDetails.getSigned()));
                                    tempAudio.write("ISBE".getBytes());
                                    tempAudio.write(intToBytes(tempDetails.getBigEndian()));
                                    

                                    if(tempDetails.containsTag()){
                                        //
                                        //Extended Header
                                        //
                                        tempAudio.write("XHDR".getBytes());
                                        //
                                        //Size of Header
                                        //Value Header 4bytes - XTIT XART XALB XTRK XCVR XYER
                                        //Value Size Declare 4bytes - int to byte[]
                                        //Value Data
                                        //...
                                        //Add 8bytes 0x00
                                        //
                                        byte[] tagTitle = tempDetails.getTagTitle().getBytes("UTF-8");
                                        byte[] tagArtist =tempDetails.getTagArtist().getBytes("UTF-8");
                                        byte[] tagAlbum = tempDetails.getTagAlbum().getBytes("UTF-8");
                                        byte[] tagTrack = tempDetails.getTagTrack().getBytes("UTF-8");
                                        byte[] tagYear  = tempDetails.getTagYear().getBytes("UTF-8");
                                        byte[] tagCover = tempDetails.getTagCoverInBytes();
                                        
                                        int xheaderSize = 0;
                                        if(tagTitle.length != 0)xheaderSize += tagTitle.length + 8;
                                        if(tagArtist.length != 0)xheaderSize += tagArtist.length + 8;
                                        if(tagAlbum.length != 0)xheaderSize += tagAlbum.length + 8;
                                        if(tagTrack.length != 0)xheaderSize += tagTrack.length + 8;
                                        if(tagYear.length != 0)xheaderSize += tagYear.length + 8;
                                        if(tagCover != null)xheaderSize += tagCover.length + 8;
                                        
                                        xheaderSize += 8;   //8 bytes 0x00
                                        
                                        //Add Size of Header
                                        tempAudio.write(intToBytes(xheaderSize));
                                        if(tagTitle.length != 0)this.addXHeader(tempAudio, "XTIT",tagTitle);
                                        if(tagArtist.length != 0)this.addXHeader(tempAudio, "XART",tagArtist);
                                        if(tagAlbum.length != 0)this.addXHeader(tempAudio, "XALB",tagAlbum);
                                        if(tagTrack.length != 0)this.addXHeader(tempAudio, "XTRK",tagTrack);
                                        if(tagYear.length != 0)this.addXHeader(tempAudio, "XYER",tagYear);
                                        if(tagCover != null)this.addXHeader(tempAudio, "XCVR",tagCover);
                                        
                                        tempAudio.write("ENDOFHDR".getBytes()); //8 bytes 0x00
                                        
                                        
                                        //
                                        //End of Extended Header
                                        //
                                    }else{
                                        //
                                        //Footer
                                        //
                                        tempAudio.write(0xFF);tempAudio.write(0xFA);
                                        tempAudio.write(0xFA);tempAudio.write(0xFF);
                                    }
                                    
                                    long readSize      = 0;
                                    
                                    while(true){
                                        byte[] buffer   = new byte[bufferSize]; //Buffer size
                                        in.readFully(buffer);

                                        if(new String(buffer,0,3).equals("EOF")){
                                            out.writeObject("#CkRepeat");
                                            break;
                                        }else{
                                            readSize += bufferSize;
                                            //out.writeObject(readSize+"/"+audioSize);
                                            tempAudio.write(buffer);

                                        }
                                    }

                                    tempAudio.close();

                                    tempDetails.setSongID(SongID);
                                    tempDetails.setPath("MSP:"+SongID+".msw");
                                    tempDetails.setDurationInSec(new MSWPlayer(this.tempPath+SongID+".msw").getDurationInSec());

                                    /*
                                     * Check Do Audio Repeat
                                     */
                                    boolean audioRepeated = false;
                                    int repeatedID        = -1;
                                    in.readObject();

                                    try{
                                        Socket requestSocket    = new Socket(this.CentralIP,this.CentralPort);
                                        ObjectOutputStream Cout = new ObjectOutputStream(requestSocket.getOutputStream());
                                        ObjectInputStream Cin   = new ObjectInputStream(requestSocket.getInputStream());


                                        Cout.writeObject("M02");
                                        if(Cin.readObject().equals("#AD")){
                                            Cout.writeObject(tempDetails);

                                            String res2 = (String)Cin.readObject();
                                            if(res2.equals(ACK)){
                                                /*
                                                 * Copying Audio to Sng Folder
                                                 */
                                                sysPrint("Client Send to File > " + tempDetails.getTagTitle());
                                                File newAudio = new File(this.tempPath+SongID+".msw");
                                                if(new File(this.songPath+SongID+".msw").exists())new File(this.songPath+SongID+".msw").delete();
                                                
                                                newAudio.renameTo(new File(this.songPath+SongID+".msw"));

                                                audioRepeated = false;
                                            }else if(res2.startsWith("#Repeated")){     //Command #Repeated - ID:99
                                                /*
                                                 * Removed the repeated file
                                                 */
                                                repeatedID = Integer.parseInt(res2.replaceAll("#Repeated - ID:",""));
                                                errPrint("Repeated File > " + tempDetails.getTagTitle());
                                                errPrint("Reset to SongID > " + repeatedID);

                                                audioRepeated = true;
                                            }
                                            else
                                                throw new Exception();
                                        }
                                        
                                        Cout.writeObject("M05");
                                        if(Cin.readObject().equals("#UID/SGID")){
                                            if(!audioRepeated)Cout.writeObject(userID+"/"+SongID);
                                            else Cout.writeObject(userID+"/"+repeatedID);
                                            
                                            if(Cin.readObject().equals(ACK))sysPrint("Success to pass song ID to Central Server");
                                            else errPrint("Fail to pass song ID to Central Server");
                                        }else
                                            Cout.writeObject(ERR);
                                        
                                        
                                        try{
                                        if(!audioRepeated){
                                            sysPrint("Upload Complete");
                                            out.writeObject("#Success >"+SongID);
                                        }else out.writeObject("#Repeated >"+repeatedID);
                                        }catch(Exception e){
                                            e.printStackTrace();
                                            out.writeObject(ERR);
                                        }
                                        
                                        
                                        requestSocket.close();
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }catch(Exception e){
                                    out.writeObject(ERR);
                                    e.printStackTrace();
                                }
                            }else
                                out.writeObject(noUserID);
                            break;
                            
                            
                            
                        /* vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         * Song Download to Client
                         * vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                         */
                        case 52:
                            if(userID != null){
                                try{
                                    out.writeObject("#SongID");
                                    int songID      = (Integer)in.readObject();
                                    String audioPath= null;
                                    
                                    boolean haveSong    = false;
                                    
                                    sysPrint("Getting Audio Path..");
                                    //Ask Server for Song Path
                                    try{
                                        Socket requestSocket    = new Socket(this.CentralIP,this.CentralPort);
                                        ObjectOutputStream Cout = new ObjectOutputStream(requestSocket.getOutputStream());
                                        ObjectInputStream Cin   = new ObjectInputStream(requestSocket.getInputStream());
                                        
                                        Cout.writeObject("M03");
                                        if(Cin.readObject().equals("#GetID")){
                                            Cout.writeObject(songID);
                                            
                                            String res = (String)Cin.readObject();
                                            if(res.equals(NULL)){
                                                out.writeObject("#WNGID");
                                                break;
                                            }else if(res.startsWith("MSP:")){
                                                haveSong    = true;
                                                audioPath    = res.replace("MSP:","");
                                            }else{
                                                out.writeObject(ERR);
                                                break;
                                            }
                                        }else{
                                            requestSocket.close();
                                            out.writeObject(ERR);
                                            break;
                                        }
                                        requestSocket.close();
                                    }catch(Exception e){
                                        out.writeObject(ERR);
                                        e.printStackTrace();
                                        break;
                                    }
                                    
                                    /*
                                     * Check is File exist
                                     */
                                    sysPrint("Checking Files");
                                    if(haveSong){
                                        File listMyAudio[] = new File(this.songPath).listFiles();
                                        
                                        /*
                                         * Getting Audio
                                         */
                                        boolean checkFile   = false;
                                        for(int i = 0 ; i < listMyAudio.length; i++){
                                            if(listMyAudio[i].getName().equals(audioPath)){
                                                checkFile = true;
                                                FileInputStream getSong = new FileInputStream(new File(listMyAudio[i].getPath()));
                                                /*
                                                 * Check file
                                                 * 60 Byte Header
                                                 */
                                                byte tempHeader[] = new byte[60];
                                                getSong.read(tempHeader, 0, 60);
                                                getSong.close();
                                                
                                                if(new String(tempHeader,0,3).equals("MSP")){
                                                    int tempID = (tempHeader[8] << (8*3))&0xFF000000 | 
                                                                (tempHeader[9] << (8*2))&0x00FF0000 | 
                                                                (tempHeader[10] << (8*1))&0x0000FF00 |
                                                                (tempHeader[11] << (8*0))&0x000000FF;
                                                    
                                                    if(tempID == songID){
                                                        out.writeObject(new File(listMyAudio[i].getPath()).length());
                                                        in.readObject();    //NULL Input
                                                        /*
                                                         * Downloading
                                                         * Change to byte array sending
                                                         * 1024byte per packet
                                                         */
                                                        sysPrint("Audio downloading from IP > "+connection.getInetAddress());
                                                        InputStream getAudioFile = new FileInputStream(new File(listMyAudio[i].getPath()));
                                                        
                                                        
                                                        byte[] tempBuffer   = new byte[DEFAULT_NET_BUFFERSIZE];
                                                        do{
                                                            if(!(getAudioFile.read(tempBuffer) == -1)){
                                                                out.write(tempBuffer);
                                                            }else{
                                                                sysPrint("Download Complete");
                                                                fillBytes(tempBuffer,"ACK");
                                                                out.write(tempBuffer);
                                                                break;
                                                            }
                                                            
                                                        }while(true);
                                                        
                                                        getAudioFile.close();
                                                    }else{
                                                        out.writeObject(ERR);   //Wrong ID
                                                        break;
                                                    } 
                                                }else{
                                                    out.writeObject(ERR);       //No Header
                                                    break;
                                                }
                                            }
                                        }
                                        /*
                                         * Process if NO This Audio
                                         */
                                        if(!checkFile){
                                            errPrint("Server haven't this file ID : "+songID);
                                            boolean noServer    = false;
                                            String mediaIP      = null;
                                            int mediaPort       = 0;
                                            boolean check       = false;
                                            boolean downComplete= false;
                                            
                                            Socket CrequestSocket    = null;
                                            ObjectOutputStream Cout = null;
                                            ObjectInputStream Cin   = null;      
                                            
                                            try{
                                                CrequestSocket      = new Socket(this.CentralIP,this.CentralPort);
                                                Cout                = new ObjectOutputStream(CrequestSocket.getOutputStream());
                                                Cin                 = new ObjectInputStream(CrequestSocket.getInputStream());

                                                Cout.writeObject("M04");
                                                if(Cin.readObject().equals("#Port")){
                                                    Cout.writeObject(port);
                                                }else{
                                                    throw new Exception("Wrong receive response");
                                                }
                                            }catch(Exception e){
                                                errPrint("Fail to receive from central server");
                                                CrequestSocket.close();
                                                
                                                out.writeObject(ERR);
                                                e.printStackTrace();
                                                break;
                                            }
                                            
                                            while(true){
                                                try{
        
                                                    mediaIP     = ((String)Cin.readObject()).replaceAll("#IP ","");

                                                    if(mediaIP.equals(ERR)){
                                                        noServer = true;
                                                        errPrint("Fail to receive media server details");
                                                        break;
                                                    }else if(mediaIP.equals(NULL)||mediaIP.equals(ACK)){
                                                        noServer = true;
                                                        errPrint("No file found from other Media Server(s)");
                                                        break;
                                                    }
                                                    //Get Server Port
                                                    Cout.writeObject(ACK);
                                                    mediaPort   = Integer.parseInt(((String)Cin.readObject()).replaceAll("#Port ", ""));

                                                    sysPrint("Received media server : "+mediaIP+"#"+mediaPort);

                                                    check = true;
                                                }catch(Exception e){
                                                    errPrint("Fail to receive from central server");
                                                    out.writeObject(ERR);
                                                    e.printStackTrace();
                                                    break;
                                                }
                                                
                                                /*
                                                 * Check data
                                                 */
                                                try{
                                                    if(check && !noServer){
                                                        Socket MrequestSocket      = new Socket(mediaIP,mediaPort);
                                                        ObjectOutputStream Mout    = new ObjectOutputStream(MrequestSocket.getOutputStream());
                                                        ObjectInputStream Min      = new ObjectInputStream(MrequestSocket.getInputStream());

                                                        Mout.writeObject("C03");
                                                        if(Min.readObject().equals("#CKID"))Mout.writeObject(songID);
                                                        else
                                                            throw new Exception ("Fail to Connecting Server");
                                                        
                                                        if(!Min.readObject().equals(ACK)){
                                                            errPrint("No file requesting from Media Server > "+mediaIP);
                                                            Cout.writeObject(NULL);
                                                            MrequestSocket.close();
                                                        }else{
                                                            if(new File(tempPath).exists()){
                                                                deleteDirectory(new File(tempPath));
                                                            }
                                                            Mout.writeObject(ACK);
                                                            
                                                            new File(tempPath).mkdir();
                                                            FileOutputStream tempAudio = new FileOutputStream(new File(tempPath+songID),false);

                                                            long readSize      = 0;

                                                            sysPrint("Start Downloading");
                                                            while(true){
                                                                byte[] audioBuffer   = new byte[DEFAULT_NET_BUFFERSIZE]; //Buffer size

                                                                Min.readFully(audioBuffer);

                                                                if(new String(audioBuffer,0,3).equals("ACK")){
                                                                    sysPrint("Download Complete");
                                                                    downComplete = true;
                                                                    tempAudio.close();
                                                                    break;
                                                                }else{
                                                                    //Mout.writeObject(readSize);
                                                                    tempAudio.write(audioBuffer);
                                                                    readSize += audioBuffer.length;

                                                                }

                                                            }

                                                            /*
                                                             * Rename
                                                             */
                                                            if(new File(songPath).exists());
                                                            else
                                                                new File(songPath).mkdir();

                                                            File newAudio = new File(this.tempPath+songID);
                                                            newAudio.renameTo(new File(this.songPath+songID+".msw"));
                                                            
                                                            tempAudio.close();
                                                            MrequestSocket.close();
                                                            Cout.writeObject(ACK);
                                                            break;
                                                        }

                                                    }
                                                }catch(Exception e){
                                                    e.printStackTrace();
                                                    Cout.writeObject(NULL);
                                                }
                                                
                                            }
                                            CrequestSocket.close();
                                            
                                            if(downComplete){
                                                sysPrint("Rechecking Files");
                                                listMyAudio = new File(this.songPath).listFiles();

                                                /*
                                                 * Getting Audio
                                                 */
                                                checkFile   = false;
                                                for(int i = 0 ; i < listMyAudio.length; i++){
                                                    
                                                    if(listMyAudio[i].getName().equals(audioPath)){
                                                        checkFile = true;
                                                        FileInputStream getSong = new FileInputStream(new File(listMyAudio[i].getPath()));
                                                        /*
                                                         * Check file
                                                         * 60 Byte Header
                                                         */
                                                        byte tempHeader[] = new byte[60];
                                                        getSong.read(tempHeader, 0, 60);
                                                        getSong.close();

                                                        if(new String(tempHeader,0,3).equals("MSP")){
                                                            int tempID = (tempHeader[8] << (8*3))&0xFF000000 | 
                                                                        (tempHeader[9] << (8*2))&0x00FF0000 | 
                                                                        (tempHeader[10] << (8*1))&0x0000FF00 |
                                                                        (tempHeader[11] << (8*0))&0x000000FF;

                                                            if(tempID == songID){
                                                                out.writeObject(new File(listMyAudio[i].getPath()).length());
                                                                in.readObject();    //NULL Input
                                                                /*
                                                                 * Downloading
                                                                 * Change to byte array sending
                                                                 * 1024byte per packet
                                                                 */
                                                                sysPrint("Audio downloading from IP > "+connection.getInetAddress());
                                                                InputStream getAudioFile = new FileInputStream(new File(listMyAudio[i].getPath()));


                                                                byte[] tempBuffer   = new byte[1024];
                                                                do{
                                                                    if(!(getAudioFile.read(tempBuffer) == -1)){
                                                                        out.write(tempBuffer);
                                                                    }else{
                                                                        sysPrint("Download Complete");
                                                                        fillBytes(tempBuffer,"ACK");
                                                                        out.write(tempBuffer);
                                                                        break;
                                                                    }

                                                                }while(!(in.readObject().equals("ERR")));

                                                                getAudioFile.close();
                                                            }else{
                                                                out.writeObject(ERR);   //Wrong ID
                                                                break;
                                                            } 
                                                        }else{
                                                            out.writeObject(ERR);       //No Header
                                                            break;
                                                        }
                                                    }
                                                }
                                            }else
                                                out.writeObject(ERR);
                                        }
                                    }
                                }catch(Exception e){
                                    out.writeObject(ERR);
                                    e.printStackTrace();
                                }
                            }else
                                out.writeObject(noUserID);
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
        }catch(Exception e){/*DO NOTHING- Close socket*/}
        clientOnLine--;
    }
    
    
    public final byte[] longToBytes(long v) {
        byte[] writeBuffer = new byte[ 8 ];

        writeBuffer[0] = (byte)(v >>> 56);
        writeBuffer[1] = (byte)(v >>> 48);
        writeBuffer[2] = (byte)(v >>> 40);
        writeBuffer[3] = (byte)(v >>> 32);
        writeBuffer[4] = (byte)(v >>> 24);
        writeBuffer[5] = (byte)(v >>> 16);
        writeBuffer[6] = (byte)(v >>>  8);
        writeBuffer[7] = (byte)(v >>>  0);

        return writeBuffer;
    }
    
    public static final byte[] intToBytes(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
    
    public static byte[] floatToBytes(float v) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte[] ret = new byte [4];
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(v);
        bb.get(ret);
        return ret;
    }
    
    public static float bytesToFloat(byte[] v){
        ByteBuffer bb = ByteBuffer.wrap(v);
        FloatBuffer fb = bb.asFloatBuffer();
        return fb.get();
    }
    
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
    
    public byte[] fillBytes(byte[] vb,String v){
        char[] tempChar = v.toCharArray();
        if(tempChar.length > vb.length)return vb;
        for(int i = 0;i< vb.length;i++){
            if(i >= tempChar.length)vb[i] = 0x00;
            else vb[i]   = (byte)tempChar[i];
        }
        return vb;
    }
    
    public void addXHeader(FileOutputStream file,String identifier,byte[] value) throws Exception{
        file.write(identifier.getBytes());
        file.write(intToBytes(value.length));
        file.write(value);
    }
}

