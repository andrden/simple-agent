package audio.cords;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class RepeatNoise {
  public static void main(String[] args) throws Exception{
      int sampleRate = 11025*4;
      SourceDataLine line =
              AudioSystem.getSourceDataLine(new AudioFormat(sampleRate,16,1,true,true));
      line.open();
      line.start();
      Noise noise=new Noise();

      for( ;; ){
          ByteArrayOutputStream ba = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(ba);
          for( int i=0; i<1000; i++ ){
               dos.writeShort(noise.next());
          }
          line.write(ba.toByteArray(), 0, ba.size());

      }
  }

  static class Noise{
      short[] buf = new short[11025*4*4/5];
      int idx=-1;
      Noise(){
          Random rnd = new Random(System.nanoTime());
          for( int i=0; i<buf.length; i++ ){
//              double s = 0;
//              for( int j=0; j<12; j++ ){
//                  s+=Math.random();
//              }
//              buf[i] = (short)(s/12*25000);
              buf[i] = (short)(rnd.nextGaussian()*25000);
          }
      }
      short next(){
         idx = (idx+1)%buf.length;
         return buf[idx];
      }
  }

    
}
