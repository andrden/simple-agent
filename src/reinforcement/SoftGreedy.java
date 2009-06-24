package reinforcement;

import com.pmstation.common.utils.MinMaxFinder;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Soft policy mixing exploration and exploitation
 * and choosing very bad opertions very seldom
 * to avoid losing too much - should be better than
 * epsilon-greedy because that ignores the fact that some
 * actions are very, very bad.
 *
 * NO!!! not always - large negative qvals with little difference
 * cause this alg. to assy same probability to them - thus
 * can't find way out of a maze. Epsilon-greedy always seeks the best anyway,
 * so maybe only splitting within epsilon should be changed,
 * not whole distributions
 *
 * Howevery in animal practice reward values are never precise (!!!),
 * so differentiating nearly same large negative values seems strange.
 * In animal practice rewarding only at finish would not do, there must be
 * constant related reward - probably over another channel - goal based,
 * created by creature from original channel - raw real reward. 
 */
public class SoftGreedy {
  Map<String,Double> qvals = new HashMap<String,Double>();
  double maxLoss;

  public SoftGreedy(double maxLoss) {
    this.maxLoss = maxLoss;
  }

  public void put(String a, double q){
    qvals.put(a, q);
  }

  public String policyAction(){
    double r = Math.random();
    for( Map.Entry<String,Double> me : policy().entrySet() ){
      r -= me.getValue();
      if( r<=0 ){
        return me.getKey();
      }
    }
    throw new RuntimeException("policy "+policy());
  }

  double nonZeroMax(){
    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : qvals.keySet() ){
      if( qvals.get(a)!=0 ){
        mmf.add(qvals.get(a), a);
      }
    }
    return mmf.getMaxVal();
  }

  public Map<String,Double> policy(){
    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : qvals.keySet() ){
      mmf.add(qvals.get(a), a);
    }
    int subOptCount = qvals.size() - mmf.getMaxNames().size();
    double singleLoss = Math.abs(mmf.getMaxVal()) * maxLoss/subOptCount;
    if( mmf.getMaxVal()==0 && mmf.getMinVal()<0 ){
      singleLoss = Math.abs(nonZeroMax()) * maxLoss/subOptCount;
//      throw new RuntimeException("not implemented yet mmf.getMaxVal()==0 && mmf.getMinVal()<0 " +
//          "qvals="+qvals);
    }

    Map<String,Double> ret = new HashMap<String,Double>();
    double piRem=1;
    //double piMaxSubopt=0;
    for( String a : qvals.keySet() ){
      if( qvals.get(a)!=mmf.getMaxVal() ){
        double aPi = singleLoss / (mmf.getMaxVal() - qvals.get(a));
        ret.put(a,aPi);
        //piMaxSubopt = Math.max(piMaxSubopt, aPi);
        piRem -= aPi;
      }
    }
    List maxList = mmf.getMaxNames();
    double aPi = piRem/mmf.getMaxNames().size();
    for( Iterator<String> i = ret.keySet().iterator(); i.hasNext(); ){
      String a = i.next();
      if( ret.get(a)>aPi ){
        piRem += ret.get(a);
        maxList.add(a);
        i.remove();
      }
    }
    if( piRem<=0 ){
      throw new RuntimeException("piRem<=0 "+piRem);
    }

    double aPiNew = piRem/maxList.size();
    for( Object o : maxList ){
      String a = (String)o;
//      if( piMaxSubopt>aPiNew ){
//        throw new RuntimeException("piMaxSubopt>aPi "+piMaxSubopt+" "+aPiNew);
//      }
      ret.put(a,aPiNew);
    }
    for( double r : ret.values() ){
      if( r<=0 ){
        throw new RuntimeException("r<=0 "+ret);
      }
    }
    return ret;
  }
}
