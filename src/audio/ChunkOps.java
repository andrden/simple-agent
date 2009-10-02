package audio;

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
}
