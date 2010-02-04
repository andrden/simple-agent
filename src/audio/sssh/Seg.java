package audio.sssh;

import com.pmstation.common.utils.MinMaxFinder;

import java.util.List;
import java.util.ArrayList;

import audio.ChunkOps;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Jan 25, 2010
* Time: 5:55:10 PM
* To change this template use File | Settings | File Templates.
*/
class Seg {
  int a1, a2,  b1, b2;
  List<Double> points = new ArrayList<Double>();
  List<Clast> clusters;

  Seg(int a1, int a2, int b1, int b2) {
    if( a2>a1 ){
      this.a1 = a1;
      this.a2 = a2;
    }else{
      this.a1 = a2;
      this.a2 = a1;
    }

    if(b2>b1){
      this.b1 = b1;
      this.b2 = b2;
    }else{
      this.b1 = b2;
      this.b2 = b1;
    }
  }

  class Clast{
    double a;
    double b;
    double quality;

    Clast(double a, double b, double quality) {
      this.a = a;
      this.b = b;
      this.quality=quality;
    }

    boolean contains(double v){
      return v>a && v<b;
    }
  }

  int clusterIdx(double v){
    for( int i=0; i<clusters.size(); i++ ){
      if( clusters.get(i).contains(v) ){
        return i+1;
      }
    }
    return 0;
  }

  double[] histo(int size){
    MinMaxFinder mmf = new MinMaxFinder();
    for( double d : points ){
      mmf.add(d,"");
    }
    double[] h = new double[size];
    for( double d : points ){
      double perc=(d-mmf.getMinVal())/(mmf.getMaxVal()-mmf.getMinVal());
      int n = (int)(size*perc);
      if( n>=size-1 ){
        n=size-1;
      }
      h[n]++;
    }
    return h;
  }

  void clusterSearch(int histoSize, int movingAvgSize){
    кластер не может быть повёрнут своей максимальной частью к соседнему
    кластеру!!!

    clusters = new ArrayList<Clast>();
    MinMaxFinder mmf = new MinMaxFinder();
    for( double d : points ){
      mmf.add(d,"");
    }
    double[] h = new double[histoSize];
    for( double d : points ){
      double perc=(d-mmf.getMinVal())/(mmf.getMaxVal()-mmf.getMinVal());
      int n = (int)(histoSize*perc);
      if( n>=histoSize-1 ){
        n=histoSize-1;
      }
      h[n]++;
    }

    double[] havg = ChunkOps.movingAvg(h, movingAvgSize);
    localClusterSearch(havg,0,havg.length-1, mmf);
  }

  interface Slope{
    double val(int offset);
    double maxOffset();
  }

  int moveDown(Slope s){
    double max=s.val(0);
    int i=0;
//    double clusterCut = max * 0.8;
//    while( i<s.maxOffset() && s.val(i+1)>clusterCut ){
//      i++;
//    }
    while( i<s.maxOffset() && s.val(i+1)<s.val(i) ){
      i++;
    }
    return i;
  }
//  boolean shouldStep(Slope s, int curr){
//    int j=0;
//    while( curr+j<s.maxOffset() ){
//
//    }
//  }

  private void localClusterSearch(final double[] h,
                                  final int beg, final int end, MinMaxFinder ranges) {
    MinMaxFinder hmax = new MinMaxFinder();
    for( int i=beg; i<=end; i++ ){
      hmax.add(h[i],i);
    }
    final int top=(Integer)hmax.getMaxNames().get(0);
    Slope right = new Slope(){
      public double val(int offset) {
        return h[top+offset];
      }
      public double maxOffset() {
        return end-top;
      }
    };
    Slope left = new Slope(){
      public double val(int offset) {
        return h[top-offset];
      }
      public double maxOffset() {
        return top-beg;
      }
    };

    int a = top - moveDown(left);
    int b = top + moveDown(right);

    if( b-a<5 ){
      return;
    }
    double worstSideVal = Math.max(h[a],h[b]);
    double quality = hmax.getMaxVal() / worstSideVal;

    System.out.println(toString()+ " cluster idx "+a+" ... "+b+ " q="+quality);
    clusters.add(new Clast(
        ranges.getMinVal()+a*(ranges.getMaxVal()-ranges.getMinVal())/h.length,
        ranges.getMinVal()+(b+1)*(ranges.getMaxVal()-ranges.getMinVal())/h.length,
        quality
        ));

    if( a>beg ){
      localClusterSearch(h,beg,a-1, ranges);
    }
    if( b<end ){
      localClusterSearch(h,b+1,end, ranges);
    }
  }

  @Override
  public String toString() {
    return a1+" "+a2+" "+b1+" "+b2;
  }

  double comp(double[] freqMagI){
    double sa=0;
    for( int i=a1; i<=a2; i++ ){
      sa+=freqMagI[i];
    }
    double sb=0;
    for( int i=b1; i<=b2; i++ ){
      sb+=freqMagI[i];
    }
    return sa/sb;
  }

}
