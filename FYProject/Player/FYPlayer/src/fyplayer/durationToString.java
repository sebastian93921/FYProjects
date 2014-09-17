package fyplayer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package fypaudioplayer;

/**
 *
 * @author Sebastian
 */
public class durationToString {
    String stringDuration = "";
    
    public durationToString(long duration){
        
        String currentMin = new Integer((int)duration/60).toString();
        if(currentMin.length() == 1)currentMin = "0"+currentMin;
        String currentSec = new Integer((int)duration%60).toString();
        if(currentSec.length() == 1)currentSec = "0"+currentSec;
        
        stringDuration = currentMin+":"+currentSec;
    }
    
    public String toString(){
        return stringDuration;
    }
}
