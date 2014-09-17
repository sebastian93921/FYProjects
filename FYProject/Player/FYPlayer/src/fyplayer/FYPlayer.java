/*
 * Final Year Project Audio/Music Player (Extended Functions)
 * Rebuild at 06/01/2013
 * 
 * ADD: Artist tab to player - 09/01/2013
 * ADD: Album tab to player - 23/01/2013
 * ADD: Download uploaded information from server - 13/02/2013
 * Debug: Song not playing properly - 20/02/2013
 * ADD: Album animation button to player (Rebuild) - 10/03/2013
 * ADD: Background processing from loading tabs - 12/03/2013
 * Fix: Graphic problem in poping out the loading frame - 12/03/2013
 * ADD: Loading processing - 19/03/2013
 * ADD: Download queue processing - 19/03/2013
 * ADD: Playlist(developing) - 20/03/2013
 * ADD: Playlist & Table moving & Playlist queuing - 22/03/2013
 * Fix: Playlist are playing in wrong order - 22/03/2013
 * Debug: Play button was not in same position - 23/04/2013
 * ADD: Upload functions, active from (Upload) button - 01/05/2013
 * ADD: Register functions to music player - 16/05/2013
 * Fix: Registing in wrong ID - 17/05/2013
 * ADD: Normal Audio List playing - 17/05/2013
 * Fix: Audio uploading fail issue - 17/05/2013
 * Fix: Audio Table playing in wrong position - 25/05/2013
 * ADD: New setting icon - 25/05/2013
 * Debug: downloading queue was auto locking - 26/05/2013
 * Change: background of Current Details - 05/06/2013
 * Debug: Major Debugging - 11/06/2013
 * Debug: Playing in multiple thread - 11/06/2013
 * 
 * 
 * Sebastian Ko
 * 06/01/2013
 */

package fyplayer;


/*
 * FYPlayer.java
 *
 * Created on 2012年11月3日, 下午08:05:56
 */

/**
 *
 * @author user
 */
import ClientNet.Client;
import ClientNet.ConnectionFailException;
import MSWPlayer.MSWPlayer;
import com.sun.awt.AWTUtilities;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.*;
import mswmusickernel.MSWMusicKernel;
import net.AudioDetails;
import net.AudioElement;
import net.AudioImage;
import tag.codec.AudioTAG;

public class FYPlayer extends MSWMusicKernel {
    public static final String version        = "0.5.0 EMPlayerMainFrame Update 12 - BETA 1";
    private static final String songPath      = ".\\net\\";
    private static final String tempPath      = ".\\tmp\\";
    
    private final int WIDTH         = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int HEIGHT        = Toolkit.getDefaultToolkit().getScreenSize().height;

    private static Thread playBack    = null;
    private static int previousPlay    = -1;

    private int progress            = 0;
    private int loop                = 0;
    private String time             = null;
    private static detail Control     = null;
    private int curprogress         = 0;
    private int cloop               = 0;
    
    private static final ImageIcon detailsCover                = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/default-listCover.png"));
    /*
     * Player
     */
    private static boolean tableListWaiting                     = false;
    private static final ImageIcon btnPlayIcon                = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_Play.png"));
    private static final ImageIcon btnPlayHoverIcon            = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_Play_Hover.png"));
    private static final ImageIcon btnBackIcon                = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_MainBack.png"));
    private static final ImageIcon btnBackHoverIcon            = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_MainBack_Hover.png"));
    private static final ImageIcon btnNextIcon                = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_MainNext.png"));
    private static final ImageIcon btnNextHoverIcon            = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_MainNext_Hover.png"));
    private static final ImageIcon btnPauseIcon               = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_Pause.png"));
    private static final ImageIcon btnPauseHoverIcon           = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_Pause_Hover.png"));
    private static final ImageIcon btnRandomIcon             = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/Random.png"));  
    private static final ImageIcon btnRandomHoverIcon         = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/Random_Hover.png"));
    private static final ImageIcon btnNotRandomIcon           = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/NotRandom.png"));  
    private static final ImageIcon btnNotRandomHoverIcon       = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/NotRandom_Hover.png"));
    //loop button
    private static final ImageIcon btnLoopIcon                = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_loop.png"));
    private static final ImageIcon btnLoopHoverIcon            = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_loop_Hover.png"));
    private static final ImageIcon btnLoopOnceIcon            = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_loop_Once.png"));
    private static final ImageIcon btnLoopOnceHoverIcon        = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_loop_Once_Hover.png"));
    private static final ImageIcon btnLoopAllIcon              = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_loop_All.png"));
    private static final ImageIcon btnLoopAllHoverIcon          = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/btn_loop_All_Hover.png"));
    
    
    private static PlayerSetting ps = null;
    
    /*
     * Cursor
     */
    private Point cursor                    = null;
    private boolean statusArtistTabCheck    = false;
    private boolean statusAlbumTabCheck     = false;
    
    /*
     * Artist Tab
     */
    private static boolean artistTableWaiting              = false;
    
    /*
     * Album Tab
     */
    private static boolean albumTableWaiting             = false;
    private static boolean albumNoWaiting               = false;
    private static boolean albumAudioWaiting            = false;
    
    /*
     * Gobal Tab
     */
    private static final ImageIcon tab1bg              = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/Tab1.png"));
    private static final ImageIcon tab2bg              = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/Tab2.png"));
    private static final ImageIcon tab3bg              = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/Tab3.png"));
    private static final ImageIcon tab4bg              = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/Tab4.png"));
    private static final ImageIcon defaultCover          = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/default-cover.png"));
    private static final ImageIcon smallCover           = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/small-cover.png"));
    private static loadingFrame loadingFrame            = null;
    
    /*
     * Network
     */
    private Client connection                           = null;
    private boolean NETWORK_ERROR                       = false;
    private static final char ONLINE_MODE              = 'N';
    private static final char OFFLINE_MODE             = 'O';
    private static boolean downloadServicesStart              = false;
    private static boolean downloading                     = false;
    /**Uploading**/
    private static UploadAudioFrame uploadFrame            = null;
    
    private LinkedList<AudioDetails> UploadList = null;
    private LinkedList<String> ArtistList       = null;
    private LinkedList<String> AlbumList        = null;
    
    
    /*
     * Register
     */
    private static RegForm rg                  = null;
    
    
    /*
     * PlayList
     */
    private boolean PlayListTableWaiting         = false;
    private boolean PlayListShowed               = false;
    private File playlistFiles[]                 = null;
    private static final ImageIcon currentPlaylistIcon = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/playlistbg.png"));
    private static final ImageIcon defaultPlaylistBG  = new ImageIcon(FYPlayer.class.getResource("/defaultSkins/defaultPlaylistbg.png"));
    
    private DefaultTableModel tableModel = new DefaultListModel();
    
