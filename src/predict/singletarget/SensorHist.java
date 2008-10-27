package predict.singletarget;

import mem.OneView;

import java.util.*;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.ADTree;
import weka.classifiers.trees.BFTree;
import weka.classifiers.trees.DecisionStump;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;
import com.pmstation.common.utils.PrivateFieldGetter;

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

  Classifier lastUsedClassifier;
  Map<OneView, Object> exampleVals = new HashMap<OneView, Object>();

  List<SRule> srules = new ArrayList<SRule>();
  Object otherRulesResult = null;

  public void printRules(){
    System.out.println(srules+" other="+otherRulesResult);
  }

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
    if( v.prev==null ){
      return;
    }
    addAsCurrent(val, v.prev);
  }

  boolean ruleIsExtra(SRule r){
    for( SRule s : srules ){
      if( r.condWiderIn(s) ){
        return true;
      }
    }
    return false;
  }

  List<OneView> unexplainedExamples(){
    List<OneView> ret = new ArrayList<OneView>();
    for( OneView ve : exampleVals.keySet() ){
      boolean found=false;
      for( SRule r : srules ){
        if( r.condHolds(ve) ){
          found=true;
          break;
        }
      }
      if( !found ){
        ret.add(ve);
      }
    }
    return ret;
  }

  void analyzeNewExample(Object val, OneView vprev){
    if( exampleVals.size()<2 ){
      return;
    }

    boolean explained = verifyRules(val, vprev);
    if( explained ){
      return;
    }
    Object commonResAll = commonResValue(exampleVals.keySet());
    if( commonResAll!=null ){
      otherRulesResult=commonResAll;
      return;
    }

    DecisionStump myClassif  = new DecisionStump();
    WekaBuilder wf = buildClassifier(myClassif);
    Attribute splitAttr = wf.getInstances().attribute((Integer)PrivateFieldGetter.evalNoEx(myClassif,"m_AttIndex"));

    String attName = splitAttr.name();
    Object attVal = wf.attVal(attName, ((Double)PrivateFieldGetter.evalNoEx(myClassif,"m_SplitPoint")).intValue() );
    SRule r = new SRule(attName, attVal, true);
    ruleCheckAndAdd(r);
    SRule rn = new SRule(attName, attVal, false);
    ruleCheckAndAdd(rn);

    List<OneView> unex = unexplainedExamples();
    for( int j=0; j<10 && !unexplainedExamples().isEmpty(); j++ ){
      for( int i=unex.size()-1; i>=0; i-- ){
        if( singleAttrRuleHunting(unex.get(i)) ){
          //must try to find beautiful solution - break;
        }
      }
      unex = unexplainedExamples();
    }

    Object commonResUnex = commonResValue(unex);
    if( commonResUnex!=null ){
      otherRulesResult=commonResUnex;
    }
  }

  boolean singleAttrRuleHunting(OneView vprev){
    Map<String, Object> m = vprev.getViewAll();
    for( String s : m.keySet() ){
      SRule r = new SRule(s, m.get(s), true);
      if( ruleCheckAndAdd(r) ){
        //must try to find beautiful solution - return true;
      }
    }
    return false;
  }

  private boolean ruleCheckAndAdd(SRule r) {
    List<OneView> exList = examplesCondHolds(r);
    if( exList.size()<2 ){
      return false;
    }
    Object commonRes = commonResValue(exList);
    if( commonRes!=null ){
      r.setResult(commonRes);
      if( !ruleIsExtra(r) ){
        srules.add(r);
        return true;
      }
    }
    return false;
  }

  private boolean verifyRules(Object val, OneView vprev) {
    boolean explained=false;
    for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
      SRule sr = i.next();
      if( sr.condHolds(vprev) ){
        if( !sr.getResult().equals(val) ){
          i.remove();
        }else{
          explained=true;
        }
      }
    }
    if( !explained && otherRulesResult!=null ){
      if( !otherRulesResult.equals(val) ){
        otherRulesResult=null;
      }else{
        explained=true;
      }
    }
    return explained;
  }

  Object predictWithDecisionStumpBasedRules(OneView vprev){
    for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
      SRule sr = i.next();
      if( sr.condHolds(vprev) ){
        return sr.getResult();
      }
    }
    return otherRulesResult; // can be null if no global 'other' rule exists
  }

  Object commonResValue(Collection<OneView> exList){
    Object com = null;
    for( OneView v : exList ){
      Object r = exampleVals.get(v);
      if( com!=null ){
        if( !com.equals(r) ){
          return null;
        }
      }
      com=r;
    }
    return com;
  }

  List<OneView> examplesCondHolds(SRule r){
    List<OneView> ret = new ArrayList<OneView>();
    for( TargetHist t : vals.values() ){
      for( OneView v : t.examples ){
        if( r.condHolds(v) ){
          ret.add(v);
        }
      }
    }
    return ret;
  }

  public void printAsTestCase(){
    for( TargetHist th : vals.values() ){
      for( OneView v : th.examples ){
        System.out.println(th.sensorVal+""+v.getViewAll());
      }
    }
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
    exampleVals.put(v, val);

    analyzeNewExample(val, v);
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
    return val.equals( predict(v) );
    //return val.equals( predictWithWeka(v) );
    //return vals.get(val).acceptedByRules(v)!=null;
  }

  public WekaBuilder buildClassifier(Classifier myClassif){
    WekaBuilder wf = new WekaBuilder(myClassif);
    for( TargetHist t : vals.values() ){
      for( OneView v : t.examples ){
        wf.collectAttrs(v, skippedViewKeys);
      }
    }

    for( Object o : vals.keySet() ){
      wf.addForRes(o);
    }
    wf.mkInstances();


    for( Object tv : vals.keySet() ){
      TargetHist t = vals.get(tv);
      for( OneView v : t.examples ){
        wf.addInstance(v, tv.toString());
      }
    }

    Instances ins = wf.getInstances();
    if( ins.numInstances()<1 ){
      return null;
    }


    try {
      wf.getClassifier().buildClassifier(ins);
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
    return wf;
  }

  public Object predictWithWeka(OneView vnew){
//    J48 myClassif = new J48();
//    myClassif.setUnpruned(true);
//    myClassif.setConfidenceFactor(1);

    DecisionStump myClassif  = new DecisionStump();
    lastUsedClassifier = myClassif;

    WekaBuilder wf = buildClassifier(myClassif);

    double d;
    try {
//      { 0.5, 0.5 } - J48 classifies as first, bug! don't use classifyInstance()
//      source code for J48?
//      Weka alg comparison?

//      ((SensorHist)((HashMap.Entry)((HashMap)((Pred)p.p.algs.toArray()[0]).singles).entrySet().toArray()[0]).getValue()).lastUsedClassifier = Type is unknown for '((HashMap)((Pred)p.p.algs.toArray()[0]).singles).entrySet()'
//     -C 0.25 -M 2

      // confidence must be 0.99, min=1

      //      http://grb.mnsu.edu/grbts/doc/manual/J48_Decision_Trees.html

      d = lastUsedClassifier.classifyInstance( wf.mkInstance(vnew) );
      if( d == Instance.missingValue() ){
        return null;
      }

      double[] dist = lastUsedClassifier.distributionForInstance( wf.mkInstance(vnew) );
      int di = (int)d;
      double dProb = dist[ di ];
      for( int i=0; i<dist.length; i++ ){
        if( i!=di && dist[i] >= dProb/1.5 ){
          return null; // there is no really outstanding class predicted
        }
      }
      return wf.getForResObj(di);

    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
  }

  public boolean hasRulesForVal(Object val){
    TargetHist th = vals.get(val);
    return ( th!=null && th.rules.size()>0 );
  }

  public Object predict(OneView v) {
    return predictWithDecisionStumpBasedRules(v);
    //return predictWithWeka(v);
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
