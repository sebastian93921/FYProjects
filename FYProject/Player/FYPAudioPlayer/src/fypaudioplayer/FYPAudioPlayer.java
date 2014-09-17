/*
 * A Very Simple Music Player w/ Tool Kit
 * Using MSWPlayer for playback
 * A Part of Extended Music Player
 * 
 * ADD: Audio List / The total data of Playlist - 18/12/2012
 * ADD: Control Panel Popup - 20/12/2012
 * Debug: Memory Usage & Music Details Class Name - 21/12/2012
 * ADD: Audio Download List Queuing - 25/02/2013
 * ADD: Queuing List locking to reduce system resources - 25/02/2013
 * Debug: Stoping a thread when multi click on one button - 26/02/2013
 * ADD: Two Player combining - 26/02/2013
 * ADD: Background processing in reading files - 01/03/2013
 * ADD: Playlist reading(developing) - 20/03/2013
 * Fix: Playlist may not shown - 23/03/2013
 * Debug: Filelist error - 23/03/2013
 * ADD: Playlist loading - 23/03/2013
 * ADD: Support Playlist function - 02/05/2013
 * Debug: Combining two player with wrong playing list - 02/05/2013
 * Fix: Playing wrong audio when in different playlist - 17/05/2013
 * Fix: function name in maximize frame - 18/05/2013
 * ADD: Loop icon of player - 25/05/2013
 * Debug: Downloading queue running before library updating - 26/05/2013
 * Debug: Playing wrong music when changing the playlist - 29/05/2013
 * Debug: Major Debugging - 11/06/2013
 * Debug: Playing in multiple thread - 11/06/2013
 * 
 * 
 * Sebastian Ko
 * 16/11/2012
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fypaudioplayer;

import ClientNet.Client;
import MSWPlayer.MSWPlayer;
import com.sun.awt.AWTUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import mswmusickernel.MSWMusicKernel;
import net.AudioDetails;
import net.AudioElement;
import net.AudioImage;
import tag.codec.AudioIDInvalidException;
import tag.codec.AudioTAG;


/**
 *
 * @author Ivan
 */
public class FYPAudioPlayer extends MSWMusicKernel implements DropTargetListener{
    public static final String version    = "0.4.0 EMPlayerToolBar Update 11 - BETA 1";
    private final int WIDTH             = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int HEIGHT            = Toolkit.getDefaultToolkit().getScreenSize().height;

    /*
     * Music Details
     */
    private static musicDetails MusicDetails                = null;
    private static boolean isPopup                        = false;
    
    public static boolean showControl                      = false;
    private int previousPlay                             = -1;
    private int clickPreviousTime                        = 0;
    private static DefaultTableModel tm                  = null;
    
    /*
     * Image Icon
     */
    private static final ImageIcon listCover                = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/default-listCover.png"));
    private static final ImageIcon openListIcon             = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/open.png"));
    private static final ImageIcon closeListIcon             = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/closeList.png"));
    private static final ImageIcon pauseIcon                = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/pause.png"));
    private static final ImageIcon playIcon                 = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/play.png"));
    private static final ImageIcon loopIcon                 = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/loop.png"));
    private static final ImageIcon loopOnceIcon             = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/loopOnce.png"));
    private static final ImageIcon loopAllIcon              = new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/loopAll.png"));
    
    /*
     * Playerlist
     */
    private int rebuildNum[]                             = null;
    private String presetPlaylistName                    = null;
    
    /*
     * Media Connection
     */
    private static final String songPath                  = ".\\net\\";
    private static boolean NETWORK_ERROR              = false;
    private static final char ONLINE_MODE              = 'N';
    private static final char OFFLINE_MODE             = 'O';
    private static Client mediaCon                       = null;
    private static networkDetails networkDetails            = null;
    private static boolean downloading                     = false;
    
