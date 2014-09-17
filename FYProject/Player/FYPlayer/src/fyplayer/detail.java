package fyplayer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
import com.sun.awt.AWTUtilities;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import net.AudioDetails;
import tag.codec.AudioTAG;
public class detail extends javax.swing.JFrame {

    private File readSong           = null;
    private AudioDetails readDetails= null;
    private Point cursor            = null;
    private ImageIcon defaultCover  = null;
    
    private String _Title           = "";
    private String _Artist          = "";
    private String _Album           = "";
    private String _Track           = "";
    private String _Year            = "";
    private Image _Cover            = null;
    private String duration         = null;
    
    private ImageIcon previousCover = defaultCover;
    private static String previousAlbum = "";
    /**
     * Creates new form detail
     */
    public detail() {
        initComponents();
        /*/SE 1.7
        this.setOpacity(0.9f);
        this.setBackground(new Color(255, 255, 255, 0));
        /**/
        
        //SE 1.6 u 10
        AWTUtilities.setWindowOpaque(this, false);
        /**/
        
        try {
            defaultCover = new ImageIcon((ImageIO.read(getClass().getResource("/defaultSkins/default-cover.png"))).getScaledInstance(350,350, Image.SCALE_SMOOTH));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setSong(File song,String time){
        previousAlbum = _Album;
        readSong = song;
        duration=time;

        new Thread(new Runnable(){public void run(){
            try {
                
                AudioTAG songDetails = new AudioTAG(readSong.getPath());
                _Title      = songDetails.getTagTitle();
                _Artist     = songDetails.getTagArtist();
                _Album      = songDetails.getTagAlbum();
                _Track      = songDetails.getTagTrack();
                _Cover      = songDetails.getTagCover();
                _Year       = songDetails.getTagYear();

                if(_Artist == null)_Artist    = "-";
                if(_Album == null)_Album      = "-";
                if(_Track == null)_Track      = "-";
                if(_Year == null)_Year        = "-";


                lblSname.setText(_Title);
                lblSartist.setText(_Artist);
                lblSalbum.setText(_Album);
                lblStime.setText(duration);
                lblStrack.setText(_Track);
                lblYear.setText(_Year);

                darkCover.setBackground(new Color(0,0,0,120));

                if(!previousAlbum.equals(_Album)){
                    lblTempCover.setIcon(previousCover);
                    lblTempCover.setVisible(true);

                    if(_Cover != null){
                        _Cover = _Cover.getScaledInstance(350,350, Image.SCALE_SMOOTH);
                        ImageIcon coverIcon = new ImageIcon(_Cover);

                        lblCover.setIcon(coverIcon);

                        previousCover = coverIcon;
                    }else{
                        lblCover.setIcon(defaultCover);

                        previousCover = defaultCover;
                    }

                    for(int i = 0 ; i >= -350 ; i-=25){
                        lblCover.setLocation(i+350,lblCover.getY());
                        lblTempCover.setLocation(i,lblCover.getY());
                        try{Thread.sleep(20);}catch(Exception e){}
                    }
                    
                    lblTempCover.setVisible(false);

                }
                
            } catch (Exception ex) {}
        }}).start();
    }
     
    public void setSong(AudioDetails audioDetails,String time){
        readDetails = audioDetails;
        duration=time;
        new Thread(new Runnable(){public void run(){
            if(readDetails.containsTag()){
                _Title      = readDetails.getTagTitle();
                _Artist     = readDetails.getTagArtist();
                _Album      = readDetails.getTagAlbum();
                _Track      = readDetails.getTagTrack();
            }

            lblSname.setText(_Title);
            lblSartist.setText(_Artist);
            lblSalbum.setText(_Album);
            lblStime.setText(duration);
            lblStrack.setText(_Track);

            if(readDetails.containCover()){
                try {
                    _Cover  = readDetails.getTagCover();
                    _Cover = _Cover.getScaledInstance(200,200, Image.SCALE_SMOOTH);
                    ImageIcon coverIcon = new ImageIcon(_Cover);
                    lblCover.setIcon(coverIcon);
                } catch (IOException ex) {}
            }else
                lblCover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/default-cover.png")));

            //Popup(true);
            try {Thread.sleep(2000);} catch (InterruptedException ex) {}
            //Popup(false);
        }}).start();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainPanel = new javax.swing.JPanel();
        btnClose = new javax.swing.JButton();
        btnMove = new javax.swing.JButton();
        lblSong = new javax.swing.JLabel();
        lblArtist = new javax.swing.JLabel();
        lblAlbum = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblSalbum = new javax.swing.JLabel();
        lblSartist = new javax.swing.JLabel();
        lblStime = new javax.swing.JLabel();
        lblSname = new javax.swing.JLabel();
        lblTrack = new javax.swing.JLabel();
        lblStrack = new javax.swing.JLabel();
        lblYearTitle = new javax.swing.JLabel();
        lblYear = new javax.swing.JLabel();
        darkCover = new javax.swing.JLabel();
        lblTempCover = new javax.swing.JLabel();
        lblCover = new javax.swing.JLabel();
        MainShadow = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Current Music");
        setAlwaysOnTop(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(350, 350));
        setResizable(false);
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(null);

        MainPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        MainPanel.setLayout(null);

        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/defaultSkins/close.png"))); // NOI18N
        btnClose.setBorder(null);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        MainPanel.add(btnClose);
        btnClose.setBounds(325, 0, 25, 25);

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
        btnMove.setBounds(0, 0, 350, 350);

        lblSong.setForeground(new java.awt.Color(255, 255, 255));
        lblSong.setText("Song:");
        MainPanel.add(lblSong);
        lblSong.setBounds(10, 11, 40, 30);

        lblArtist.setForeground(new java.awt.Color(255, 255, 255));
        lblArtist.setText("Artist:");
        MainPanel.add(lblArtist);
        lblArtist.setBounds(10, 110, 40, 30);

        lblAlbum.setForeground(new java.awt.Color(255, 255, 255));
        lblAlbum.setText("Album:");
        MainPanel.add(lblAlbum);
        lblAlbum.setBounds(10, 165, 40, 30);

        lblTime.setForeground(new java.awt.Color(255, 255, 255));
        lblTime.setText("Time:");
        MainPanel.add(lblTime);
        lblTime.setBounds(10, 60, 40, 30);

        lblSalbum.setForeground(java.awt.Color.white);
        lblSalbum.setText(null);
        MainPanel.add(lblSalbum);
        lblSalbum.setBounds(60, 165, 280, 30);

        lblSartist.setForeground(java.awt.Color.white);
        lblSartist.setText(null);
        MainPanel.add(lblSartist);
        lblSartist.setBounds(60, 110, 280, 30);

        lblStime.setForeground(java.awt.Color.white);
        lblStime.setText(null);
        MainPanel.add(lblStime);
        lblStime.setBounds(60, 60, 120, 30);

        lblSname.setForeground(java.awt.Color.white);
        lblSname.setText(null);
        MainPanel.add(lblSname);
        lblSname.setBounds(61, 11, 160, 30);

        lblTrack.setForeground(new java.awt.Color(255, 255, 255));
        lblTrack.setText("Track:");
        MainPanel.add(lblTrack);
        lblTrack.setBounds(230, 10, 40, 30);

        lblStrack.setForeground(java.awt.Color.white);
        lblStrack.setText(null);
        MainPanel.add(lblStrack);
        lblStrack.setBounds(270, 10, 60, 30);

        lblYearTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblYearTitle.setText("Year:");
        MainPanel.add(lblYearTitle);
        lblYearTitle.setBounds(230, 60, 40, 30);

        lblYear.setForeground(new java.awt.Color(255, 255, 255));
        MainPanel.add(lblYear);
        lblYear.setBounds(270, 60, 60, 30);

        darkCover.setOpaque(true);
        MainPanel.add(darkCover);
        darkCover.setBounds(0, 0, 350, 350);

        lblTempCover.setText("jLabel1");
        MainPanel.add(lblTempCover);
        lblTempCover.setBounds(0, 0, 350, 350);

        lblCover.setForeground(java.awt.Color.white);
        MainPanel.add(lblCover);
        lblCover.setBounds(0, 0, 350, 350);

        getContentPane().add(MainPanel);
        MainPanel.setBounds(5, 5, 350, 350);

        MainShadow.setBackground(new java.awt.Color(51, 51, 51));
        MainShadow.setOpaque(true);
        getContentPane().add(MainShadow);
        MainShadow.setBounds(2, 2, 356, 356);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-360)/2, (screenSize.height-360)/2, 360, 360);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void btnMoveMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMoveMousePressed
        // TODO add your handling code here:
        cursor = evt.getPoint();
    }//GEN-LAST:event_btnMoveMousePressed

    private void btnMoveMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMoveMouseDragged
        // TODO add your handling code here:
        this.setLocation(this.getX()+(int)(evt.getX()-cursor.getX()),this.getY()+(int)(evt.getY()-cursor.getY()));
    }//GEN-LAST:event_btnMoveMouseDragged

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainPanel;
    private javax.swing.JLabel MainShadow;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnMove;
    private javax.swing.JLabel darkCover;
    private javax.swing.JLabel lblAlbum;
    private javax.swing.JLabel lblArtist;
    private javax.swing.JLabel lblCover;
    private javax.swing.JLabel lblSalbum;
    private javax.swing.JLabel lblSartist;
    private javax.swing.JLabel lblSname;
    private javax.swing.JLabel lblSong;
    private javax.swing.JLabel lblStime;
    private javax.swing.JLabel lblStrack;
    private javax.swing.JLabel lblTempCover;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTrack;
    private javax.swing.JLabel lblYear;
    private javax.swing.JLabel lblYearTitle;
    // End of variables declaration//GEN-END:variables
}