    /** Creates new form FYPlayer */
    public FYPlayer() {
        FYPclosed          = false;
        initComponents();
        
        /*/SE 1.7
        this.setOpacity(0.9f);
        this.setBackground(new Color(255, 255, 255, 0));
        /**/
        
        //SE 1.6 u 10
        AWTUtilities.setWindowOpaque(this, false);
        /**/

        
        /*===================================Layout Design===================================*/
        try{this.setIconImage(new ImageIcon(FYPlayer.class.getResource("/defaultSkins/frameIcon.png")).getImage());}catch(Exception e){}
        
        
        PlayListSPTitle.setText("<html><div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#FF005A\">PlayList</font></b></div><hr width=160 size=1 Noshade align=left ></html>");
        PlayListSPTitle.setBounds(2, 0, 160, 40);
        ArtistLeftPanelTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#FF005A\">Artist</font></b></div><hr width=160 size=1 Noshade align=left ></html>");
        AlbumLeftPanelTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#FF005A\">Album</font></b></div><hr width=160 size=1 Noshade align=left ></html>");
        UploadLeftTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#FF005A\">Uploaded Audio</font></b></div><hr width=160 size=1 Noshade align=left ></html>");
        LoginTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"6\" color=\"#FF005A\">Server Login</font></b></div><hr width=480 size=1 Noshade align=left ></html>");
        AddAudioTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"6\" color=\"#FF005A\">Ooooops!!</font></b></div>"
                + "<div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#FF005A\">You haven't import any audio into the player</font></b></div>"
                + "<div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#FF005A\">Import audio by clicking open icon</font></b></div>"
                + "</html>"
        );
        loginDec.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#FF005A\">We have provide music upload functions</font></b></div></html>");
        loginDec2.setText("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#5E5E5E\">And search music from others user uploaded</font></b></div></html>");
        loginDec3.setText("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#5E5E5E\">By clicking this Login button ↓</font></b></div></html>");
        UploadedDec.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#FF005A\">Search Name, Artist</font></b></div>"
                +"<div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#FF005A\">and Album from </font></b></div>"
                + "<div style=\"white-space: nowrap;\"><b><font size=\"3\" color=\"#FF005A\">different users</font></b></div>"
                + "</html>");
        uploadTextAlbum.setText("<html><div style=\"white-space: nowrap;\"><b><font size=\"5\" color=\"#000000\">Uploaded Album</font></b></div></html>");
        
        durationProcessBar.setUI(new PlayingProgressBarUI());
        durationProcessBar.setForeground(new Color(255,0,90));
        durationProcessBar.setBackground(new Color(255,255,255));
        
        logPassword.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    btnLoginActionPerformed(null);
                }
            }
        });
        /*===================================Layout Design===================================*/
        
        /*
         * Tab Panel
         */
        UploadedLeftPanel.setVisible(false);
        ArtistLeftPanel.setVisible(false);
        AlbumLeftPanel.setVisible(false);
        PlayListSelectPanel.setVisible(true);
        TableTabBackground.setIcon(tab1bg);
        
        /*
         * Make Playlist to a glass panel
         */
        setGlassPane(PlayListShowPanel);
        PlayListShowPanel.setOpaque(false);
        PlayListBackGround.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e){
                super.mousePressed(e);
            }
        });
        PlayListShowPanel.setVisible(false);
        
        /*
         * Golbal Panel
         */
        loginPanel.setVisible(false);
        
        loadingFrame = new loadingFrame();
        
        /*
         * Default Table list
         */
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        /*
         * User's Folder Defining
         */
        if(new File(this.userDataPath).exists()){
            if(!new File(this.userDataPath).isDirectory())new File(this.userDataPath).mkdir();
        }else
            new File(this.userDataPath).mkdir();
        
        
        /*
         * Connecting to network
         */
        this.volumeSilder.setValue(defaultVolume);
        Control = new detail();
        this.AudioTabbedPane.setSize(590, 400);
        
        if(connection == null && onlineMode){
            connection = new Client(CentralServerIP,17220);

            if(connection.ConnectCentral() == 0){
                this.controlMsg.setText("Connection Start");

                if(readLoginFile() == 0){

                    if(connection.Login(super.getUserName(),super.getUserPass()) == 0){
                        this.controlMsg.setText("Login Success");
                        this.welcomeLabel.setText("Welcome ! "+super.getUserName());
                        
                        /*
                         * Add Queue
                         */
                        if(downList == null){
                            downList = new LinkedList<Integer>();
                        }
                    }else {
                        this.controlMsg.setText("Login Fail");
                        this.welcomeLabel.setText("Click \"Login\" to Login");
                        onlineMode = false;
                    }
                }else{
                    this.controlMsg.setText("Fail to read User files (E:00)"); //Error code 00
                    onlineMode = false;
                }

            }else{
                onlineMode      = false;
                NETWORK_ERROR = true;
                connection = null;
                this.welcomeLabel.setText("No connections");
                this.controlMsg.setText("Connections Fail");
            }
            
        }
        
        
        /*
         * Iniliatize AudioList
         */
        if(AudioList == null){
            System.out.println("Iniliatize AudioList");
            
            if(AudioLibrary.exists() && AudioList == null){
                try{
                    ObjectInputStream audioListFile = new ObjectInputStream(new FileInputStream(AudioLibrary));
                    AudioList                  = (LinkedList<AudioElement>)audioListFile.readObject();
                    System.out.println("Audio Library Loaded");
                    currentFileNumber          = 0;
                    audioListFile.close();
                    
                    tableModel = new DefaultListModel();
                    this.tabList.setModel(tableModel);
                    for(int i = 0 ; i < AudioList.size(); i++){
                        try {
                            if(AudioList.get(i).getType() == OFFLINE_MODE){
                                AudioTAG playAudioTAG = new AudioTAG(AudioList.get(i).getPath());
                                this.tableModel.addRow(new Object[]{playAudioTAG.getTagTitle(),new durationToString(AudioList.get(i).getDuration()).toString(),playAudioTAG.getTagArtist(),playAudioTAG.getTagAlbum()});
                            }else
                                if(onlineMode){
                                    AudioDetails au = (AudioDetails)connection.GetAudioDetails(AudioList.get(i).getNetID());

                                    this.tableModel.addRow(new Object[]{au.getTagTitle(),new durationToString(au.getDurationInSec()).toString(),au.getTagArtist(),au.getTagAlbum()});

                                    downList.add(au.getSongID());
                                }else{
                                    tableModel.addRow(new Object[]{"Network files","-","-","-"});
                                }
                                    
                        } catch (Exception ex) {}
                    }

                    /*
                     * Read specification from audio
                     * 
                     * Sebastian Ko 05/03/2013
                     */
                    anlysisAudioDetails();
                }catch(Exception e){
                    AudioList                  = new LinkedList<AudioElement>();
                }
            }else
                AudioList = new LinkedList<AudioElement>();
            
        }else{
            for(int i = 0 ; i < AudioList.size(); i++){
                try {
                    AudioTAG playAudioTAG = new AudioTAG(AudioList.get(i).getPath());
                    this.tableModel.addRow(new Object[]{playAudioTAG.getTagTitle(),new durationToString(AudioList.get(i).getDuration()).toString(),playAudioTAG.getTagArtist(),playAudioTAG.getTagAlbum()});

                } catch (Exception ex) {}
            }
            anlysisAudioDetails();
        }
        
        /*
         * Playlist
         */
        if(!new File(userPlaylistPath).exists() || !new File(userPlaylistPath).isDirectory())new File(userPlaylistPath).mkdir();
        printPlaylistButton();
        
        
        /*
         * Audio List
         */
        tabList.addMouseListener(new MouseAdapter() {
            private void showPopup(MouseEvent evt){
                if(evt.isPopupTrigger()){
                    tabList.setRowSelectionInterval(tabList.rowAtPoint(evt.getPoint()),tabList.rowAtPoint(evt.getPoint()));
                    final Point p       = evt.getPoint();
                    final int tempRow   = tabList.rowAtPoint(p);
                    /*
                     * Popup menu
                     */
                    JPopupMenu tablePopup  = new JPopupMenu();

                    JMenuItem audioTitle = new JMenuItem("-"+(String)tableModel.getValueAt(tempRow,0)+"-");
                    audioTitle.setForeground(Color.MAGENTA);
                    audioTitle.setEnabled(false);
                    tablePopup.add(audioTitle);

                    for(int i = 0 ; i < playlistFiles.length ; i ++){
                        if(playlistFiles[i].getName().endsWith(".list")){
                            final int tempListNum = i;

                            JMenuItem addPlaylist      = new JMenuItem("Add to ["+playlistFiles[i].getName().replaceAll(".list", "")+"]");
                            addPlaylist.addMouseListener(new MouseAdapter(){
                                public void mousePressed(MouseEvent evt){
                                    /*
                                     * ADD to list
                                     */
                                    try{
                                        ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(playlistFiles[tempListNum]));
                                        LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();
                                        tempListRead.close();

                                        /**ADD**/
                                        tempList.add(tempRow);

                                        ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(playlistFiles[tempListNum],false));
                                        playlistRegen.writeObject(tempList);
                                        playlistRegen.close();

                                        if(currentPlaylistName != null && currentPlaylistName.equals(playlistFiles[tempListNum].getName().replaceAll(".list", ""))){
                                            currentPlaylist.add(tempRow);
                                        }

                                        updatePlaylistStatus(playlistFiles[tempListNum].getName(),(String)tableModel.getValueAt(tempRow,0));
                                    }catch(Exception e){
                                        /*
                                         * Error
                                         */
                                        e.printStackTrace();
                                    }
                                }
                            });
                            tablePopup.add(addPlaylist);
                        }
                    }

                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt){
                showPopup(evt);
            }

            public void mouseClicked(MouseEvent evt) {
                if(!tableListWaiting && evt.getClickCount() == 2){
                    tableListWaiting = true;
                    try{Thread.sleep(500);}catch(Exception e){}

                    synchronized(this){
                        currentPlaylistName = null;
                        currentPlaylist     = null;

                        if(isPlaying)stop();
                        currentFileNumber = tabList.rowAtPoint(evt.getPoint());
                        
                        musicPlay();

                        /*Delay clicking time*/
                        try{Thread.sleep(1000);}catch(Exception e){}
                        tableListWaiting = false;
                    }
                }
            }
        });
        
        new Thread(new Runnable(){public void run(){
            while(true){
                synchronized(this){
                    try{
                        wait(1000);
                        if(isPlaying){
                            
                            durationProcessBar.setValue((int)(player.getCurrentPosInSec()*100/player.getDurationInSec()));
                            curprogress=(int)(player.getCurrentPosInSec()*100/player.getDurationInSec());
                            
                            playingTime.setText(new durationToString(player.getCurrentPosInSec()).toString());
                            leftTime.setText("-"+new durationToString(player.getDurationInSec()-player.getCurrentPosInSec()).toString());
                            totalTime.setText(new durationToString(player.getDurationInSec()).toString());
                            durationText.setText(playingTitle);
                            
                        }else
                            durationText.setText("Stop");
                        
                        if(closeTime > 0){
                            String closeMin = closeTime/2/60 >= 10?closeTime/2/60+"":"0"+closeTime/2/60;
                            String closeSec = closeTime/2%60 >= 10?closeTime/2%60+"":"0"+closeTime/2%60;
                            lblClose.setText(closeMin+":"+closeSec+" Min(s) remaining");

                        }else
                            lblClose.setText("");
                        
                        if(!onlineMode && !NETWORK_ERROR){
                            welcomeLabel.setText("Not yet Login");
                        }
                        
                    }catch(Exception ex){
                        durationText.setText("Stop");
                    }
                }
            }
        }}).start();
        
        /*
         * Set cover while audio is playing from other player
         */
        if(isPlaying)
            new Thread(new Runnable(){public void run(){
                String playAudioPath = null;

                if(currentPlaylistName == null){
                    if(AudioList.get(currentFileNumber).getType() == 'N'){  //Type 'N' Network
                        playAudioPath = songPath + AudioList.get(currentFileNumber).getPath().replaceAll("MSP:","");
                    }else   //Type 'O' Offline
                        playAudioPath = AudioList.get(currentFileNumber).getPath();
                }else{
                    if(AudioList.get(currentPlaylist.get(currentFileNumber)).getType() == 'N'){  //Type 'N' Network
                        playAudioPath = songPath + AudioList.get(currentPlaylist.get(currentFileNumber)).getPath().replaceAll("MSP:","");
                    }else   //Type 'O' Offline
                        playAudioPath = AudioList.get(currentPlaylist.get(currentFileNumber)).getPath();

                }

                /*
                 * Read Cover from Audio
                 */
                Image tempCover = null;
                try{
                    tempCover = new AudioTAG(playAudioPath).getTagCover();
                }catch(Exception e){
                    e.printStackTrace();
                }
                if(tempCover != null){
                    tempCover = tempCover.getScaledInstance(40,40, Image.SCALE_SMOOTH);
                    ImageIcon coverIcon = new ImageIcon(tempCover);
                    btnDetail.setIcon(coverIcon);
                }else
                    btnDetail.setIcon(detailsCover);

            }}).start();
        
        audioDownloadingQueue();
        if(AudioList.size() == 0)AudioTabbedPane.setSelectedIndex(5);
    }
    
    
    public void printPlaylistButton(){
        new Thread(new Runnable(){public void run(){
            playlistFiles = new File(userPlaylistPath).listFiles();
            PlayListSelectSP.getViewport().setBackground(new Color(255,255,255,90));

            
            int totalListHeight = 0;
            PlayListSelectContainer.removeAll();
            PlayListSelectContainer.setOpaque(false);
            
            
            if(playlistFiles.length != 0){

                for(int i = 0 ; i < playlistFiles.length; i++){
                    if(playlistFiles[i].getName().endsWith(".list")){
                        final String tempListName = playlistFiles[i].getName();
                        totalListHeight += 2;
                        
                        final JButton playlistDelete = new JButton("x");
                        playlistDelete.setBorder(null);
                        playlistDelete.setContentAreaFilled(false);
                        PlayListSelectContainer.add(playlistDelete);
                        playlistDelete.setBounds(130, totalListHeight, 16, 25);
                        playlistDelete.setFont(new java.awt.Font("Tahoma", 1, 12));
                        playlistDelete.setOpaque(true);
                        playlistDelete.setForeground(Color.WHITE);
                        playlistDelete.setBackground(Color.WHITE);

                        final JButton playlistButton = new JButton(tempListName.replaceAll(".list",""));
                        playlistButton.setHorizontalAlignment(SwingConstants.LEFT);
                        PlayListSelectContainer.add(playlistButton);
                        playlistButton.setBounds(5, totalListHeight, 125, 25);
                        playlistButton.setContentAreaFilled(false);
                        playlistButton.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
                        playlistButton.setOpaque(true);
                        playlistButton.setBackground(Color.WHITE);
                        totalListHeight += 25;
                        
                        playlistDelete.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent evt){
                                playlistDelete.setForeground(Color.BLACK);
                            }
                            
                            @Override
                            public void mouseExited(MouseEvent evt){
                                playlistDelete.setForeground(Color.WHITE);
                            }
                            
                            @Override
                            public void mousePressed(MouseEvent evt){
                                playlistDelete.setForeground(Color.LIGHT_GRAY);
                            }
                            
                            @Override
                            public void mouseReleased(MouseEvent evt){
                                playlistDelete.setForeground(Color.BLACK);
                            }
                        });
                        
                        playlistDelete.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try{
                                    if(new File(userPlaylistPath+tempListName).delete()){
                                    
                                        /*
                                         * Delete done, reload list
                                         */
                                        printPlaylistButton();
                                    }else{
                                        /*
                                         * Playlist Delete error E:21
                                         */
                                        new Thread(new Runnable(){public void run(){
                                            new errPopUp("Delete Playlist Fail (E:21)").popup();
                                        }}).start();
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                    new Thread(new Runnable(){public void run(){
                                        new errPopUp("Delete Playlist Fail (E:21)").popup();
                                    }}).start();
                                }
                                
                            }
                        });

                        playlistButton.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent evt){
                                playlistButton.setBackground(Color.LIGHT_GRAY);
                            }
                            
                            @Override
                            public void mouseExited(MouseEvent evt){
                                playlistButton.setBackground(Color.WHITE);
                            }
                            
                            @Override
                            public void mousePressed(MouseEvent evt){
                                playlistButton.setBackground(Color.DARK_GRAY);
                                playlistButton.setForeground(Color.WHITE);
                            }
                            
                            @Override
                            public void mouseReleased(MouseEvent evt){
                                playlistButton.setBackground(Color.LIGHT_GRAY);
                                playlistButton.setForeground(Color.BLACK);
                            }
                        });
        
                        playlistButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if(PlayListShowed)PlayListShowPanel.setVisible(false);
                                PlayListShowed = true;
                                
                                final String playListName = tempListName.replaceAll(".list","");
                                try{
                                    System.out.println(">>>"+tempListName);
                                    ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(new File(userPlaylistPath+tempListName)));
                                    final LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();
                                    tempListRead.close();



                                    PlayListShowPanel.setVisible(true);
                                    PlayListScrollPane.setVisible(false);
                                    PlayListLoading.setVisible(true);
                                    /*
                                     * Playlist panel position
                                     */
                                    PlayListBackGround.setIcon(defaultPlaylistBG);
                                    PlayListLoading.setBounds(242, 250, 50, 50);
                                    PlayListTitle.setBounds(158, 120, 230, 30);
                                    PlayListBackGround.setBounds(150, 100, 320, 390);
                                    PlayListScrollPane.setBounds(156, 160, 234, 322);
                                    /*
                                     * Playlist title
                                     */
                                    PlayListTitle.setText(playListName);
                                    new Thread(new Runnable(){public void run(){
                                        final DefaultTableModel playlistModel = new PlayListModel(tempList,playListName);
                                        PlayListTable = new JTable();
                                        PlayListTable.setModel(playlistModel);
                                        PlayListTable.setRowHeight(20);
                                        PlayListTable.setSelectionBackground(Color.PINK);
                                        PlayListTable.setTableHeader(null);
                                        PlayListTable.getColumn("Playing").setMaxWidth(20);
                                        PlayListTable.getColumn("Duration").setMaxWidth(80);
                                        PlayListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                                        PlayListTable.setDragEnabled(true);
                                        PlayListTable.setDropMode(DropMode.INSERT_ROWS);
                                        PlayListTable.setTransferHandler(new PlaylistTransferHandler(PlayListTable));
                                        PlayListScrollPane.getViewport().setBackground(new Color(255,255,255));
                                        PlayListScrollPane.setViewportView(PlayListTable);


                                        for(int i = 0 ; i < tempList.size() ; i++){
                                            try{
                                                playlistModel.addRow(new Object[]{
                                                    " ",
                                                    tableModel.getValueAt(tempList.get(i),0),
                                                    tableModel.getValueAt(tempList.get(i),1)
                                                });

                                            }catch(Exception e){e.printStackTrace();}
                                        }

                                        PlayListTable.addKeyListener(new KeyAdapter(){
                                            public void keyTyped(KeyEvent ke)
                                            {
                                                switch(ke.getKeyChar())
                                                {
                                                    case '\u007F':
                                                    if(PlayListTable.getSelectedRow() != -1){
                                                        tempList.remove(PlayListTable.getSelectedRow());
                                                        try{
                                                            ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(".\\usr\\playlist\\"+playListName+".list",false));
                                                            playlistRegen.writeObject(tempList);
                                                            playlistRegen.close();
                                                        }catch(Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        playlistModel.removeRow(PlayListTable.getSelectedRow());
                                                    }
                                                    break;
                                                }
                                            }
                                        });

                                        PlayListTable.addMouseListener(new MouseAdapter() {
                                            public void mouseClicked(MouseEvent evt) {
                                                if(!PlayListTableWaiting && evt.getClickCount() == 2){
                                                    PlayListTableWaiting = true;
                                                    try{Thread.sleep(500);}catch(Exception e){}

                                                    synchronized(this){
                                                        if(isPlaying)stop();
                                                        currentPlaylistName     = playListName;
                                                        currentPlaylist         = tempList;
                                                        currentFileNumber      = PlayListTable.rowAtPoint(evt.getPoint());

                                                        PlayListTable.clearSelection();
                                                        musicPlay();

                                                        PlayListShowPanel.setVisible(false);
                                                        PlayListShowed = false;

                                                        /*Delay clicking time*/
                                                        try{Thread.sleep(1000);}catch(Exception e){}
                                                        PlayListTableWaiting = false;

                                                    }
                                                }
                                            }
                                        });
                                        
                                        PlayListBackGround.addMouseListener(new MouseAdapter() {
                                            public void mouseClicked(MouseEvent evt) {
                                                PlayListShowPanel.setVisible(false);
                                                PlayListShowed = false;
                                            }
                                        });
                                        
                                        PlayListLoading.setVisible(false);
                                        PlayListScrollPane.setVisible(true);
                                    }}).start();
                                    System.out.println("End");
                                }catch(Exception e){
                                    /*
                                     * Print Error
                                     */
                                    e.printStackTrace();
                                }
                            }

                        });

                        PlayListSelectContainer.setPreferredSize(new Dimension(170,totalListHeight));
                    }
                }
            }
            
            /*
             * Add new playList button
             */
            final JTextField newListName = new JTextField("New Playlist");
            newListName.setForeground(Color.LIGHT_GRAY);
            PlayListSelectContainer.add(newListName);
            newListName.setBounds(5, totalListHeight+5, 115, 25);

            newListName.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt){
                    newListName.setForeground(Color.BLACK);
                    newListName.setText("");
                }
            });

            JButton btnNewList     = new JButton("+");
            PlayListSelectContainer.add(btnNewList);
            btnNewList.setContentAreaFilled(false);
            btnNewList.setFont(new java.awt.Font("Tahoma", 1, 14));
            btnNewList.setBounds(118, totalListHeight+5, 40, 25);

            totalListHeight += 5 + 25;

            btnNewList.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String tempListName = newListName.getText();
                    newListName.setText("");

                    if(!tempListName.equals("") && !tempListName.equals("#All Songs") && !tempListName.equals("New Playlist")){
                        if(!new File(userPlaylistPath+tempListName+".list").exists()){
                            try{
                                ObjectOutputStream playlistOut = new ObjectOutputStream(new FileOutputStream(new File(userPlaylistPath+tempListName+".list"),false));
                                playlistOut.writeObject(new LinkedList<Integer>());
                                playlistOut.close();

                                System.out.println("New playlist -> "+tempListName);

                                /*
                                 * Create done, reload list
                                 */
                                printPlaylistButton();
                            }catch(Exception e){
                                /*
                                 * Error 02
                                 */
                                e.printStackTrace();
                            }
                        }else{
                            System.err.println("Same playlist.");
                        }
                    }else{
                        System.err.println("The textbar is empty.  / Using system declared words.");
                    }
                }
            });
            
            PlayListSelectContainer.setPreferredSize(new Dimension(170,totalListHeight));
            PlayListSelectContainer.repaint();
        }}).start();
    }

    
    public void audioDownloadingQueue(){
        /*
         * Audio Downloadingaddto
         * Sebastian Ko - 19/03/2013
         */
        new Thread(new Runnable(){public void run(){
            try{
                if(onlineMode && connection.receiveUserID() != null && !downloadServicesStart){
                    downloadServicesStart = true;
                    try{
                        connection.ConnectMedia(connection.ReceiveMediaServer(), connection.receiveUserID());

                        controlMsg.setText("Downloading Audio..");

                        synchronized(downloadLock){
                            while(!NETWORK_ERROR){
                                try{
                                    downloading = true;
                                    new Thread(new Runnable(){public void run(){
                                        try{
                                            Thread.sleep(500);
                                            while(downloading){
                                                Thread.sleep(500);
                                                controlMsg.setText(downList.size()+" Audio Remaining - "+connection.downloadProcess+"%");
                                            }
                                            controlMsg.setText("Download Complete ");
                                        }catch(Exception e){}
                                    }}).start();
                                    
                                    while(downList.size() != 0){
                                        connection.downloadFromMedia(downList.get(0));
                                        downList.remove(0);
                                    }
                                    downloading = false;
                                    anlysisAudioDetails();
                                    downloadLock.wait();
                                }catch(Exception e){
                                    e.printStackTrace();
                                    NETWORK_ERROR = true;
                                }
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        connection.DisconnectMedia();
                        NETWORK_ERROR = true;
                        new errPopUp("Fail to connecting Servers (E:22)").popup();
                    }
                }
            }catch(Exception e){}
            downloadServicesStart = false;
        }}).start();
    }
    
    /*
     * Comfirm all audio information for later researching
     */
    public void anlysisAudioDetails(){
        /*
         * ArtistList
         */
        ArtistList      = new LinkedList<String>();
        String artist   = null;
        
        for(int i = 0 ; i < AudioList.size() ; i++){
            try{
                artist = new AudioTAG(AudioList.get(i).getPath()).getTagArtist();
            }catch(Exception e){}
            
            if(ArtistList.size() == 0 && artist != null)
                ArtistList.add(artist);
            else{
                boolean con = false;
                for(int j = 0 ; j < ArtistList.size() ; j++){
                    if(artist == null || artist.equals(ArtistList.get(j)) ){
                        con = true;
                        break;
                    }
                }
                if(!con)ArtistList.add(artist);
            }
        }
        
        /*
         * - 10/03/2013 - Sebastian Ko -
         * Album
         */
        AlbumList = new LinkedList<String>();
        String album   = null;
        
        for(int i = 0 ; i < AudioList.size() ; i++){
            try{
                album = new AudioTAG(AudioList.get(i).getPath()).getTagAlbum();
            }catch(Exception e){}
            
            if(AlbumList.size() == 0 && artist != null)
                AlbumList.add(album);
            else{
                boolean con = false;
                for(int j = 0 ; j < AlbumList.size() ; j++){
                    if(album == null || album.equals(AlbumList.get(j)) ){
                        con = true;
                        break;
                    }
                }
                if(!con)AlbumList.add(album);
            }
        }
        
        
        
        
        /*
         * Cover
         */
        for(int i = 0 ; i < AudioList.size();i++){
            if(AudioList.get(i).getType() == 'O'){
                if(AudioList.get(i).getPath() != null){
                    try{
                        if(AudioList.get(i).getDefaultImagePath() == null)
                            AudioList.get(i).setDefaultImagePath(new AudioImage().ImageIconToFile(new AudioTAG(AudioList.get(i).getPath()).getTagCover().getScaledInstance(128,128, Image.SCALE_SMOOTH),"png",".\\cache\\"));
                    }catch(Exception e){
                        /*DO NOTHING*/
                    }
                }
            }
        }
        
        
        /*
         * Let method reload
         */
        statusArtistTabCheck = false;
        statusAlbumTabCheck = false;
        
        /*
         * Save audio list to file
         */
        saveListToFile();
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainPanel = new javax.swing.JPanel();
        btnSetting = new javax.swing.JButton();
        PlayListShowPanel = new javax.swing.JPanel();
        PlayListLoading = new javax.swing.JLabel();
        PlayListTitle = new javax.swing.JLabel();
        PlayListScrollPane = new javax.swing.JScrollPane();
        PlayListTable = new javax.swing.JTable();
        PlayListBackGround = new javax.swing.JLabel();
        loginPanel = new javax.swing.JPanel();
        LoginTitle = new javax.swing.JLabel();
        loginDec = new javax.swing.JLabel();
        loginDec2 = new javax.swing.JLabel();
        loginDec3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        logUsername = new javax.swing.JTextField();
        logPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();
        btnReg = new javax.swing.JButton();
        btnCover = new javax.swing.JButton();
        BackGround = new javax.swing.JLabel();
        tabCover = new javax.swing.JButton();
        controlMsg = new javax.swing.JLabel();
        btnPlay = new javax.swing.JButton();
        CurrentPlPanel = new javax.swing.JPanel();
        btnCurrentPlaylist = new javax.swing.JButton();
        uploadButtonPanel = new javax.swing.JPanel();
        btnUpload = new javax.swing.JButton();
        TableTab = new javax.swing.JPanel();
        btnAllSong = new javax.swing.JButton();
        btnArtist = new javax.swing.JButton();
        btnAlbum = new javax.swing.JButton();
        btnUploadedSongs = new javax.swing.JButton();
        TableTabBackground = new javax.swing.JLabel();
        PlayingPanelContainer = new javax.swing.JPanel();
        durationText = new javax.swing.JLabel();
        btnDetail = new javax.swing.JButton();
        durationProcessBar = new javax.swing.JProgressBar();
        playingTime = new javax.swing.JLabel();
        leftTime = new javax.swing.JLabel();
        totalTime = new javax.swing.JLabel();
        lblClose = new javax.swing.JLabel();
        btnOpen = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        volumeSilder = new javax.swing.JSlider();
        btnLoop = new javax.swing.JButton();
        btnRandom = new javax.swing.JButton();
        PlayListSelectPanel = new javax.swing.JPanel();
        PlayListSPTitle = new javax.swing.JLabel();
        PlayListSelectSP = new javax.swing.JScrollPane();
        PlayListSelectContainer = new javax.swing.JPanel();
        ArtistLeftPanel = new javax.swing.JPanel();
        ArtistLeftPanelTitle = new javax.swing.JLabel();
        ArtistLeftPanelBg = new javax.swing.JLabel();
        AlbumLeftPanel = new javax.swing.JPanel();
        AlbumLeftPanelTitle = new javax.swing.JLabel();
        AlbumLeftPanelBg = new javax.swing.JLabel();
        UploadedLeftPanel = new javax.swing.JPanel();
        UploadLeftTitle = new javax.swing.JLabel();
        UploadedDec = new javax.swing.JLabel();
        UploadDec2 = new javax.swing.JLabel();
        UploadDec3 = new javax.swing.JLabel();
        uploadSearch = new javax.swing.JButton();
        upldSearchValue = new javax.swing.JTextField();
        UploadedLeftPanelBg = new javax.swing.JLabel();
        AudioTabbedPane = new javax.swing.JTabbedPane();
        PlaylistPanel = new javax.swing.JScrollPane();
        tabList = new javax.swing.JTable();
        ArtistScrollPane = new javax.swing.JScrollPane();
        artistContainer = new javax.swing.JPanel();
        AlbumScrollPane = new javax.swing.JScrollPane();
        albumContainer = new javax.swing.JPanel();
        UploadedPanel = new javax.swing.JPanel();
        uploadLoadingPanel = new javax.swing.JPanel();
        uploadProcessLabel = new javax.swing.JLabel();
        UploadedLoading = new javax.swing.JLabel();
        UploadedMainPanel = new javax.swing.JPanel();
        uploadTextAlbum = new javax.swing.JLabel();
        uploadTextTotal = new javax.swing.JButton();
        uploadScrollPane = new javax.swing.JScrollPane();
        uploadPublicPanel = new javax.swing.JScrollPane();
        uploadContainer = new javax.swing.JPanel();
        UploadSubPanel = new javax.swing.JPanel();
        UploadSubScrollPane = new javax.swing.JScrollPane();
        UploadSubPanelContainer = new javax.swing.JPanel();
        SearchedPanel = new javax.swing.JScrollPane();
        searchContainer = new javax.swing.JPanel();
        AddAudioPanel = new javax.swing.JPanel();
        btnAddAudioOpen = new javax.swing.JButton();
        AddAudioTitle = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        btnSettings = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnMini = new javax.swing.JButton();
        btnMove = new javax.swing.JButton();
        Background = new javax.swing.JLabel();
        MainBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FYPlayer");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("frame"); // NOI18N
        setResizable(false);
        setUndecorated(true);
        getContentPane().setLayout(null);

        MainPanel.setOpaque(false);
        MainPanel.setLayout(null);

        btnSetting.setBackground(new java.awt.Color(255, 255, 255));
        btnSetting.setText("FYPMusicPlayer BETA");
        btnSetting.setBorder(null);
        btnSetting.setContentAreaFilled(false);
        btnSetting.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSettingMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSettingMouseExited(evt);
            }
        });
        btnSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSettingActionPerformed(evt);
            }
        });
        MainPanel.add(btnSetting);
        btnSetting.setBounds(630, 480, 130, 20);

        PlayListShowPanel.setOpaque(false);
        PlayListShowPanel.setLayout(null);

        PlayListLoading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/loading.gif"))); // NOI18N
        PlayListShowPanel.add(PlayListLoading);
        PlayListLoading.setBounds(220, 190, 50, 50);

        PlayListTitle.setBackground(new java.awt.Color(255, 255, 255));
        PlayListTitle.setFont(new java.awt.Font("Tahoma", 0, 18));
        PlayListTitle.setForeground(new java.awt.Color(255, 255, 255));
        PlayListTitle.setText("Playing on list - ");
        PlayListShowPanel.add(PlayListTitle);
        PlayListTitle.setBounds(80, 20, 320, 22);

        PlayListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        PlayListScrollPane.setViewportView(PlayListTable);

        PlayListShowPanel.add(PlayListScrollPane);
        PlayListScrollPane.setBounds(90, 60, 230, 310);

        PlayListBackGround.setBackground(new java.awt.Color(255, 204, 204));
        PlayListBackGround.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/playlistbg.png"))); // NOI18N
        PlayListShowPanel.add(PlayListBackGround);
        PlayListBackGround.setBounds(0, 0, 320, 390);

        MainPanel.add(PlayListShowPanel);
        PlayListShowPanel.setBounds(110, 100, 450, 390);

        loginPanel.setLayout(null);
        loginPanel.add(LoginTitle);
        LoginTitle.setBounds(0, 0, 760, 40);
        loginPanel.add(loginDec);
        loginDec.setBounds(60, 60, 450, 30);
        loginPanel.add(loginDec2);
        loginDec2.setBounds(170, 110, 420, 30);
        loginPanel.add(loginDec3);
        loginDec3.setBounds(305, 235, 220, 20);

        jLabel1.setText("Username");
        loginPanel.add(jLabel1);
        jLabel1.setBounds(220, 280, 90, 14);

        jLabel2.setText("Passwords");
        loginPanel.add(jLabel2);
        jLabel2.setBounds(340, 280, 120, 14);
        loginPanel.add(logUsername);
        logUsername.setBounds(218, 255, 110, 25);
        loginPanel.add(logPassword);
        logPassword.setBounds(340, 255, 120, 25);

        btnLogin.setText("Login!");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        loginPanel.add(btnLogin);
        btnLogin.setBounds(470, 256, 73, 23);

        btnReg.setForeground(new java.awt.Color(153, 153, 255));
        btnReg.setText("No Account ?");
        btnReg.setBorder(null);
        btnReg.setBorderPainted(false);
        btnReg.setContentAreaFilled(false);
        btnReg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnRegMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnRegMouseReleased(evt);
            }
        });
        btnReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegActionPerformed(evt);
            }
        });
        loginPanel.add(btnReg);
        btnReg.setBounds(550, 256, 80, 20);

        btnCover.setBorder(null);
        btnCover.setBorderPainted(false);
        btnCover.setContentAreaFilled(false);
        loginPanel.add(btnCover);
        btnCover.setBounds(0, 0, 760, 370);

        BackGround.setBackground(new java.awt.Color(255, 255, 255));
        BackGround.setOpaque(true);
        loginPanel.add(BackGround);
        BackGround.setBounds(0, 0, 760, 370);

        MainPanel.add(loginPanel);
        loginPanel.setBounds(0, 110, 760, 370);

        tabCover.setBorder(null);
        tabCover.setBorderPainted(false);
        tabCover.setContentAreaFilled(false);
        MainPanel.add(tabCover);
        tabCover.setBounds(3, 480, 630, 20);

        controlMsg.setBackground(new java.awt.Color(255, 255, 255));
        controlMsg.setOpaque(true);
        MainPanel.add(controlMsg);
        controlMsg.setBounds(0, 480, 760, 20);

        btnPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/btn_Play.png"))); // NOI18N
        btnPlay.setAlignmentY(0.0F);
        btnPlay.setBorder(null);
        btnPlay.setBorderPainted(false);
        btnPlay.setContentAreaFilled(false);
        btnPlay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnPlayMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnPlayMouseReleased(evt);
            }
        });
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });
        MainPanel.add(btnPlay);
        btnPlay.setBounds(650, 30, 40, 40);

        CurrentPlPanel.setOpaque(false);
        CurrentPlPanel.setLayout(null);

        btnCurrentPlaylist.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnCurrentPlaylist.setForeground(java.awt.Color.lightGray);
        btnCurrentPlaylist.setText("Current List");
        btnCurrentPlaylist.setBorder(null);
        btnCurrentPlaylist.setContentAreaFilled(false);
        btnCurrentPlaylist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCurrentPlaylistMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCurrentPlaylistMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnCurrentPlaylistMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCurrentPlaylistMouseReleased(evt);
            }
        });
        btnCurrentPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCurrentPlaylistActionPerformed(evt);
            }
        });
        CurrentPlPanel.add(btnCurrentPlaylist);
        btnCurrentPlaylist.setBounds(0, 5, 100, 30);

        MainPanel.add(CurrentPlPanel);
        CurrentPlPanel.setBounds(0, 70, 100, 40);

        uploadButtonPanel.setOpaque(false);
        uploadButtonPanel.setLayout(null);

        btnUpload.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnUpload.setForeground(java.awt.Color.lightGray);
        btnUpload.setText("Upload Now!");
        btnUpload.setBorder(null);
        btnUpload.setContentAreaFilled(false);
        btnUpload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUploadMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUploadMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnUploadMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnUploadMouseReleased(evt);
            }
        });
        btnUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadActionPerformed(evt);
            }
        });
        uploadButtonPanel.add(btnUpload);
        btnUpload.setBounds(0, 5, 100, 30);

        MainPanel.add(uploadButtonPanel);
        uploadButtonPanel.setBounds(660, 70, 100, 40);

        TableTab.setOpaque(false);
        TableTab.setLayout(null);

        btnAllSong.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnAllSong.setText("All Songs");
        btnAllSong.setContentAreaFilled(false);
        btnAllSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAllSongActionPerformed(evt);
            }
        });
        TableTab.add(btnAllSong);
        btnAllSong.setBounds(54, 10, 90, 24);

        btnArtist.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnArtist.setText("Artist");
        btnArtist.setContentAreaFilled(false);
        btnArtist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArtistActionPerformed(evt);
            }
        });
        TableTab.add(btnArtist);
        btnArtist.setBounds(172, 10, 90, 24);

        btnAlbum.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnAlbum.setText("Album");
        btnAlbum.setContentAreaFilled(false);
        btnAlbum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlbumActionPerformed(evt);
            }
        });
        TableTab.add(btnAlbum);
        btnAlbum.setBounds(292, 10, 90, 24);

        btnUploadedSongs.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnUploadedSongs.setText("Uploaded");
        btnUploadedSongs.setContentAreaFilled(false);
        btnUploadedSongs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadedSongsActionPerformed(evt);
            }
        });
        TableTab.add(btnUploadedSongs);
        btnUploadedSongs.setBounds(412, 10, 90, 24);

        TableTabBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/Tab1.png"))); // NOI18N
        TableTab.add(TableTabBackground);
        TableTabBackground.setBounds(0, 0, 560, 40);

        MainPanel.add(TableTab);
        TableTab.setBounds(100, 70, 560, 40);

        PlayingPanelContainer.setOpaque(false);
        PlayingPanelContainer.setLayout(null);

        durationText.setForeground(new java.awt.Color(255, 255, 255));
        durationText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        durationText.setText("--:-");
        PlayingPanelContainer.add(durationText);
        durationText.setBounds(60, 10, 260, 15);

        btnDetail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/default-listCover.png"))); // NOI18N
        btnDetail.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        btnDetail.setContentAreaFilled(false);
        btnDetail.setFocusPainted(false);
        btnDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetailActionPerformed(evt);
            }
        });
        PlayingPanelContainer.add(btnDetail);
        btnDetail.setBounds(10, 10, 40, 40);
        PlayingPanelContainer.add(durationProcessBar);
        durationProcessBar.setBounds(60, 29, 310, 4);

        playingTime.setBackground(new java.awt.Color(255, 255, 255));
        playingTime.setForeground(new java.awt.Color(255, 255, 255));
        playingTime.setText("--:--");
        PlayingPanelContainer.add(playingTime);
        playingTime.setBounds(60, 34, 100, 20);

        leftTime.setForeground(new java.awt.Color(255, 255, 255));
        leftTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        leftTime.setText("--:--");
        leftTime.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        PlayingPanelContainer.add(leftTime);
        leftTime.setBounds(270, 34, 100, 20);

        totalTime.setForeground(new java.awt.Color(255, 255, 255));
        totalTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalTime.setText("--:--");
        totalTime.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        PlayingPanelContainer.add(totalTime);
        totalTime.setBounds(329, 10, 40, 15);

        lblClose.setForeground(new java.awt.Color(255, 255, 255));
        lblClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        PlayingPanelContainer.add(lblClose);
        lblClose.setBounds(145, 34, 140, 20);

        MainPanel.add(PlayingPanelContainer);
        PlayingPanelContainer.setBounds(190, 10, 380, 60);

        btnOpen.setForeground(new java.awt.Color(255, 255, 255));
        btnOpen.setText("Open");
        btnOpen.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        btnOpen.setContentAreaFilled(false);
        btnOpen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnOpenMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnOpenMouseReleased(evt);
            }
        });
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });
        MainPanel.add(btnOpen);
        btnOpen.setBounds(20, 25, 40, 25);

        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/btn_MainBack.png"))); // NOI18N
        btnBack.setBorder(null);
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnBackMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnBackMouseReleased(evt);
            }
        });
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        MainPanel.add(btnBack);
        btnBack.setBounds(610, 35, 30, 30);

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/btn_MainNext.png"))); // NOI18N
        btnNext.setBorder(null);
        btnNext.setBorderPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnNextMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNextMouseReleased(evt);
            }
        });
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        MainPanel.add(btnNext);
        btnNext.setBounds(700, 35, 30, 30);

        volumeSilder.setOpaque(false);
        volumeSilder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSilderStateChanged(evt);
            }
        });
        MainPanel.add(volumeSilder);
        volumeSilder.setBounds(30, 55, 100, 15);

        btnLoop.setIcon(btnLoopIcon);
        btnLoop.setBorderPainted(false);
        btnLoop.setContentAreaFilled(false);
        btnLoop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnLoopMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnLoopMouseReleased(evt);
            }
        });
        btnLoop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoopActionPerformed(evt);
            }
        });
        MainPanel.add(btnLoop);
        btnLoop.setBounds(118, 25, 26, 25);

        btnRandom.setIcon(btnNotRandomIcon);
        btnRandom.setBorderPainted(false);
        btnRandom.setContentAreaFilled(false);
        btnRandom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnRandomMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnRandomMouseReleased(evt);
            }
        });
        btnRandom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRandomActionPerformed(evt);
            }
        });
        MainPanel.add(btnRandom);
        btnRandom.setBounds(75, 25, 30, 25);

        PlayListSelectPanel.setBackground(new java.awt.Color(255, 255, 255));
        PlayListSelectPanel.setLayout(null);
        PlayListSelectPanel.add(PlayListSPTitle);
        PlayListSPTitle.setBounds(0, 0, 170, 40);

        PlayListSelectSP.setBackground(new java.awt.Color(255, 255, 255));
        PlayListSelectSP.setBorder(null);
        PlayListSelectSP.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        PlayListSelectContainer.setLayout(null);
        PlayListSelectSP.setViewportView(PlayListSelectContainer);

        PlayListSelectPanel.add(PlayListSelectSP);
        PlayListSelectSP.setBounds(0, 40, 170, 330);

        MainPanel.add(PlayListSelectPanel);
        PlayListSelectPanel.setBounds(0, 110, 170, 370);

        ArtistLeftPanel.setBackground(new java.awt.Color(255, 255, 255));
        ArtistLeftPanel.setLayout(null);
        ArtistLeftPanel.add(ArtistLeftPanelTitle);
        ArtistLeftPanelTitle.setBounds(2, 0, 160, 40);

        ArtistLeftPanelBg.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        ArtistLeftPanel.add(ArtistLeftPanelBg);
        ArtistLeftPanelBg.setBounds(0, 0, 170, 370);

        MainPanel.add(ArtistLeftPanel);
        ArtistLeftPanel.setBounds(0, 110, 170, 370);

        AlbumLeftPanel.setBackground(new java.awt.Color(255, 255, 255));
        AlbumLeftPanel.setLayout(null);
        AlbumLeftPanel.add(AlbumLeftPanelTitle);
        AlbumLeftPanelTitle.setBounds(2, 0, 160, 40);

        AlbumLeftPanelBg.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        AlbumLeftPanel.add(AlbumLeftPanelBg);
        AlbumLeftPanelBg.setBounds(0, 0, 170, 370);

        MainPanel.add(AlbumLeftPanel);
        AlbumLeftPanel.setBounds(0, 110, 170, 370);

        UploadedLeftPanel.setBackground(new java.awt.Color(255, 255, 255));
        UploadedLeftPanel.setLayout(null);
        UploadedLeftPanel.add(UploadLeftTitle);
        UploadLeftTitle.setBounds(0, 0, 170, 40);
        UploadedLeftPanel.add(UploadedDec);
        UploadedDec.setBounds(5, 105, 160, 60);

        UploadDec2.setForeground(new java.awt.Color(0, 102, 102));
        UploadDec2.setText("Search anythings");
        UploadedLeftPanel.add(UploadDec2);
        UploadDec2.setBounds(40, 40, 120, 20);

        UploadDec3.setForeground(new java.awt.Color(0, 102, 102));
        UploadDec3.setText("by blank value");
        UploadedLeftPanel.add(UploadDec3);
        UploadDec3.setBounds(40, 60, 120, 14);

        uploadSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/search.png"))); // NOI18N
        uploadSearch.setBorderPainted(false);
        uploadSearch.setContentAreaFilled(false);
        uploadSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadSearchActionPerformed(evt);
            }
        });
        UploadedLeftPanel.add(uploadSearch);
        uploadSearch.setBounds(5, 45, 30, 30);
        UploadedLeftPanel.add(upldSearchValue);
        upldSearchValue.setBounds(5, 80, 150, 20);
        UploadedLeftPanel.add(UploadedLeftPanelBg);
        UploadedLeftPanelBg.setBounds(0, 0, 170, 370);

        MainPanel.add(UploadedLeftPanel);
        UploadedLeftPanel.setBounds(0, 110, 170, 370);

        AudioTabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        AudioTabbedPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        AudioTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        AudioTabbedPane.setAutoscrolls(true);
        AudioTabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tabList.setModel(tableModel);
        PlaylistPanel.setViewportView(tabList);

        AudioTabbedPane.addTab("All Songs", PlaylistPanel);

        ArtistScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        ArtistScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ArtistScrollPane.setToolTipText("");
        ArtistScrollPane.setOpaque(false);

        artistContainer.setLayout(null);
        ArtistScrollPane.setViewportView(artistContainer);

        AudioTabbedPane.addTab("Artist", ArtistScrollPane);

        AlbumScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        AlbumScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        albumContainer.setLayout(null);
        AlbumScrollPane.setViewportView(albumContainer);

        AudioTabbedPane.addTab("Album", AlbumScrollPane);

        UploadedPanel.setBackground(new java.awt.Color(255, 255, 255));
        UploadedPanel.setLayout(null);

        uploadLoadingPanel.setOpaque(false);
        uploadLoadingPanel.setLayout(null);

        uploadProcessLabel.setFont(new java.awt.Font("Tahoma", 1, 12));
        uploadProcessLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        uploadProcessLabel.setText("--");
        uploadLoadingPanel.add(uploadProcessLabel);
        uploadProcessLabel.setBounds(10, 160, 560, 20);

        UploadedLoading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/loading.gif"))); // NOI18N
        uploadLoadingPanel.add(UploadedLoading);
        UploadedLoading.setBounds(260, 110, 50, 50);

        UploadedPanel.add(uploadLoadingPanel);
        uploadLoadingPanel.setBounds(0, 0, 590, 370);

        UploadedMainPanel.setBackground(new java.awt.Color(255, 255, 255));
        UploadedMainPanel.setLayout(null);
        UploadedMainPanel.add(uploadTextAlbum);
        uploadTextAlbum.setBounds(0, 0, 590, 50);

        uploadTextTotal.setBackground(new java.awt.Color(255, 255, 255));
        uploadTextTotal.setFont(new java.awt.Font("Tahoma", 1, 12));
        uploadTextTotal.setText("Your Total Audio");
        uploadTextTotal.setBorder(null);
        uploadTextTotal.setBorderPainted(false);
        uploadTextTotal.setContentAreaFilled(false);
        uploadTextTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        uploadTextTotal.setOpaque(true);
        UploadedMainPanel.add(uploadTextTotal);
        uploadTextTotal.setBounds(0, 255, 580, 25);

        uploadScrollPane.setBorder(null);
        UploadedMainPanel.add(uploadScrollPane);
        uploadScrollPane.setBounds(0, 280, 580, 90);

        uploadPublicPanel.setBorder(null);
        uploadPublicPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        uploadContainer.setBackground(new java.awt.Color(255, 255, 255));
        uploadPublicPanel.setViewportView(uploadContainer);

        UploadedMainPanel.add(uploadPublicPanel);
        uploadPublicPanel.setBounds(0, 40, 580, 210);

        UploadedPanel.add(UploadedMainPanel);
        UploadedMainPanel.setBounds(0, 0, 590, 370);

        UploadSubPanel.setLayout(null);

        UploadSubScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        UploadSubScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        UploadSubPanelContainer.setBackground(new java.awt.Color(255, 255, 255));
        UploadSubPanelContainer.setLayout(null);
        UploadSubScrollPane.setViewportView(UploadSubPanelContainer);

        UploadSubPanel.add(UploadSubScrollPane);
        UploadSubScrollPane.setBounds(0, 0, 590, 370);

        UploadedPanel.add(UploadSubPanel);
        UploadSubPanel.setBounds(0, 0, 590, 370);

        AudioTabbedPane.addTab("Uploaded", UploadedPanel);

        SearchedPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 153)));
        SearchedPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        searchContainer.setLayout(null);
        SearchedPanel.setViewportView(searchContainer);

        AudioTabbedPane.addTab("tab5", SearchedPanel);

        AddAudioPanel.setBackground(new java.awt.Color(255, 255, 255));
        AddAudioPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        AddAudioPanel.setLayout(null);

        btnAddAudioOpen.setFont(new java.awt.Font("Tahoma", 0, 12));
        btnAddAudioOpen.setText("Click me to import audios :D");
        btnAddAudioOpen.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 51, 51), 1, true));
        btnAddAudioOpen.setContentAreaFilled(false);
        btnAddAudioOpen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnAddAudioOpenMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnAddAudioOpenMouseReleased(evt);
            }
        });
        btnAddAudioOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAudioOpenActionPerformed(evt);
            }
        });
        AddAudioPanel.add(btnAddAudioOpen);
        btnAddAudioOpen.setBounds(10, 100, 170, 23);

        AddAudioTitle.setBackground(new java.awt.Color(255, 255, 255));
        AddAudioTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        AddAudioPanel.add(AddAudioTitle);
        AddAudioTitle.setBounds(10, 10, 500, 200);

        AudioTabbedPane.addTab("tab6", AddAudioPanel);

        MainPanel.add(AudioTabbedPane);
        AudioTabbedPane.setBounds(170, 110, 590, 380);
        AudioTabbedPane.getAccessibleContext().setAccessibleName("PlayerTab");

        welcomeLabel.setForeground(new java.awt.Color(255, 255, 255));
        welcomeLabel.setText("Not yet Login");
        MainPanel.add(welcomeLabel);
        welcomeLabel.setBounds(5, 2, 160, 14);

        btnSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/settings.png"))); // NOI18N
        btnSettings.setBorderPainted(false);
        btnSettings.setContentAreaFilled(false);
        btnSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSettingsActionPerformed(evt);
            }
        });
        MainPanel.add(btnSettings);
        btnSettings.setBounds(700, 0, 20, 20);

        btnExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/close.png"))); // NOI18N
        btnExit.setBorderPainted(false);
        btnExit.setContentAreaFilled(false);
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        MainPanel.add(btnExit);
        btnExit.setBounds(740, 0, 20, 20);

        btnMini.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/minium.png"))); // NOI18N
        btnMini.setBorderPainted(false);
        btnMini.setContentAreaFilled(false);
        btnMini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMiniActionPerformed(evt);
            }
        });
        MainPanel.add(btnMini);
        btnMini.setBounds(720, 0, 20, 20);

        btnMove.setBorderPainted(false);
        btnMove.setContentAreaFilled(false);
        btnMove.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnMoveMousePressed(evt);
            }
        });
        btnMove.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                btnMoveMouseDragged(evt);
            }
        });
        MainPanel.add(btnMove);
        btnMove.setBounds(0, 0, 760, 70);

        Background.setBackground(new java.awt.Color(41, 40, 40));
        Background.setOpaque(true);
        MainPanel.add(Background);
        Background.setBounds(0, 0, 760, 500);

        getContentPane().add(MainPanel);
        MainPanel.setBounds(5, 5, 760, 500);

        MainBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/MainBackground.png"))); // NOI18N
        getContentPane().add(MainBackground);
        MainBackground.setBounds(0, 0, 770, 510);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-770)/2, (screenSize.height-510)/2, 770, 510);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayActionPerformed
        // TODO add your handling code here:
        stop = false;
        if(!isPlaying){
            musicPlay();
        }else if(!pause && isPlaying){
            player.pause();
            pause = true;
        }else if(pause && isPlaying){
            player.resume();
            pause = false;
        }
}//GEN-LAST:event_btnPlayActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileFilter(new AudioFileFilter());
        int returnVal = jfc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            final File[] files = jfc.getSelectedFiles();

        
            loadingFrame.popout();
            new Thread(new Runnable(){public void run(){
                if(isPlaying)stop();
                
                for(int i = 0 ; i < files.length ; i++){
                    addToLibrary(files[i]);
                }
                controlMsg.setText("Read "+(AudioList.size()+1)+" Files");

                tableModel = new DefaultListModel();
                tabList.setModel(tableModel);
                for(int i = 0 ; i < AudioList.size(); i++){
                    try {
                        controlMsg.setText("Reading "+(i+1)+"/"+AudioList.size()+" of file(s)");
                    } catch (Exception ex) {}
                }


                /*
                 * read the audio again to receive new information
                 */
                anlysisAudioDetails();
                
                tableModel = new DefaultListModel();
                tabList.setModel(tableModel);
                for(int i = 0 ; i < AudioList.size(); i++){
                    try {
                        if(AudioList.get(i).getType() == OFFLINE_MODE){
                            AudioTAG playAudioTAG = new AudioTAG(AudioList.get(i).getPath());
                            tableModel.addRow(new Object[]{playAudioTAG.getTagTitle(),new durationToString(AudioList.get(i).getDuration()).toString(),playAudioTAG.getTagArtist(),playAudioTAG.getTagAlbum()});
                        }else
                            if(onlineMode){
                                AudioDetails au = (AudioDetails)connection.GetAudioDetails(AudioList.get(i).getNetID());

                                tableModel.addRow(new Object[]{au.getTagTitle(),new durationToString(au.getDurationInSec()).toString(),au.getTagArtist(),au.getTagAlbum()});

                                downList.add(au.getSongID());
                                try{
                                    if(downloadServicesStart)
                                        synchronized(downloadLock){
                                            downloadLock.notifyAll();
                                        }
                                    else
                                        audioDownloadingQueue();
                                }catch(Exception e){}
                                
                            }else{
                                tableModel.addRow(new Object[]{"Network files","-","-","-"});
                            }
                    } catch (Exception ex) {}
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
                
                loadingFrame.close();
                AudioTabbedPane.setSelectedIndex(0);
            }}).start();
        }
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // TODO add your handling code here:
        try{
            
            if(isPlaying){
                stop();
            }
            currentFileNumber--;
            musicPlay();
        }catch(Exception e){e.printStackTrace();}
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        try{
            //currentFileNumber += 2;
            if(isPlaying){
                stop();
            }
            currentFileNumber++;
            musicPlay();
        }catch(Exception e){e.printStackTrace();}
    }//GEN-LAST:event_btnNextActionPerformed

    private void volumeSilderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSilderStateChanged
        // TODO add your handling code here:
        defaultVolume = volumeSilder.getValue();
        durationText.setText("Set volume to - "+defaultVolume+"%");
        try{
            player.setVolume(defaultVolume);
        }catch(Exception e){}
}//GEN-LAST:event_volumeSilderStateChanged

    private void btnLoopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoopActionPerformed
        // TODO add your handling code here:
        if(loop==0){
            loop++;
            btnLoop.setIcon(btnLoopAllIcon);
        }else if(loop==1){
            loop++;
            btnLoop.setIcon(btnLoopOnceIcon);
        }else{
            loop=0;
            btnLoop.setIcon(btnLoopIcon);
        }
        cloop=loop;
    }//GEN-LAST:event_btnLoopActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        if(currentPlaylistName == null){
            Control.setSong(new File(AudioList.get(currentFileNumber).getPath()),time);
        }else{
            Control.setSong(new File(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath()),time);
        }
        Control.setVisible(true);
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnMoveMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMoveMouseDragged
        // TODO add your handling code here:
        this.setLocation(this.getX()+(int)(evt.getX()-cursor.getX()),this.getY()+(int)(evt.getY()-cursor.getY()));
    }//GEN-LAST:event_btnMoveMouseDragged

    private void btnMoveMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMoveMousePressed
        // TODO add your handling code here:
        cursor = evt.getPoint();
    }//GEN-LAST:event_btnMoveMousePressed

    private void btnAllSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAllSongActionPerformed
        // TODO add your handling code here:
        this.AudioTabbedPane.setSelectedIndex(0);
        this.loginPanel.setVisible(false);
        this.TableTabBackground.setIcon(tab1bg);
        
        /*
         * Panel Select
         */
        UploadedLeftPanel.setVisible(false);
        ArtistLeftPanel.setVisible(false);
        AlbumLeftPanel.setVisible(false);
        PlayListSelectPanel.setVisible(true);
        
        if(AudioList.size() == 0){
            AudioTabbedPane.setSelectedIndex(5);
            return;
        }
        
    }//GEN-LAST:event_btnAllSongActionPerformed

    private void btnArtistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArtistActionPerformed
        // TODO add your handling code here:
        this.AudioTabbedPane.setSelectedIndex(1);
        this.loginPanel.setVisible(false);
        this.TableTabBackground.setIcon(tab2bg);
        
        /*
         * Panel Select
         */
        UploadedLeftPanel.setVisible(false);
        ArtistLeftPanel.setVisible(true);
        AlbumLeftPanel.setVisible(false);
        PlayListSelectPanel.setVisible(false);
        
        if(AudioList.size() == 0){
            AudioTabbedPane.setSelectedIndex(5);
            return;
        }
        
        /*
         * Artist Table Audio Selecter
         * jPanel(artistNameTable)
         *    |-- jTable(artistSongTable)
         *           |-- Audio Name in row
         * 
         * addMouseListener to final Table
         * problems(if update, MUST regenerate tables)
         * 
         * Sebastian Ko 28/02/2013
         */
        loadingFrame.popout();
        new Thread(new Runnable(){public void run(){
            if(ArtistList.size() != 0 && !statusArtistTabCheck){
                artistContainer.removeAll();
                artistContainer.setBackground(Color.white);
                artistContainer.setPreferredSize(new Dimension(590,150));
                int panelHeight = 0;
                for(int i = 0 ; i < ArtistList.size() ; i++){
                    try{
                        /*
                         * Title height = 40
                         * Basic height = 150(Extra Data + 150)
                         * Adding data to artistContainer to provide Table look details
                         * 
                         * Sebastian Ko 02/03/2013
                         */
                        int totalUsedHeight = panelHeight+5;

                        JLabel artistName = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">"+ArtistList.get(i)+"</font></b></div><hr width=560 size=1 Noshade align=left></html>");
                        artistContainer.add(artistName);
                        artistName.setBounds(0, totalUsedHeight+2, 590, 30);
                        totalUsedHeight += 2+30;


                        JLabel artistAlbumCover = new JLabel();
                        artistAlbumCover.setSize(128, 128);
                        artistAlbumCover.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
                        boolean check = false;
                        for(int j = 0 ; j < AudioList.size() ; j++){
                            if(AudioList.get(j).getType() == 'O'){
                                try{
                                if(new AudioTAG(AudioList.get(j).getPath()).getTagArtist().equals(ArtistList.get(i))){


                                    if(AudioList.get(j).getDefaultImagePath() != null && !new File(AudioList.get(j).getDefaultImagePath()).exists()){
                                        AudioList.get(j).setDefaultImagePath(
                                            new AudioImage().ImageIconToFile(new AudioTAG(AudioList.get(j).getPath()).getTagCover().getScaledInstance(128,128, Image.SCALE_SMOOTH),"png",".\\cache\\")
                                        );
                                    }

                                    if(AudioList.get(j).getDefaultImagePath() != null){
                                        artistAlbumCover.setIcon(new ImageIcon(AudioList.get(j).getDefaultImagePath()));
                                        check = true;
                                        break;
                                    }
                                }
                                }catch(Exception e){}
                            }else
                                try{
                                if(new AudioTAG(AudioList.get(j).getPath()).getTagArtist().equals(ArtistList.get(i))){
                                    Image tempCover = new AudioTAG(AudioList.get(j).getPath()).getTagCover();
                                    if(tempCover != null){
                                        artistAlbumCover.setIcon(new ImageIcon(tempCover.getScaledInstance(128,128, Image.SCALE_SMOOTH)));
                                        check = true;
                                        break;
                                    }
                                }
                                }catch(Exception e){}
                        }
                        if(!check)artistAlbumCover.setIcon(defaultCover);
                        /*
                         * Add height / set bounds
                         */
                        artistContainer.add(artistAlbumCover);
                        artistAlbumCover.setBounds(5, totalUsedHeight, 128, 128);
                        totalUsedHeight += 128;

                        /*
                         * Add audio list from this artist
                         */
                        final JTable artistAListTable         = new JTable();
                        final DefaultTableModel artistAModel  = new ArtistListCellModel();
                        artistAListTable.setModel(artistAModel);
                        artistAListTable.removeColumn(artistAListTable.getColumn("LibID"));
                        artistAListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                        for(int j = 0 ; j < AudioList.size() ; j++){
                            try{
                            if(new AudioTAG(AudioList.get(j).getPath()).getTagArtist().equals(ArtistList.get(i))){
                                artistAModel.addRow(new Object[]{
                                    new AudioTAG(AudioList.get(j).getPath()).getTagTitle(),
                                    j
                                });
                            }
                            }catch(Exception e){}
                        }
                        artistAListTable.addMouseListener(new MouseAdapter() {
                            private void showPopup(MouseEvent evt){
                                if(evt.isPopupTrigger()){
                                    final Point p       = evt.getPoint();
                                    final int tempRow   = artistAListTable.rowAtPoint(p);
                                    System.out.println("Library ID : "+artistAModel.getValueAt(tempRow, artistAModel.findColumn("LibID")));
                                    /*
                                     * Popup menu
                                     */
                                    JPopupMenu tablePopup  = new JPopupMenu();
                                    
                                    JMenuItem audioTitle = new JMenuItem("-"+(String)artistAModel.getValueAt(tempRow,0)+"-");
                                    audioTitle.setForeground(Color.MAGENTA);
                                    audioTitle.setEnabled(false);
                                    tablePopup.add(audioTitle);
                                    
                                    for(int i = 0 ; i < playlistFiles.length ; i ++){
                                        if(playlistFiles[i].getName().endsWith(".list")){
                                            final int tempListNum = i;
                                            
                                            JMenuItem addPlaylist      = new JMenuItem("Add to ["+playlistFiles[i].getName().replaceAll(".list", "")+"]");
                                            addPlaylist.addMouseListener(new MouseAdapter(){
                                                public void mousePressed(MouseEvent evt){
                                                    /*
                                                     * ADD to list
                                                     */
                                                    try{
                                                        ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(playlistFiles[tempListNum]));
                                                        LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();
                                                        tempListRead.close();
                                                        
                                                        /**ADD**/
                                                        tempList.add((Integer)artistAModel.getValueAt(tempRow, artistAModel.findColumn("LibID")));
                                                        
                                                        ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(playlistFiles[tempListNum],false));
                                                        playlistRegen.writeObject(tempList);
                                                        playlistRegen.close();

                                                        if(currentPlaylistName != null && currentPlaylistName.equals(playlistFiles[tempListNum].getName().replaceAll(".list", ""))){
                                                            currentPlaylist.add((Integer)artistAModel.getValueAt(tempRow, artistAModel.findColumn("LibID")));
                                                        }
                                                        
                                                        updatePlaylistStatus(playlistFiles[tempListNum].getName(),(String)artistAModel.getValueAt(tempRow,0));
                                                    }catch(Exception e){
                                                        /*
                                                         * Error
                                                         */
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            tablePopup.add(addPlaylist);
                                        }
                                    }

                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }

                            public void mouseReleased(MouseEvent evt){
                                showPopup(evt);
                            }
                            
                            public void mouseClicked(MouseEvent evt) {
                                if(!artistTableWaiting && evt.getClickCount() == 2){
                                    artistTableWaiting = true;
                                    try{Thread.sleep(500);}catch(Exception e){}

                                    synchronized(this){
                                        currentPlaylistName = null;
                                        currentPlaylist     = null;
                                        
                                        if(isPlaying)stop();
                                        currentFileNumber = (Integer)artistAModel.getValueAt(artistAListTable.rowAtPoint(evt.getPoint()),1);
                                        artistAListTable.clearSelection();
                                        musicPlay();

                                        /*Delay clicking time*/
                                        try{Thread.sleep(1000);}catch(Exception e){}
                                        artistTableWaiting = false;
                                    }
                                }
                            }
                        });

                        artistContainer.add(artistAListTable);
                        artistAListTable.setBounds(150, totalUsedHeight-128, 400, (int)artistAListTable.getPreferredSize().getHeight());

                        if(artistAListTable.getPreferredSize().getHeight() > 128)totalUsedHeight += artistAListTable.getPreferredSize().getHeight() - 128;

                        panelHeight = totalUsedHeight+5;
                        artistContainer.setPreferredSize(new Dimension(590,panelHeight));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                /*
                 * Audio with no artist
                 */
                try{
                    int totalUsedHeight = panelHeight+5;

                    JLabel artistName = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">Others</font></b></div><hr width=560 size=1 Noshade align=left></html>");
                    artistContainer.add(artistName);
                    artistName.setBounds(0, totalUsedHeight+2, 590, 30);
                    totalUsedHeight += 2+30;


                    JLabel artistAlbumCover = new JLabel();
                    artistAlbumCover.setSize(128, 128);
                    artistAlbumCover.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
                    artistAlbumCover.setIcon(defaultCover);
                    /*
                     * Add height / set bounds
                     */
                    artistContainer.add(artistAlbumCover);
                    artistAlbumCover.setBounds(5, totalUsedHeight, 128, 128);
                    totalUsedHeight += 128;

                    /*
                     * Add audio list from this artist
                     */
                    final JTable artistAListTable         = new JTable();
                    final DefaultTableModel artistAModel  = new ArtistListCellModel();
                    artistAListTable.setModel(artistAModel);
                    artistAListTable.removeColumn(artistAListTable.getColumn("LibID"));

                    for(int j = 0 ; j < AudioList.size() ; j++){
                        try{
                        if(new AudioTAG(AudioList.get(j).getPath()).getTagArtist() == null){
                            artistAModel.addRow(new Object[]{
                                new AudioTAG(AudioList.get(j).getPath()).getTagTitle(),
                                j
                            });
                        }
                        }catch(Exception e){}
                        
                    }
                    artistAListTable.addMouseListener(new MouseAdapter() {
                        private void showPopup(MouseEvent evt){
                            if(evt.isPopupTrigger()){
                                final Point p       = evt.getPoint();
                                final int tempRow   = artistAListTable.rowAtPoint(p);
                                System.out.println("Library ID : "+artistAModel.getValueAt(tempRow, artistAModel.findColumn("LibID")));
                                /*
                                 * Popup menu
                                 */
                                JPopupMenu tablePopup  = new JPopupMenu();

                                JMenuItem audioTitle = new JMenuItem("-"+(String)artistAModel.getValueAt(tempRow,0)+"-");
                                audioTitle.setForeground(Color.MAGENTA);
                                audioTitle.setEnabled(false);
                                tablePopup.add(audioTitle);

                                for(int i = 0 ; i < playlistFiles.length ; i ++){
                                    if(playlistFiles[i].getName().endsWith(".list")){
                                        final int tempListNum = i;

                                        JMenuItem addPlaylist      = new JMenuItem("Add to ["+playlistFiles[i].getName().replaceAll(".list", "")+"]");
                                        addPlaylist.addMouseListener(new MouseAdapter(){
                                            public void mousePressed(MouseEvent evt){
                                                /*
                                                 * ADD to list
                                                 */
                                                try{
                                                    ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(playlistFiles[tempListNum]));
                                                    LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();
                                                    tempListRead.close();

                                                    /**ADD**/
                                                    tempList.add((Integer)artistAModel.getValueAt(tempRow, artistAModel.findColumn("LibID")));

                                                    ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(playlistFiles[tempListNum],false));
                                                    playlistRegen.writeObject(tempList);
                                                    playlistRegen.close();

                                                    if(currentPlaylistName.equals(playlistFiles[tempListNum].getName().replaceAll(".list", ""))){
                                                        currentPlaylist.add((Integer)artistAModel.getValueAt(tempRow, artistAModel.findColumn("LibID")));
                                                    }

                                                    updatePlaylistStatus(playlistFiles[tempListNum].getName(),(String)artistAModel.getValueAt(tempRow,0));
                                                }catch(Exception e){
                                                    /*
                                                     * Error
                                                     */
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        tablePopup.add(addPlaylist);
                                    }
                                }

                                tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                            }
                        }

                        public void mouseReleased(MouseEvent evt){
                            showPopup(evt);
                        }

                        public void mouseClicked(MouseEvent evt) {
                            if(!artistTableWaiting && evt.getClickCount() == 2){
                                artistTableWaiting = true;
                                try{Thread.sleep(500);}catch(Exception e){}

                                synchronized(this){
                                    currentPlaylistName = null;
                                    currentPlaylist     = null;

                                    if(isPlaying)stop();
                                    currentFileNumber = (Integer)artistAModel.getValueAt(artistAListTable.rowAtPoint(evt.getPoint()),1);
                                    artistAListTable.clearSelection();
                                    musicPlay();

                                    /*Delay clicking time*/
                                    try{Thread.sleep(1000);}catch(Exception e){}
                                    artistTableWaiting = false;
                                }
                            }
                        }
                    });

                    artistContainer.add(artistAListTable);
                    artistAListTable.setBounds(150, totalUsedHeight-128, 400, (int)artistAListTable.getPreferredSize().getHeight());

                    if(artistAListTable.getPreferredSize().getHeight() > 128)totalUsedHeight += artistAListTable.getPreferredSize().getHeight() - 128;

                    panelHeight = totalUsedHeight+5;
                    artistContainer.setPreferredSize(new Dimension(590,panelHeight));
                }catch(Exception e){
                    e.printStackTrace();
                }

                /*
                 * No need to reload table
                 */
                statusArtistTabCheck = true;
                artistContainer.repaint();
                ArtistScrollPane.repaint();
            }
            
            loadingFrame.close();
            
        }}).start();
    }//GEN-LAST:event_btnArtistActionPerformed

    private void btnAlbumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlbumActionPerformed
        // TODO add your handling code here:
        this.AudioTabbedPane.setSelectedIndex(2);
        this.loginPanel.setVisible(false);
        this.TableTabBackground.setIcon(tab3bg);
        /*
         * Panel Select
         */
        UploadedLeftPanel.setVisible(false);
        ArtistLeftPanel.setVisible(false);
        AlbumLeftPanel.setVisible(true);
        PlayListSelectPanel.setVisible(false);
        
        if(AudioList.size() == 0){
            AudioTabbedPane.setSelectedIndex(5);
            return;
        }
        
        /*
         * Album Selecter
         * jPanel(albumContainer)
         *    |-- jTable(albumTable)
         *           |-- Audio Name in row
         * 
         * addMouseListener to final Table
         * problems(if update, MUST regenerate tables)
         * 
         * Sebastian Ko 10/03/2013
         */
        loadingFrame.popout();
        new Thread(new Runnable(){public void run(){
            if(AlbumList.size() != 0 && !statusAlbumTabCheck){
                albumContainer.removeAll();
                albumContainer.setBackground(Color.white);
                albumContainer.setPreferredSize(new Dimension(590,150));
                int panelHeight = 0;

                for(int i = 0 ; i < AlbumList.size() ; i++){
                    try{
                        /*
                         * Title height = 40
                         * Basic height = 150(Extra Data + 150)
                         * 
                         * Layout : 
                         * <Before Click>
                         * |           |
                         * |Cover Image|  Album Name <- Click Image
                         * |           |
                         * 
                         * <After Click>
                         *             |-----------|
                         * audio Title |albumTable |
                         *             |-----------|
                         * 
                         * Sebastian Ko 10/03/2013
                         */
                        int totalUsedHeight = panelHeight+5;

                        final JButton albumCover = new JButton();
                        albumCover.setSize(128, 128);
                        albumCover.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
                        boolean check = false;
                        for(int j = 0 ; j < AudioList.size() ; j++){
                            if(AudioList.get(j).getType() == 'O'){
                                try{
                                if(new AudioTAG(AudioList.get(j).getPath()).getTagAlbum().equals(AlbumList.get(i))){


                                    if(AudioList.get(j).getDefaultImagePath() != null && !new File(AudioList.get(j).getDefaultImagePath()).exists()){
                                        AudioList.get(j).setDefaultImagePath(
                                            new AudioImage().ImageIconToFile(new AudioTAG(AudioList.get(j).getPath()).getTagCover().getScaledInstance(128,128, Image.SCALE_SMOOTH),"png",".\\cache\\")
                                        );
                                    }

                                    if(AudioList.get(j).getDefaultImagePath() != null){
                                        albumCover.setIcon(new ImageIcon(AudioList.get(j).getDefaultImagePath()));
                                        check = true;
                                        break;
                                    }
                                }
                                }catch(Exception e){}
                            }else
                                try{
                                if(new AudioTAG(AudioList.get(j).getPath()).getTagAlbum().equals(AlbumList.get(i))){
                                    Image tempCover = new AudioTAG(AudioList.get(j).getPath()).getTagCover();
                                    if(tempCover != null){
                                        albumCover.setIcon(new ImageIcon(tempCover.getScaledInstance(128,128, Image.SCALE_SMOOTH)));
                                        check = true;
                                        break;
                                    }
                                }
                                }catch(Exception e){}
                        }
                        if(!check)albumCover.setIcon(defaultCover);
                        /*
                         * Add height / set bounds
                         */
                        albumContainer.add(albumCover);
                        albumCover.setBounds(5, totalUsedHeight, 128, 128);
                        totalUsedHeight += 128;

                        /*
                         * Album Title
                         */
                        final JLabel albumName = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">"+AlbumList.get(i)+"</font></b></div></html>");
                        albumContainer.add(albumName);
                        albumName.setBounds(128+50, totalUsedHeight-(128/2)-15, 400, 25);
                        totalUsedHeight += 0;



                        /*
                         * Add audio list from this artist
                         */
                        final JScrollPane albumScrollPane    = new JScrollPane();
                        albumScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                        final JTable albumAudioTable         = new JTable();
                        final DefaultTableModel albumModel   = new AlbumListCellModel();
                        albumAudioTable.setModel(albumModel);
                        albumAudioTable.removeColumn(albumAudioTable.getColumn("LibID"));
                        albumAudioTable.setTableHeader(null);
                        albumAudioTable.setForeground(Color.DARK_GRAY);
                        albumAudioTable.setBackground(Color.PINK);
                        albumAudioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        albumScrollPane.getViewport().setBackground(Color.PINK);


                        for(int j = 0 ; j < AudioList.size() ; j++){
                            try{
                            if(new AudioTAG(AudioList.get(j).getPath()).getTagAlbum().equals(AlbumList.get(i))){
                                albumModel.addRow(new Object[]{
                                    new AudioTAG(AudioList.get(j).getPath()).getTagTitle(),
                                    j
                                });
                            }
                            }catch(Exception e){}
                            
                        }
                        albumAudioTable.addMouseListener(new MouseAdapter() {
                            private void showPopup(MouseEvent evt){
                                if(evt.isPopupTrigger()){
                                    final Point p       = evt.getPoint();
                                    final int tempRow   = albumAudioTable.rowAtPoint(p);
                                    System.out.println("Library ID : "+albumModel.getValueAt(tempRow, albumModel.findColumn("LibID")));
                                    /*
                                     * Popup menu
                                     */
                                    JPopupMenu tablePopup  = new JPopupMenu();
                                    
                                    JMenuItem audioTitle = new JMenuItem("-"+(String)albumModel.getValueAt(tempRow,0)+"-");
                                    audioTitle.setForeground(Color.MAGENTA);
                                    audioTitle.setEnabled(false);
                                    tablePopup.add(audioTitle);
                                    
                                    for(int i = 0 ; i < playlistFiles.length ; i ++){
                                        if(playlistFiles[i].getName().endsWith(".list")){
                                            final int tempListNum = i;
                                            
                                            JMenuItem addPlaylist      = new JMenuItem("Add to ["+playlistFiles[i].getName().replaceAll(".list", "")+"]");
                                            addPlaylist.addMouseListener(new MouseAdapter(){
                                                public void mousePressed(MouseEvent evt){
                                                    /*
                                                     * ADD to list
                                                     */
                                                    try{
                                                        ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(playlistFiles[tempListNum]));
                                                        LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();
                                                        tempListRead.close();
                                                        
                                                        /**ADD**/
                                                        tempList.add((Integer)albumModel.getValueAt(tempRow, albumModel.findColumn("LibID")));
                                                        
                                                        ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(playlistFiles[tempListNum],false));
                                                        playlistRegen.writeObject(tempList);
                                                        playlistRegen.close();

                                                        if(currentPlaylistName.equals(playlistFiles[tempListNum].getName().replaceAll(".list", ""))){
                                                            currentPlaylist.add((Integer)albumModel.getValueAt(tempRow, albumModel.findColumn("LibID")));
                                                        }
                                                        
                                                        updatePlaylistStatus(playlistFiles[tempListNum].getName(),(String)albumModel.getValueAt(tempRow,0));
                                                    }catch(Exception e){
                                                        /*
                                                         * Error
                                                         */
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            tablePopup.add(addPlaylist);
                                        }
                                    }

                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }

                            public void mouseReleased(MouseEvent evt){
                                showPopup(evt);
                            }
                            
                            public void mouseClicked(MouseEvent evt) {
                                if(!albumAudioWaiting && evt.getClickCount() == 2){
                                    albumAudioWaiting = true;
                                    try{Thread.sleep(500);}catch(Exception e){}

                                    currentPlaylistName = null;
                                    currentPlaylist     = null;
                                    
                                    stop();
                                    currentFileNumber  = (Integer)albumModel.getValueAt(albumAudioTable.rowAtPoint(evt.getPoint()),1);
                                    albumAudioTable.clearSelection();
                                    musicPlay();

                                    /*Delay clicking time*/
                                    try{Thread.sleep(1000);}catch(Exception e){}
                                    albumAudioWaiting = false;
                                }
                            }
                        });
                        albumScrollPane.setViewportView(albumAudioTable);
                        albumContainer.add(albumScrollPane);
                        albumScrollPane.setBounds(-300, totalUsedHeight-110, 300, 110);
                        albumScrollPane.setVisible(false);


                        /*
                         * Background
                         */
                        final JLabel albumBkgd = new JLabel();
                        albumBkgd.setBackground(Color.PINK);
                        albumBkgd.setOpaque(true);
                        albumContainer.add(albumBkgd);
                        albumBkgd.setBounds(-590, totalUsedHeight-130, 590, 132);
                        albumBkgd.setVisible(false);

                        /*
                         * Add mouse listener to albumCover
                         */
                        albumCover.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent evt) {
                                if(!albumTableWaiting){
                                    albumTableWaiting = true;
                                    try{Thread.sleep(5);}catch(Exception e){}

                                    new Thread(new Runnable(){public void run(){
                                        int albumHeight     = albumCover.getY();
                                        int albumNameHeight = albumName.getY();
                                        int albumScrollHeight= albumScrollPane.getY();
                                        int albumBackground = albumBkgd.getY();

                                        for(int i = 10 ; i <= 320; i+= 10){
                                            albumCover.setLocation(i,albumHeight);
                                            albumName.setLocation(i+300,albumNameHeight);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }

                                        albumBkgd.setVisible(true);
                                        for(int i = -600 ; i <= 0; i+= 50){
                                            albumBkgd.setLocation(i, albumBackground);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }

                                        for(int j = -300 ; j <= 0; j += 10){
                                            albumName.setLocation(j, albumNameHeight - 128/2+10);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }
                                        albumName.setLocation(5, albumNameHeight - 128/2+10);

                                        albumScrollPane.setVisible(true);
                                        albumScrollPane.setBounds(5, albumScrollHeight, 300, 110);

                                        /*
                                         * Reverse
                                         */
                                        for(int i = 0; i < 1000;i++){
                                            if(albumNoWaiting)break;
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }
                                        albumNoWaiting = false;

                                        for(int e = 10 ; e >= -325; e -= 25){
                                            albumScrollPane.setLocation(e, albumScrollHeight);
                                            try{Thread.sleep(5);}catch(Exception ex){}
                                        }
                                        albumScrollPane.setVisible(false);

                                        for(int j = 10 ; j >= -300; j -= 25){
                                            albumName.setLocation(j, albumNameHeight - 128/2+10);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }

                                        for(int i = 0 ; i >= -600; i-= 100){
                                            albumBkgd.setLocation(i, albumBackground);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }
                                        albumBkgd.setVisible(false);

                                        for(int i = 320 ; i >= 10; i-= 25){
                                            albumCover.setLocation(i,albumHeight);
                                            albumName.setLocation(i+300,albumNameHeight);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }
                                        albumCover.setLocation(5,albumHeight);
                                        albumName.setLocation(128+50,albumNameHeight);

                                        albumTableWaiting = false;
                                    }}).start();

                                }else
                                    albumNoWaiting = true;
                            }
                        });


                        /*
                         * Finally
                         */
                        panelHeight = totalUsedHeight+5;
                        albumContainer.setPreferredSize(new Dimension(590,panelHeight));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                /*
                 * Audio with no album
                 */
                try{
                    int totalUsedHeight = panelHeight+5;

                    final JButton albumCover = new JButton();
                    albumCover.setSize(128, 128);
                    albumCover.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
                    albumCover.setIcon(defaultCover);
                    /*
                     * Add height / set bounds
                     */
                    albumContainer.add(albumCover);
                    albumCover.setBounds(5, totalUsedHeight, 128, 128);
                    totalUsedHeight += 128;

                    /*
                     * Album Title
                     */
                    final JLabel albumName = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">Others</font></b></div></html>");
                    albumContainer.add(albumName);
                    albumName.setBounds(128+50, totalUsedHeight-(128/2)-15, 400, 25);
                    totalUsedHeight += 0;



                    /*
                     * Add audio list from this artist
                     */
                    final JScrollPane albumScrollPane    = new JScrollPane();
                    albumScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                    final JTable albumAudioTable         = new JTable();
                    final DefaultTableModel albumModel   = new AlbumListCellModel();
                    albumAudioTable.setModel(albumModel);
                    albumAudioTable.removeColumn(albumAudioTable.getColumn("LibID"));
                    albumAudioTable.setTableHeader(null);
                    albumAudioTable.setForeground(Color.DARK_GRAY);
                    albumAudioTable.setBackground(Color.PINK);
                    albumScrollPane.getViewport().setBackground(Color.PINK);


                    for(int j = 0 ; j < AudioList.size() ; j++){
                        try{
                        if(new AudioTAG(AudioList.get(j).getPath()).getTagAlbum() == null){
                            albumModel.addRow(new Object[]{
                                new AudioTAG(AudioList.get(j).getPath()).getTagTitle(),
                                j
                            });
                        }
                        }catch(Exception e){}
                        
                    }
                    albumAudioTable.addMouseListener(new MouseAdapter() {
                        private void showPopup(MouseEvent evt){
                            if(evt.isPopupTrigger()){
                                final Point p       = evt.getPoint();
                                final int tempRow   = albumAudioTable.rowAtPoint(p);
                                System.out.println("Library ID : "+albumModel.getValueAt(tempRow, albumModel.findColumn("LibID")));
                                /*
                                 * Popup menu
                                 */
                                JPopupMenu tablePopup  = new JPopupMenu();

                                JMenuItem audioTitle = new JMenuItem("-"+(String)albumModel.getValueAt(tempRow,0)+"-");
                                audioTitle.setForeground(Color.MAGENTA);
                                audioTitle.setEnabled(false);
                                tablePopup.add(audioTitle);

                                for(int i = 0 ; i < playlistFiles.length ; i ++){
                                    if(playlistFiles[i].getName().endsWith(".list")){
                                        final int tempListNum = i;

                                        JMenuItem addPlaylist      = new JMenuItem("Add to ["+playlistFiles[i].getName().replaceAll(".list", "")+"]");
                                        addPlaylist.addMouseListener(new MouseAdapter(){
                                            public void mousePressed(MouseEvent evt){
                                                /*
                                                 * ADD to list
                                                 */
                                                try{
                                                    ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(playlistFiles[tempListNum]));
                                                    LinkedList<Integer> tempList = (LinkedList<Integer>)tempListRead.readObject();
                                                    tempListRead.close();

                                                    /**ADD**/
                                                    tempList.add((Integer)albumModel.getValueAt(tempRow, albumModel.findColumn("LibID")));

                                                    ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(playlistFiles[tempListNum],false));
                                                    playlistRegen.writeObject(tempList);
                                                    playlistRegen.close();

                                                    if(currentPlaylistName.equals(playlistFiles[tempListNum].getName().replaceAll(".list", ""))){
                                                        currentPlaylist.add((Integer)albumModel.getValueAt(tempRow, albumModel.findColumn("LibID")));
                                                    }

                                                    updatePlaylistStatus(playlistFiles[tempListNum].getName(),(String)albumModel.getValueAt(tempRow,0));
                                                }catch(Exception e){
                                                    /*
                                                     * Error
                                                     */
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        tablePopup.add(addPlaylist);
                                    }
                                }

                                tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                            }
                        }

                        public void mouseReleased(MouseEvent evt){
                            showPopup(evt);
                        }
                        
                        public void mouseClicked(MouseEvent evt) {
                            if(!albumAudioWaiting && evt.getClickCount() == 2){
                                albumAudioWaiting = true;
                                try{Thread.sleep(500);}catch(Exception e){}

                                currentPlaylistName = null;
                                currentPlaylist     = null;
                                
                                stop();
                                currentFileNumber  = (Integer)albumModel.getValueAt(albumAudioTable.rowAtPoint(evt.getPoint()),1);
                                albumAudioTable.clearSelection();
                                musicPlay();

                                /*Delay clicking time*/
                                try{Thread.sleep(1000);}catch(Exception e){}
                                albumAudioWaiting = false;
                            }
                        }
                    });
                    albumScrollPane.setViewportView(albumAudioTable);
                    albumContainer.add(albumScrollPane);
                    albumScrollPane.setBounds(-300, totalUsedHeight-110, 300, 110);
                    albumScrollPane.setVisible(false);

                    /*
                     * Background
                     */
                    final JLabel albumBkgd = new JLabel();
                    albumBkgd.setBackground(Color.PINK);
                    albumBkgd.setOpaque(true);
                    albumContainer.add(albumBkgd);
                    albumBkgd.setBounds(-590, totalUsedHeight-130, 590, 132);
                    albumBkgd.setVisible(false);

                    /*
                     * Add mouse listener to jbutton
                     */
                    albumCover.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            if(!albumTableWaiting){
                                albumTableWaiting = true;
                                try{Thread.sleep(5);}catch(Exception e){}

                                new Thread(new Runnable(){public void run(){
                                    int albumHeight     = albumCover.getY();
                                    int albumNameHeight = albumName.getY();
                                    int albumScrollHeight= albumScrollPane.getY();
                                    int albumBackground = albumBkgd.getY();

                                    for(int i = 10 ; i <= 320; i+= 10){
                                        albumCover.setLocation(i,albumHeight);
                                        albumName.setLocation(i+300,albumNameHeight);
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }

                                    albumBkgd.setVisible(true);
                                    for(int i = -600 ; i <= 0; i+= 50){
                                        albumBkgd.setLocation(i, albumBackground);
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }

                                    for(int j = -300 ; j <= 0; j += 10){
                                        albumName.setLocation(j, albumNameHeight - 128/2+10);
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }
                                    albumName.setLocation(5, albumNameHeight - 128/2+10);

                                    albumScrollPane.setVisible(true);
                                    albumScrollPane.setBounds(5, albumScrollHeight, 300, 110);

                                    /*
                                     * Reverse
                                     */
                                    for(int i = 0; i < 1000;i++){
                                        if(albumNoWaiting)break;
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }
                                    albumNoWaiting = false;

                                    for(int e = 10 ; e >= -325; e -= 25){
                                        albumScrollPane.setLocation(e, albumScrollHeight);
                                        try{Thread.sleep(5);}catch(Exception ex){}
                                    }
                                    albumScrollPane.setVisible(false);

                                    for(int j = 10 ; j >= -300; j -= 25){
                                        albumName.setLocation(j, albumNameHeight - 128/2+10);
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }

                                    for(int i = 0 ; i >= -600; i-= 100){
                                        albumBkgd.setLocation(i, albumBackground);
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }
                                    albumBkgd.setVisible(false);

                                    for(int i = 320 ; i >= 10; i-= 25){
                                        albumCover.setLocation(i,albumHeight);
                                        albumName.setLocation(i+300,albumNameHeight);
                                        try{Thread.sleep(5);}catch(Exception e){}
                                    }
                                    albumCover.setLocation(5,albumHeight);
                                    albumName.setLocation(128+50,albumNameHeight);

                                    albumTableWaiting = false;
                                }}).start();

                            }else
                                albumNoWaiting = true;
                        }
                    });


                    /*
                     * Finally
                     */
                    panelHeight = totalUsedHeight+5;
                    albumContainer.setPreferredSize(new Dimension(590,panelHeight));
                }catch(Exception e){
                    e.printStackTrace();
                }

                statusAlbumTabCheck = true;
                albumContainer.repaint();
                AlbumScrollPane.repaint();
            }
                
            loadingFrame.close();
            
        }}).start();
    }//GEN-LAST:event_btnAlbumActionPerformed

    /*
     * Uploading Song Action (with others networking function - Searching/ Downloading)
     * 
     * Sebastian Ko - 02/03/2013
     */
    private void btnUploadedSongsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadedSongsActionPerformed
        // TODO add your handling code here:
        this.AudioTabbedPane.setSelectedIndex(3);
        this.loginPanel.setVisible(false);
        this.TableTabBackground.setIcon(tab4bg);
        /*
         * Panel Select
         */
        ArtistLeftPanel.setVisible(false);
        AlbumLeftPanel.setVisible(false);
        PlayListSelectPanel.setVisible(false);
        
        /*
         * Uploaded panel control
         */
        this.uploadLoadingPanel.setVisible(true);
        this.UploadedMainPanel.setVisible(false);
        this.UploadSubPanel.setVisible(false);
        
        
        if(connection != null && onlineMode){
            UploadedLeftPanel.setVisible(true);
            uploadProcessLabel.setText("Loading....");
        
            try {
                UploadList = connection.receiveUploadList();
            } catch (ConnectionFailException ex) {
                this.uploadProcessLabel.setText("Connections Fail");
                onlineMode = false;
            }
            
        }else{
            this.uploadProcessLabel.setText("Please Login to use the function");
            this.loginPanel.setVisible(true);
            UploadedLeftPanel.setVisible(false);
        }
        
        new Thread(new Runnable(){public void run(){
        if(UploadList != null){
            final DefaultTableModel ulm = new UploadListModel();
            final JTable uploadTable = new JTable();
            uploadTable.setModel(ulm);
            uploadTable.setRowHeight(30);
            uploadTable.setSelectionBackground(Color.PINK);
            uploadTable.setTableHeader(null);
            uploadTable.getColumn("-").setCellRenderer(new UploadListRenderer());
            uploadTable.getColumn("-").setMaxWidth(40);
            uploadTable.getColumn("-").setResizable(false);
            uploadTable.getColumn("Title").setCellRenderer(new UploadListRenderer());
            uploadTable.removeColumn( uploadTable.getColumn("Song ID"));
            uploadScrollPane.getViewport().setBackground(new Color(255,255,255));
            uploadScrollPane.setViewportView(uploadTable);
            
            
            for(int i = 0 ; i < UploadList.size() ; i++){
                try{
                    if(UploadList.get(i).containCover())
                        ulm.addRow(new Object[]{
                            new ImageIcon(UploadList.get(i).getTagCover().getScaledInstance(30,30, Image.SCALE_SMOOTH)),
                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+UploadList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+UploadList.get(i).getTagArtist()+"</font></div></HTML>"),
                            UploadList.get(i).getSongID()
                            });
                    else
                        ulm.addRow(new Object[]{
                            smallCover,
                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+UploadList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+UploadList.get(i).getTagArtist()+"</font></div></HTML>"),
                            UploadList.get(i).getSongID()
                            });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            
            
            uploadTable.addMouseListener(new MouseAdapter(){    
                private void showPopup(MouseEvent evt){
                    if(evt.isPopupTrigger() && uploadTable.getSelectedRow() != -1){
                        /*
                         * Popup menu
                         */
                        JPopupMenu tablePopup  = new JPopupMenu();
                        JMenuItem tablePopupTit = new JMenuItem("You have select "+uploadTable.getSelectedRowCount()+ " item(s)");
                        tablePopupTit.setForeground(Color.MAGENTA);
                        tablePopupTit.setEnabled(false);
                        tablePopup.add(tablePopupTit);
                        
                        JMenuItem addLib      = new JMenuItem("Add to library");
                        addLib.addMouseListener(new MouseAdapter(){
                            public void mousePressed(MouseEvent evt){
                                int[] tempRows  = uploadTable.getSelectedRows();
                                
                                for(int i = 0 ; i < uploadTable.getSelectedRowCount() ; i++){
                                    if((Integer)ulm.getValueAt(tempRows[i], 2) == UploadList.get(tempRows[i]).getSongID()){
                                        int tempSize = AudioList.size();
                                        addToLibrary((Integer)ulm.getValueAt(tempRows[i],2),UploadList.get(tempRows[i]).getPath());
                                        if(tempSize < AudioList.size()){
                                            tableModel.addRow(new Object[]{UploadList.get(tempRows[i]).getTagTitle(),
                                                new durationToString(UploadList.get(tempRows[i]).getDurationInSec()).toString(),
                                                UploadList.get(tempRows[i]).getTagArtist(),
                                                UploadList.get(tempRows[i]).getTagAlbum()
                                            });
                                        }
                                    }
                                }
                                
                                if(downloadServicesStart)
                                    synchronized(downloadLock){
                                        downloadLock.notifyAll();
                                    }
                                else
                                    audioDownloadingQueue();
                            }
                        });
                        tablePopup.add(addLib);
                        
                        tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
                
                public void mouseReleased(MouseEvent evt){
                    showPopup(evt);
                }
            });
            
            
            /*
             * Uploaded Select Album Panel
             */
            LinkedList<String> uploadAlbum = new LinkedList<String>();
            LinkedList<String> uploadArtist = new LinkedList<String>();
            for(int i = 0 ; i < UploadList.size() ; i++){
                boolean check = false;
                if(uploadAlbum.size() == 0){
                    check = false;
                }else
                    for(int j = 0;j < uploadAlbum.size();j++){
                        if(UploadList.get(i).getTagAlbum().equals(uploadAlbum.get(j))){
                            check = true;break;
                        }
                    }
                
                if(!check && UploadList.get(i).containsTag() ){
                    uploadAlbum.add(UploadList.get(i).getTagAlbum());
                    uploadArtist.add(UploadList.get(i).getTagArtist());
                }
            }
            
            for(int i = 0 ; i < uploadAlbum.size() ; i++){
                System.out.println(">"+uploadAlbum.get(i)+"\\"+uploadArtist.get(i));
            }
            
            
            
            /*
             * Every song album container
             */
            uploadContainer.removeAll();
            uploadContainer.setLayout(null);
            
            final int UPLD_COVER_SIZE = 140;
            int totalWidth = 10;
            for(int i = 0 ; i < uploadAlbum.size() ; i++){
                boolean check = false;
                for(int j = 0 ; j < UploadList.size() ; j++){
                    if(UploadList.get(j).getTagAlbum().equals(uploadAlbum.get(i))){
                    if(UploadList.get(j).containCover()){
                    try{
                        check = true;

                        JButton publicCover = new JButton();
                        publicCover.setContentAreaFilled(false);
                        publicCover.setSize(UPLD_COVER_SIZE, UPLD_COVER_SIZE);
                        publicCover.setIcon(new ImageIcon(UploadList.get(j).getTagCover().getScaledInstance(UPLD_COVER_SIZE, UPLD_COVER_SIZE, Image.SCALE_SMOOTH)));
                        uploadContainer.add(publicCover);
                        publicCover.setBounds(totalWidth, 5, UPLD_COVER_SIZE, UPLD_COVER_SIZE);
                        
                        JLabel publicDetails = new JLabel("<html> <div style=\"white-space: nowrap;\"><b><font size=\"3\" color=\"#000000\">"+uploadAlbum.get(i)+"</font></b></div>"
                            + "<div style=\"white-space: nowrap;\"><b><font size=\"2\" color=\"#FF005A\">"+uploadArtist.get(i)+"</font></b></div></html>");
                        publicDetails.setSize(UPLD_COVER_SIZE,40);
                        uploadContainer.add(publicDetails);
                        publicDetails.setBounds(totalWidth, UPLD_COVER_SIZE+5, UPLD_COVER_SIZE, 40);
                        
                        totalWidth += UPLD_COVER_SIZE+20;
                        
                        /*
                         * Sub Panel Control
                         */
                        final int subCoverSize      = 160;
                        final ImageIcon subCover    = new ImageIcon(UploadList.get(j).getTagCover().getScaledInstance(subCoverSize, subCoverSize, Image.SCALE_SMOOTH));
                        final String subAlbum       = uploadAlbum.get(i);
                        final String subArtist      = uploadArtist.get(i);
                        final int perm              = UploadList.get(j).getPerm();
                        publicCover.addMouseListener(new MouseAdapter(){
                            @Override
                            public void mouseClicked(MouseEvent evt){
                                if(evt.getClickCount() == 2){
                                    uploadLoadingPanel.setVisible(true);
                                    UploadedMainPanel.setVisible(false);
                                    UploadSubPanel.setVisible(false);
                                    new Thread(new Runnable(){public void run(){
                                        UploadSubPanelContainer.removeAll();
                                        /*
                                         * Sub Panel Layout
                                         * Size : 590x370
                                         */
                                        //Cover
                                        JLabel cover = new JLabel();
                                        cover.setSize(subCoverSize,subCoverSize);
                                        cover.setIcon(subCover);
                                        UploadSubPanelContainer.add(cover);
                                        cover.setBounds(10, 40, subCoverSize, subCoverSize);
                                        
                                        //back button
                                        JButton backToMain = new JButton("Main Page");
                                        backToMain.setSize(60,20);
                                        backToMain.setBorder(null);
                                        backToMain.setContentAreaFilled(false);
                                        backToMain.setForeground(Color.red);
                                        UploadSubPanelContainer.add(backToMain);
                                        backToMain.setBounds(5, 5, 60, 20);
                                        
                                        backToMain.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent evt) {
                                                btnUploadedSongsActionPerformed(null);
                                            }
                                        });
                                        
                                        //back sub button
                                        JLabel btnTitle = new JLabel(" > "+subAlbum);
                                        btnTitle.setSize(500, 20);
                                        btnTitle.setForeground(Color.red);
                                        UploadSubPanelContainer.add(btnTitle);
                                        btnTitle.setBounds(65, 5, 500, 20);
                                        
                                        //graphic line 1
                                        JComponent line1 = new JComponent(){
                                            @Override
                                            public void paintComponent(Graphics g){
                                                super.paintComponent(g);
                                                Graphics2D g2d = (Graphics2D)g;
                                                g2d.setColor(new Color(255,153,153));
                                                g2d.fillRect(0, 0, 540, 2);
                                            }
                                        };
                                        UploadSubPanelContainer.add(line1);
                                        line1.setBounds(5, 140, 540, 2);
                                        
                                        //graphic album title
                                        JLabel TitleAlbum = new JLabel("<html><div style=\"width: 300px;\"><b><font size=\"5\" color=\"#000000\">"+subAlbum+"</font></b></div></html>");
                                        UploadSubPanelContainer.add(TitleAlbum);
                                        TitleAlbum.setBounds(15+subCoverSize, 140-(int)TitleAlbum.getPreferredSize().getHeight()-2, 300,(int)TitleAlbum.getPreferredSize().getHeight());
                                        
                                        //grahpic artist title
                                        JLabel TitleArtist = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"3\" color=\"#FF005A\">"+subArtist+"</font></b></div></html>");
                                        UploadSubPanelContainer.add(TitleArtist);
                                        TitleArtist.setBounds(15+subCoverSize, 142, 300, 20);
                                        
                                        //Panel 1 - Public Table
                                        final DefaultTableModel publicTm = new SubUploadCellModel();
                                        final JTable publicTable = new JTable();
                                        publicTable.setModel(publicTm);
                                        publicTable.setRowHeight(30);
                                        publicTable.setSelectionBackground(Color.PINK);
                                        publicTable.setTableHeader(null);
                                        publicTable.getColumn("Num").setMaxWidth(30);
                                        publicTable.removeColumn( publicTable.getColumn("SongID"));
                                       
                                        
                                        //Panel 2 - Private Table
                                        final DefaultTableModel privateTm = new SubUploadCellModel();
                                        final JTable privateTable = new JTable();
                                        privateTable.setModel(privateTm);
                                        privateTable.setRowHeight(30);
                                        privateTable.setSelectionBackground(Color.PINK);
                                        privateTable.setTableHeader(null);
                                        privateTable.getColumn("Num").setMaxWidth(30);
                                        privateTable.removeColumn( privateTable.getColumn("SongID"));

                                        //Add to 2 table
                                        int totalPublic  = 0;
                                        int totalPrivate = 0;
                                        for(int i = 0 ; i < UploadList.size() ; i++){
                                            try{
                                                if(UploadList.get(i).getTagAlbum().equals(subAlbum) && UploadList.get(i).getPerm() == AudioDetails.NET_PUBLIC)
                                                    publicTm.addRow(new Object[]{
                                                        ++totalPublic,
                                                        UploadList.get(i).getTagTitle(),
                                                        i
                                                        });
                                                else if(UploadList.get(i).getTagAlbum().equals(subAlbum) && UploadList.get(i).getPerm() == AudioDetails.NET_PRIVATE)
                                                    privateTm.addRow(new Object[]{
                                                        ++totalPrivate,
                                                        UploadList.get(i).getTagTitle(),
                                                        i
                                                        });
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                        
                                        /*
                                         * Table mouse listener
                                         */
                                        /*Public===============================================================================================================*/
                                        publicTable.addMouseListener(new MouseAdapter(){    
                                            private void showPopup(MouseEvent evt){
                                                if(evt.isPopupTrigger() && publicTable.getSelectedRow() != -1){
                                                    if(publicTable.getSelectedRow() != ((Integer)(publicTable.getValueAt(publicTable.rowAtPoint(evt.getPoint()), 0))-1) && publicTable.getSelectedRowCount() == 1){
                                                        publicTable.setRowSelectionInterval(publicTable.rowAtPoint(evt.getPoint()),publicTable.rowAtPoint(evt.getPoint()));
                                                    }
                                                    /*
                                                     * Popup menu
                                                     */
                                                    JPopupMenu tablePopup  = new JPopupMenu();
                                                    JMenuItem tablePopupTit = new JMenuItem();
                                                    if(publicTable.getSelectedRowCount() == 1){
                                                        tablePopupTit.setText("- "+publicTable.getValueAt(publicTable.rowAtPoint(evt.getPoint()), 1)+" -");
                                                    }
                                                    else tablePopupTit.setText("You have select "+publicTable.getSelectedRowCount()+ " items");
                                                    tablePopupTit.setForeground(Color.MAGENTA);
                                                    tablePopupTit.setEnabled(false);
                                                    tablePopup.add(tablePopupTit);

                                                    JMenuItem addLib      = new JMenuItem("Add to My library");
                                                    addLib.addMouseListener(new MouseAdapter(){
                                                        public void mousePressed(MouseEvent evt){
                                                            int[] tempRows  = publicTable.getSelectedRows();

                                                            for(int i = 0 ; i < publicTable.getSelectedRowCount() ; i++){
                                                                if(publicTm.getValueAt(tempRows[i], 1).equals(UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getTagTitle())){
                                                                    int tempSize = AudioList.size();
                                                                    addToLibrary(UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getSongID(),UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getPath());
                                                                    if(tempSize < AudioList.size()){
                                                                        tableModel.addRow(new Object[]{UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getTagTitle(),
                                                                            new durationToString(UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getDurationInSec()).toString(),
                                                                            UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getTagArtist(),
                                                                            UploadList.get((Integer)publicTm.getValueAt(tempRows[i], 2)).getTagAlbum()
                                                                        });
                                                                    }
                                                                }
                                                            }

                                                            if(downloadServicesStart)
                                                                synchronized(downloadLock){
                                                                    downloadLock.notifyAll();
                                                                }
                                                            else
                                                                audioDownloadingQueue();
                                                        }
                                                    });
                                                    tablePopup.add(addLib);

                                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                                }
                                                else if(evt.isPopupTrigger()){
                                                    publicTable.setRowSelectionInterval(publicTable.rowAtPoint(evt.getPoint()),publicTable.rowAtPoint(evt.getPoint()));
                                                    showPopup(evt);
                                                }
                                            }

                                            public void mouseReleased(MouseEvent evt){
                                                showPopup(evt);
                                            }
                                        });
                                        
                                        
                                        /*Private===============================================================================================================*/
                                        privateTable.addMouseListener(new MouseAdapter(){    
                                            private void showPopup(MouseEvent evt){
                                                if(evt.isPopupTrigger() && privateTable.getSelectedRow() != -1){
                                                    if(privateTable.getSelectedRow() != ((Integer)(privateTable.getValueAt(privateTable.rowAtPoint(evt.getPoint()), 0))-1) && privateTable.getSelectedRowCount() == 1){
                                                        privateTable.setRowSelectionInterval(privateTable.rowAtPoint(evt.getPoint()),privateTable.rowAtPoint(evt.getPoint()));
                                                    }
                                                    /*
                                                     * Popup menu
                                                     */
                                                    JPopupMenu tablePopup  = new JPopupMenu();
                                                    JMenuItem tablePopupTit = new JMenuItem();
                                                    if(privateTable.getSelectedRowCount() == 1){
                                                        tablePopupTit.setText("- "+privateTable.getValueAt(privateTable.rowAtPoint(evt.getPoint()), 1)+" -");
                                                    }
                                                    else tablePopupTit.setText("You have select "+privateTable.getSelectedRowCount()+ " items");
                                                    tablePopupTit.setForeground(Color.MAGENTA);
                                                    tablePopupTit.setEnabled(false);
                                                    tablePopup.add(tablePopupTit);

                                                    JMenuItem addLib      = new JMenuItem("Add to My library");
                                                    addLib.addMouseListener(new MouseAdapter(){
                                                        public void mousePressed(MouseEvent evt){
                                                            int[] tempRows  = privateTable.getSelectedRows();

                                                            for(int i = 0 ; i < privateTable.getSelectedRowCount() ; i++){
                                                                if(privateTm.getValueAt(tempRows[i], 1).equals(UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getTagTitle())){
                                                                    int tempSize = AudioList.size();
                                                                    addToLibrary(UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getSongID(),UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getPath());
                                                                    if(tempSize < AudioList.size()){
                                                                        tableModel.addRow(new Object[]{UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getTagTitle(),
                                                                            new durationToString(UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getDurationInSec()).toString(),
                                                                            UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getTagArtist(),
                                                                            UploadList.get((Integer)privateTm.getValueAt(tempRows[i], 2)).getTagAlbum()
                                                                        });
                                                                    }
                                                                }
                                                            }

                                                            if(downloadServicesStart)
                                                                synchronized(downloadLock){
                                                                    downloadLock.notifyAll();
                                                                }
                                                            else
                                                                audioDownloadingQueue();
                                                        }
                                                    });
                                                    tablePopup.add(addLib);

                                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                                }
                                                else if(evt.isPopupTrigger()){
                                                    privateTable.setRowSelectionInterval(privateTable.rowAtPoint(evt.getPoint()),privateTable.rowAtPoint(evt.getPoint()));
                                                    showPopup(evt);
                                                }
                                            }

                                            public void mouseReleased(MouseEvent evt){
                                                showPopup(evt);
                                            }
                                        });
                                        
                                        //Add to panel
                                        UploadSubPanelContainer.add(publicTable);
                                        publicTable.setBounds(20, 220, 500, (int)publicTable.getPreferredSize().getHeight());
                                        UploadSubPanelContainer.add(privateTable);
                                        privateTable.setBounds(20, 220, 500, (int)privateTable.getPreferredSize().getHeight());
                                        privateTable.setVisible(false);
                                        
                                        //swap perm page button
                                        final JButton swapButton = new JButton("Public");
                                        swapButton.setBorder(null);
                                        swapButton.setContentAreaFilled(false);
                                        swapButton.setHorizontalAlignment(SwingConstants.LEFT);
                                        swapButton.setForeground(Color.GRAY);
                                        UploadSubPanelContainer.add(swapButton);
                                        swapButton.setBounds(15+subCoverSize, 180, (int)swapButton.getPreferredSize().getWidth()+50, 20);
                                        
                                        swapButton.addActionListener(new ActionListener() {
                                            private boolean showPublic = true;
                                            
                                            public void actionPerformed(ActionEvent evt) {
                                                if(showPublic){
                                                    publicTable.setVisible(false);
                                                    privateTable.setVisible(true);
                                                    swapButton.setText("Private");
                                                    showPublic = false;
                                                }else{
                                                    privateTable.setVisible(false);
                                                    publicTable.setVisible(true);
                                                    swapButton.setText("Public");
                                                    showPublic = true;
                                                }
                                            }
                                        });
                                        
                                        
                                        //final size
                                        int TableSize = 0;
                                        if(publicTable.getHeight() > privateTable.getHeight())TableSize = publicTable.getHeight();
                                        else TableSize = privateTable.getHeight();
                                        
                                        //text total
                                        JLabel textTotal = new JLabel("Total "+(totalPublic+totalPrivate)+" Audio(s) in this album");
                                        textTotal.setForeground(Color.GRAY);
                                        UploadSubPanelContainer.add(textTotal);
                                        textTotal.setBounds(20, 230+TableSize, 500, 25);
                                        
                                        UploadSubPanelContainer.setPreferredSize(new Dimension(UploadSubPanel.getWidth(),240+TableSize+40));
                                        
                                        UploadSubPanel.setLocation(570, 0);
                                        uploadLoadingPanel.setVisible(false);
                                        UploadSubPanel.setVisible(true);
                                        for(int i = 600 ; i >= 0 ; i-= 50){
                                            UploadSubPanel.setLocation(i, 0);
                                            try{Thread.sleep(5);}catch(Exception e){}
                                        }
                                        for(int i = 0 ; i <= 50 ; i+= 10){
                                            UploadSubPanel.setLocation(i, 0);
                                            try{Thread.sleep(15);}catch(Exception e){}
                                        }
                                        for(int i = 50 ; i >= 0 ; i-= 10){
                                            UploadSubPanel.setLocation(i, 0);
                                            try{Thread.sleep(15);}catch(Exception e){}
                                        }
                                    }}).start();
                                }
                            }
                        });
                        
                        uploadContainer.setPreferredSize(new Dimension(totalWidth,uploadPublicPanel.getHeight()));
                        break;
                    }catch(Exception e){e.printStackTrace();}
                    }
                    }
                    
                }
            }
        }
        
        uploadLoadingPanel.setVisible(false);
        UploadedMainPanel.setVisible(true);
        }}).start();
            
    }//GEN-LAST:event_btnUploadedSongsActionPerformed

    private void uploadSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadSearchActionPerformed
        // TODO add your handling code here:
        if(connection != null && onlineMode){
            this.AudioTabbedPane.setSelectedIndex(4);
            loadingFrame.popout();
            new Thread(new Runnable(){public void run(){
                try {
                    LinkedList<AudioDetails> tempNameList   = connection.searchAudioByName(upldSearchValue.getText());
                    LinkedList<AudioDetails> tempArtistList = connection.searchAudioByArtist(upldSearchValue.getText());
                    LinkedList<AudioDetails> tempAlbumList  = connection.searchAudioByAlbum(upldSearchValue.getText());

                    searchContainer.removeAll();
                    searchContainer.setBackground(Color.white);
                    searchContainer.setPreferredSize(new Dimension(590,150));
                    int totalSearchHeight = 0;
                    /*
                     * Audio Title List
                     */
                    if(tempNameList.size() != 0){
                        JLabel searchNameTitle  = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">Searched by Name</font></b></div><hr width=560 size=1 Noshade align=left></html>");
                        searchContainer.add(searchNameTitle);
                        searchNameTitle.setBounds(0, totalSearchHeight+2, 590, 30);
                        totalSearchHeight += 2+30;

                        final JTable searchNameTable         = new JTable();
                        final DefaultTableModel ulm          = new UploadListModel();
                        searchNameTable.setModel(ulm);
                        searchNameTable.setRowHeight(40);
                        searchNameTable.setSelectionBackground(Color.PINK);
                        searchNameTable.setTableHeader(null);
                        searchNameTable.getColumn("-").setCellRenderer(new UploadListRenderer());
                        searchNameTable.getColumn("-").setMaxWidth(40);
                        searchNameTable.getColumn("-").setResizable(false);
                        searchNameTable.getColumn("Title").setCellRenderer(new UploadListRenderer());
                        searchNameTable.removeColumn(searchNameTable.getColumn("Song ID"));

                        for(int i = 0 ; i < tempNameList.size() ; i++){
                            try{
                                if(tempNameList.get(i).containCover())
                                    ulm.addRow(new Object[]{
                                            new ImageIcon(tempNameList.get(i).getTagCover().getScaledInstance(30,30, Image.SCALE_SMOOTH)),
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+tempNameList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+tempNameList.get(i).getTagArtist()+"</font></div></HTML>"),
                                            tempNameList.get(i).getSongID()
                                            });
                                    else
                                        ulm.addRow(new Object[]{
                                            smallCover,
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+tempNameList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+tempNameList.get(i).getTagArtist()+"</font></div></HTML>"),
                                            tempNameList.get(i).getSongID()
                                            });
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        
                        searchNameTable.addMouseListener(new MouseAdapter(){    
                            private void showPopup(MouseEvent evt){
                                if(evt.isPopupTrigger() && searchNameTable.getSelectedRow() != -1){
                                    /*
                                     * Popup menu
                                     */
                                    JPopupMenu tablePopup  = new JPopupMenu();
                                    JMenuItem tablePopupTit = new JMenuItem("You have select "+searchNameTable.getSelectedRowCount()+ " item(s)");
                                    tablePopupTit.setForeground(Color.MAGENTA);
                                    tablePopupTit.setEnabled(false);
                                    tablePopup.add(tablePopupTit);

                                    JMenuItem addLib      = new JMenuItem("Add to library");
                                    addLib.addMouseListener(new MouseAdapter(){
                                        public void mousePressed(MouseEvent evt){
                                            int[] tempRows  = searchNameTable.getSelectedRows();

                                            for(int i = 0 ; i < searchNameTable.getSelectedRowCount() ; i++){
                                                if((Integer)ulm.getValueAt(tempRows[i], 2) == UploadList.get(tempRows[i]).getSongID()){
                                                    int tempSize = AudioList.size();
                                                    addToLibrary((Integer)ulm.getValueAt(tempRows[i],2),UploadList.get(tempRows[i]).getPath());
                                                    if(tempSize < AudioList.size()){
                                                        tableModel.addRow(new Object[]{UploadList.get(tempRows[i]).getTagTitle(),
                                                            new durationToString(UploadList.get(tempRows[i]).getDurationInSec()).toString(),
                                                            UploadList.get(tempRows[i]).getTagArtist(),
                                                            UploadList.get(tempRows[i]).getTagAlbum()
                                                        });
                                                    }
                                                }
                                            }
                                            
                                            if(downloadServicesStart)
                                                synchronized(downloadLock){
                                                    downloadLock.notifyAll();
                                                }
                                            else
                                                audioDownloadingQueue();
                                        }
                                    });
                                    tablePopup.add(addLib);

                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }

                            public void mouseReleased(MouseEvent evt){
                                showPopup(evt);
                            }
                        });
                        

                        searchContainer.add(searchNameTable);
                        searchNameTable.setBounds(0, totalSearchHeight + 5, 560, (int)searchNameTable.getPreferredSize().getHeight());
                        totalSearchHeight += searchNameTable.getPreferredSize().getHeight()+10;

                        searchContainer.setPreferredSize(new Dimension(590,totalSearchHeight));
                    }



                    /*
                     * Audio Artist List
                     */
                    if(tempArtistList.size() != 0 && !upldSearchValue.getText().equals("")){
                        JLabel searchArtistTitle  = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">Searched by Artist</font></b></div><hr width=560 size=1 Noshade align=left></html>");
                        searchContainer.add(searchArtistTitle);
                        searchArtistTitle.setBounds(0, totalSearchHeight+2, 590, 30);
                        totalSearchHeight += 2+30;

                        final JTable searchArtistTable         = new JTable();
                        final DefaultTableModel ulm            = new UploadListModel();
                        searchArtistTable.setModel(ulm);
                        searchArtistTable.setRowHeight(40);
                        searchArtistTable.setSelectionBackground(Color.PINK);
                        searchArtistTable.setTableHeader(null);
                        searchArtistTable.getColumn("-").setCellRenderer(new UploadListRenderer());
                        searchArtistTable.getColumn("-").setMaxWidth(40);
                        searchArtistTable.getColumn("-").setResizable(false);
                        searchArtistTable.getColumn("Title").setCellRenderer(new UploadListRenderer());
                        searchArtistTable.removeColumn(searchArtistTable.getColumn("Song ID"));

                        for(int i = 0 ; i < tempArtistList.size() ; i++){
                            try{
                                if(tempArtistList.get(i).containCover())
                                    ulm.addRow(new Object[]{
                                            new ImageIcon(tempArtistList.get(i).getTagCover().getScaledInstance(30,30, Image.SCALE_SMOOTH)),
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+tempArtistList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+tempArtistList.get(i).getTagArtist()+"</font></div></HTML>"),
                                            tempArtistList.get(i).getSongID()
                                            });
                                    else
                                        ulm.addRow(new Object[]{
                                            smallCover,
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+tempArtistList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+tempArtistList.get(i).getTagArtist()+"</font></div></HTML>"),
                                            tempArtistList.get(i).getSongID()
                                            });
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        
                        searchArtistTable.addMouseListener(new MouseAdapter(){    
                            private void showPopup(MouseEvent evt){
                                if(evt.isPopupTrigger() && searchArtistTable.getSelectedRow() != -1){
                                    /*
                                     * Popup menu
                                     */
                                    JPopupMenu tablePopup  = new JPopupMenu();
                                    JMenuItem tablePopupTit = new JMenuItem("You have select "+searchArtistTable.getSelectedRowCount()+ " item(s)");
                                    tablePopupTit.setForeground(Color.MAGENTA);
                                    tablePopupTit.setEnabled(false);
                                    tablePopup.add(tablePopupTit);

                                    JMenuItem addLib      = new JMenuItem("Add to library");
                                    addLib.addMouseListener(new MouseAdapter(){
                                        public void mousePressed(MouseEvent evt){
                                            int[] tempRows  = searchArtistTable.getSelectedRows();

                                            for(int i = 0 ; i < searchArtistTable.getSelectedRowCount() ; i++){
                                                if((Integer)ulm.getValueAt(tempRows[i], 2) == UploadList.get(tempRows[i]).getSongID()){
                                                    int tempSize = AudioList.size();
                                                    addToLibrary((Integer)ulm.getValueAt(tempRows[i],2),UploadList.get(tempRows[i]).getPath());
                                                    if(tempSize < AudioList.size()){
                                                        tableModel.addRow(new Object[]{UploadList.get(tempRows[i]).getTagTitle(),
                                                            new durationToString(UploadList.get(tempRows[i]).getDurationInSec()).toString(),
                                                            UploadList.get(tempRows[i]).getTagArtist(),
                                                            UploadList.get(tempRows[i]).getTagAlbum()
                                                        });
                                                    }
                                                }
                                            }
                                            
                                            if(downloadServicesStart)
                                                synchronized(downloadLock){
                                                    downloadLock.notifyAll();
                                                }
                                            else
                                                audioDownloadingQueue();
                                        }
                                    });
                                    tablePopup.add(addLib);

                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }

                            public void mouseReleased(MouseEvent evt){
                                showPopup(evt);
                            }
                        });

                        searchContainer.add(searchArtistTable);
                        searchArtistTable.setBounds(0, totalSearchHeight + 5, 560, (int)searchArtistTable.getPreferredSize().getHeight());
                        totalSearchHeight += searchArtistTable.getPreferredSize().getHeight()+10;

                        searchContainer.setPreferredSize(new Dimension(590,totalSearchHeight));
                    }



                    /*
                     * Audio Album List
                     */
                    if(tempAlbumList.size() != 0 && !upldSearchValue.getText().equals("")){
                        JLabel searchAlbumTitle  = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">Searched by Album</font></b></div><hr width=560 size=1 Noshade align=left></html>");
                        searchContainer.add(searchAlbumTitle);
                        searchAlbumTitle.setBounds(0, totalSearchHeight+2, 590, 30);
                        totalSearchHeight += 2+30;

                        final JTable searchAlbumTable         = new JTable();
                        final DefaultTableModel ulm            = new UploadListModel();
                        searchAlbumTable.setModel(ulm);
                        searchAlbumTable.setRowHeight(40);
                        searchAlbumTable.setSelectionBackground(Color.PINK);
                        searchAlbumTable.setTableHeader(null);
                        searchAlbumTable.getColumn("-").setCellRenderer(new UploadListRenderer());
                        searchAlbumTable.getColumn("-").setMaxWidth(40);
                        searchAlbumTable.getColumn("-").setResizable(false);
                        searchAlbumTable.getColumn("Title").setCellRenderer(new UploadListRenderer());
                        searchAlbumTable.removeColumn(searchAlbumTable.getColumn("Song ID"));

                        for(int i = 0 ; i < tempAlbumList.size() ; i++){
                            try{
                                if(tempAlbumList.get(i).containCover())
                                    ulm.addRow(new Object[]{
                                            new ImageIcon(tempAlbumList.get(i).getTagCover().getScaledInstance(30,30, Image.SCALE_SMOOTH)),
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+tempAlbumList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+tempAlbumList.get(i).getTagArtist()+"</font></div></HTML>"),
                                            tempAlbumList.get(i).getSongID()
                                            });
                                    else
                                        ulm.addRow(new Object[]{
                                            smallCover,
                                            new JLabel("<HTML><div style=\"white-space: nowrap;\"><b><font size=\"3\">"+tempAlbumList.get(i).getTagTitle()+"</font></b><BR><font size=\"2\">"+tempAlbumList.get(i).getTagArtist()+"</font></div></HTML>"),
                                            tempAlbumList.get(i).getSongID()
                                            });
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        
                        searchAlbumTable.addMouseListener(new MouseAdapter(){    
                            private void showPopup(MouseEvent evt){
                                if(evt.isPopupTrigger() && searchAlbumTable.getSelectedRow() != -1){
                                    /*
                                     * Popup menu
                                     */
                                    JPopupMenu tablePopup  = new JPopupMenu();
                                    JMenuItem tablePopupTit = new JMenuItem("You have select "+searchAlbumTable.getSelectedRowCount()+ " item(s)");
                                    tablePopupTit.setForeground(Color.MAGENTA);
                                    tablePopupTit.setEnabled(false);
                                    tablePopup.add(tablePopupTit);

                                    JMenuItem addLib      = new JMenuItem("Add to library");
                                    addLib.addMouseListener(new MouseAdapter(){
                                        public void mousePressed(MouseEvent evt){
                                            int[] tempRows  = searchAlbumTable.getSelectedRows();

                                            for(int i = 0 ; i < searchAlbumTable.getSelectedRowCount() ; i++){
                                                if((Integer)ulm.getValueAt(tempRows[i], 2) == UploadList.get(tempRows[i]).getSongID()){
                                                    int tempSize = AudioList.size();
                                                    addToLibrary((Integer)ulm.getValueAt(tempRows[i],2),UploadList.get(tempRows[i]).getPath());
                                                    if(tempSize < AudioList.size()){
                                                        tableModel.addRow(new Object[]{UploadList.get(tempRows[i]).getTagTitle(),
                                                            new durationToString(UploadList.get(tempRows[i]).getDurationInSec()).toString(),
                                                            UploadList.get(tempRows[i]).getTagArtist(),
                                                            UploadList.get(tempRows[i]).getTagAlbum()
                                                        });
                                                    }
                                                }
                                            }
                                            
                                            if(downloadServicesStart)
                                                synchronized(downloadLock){
                                                    downloadLock.notifyAll();
                                                }
                                            else
                                                audioDownloadingQueue();
                                        }
                                    });
                                    tablePopup.add(addLib);

                                    tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }

                            public void mouseReleased(MouseEvent evt){
                                showPopup(evt);
                            }
                        });

                        searchContainer.add(searchAlbumTable);
                        searchAlbumTable.setBounds(0, totalSearchHeight + 5, 560, (int)searchAlbumTable.getPreferredSize().getHeight());
                        totalSearchHeight += searchAlbumTable.getPreferredSize().getHeight()+10;

                        searchContainer.setPreferredSize(new Dimension(590,totalSearchHeight));
                    }

                    if(tempNameList.size() == 0 && tempArtistList.size() == 0 && tempAlbumList.size() == 0){
                        JLabel noresult  = new JLabel("<html><div style=\"white-space: nowrap;\"><b><font size=\"4\">Oops! No result found!</font></b></div></html>");
                        searchContainer.add(noresult);
                        noresult.setBounds(2, 2, 590, 30);
                    }

                    searchContainer.repaint();
                    SearchedPanel.repaint();
                } catch (ConnectionFailException ex) {
                    ex.printStackTrace();
                }
                
                loadingFrame.close();
            }}).start();
        }else
            controlMsg.setText("Connection was lost. Please Login");
    }//GEN-LAST:event_uploadSearchActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        
        loadingFrame.popout();
        new Thread(new Runnable(){public void run(){
            
            String logName = logUsername.getText();
            String logPass = new String(logPassword.getPassword());
            
            //Try to disconnect difference server
            if(connection != null){
                connection.DisconnectCentral();
                connection.DisconnectMedia();
            }
            
            connection = new Client(CentralServerIP,17220);

            if(connection.ConnectCentral() == 0){
                controlMsg.setText("Connection Start");
                
                if(connection.Login(logName, logPass) == 0){
                    controlMsg.setText("Login Success");
                    welcomeLabel.setText("Welcome ! "+logName);
                    setUserName(logName);
                    
                    /*
                     * Save to file
                     */
                    if(saveLoginFile(logName,logPass) == -1)controlMsg.setText("Fail to save user information (E:01)");       //Error Code 01
                    
                    NETWORK_ERROR = false;
                    onlineMode      = true;
                    
                    /*
                     * Add Queue
                     */
                    if(downList == null){
                        downList = new LinkedList<Integer>();
                    }
                    
                    tableModel = new DefaultListModel();
                    tabList.setModel(tableModel);
                    for(int i = 0 ; i < AudioList.size(); i++){
                        try {
                            if(AudioList.get(i).getType() == OFFLINE_MODE){
                                AudioTAG playAudioTAG = new AudioTAG(AudioList.get(i).getPath());
                                tableModel.addRow(new Object[]{playAudioTAG.getTagTitle(),new durationToString(AudioList.get(i).getDuration()).toString(),playAudioTAG.getTagArtist(),playAudioTAG.getTagAlbum()});
                            }else
                                if(onlineMode){
                                    AudioDetails au = (AudioDetails)connection.GetAudioDetails(AudioList.get(i).getNetID());

                                    tableModel.addRow(new Object[]{au.getTagTitle(),new durationToString(au.getDurationInSec()).toString(),au.getTagArtist(),au.getTagAlbum()});

                                    downList.add(au.getSongID());
                                    
                                    try{
                                        if(downloadServicesStart)
                                            synchronized(downloadLock){
                                                downloadLock.notifyAll();
                                            }
                                        else
                                            audioDownloadingQueue();
                                    }catch(Exception e){}
                                }else{
                                    tableModel.addRow(new Object[]{"Network files","-","-","-"});
                                }
                        } catch (Exception ex) {}
                    }
                }else {
                    controlMsg.setText("Login Fail");
                    welcomeLabel.setText("Click \"Login\" to Login");
                    onlineMode = false;
                    
                    loadingFrame.close();
                    new errPopUp("Login Fail, Please try again later").popup();
                }
                
                btnUploadedSongsActionPerformed(null);
                loadingFrame.close();
            }else{
                onlineMode = false;
                connection = null;
                welcomeLabel.setText("No connections");
                controlMsg.setText("Connections Fail");
                
                loadingFrame.close();
                new errPopUp("Connection Fail, Please try again later").popup();
                btnAllSongActionPerformed(null);
            }
            
        }}).start();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnCurrentPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCurrentPlaylistActionPerformed
        // TODO add your handling code here:
        this.showCurrentPlayList();
    }//GEN-LAST:event_btnCurrentPlaylistActionPerformed

    private void btnRandomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRandomActionPerformed
        // TODO add your handling code here:
        if(!random){
            random = true;
            btnRandom.setIcon(btnRandomIcon);
            if(currentPlaylistName != null){
                Collections.shuffle(currentPlaylist);
            }else{
                currentPlaylist = new LinkedList<Integer>();
                for(int i = 0 ; i < AudioList.size() ; i++){
                    currentPlaylist.add(i);
                }
                Collections.shuffle(currentPlaylist);
                currentPlaylistName = "#All Songs";
            }
        }else{
            random = false;
            if(!currentPlaylistName.equals("#All Songs")){
                try{
                    ObjectInputStream tempListRead = new ObjectInputStream(new FileInputStream(new File(userPlaylistPath+currentPlaylistName+".list")));
                    currentPlaylist = (LinkedList<Integer>)tempListRead.readObject();
                    tempListRead.close();
                }catch(Exception e){
                    e.printStackTrace();
                    currentPlaylistName = null;
                    currentPlaylist     = null;
                }
            }else{
                currentPlaylistName = null;
                currentPlaylist     = null;
            }
        }
            
    }//GEN-LAST:event_btnRandomActionPerformed

    private void btnSettingMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSettingMouseEntered
        // TODO add your handling code here:
        btnSetting.setForeground(new Color(255,0,90));
    }//GEN-LAST:event_btnSettingMouseEntered

    private void btnSettingMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSettingMouseExited
        // TODO add your handling code here:
        btnSetting.setForeground(Color.BLACK);
    }//GEN-LAST:event_btnSettingMouseExited

    private void btnSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingActionPerformed
        // TODO add your handling code here:
        if(ps == null || !ps.isDisplayable())ps = new PlayerSetting();
    }//GEN-LAST:event_btnSettingActionPerformed

    private void btnCurrentPlaylistMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCurrentPlaylistMouseEntered
        // TODO add your handling code here:
        btnCurrentPlaylist.setForeground(Color.WHITE);
    }//GEN-LAST:event_btnCurrentPlaylistMouseEntered

    private void btnCurrentPlaylistMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCurrentPlaylistMouseExited
        // TODO add your handling code here:
        btnCurrentPlaylist.setForeground(Color.lightGray);
    }//GEN-LAST:event_btnCurrentPlaylistMouseExited

    private void btnCurrentPlaylistMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCurrentPlaylistMousePressed
        // TODO add your handling code here:
        btnCurrentPlaylist.setForeground(Color.GRAY);
    }//GEN-LAST:event_btnCurrentPlaylistMousePressed

    private void btnCurrentPlaylistMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCurrentPlaylistMouseReleased
        // TODO add your handling code here:
        btnCurrentPlaylist.setForeground(Color.WHITE);
    }//GEN-LAST:event_btnCurrentPlaylistMouseReleased

    private void btnUploadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUploadMouseEntered
        // TODO add your handling code here:
        btnUpload.setForeground(Color.WHITE);
    }//GEN-LAST:event_btnUploadMouseEntered

    private void btnUploadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUploadMouseExited
        // TODO add your handling code here:
        btnUpload.setForeground(Color.lightGray);
    }//GEN-LAST:event_btnUploadMouseExited

    private void btnUploadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUploadMousePressed
        // TODO add your handling code here:
        btnUpload.setForeground(Color.GRAY);
    }//GEN-LAST:event_btnUploadMousePressed

    private void btnUploadMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUploadMouseReleased
        // TODO add your handling code here:
        btnUpload.setForeground(Color.WHITE);
    }//GEN-LAST:event_btnUploadMouseReleased

    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadActionPerformed
        // TODO add your handling code here:
        if(connection != null && onlineMode){
            /*
             * Do uploading
             * (Popup new Frame)
             */
            if(uploadFrame == null){
                new Thread(new Runnable(){public void run(){
                    uploadFrame       = new UploadAudioFrame(connection,AudioList);
                }}).start();
            }else if(uploadFrame != null && !uploadFrame.isShowing()){
                uploadFrame.dispose();
                new Thread(new Runnable(){public void run(){
                    uploadFrame       = new UploadAudioFrame(connection,AudioList);
                }}).start();
            }
            stop();
        }else{
            btnUploadedSongsActionPerformed(null);
        }
    }//GEN-LAST:event_btnUploadActionPerformed

    private void btnPlayMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPlayMousePressed
        // TODO add your handling code here:
        if(pause)this.btnPlay.setIcon(btnPlayHoverIcon);
        else this.btnPlay.setIcon(btnPauseHoverIcon);
    }//GEN-LAST:event_btnPlayMousePressed

    private void btnBackMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMousePressed
        // TODO add your handling code here:
        this.btnBack.setIcon(btnBackHoverIcon);
    }//GEN-LAST:event_btnBackMousePressed

    private void btnNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNextMousePressed
        // TODO add your handling code here:
        this.btnNext.setIcon(btnNextHoverIcon);
    }//GEN-LAST:event_btnNextMousePressed

    private void btnBackMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseReleased
        // TODO add your handling code here:
        this.btnBack.setIcon(btnBackIcon);
    }//GEN-LAST:event_btnBackMouseReleased

    private void btnNextMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNextMouseReleased
        // TODO add your handling code here:
        this.btnNext.setIcon(btnNextIcon);
    }//GEN-LAST:event_btnNextMouseReleased

    private void btnPlayMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPlayMouseReleased
        // TODO add your handling code here:
        if(pause)this.btnPlay.setIcon(btnPlayIcon);
        else this.btnPlay.setIcon(btnPauseIcon);
    }//GEN-LAST:event_btnPlayMouseReleased

    private void btnMiniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMiniActionPerformed
        // TODO add your handling code here:
        try{
            playBack                            = null;
            time                              = null;
            Control.dispose();Control             = null;
            ps.dispose();ps                    = null;
            cursor                            = null;
            loadingFrame.dispose();loadingFrame    = null;
            connection                        = null;
            uploadFrame.dispose();uploadFrame     = null;
            UploadList                        = null;
            ArtistList                        = null;
            AlbumList                         = null;
            rg.dispose();rg                    = null;
            playlistFiles                     = null;
            tableModel                        = null;
            System.out.println("Frame reinitialize with no error");
        }catch(Exception e){}
        this.dispose();
        FYPclosed = true;
    }//GEN-LAST:event_btnMiniActionPerformed

    private void btnRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegActionPerformed
        // TODO add your handling code here:
        if(rg == null || !rg.isDisplayable()){
            rg = new RegForm();
            rg.setVisible(true);
        }
    }//GEN-LAST:event_btnRegActionPerformed

    private void btnRegMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRegMousePressed
        // TODO add your handling code here:
        btnReg.setForeground(Color.BLACK);
    }//GEN-LAST:event_btnRegMousePressed

    private void btnRegMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRegMouseReleased
        // TODO add your handling code here:
        btnReg.setForeground(new Color(153,153,255));
    }//GEN-LAST:event_btnRegMouseReleased

    private void btnRandomMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRandomMousePressed
        // TODO add your handling code here:
        if(!random){
            btnRandom.setIcon(btnNotRandomHoverIcon);
        }else
            btnRandom.setIcon(btnRandomHoverIcon);
    }//GEN-LAST:event_btnRandomMousePressed

    private void btnRandomMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRandomMouseReleased
        // TODO add your handling code here:
        if(!random){
            btnRandom.setIcon(btnNotRandomIcon);
        }else
            btnRandom.setIcon(btnRandomIcon);
    }//GEN-LAST:event_btnRandomMouseReleased

    private void btnLoopMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLoopMousePressed
        // TODO add your handling code here:
        if(loop==0){
            btnLoop.setIcon(btnLoopHoverIcon);
        }else if(loop==1){
            btnLoop.setIcon(btnLoopAllHoverIcon);
        }else{
            btnLoop.setIcon(btnLoopOnceHoverIcon);
        }
    }//GEN-LAST:event_btnLoopMousePressed

    private void btnLoopMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLoopMouseReleased
        // TODO add your handling code here:
        if(loop==0){
            btnLoop.setIcon(btnLoopIcon);
        }else if(loop==1){
            btnLoop.setIcon(btnLoopAllIcon);
        }else{
            btnLoop.setIcon(btnLoopOnceIcon);
        }
    }//GEN-LAST:event_btnLoopMouseReleased

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        // TODO add your handling code here:
        if(ps == null || !ps.isDisplayable())ps = new PlayerSetting();
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnAddAudioOpenMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddAudioOpenMousePressed
        // TODO add your handling code here:
        btnAddAudioOpen.setForeground(Color.GRAY);
    }//GEN-LAST:event_btnAddAudioOpenMousePressed

    private void btnAddAudioOpenMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddAudioOpenMouseReleased
        // TODO add your handling code here:
        btnAddAudioOpen.setForeground(Color.BLACK);
    }//GEN-LAST:event_btnAddAudioOpenMouseReleased

    private void btnAddAudioOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAudioOpenActionPerformed
        // TODO add your handling code here:
        btnOpenActionPerformed(null);
    }//GEN-LAST:event_btnAddAudioOpenActionPerformed

    private void btnOpenMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOpenMousePressed
        // TODO add your handling code here:
        btnOpen.setForeground(Color.GRAY);
    }//GEN-LAST:event_btnOpenMousePressed

    private void btnOpenMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOpenMouseReleased
        // TODO add your handling code here:
        btnOpen.setForeground(Color.WHITE);
    }//GEN-LAST:event_btnOpenMouseReleased

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
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FYPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
       
        Runnable r=new Runnable() {
            public void run() {
                new FYPlayer().setVisible(true);                
            }
        };
       
        java.awt.EventQueue.invokeLater(r);
        
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
                if(tempElement.getType() == OFFLINE_MODE){
                    if(tempElement.getPath() != null){
                        try{
                            tempElement.setImagePath(new AudioImage().ImageIconToFile(new AudioTAG(tempElement.getPath()).getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH),"png",".\\cache\\"));
                        }catch(Exception e){
                            /*DO NOTHING*/
                        }
                    }
                }
                
                if(tempElement.getType() == ONLINE_MODE){
                    downList.add(tempElement.getNetID());
                }
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
            
            if(check){
                if(tempElement.getType() == OFFLINE_MODE){
                    if(tempElement.getPath() != null){
                        try{
                            tempElement.setImagePath(new AudioImage().ImageIconToFile(new AudioTAG(tempElement.getPath()).getTagCover().getScaledInstance(25,25, Image.SCALE_SMOOTH),"png",".\\cache\\"));
                        }catch(Exception e){
                            /*DO NOTHING*/
                        }
                    }
                }
                
                if(tempElement.getType() == ONLINE_MODE){
                    downList.add(tempElement.getNetID());
                }
                
                AudioList.add(tempElement);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    private void stop(){
        if(!stop && !FYPclosed){
            stop        = true;
            userControlled = true;
            try{player.close();}catch(Exception e){e.printStackTrace();}
            
            System.out.println("Player stopped by user");

            player      = null;
            playBack    = null;
        }
    }
    
    
    private void musicPlay(){
        playBack = new Thread(new Runnable(){public void run(){
            stop = false;    
            if(AudioList != null){
                try{
                    audioPlayback();
                }catch(IndexOutOfBoundsException e){
                    currentFileNumber = 0;
                    isPlaying = false;
                }finally{
                    pause = true;
                    stop = true;
                    btnPlay.setIcon(btnPlayIcon);
                }
            }
        }});
        playBack.start();
    }
    
    private void audioPlayback(){
        try{
            while(!stop && !FYPclosed){
                System.out.println("Playing At List Position >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+currentFileNumber);
                stop             = false;
                isPlaying         = true;
                pause            = false;
                String playAudioPath = "";
                
                userControlled = false;

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

                
                System.out.println("play path:"+playAudioPath);
                player = new MSWPlayer(playAudioPath,defaultVolume);              
                time=new durationToString(player.getDurationInSec()).toString();

                if(currentPlaylistName == null){
                    Control.setSong(new File(AudioList.get(currentFileNumber).getPath()),time);
                    playingTitle = new AudioTAG(AudioList.get(currentFileNumber).getPath()).getTagTitle();
                }else{
                    Control.setSong(new File(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath()),time);
                    playingTitle = new AudioTAG(AudioList.get(currentPlaylist.get(currentFileNumber)).getPath()).getTagTitle();
                }


                this.btnPlay.setIcon(this.btnPauseIcon);
                if(currentPlaylistName != null){
                    setPlaying(currentFileNumber);
                }


                new Thread(new Runnable(){public void run(){
                    String playAudioPath = null;

                    if(currentPlaylistName == null){
                        if(AudioList.get(currentFileNumber).getType() == 'N'){  //Type 'N' Network
                            playAudioPath = songPath + AudioList.get(currentFileNumber).getPath().replaceAll("MSP:","");
                        }else   //Type 'O' Offline
                            playAudioPath = AudioList.get(currentFileNumber).getPath();
                    }else{
                        if(AudioList.get(currentPlaylist.get(currentFileNumber)).getType() == 'N'){  //Type 'N' Network
                            playAudioPath = songPath + AudioList.get(currentPlaylist.get(currentFileNumber)).getPath().replaceAll("MSP:","");
                        }else   //Type 'O' Offline
                            playAudioPath = AudioList.get(currentPlaylist.get(currentFileNumber)).getPath();

                    }

                    /*
                     * Read Cover from Audio
                     */
                    Image tempCover = null;
                    try{
                        tempCover = new AudioTAG(playAudioPath).getTagCover();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    if(tempCover != null){
                        tempCover = tempCover.getScaledInstance(40,40, Image.SCALE_SMOOTH);
                        ImageIcon coverIcon = new ImageIcon(tempCover);
                        btnDetail.setIcon(coverIcon);
                    }else
                        btnDetail.setIcon(detailsCover);

                }}).start();

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


                if(!userControlled){
                    if(loop == 2);
                    else currentFileNumber++;
                }
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
            if(currentPlaylistName == null){
                if(AudioList.get(currentFileNumber).getType() == 'N'){  //Type 'N' Network
                    if(onlineMode){
                        System.err.println("Redownload network file");
                        downList.addFirst(AudioList.get(currentFileNumber).getNetID());
                        
                        if(this.downloadServicesStart)
                            synchronized(downloadLock){
                                downloadLock.notifyAll();
                            }
                        else
                            audioDownloadingQueue();
                    }
                }
            }else{
                if(AudioList.get(currentPlaylist.get(currentFileNumber)).getType() == 'N'){  //Type 'N' Network
                    if(onlineMode){
                        System.err.println("Redownload network file");
                        downList.addFirst(AudioList.get(currentPlaylist.get(currentFileNumber)).getNetID());
                        
                        if(this.downloadServicesStart)
                            synchronized(downloadLock){
                                downloadLock.notifyAll();
                            }
                        else
                            audioDownloadingQueue();
                    }
                }
            }
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
            currentFileNumber = 0;
            isPlaying = false;
        }catch(Exception e){
            e.printStackTrace();
            isPlaying = false;
        }
        
        if(loop == 1){
            if(currentPlaylistName == null && currentFileNumber == AudioList.size()) currentFileNumber = 0;
            else if(currentPlaylistName != null && currentFileNumber == currentPlaylist.size()) currentFileNumber = 0;
        }
        
    }
    
    public void setPlaying(int ListNum){
        try{
            if(previousPlay != -1){
                PlayListTable.setValueAt(" ", previousPlay, 0);
            }
        }catch(Exception e){}
        try{
            PlayListTable.setValueAt(">", ListNum, 0);
        }catch(Exception e){}
        previousPlay = ListNum;
        
    }
    
    
    public void showCurrentPlayList(){
        if(currentPlaylistName != null){
            if(PlayListShowed)PlayListShowPanel.setVisible(false);
            PlayListShowed = true;
            
            PlayListShowPanel.setVisible(true);
            PlayListScrollPane.setVisible(false);
            PlayListLoading.setVisible(true);
            /*
             * Playlist panel position
             */
            PlayListBackGround.setIcon(currentPlaylistIcon);
            PlayListLoading.setBounds(92, 250, 50, 50);
            PlayListTitle.setBounds(8, 120, 230, 40);
            PlayListBackGround.setBounds(0, 100, 320, 390);
            PlayListScrollPane.setBounds(6, 160, 234, 322);
            /*
             * Playlist title
             */
            if(random)PlayListTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"3\" color=\"#FFFFFF\">Now playing on playlist</font></b></div>"
                +"<div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#FFFFFF\">"+currentPlaylistName+"(Random)</font></b></div>");
            else PlayListTitle.setText("<html> <div style=\"white-space: nowrap;\"><b><font size=\"3\" color=\"#FFFFFF\">Now playing on playlist</font></b></div>"
                +"<div style=\"white-space: nowrap;\"><b><font size=\"4\" color=\"#FFFFFF\">"+currentPlaylistName+"</font></b></div>");
            
            new Thread(new Runnable(){public void run(){
                final DefaultTableModel playlistModel = new PlayListModel(currentPlaylist,currentPlaylistName);
                PlayListTable = new JTable();
                PlayListTable.setModel(playlistModel);
                PlayListTable.setRowHeight(20);
                PlayListTable.setSelectionBackground(Color.PINK);
                PlayListTable.setTableHeader(null);
                PlayListTable.getColumn("Playing").setMaxWidth(20);
                PlayListTable.getColumn("Duration").setMaxWidth(80);
                PlayListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                
                PlayListTable.setDragEnabled(true);
                PlayListTable.setDropMode(DropMode.INSERT_ROWS);
                PlayListTable.setTransferHandler(new PlaylistTransferHandler(PlayListTable));
                PlayListScrollPane.getViewport().setBackground(new Color(255,255,255));
                PlayListScrollPane.setViewportView(PlayListTable);

                
                for(int i = 0 ; i < currentPlaylist.size() ; i++){
                    try{
                        playlistModel.addRow(new Object[]{
                            " ",
                            tableModel.getValueAt(currentPlaylist.get(i),0),
                            tableModel.getValueAt(currentPlaylist.get(i),1)
                        });
                        
                    }catch(Exception e){e.printStackTrace();}
                }
                setPlaying(currentFileNumber);
                PlayListTable.addKeyListener(new KeyAdapter(){
                    public void keyTyped(KeyEvent ke)
                    {
                        switch(ke.getKeyChar())
                        {
                            case '\u007F':
                            if(PlayListTable.getSelectedRow() != -1 && !currentPlaylistName.startsWith("#All Songs")){
                                currentPlaylist.remove(PlayListTable.getSelectedRow());
                                try{
                                    ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(".\\usr\\playlist\\"+currentPlaylistName+".list",false));
                                    playlistRegen.writeObject(currentPlaylist);
                                    playlistRegen.close();
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                playlistModel.removeRow(PlayListTable.getSelectedRow());
                            }
                            break;
                        }
                    }
                });
                
                PlayListTable.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        if(!PlayListTableWaiting && evt.getClickCount() == 2){
                            PlayListTableWaiting = true;
                            try{Thread.sleep(500);}catch(Exception e){}

                            synchronized(this){
                                if(isPlaying)stop();
                                currentFileNumber = PlayListTable.rowAtPoint(evt.getPoint());
                                PlayListTable.clearSelection();
                                musicPlay();

                                /*Delay clicking time*/
                                try{Thread.sleep(1000);}catch(Exception e){}
                                PlayListTableWaiting = false;
                            }
                        }
                    }
                });
                
                PlayListBackGround.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        PlayListShowPanel.setVisible(false);
                        PlayListShowed = false;
                    }
                });
                PlayListLoading.setVisible(false);
                PlayListScrollPane.setVisible(true);
            }}).start();
        }else{
            new Thread(new Runnable(){public void run(){
                new errPopUp("You haven't Select a Playlist.").popup();
            }}).start();
        }
    }
    
    
    /*
     * Add a new status to new playlist status frame
     * 
     */
    public void updatePlaylistStatus(String playlistName,String audioName){
        System.out.println("Update playlist -> "+playlistName+" ADD: "+audioName);
    }
    
    /*
     * Deleting playlist status frame
     * 
     */
    public void deletePlaylistStatus(String playlistName){
        System.out.println("Delete playlist -> "+playlistName);
    }
    
    
    private static class PlayingProgressBarUI extends BasicProgressBarUI {

        private Rectangle r = new Rectangle();

        @Override
        protected void paintIndeterminate(Graphics g, JComponent c) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            r = getBox(r);
            g.setColor(progressBar.getForeground());
            g.fillOval(r.x, r.y, r.width, r.height);
        }
    }
    
    private static class UploadListRenderer extends DefaultTableCellRenderer{

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


    class UploadListModel extends DefaultTableModel{

        Object[][] row = {};

        Object[] col = {"-", "Title","Song ID"};

        public UploadListModel (){

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


    private static class ArtistListCellModel extends DefaultTableModel{
        Object[][] row = {};

        Object[] col = {"Audio Title","LibID"};

        public ArtistListCellModel (){

            //Adding columns
            for(Object c: col)
                this.addColumn(c);

            //Adding rows
            for(Object[] r: row)
                addRow(r);

        }

        public boolean isCellEditable(int row, int col){ return false; }
    }



    private static class AlbumListCellModel extends DefaultTableModel{
        Object[][] row = {};

        Object[] col = {"Audio Title","LibID"};

        public AlbumListCellModel (){

            //Adding columns
            for(Object c: col)
                this.addColumn(c);

            //Adding rows
            for(Object[] r: row)
                addRow(r);

        }

        public boolean isCellEditable(int row, int col){ return false; }
    }


    private static class AudioFileFilter extends javax.swing.filechooser.FileFilter  
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
     * Handles drag & drop row reordering
     * 
     */
    interface Reordering {
       public void reorder(int fromIndex, int toIndex);
    }

    private static class PlayListModel extends DefaultTableModel implements Reordering{
        LinkedList<Integer> tempList;
        String tempName;

        Object[][] row = {};

        Object[] col = {"Playing","Title","Duration"};

        public PlayListModel (LinkedList<Integer> tempList,String listName){
            this.tempList = tempList;
            this.tempName = listName; 

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

        public void reorder(int fromIndex, int toIndex) {
            if(fromIndex < toIndex)toIndex -= 1;

            System.out.println("Move row >>> " + fromIndex + " <<< to Row >>> "+toIndex+" <<<");
            Object fromData[] = {getValueAt(fromIndex, 0),getValueAt(fromIndex, 1),getValueAt(fromIndex, 2)};

            int movedRow = toIndex - fromIndex;
            if(movedRow > 0){
                System.out.println("Moved Row > "+movedRow);

                Object tempData[][] = new Object[movedRow][3];
                for(int i = fromIndex+1, j = 0 ; j < tempData.length ; i++,j++){
                    tempData[j][0] = getValueAt(i, 0);
                    tempData[j][1] = getValueAt(i, 1);
                    tempData[j][2] = getValueAt(i, 2);
                }

                for(int i = fromIndex, j = 0; j < tempData.length; i++,j++){
                    setValueAt(tempData[j][0],i,0);
                    setValueAt(tempData[j][1],i,1);
                    setValueAt(tempData[j][2],i,2);
                }

                setValueAt(fromData[0],toIndex,0);
                setValueAt(fromData[1],toIndex,1);
                setValueAt(fromData[2],toIndex,2);

                int tempNum = tempList.get(fromIndex);
                tempList.remove(fromIndex);
                tempList.add(toIndex, tempNum);


                try{
                    ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(".\\usr\\playlist\\"+tempName+".list",false));
                    playlistRegen.writeObject(tempList);
                    playlistRegen.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else if(movedRow < 0){
                for(int i = fromIndex; i > toIndex ; i--){
                    setValueAt(getValueAt(i-1,0),i,0);
                    setValueAt(getValueAt(i-1,1),i,1);
                    setValueAt(getValueAt(i-1,2),i,2);
                }

                setValueAt(fromData[0],toIndex,0);
                setValueAt(fromData[1],toIndex,1);
                setValueAt(fromData[2],toIndex,2);

                int tempNum = tempList.get(fromIndex);
                tempList.remove(fromIndex);
                tempList.add(toIndex, tempNum);

                try{
                    ObjectOutputStream playlistRegen = new ObjectOutputStream(new FileOutputStream(".\\usr\\playlist\\"+tempName+".list",false));
                    playlistRegen.writeObject(tempList);
                    playlistRegen.close();
                }catch(Exception e){
                    e.printStackTrace();
                } 
            }
        }


    }


    private static class DefaultListModel extends DefaultTableModel{

        Object[][] row = {};

        Object[] col = {"Title","Time","Artist","Album"};

        public DefaultListModel (){

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



    private static class PlaylistTransferHandler extends TransferHandler {  
      private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class, "Integer Row Index");
       private JTable           table             = null;

       public PlaylistTransferHandler(JTable table) {
          this.table = table;
       }

       @Override
       protected Transferable createTransferable(JComponent c) {
          assert (c == table);
          return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
       }

       @Override
       public boolean canImport(TransferHandler.TransferSupport info) {
          boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
          table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
          return b;
       }

       @Override
       public int getSourceActions(JComponent c) {
          return TransferHandler.COPY_OR_MOVE;
       }

       @Override
       public boolean importData(TransferHandler.TransferSupport info) {
          JTable target = (JTable) info.getComponent();
          JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
          int index = dl.getRow();
          int max = table.getModel().getRowCount();
          if (index < 0 || index > max)
             index = max;
          target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          try {
             Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
             if (rowFrom != -1 && rowFrom != index) {
                ((Reordering)table.getModel()).reorder(rowFrom, index);
                if (index > rowFrom)
                   index--;
                target.getSelectionModel().addSelectionInterval(index, index);
                return true;
             }
          } catch (Exception e) {
             e.printStackTrace();
          }
          return false;
       }

       @Override
       protected void exportDone(JComponent c, Transferable t, int act) {
          if (act == TransferHandler.MOVE) {
             table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
       }

    }
    
    
    private static class SubUploadCellModel extends DefaultTableModel{
        Object[][] row = {};

        Object[] col = {"Num","Audio Title","SongID"};

        public SubUploadCellModel (){

            //Adding columns
            for(Object c: col)
                this.addColumn(c);

            //Adding rows
            for(Object[] r: row)
                addRow(r);

        }

        public boolean isCellEditable(int row, int col){ return false; }
    }


    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AddAudioPanel;
    private javax.swing.JLabel AddAudioTitle;
    private javax.swing.JPanel AlbumLeftPanel;
    private javax.swing.JLabel AlbumLeftPanelBg;
    private javax.swing.JLabel AlbumLeftPanelTitle;
    private javax.swing.JScrollPane AlbumScrollPane;
    private javax.swing.JPanel ArtistLeftPanel;
    private javax.swing.JLabel ArtistLeftPanelBg;
    private javax.swing.JLabel ArtistLeftPanelTitle;
    private javax.swing.JScrollPane ArtistScrollPane;
    private javax.swing.JTabbedPane AudioTabbedPane;
    private javax.swing.JLabel BackGround;
    private javax.swing.JLabel Background;
    private javax.swing.JPanel CurrentPlPanel;
    private javax.swing.JLabel LoginTitle;
    private javax.swing.JLabel MainBackground;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JLabel PlayListBackGround;
    private javax.swing.JLabel PlayListLoading;
    private javax.swing.JLabel PlayListSPTitle;
    private javax.swing.JScrollPane PlayListScrollPane;
    private javax.swing.JPanel PlayListSelectContainer;
    private javax.swing.JPanel PlayListSelectPanel;
    private javax.swing.JScrollPane PlayListSelectSP;
    private javax.swing.JPanel PlayListShowPanel;
    private javax.swing.JTable PlayListTable;
    private javax.swing.JLabel PlayListTitle;
    private javax.swing.JPanel PlayingPanelContainer;
    private javax.swing.JScrollPane PlaylistPanel;
    private javax.swing.JScrollPane SearchedPanel;
    private javax.swing.JPanel TableTab;
    private javax.swing.JLabel TableTabBackground;
    private javax.swing.JLabel UploadDec2;
    private javax.swing.JLabel UploadDec3;
    private javax.swing.JLabel UploadLeftTitle;
    private javax.swing.JPanel UploadSubPanel;
    private javax.swing.JPanel UploadSubPanelContainer;
    private javax.swing.JScrollPane UploadSubScrollPane;
    private javax.swing.JLabel UploadedDec;
    private javax.swing.JPanel UploadedLeftPanel;
    private javax.swing.JLabel UploadedLeftPanelBg;
    private javax.swing.JLabel UploadedLoading;
    private javax.swing.JPanel UploadedMainPanel;
    private javax.swing.JPanel UploadedPanel;
    private javax.swing.JPanel albumContainer;
    private javax.swing.JPanel artistContainer;
    private javax.swing.JButton btnAddAudioOpen;
    private javax.swing.JButton btnAlbum;
    private javax.swing.JButton btnAllSong;
    private javax.swing.JButton btnArtist;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCover;
    private javax.swing.JButton btnCurrentPlaylist;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLoop;
    private javax.swing.JButton btnMini;
    private javax.swing.JButton btnMove;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnPlay;
    private javax.swing.JButton btnRandom;
    private javax.swing.JButton btnReg;
    private javax.swing.JButton btnSetting;
    private javax.swing.JButton btnSettings;
    private javax.swing.JButton btnUpload;
    private javax.swing.JButton btnUploadedSongs;
    private javax.swing.JLabel controlMsg;
    private javax.swing.JProgressBar durationProcessBar;
    private javax.swing.JLabel durationText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblClose;
    private javax.swing.JLabel leftTime;
    private javax.swing.JPasswordField logPassword;
    private javax.swing.JTextField logUsername;
    private javax.swing.JLabel loginDec;
    private javax.swing.JLabel loginDec2;
    private javax.swing.JLabel loginDec3;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JLabel playingTime;
    private javax.swing.JPanel searchContainer;
    private javax.swing.JButton tabCover;
    private javax.swing.JTable tabList;
    private javax.swing.JLabel totalTime;
    private javax.swing.JTextField upldSearchValue;
    private javax.swing.JPanel uploadButtonPanel;
    private javax.swing.JPanel uploadContainer;
    private javax.swing.JPanel uploadLoadingPanel;
    private javax.swing.JLabel uploadProcessLabel;
    private javax.swing.JScrollPane uploadPublicPanel;
    private javax.swing.JScrollPane uploadScrollPane;
    private javax.swing.JButton uploadSearch;
    private javax.swing.JLabel uploadTextAlbum;
    private javax.swing.JButton uploadTextTotal;
    private javax.swing.JSlider volumeSilder;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables



}

