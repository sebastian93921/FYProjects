/*
 * Central Music Player Kernel
 * This used to combine two player in one single thread
 * providing much more control to 2 player
 * 
 * ADD: Inidividual type between two players - 26/02/2013
 * ADD: Network connection user - 28/02/2013
 * ADD: Moving two player values to this kernel - 19/03/2013
 * ADD: Users encrypted login information - 19/03/2013
 * ADD: Reset user - 19/05/2013
 * 
 * 
 * Sebastian Ko 25/02/2013-2255
 */
package mswmusickernel;

import MSWPlayer.MSWPlayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import net.AudioElement;

/**
 *
 * @author Ivan
 */
public class MSWMusicKernel extends javax.swing.JFrame{

    public static MSWPlayer player   = null;
    public static Thread playBack    = null;
    public static int defaultVolume   = 90;
    public static boolean FYPAClosed= false;
    public static boolean FYPclosed  = false;
    public static int closeTime       = -1;
    
    public static String playingTitle     = "";
    
    /*
     * Global Playback
     */
    public static LinkedList<AudioElement> AudioList     = null;
    public static int currentFileNumber                    = -1;
    public static boolean isPlaying                       = false;
    public static boolean stop                          = false;
    public static boolean pause                         = false;
    
    
    public static boolean userControlled                   = false;
    public static boolean loopOne                       = false;   //-------------------------------------------Loop One Song
    public static boolean loop                          = false;   //-------------------------------------------Loop Button
    
    public static String userDataPath                     = ".\\usr\\";
    public static String userPlaylistPath                   = userDataPath+"playlist\\";
    public static final File AudioLibrary                 = new File(userDataPath+"MusicLibrary.fypapl");
    public static boolean random                        = false;
    
    
    /*
     * Network Used
     */
    public static boolean onlineMode                     = true;
    public static String CentralServerIP                   = "127.0.0.1";
    /*Login*/
    private static String userName                      = null;
    private static String userPass                       = null;
    public static char nc[] = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T',
                    'U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q',
                    'r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','~','!','@','#','$','%',
                    '^','&','*','(',')','_','+','=','-','`','[',']','{','}',';','\'',':','\"','<','>','?',',','.','/'
                    };
    private static char pc[] = {'A','B','C','D','E','F','N','O','P','Q','R','S','T','7','8','9','~','!','@','#','$','%',
                    'U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q',
                    'r','s','0','1','2','3','4','t','u','v','w','x','y','z','5','6','G','H','I','J','K','L','M',
                    '[',']','{','}',';','\'',':','\"','<','>','?',',','.','^','&','*','(',')','_','+','=','-','`','/'
                    };
    
    public static void setUserName(String name){
        userName = name;
    }
    public static String getUserName(){return userName;}
    public static String getUserPass(){return userPass;}
    public static void resetUser(){
        userName                      = null;
        userPass                       = null;
        
        new File(userDataPath+"udlfn.dat").delete();
        new File(userDataPath+"udlfp.dat").delete();
    }
    
    public static int readLoginFile(){
        try{
            /*
             * AC Name
             */
            FileInputStream readB = new FileInputStream(new File(userDataPath+"udlfn.dat"));
            byte[] buffer         = new byte[readB.available()];
            readB.read(buffer);

            String tempName = "";
            for(int i = 0 ; i < buffer.length ; i++){
                tempName += nc[(buffer[i] << (8*0))&0xFF];
            }
            userName = tempName;
            
            /*
             * AC Pass
             */
            readB            = new FileInputStream(new File(userDataPath+"udlfp.dat"));
            buffer           = new byte[readB.available()];
            readB.read(buffer);

            String tempPass = "";
            for(int i = 0 ; i < buffer.length ; i++){
                tempPass += pc[(buffer[i] << (8*0))&0xFF];
            }
            userPass = tempPass;
            
            return 0;
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    
    public static int saveLoginFile(String name,String pass){
        try{
            if(!new File(userDataPath).exists() || !new File(userDataPath).isDirectory())new File(userDataPath).mkdir();
            /*
             * AC Name
             */
            FileOutputStream fout = new FileOutputStream( new File(userDataPath+"udlfn.dat"),false);
            char nameData[] = name.toCharArray();
            for(int i = 0 ; i < nameData.length ; i++){
                for(int j = 0;j < nc.length ; j++){
                    if(nc[j] == nameData[i]){
                        fout.write((byte)j);
                        break;
                    }
                }
            }
            /*
             * AC Pass
             */
            fout = new FileOutputStream(new File(userDataPath+"udlfp.dat"),false);
            char passData[] = pass.toCharArray();
            for(int i = 0 ; i < passData.length ; i++){
                for(int j = 0;j < pc.length ; j++){
                    if(pc[j] == passData[i]){
                        fout.write((byte)j);
                        break;
                    }
                }
            }
            
            return 0;
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    
    
    /*
     * Media
     */
    public static final Object downloadLock             = new Object();
    public static LinkedList<Integer> downList          = null;
    
    
    /*
     * Playlist
     */
    public static String currentPlaylistName               = null;
    public static LinkedList<Integer> currentPlaylist      = null;
    
    public static void saveListToFile(){
        try {
            /*
             * Save Total Audio List to File
             */
            ObjectOutputStream totalAudioList = new ObjectOutputStream ( new FileOutputStream(AudioLibrary,false));
            totalAudioList.writeObject(AudioList);
            totalAudioList.close();
        } catch (IOException ex) {ex.printStackTrace();}
    }
}