    /*
     * Reading Files
     */
    private static boolean reading                        = false;
    private static File[] files                           = null;
    
        
    /** Creates new form FYPAudioPlayer */
    public FYPAudioPlayer() {
        FYPAClosed = false;
        System.out.println("System Default Encoding Type : "+System.getProperty("sun.jnu.encoding"));
        if(currentPlaylistName != null){
            System.out.println(currentPlaylistName+" playlist are playing");
        }
        //System.setOut(new PrintStream(new NullOutputStream()));
        
        
        initComponents();
        audioScrollPane.getViewport().setBackground(Color.WHITE);
        try{this.setIconImage(new ImageIcon(FYPAudioPlayer.class.getResource("/defaultSkin/frameIcon.png")).getImage());}catch(Exception e){}
        
        /*
         * Frame setting
         */
        this.setLocation(0, HEIGHT-this.getHeight()-40);
        audioListPanel.setSize(220,0);
        controlPanel.setVisible(false);
        durationText.setLocation(20, 280);
        
        /*/SE 1.7
        this.setOpacity(0.9f);
        this.setBackground(new Color(255, 255, 255, 0));
        /**/
        
        //SE 1.6 u 10
        AWTUtilities.setWindowOpacity(this,0.9f);
        AWTUtilities.setWindowOpaque(this, false);
        /**/
        
        
        /*
         * Initiatize Function
         */
        this.volumeSilder.setValue(defaultVolume);
        DropTarget dt   = new DropTarget(this.backgroundLabel,this);
        DropTarget dt2  = new DropTarget(this.musicListBackground,this);
        
        
        /*
         * Control Frame
         */
        MusicDetails = new musicDetails(0,HEIGHT-this.getHeight()-40-100+280);
        if(currentFileNumber >= 0){
            try{
                if(currentPlaylistName == null){
                    MusicDetails.setSong(new File(AudioList.get(currentFileNumber).getPath()));

                }else{
                    MusicDetails.setSong(new File(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath()));

                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        
        /*
         * User's Folder Defining
         */
        if(new File(this.userDataPath).exists()){
            if(!new File(this.userDataPath).isDirectory())new File(this.userDataPath).mkdir();
        }else
            new File(this.userDataPath).mkdir();
        
        /*
         * Playlist Library
         */
        if(AudioLibrary.exists() && AudioList == null){
            try{
                ObjectInputStream audioListFile = new ObjectInputStream(new FileInputStream(AudioLibrary));
                AudioList                  = (LinkedList<AudioElement>)audioListFile.readObject();
                System.out.println("Audio Library Loaded");
                if(currentFileNumber < 0)currentFileNumber            = 0;
                audioListFile.close();
            }catch(Exception e){
                AudioList                  = new LinkedList<AudioElement>();
            }
        }else if(AudioList == null)
            AudioList = new LinkedList<AudioElement>();
        
        /*
         * Playlist define
         */
        try{
            if(!new File(userPlaylistPath).exists() || !new File(userPlaylistPath).isDirectory())new File(userPlaylistPath).mkdir();

            File playlistFiles[] = new File(userPlaylistPath).listFiles();
            this.PlaylistComboBox.removeAllItems();
            this.PlaylistComboBox.addItem("#All Songs");
            
            if(playlistFiles.length != 0){
                for(int i = 0 ; i < playlistFiles.length; i++){
                    if(playlistFiles[i].getName().endsWith(".list")){
                        final String tempListName = playlistFiles[i].getName();
                        this.PlaylistComboBox.addItem(tempListName.replaceAll(".list",""));
                    }
                }
            }

            System.out.println("Playlist Loaded");
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Playlist loading failed");
        }
        
        /*
         * Network Connection
         */
        if(mediaCon == null && onlineMode){
            NETWORK_ERROR = false;
            networkDetails = new networkDetails(0,HEIGHT-this.getHeight()-40-12+280);
            networkDetails.setVisible(true);
            
            new Thread(new Runnable(){public void run(){
                /*
                 * Read saved User password
                 */
                if(readLoginFile() == -1 && onlineMode){
                    networkDetails.setStatus("Fail to read user's file");
                    onlineMode = false;
                }
                else{

                    mediaCon = new Client(CentralServerIP,17220);

                    if(mediaCon.ConnectCentral() == 0){
                        networkDetails.setStatus("Connection Start");

                        if(mediaCon.Login(getUserName(),getUserPass()) == 0)networkDetails.setStatus("Login Success");
                        else {
                            networkDetails.setStatus("Login Fail");
                            onlineMode = false;
                        }

                        /*
                         * Add Queue
                         */
                        if(downList == null){
                            downList = new LinkedList<Integer>();
                        }
                    }else{
                        mediaCon  = null;
                        onlineMode = false;
                    }
                }
            }}).start();
        }
        
        this.LibraryUpdate();
        
        /*
         * Audio Downloading
         */
        new Thread(new Runnable(){public void run(){
            if(onlineMode && mediaCon.receiveUserID() != null){
                try{
                    mediaCon.ConnectMedia(mediaCon.ReceiveMediaServer(), mediaCon.receiveUserID());
                    mediaCon.DisconnectCentral();
                    
                    networkDetails.setStatus("Downloading Audio..");
                    System.out.println("Downloading Audio..");
                    
                    synchronized(downloadLock){
                        
                        downloading = true;
                        new Thread(new Runnable(){public void run(){
                            try{
                                Thread.sleep(500);
                                while(downloading){
                                    Thread.sleep(500);
                                    networkDetails.setStatus(downList.size()+" Audio Remaining - "+mediaCon.downloadProcess+"%");
                                }
                                networkDetails.setStatus("Download Complete ");
                            }catch(Exception e){}
                        }}).start();
                        
                        while(true){
                            if(NETWORK_ERROR)downloadLock.wait();
                            try{
                                while(downList.size() != 0){
                                    mediaCon.downloadFromMedia(downList.get(0));
                                    downList.remove(0);
                                }
                                downloading = false;
                                System.out.println("Download Complete ");
                                networkDetails.waitToClose(2);
                                downloadLock.wait();
                            }catch(Exception e){
                                e.printStackTrace();
                                NETWORK_ERROR = true;
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    mediaCon.DisconnectMedia();
                    NETWORK_ERROR = true;
                }
            }
        }}).start();
        
        
        
        /*
         * Details Panel
         */
        new Thread(new Runnable(){public void run(){
            while(true){
                synchronized(this){
                    try{
                        wait(500);
                        if(pause){
                            durationText.setText(playingTitle+" - Pause");
                        }else if(isPlaying){
                            String duration = null;
                            
                            if(currentPlaylistName == null)
                                duration = playingTitle+" - "+
                                    new durationToString(player.getCurrentPosInSec()).toString()+
                                    "/" + new durationToString(player.getDurationInSec()).toString();
                            else
                                duration = playingTitle+"/"+currentPlaylistName+" - "+
                                    new durationToString(player.getCurrentPosInSec()).toString()+
                                    "/" + new durationToString(player.getDurationInSec()).toString();

                            durationText.setText(duration);
                        }else if(reading){
                            durationText.setText("Reading "+AudioList.size()+" Audio(s)");
                        }else
                            durationText.setText("Stop");
                        
                    }catch(Exception ex){
                        durationText.setText("Stop");
                    }
                    System.gc();    //Force System clean up
                    
                }
            }
        }}).start();
        
        if(networkDetails != null)networkDetails.waitToClose(10);
        
    }

    private void musicPlay(){
        
        
        playBack = new Thread(new Runnable(){public void run(){
            stop = false;
            try{player.close();}catch(Exception e){}            
            if(AudioList != null){
                try{
                    audioPlayback();
                }catch(IndexOutOfBoundsException e){
                    currentFileNumber = 0;
                    isPlaying = false;
                }finally{
                    stop = true;
                    btnPlay.setIcon(playIcon);
                }
            }
        }});
        playBack.start();
    }
    
    private void audioPlayback(){
        stop  = false;
        pause = false;
        try{player.close();}catch(Exception e){}
        if(AudioList != null ){
            try{
                while(!stop  && !FYPAClosed){
                    stop  = false;
                    pause = false;
                    userControlled = false;
                    isPlaying      = true;
                    try{
                        try{
                            String playAudioPath = "";
            
                            if(currentPlaylistName == null){
                                if(AudioList.get(currentFileNumber).getType() == 'N'){  //Type 'N' Network
                                    if(!onlineMode)throw new Exception("Player are not in online mode");
                                    playAudioPath = songPath + AudioList.get(currentFileNumber).getPath().replaceAll("MSP:","");
                                }else   //Type 'O' Offline
                                    playAudioPath = AudioList.get(currentFileNumber).getPath();
                            }else{
                                if(AudioList.get(currentPlaylist.get(currentFileNumber)).getType() == 'N'){  //Type 'N' Network
                                    if(!onlineMode)throw new Exception("Player are not in online mode");
                                    playAudioPath = songPath + AudioList.get(currentPlaylist.get(currentFileNumber)).getPath().replaceAll("MSP:","");
                                }else   //Type 'O' Offline
                                    playAudioPath = AudioList.get(currentPlaylist.get(currentFileNumber)).getPath();

                            }

                            /*Test*/
                            System.out.println("Playing on path : "+playAudioPath);

                            player = new MSWPlayer(playAudioPath,defaultVolume);
                            if(currentPlaylistName == null){
                                MusicDetails.setSong(new File(AudioList.get(currentFileNumber).getPath()));
                                
                            }else{
                                MusicDetails.setSong(new File(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath()));
                                
                            }
                            setPlaying(currentFileNumber);
                            btnPlay.setIcon(pauseIcon);
                            try{player.playSound();}catch(IndexOutOfBoundsException e){e.printStackTrace();}
                            
                            try{
                                while(!player.isComplete()){
                                    try{player.close();}catch(Exception e){e.printStackTrace();}
                                    Thread.sleep(5);
                                }
                            }catch(NullPointerException e){
                                //break while player is missing
                            }
                            isPlaying = false;

                        }catch(FileNotFoundException e){
                            if(AudioList.get(currentFileNumber).getType() == ONLINE_MODE && onlineMode){
                                networkDetails.setStatus("Download not yet complete");
                                
                                if(currentPlaylistName == null){
                                    synchronized(downloadLock){
                                        downList.addFirst(AudioList.get(currentFileNumber).getNetID());
                                        downloadLock.notifyAll();
                                    }

                                }else{
                                    synchronized(downloadLock){
                                        downList.addFirst(AudioList.get(currentPlaylist.get(currentFileNumber)).getNetID());
                                        downloadLock.notifyAll();
                                    }
                                }

                                
                            }else if(AudioList.get(currentFileNumber).getType() == ONLINE_MODE){
                                tm.setValueAt("N", currentFileNumber, 0);
                            }else
                                tm.setValueAt("!!", currentFileNumber, 0);
                        }catch(AudioIDInvalidException ex){
                            if(onlineMode){
                                networkDetails.setStatus("Audio ID does not match the File");
                                
                                if(currentPlaylistName == null){
                                    new File(AudioList.get(currentFileNumber).getPath().replace("MSP:", ".\\net\\")).delete();
                                    
                                    synchronized(downloadLock){
                                        downList.addFirst(AudioList.get(currentFileNumber).getNetID());
                                        downloadLock.notifyAll();
                                    }

                                }else{
                                    new File(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath().replace("MSP:", ".\\net\\")).delete();
                                    
                                    synchronized(downloadLock){
                                        downList.addFirst(AudioList.get(currentPlaylist.get(currentFileNumber)).getNetID());
                                        downloadLock.notifyAll();
                                    }

                                }
                            }
                        }catch(IndexOutOfBoundsException e){
                            currentFileNumber = 0;
                            isPlaying = false;
                            break;
                        }

                        if(!userControlled){
                            if(loopOne);
                            else currentFileNumber++;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        isPlaying = false;
                    }
                }

                if(loop){
                    if(currentPlaylistName != null && currentFileNumber == currentPlaylist.size())currentFileNumber = 0;
                    else if(currentFileNumber == AudioList.size())currentFileNumber = 0;
                }
            
            }catch(IndexOutOfBoundsException e){
                currentFileNumber = 0;
                isPlaying = false;
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        checkPanelTop = new javax.swing.JPanel();
        checkPanelRight = new javax.swing.JPanel();
        checkPanelLeft = new javax.swing.JPanel();
        checkPanelBottom = new javax.swing.JPanel();
        deControlPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        btnListPopup = new javax.swing.JButton();
        btnPlay = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLoop = new javax.swing.JButton();
        volumeSilder = new javax.swing.JSlider();
        detailsExit = new javax.swing.JButton();
        PopupDetails = new javax.swing.JButton();
        durationText = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();
        audioListPanel = new javax.swing.JPanel();
        PlaylistComboBox = new javax.swing.JComboBox();
        btnMax = new javax.swing.JButton();
        openFile = new javax.swing.JButton();
        audioScrollPane = new javax.swing.JScrollPane();
        audioTable = new javax.swing.JTable();
        musicListBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(0, 0, 0));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("FYPAudioPlayer"); // NOI18N
        setResizable(false);
        setUndecorated(true);
        getContentPane().setLayout(null);

        checkPanelTop.setOpaque(false);
        checkPanelTop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                checkPanelTopMouseEntered(evt);
            }
        });
        getContentPane().add(checkPanelTop);
        checkPanelTop.setBounds(0, 280, 280, 2);

        checkPanelRight.setOpaque(false);
        checkPanelRight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                checkPanelRightMouseEntered(evt);
            }
        });
        getContentPane().add(checkPanelRight);
        checkPanelRight.setBounds(240, 240, 300, 90);

        checkPanelLeft.setOpaque(false);
        checkPanelLeft.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                checkPanelLeftMouseEntered(evt);
            }
        });
        getContentPane().add(checkPanelLeft);
        checkPanelLeft.setBounds(0, 270, 2, 50);

