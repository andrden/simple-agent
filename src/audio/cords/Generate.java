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
      int sampleRate = 11025;
      SourceDataLine line =
              AudioSystem.getSourceDataLine(new AudioFormat(sampleRate,16,1,true,true));
      line.open();
      line.start();
      int t=0;

      Map<Double,Double> sines = new HashMap<Double,Double>();
      //phoneBuzz(sines);
      rrrr(sines);
      double sum=0;
      for( Double v : sines.values() ){
          sum += v;
      }

      long tglob=0;
      for( ;; ){
          ByteArrayOutputStream ba = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(ba);
          for( int i=0; i<1000; i++ ){
              tglob++;
              t++;
              double sig=0;
              for( Double f : sines.keySet() ){
                if( tglob<25000 ){
                  break;
                }
                if( f*30 > tglob-25000){
                  break;
                }

                  sig += sines.get(f)*sin(f*(tglob-25000-f*30)/sampleRate*2*Math.PI);
              }
              dos.writeShort((short)(sig/sum*25000));
          }
          //line.flush();
          line.write(ba.toByteArray(), 0, ba.size());

      }
  }

    private static void phoneBuzz(Map<Double, Double> sines) {
        sines.put(350., 1.);
        sines.put(440., 1.);
    }


    private static void rrrr(Map<Double, Double> sines) {
        for( int i=2; i<15; i++ ){
          for( int j=-5; j<5; j++ ){
            sines.put(20.*i+j/20.+Math.random(), Math.random()/(1+j*j*j*j));
          }

        }
    }
}
