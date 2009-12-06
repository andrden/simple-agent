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
    static class TwoRanges{
        List<Pendulum> pends1=new ArrayList<Pendulum>();
        List<Pendulum> pends2=new ArrayList<Pendulum>();
        TwoRanges(List<Pendulum> pends, int a1, int a2, int b1, int b2){
            for( Pendulum p : pends ){
              if( p.freqHz>=a1 && p.freqHz<=a2 ){
                  pends1.add(p);
              }
              if( p.freqHz>=b1 && p.freqHz<=b2 ){
                  pends2.add(p);
              }
            }
        }
        double avg(List<Pendulum> pends){
            double d=0;
            for( Pendulum p : pends ){
                d+=p.power();
            }
            return d/pends.size();
        }
        void paint(Graphics g, int x0, int w, int h){
           double a1 = avg(pends1);
           double a2 = avg(pends2);
           double k=a1/a2;
           double y = k/10*h;
           double x = (a1+a2)/0.01*w;
           if( x>w ){
               x=w;
           }
           g.drawOval(x0+(int)x, (int)y, 2, 2);
        }
    }

   public static void main(String[] args) throws Exception{
     JFrame jframe = new JFrame();
     //jframe.setShape(new Ellipse2D.Double(100,200,400,200));
     jframe.setSize(600,600);
     jframe.setVisible(true);

     JFrame singframe = new JFrame();
     singframe.setSize(800,300);
     singframe.setVisible(true);

     JFrame cmpFrame = new JFrame("CMPFrame");
     cmpFrame.setSize(400,400);
     cmpFrame.setVisible(true);

     Pendulum sing=null;
     Pendulum sing2=null;
     List<Pendulum> pends = new ArrayList<Pendulum>();
     for( double i=70; i<=5000; i*= (i<200 ? 1.3 :1.07)  ){
         System.out.println(i);
         Pendulum pi = new Pendulum(i);
         if( sing==null && i>350 ){
             sing=pi;
             System.out.println("single "+i);
         }
         if( sing2==null && i>1820 ){
             sing2=pi;
             System.out.println("single2 "+i);
         }
         pends.add(pi);
     }
     sing = new Pendulum(350);
     pends.add(sing);
     List<TwoRanges> twoRList = new ArrayList<TwoRanges>();
     twoRList.add( new TwoRanges(pends,200,500,500,1000) );
     twoRList.add( new TwoRanges(pends,100,200,1000,1500) );
     twoRList.add( new TwoRanges(pends,200,1000,1000,2000) );
     twoRList.add( new TwoRanges(pends,1000,2000,2000,3000) );

     int sampleRate = 11025;
     SoundIn soundIn = new Mike(sampleRate);
     //SoundIn soundIn = new Noise();
     int globalIdx=0;
     for( int j=0; ; j++ ){
         double audioValPrev=0;
         for( int i=0; ; i++ ){
         final Graphics graphics = jframe.getContentPane().getGraphics();
         final Graphics sgraphics = singframe.getContentPane().getGraphics();
             if(globalIdx%1000==0){
               //Thread.sleep(150);
               //System.out.println(twoR.avg(twoR.pends1)+" rates to "+twoR.avg(twoR.pends2));
             }
             double audioVal = soundIn.next()/256./3;
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
             if(globalIdx%10==0){
                 int ii=0;
                 final Graphics gr = cmpFrame.getContentPane().getGraphics();
                 int w = cmpFrame.getWidth();
                 for( TwoRanges twoR  : twoRList ){

                   twoR.paint(gr,w*ii/twoRList.size(),w/twoRList.size(),cmpFrame.getHeight());
                   ii++;
                 }
             }
             if(false && globalIdx%100==0){

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
             if(false && globalIdx%10==0){
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
