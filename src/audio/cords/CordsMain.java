/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.cords;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import sun.applet.Main;

/**
 *
 * @author дом
 */
public class CordsMain {
   public static void main(String[] args) throws Exception{
     List<Pendulum> pends = new ArrayList<Pendulum>();
     for( int i=100; i<=1000; i+=100 ){
         pends.add(new Pendulum(i));
     }

     int sampleRate = 11025;
     TargetDataLine line = AudioSystem.getTargetDataLine(new AudioFormat(sampleRate,8,1,true,false));
     line.open();
     line.start();
     byte[] buf = new byte[1024];
     int globalIdx=0;
     for( int j=0; j<300; j++ ){
         line.read(buf, 0, buf.length);
         for( int i=0; i<buf.length; i++ ){
             for( Pendulum p : pends ){
                 p.timeStep(1./sampleRate);
                 p.vchange(buf[i]*p.freq/1000);
             }
             globalIdx++;

             if(globalIdx%1000==0){
                 for( Pendulum p : pends ){
                     int pi = (int)(p.power()*100);
                     System.out.print(pi+" ");
                 }
                 System.out.println("==");
             }

             /*
             System.out.print(buf[i]+" ");
             if(i%50==0){
                 System.out.println();
             }
              */
         }
     }
   }
}
