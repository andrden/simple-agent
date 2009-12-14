package audio.cords;

import audio.DFT;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.util.List;
import java.util.Arrays;

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
  private DataInputStream soundFile() throws FileNotFoundException {
    DataInputStream di = new DataInputStream(new FileInputStream(
        "C:\\proj\\cr6\\sounds/shshss.voice"));
    return di;
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

    List<double[]> freqMagRefs = Arrays.asList(
        freqMagnitudes(soundBufAt(250)),
        freqMagnitudes(soundBufAt(339))
        //freqMagnitudes(soundBufAt(240)),
        //freqMagnitudes(soundBufAt(253))
    );

    DataInputStream di = soundFile();
    short[] buf = new short[128];
    int freq = 11025;
    SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(freq,16,1,true,true));
    line.open();
    line.start();
    double oldMight=0;
    for( int i=0; ; i++ ){
      readAll(di, buf);
      double might = Math.sqrt(sumSq(buf)/buf.length);
      double[] freqMagI = freqMagnitudes(buf);
      //Thread.sleep( 1000*buf.length/freq );

      double[] korrs = new double[freqMagRefs.size()];
      double ksum=0;
      StringBuilder kstr=new StringBuilder();
      for( int ki=0; ki<korrs.length; ki++ ){
        korrs[ki] = korr0(freqMagRefs.get(ki), freqMagI);
        ksum += korrs[ki];
        kstr.append( " "+(int)(korrs[ki]*100) );
      }

      System.out.println(System.currentTimeMillis()%100000+" "
          +i+": "
          +(int)might
          +" korr%="+(int)(ksum/korrs.length*100)
          +" []="+kstr );

      byte[] b = toBytes( mightCorrect(buf,oldMight,might,500) );
      line.write(b, 0, b.length);
      //line.drain();
      oldMight = might;
    }
  }

  short[] mightCorrect(short[] src, double might1, double might2, double mightTarget){
    short[] ret = new short[src.length];
    for( int i=0 ;i<src.length; i++ ){
      double k = mightTarget / (might1 + (might2-might1)*(i+1)/src.length);
      ret[i] = (short)(k*src[i]);
    }
    return ret;
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
