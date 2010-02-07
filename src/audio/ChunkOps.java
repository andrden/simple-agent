package audio;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Oct 2, 2009
 * Time: 1:01:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkOps {
  double[] sum;
  int count;

  void add(double[] c){
    count++;
    if( sum ==null ){
      sum =c.clone();
    }else{
      for( int i=0; i<c.length; i++ ){
        sum[i]+=c[i];
      }
    }
  }
  double[] avg(){
    double[] r = new double[sum.length];
    for( int i=0; i< sum.length; i++ ){
      r[i]= sum[i]/count;
    }
    return r;
  }

  static double[] hammingWindow(double[] d){
    double[] ret = new double[d.length];
    for( int i=0; i<d.length; i++ ){
      double hamming = 0.54-0.46*Math.cos(2*Math.PI*i/(d.length-1));
      ret[i] = d[i]*hamming;
    }
    return ret;
  }

  static double[] first(double[] d, int count){
    double[] ret = new double[count];
    System.arraycopy(d,0,ret,0,count);
    return ret;
  }

  static double[] toDecibels(double[] d){
    double[] res = new double[d.length];
    for( int i=0;i<d.length; i++ ){
      if(d[i]==0 ){
        res[i] = -100;
      }else{
        res[i] = 20*Math.log10(d[i]);
      }
      if( Double.isInfinite(res[i]) || Double.isNaN(res[i]) ){
        throw new RuntimeException("log() err "+res[i]);
      }
    }
    return res;
  }

  static double sumSquared(double[] d){
    double s = 0;
    for( double di : d ){
      s += di*di;
    }
    return s;
  }

  public static double[] add(double[] d1, double[] d2){
    double[] ret = new double[d1.length];
    for( int i=0; i<d1.length; i++ ){
      ret[i] = d1[i]+d2[i];
    }
    return ret;
  }
  public static double[] div(double[] d1, double k){
    double[] ret = new double[d1.length];
    for( int i=0; i<d1.length; i++ ){
      ret[i] = d1[i]/k;
    }
    return ret;
  }

  public static double[] movingAvg(double[] ar, int len){
    double[] ret = new double[ar.length];
    for( int i=0; i<ar.length; i++ ){
      int half = (len-1)/2;
      int count=0;
      double sum=0;
      for( int j = -half; j<=half; j++ ){
        if( i+j>=0 && i+j<ar.length ){
          count++;
          sum+=ar[i+j];
        }
      }
      ret[i] = sum/count;
    }
    return ret;
  }
  public static double[] freqMagnitudes(short[] sh){
    double[] d = new double[sh.length];
    for( int i=0; i<sh.length; i++ ){
      d[i]=sh[i];
    }
    DFT dft = new DFT();
    dft.forward(d);
    return dft.getMagnitudes();
  }
  public static void readAll(DataInput in, short[] sh) throws IOException {
    for( int i=0; i<sh.length; i++ ){
      sh[i]=in.readShort();
    }
  }
  public static double sum(double[] h){
      return sum(h,0,h.length);
  }
  public static double sum(double[] h, int start, int endExclusive){
      double s = 0;
      for( int i=start; i<endExclusive; i++ ){
          s += h[i];
      }
      return s;
  }

}
