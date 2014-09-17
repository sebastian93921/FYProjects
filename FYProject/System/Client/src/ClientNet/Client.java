/*
 * Network Connection Tool for Final year project player
 * Start connection from Central Server
 * Connection tool including connect to media server
 * 
 * 20/02/2013 Sebastian Ko
 */
package ClientNet;

import MSWPlayer.MSWPlayer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.AudioDetails;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;
import net.AudioElement;
import tag.codec.AudioTAG;

/**
 *
 * @author Sebastian
 */
public class Client {
    /*
     * Client Version Format
     * 0.0.0-0
     * ^ ^ ^ ^
     * | | | Build Test version
     * | | Minor version
     * | Major version
     * Major System version
     */
    public static final String version                      = "0.0.3-0";
    private static  String CentralServerIP                    = null;
    private static  int CentralServerPort                      = 0;
    private static final String songPath                    = ".\\net\\";
    private static final String tempPath                    = ".\\tmp\\";
    private Socket CentralConnect                          = null;
    private ObjectInputStream CentralIn                    = null;
    private ObjectOutputStream CentralOut                  = null;
    private Socket MediaConnect                            = null;
    private ObjectInputStream MediaIn                      = null;
    private ObjectOutputStream MediaOut                    = null;
    
    private static String userID                             = null;
    
    //Result
    public static final int NETWORK_ERROR                = -2;
    public static int downloadProcess                         = -1;
    public static int uploadProcess                           = -1;
    public static long lastCount                             = 0;

    
    /*
     * Status
     */
    private static boolean CentralConnected                     = false;
    private static boolean MediaConnected                     = false;
    
    
    public Client(String ip,int port){
        CentralServerIP     = ip;
        CentralServerPort    = port;
    }
    
    public int checkUpdate(String readVersion){
        try {
            CentralOut.writeObject("X02");
            String getVersion = (String)CentralIn.readObject();
            System.out.println("OldVersion : "+readVersion +"/"+getVersion);

            if(readVersion.equals(getVersion)){
                return 0;
            }else
                return -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return NETWORK_ERROR;   //Connection fail
        }
    }
    
    public int Registration(String LogName,String LogPass){
        try{
            CentralOut.writeObject("X11");
            if(CentralIn.readObject().equals("#LogNam"))
                CentralOut.writeObject(LogName);
            else
                throw new ConnectionFailException();
            if(CentralIn.readObject().equals("#LogPas"))
                CentralOut.writeObject(LogPass);
            else
                throw new ConnectionFailException();
            if(CentralIn.readObject().equals("ACK"))
                return 0;
            else
                return -1;
        }catch(Exception e){
            e.printStackTrace();
            return NETWORK_ERROR;
        }
    }
    
    public int Login(String LogName,String LogPass){
        try{
            CentralOut.writeObject("X01");
            if(((String)CentralIn.readObject()).equals("#Nam")){
                CentralOut.writeObject(LogName);    //Test Name
            }
            if(((String)CentralIn.readObject()).equals("#Pas")){
                CentralOut.writeObject(LogPass);    //Test Password
            }

            String conRes = (String)CentralIn.readObject();
            if(conRes.equals("ERR")){
                return -1;
            }else{
                userID  = conRes;
                return 0;
            }
        }catch(Exception e){
            e.printStackTrace();
            return NETWORK_ERROR;
        }
    }
    
    public String receiveUserID(){
        return userID;
    }
    
    public int UploadAudioList(LinkedList<AudioElement> UploadList){
        try{
            CentralOut.writeObject("X05");
            if(CentralIn.readObject().equals("#MSL")){
                CentralOut.writeObject(UploadList);

                if(CentralIn.readObject().equals("ACK"))return 0;
                else return -1;
            }else
                return NETWORK_ERROR;
        }catch(Exception e){
            e.printStackTrace();
            return NETWORK_ERROR;
        }
    }
    
