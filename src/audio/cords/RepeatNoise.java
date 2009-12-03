package audio.cords;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
      SoundIn noise=new Noise();

      for( ;; ){
          ByteArrayOutputStream ba = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(ba);
          for( int i=0; i<1000; i++ ){
               dos.writeShort(noise.next());
          }
          line.write(ba.toByteArray(), 0, ba.size());

      }
  }


}
