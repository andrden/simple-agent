package audio.cords;

import audio.DFT;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Dec 10, 2009
 * Time: 5:42:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShShCorrelate {
  public static void main(String[] args) throws Exception{
    new ShShCorrelate().play();
  }

  short[] soundBufAt(int pos) throws Exception{
    DataInputStream di = soundFile();
    short[] buf = new short[128];
    int freq = 11025;
    for( int i=0; i<=pos; i++ ){
      readAll(di, buf);
    }
    return buf;
  }

  void play() throws Exception{
    // sh ш to higher (*3/2 or *2) freq = sh' щ
    // ch ч to higher (*3/2 or *2) freq = ts ц - very distinct
    // ss с to higher (*3/2 or *2) freq = ss с - no change
    // sh ш to lower freq (/2) = mechanic sound
    // ch ч to lower freq (/2) = ч ch-strong
    // ss с to lower freq (/2) = щ sh'
    // [sh - sh' - ss  ш-щ-с] is lower-higher series of the same freq curve


    double[] freqMagRef = freqMagnitudes(soundBufAt(250));

    DataInputStream di = soundFile();
    short[] buf = new short[128];
    int freq = 11025*2;
    SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(freq,16,1,true,true));
    line.open();
    line.start();
    for( int i=0; ; i++ ){
      readAll(di, buf);
      double[] freqMagI = freqMagnitudes(buf);
      double korr = korr0(freqMagRef, freqMagI);
      //Thread.sleep( 1000*buf.length/freq );
      System.out.println(System.currentTimeMillis()%100000+" "
          +i+": "
          +(int)Math.sqrt(sumSq(buf)/buf.length)
          +" korr%="+(int)(korr*100) );

      byte[] b = toBytes(buf);
      line.write(b, 0, b.length);
      //line.drain();
    }
  }

  private DataInputStream soundFile() throws FileNotFoundException {
    DataInputStream di = new DataInputStream(new FileInputStream(
        "C:\\proj\\cr6\\sounds/shshss.voice"));
    return di;
  }

  double sumSq(short[] sh){
    double d=0;
    for( short s : sh ){
      d += ((double)s)*s;
    }
    return d;
  }
  double sumSq(double[] sh){
    double d=0;
    for( double s : sh ){
      d += s*s;
    }
    return d;
  }

  byte[] toBytes(short[] sh) throws IOException{
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for( int i=0; i<sh.length; i++ ){
      dos.writeShort(sh[i]);
    }
    return baos.toByteArray();
  }

  void readAll(DataInput in, short[] sh) throws IOException {
    for( int i=0; i<sh.length; i++ ){
      sh[i]=in.readShort();
    }
  }

  double[] freqMagnitudes(short[] sh){
    double[] d = new double[sh.length];
    for( int i=0; i<sh.length; i++ ){
      d[i]=sh[i];
    }
    DFT dft = new DFT();
    dft.forward(d);
    return dft.getMagnitudes();
  }

  double korr0(double[] d1, double[] d2){
    double sum=0;
    for( int i=0; i<d1.length; i++ ){
      sum += d1[i]*d2[i];
    }
    return sum/Math.sqrt(sumSq(d1))/Math.sqrt(sumSq(d2));
  }
}
