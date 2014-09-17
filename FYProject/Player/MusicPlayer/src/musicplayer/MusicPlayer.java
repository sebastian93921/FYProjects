/*
 * Central player combiner
 * Used to linked up two player
 * 
 * Sebastian Ko 25/02/2013-2255
 */
package musicplayer;

import ClientNet.Client;
import fypaudioplayer.FYPAudioPlayer;
import fyplayer.FYPlayer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import mswmusickernel.MSWMusicKernel;

/**
 *
 * @author Sebastian Ko
 */
public class MusicPlayer extends MSWMusicKernel{
    private static boolean startFromToolBar = false;
    private static FYPAudioPlayer fap;
    private static FYPlayer fyp;
    private static Thread th = null;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        System.out.println(System.getProperty("user.dir"));
        System.out.println(System.getProperty("java.version"));
        System.out.println("T::"+FYPAudioPlayer.version);
        System.out.println("P::"+FYPlayer.version);
        System.out.println("C::"+MSWPlayer.MSWPlayer.version);
        System.out.println("N::"+Client.version);
        
        readConfig();
        
        if(startFromToolBar){
            fap = new FYPAudioPlayer();
            fap.setVisible(true);
            do{
                try{Thread.sleep(1000);}catch(Exception e){}
            }while(!FYPAClosed);
            do{
                try{
                    fap.dispose();
                    try{Thread.sleep(1000);}catch(Exception e){}
                }catch(Exception e){e.printStackTrace();}
            }while(fap.isDisplayable());
            fap = null;
        }
        while(true){
            fyp        = new FYPlayer();
            fyp.setVisible(true);
            do{
                try{Thread.sleep(1000);}catch(Exception e){}
            }while(!FYPclosed);
            
            do{
                try{
                    fyp.dispose();
                    try{Thread.sleep(1000);}catch(Exception e){}
                }catch(Exception e){e.printStackTrace();}
            }while(fyp.isDisplayable());
            fyp = null;
            
            /*
             * Change player
             */
            
            fap = new FYPAudioPlayer();
            fap.setVisible(true);
            do{
                try{Thread.sleep(1000);}catch(Exception e){}
            }while(!FYPAClosed);
            
            do{
                try{
                    fap.dispose();
                    try{Thread.sleep(1000);}catch(Exception e){}
                }catch(Exception e){e.printStackTrace();}
            }while(fap.isDisplayable());
            fap = null;
        }
    }
    
    private static void readConfig(){
        boolean dirError = false;
        try{
            try{if(!new File(userDataPath).exists() && !new File(userDataPath).isDirectory())new File(userDataPath).mkdir();}catch(Exception ex){}
            
            BufferedReader in = new BufferedReader(new FileReader(userDataPath+"Settings.conf"));
            String tempRead = "";
            if(in.readLine().equals(":MPS")){
                while((tempRead = in.readLine()) != null){
                    if(!tempRead.startsWith("//")){
                       //Global setting - User directory
                        if(tempRead.startsWith("::user.dir = ") && tempRead.endsWith(";")){
                            String userDir = new String(tempRead.replace("::user.dir = ","")).replace(";","");
                            if(System.getProperty("user.dir").equals(userDir)){
                                System.out.println("User directory are same");
                            }else{
                                System.err.println("User directory are not same!");
                                dirError = true;
                                break;
                            }
                        }
                        //Start from tool bar
                        else if(tempRead.startsWith("::StartFromToolBar = ") && tempRead.endsWith(";")){
                            if(tempRead.endsWith("false;"))startFromToolBar = false;
                            else if(tempRead.endsWith("true;"))startFromToolBar = true;
                        }
                        //Nsetting - online mode
                        else if(tempRead.startsWith("::OnlineMode = ") && tempRead.endsWith(";")){
                            if(tempRead.endsWith("false;"))onlineMode = false;
                            else if(tempRead.endsWith("true;"))onlineMode = true;
                        }
                        //Nsetting - central server ip
                        else if(tempRead.startsWith("::ServerIP = ") && tempRead.endsWith(";")){
                            CentralServerIP = new String(tempRead.replace("::ServerIP = ", "")).replace(";","");
                            System.out.println("Read server ip >"+CentralServerIP);
                        }
                        
                    }
                }
            }
            in.close();
        }catch(FileNotFoundException e){
            try{
                PrintWriter ps = new PrintWriter(new FileWriter(userDataPath+"Settings.conf",false));
                ps.println(":MPS");  //MPS - Music player settings header
                ps.println("//Music Player Settings");
                ps.println("::StartFromToolBar = false;");
                ps.println("//Network Setting");
                ps.println("::OnlineMode = true;");
                ps.println("::ServerIP = 127.0.0.1;");
                ps.println("//Global Setting");
                ps.println("::user.dir = "+System.getProperty("user.dir")+";");
                ps.close();
                System.out.println("Reprint settings file");
            }catch(Exception ex){
                System.err.println("Reprint settings file fail");
            }
        }catch(Exception e){
            dirError = true;
        }
        
        if(dirError){
            if(!removeDir(new File(userDataPath)))System.err.println("Directory remove fail.");
        }
    }
    
    public static boolean removeDir(File directory) {
        try{
            if (directory == null)
                return false;
            if (!directory.exists())
                return true;
            if (!directory.isDirectory())
                return false;

            String[] list = directory.list();

            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    File entry = new File(directory, list[i]);


                    if (entry.isDirectory())
                    {
                        if (!removeDir(entry))
                        return false;
                    }
                    else
                    {
                        if (!entry.delete())
                        return false;
                    }
                }
            }

            return directory.delete();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}


