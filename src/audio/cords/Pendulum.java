/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.cords;
import static java.lang.Math.*;

/**
 *
 * @author дом
 */
public class Pendulum {
  final double freq;
  final double freqHz;
  double a = 25;
  double x=0;
  double v=0;
  double c1=0;
  double c2=0;

  double[] powersPrev=new double[2000];
  int powersPrevIdx=0;
  double prevP;
  double oldPdelta=0;
  double segStartPAvg=0;

  final double stdDelta;
  final double cosFD;
  final double sinFD;
  final double expMAD;
  final double revFreq;

  Pendulum(double freqHz, double stdDelta){
      this.freqHz=freqHz;
      freq = freqHz  *2*PI;
      this.stdDelta=stdDelta;

      cosFD=cos(freq*stdDelta);
      sinFD=sin(freq*stdDelta);
      expMAD=exp(-a*stdDelta);
      revFreq=1/freq;
  }

  void timeStep(){
      double curP = power();
      double curPAvg=powerAvg();
      double pdelta=Math.signum(curP-powersPrev[powersPrevIdx]);
      powersPrev[powersPrevIdx]=curP;
      powersPrevIdx = (powersPrevIdx + 1) % powersPrev.length;
      if( pdelta*oldPdelta<0 ){
          if(curP>0.2 && Math.abs(curPAvg-segStartPAvg)>0.01){
            //System.out.println(System.currentTimeMillis()+
             //       " freq="+freqHz+" end delta "+oldPdelta+" pavg="+curPAvg);
          }
          segStartPAvg=curPAvg;
      }
      if( pdelta!=0 ){
          oldPdelta = pdelta;
      }
      prevP=curP;

      
      x = expMAD*(c1*cosFD+c2*sinFD);
      v = -a*x + freq*expMAD*(-c1*sinFD+c2*cosFD);
      vchange(0); // recalc c1 & c2
  }
  void vchange(double delta){
      v+=delta;
      // t=0
      c1 = x;
      c2 = (v+a*x)*revFreq;
  }
  double power(){
      return sqrt(c1*c1+c2*c2);
  }
  double powerAvg(){
      double s=0;
      for( double d : powersPrev ){
          s+=d;
      }
      return s/powersPrev.length;
  }

  public static void main(String[] args){
      Pendulum p = new Pendulum(300, 1/10000.);
      //p.vchange(1);
      for( int i=0; i<10000; i++ ){
          double treal=i/10000.;
          double force = sin(treal*2*PI*400);
          // +0 Hz - 5000,  +10 Hz - 400, +100 Hz - 36   - resonance at 300 Hz
          p.timeStep();
          p.vchange(force);
          System.out.println(i+" "+p.power()+" "+10000*p.x);
      }
  }


  public static void mainAttenuating(String[] args){
      Pendulum p = new Pendulum(300, 1/10000.);
      p.vchange(1);
      for( int i=0; i<10000; i++ ){
          //double treal=
          p.timeStep();
          System.out.println(i+" "+p.power());
      }
  }
}
