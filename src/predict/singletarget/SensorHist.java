package predict.singletarget;

import mem.OneView;

import java.util.*;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Instance;

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
   * @param v - next state, v.prev finishes info which can be used for prediction,
   * v usually has info describing the result because of which this categorization is taking place.
   */
  public void add(Object val, OneView v) {
    TargetHist th = targetHist(val);
    th.addExample(v.prev);
  }

  private TargetHist targetHist(Object val) {
    TargetHist th = vals.get(val);
    if (th == null) {
      th = new TargetHist(this, val);
      vals.put(val, th);
    }
    return th;
  }

  public void addAsCurrent(Object val, OneView v) {
    TargetHist th = targetHist(val);
    th.addExample(v);
  }

  public int valsSize(){
    return vals.size();
  }

  /**
   * Can be conflicting with other values if our prior experience is limited.
   * @param v
   * @param val
   * @return
   */
  public boolean valAcceptedByRules(OneView v, Object val){
    return val.equals( predictWithWeka(v) );
    //return vals.get(val).acceptedByRules(v)!=null;
  }

  public Object predictWithWeka(OneView vnew){
    WekaBuilder wf = new WekaBuilder();
    for( TargetHist t : vals.values() ){
      for( OneView v : t.examples ){
        wf.collectAttrs(v, skippedViewKeys);
      }
    }

    LinkedHashSet<String> forRes = new LinkedHashSet<String>();
    List forResObj = new ArrayList();
    for( Object o : vals.keySet() ){
      forResObj.add(o);
      forRes.add(o.toString());
    }
    wf.mkInstances(forRes);


    for( Object tv : vals.keySet() ){
      TargetHist t = vals.get(tv);
      for( OneView v : t.examples ){
        wf.addInstance(v, tv.toString());
      }
    }

    Instances ins = wf.getInstances();
    Classifier classif = new J48();
    try {
      classif.buildClassifier(ins);
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }

    double d;
    try {
      d = classif.classifyInstance( wf.mkInstance(vnew) );
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }

    if( d== Instance.missingValue() ){
      return null;
    }
    return forResObj.get((int)d);
  }

  public boolean hasRulesForVal(Object val){
    TargetHist th = vals.get(val);
    return ( th!=null && th.rules.size()>0 );
  }

  public Object predict(OneView v) {
    return predictWithWeka(v);
    //return predictSimpleWay(v);
  }

  private Object predictSimpleWay(OneView v) {
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

  public String toString() {
    StringBuilder sb = new StringBuilder(sensorName);
    sb.append(" {");
    boolean first=true;
    for( Object v : vals.keySet() ){
      if( !first ){
        sb.append(",");
      }
      sb.append(v);
      first = false;
    }
    sb.append("}");
    return sb.toString();
  }
}