        checkPanelBottom.setOpaque(false);
        checkPanelBottom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                checkPanelBottomMouseEntered(evt);
            }
        });
        getContentPane().add(checkPanelBottom);
        checkPanelBottom.setBounds(0, 298, 300, 10);

        deControlPanel.setOpaque(false);
        deControlPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deControlPanelMouseEntered(evt);
            }
        });
        getContentPane().add(deControlPanel);
        deControlPanel.setBounds(20, 282, 220, 16);

        controlPanel.setOpaque(false);
        controlPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                controlPanelMouseExited(evt);
            }
        });
        controlPanel.setLayout(null);

        btnListPopup.setIcon(openListIcon);
        btnListPopup.setContentAreaFilled(false);
        btnListPopup.setMaximumSize(new java.awt.Dimension(50, 50));
        btnListPopup.setMinimumSize(new java.awt.Dimension(50, 50));
        btnListPopup.setPreferredSize(new java.awt.Dimension(50, 50));
        btnListPopup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnListPopupActionPerformed(evt);
            }
        });
        controlPanel.add(btnListPopup);
        btnListPopup.setBounds(5, 0, 20, 20);

        btnPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/play.png"))); // NOI18N
        btnPlay.setContentAreaFilled(false);
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });
        controlPanel.add(btnPlay);
        btnPlay.setBounds(61, 0, 20, 20);

        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/back.png"))); // NOI18N
        btnBack.setContentAreaFilled(false);
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        controlPanel.add(btnBack);
        btnBack.setBounds(32, 0, 20, 20);

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/next.png"))); // NOI18N
        btnNext.setContentAreaFilled(false);
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        controlPanel.add(btnNext);
        btnNext.setBounds(90, 0, 20, 20);

        btnLoop.setIcon(loopIcon);
        btnLoop.setContentAreaFilled(false);
        btnLoop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoopActionPerformed(evt);
            }
        });
        controlPanel.add(btnLoop);
        btnLoop.setBounds(121, 0, 20, 20);

        volumeSilder.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        volumeSilder.setOpaque(false);
        volumeSilder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSilderStateChanged(evt);
            }
        });
        controlPanel.add(volumeSilder);
        volumeSilder.setBounds(150, 3, 58, 15);

        getContentPane().add(controlPanel);
        controlPanel.setBounds(20, 280, 210, 20);

        detailsExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/exit.png"))); // NOI18N
        detailsExit.setContentAreaFilled(false);
        detailsExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detailsExitActionPerformed(evt);
            }
        });
        getContentPane().add(detailsExit);
        detailsExit.setBounds(5, 286, 10, 10);

        PopupDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/popup.png"))); // NOI18N
        PopupDetails.setBorderPainted(false);
        PopupDetails.setContentAreaFilled(false);
        PopupDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                PopupDetailsMouseEntered(evt);
            }
        });
        getContentPane().add(PopupDetails);
        PopupDetails.setBounds(0, 280, 20, 20);

        durationText.setFont(new java.awt.Font("微軟正黑體", 1, 11));
        durationText.setForeground(new java.awt.Color(255, 255, 255));
        durationText.setText("--");
        getContentPane().add(durationText);
        durationText.setBounds(240, 280, 200, 20);

        backgroundLabel.setBackground(new java.awt.Color(255, 255, 255));
        backgroundLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/bg.png"))); // NOI18N
        backgroundLabel.setAlignmentY(0.0F);
        backgroundLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        getContentPane().add(backgroundLabel);
        backgroundLabel.setBounds(0, 280, 510, 20);

        audioListPanel.setOpaque(false);
        audioListPanel.setLayout(null);

        PlaylistComboBox.setBorder(null);
        PlaylistComboBox.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        PlaylistComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlaylistComboBoxActionPerformed(evt);
            }
        });
        audioListPanel.add(PlaylistComboBox);
        PlaylistComboBox.setBounds(4, 4, 120, 18);

        btnMax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/maximum.png"))); // NOI18N
        btnMax.setContentAreaFilled(false);
        btnMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMaxActionPerformed(evt);
            }
        });
        audioListPanel.add(btnMax);
        btnMax.setBounds(130, 4, 20, 20);

        openFile.setForeground(new java.awt.Color(255, 255, 255));
        openFile.setText("Open");
        openFile.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        openFile.setContentAreaFilled(false);
        openFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });
        audioListPanel.add(openFile);
        openFile.setBounds(155, 5, 60, 17);

        audioScrollPane.setBackground(new java.awt.Color(0, 0, 0));
        audioScrollPane.setBorder(null);
        audioScrollPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        audioScrollPane.setOpaque(false);

        audioTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        audioTable.setAlignmentX(0.0F);
        audioTable.setAlignmentY(0.0F);
        audioTable.setGridColor(new java.awt.Color(255, 255, 255));
        audioTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        audioTable.setOpaque(false);
        audioTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                audioTableMouseClicked(evt);
            }
        });
        audioScrollPane.setViewportView(audioTable);

        audioListPanel.add(audioScrollPane);
        audioScrollPane.setBounds(4, 25, 212, 250);

        musicListBackground.setBackground(new java.awt.Color(250, 250, 250));
        musicListBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkin/audioListBk.png"))); // NOI18N
        audioListPanel.add(musicListBackground);
        musicListBackground.setBounds(0, 0, 220, 280);

        getContentPane().add(audioListPanel);
        audioListPanel.setBounds(0, 0, 220, 280);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-507)/2, (screenSize.height-300)/2, 507, 300);
    }// </editor-fold>//GEN-END:initComponents

    
    private void btnListPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListPopupActionPerformed
        // TODO add your handling code here:
        if(!isPopup){
            Popup();
            btnListPopup.setIcon(closeListIcon);
        }else{
            Popup();
            btnListPopup.setIcon(openListIcon);
        }
    }//GEN-LAST:event_btnListPopupActionPerformed

    private void btnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayActionPerformed
        // TODO add your handling code here:
       if(!isPlaying && !reading){
            musicPlay();
        }else if(!pause && isPlaying){
            player.pause();
            pause = true;
            btnPlay.setIcon(playIcon);
        }else if(pause && isPlaying){
            player.resume();
            pause = false;
            btnPlay.setIcon(pauseIcon);
        }
    }//GEN-LAST:event_btnPlayActionPerformed

    private void btnMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMaxActionPerformed
        // TODO add your handling code here:
        float value = 0.9f;
        while(value >= 0.0){
            try {Thread.sleep(20);} catch (Exception ex) {}
            //this.setOpacity(value);        //SE 1.7
            AWTUtilities.setWindowOpacity(this,value);  //SE 1.6u10
            value -= 0.1f;
        }
        //this.setOpacity(0.0f);    //SE 1.7
        AWTUtilities.setWindowOpacity(this,0.0f);
        try{
            MusicDetails.dispose();MusicDetails       = null;
            tm                                  = null;
            rebuildNum                          = null;
            presetPlaylistName                  = null;
            try{mediaCon.DisconnectCentral();}catch(Exception ex){}
            try{mediaCon.DisconnectMedia();}catch(Exception ex){}
            mediaCon                             = null;
            try{networkDetails.dispose();}catch(Exception ex){}
            networkDetails                          = null;
            files                                 = null;
            MusicDetails                           = null;
            networkDetails                          = null;
        }catch(Exception e){
            /*
             * Do NOTHING
             */
            e.printStackTrace();
        }
        this.dispose();
        FYPAClosed    = true;
    }//GEN-LAST:event_btnMaxActionPerformed

    
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        try{
            currentFileNumber += 2;
            userControlled = true;
            
            if(isPlaying){
                stop();
            }
            musicPlay();
            
            try {playingTitle = new AudioTAG(AudioList.get(currentFileNumber).getPath()).getTagTitle();} catch (IOException ex) {}
        }catch(Exception e){e.printStackTrace();}
        try {Thread.sleep(500);} catch (InterruptedException ex) {}
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // TODO add your handling code here:
        clickPreviousTime ++;
        if(clickPreviousTime == 2){
            try{
                clickPreviousTime = 0;
                userControlled = true;

                if(isPlaying){
                    stop();
                }
                musicPlay();
                try {playingTitle = new AudioTAG(AudioList.get(currentFileNumber).getPath()).getTagTitle();} catch (IOException ex) {}
            }catch(Exception e){e.printStackTrace();}
            try {Thread.sleep(1000);} catch (InterruptedException ex) {}
        }else{     
            new Thread(new Runnable(){public void run(){
                try {Thread.sleep(500);} catch (InterruptedException ex) {}
                if(clickPreviousTime == 1){
                    clickPreviousTime = 0;
                    currentFileNumber ++;
                    userControlled = true;

                    if(isPlaying && !stop){
                        stop();
                    }
                    musicPlay();
                    try {playingTitle = new AudioTAG(AudioList.get(currentFileNumber).getPath()).getTagTitle();} catch (Exception ex) {}
                }
                try {Thread.sleep(1000);} catch (InterruptedException ex) {}
           }}).start();
        }
    }//GEN-LAST:event_btnBackActionPerformed

    private void volumeSilderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSilderStateChanged
        // TODO add your handling code here:
        defaultVolume = volumeSilder.getValue();
        durationText.setText("Set volume to - "+defaultVolume+"%");
        try{
            player.setVolume(defaultVolume);
        }catch(Exception e){}
    }//GEN-LAST:event_volumeSilderStateChanged

    private void PopupDetailsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PopupDetailsMouseEntered
        // TODO add your handling code here:
        if(!MusicDetails.isPopup && isPlaying && !isPopup){
            MusicDetails.Popup(true);
        }
    }//GEN-LAST:event_PopupDetailsMouseEntered

    private void detailsExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detailsExitActionPerformed
        // TODO add your handling code here:
        float value = 0.9f;
        while(value >= 0.0){
            try {Thread.sleep(20);} catch (Exception ex) {}
            //this.setOpacity(value);        //SE 1.7
            AWTUtilities.setWindowOpacity(this,value);  //SE 1.6u10
            value -= 0.1f;
        }
        //this.setOpacity(0.0f);    //SE 1.7
        AWTUtilities.setWindowOpacity(this,0.0f);
        this.setVisible(false);
            
        System.exit(0); 
    }//GEN-LAST:event_detailsExitActionPerformed

    private void audioTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_audioTableMouseClicked
        // TODO add your handling code here:
        if(evt.getClickCount() == 2){
            if(presetPlaylistName != null){
                currentPlaylistName     = presetPlaylistName;
                try{
                    currentPlaylist = (LinkedList<Integer>)new ObjectInputStream(new FileInputStream(new File(userPlaylistPath+currentPlaylistName+".list"))).readObject();
                }catch(Exception ex){
                    currentPlaylistName = null;
                    currentPlaylist     = null;
                }
            }else{
                currentPlaylistName = null;
                currentPlaylist     = null;
            }
            
            int row = audioTable.rowAtPoint(evt.getPoint());
            if (row >= 0) {
                userControlled = true;
                
                if(pause || isPlaying){
                    stop();
                }
                
                this.currentFileNumber = row;
                
                musicPlay();
                System.out.println("ROW : "+row + " Current Playing Number : "+ currentFileNumber);
                
                Popup();    //Close Popup
                btnListPopup.setIcon(openListIcon);

            }
        }
    }//GEN-LAST:event_audioTableMouseClicked

    private void openFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileActionPerformed
        // TODO add your handling code here:
        currentFileNumber = 0;
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileFilter(new AudioFileFilter());
        int returnVal = jfc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            files = jfc.getSelectedFiles();
            
            reading = true;
            new Thread(new Runnable(){public void run(){
                stop();
                for(int i = 0 ; i < files.length ; i++){
                    addToLibrary(files[i]);
                }

                if(!isPlaying){
                    currentFileNumber   = 0;
                }

                saveListToFile();
                LibraryUpdate();
                reading = false;
            }}).start();
            
        }
    }//GEN-LAST:event_openFileActionPerformed

    private void controlPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_controlPanelMouseExited
        // TODO add your handling code here:
        /*System.out.println(evt.getSource());
        try { Thread.sleep(2000);} catch (InterruptedException ex) {}
        showControl();*/
    }//GEN-LAST:event_controlPanelMouseExited

    private void deControlPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deControlPanelMouseEntered
        // TODO add your handling code here:
        if(!showControl)showControl();
    }//GEN-LAST:event_deControlPanelMouseEntered

    private void checkPanelTopMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkPanelTopMouseEntered
        // TODO add your handling code here:
        if(showControl)showControl();
    }//GEN-LAST:event_checkPanelTopMouseEntered

    private void checkPanelRightMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkPanelRightMouseEntered
        // TODO add your handling code here:
        if(showControl)showControl();
    }//GEN-LAST:event_checkPanelRightMouseEntered

    private void checkPanelLeftMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkPanelLeftMouseEntered
        // TODO add your handling code here:
        if(showControl)showControl();
    }//GEN-LAST:event_checkPanelLeftMouseEntered

    private void checkPanelBottomMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkPanelBottomMouseEntered
        // TODO add your handling code here:
        if(showControl)showControl();
    }//GEN-LAST:event_checkPanelBottomMouseEntered

    private void btnLoopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoopActionPerformed
        // TODO add your handling code here:
        if(!loop && !loopOne){
            loopOne = true;
            btnLoop.setIcon(loopOnceIcon);
        }
        else if(!loop && loopOne){
            loopOne = false;
            loop    = true;
            btnLoop.setIcon(loopAllIcon);
        }else{
            loop    = false;
            loopOne = false;
            btnLoop.setIcon(loopIcon);
        }
        
        System.out.println("Loop : "+loop +" / LoopOne : "+loopOne);
    }//GEN-LAST:event_btnLoopActionPerformed

    private void PlaylistComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlaylistComboBoxActionPerformed
        // TODO add your handling code here:
        String tempListName = (String)PlaylistComboBox.getSelectedItem();
        
        if(tempListName.equals("#All Songs")){
            presetPlaylistName = null;
            this.LibraryUpdate();
        }else if(!tempListName.equals("")){
            presetPlaylistName = tempListName;
            tm = new AudioListModel();
            audioTable.setModel(tm);
            audioTable.setRowHeight(30);
            audioTable.setTableHeader(null);
            audioTable.setSelectionBackground(Color.PINK);
            audioTable.getColumn("Column 1").setCellRenderer(new AudioListRenderer());
            audioTable.getColumn("Column 1").setMaxWidth(19);
            audioTable.getColumn("Column 1").setResizable(false);
            audioTable.getColumn("Column 2").setCellRenderer(new AudioListRenderer());
            audioTable.getColumn("Column 2").setMaxWidth(32);
            audioTable.getColumn("Column 2").setResizable(false);
            audioTable.getColumn("Column 3").setCellRenderer(new AudioListRenderer());
            audioTable.setBackground(new Color(255,255,255));

            try {
                if(onlineMode)if(!mediaCon.isCentralConnected())mediaCon.ConnectCentral();
                ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(new File(userPlaylistPath+tempListName+".list")));
                final LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();

                for(int i = 0 ; i < tempList.size() ; i++){
                    try{
                        if(AudioList.get(tempList.get(i)).getType() == OFFLINE_MODE){
                            AudioTAG tag        = new AudioTAG(AudioList.get(tempList.get(i)).getPath());
                            String tagTitle     = tag.getTagTitle();
                            String tagArtist    = tag.getTagArtist();
                            if(tagArtist == null)tagArtist = "";

                            if(AudioList.get(tempList.get(i)).getImagePath() != null){
                                if(!new File(AudioList.get(tempList.get(i)).getImagePath()).exists()){
                                    AudioList.get(tempList.get(i)).setImagePath(
                                        new AudioImage().ImageIconToFile(new AudioTAG(AudioList.get(tempList.get(i)).getPath()).getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH),"png",".\\cache\\")
                                    );
                                }

                                tm.addRow(new Object[]{
                                    " ",
                                    new ImageIcon(AudioList.get(tempList.get(i)).getImagePath()),
                                    new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+tagTitle+"</font></b><BR><font size=\"1\">"+tagArtist+"</font></div></HTML>")
                                    });
                            }else
                                tm.addRow(new Object[]{
                                    " ",
                                    listCover,
                                    new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+tagTitle+"</font></b><BR><font size=\"1\">"+tagArtist+"</font></div></HTML>")
                                    });
                        }else{
                            try {
                                if(onlineMode){
                                    AudioDetails au = (AudioDetails)mediaCon.GetAudioDetails(AudioList.get(tempList.get(i)).getNetID());
                                    if(au.containCover()){
                                        tm.addRow(new Object[]{
                                            "N",
                                            new ImageIcon(au.getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH)),
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+au.getTagTitle()+"</font></b><BR><font size=\"1\">"+au.getTagArtist()+"</font></div></HTML>")
                                            });
                                    }else
                                        tm.addRow(new Object[]{
                                            "N",
                                            listCover,
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+au.getTagTitle()+"</font></b><BR><font size=\"1\">"+au.getTagArtist()+"</font></div></HTML>")
                                            });

                                    downList.add(au.getSongID());
                                }else
                                    tm.addRow(new Object[]{
                                        "N",
                                        listCover,
                                        new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">Network files</font></b><BR><font size=\"1\">No connections</font></div></HTML>")
                                    });
                            }catch(Exception ex){
                                ex.printStackTrace();
                                NETWORK_ERROR = true;
                            }
                        }
                    } catch( AudioIDInvalidException e){}
                }
                if(onlineMode)mediaCon.DisconnectCentral();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch( NullPointerException ex){
                ex.printStackTrace();
                AudioList.clear();
            } catch( ClassNotFoundException ex){
                /*
                 * DO Nothing
                 */
            }
        }else{
            presetPlaylistName = null;
        }
    }//GEN-LAST:event_PlaylistComboBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(FYPAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FYPAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FYPAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FYPAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FYPAudioPlayer().setVisible(true);
            }
            
        });
   
    }
    
    public void drop(DropTargetDropEvent dropTarget){
        Transferable tr = dropTarget.getTransferable();
        dropTarget.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        try {
            List<File> droppedFiles = (List<File>)tr.getTransferData(DataFlavor.javaFileListFlavor);
            
            files = (File[])droppedFiles.toArray();
            
            reading = true;
            new Thread(new Runnable(){public void run(){
                if(isPlaying)stop();
                for(int i=0;i<files.length;i++){
                    System.out.println("Drop Files : "+files[i]);

                    boolean check = false;
                    for(int j = 0 ; j < MSWPlayer.SUPPORTED_FILES.length; j++){
                        if(files[i].getName().endsWith(MSWPlayer.SUPPORTED_FILES[j])){
                            check = true;
                            break;
                        }
                    }
                    if(check){
                        addToLibrary(files[i]);
                    }
                }

                if(!isPlaying){
                    currentFileNumber   = 0;
                    for(int i = 0 ; i < AudioList.size() ; i++){
                        if(files[files.length-1].getAbsolutePath().equals(AudioList.get(i).getPath())){
                            currentFileNumber   = i;break;
                        }
                    }
                    musicPlay();
                }

                saveListToFile();
                reading = false;
            }}).start();
        } catch (Exception ex) {ex.printStackTrace();}
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde) {
        //Drop Action Changed
    }
    
    public void dragEnter(DropTargetDragEvent dtde) {
        //Object drag in the target
    }

    public void dragExit(DropTargetEvent dte) {
        //Object drag out the target
    }
    
    public void dragOver(DropTargetDragEvent dtde) {
        //Object drag over the target
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
            
            if(AudioList.size() == 0){
                check = true;
            }
            
            if(check){
                if(tempElement.getType() == OFFLINE_MODE){
                    if(tempElement.getPath() != null){
                        try{
                            tempElement.setImagePath(new AudioImage().ImageIconToFile(new AudioTAG(tempElement.getPath()).getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH),"png",".\\cache\\"));
                        }catch(Exception e){
                            /*DO NOTHING*/
                        }
                    }
                    AudioList.add(tempElement);

                    AudioTAG tag        = new AudioTAG(AudioList.get(AudioList.size()-1).getPath());
                    String tagTitle     = tag.getTagTitle();
                    String tagArtist    = tag.getTagArtist();
                    if(tagArtist == null)tagArtist = "";

                    if(AudioList.get(AudioList.size()-1).getImagePath() != null){
                        tm.addRow(new Object[]{
                            " ",
                            new ImageIcon(AudioList.get(AudioList.size()-1).getImagePath()),
                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+tagTitle+"</font></b><BR><font size=\"1\">"+tagArtist+"</font></div></HTML>")
                            });
                    }else
                        tm.addRow(new Object[]{
                            " ",
                            listCover,
                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+tagTitle+"</font></b><BR><font size=\"1\">"+tagArtist+"</font></div></HTML>")
                            });
                }else{
                    try {
                        if(onlineMode){
                            AudioList.add(tempElement);
                            
                            AudioDetails au = (AudioDetails)mediaCon.GetAudioDetails(tempElement.getNetID());
                            if(au.containCover()){
                                tm.addRow(new Object[]{
                                    "N",
                                    new ImageIcon(au.getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH)),
                                    new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+au.getTagTitle()+"</font></b><BR><font size=\"1\">"+au.getTagArtist()+"</font></div></HTML>")
                                    });
                            }else
                                tm.addRow(new Object[]{
                                    "N",
                                    listCover,
                                    new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+au.getTagTitle()+"</font></b><BR><font size=\"1\">"+au.getTagArtist()+"</font></div></HTML>")
                                    });
                            
                            /*
                             * Network downloading queue
                             */
                            downList.add(au.getSongID());
                            synchronized(downloadLock){
                                downloadLock.notifyAll();
                            }
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                        NETWORK_ERROR = true;
                    }
                }
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch( AudioIDInvalidException e){}
    }
    
    public void LibraryUpdate(){
        tm = new AudioListModel();
        audioTable.setModel(tm);
        audioTable.setRowHeight(30);
        audioTable.setTableHeader(null);
        audioTable.setSelectionBackground(Color.PINK);
        audioTable.getColumn("Column 1").setCellRenderer(new AudioListRenderer());
        audioTable.getColumn("Column 1").setMaxWidth(19);
        audioTable.getColumn("Column 1").setResizable(false);
        audioTable.getColumn("Column 2").setCellRenderer(new AudioListRenderer());
        audioTable.getColumn("Column 2").setMaxWidth(32);
        audioTable.getColumn("Column 2").setResizable(false);
        audioTable.getColumn("Column 3").setCellRenderer(new AudioListRenderer());
        audioTable.setBackground(new Color(255,255,255));
        setVisible(true);
        
        try {
            //audioTable.setDefaultRenderer(ImageIcon.class,new AudioListRenderer());
            
            for(int i = 0 ; i < AudioList.size() ; i++){
                try{
                    if(AudioList.get(i).getType() == OFFLINE_MODE){
                        AudioTAG tag        = new AudioTAG(AudioList.get(i).getPath());
                        String tagTitle     = tag.getTagTitle();
                        String tagArtist    = tag.getTagArtist();
                        if(tagArtist == null)tagArtist = "";

                        if(AudioList.get(i).getImagePath() != null){
                            if(!new File(AudioList.get(i).getImagePath()).exists()){
                                AudioList.get(i).setImagePath(
                                    new AudioImage().ImageIconToFile(new AudioTAG(AudioList.get(i).getPath()).getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH),"png",".\\cache\\")
                                );
                            }

                            tm.addRow(new Object[]{
                                " ",
                                new ImageIcon(AudioList.get(i).getImagePath()),
                                new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+tagTitle+"</font></b><BR><font size=\"1\">"+tagArtist+"</font></div></HTML>")
                                });
                        }else
                            tm.addRow(new Object[]{
                                " ",
                                listCover,
                                new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+tagTitle+"</font></b><BR><font size=\"1\">"+tagArtist+"</font></div></HTML>")
                                });
                    }else{
                        try {
                            if(onlineMode){
                                AudioDetails au = (AudioDetails)mediaCon.GetAudioDetails(AudioList.get(i).getNetID());
                                if(au.containCover()){
                                    tm.addRow(new Object[]{
                                        "N",
                                        new ImageIcon(au.getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH)),
                                        new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+au.getTagTitle()+"</font></b><BR><font size=\"1\">"+au.getTagArtist()+"</font></div></HTML>")
                                        });
                                }else
                                    tm.addRow(new Object[]{
                                        "N",
                                        listCover,
                                        new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">"+au.getTagTitle()+"</font></b><BR><font size=\"1\">"+au.getTagArtist()+"</font></div></HTML>")
                                        });
                                
                                downList.add(au.getSongID());
                            }else
                                tm.addRow(new Object[]{
                                    "N",
                                    listCover,
                                    new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"2\">Network files</font></b><BR><font size=\"1\">No connections</font></div></HTML>")
                                });
                        }catch(Exception ex){
                            NETWORK_ERROR = true;
                        }
                    }
                } catch( AudioIDInvalidException e){}
            }
            
            saveListToFile();   //Save To File
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch( NullPointerException ex){
            ex.printStackTrace();
            AudioList.clear();
        }
    }
    
    public void setPlaying(int ListNum){
        if(presetPlaylistName == null){
            try{
                if(previousPlay != -1){
                    tm.setValueAt(" ", previousPlay, 0);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            tm.setValueAt(">", ListNum, 0);
            previousPlay = ListNum;
        }
        
        new Thread(new Runnable(){public void run(){
            try {
                if(currentPlaylistName == null)
                    playingTitle = new AudioTAG(AudioList.get(currentFileNumber).getPath()).getTagTitle();
                else
                    playingTitle = new AudioTAG(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath()).getTagTitle();
                
                if(playingTitle.length() > 40)playingTitle = playingTitle.substring(0,36) + "...";
            } catch (Exception ex) {}
        }}).start();
    }
    
    
    public void Popup(){
        if(!isPopup){
            isPopup = true;
            new Thread(new Runnable(){public void run(){
                audioListPanel.setVisible(true);
                for(int i = 280 ; i >= 0 ; i-= 10){
                    audioListPanel.setLocation(0, i);
                    audioListPanel.setSize(220, 280 - i);
                    try {Thread.sleep(5);} catch (InterruptedException ex) {}
                }
            }}).start();
        }else{
            isPopup = false;
            new Thread(new Runnable(){public void run(){
                for(int i = 0 ; i <= 280 ; i+= 10){
                    audioListPanel.setLocation(0, i);
                    audioListPanel.setSize(220, 280 - i);
                    try {Thread.sleep(5);} catch (InterruptedException ex) {}
                }
                audioListPanel.setVisible(false);
            }}).start();
        }
    }
    
    
    private void stop(){
        if(!stop && !FYPAClosed){
            stop        = true;
            try{player.close();}catch(Exception e){e.printStackTrace();}
            System.out.println("Player stopped by user");

            player      = null;
            playBack    = null;
            isPlaying    = false;
            currentFileNumber--;
            btnPlay.setIcon(playIcon);
            
            if(MusicDetails.isPopup){
                MusicDetails.Popup(false);
            }
        }
    }
    
    private void showControl(){
        if(!showControl){
            showControl = true;
            
            new Thread(new Runnable(){public void run(){
                deControlPanel.setVisible(false);
                durationText.setSize(200, 20);
                for(int i = 20 ; i <= 240 ; i+= 20){
                    durationText.setLocation(i, 280);
                    try {Thread.sleep(5);} catch (InterruptedException ex) {}
                }
                deControlPanel.setVisible(false);
                controlPanel.setVisible(true);
            }}).start();
        }else{
            showControl = false;
            new Thread(new Runnable(){public void run(){
                controlPanel.setVisible(false);
                durationText.setSize(400, 20);
                for(int i = 240 ; i >= 20 ; i-= 20){
                    durationText.setLocation(i, 280);
                    try {Thread.sleep(5);} catch (InterruptedException ex) {}
                }
                controlPanel.setVisible(false);
                deControlPanel.setVisible(true);
            }}).start();
        }
            
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox PlaylistComboBox;
    private javax.swing.JButton PopupDetails;
    private javax.swing.JPanel audioListPanel;
    private javax.swing.JScrollPane audioScrollPane;
    private javax.swing.JTable audioTable;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnListPopup;
    private javax.swing.JButton btnLoop;
    private javax.swing.JButton btnMax;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPlay;
    private javax.swing.JPanel checkPanelBottom;
    private javax.swing.JPanel checkPanelLeft;
    private javax.swing.JPanel checkPanelRight;
    private javax.swing.JPanel checkPanelTop;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel deControlPanel;
    private javax.swing.JButton detailsExit;
    private javax.swing.JLabel durationText;
    private javax.swing.JLabel musicListBackground;
    private javax.swing.JButton openFile;
    private javax.swing.JSlider volumeSilder;
    // End of variables declaration//GEN-END:variables
}


class AudioFileFilter extends javax.swing.filechooser.FileFilter  
{  
     public boolean accept(File file)  
     {  
          //Convert to lower case before checking extension  
         return (file.getName().toLowerCase().endsWith(".mp3") 
                 || file.isDirectory()
                 ||file.getName().toLowerCase().endsWith(".wav")
                 );
    }  

    public String getDescription()  
    {  
        return "Audio Files (*.wav/*.mp3)";  
    }  
}


/*
 * AudioListModel
 */
class AudioListModel extends javax.swing.table.DefaultTableModel{

    Object[][] row = {};

    Object[] col = {"Column 1", "Column 2","Column 3"};

    public AudioListModel (){

	//Adding columns
        for(Object c: col)
            this.addColumn(c);

	//Adding rows
        for(Object[] r: row)
            addRow(r);

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex == 0)return getValueAt(0, columnIndex).getClass();

        else return super.getColumnClass(columnIndex);

    }
    
    public boolean isCellEditable(int row, int col){ return false; }
}



/*
 * AudioListRenderer
 */
class AudioListRenderer extends DefaultTableCellRenderer{
    
    public void fillLabelColor(JTable t,JLabel l,boolean isSelected ){
        //setting the background and foreground when JLabel is selected
        l.setOpaque(true);
        if(isSelected){
            l.setBackground(t.getSelectionBackground());
            l.setForeground(t.getSelectionForeground());
        }

        else{
            l.setBackground(t.getBackground());
            l.setForeground(t.getForeground());
        }

    }
    
     public void fillColor(JTable t,boolean isSelected ){
        //setting the background and foreground when JLabel is selected
        if(isSelected){
            this.setBackground(t.getSelectionBackground());
            this.setForeground(t.getSelectionForeground());
        }

        else{
            this.setBackground(t.getBackground());
            this.setForeground(t.getForeground());
        }

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,boolean hasFocus, int row, int column)
    {

        if(value instanceof JLabel){
           //This time return only the JLabel without icon
            JLabel lbl = (JLabel)value;
            fillLabelColor(table,lbl,isSelected);
            return lbl;
        }
        
        else if(value instanceof ImageIcon){
            this.setIcon((ImageIcon)value);
            fillColor(table,isSelected);
            return this;
        }
        

        else
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    }
}
