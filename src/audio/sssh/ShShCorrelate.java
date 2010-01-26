package audio.sssh;

import audio.DFT;
import audio.cords.old.LinearRegression;
import audio.sssh.NoiseRnd;
import audio.cords.Filter;
import audio.cords.SimplestChart;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.util.*;

import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RefineryUtilities;
import utils.Utils;
import com.pmstation.common.utils.MinMaxFinder;

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
    //new ShShCorrelate(128).clusterSegments();
    new ShShCorrelate(128).graphSegments();

    //new ShShCorrelate(128).extractClusters();
    //new ShShCorrelate(128).play();
    //new ShShCorrelate(128).playNoiseModulated();
  }

  private void extractClusters() throws Exception{
    DataInputStream di = soundFile();
    Seg seg1 = new Seg(25,35,45,55); // size=11, size=11
/*  shsh discriminators:
seg2=57 61 7 12
seg2=17 36 0 63

whispering sounds discriminator: seg2=10 12 47 57

sssss discriminator: seg2=25 48 14 45
*/

      LastVals lv = new LastVals();
      lv.histoMaxMin=0;
      lv.historMaxMax=5;
      lv.histoMaxCount=20;
      LastValsCmp lvc = new LastValsCmp();

      try{
      for( int i=0; /*i<1500*/; i++ ){
        readAll(di, buf);
        double might = might(buf);
        final double[] freqMagI = freqMagnitudes(buf);
        double c1 = seg1.comp(freqMagI);

        lvc.add(c1);
        if( i%10==0 ){
          lvc.desc();
        }

        lv.add(c1);
        if( lv.vals.size()==lv.LEN ){
          lv.updateHistoMax();
        }
//        if( (i+1)%5==0 ){
//          for( int ii=0; ii<50; ii++ ){
//            lv.cluster();
//          }
//          /*
//          System.out.println("histo up to "+i+":");
//
//          double[] h = lv.histo(30);
//          for( double d : h ){
//            System.out.println(d);
//          }
//          */
//        }
      }
      }catch(Exception e){
        e.printStackTrace();
      }
    display(Arrays.asList(lv.histoMax,lv.histoMax));
    Utils.breakPoint();
  }

  static class Cluster{
    double a;
    double b;

    Cluster(double a, double b) {
      this.a = a;
      this.b = b;
    }
    boolean covers(double v){
      return v>=a && v<=b;
    }
  }

  static class ValDesc{
    double avg;
    double disp;

    ValDesc(List<Double> vs) {
      double sum=0;
      for( double d : vs ){
        sum += d;
      }
      avg = sum/vs.size();

      double sum2=0;
      for( double d : vs ){
        sum2 += (d-avg)*(d-avg);
      }
      disp = Math.sqrt(sum2/vs.size());
    }

    @Override
    public String toString() {
      return avg+" ~ "+disp+" ["+(avg-disp)+" "+(avg+disp)+"]";
    }
  }
  static class LastValsCmp{
    int LEN=20;
    LinkedList<Double> vals = new LinkedList<Double>();
    void add(double v){
      vals.addLast(v);
      if( vals.size()>LEN ){
        vals.removeFirst();
      }
    }
    void desc(){
      if( vals.size()==LEN ){
        System.out.println( new ValDesc(vals.subList(0,10)) );
      }
    }
  }

  static class LastVals{
    int LEN=50;
    LinkedList<Double> vals = new LinkedList<Double>();
    List<Cluster> clusters = new ArrayList<Cluster>();

    double[] histoMax;
    double histoMaxMin, historMaxMax;
    int histoMaxCount;


    void add(double v){
      vals.addLast(v);
      if( vals.size()>LEN ){
        vals.removeFirst();
      }
    }

    void updateHistoMax(){
      if( histoMax==null ){
        histoMax = new double[histoMaxCount];
      }
      double[] h = new double[histoMaxCount];
      for( double d : vals ){
        double perc=(d-histoMaxMin)/(historMaxMax - histoMaxMin);
        int n = (int)(h.length*perc);
        if( n>=h.length-1 ){
          n=h.length-1;
        }
        h[n]++;
      }
      for( int i=0; i<h.length; i++ ){
        histoMax[i] = Math.max(histoMax[i],h[i]);
      }
    }

    boolean clustersCover(double d){
      boolean covers=false;
      for( Cluster c : clusters ){
        if( c.covers(d) ){
          covers=true;
          break;
        }
      }
      return covers;
    }

    boolean clustersIntersect(double a, double b){
      for( Cluster c : clusters ){
        if( a<c.a && b<c.a || a>c.b && b>c.b ){
        }else{
          return true;
        }
      }
      return false;
    }

    void cluster(){
      List<Double> scatteredVals = new ArrayList<Double>();
      for( double d : vals ){
        if( !clustersCover(d) ){
          scatteredVals.add(d);
        }
      }

      MinMaxFinder mmf = new MinMaxFinder();
      for( double d : scatteredVals ){
        mmf.add(d,"");
      }
      //System.out.println(mmf.getMinVal()+ " "+mmf.getMaxVal());
      if( mmf.getMinVal()==mmf.getMaxVal() ){
        return;
      }
      double a = 0;
      double b = 0;
      while( b<=a || clustersIntersect(a,b) ){
        a = mmf.getMinVal() + Math.random()*(mmf.getMaxVal()-mmf.getMinVal());
        b = mmf.getMinVal() + Math.random()*(mmf.getMaxVal()-mmf.getMinVal());
      }
      double[] h = new double[10];
      for( double d : scatteredVals ){
        if( d>a && d<b ){
          double perc=(d-a)/(b-a);
          int n = (int)(h.length*perc);
          if( n>=h.length-1 ){
            n=h.length-1;
          }
          h[n]++;
        }
      }
      if( isCluster(h) ){
        clusters.add( new Cluster(a,b) );
        System.err.println("cluster add "+a+" "+b);
      }
    }

    boolean isCluster(double[] h){
      int i=0;
      while(i+1<h.length && h[i+1]>=h[i]){
        i++;
      }
      int j=h.length-1;
      while(j-1>=0 && h[j-1]>=h[j]){
        j--;
      }
      double diff=5;//10;
      if( h[i]<diff ){
        return false;
      }
      if( h[0]*diff>h[i] ){
        return false;
      }
      if( h[h.length-1]*diff>h[i] ){
        return false;
      }
      return i>=j;
    }

    double[] histo(int size){
      MinMaxFinder mmf = new MinMaxFinder();
      for( double d : vals ){
        mmf.add(d,"");
      }
      System.out.println(mmf.getMinVal()+ " "+mmf.getMaxVal());
      double[] h = new double[size];
      for( double d : vals ){
        double perc=(d-mmf.getMinVal())/(mmf.getMaxVal()-mmf.getMinVal());
        int n = (int)(size*perc);
        if( n>=size-1 ){
          n=size-1;
        }
        h[n]++;
      }
      return h;
    }

  }

  private DataInputStream soundFile() throws FileNotFoundException {
    // "C:\\proj\\cr6\\sounds/onetwothree.voice" says the following:
    // "рас, рас, рас, два, три, четыре, пять ... в веб-камере мирофон ещё есть..
    // . правда не понял где он там ..."


    DataInputStream di = new DataInputStream(new FileInputStream(
        //"C:\\proj\\cr6\\sounds/onetwothree.voice"
        "C:\\proj\\cr6\\sounds/shshss.voice"
        //"C:\\Projects\\simple-agent\\sounds/shshss.voice"
    ));

/*
Mapping of sounds/shshss.voice:
200-300 shsh
380-480 ssss
565-655 shsh
745-840 ssss
930-1030 shsh
1140-1260 ssss
1400-1415 ch!
...chch
...chch
...shsh
...ssss
...chch
~=2500 ss
*/

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
/*
    display(Arrays.asList(
        */
/*freqMagnitudes(soundBufAt(1399)),
        freqMagnitudes(soundBufAt(1400)),
        freqMagnitudes(soundBufAt(1401)),*/
/*
        freqMagnitudes(soundBufAt(1402)),
        freqMagnitudes(soundBufAt(1403)),
        freqMagnitudes(soundBufAt(1404)),
        freqMagnitudes(soundBufAt(1405)),
        freqMagnitudes(soundBufAt(1406))
    ));
*/

    List<double[]> freqMagRefs = Arrays.asList(
        freqMagnitudes(soundBufAt(1405))
     );


    // #1405 - russian sound chchchch
    // #1402 - just white noise
    // #1401 - like waterfall
    // #1400 - remote street with traffic
    playNoiseModulated(freqMagRefs);
  }

  void playNoiseModulated(List<double[]> freqMagRefs) throws Exception{
    for( double[] dd : freqMagRefs ){
      blockAvg(dd, 8);
    }
    
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

        short[] convolve= Filter.apply(noiseRnd.next(buf.length),freqMagI,kernelLen,0.1);
        buf = Filter.convolveOverlap(remain, convolve);

        double[] mnew = freqMagnitudes(buf);

        //display(Arrays.asList(freqMagI,  mnew));

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
        double koef=1;//Math.sqrt(sumSq(fdata));
        for (int j = 0; j <fdata.length; j++) {
          series.add(j, fdata[j]/koef);
        }
        data.addSeries(series);
      }

      SimplestChart demo = new SimplestChart(data);
      RefineryUtilities.centerFrameOnScreen(demo);
      demo.setVisible(true);
      while(demo.isVisible()){
        Thread.sleep(0);
      }
      Utils.breakPoint();
  }

  void clusterSegments()  throws Exception{
    DataInputStream di = soundFile();
    Random r = new Random();

    List<Seg> segs = new ArrayList<Seg>();
    Seg seg1 = new Seg(25,35,45,55); // size=11, size=11
    segs.add(seg1);
    segs.add( new Seg(3, 15, 45, 63) );
    segs.add(new Seg(32, 42, 0, 38));
    segs.add(new Seg(5, 22, 1, 7));
    segs.add(new Seg(20, 49, 3, 35));

    for( int i=0; i<10; i++ ){
      Seg seg2 = new Seg(r.nextInt(65),r.nextInt(65),r.nextInt(65),r.nextInt(65));
      segs.add(seg2);
    }
/*
29 64 12 54 cluster idx 11 ... 19
29 64 12 54 cluster idx 37 ... 42
29 64 12 54 cluster idx 47 ... 66
     */

    //List<Seg> segs = Arrays.asList(seg1, seg2);
    System.out.println("segs="+segs);
    try{
      for( int i=0; /*i<250*/; i++ ){
        readAll(di, buf);
        final double[] freqMagI = freqMagnitudes(buf);
        for( Seg s : segs ){
          double c = s.comp(freqMagI);
          s.points.add(c);
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    for( Seg s : segs ){
      s.clusterSearch(100);
    }

    int[] changes = new int[segs.size()];
    try{
      DataInputStream di2 = soundFile();
      int[] oldClusterIdx = new int[segs.size()];
      boolean firstRow=true;
      for( int i=0; /*i<250*/; i++ ){
        readAll(di2, buf);
        final double[] freqMagI = freqMagnitudes(buf);
        double might = might(buf);
        System.out.printf("%4d  %5.1f   " , i,  might);
        for( int j=0; j<segs.size(); j++ ){
          Seg s = segs.get(j);
          double c = s.comp(freqMagI);
          final int clustIdx = s.clusterIdx(c);
          if( !firstRow && clustIdx!=oldClusterIdx[j] ){
            changes[j]++;
          }
          oldClusterIdx[j] = clustIdx;
          System.out.print(clustIdx +" ");
        }
        System.out.println();
        firstRow=false;
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    for( int j=0; j<segs.size(); j++ ){
      Seg s = segs.get(j);
      System.out.println(s+" changes="+changes[j]);
    }
  }

  void graphSegments() throws Exception{
    DataInputStream di = soundFile();
    List<Double> mights = new ArrayList<Double>();
    List<Double> korrs = new ArrayList<Double>();
    Random r = new Random();

    Seg seg1 = new Seg(25,35,45,55); // size=11, size=11
    //Seg seg1 = new Seg(45,55, 25,35);
    //Seg seg1 = new Seg(r.nextInt(65),r.nextInt(65),r.nextInt(65),r.nextInt(65));
    //Seg seg1 = new Seg(3, 15, 45, 63); // - wispering sounds discriminator

    //double[] refKorrPoint = freqMagnitudes(soundBufAt(250));
    double[] refKorrPoint = freqMagnitudes(soundBufAt(2501));
    //Seg seg2 = new Seg(50,55,60,64);
    Seg seg2 = new Seg(r.nextInt(65),r.nextInt(65),r.nextInt(65),r.nextInt(65));
/*  shsh discriminators:
seg2=57 61 7 12 
seg2=17 36 0 63

whispering sounds discriminator: seg2=10 12 47 57

sssss discriminator: seg2=25 48 14 45
*/

    System.out.println("seg2="+seg2.toString());
    try{
      for( int i=0; /*i<250*/; i++ ){
        readAll(di, buf);
        double might = might(buf);
        mights.add(might/300);
        final double[] freqMagI = freqMagnitudes(buf);
        korrs.add(korr0(refKorrPoint, freqMagI));

        double c1 = seg1.comp(freqMagI);
        seg1.points.add(c1);
        double c2 = seg2.comp(freqMagI);
        seg2.points.add(c2);

        System.out.println(""+i+" "+might+" "+c1+" "+c2);
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    seg1.clusterSearch(100);

    display(Arrays.asList(toArr(mights),
         toArr(seg1.points)
        //toArr(korrs)
        /*, toArr(seg2.points)*/));
    List<Double> lrnd = new ArrayList<Double>(seg1.points);
    Collections.shuffle(lrnd);
    display(Arrays.asList(toArr(lrnd)));
    display(Arrays.asList(seg1.histo(100)));
  }


  double[] toArr(List<Double> l){
    double[] r = new double[l.size()];
    for( int i=0; i<l.size(); i++ ){
      r[i] = l.get(i);
    }
    return r;
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
    for( double[] dd : freqMagRefs ){
      blockAvg(dd, 8);
    }

    DataInputStream di = soundFile();
    SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(freq,16,1,true,true));
    line.open();
    line.start();
    double oldMight=0;

    int kernelLen=121;
    NoiseRnd noiseRnd = new NoiseRnd();
    short[] remain=new short[kernelLen-1];

    double[] freqMagIPrev=null;
    for( int i=0; ; i++ ){
      readAll(di, buf);
      double might = might(buf);
      //buf=mightCorrect(buf,oldMight,might,200);
      final double[] freqMagI = freqMagnitudes(buf);
      LinearRegression linRegr = new LinearRegression();
      //linRegr.addByIdx(freqMagI);
      double koef=Math.sqrt(sumSq(freqMagI));
      for (int j = 0; j <freqMagI.length; j++) {
        linRegr.add(j, freqMagI[j]/koef);
      }


      short[] bufOrig=buf;
      blockAvg(freqMagI, 8);
      short[] convolve=Filter.apply(noiseRnd.next(buf.length),freqMagI,kernelLen,0.02);
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

      double korPr = (freqMagIPrev==null) ? 0 :  korr0(freqMagI, freqMagIPrev);
      System.out.println(//System.currentTimeMillis()%100000+" "
          i+": "
          +(int)might+"->"+(int)might(buf)
          +" korr%="+(int)(ksum/korrs.length*100)
          +" []="+kstr+ " b="+linRegr.getB()*10000+" kp"+(int)(korPr*100) );

      //byte[] b = toBytes( mightCorrect(buf,oldMight,might,500) );
      byte[] b = toBytes( buf );

      line.write(b, 0, b.length);
      //line.drain();
      oldMight = might;
      freqMagIPrev = freqMagI;
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

  void blockAvg(double[] d, int blocks){
    int blockSize = d.length/blocks;
    for( int i=0; i<blocks; i++ ){
      double s = 0;
      for( int j=0; j<blockSize; j++ ){
        s += d[i*blockSize+j];
      }
      s /= blockSize;
      for( int j=0; j<blockSize; j++ ){
        d[i*blockSize+j]=s;
      }
    }
  }

  double korr0(double[] d1, double[] d2){
    d1 = d1.clone();
    d2 = d2.clone();
    for( int i=0; i<d1.length/3; i++ ){
      d1[i]=0;
      d2[i]=0;
    }

    double sum=0;
    for( int i=0; i<d1.length; i++ ){
      sum += d1[i]*d2[i];
    }
    return sum/Math.sqrt(sumSq(d1))/Math.sqrt(sumSq(d2));
  }
}
