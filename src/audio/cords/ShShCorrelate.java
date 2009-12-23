package audio.cords;

import audio.DFT;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RefineryUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Dec 10, 2009
 * Time: 5:42:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShShCorrelate {
  int chunkSize;
  short[] buf;
  int freq = 11025;

  public ShShCorrelate(int chunkSize) {
    this.chunkSize = chunkSize;
    buf = new short[chunkSize];
  }

  public static void main(String[] args) throws Exception{
    //new ShShCorrelate(128).play();
    new ShShCorrelate(128).playNoiseModulated();
  }
  private DataInputStream soundFile() throws FileNotFoundException {
    DataInputStream di = new DataInputStream(new FileInputStream(
        //"C:\\proj\\cr6\\sounds/onetwothree.voice"
        "C:\\proj\\cr6\\sounds/shshss.voice"
    ));

    return di;
  }

  short[] soundBufAt(int pos) throws Exception{
    DataInputStream di = soundFile();
    short[] buf = new short[chunkSize];
    int freq = 11025;
    for( int i=0; i<=pos; i++ ){
      readAll(di, buf);
    }
    return buf;
  }

  void playNoiseModulated() throws Exception{
    List<double[]> freqMagRefs = Arrays.asList(
        freqMagnitudes(soundBufAt(1406))
     );
    // #1405 - russian sound chchchch
    playNoiseModulated(freqMagRefs);
  }

  void playNoiseModulated(List<double[]> freqMagRefs) throws Exception{
    SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(freq,16,1,true,true));
    line.open();
    line.start();

    int kernelLen=21;
    short[] remain=new short[kernelLen-1];
    NoiseRnd noiseRnd = new NoiseRnd();

    for(;;){
      for(double[] freqMagI : freqMagRefs ){
/*
        for( int i=0; i<50; i++ ){
          //freqMagI[i]=0;
        }
*/
/*
        for( int i=30; i<freqMagI.length; i++ ){
          //freqMagI[i]=0;
        }
*/
        for( int i=35; i<45; i++ ){
          //freqMagI[i]=0;
        }

        short[] convolve=Filter.apply(noiseRnd.next(buf.length),freqMagI,kernelLen,0.1);
        buf = Filter.convolveOverlap(remain, convolve);

        double[] mnew = freqMagnitudes(buf);

        display(Arrays.asList(freqMagI,  mnew));

        byte[] b = toBytes( buf );
        line.write(b, 0, b.length);
      }
    }

  }

  void display(List<double[]> freqMagRefs) throws Exception{
      XYSeriesCollection data = new XYSeriesCollection();
      for (int i = 0; i <freqMagRefs.size(); i++) {
        XYSeries series = new XYSeries("Series " + i);
        double[] fdata = freqMagRefs.get(i);
        double koef=Math.sqrt(sumSq(fdata));
        for (int j = 0; j <fdata.length; j++) {
          series.add(j, fdata[j]/koef);
        }
        data.addSeries(series);
      }

      RegressionDemo demo = new RegressionDemo(data);
      RefineryUtilities.centerFrameOnScreen(demo);
      demo.setVisible(true);
      while(demo.isVisible()){
        Thread.sleep(0);
      }
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
    SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(freq,16,1,true,true));
    line.open();
    line.start();
    double oldMight=0;

    int kernelLen=21;
    NoiseRnd noiseRnd = new NoiseRnd();
    short[] remain=new short[kernelLen-1];

    for( int i=0; ; i++ ){
      readAll(di, buf);
      double might = might(buf);
      //buf=mightCorrect(buf,oldMight,might,200);
      double[] freqMagI = freqMagnitudes(buf);

      short[] bufOrig=buf;
      short[] convolve=Filter.apply(noiseRnd.next(buf.length),freqMagI,kernelLen,0.1);
      buf = Filter.convolveOverlap(remain, convolve);
      double[] freqMagIModif = freqMagnitudes(buf);

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
          +(int)might+"->"+(int)might(buf)
          +" korr%="+(int)(ksum/korrs.length*100)
          +" []="+kstr );

      //byte[] b = toBytes( mightCorrect(buf,oldMight,might,500) );
      byte[] b = toBytes( buf );

      line.write(b, 0, b.length);
      //line.drain();
      oldMight = might;
    }
  }

  double might(short[] buf){
    //double might = Math.sqrt(sumSq(buf)/buf.length);

    double s=0;
    for( int i=1; i<buf.length; i++ ){
      double diff = buf[i] - buf[i-1];
      s += diff*diff;
    }
    return Math.sqrt(s/(buf.length-1));
  }

  short[] mightCorrect(short[] src, double might1, double might2, double mightTarget){
    short[] ret = new short[src.length];
    for( int i=0 ;i<src.length; i++ ){
      double k = mightTarget / (might1 + (might2-might1)*(i+1)/src.length);
      double newv = k * src[i];
      if( newv>Short.MAX_VALUE ){
        newv = Short.MAX_VALUE;
      }
      if( newv<Short.MIN_VALUE ){
        newv = Short.MIN_VALUE;
      }
      ret[i] = (short) newv;
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
