package reinforcement;

import com.pmstation.common.utils.MinMaxFinder;

import java.util.*;

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
 */

//only splitting within epsilon should be changed
public class SoftGreedy2 {
  Map<String,Double> qvals = new LinkedHashMap<String,Double>();
  double epsilon;

  public SoftGreedy2(double epsilon) {
    this.epsilon = epsilon;
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

    Map<String,Double> revLosses = new HashMap<String,Double>();
    double sumRevLosses=0;
    double order = mmf.getMaxVal();
    if( order==0 ){
      order = nonZeroMax();
    }
    //order=1;
    for( String a : qvals.keySet() ){
      if( qvals.get(a)!=mmf.getMaxVal() ){
        double aloss = order/(mmf.getMaxVal() - qvals.get(a));
        sumRevLosses += aloss;
        revLosses.put(a, aloss);
      }
    }

    Map<String,Double> ret = new HashMap<String,Double>();
    double greedyTotal = 1-epsilon;
    if( qvals.size()==mmf.getMaxNames().size() ){
      greedyTotal=1;
    }
    for( String a : qvals.keySet() ){
      double api;
      if( qvals.get(a)==mmf.getMaxVal() ){
        api = greedyTotal/mmf.getMaxNames().size();
      }else{
        api = epsilon*revLosses.get(a)/sumRevLosses;
      }
      ret.put(a, api);
    }


    return ret;
  }
}