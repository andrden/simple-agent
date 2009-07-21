package reinforcement;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jul 21, 2009
 * Time: 5:52:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Damper {
  double PRECIS=0.05;

  LinkedList<Double> vals = new LinkedList<Double>();
  int window=1;
  long totalAdded=0;

  void add(double val){
    totalAdded++;
    vals.addLast(val);
    if( vals.size()>=3 ){
      double v2 = value(vals.subList(vals.size()-window-2, vals.size()-2));
      double v1 = value(vals.subList(vals.size()-window-1, vals.size()-1));
      double v0 = value();
      if( (v2-v1)*(v1-v0)<0 && v2-v1>v1*PRECIS ){
        window++;
      }
      while(vals.size()>window+1){
        vals.removeFirst();
      }
    }
  }
  double value(){
    return value(vals.subList(vals.size()-window, vals.size()));
  }
  double value(List<Double> l){
    double d = 0;
    for( Double di : l ){
      d += di;
    }
    return d/l.size();
  }

  public static void main(String[] args){
    Damper d = new Damper();
    for( int i=0; i<10000; i++ ){
      double r = Math.random();
//      double r=10;
//      if( i>50 ) r=20;
//      if( i>100 ) r=30;
//      double r=1;
//      if( Math.random()<0.5 ) r=2;
      d.add(r);
      System.out.println(d.value()+" "+r+" "+d.window);
    }
    d=d;
  }
}
