package audio.cords;

import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Dec 3, 2009
 * Time: 4:22:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class Mike implements SoundIn{
  int sampleRate = 11025;
  byte[] buf = new byte[2048];
  TargetDataLine line;
  DataInputStream di;

  public Mike(int sampleRate) throws LineUnavailableException {
    this.sampleRate = sampleRate;

    line = AudioSystem.getTargetDataLine(new AudioFormat(sampleRate,16,1,true,true));
    line.open();
    line.start();
  }

  public short next() {
    try {
      if( di==null || di.available()<1 ){
        line.read(buf, 0, buf.length);
        di = new DataInputStream(new ByteArrayInputStream(buf));
      }
      return di.readShort();
    } catch (IOException e) {
      throw new RuntimeException("",e);
    }
  }
}
