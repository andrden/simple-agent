package audio.cords;

import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.sampled.SourceDataLine;

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

  public static void main(String[] args) throws Exception{
      int sampleRate = 11025;
      Mike m = new Mike(sampleRate);
      //printEnergy(m);
      //save(m);
      play();
  }

  private static void printEnergy(Mike m) {
        for (;;) {
            double s = 0;
            for (int i = 0; i < 3000; i++) {
                s += Math.pow(m.next(), 2);
            }
            System.out.println(s/1000000);
        }
    }
  
  private static void save(Mike m) throws Exception {
      DataOutputStream dos = new DataOutputStream(new FileOutputStream("c:/tmp/f1.voice"));
        for (int i=0; i<30*11025; i++) {
            dos.writeShort(m.next());
        }
      dos.close();
    }

  private static void play() throws Exception {
    DataInputStream di = new DataInputStream(new FileInputStream("c:/tmp/f1.voice"));
    byte[] b = new byte[di.available()];
    di.read(b);
    SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(11025,16,1,true,true));
    line.open();
    line.start();
    line.write(b, 0, b.length);
  }
}
