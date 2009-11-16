/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.cords;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import sun.applet.Main;

/**
 *
 * @author дом
 */
public class CordsMain {
   public static void main(String[] args) throws Exception{
     JFrame jframe = new JFrame();
     //jframe.setShape(new Ellipse2D.Double(100,200,400,200));
     jframe.setSize(600,600);
     jframe.setVisible(true);

     List<Pendulum> pends = new ArrayList<Pendulum>();
     for( int i=100; i<=1000; i+=10 ){
         pends.add(new Pendulum(i));
     }

     int sampleRate = 11025;
     TargetDataLine line = AudioSystem.getTargetDataLine(new AudioFormat(sampleRate,8,1,true,false));
     line.open();
     line.start();
     byte[] buf = new byte[1024];
     int globalIdx=0;
     for( int j=0; ; j++ ){
         line.read(buf, 0, buf.length);
         final Graphics graphics = jframe.getContentPane().getGraphics();
         for( int i=0; i<buf.length; i++ ){
             for( Pendulum p : pends ){
                 p.timeStep(1./sampleRate);
                 p.vchange(buf[i]*p.freq/1000);
             }
             globalIdx++;
/*
             if(globalIdx%1000==0){
                 for( Pendulum p : pends ){
                     int pi = (int)(p.power()*100);
                     System.out.print(pi+" ");
                 }
                 System.out.println("==");
             }
*/
             if(globalIdx%100==0){
                 final int y = globalIdx / 100 % (jframe.getHeight()/2);
                 graphics.setColor(Color.BLUE);
                 graphics.drawLine(0, y*2+2,100, y*2+2);
                 int x=0;
                 for( Pendulum p : pends ){
                     x++;
                     double pp = p.power()*100;
                     if( pp<3 ){
                         pp=3;
                     }
                     int pi = (int)(Math.log10(1+pp)*60);
                     //System.out.println(pi);
                     graphics.setColor(new Color(pi/2,pi,pi/2));
                     graphics.fillRect(x*10,y*2, 10,2);
                     //System.out.print(pi+" ");
                 }
                 //System.out.println("==");
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
