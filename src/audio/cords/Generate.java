/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.cords;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import static java.lang.Math.sin;
/**
 *
 * @author дом
 */
public class Generate {
  public static void main(String[] args) throws Exception{
      int sampleRate = 11025*4;
      SourceDataLine line =
              AudioSystem.getSourceDataLine(new AudioFormat(sampleRate,16,1,true,true));
      line.open();
      line.start();
      int t=0;

      Map<Double,Double> sines = new HashMap<Double,Double>();
      phoneBuzz(sines);
      //rrrr(sines);
      double sum=0;
      for( Double v : sines.values() ){
          sum += v;
      }
      Noise noise=new Noise();

      for( ;; ){
          ByteArrayOutputStream ba = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(ba);
          for( int i=0; i<1000; i++ ){
              if( t%10000==0 ){
                  System.out.println("t="+t);
              }
              t++;
              double sig=0;
              for( Double f : sines.keySet() ){
                  sig += sines.get(f)*sin(f*t/sampleRate*2*Math.PI);
              }
              //dos.writeShort((short)(sig/sum*25000));
              dos.writeShort(noise.next());
          }
          //line.flush();
          line.write(ba.toByteArray(), 0, ba.size());

      }
  }

  static class Noise{
      short[] buf = new short[11025*4*4/5];
      int idx=-1;
      Noise(){
          for( int i=0; i<buf.length; i++ ){
              double s = 0;
              for( int j=0; j<12; j++ ){
                  s+=Math.random();
              }
              buf[i] = (short)(s/12*25000);
          }
      }
      short next(){
         idx = (idx+1)%buf.length;
         return buf[idx];
      }
  }

    private static void phoneBuzz(Map<Double, Double> sines) {
        sines.put(350., 9256.);
        sines.put(440., 9256.);
        sines.put(5*350., 3256.);
        sines.put(5*440., 3256.);
    }


    private static void rrrr(Map<Double, Double> sines) {
        for( int i=2; i<15; i++ ){
          for( int j=-5; j<5; j++ ){
            sines.put(20.*i+j/20.+Math.random(), Math.random()/(1+j*j*j*j));
          }

        }
    }
}
