/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Central;

import Central.CentralServer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Sebastian
 */
class mediaServerDetails extends SysPrint{
    private Socket requestSocket    = null;
    private String serverIP         = null;
    private String location         = null;
    private int port                = 0;
    private int centralPort         = 0;
    private boolean closed          = false;
    
    public mediaServerDetails(String ip,String Location,int Port,int CentralPort){
        //Initialize
        
        serverIP    = ip;
        location    = Location;
        port        = Port;
        centralPort = CentralPort;
        
        sysPrint("Getting Media Server Details : "+serverIP+":"+port+" , Location : "+location);
        
        /*
         * Provide Server Port to Media Server and
         * Check Server response
         */
        try{
            requestSocket = new Socket(serverIP,port);
            ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
            
            out.writeObject("C02");
            in.readObject();    //#Port
            out.writeObject(String.valueOf(this.centralPort));
            in.readObject();    //K02
            
            requestSocket.close();
        }catch(ConnectException e){
            //Server CLOSED
            closed = true;
        }catch(Exception e){
            e.printStackTrace();
            closed = true;
        }
    }
    
    public boolean available(){
        if(closed)return false;
        
        boolean ans = false;
        try {
            sysPrint("Checking Availability to Server > "+serverIP);
            requestSocket = new Socket(serverIP,port);
            requestSocket.setSoTimeout(5000);
            
            ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
            
            sysPrint("Connecting to Media Server > "+serverIP+":"+port);
            
            out.writeObject("C01");  //C1 = check availability commend
            out.flush();
            ans = ((String)in.readObject()).equals("K01");
            
            out.writeObject("C02");
            in.readObject();    //#Port
            out.writeObject(String.valueOf(this.centralPort));
            in.readObject();    //K02

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try {
            requestSocket.close();
        } catch (IOException e) {}
        
        return ans;
    }
    
    public String getIP(){
        return serverIP;
    }
    
    public int getPort(){
        return port;
    }
    
    public String getLocation(){
        return location;
    }
}
