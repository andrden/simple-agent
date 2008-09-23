package predict.singletarget;

import mem.OneView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 4/8/2008
 * Time: 17:37:58
 */
public class SensorHist {
  final String sensorName;
  Map<Object, TargetHist> vals = new HashMap<Object, TargetHist>();
  Set<String> skippedViewKeys;

  public SensorHist(String sensorName) {
    this.sensorName = sensorName;
  }

  public void setSkippedViewKeys(Set<String> skippedViewKeys) {
    this.skippedViewKeys = skippedViewKeys;
  }

  /**
   *
   * @param val
   * @param v - next state, v.prev finishes info which can be used for prediction
   */
  public void add(Object val, OneView v) {
    TargetHist th = vals.get(val);
    if (th == null) {
      th = new TargetHist(this, val);
      vals.put(val, th);
    }
    th.addExample(v.prev);
  }

  public Object predict(OneView v) {
    if( vals.size()==1 ){
      return vals.keySet().iterator().next();
    }

    Object retVal = null;
    long hitCountR=0;
    for (Object val : vals.keySet()) {
      TargetHist th = vals.get(val);
      Rule raccept = th.acceptedByRules(v);
      if (raccept!=null) {
        retVal = val;
        hitCountR++;
      }
    }
    if( hitCountR==1 ){
      return retVal;
    }
    if( hitCountR>1 ){
      return null; //conflict
    }

    // now when rules tell nothing trying less definite comparisons: 
    long hitCount2=0;
    Rule reason2=null;
    for (Object val : vals.keySet()) {
      TargetHist th = vals.get(val);
      Rule reason = th.reasonablyAccepted(v);
      if (reason!=null) {
        // consider only rules with minimum depth
        if( reason2==null || reason2.ruleMaxDepth()>reason.ruleMaxDepth() ){
          retVal = val;
          hitCount2=1;
          reason2 = reason;
        }else if( reason2.ruleMaxDepth()==reason.ruleMaxDepth() ){
          hitCount2++; // will cause conflict
        }
      }
    }
    if( hitCount2>1 ){
      return null; //conflict
    }
    return retVal;
  }

  public boolean skipViewKey(String k) {
    return skippedViewKeys!=null && skippedViewKeys.contains(k);
  }

  public String getSensorName() {
    return sensorName;
  }
}
