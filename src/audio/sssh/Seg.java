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

    Clast(double a, double b) {
      this.a = a;
      this.b = b;
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

  private void localClusterSearch(double[] h, int beg, int end, MinMaxFinder ranges) {
    MinMaxFinder hmax = new MinMaxFinder();
    for( int i=beg; i<=end; i++ ){
      hmax.add(h[i],i);
    }
    double clusterCut = hmax.getMaxVal()/3;
    int a=(Integer)hmax.getMaxNames().get(0);
    int b=a;
    while(a>beg && h[a-1]>clusterCut){
      a--;
    }
    while(b<end && h[b+1]>clusterCut){
      b++;
    }

    while(a>beg && h[a-1]<h[a]){
      a--;
    }
    while(b<end && h[b+1]<h[b]){
      b++;
    }

//    if( beg!=0 && a==beg ){
//      return; // pressing to cut edge
//    }
//    if( end!=h.length-1 && b==end ){
//      return; // pressing to cut edge
//    }

    if( b-a<5 ){
      return;
    }

    System.out.println(toString()+ " cluster idx "+a+" ... "+b);
    clusters.add(new Clast(
        ranges.getMinVal()+a*(ranges.getMaxVal()-ranges.getMinVal())/h.length,
        ranges.getMinVal()+(b+1)*(ranges.getMaxVal()-ranges.getMinVal())/h.length
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