    public Object GetAudioDetails(int songID){
        try{
            CentralOut.writeObject("X04");
            if(CentralIn.readObject().equals("#ADID")){
                CentralOut.writeObject(songID);
            }
            Object adAns = CentralIn.readObject();
            
            return adAns;
        }catch(Exception e){
            e.printStackTrace();
            return "NET_ERR";
        }
    }
    
    public void DisconnectCentral(){
        try{
            CentralConnected = false;
            CentralConnect.close();
            CentralIn  = null;
            CentralOut = null;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public int ConnectCentral(){
        return ConnectCentral(CentralServerIP,CentralServerPort);
    }
    
    public int ConnectCentral(String ip,int port){
        try{
            CentralConnect  =  new Socket(ip,port);
            CentralIn       = new ObjectInputStream(CentralConnect.getInputStream());
            CentralOut      = new ObjectOutputStream(CentralConnect.getOutputStream());

            System.out.println("Connection connected : "+CentralConnect.getInetAddress());
            CentralConnected = true;
            return 0;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
        
    }
    
    public LinkedList<AudioElement> DownloadAudioList() throws Exception{
        CentralOut.writeObject("X06");
        return (LinkedList<AudioElement>)CentralIn.readObject();
    }
    
    public String ReceiveMediaServer(){
        try{
            CentralOut.writeObject("X03");
            String serverIP     = ((String)CentralIn.readObject()).replaceAll("#IP ","");
            
            if(serverIP.equals("ERR")){
                System.out.println("Fail to receive media server details");
                return "ERR";
            }
            //Get Server Port
            CentralOut.writeObject("ACK");
            String serverPort   = ((String)CentralIn.readObject()).replaceAll("#Port ", "");
            
            return serverIP + ":" + serverPort;
        }catch(Exception e){
            e.printStackTrace();
            return "ERR";
        }
    }
    
    public int ConnectMedia(String IPwPort,String ID) throws Exception{
        String[] tempString = IPwPort.split(":");
        return ConnectMedia(tempString[0],Integer.parseInt(tempString[1]),ID);
    }
    
    public int ConnectMedia(String ip,int port,String ID){
        try{
            MediaConnect = new Socket(ip,port);
            MediaIn      = new ObjectInputStream(MediaConnect.getInputStream());
            MediaOut     = new ObjectOutputStream(MediaConnect.getOutputStream());

            MediaOut.writeObject("X50");
            if(MediaIn.readObject().equals("#ID")){
                MediaOut.writeObject(ID);
            }
            if(!MediaIn.readObject().equals("ACK"))
                return -1;

            MediaConnected = true;
            return 0;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
        
    }
    
    public void DisconnectMedia(){
        try{
            MediaConnected = false;
            MediaConnect.close();
            MediaIn  = null;
            MediaOut = null;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public int uploadToMedia(String song,boolean permission){
        try{
            System.out.println("\n>Part 7 : Upload Song to media Server");
            MediaOut.writeObject("X51");
            final MSWPlayer audio = new MSWPlayer(song);
            if(MediaIn.readObject().equals("#Type")){
                MediaOut.writeObject(audio.getAudioType());
            }
            if(MediaIn.readObject().equals("#Size")){
                MediaOut.writeObject(audio.getAudioSize());
                
            }
            if(MediaIn.readObject().equals("#Buffer")){
                MediaOut.writeObject(audio.NET_BUFFER_SIZE);
            }
            //Adding Details from Song

            AudioDetails songDetails    = new AudioDetails(audio.getAudioFormat());
            AudioTAG tag                = new AudioTAG(song);
            songDetails.setTag(tag);
            
            if(permission)songDetails.setPerm(AudioDetails.NET_PUBLIC);
            else songDetails.setPerm(AudioDetails.NET_PRIVATE);
            
            if(MediaIn.readObject().equals("#Details")){
                MediaOut.writeObject(songDetails);
            }
            if(MediaIn.readObject().equals("#Bytes")){
                byte[] buffer   = new byte[audio.NET_BUFFER_SIZE];
                
                System.out.println("TotalSize : "+audio.getAudioSize());
                
                buffer = audio.getByte();
                if(buffer != null)MediaOut.write(buffer);
                
                lastCount    = 0;
                uploadProcess = 0;
                try{
                    new Thread(new Runnable(){public void run(){
                        do{
                            try{
                                long lastByte = lastCount;
                                Thread.sleep(1000);
                                System.out.println("S:"+(lastCount-lastByte)/1024+"kb/s P:"+uploadProcess);

                                if(uploadProcess < 0)break;
                            }catch(Exception ee){}
                        }while(true);
                    }}).start();
                    
                    while(true){
                        //String ans = (String)MediaIn.readObject();
                        //if(ans.equals("ERR"))break;

                        //long uploadedSize = Integer.parseInt(ans.replaceAll("/"+audio.getAudioSize(),""));
                        //int uploadCount = (int)(uploadedSize*10/audio.getAudioSize());
                        //uploadProcess      = (int)(uploadedSize*100/audio.getAudioSize());
                        //if(uploadCount != lastCount)System.out.print("|");
                        //System.out.println(lastCount+"/"+audio.getAudioSize()+"-"+uploadProcess);
                        uploadProcess      = (int)(lastCount*100/audio.getAudioSize());
                        lastCount   += audio.NET_BUFFER_SIZE;//uploadCount;
                        
                        buffer   = audio.getByte();

                        if(buffer != null){
                            MediaOut.write(buffer);
                        }
                        else {
                            buffer = new byte[audio.NET_BUFFER_SIZE];
                            fillBytes(buffer,"EOF");
                            MediaOut.write(buffer);
                            break;
                        }
                        
                    }
                }catch(Exception e){
                    uploadProcess = -1;
                    e.printStackTrace();
                    return NETWORK_ERROR;
                }
                uploadProcess = -1;
            }
            
            if(!MediaIn.readObject().equals("#CkRepeat"))throw new ConnectionFailException();
            MediaOut.writeObject("OK");//DO NOTHING
            
            String uploadRes = (String)MediaIn.readObject();
            if(uploadRes.startsWith("#Success >")){     //Success
                return Integer.parseInt(uploadRes.replaceAll("#Success >", ""));
            }
            else if(uploadRes.startsWith("#Repeated >")){
                return Integer.parseInt(uploadRes.replaceAll("#Repeated >", ""));
            }else if(uploadRes.equals("ERR"))return -1;//ERR - Having mistake
        }catch(Exception e){
            e.printStackTrace();
            return NETWORK_ERROR;
        }
        return -1;
    }
    
    
    public int downloadFromMedia(int downloadID) throws ConnectionFailException{
        if(new File(songPath+downloadID+".msw").exists())return 0;
        
        try{
            MediaOut.writeObject("X52");
            if(MediaIn.readObject().equals("#SongID")){
                MediaOut.writeObject(downloadID);
                
                Object result = MediaIn.readObject();
                long downFileSize   = 0;
                if(!result.equals("#WNGID")){
                    downFileSize   = (Long)result;
                }
                System.out.println("File Size : "+downFileSize);
                
                int lastDCount       = 0;
                int readCount       = 0;
                if(downFileSize != 0){
                    MediaOut.writeObject("");    //NULL Output
                    
                    new File(tempPath).mkdir();
                    FileOutputStream tempAudio = new FileOutputStream(new File(tempPath+downloadID));
                    try{
                        long readSize      = 0;

                        while(true){
                            byte[] audioBuffer   = new byte[4096]; //Buffer size
                            MediaIn.readFully(audioBuffer);
                            
                            if(new String(audioBuffer,0,3).equals("ACK")){
                                System.out.println("DONE");
                                tempAudio.close();
                                break;
                            }else{
                                readCount       = (int)(readSize*10/downFileSize);
                                downloadProcess    = (int)(readSize*100/downFileSize);
                                if(readCount != lastDCount)System.out.print("|");
                                lastDCount = readCount;
                                
                                //MediaOut.writeObject(readSize+"/"+downFileSize);
                                tempAudio.write(audioBuffer);
                                readSize += audioBuffer.length;

                            }
                        }
                    }catch(Exception e){
                        downloadProcess = -1;
                        e.printStackTrace();
                        MediaOut.writeObject("ERR");
                        throw new ConnectionFailException();
                    }
                    downloadProcess = -1;
                    
                    //Rename
                     
                    if(new File(songPath).exists());
                    else
                        new File(songPath).mkdir();
                    
                    File newAudio = new File(tempPath+downloadID);
                    if(new File(songPath+downloadID+".msw").exists())new File(songPath+downloadID+".msw").delete();
                    newAudio.renameTo(new File(songPath+downloadID+".msw"));
                    
                    return 0;
                    
                    
                }else
                    return -1;
            }else
                throw new ConnectionFailException();
        }catch(Exception e){
            e.printStackTrace();
            throw new ConnectionFailException();
        }
    }
    
    public LinkedList<AudioDetails> receiveUploadList() throws ConnectionFailException{
        try{
            LinkedList<AudioDetails> tempList = new LinkedList<AudioDetails>();
            CentralOut.writeObject("X07");

            while(true){
                try{
                    Object read = CentralIn.readObject();
                    if(read.equals("ACK"))break;
                    else if(read.equals("noUserID")){
                        System.err.println("User Not yet login");
                        break;
                    }else
                        tempList.add((AudioDetails)read);

                    CentralOut.writeObject("ACK");
                }catch(Exception e){
                    e.printStackTrace();
                    CentralOut.writeObject("ERR");
                    throw new ConnectionFailException();
                }
            }
            
            return tempList;
        }catch(Exception e){
            e.printStackTrace();
            throw new ConnectionFailException();
        }
    }
    
    public LinkedList<AudioDetails> searchAudioByName(String name) throws ConnectionFailException{
        try{
            CentralOut.writeObject("X08");
            if(CentralIn.readObject().equals("#SearchName")){
                CentralOut.writeObject(name);

                LinkedList<AudioDetails> searchAudio = new LinkedList<AudioDetails>();
                while(true){
                    try{
                        Object searchResult = CentralIn.readObject();

                        if(searchResult.equals("ACK"))break;
                        else if(searchResult.equals("ERR"))break;
                        else
                            searchAudio.add((AudioDetails)searchResult);

                        CentralOut.writeObject("ACK");
                    }catch(Exception e){
                        CentralOut.writeObject("ERR");
                        throw new ConnectionFailException();
                    }
                }
                return searchAudio;
            }else
                throw new ConnectionFailException();
        }catch(Exception e){
            e.printStackTrace();
            throw new ConnectionFailException();
        }
    }
    
    public LinkedList<AudioDetails> searchAudioByAlbum(String album) throws ConnectionFailException{
        try{
            CentralOut.writeObject("X09");
            if(CentralIn.readObject().equals("#SearchAlbum")){
                CentralOut.writeObject(album);
                
                LinkedList<AudioDetails> searchAudio = new LinkedList<AudioDetails>();
                while(true){
                    try{
                        Object searchResult = CentralIn.readObject();

                        if(searchResult.equals("ACK"))break;
                        else if(searchResult.equals("ERR"))break;
                        else
                            searchAudio.add((AudioDetails)searchResult);
                        
                        CentralOut.writeObject("ACK");
                    }catch(Exception e){
                        CentralOut.writeObject("ERR");
                        throw new ConnectionFailException();
                    }
                }
                return searchAudio;
            }else
                throw new ConnectionFailException();
        }catch(Exception e){
            e.printStackTrace();
            throw new ConnectionFailException();
        }
    }
    
    public LinkedList<AudioDetails> searchAudioByArtist(String artist) throws ConnectionFailException{
        try{
            CentralOut.writeObject("X10");
            if(CentralIn.readObject().equals("#SearchArtist")){
                CentralOut.writeObject(artist);
                
                LinkedList<AudioDetails> searchAudio = new LinkedList<AudioDetails>();
                while(true){
                    try{
                        Object searchResult = CentralIn.readObject();

                        if(searchResult.equals("ACK"))break;
                        else if(searchResult.equals("ERR"))break;
                        else
                            searchAudio.add((AudioDetails)searchResult);
                        
                        CentralOut.writeObject("ACK");
                    }catch(Exception e){
                        CentralOut.writeObject("ERR");
                        throw new ConnectionFailException();
                    }
                }
                return searchAudio;
            }else
                throw new ConnectionFailException();
        }catch(Exception e){
            e.printStackTrace();
            throw new ConnectionFailException();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    
    public static LinkedList<AudioElement> AudioList      = null;
    public static LinkedList<AudioDetails> UploadedList   = null;
        
    public static void main(String[] args) {
        /*
         * USER Checking
         */
        String userName                         = "Test4";
        String userPass                         = "Test";
        Scanner cin                             = new Scanner(System.in);
        String sampleSong                       = "sample.mp3";
        
        try {
            System.out.println("Iniliatize AudioList");
            AudioList = new LinkedList<AudioElement>();
            
            System.out.println("Iniliatize UploadedList");
            UploadedList = new LinkedList<AudioDetails>();
            
            Client mediaCon = new Client("192.168.1.100",17220);
            if(mediaCon.ConnectCentral() == 1)
                System.out.println("Central Server was down");
            
            
            /*
             * Part 1
             * Checking update
             */
            int ans1 = mediaCon.checkUpdate(version);
            
            if(ans1 == 0){
                System.out.println("Client version match the server");
            }else if(ans1 == -1)
                System.out.println("Wrong version");
            else 
                System.out.println("Connection fail");
            
            
            
            /*
             * Part 1.1
             * Registration
             */
            System.out.println("\n>Part 1.1 : Registration");
            int ans2 = mediaCon.Registration(userName,userPass);
            if(ans2 == 0)
                System.out.println(">>Registration success");
            else if(ans2 == -1)
                System.out.println(">>Registration FAIL");
            else 
                System.out.println("Connection fail");
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            
            /*
             * Part 2/2.1
             * Connecting to Central Server
             * Login with non-saved user file
             */
            System.out.println("\n>Part 2/1 : Connecting to Central Server");
            int ans3 = mediaCon.Login(userName,userPass);
            if(ans3 == -1){
                System.out.println("Login Fail");
            }else if(ans3 == 0){
                System.out.println("Login Success");
            }else
                System.out.println("Connection fail");
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            /*
             * Part 2/2.2
             * Connecting to Central Server
             * Login with saved user file
             */
            System.out.println("\n>Part 2/2 : Connecting to Central Server");
            notcomplete();
            //NOT COMPLETE...
            
            
            /*
             * Part 3
             * Audio List Library (User's Dependent)
             */
            System.out.println("\n>Part 3 : Audio List Library (User's Dependent)");
            //Test - Upload List to file
            addToLibrary(new File("sample.mp3"));
            
            int ans3_1 = mediaCon.UploadAudioList(AudioList);
            if(ans3_1 == 0)
                System.out.println("Upload Library Success");
            else if(ans3_1 == -1)
                System.out.println("Upload Library Fail");
            else
                System.out.println("Connection fail");
            
            //First - Download Uploaded Library
            AudioList = mediaCon.DownloadAudioList();
            
            for(int i = 0;i < AudioList.size();i++){
                System.out.println("GET PATH : "+AudioList.get(i).getPath());
            }
            
            
            //Second - None Uploaded File
            System.out.println();System.out.println();
            addToLibrary(new File("sample.mp3"));
            System.out.println();System.out.println();
            addToLibrary(new File("sample.wav"));
            
            
            for(int i = 0;i < AudioList.size();i++){
                System.out.println("GET PATH : "+AudioList.get(i).getPath());
            }
            
            
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            
            
            /*
             * Part 4
             * Get permission to Cloud(Media) Server
             */
            System.out.println("\n>Part 4 : Get permission to Cloud(Media) Server");
            
            String ans4      = mediaCon.ReceiveMediaServer();
            String serverIP  = null;
            int serverPort   = 0;
            if(ans4 != "ERR"){
                String serverInfo[] = ans4.split(":");
                serverIP     = serverInfo[0];
                serverPort   = Integer.parseInt(serverInfo[1]);
                
                System.out.println("Received media server : "+serverIP+"#"+serverPort);
            }else
                System.out.println("Connection Fail");
            
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            /*
             * Part 5
             * Disconnect to Central Server
             */
            System.out.println("\n>Part 5 : Disconnect to Central Server");
            mediaCon.DisconnectCentral();
            
            
            /*
             * Part 6
             * Connecting to Media Server
             */
            System.out.println("\n>Part 6 : Connecting to Media Server");
            int ans6 = mediaCon.ConnectMedia(serverIP, serverPort,userID);
            
            if(ans6 == 0)
                System.out.println("Connect to media server successfully");
            else if(ans6 == -1)
                System.out.println("Fail to connect to media server");
            else 
                System.out.println("Central not yet disconnected");
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            
            /*
             * Part 7
             * Upload song to media server
             */
            String song = "sample.mp3";
            System.out.println("\n>Part 7 : Upload Song to media Server");
            
            int ans7 = mediaCon.uploadToMedia(song,true);
            
            if(ans7 == NETWORK_ERROR){
                System.out.println("Connection Fail");
                System.exit(1);
            }else if(ans7 == -1){
                System.out.println("Something wrong");
                System.exit(1);
            }
            
            //ADD File to Library
            //Search from Central Server
            System.out.println("Add to Library");
            try{
                mediaCon.ConnectCentral();

                Object adAns = mediaCon.GetAudioDetails(ans7);
                if(adAns.equals("NULL"))System.out.println("NO Audio found");
                else{
                    AudioDetails receiveAD = (AudioDetails)adAns;
                    addToLibrary(receiveAD.getSongID(),receiveAD.getPath());
                }
                
                mediaCon.DisconnectCentral();
            }catch(Exception e){
                e.printStackTrace();
            }
            
            /**/
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            
            
            
            /*
             * Part 8
             * Download Song From Music Server
             * with providing Song ID
             */
            System.out.println("\n>Part 8 : Download Song From Music Server with Providing Song ID");
            if(new File(tempPath).exists()){
                deleteDirectory(new File(tempPath));
            }
            
            //You need to have songDetails(From playlist) first
            //Song path is "MSP:xxxx.msw"
            //And have song ID
            int downloadID  = ans7;
            
            int ans8 = mediaCon.downloadFromMedia(downloadID);
            if(ans8 == 0){
                System.out.println("Success");
            }else if(ans8 == -1){
                System.out.println("Something error in downloading");
            }else
                System.out.println("Connection Fail");
                
            /**/

            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            /*
             * Part 9
             * Playing the Audio in Library
             */
            System.out.println("\n>Part 9 : Playing the Audio in Library");
            for(int i = 0 ; i < AudioList.size();i++){
                String playAudioPath = "";
                if(AudioList.get(i).getType() == 'N'){  //Type 'N' Network
                    playAudioPath = songPath + AudioList.get(i).getPath().replaceAll("MSP:","");
                }else   //Type 'O' Offline
                    playAudioPath = AudioList.get(i).getPath();

                AudioTAG playAudioTAG   = new AudioTAG(playAudioPath);
                System.out.println("Song Title : "+playAudioTAG.getTagTitle());
                System.out.println("Song Artist : "+playAudioTAG.getTagArtist());
                System.out.println("Song Track : "+playAudioTAG.getTagTrack());
                System.out.println("Song Album : "+playAudioTAG.getTagAlbum());
                System.out.println("Song Year : "+playAudioTAG.getTagYear());


                MSWPlayer player = new MSWPlayer(playAudioPath,75);
                player.skip(99);
                player.playSound();
            }
            /**/

            
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            /*
             * Part 10
             * Receive Uploaded List
             */
            try{

                //
                //Relogin
                //
                System.out.println("\n>Part 10.1 : Reconnecting to Central Server");
                if(mediaCon.ConnectCentral() == 0)System.out.println("Reconnect success");
                if(mediaCon.Login(userName, userPass) == 0)System.out.println("Login success");
                

                //
                //Receive Upload List
                //
                System.out.println("\n>Part 10.2 : Receive Uploaded List");
                
                UploadedList = mediaCon.receiveUploadList();
                
                for(int i = 0;i < UploadedList.size();i++){
                    System.out.println("GET PATH : "+UploadedList.get(i).getPath());
                }
            }catch(Exception e){
                 e.printStackTrace();
            }
            
            /**/
            
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            
            /*
             * Part 11
             * Searching audio by Name
             */
            String searchValue = "";
            
            System.out.println("\n>Part 11 : Searching audio by Name");
            LinkedList<AudioDetails> searchAudio = mediaCon.searchAudioByName(searchValue);


            for(int i = 0 ; i < searchAudio.size();i ++){
                System.out.println("Searched Name : "+searchAudio.get(i).getTagTitle());
            }

            if(searchAudio.size() == 0){
                System.out.println("No result");
            }
            
            /**/
            
            
            /*
             * Part 12
             * Searching by album
             */
            searchValue = "";
            
            System.out.println("\n>Part 12 : Searching by album");

            searchAudio = mediaCon.searchAudioByAlbum(searchValue);


            for(int i = 0 ; i < searchAudio.size();i ++){
                System.out.println("Searched Name : "+searchAudio.get(i).getTagTitle()+"/"+searchAudio.get(i).getTagAlbum());
            }

            if(searchAudio.size() == 0){
                System.out.println("No result");
            }
            
            
            /**/
            
            
            
            
            /*
             * Part 13
             * Searching by artist
             */
            searchValue = "";
            
            System.out.println("\n>Part 13 : Searching by artist");
            searchAudio = mediaCon.searchAudioByArtist(searchValue);


            for(int i = 0 ; i < searchAudio.size();i ++){
                System.out.println("Searched Name : "+searchAudio.get(i).getTagTitle()+"/"+searchAudio.get(i).getTagArtist());
            }

            if(searchAudio.size() == 0){
                System.out.println("No result");
            }
            /**/
            
            
            //READ USER ENTER
            cin.nextLine(); //READ USER ENTER
            //READ USER ENTER
            
            mediaCon.DisconnectCentral();
            mediaCon.DisconnectMedia();
            /*
             * Delete the temp file after used
             */
            if(new File(tempPath).exists()){
                deleteDirectory(new File(tempPath));
            }
        } catch (Exception ex) {ex.printStackTrace();}
    }
    
    public static void notcomplete(){
        System.out.println("This function are not completed");
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
    
    public static byte[] fillBytes(byte[] vb,String v){
        char[] tempChar = v.toCharArray();
        if(tempChar.length > vb.length)return vb;
        for(int i = 0;i< vb.length;i++){
            if(i >= tempChar.length)vb[i] = 0x00;
            else vb[i]   = (byte)tempChar[i];
        }
        return vb;
    }
    
    public static void addToLibrary(File audioFile){
        try {
            addToLibrary(new AudioElement(audioFile.getAbsolutePath()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void addToLibrary(int netID,String netPath){
        try {
            addToLibrary(new AudioElement(netID,netPath));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public static void addToLibrary(AudioElement tempElement){
        try {
            boolean check   = false;
            
            if(AudioList.size() == 0){
                AudioList.add(tempElement);
                return;
            }
            
            for(int i = 0;i < AudioList.size();i++){
                if(AudioList.get(i).getPath() != null){
                    if(AudioList.get(i).getPath().equals(tempElement.getPath())){
                        System.err.println("Repeated File : "+tempElement.getPath());
                        check           = false;
                        break;
                    }
                }else if(AudioList.get(i).getNetID() == tempElement.getNetID()){
                    System.err.println("Repeated Network File : "+tempElement.getNetID());
                    check           = false;
                    break;
                }
                check   = true;
            }
            
            if(check)AudioList.add(tempElement);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public boolean isCentralConnected(){return CentralConnected;}
    public boolean isMediaConnected(){return MediaConnected;}
}
