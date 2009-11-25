/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.cords;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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

     JFrame singframe = new JFrame();
     singframe.setSize(800,300);
     singframe.setVisible(true);

     Pendulum sing=null;
     Pendulum sing2=null;
     List<Pendulum> pends = new ArrayList<Pendulum>();
     for( double i=70; i<=5000; i*= (i<200 ? 1.3 :1.07)  ){
         System.out.println(i);
         Pendulum pi = new Pendulum(i);
         if( sing==null && i>820 ){
             sing=pi;
             System.out.println("single "+i);
         }
         if( sing2==null && i>1820 ){
             sing2=pi;
             System.out.println("single2 "+i);
         }
         pends.add(pi);
     }

     int sampleRate = 11025;
     TargetDataLine line = AudioSystem.getTargetDataLine(new AudioFormat(sampleRate,16,1,true,true));
     line.open();
     line.start();
     byte[] buf = new byte[2048];
     int globalIdx=0;
     for( int j=0; ; j++ ){
         line.read(buf, 0, buf.length);
         DataInputStream di = new DataInputStream(new ByteArrayInputStream(buf));
         final Graphics graphics = jframe.getContentPane().getGraphics();
         final Graphics sgraphics = singframe.getContentPane().getGraphics();
         double audioValPrev=0;
         for( int i=0; i<buf.length/2; i++ ){
             double audioVal = di.readShort()/256.;
             for( Pendulum p : pends ){
                 p.timeStep(1./sampleRate);
                 p.vchange(audioVal*p.freq/1000);
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
                 int[] pvals = new int[pends.size()];
                 double[] dvals = new double[pends.size()];
                 int pvalMax=0;
                 for( Pendulum p : pends ){
                     x++;
                     double pp = p.powerAvg()*100;
                     dvals[x-1]=pp;
                     if( pp<5 ){
                         pp=5;
                     }
                     int pi = (int)(Math.log10(1+pp)*60);
                     //System.out.println(pi);
                     pvals[x-1]=pi;
                     pvalMax = Math.max(pi,pvalMax);
                 }

                 x=0;
                 for( Pendulum p : pends ){
                     x++;
                     int pi = pvals[x-1];//*255/pvalMax;
                     if( pvals[x-1]==pvalMax ){
                       graphics.setColor(new Color(pi,pi/2,pi/2));
                     }else{
                       graphics.setColor(new Color(pi/2,pi,pi/2));
                     }
                     graphics.fillRect(x*10,y*2, 10,2);
                     //System.out.print(pi+" ");
                 }
             }
             if(globalIdx%10==0){
                 int x=globalIdx/10 % singframe.getWidth();
                 sgraphics.setColor(Color.WHITE);
                 sgraphics.drawLine(x, 0, x, singframe.getHeight());
                 double p;
                 //if( globalIdx%20==0 ){
                   sgraphics.setColor(Color.BLUE.darker());
                   //p=sing2.power()*10;
                   p=Math.abs(audioVal-audioValPrev)/256*10;
                 if( p>1 ) p=1;
                 sgraphics.drawLine(x, (int)(singframe.getHeight()*(1./2-p/2)),
                         x, singframe.getHeight()/2);

                   //}else{
                   sgraphics.setColor(Color.GREEN.darker());
                   p=sing.powerAvg()*10;
                 //}
                 if( p>1 ) p=1;
                 sgraphics.drawLine(x, (int)(singframe.getHeight()*(1-p/2)),
                         x, singframe.getHeight());
             }

                 if( sing.powerAvg()>0.1 ){
                   //System.out.println(globalIdx+ " sing.power()="+sing.power());
                 }

             /*
             System.out.print(buf[i]+" ");
             if(i%50==0){
                 System.out.println();
             }
              */
             audioValPrev=audioVal;
         }
     }
   }
}
