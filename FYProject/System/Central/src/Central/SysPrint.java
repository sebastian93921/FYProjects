/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Central;

import java.io.PrintStream;

/**
 *
 * @author Sebastian
 */
class SysPrint extends Thread{
    static{ 
        // as we are using System.out as the output stream in main 
        final PrintStream currentOut = System.out; 
        final PrintStream currentErr = System.err;

          // anonymous as we would need this sub class here only 
         PrintStream newOut = new PrintStream(currentOut){
            // Overriding 'println' method 
            public void println(String string){

                print("[System]"); 
                print(string+"\n");
            } 
        };

        PrintStream newErr = new PrintStream(currentErr){
            // Overriding 'println' method 
            public void println(String string){

                print("[ERROR]"); 
                print(string+"\n");
            } 
        };
        
        System.setOut(newOut);
        System.setErr(newErr);
        
    }
     
    public void sysPrint(String s){
        System.out.println(s);
    }
    
    public void errPrint(String s){
        System.err.println(s);
    }
}
