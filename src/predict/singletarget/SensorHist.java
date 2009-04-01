package predict.singletarget;

import mem.OneView;

import java.util.*;

import weka.classifiers.trees.DecisionStump;
import weka.core.Instance;
import com.pmstation.common.utils.MinMaxFinder;
import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 4/8/2008
 * Time: 17:37:58
 */
public class SensorHist extends HistSuggest{
  final String sensorName;

  List<OneView> exList = new ArrayList<OneView>();

  List<PRule> prules = new ArrayList<PRule>();

  OneViewToVal viewToValStatic = new OneViewToVal(){
    public Object val(OneView v) {
      return exampleVals.get(v);
    }
  };

  public void printRules(){
    //System.out.println(srules+" other="+otherRulesResult);
  }

  public SensorHist(String sensorName) {
    this.sensorName = sensorName;
    prules.add(new PRule(Collections.EMPTY_MAP, Collections.EMPTY_MAP));
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

    // auto-check
    PredictionResult pred = predictState(v.prev, viewToValStatic);
    Object predictedNow = pred.val(getSensorName());
    if( predictedNow!=null && !val.equals(predictedNow) ){
      throw new RuntimeException(  "just added "+val+" auto-check predicted "+predictedNow);
    }
  }

  boolean ruleIsExtra(RuleCond r){
    for( PRule s : prules ){
//      if( r.condWiderIn(s) ){
//        return true;
//      }
      if( s.condToString().equals(r.toString()) ){
        return true;
      }
    }
    return false;
  }

  List<OneView> unexplainedExamples(){
    List<OneView> ret = new ArrayList<OneView>();
    for( OneView ve : exampleVals.keySet() ){
      boolean found=false;
      for( RuleCond r : prules ){
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
    prulesRecordNewView(val, vprev);
    
    if( exampleVals.size()<2 ){
      return;
    }

    Object ppred = predictWithPrules(vprev, viewToValStatic);
    if( ppred!=null && val.equals(ppred) ){
      return;
    }

    makeNewRules();

    //
    //if( !val.equals(predict(vprev)) && exampleVals.size()>3 ){
    if( !val.equals(predictWithDecisionStumpBasedRules(vprev, viewToValStatic).val(sensorName))
        && exampleVals.size()>3 ){
      analyzeVerySimilar(vprev);
    }
  }

//  static class Weighter{
//    String s;
//    Object sval;
//    long eq;
//    long no;
//
//    Weighter(String s, Object sval) {
//      this.s = s;
//      this.sval = sval;
//    }
//
//    public String toString() {
//      return s+"="+sval+" eq="+eq+" no="+no;
//    }
//  }

  private void analyzeVerySimilar(OneView vprev) {
    List<Map<String, Object>> comm = new ArrayList<Map<String, Object>>();
    for( OneView ve : exampleVals.keySet() ){
      if( ve!=vprev ){
        Map<String, Object> m = Utils.intersection(ve.getViewAll(), vprev.getViewAll());
        filterSkippedKeys(m);
        comm.add(m);
      }
    }
    Collections.sort(comm, new Comparator<Map<String, Object>>(){
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        return o2.size() - o1.size();
      }
    });
    for( Map<String, Object> m : comm ){
      RuleCond r = new RuleCond(m);

      List<OneView> exList = examplesCondHolds(exampleVals.keySet(), r);
      if( exList.size()>2 ){
        Object commonRes = commonResValue(exList);
        if( commonRes!=null ){
          if( !ruleIsExtra(r) ){
            prulesAdd(r);
            break;
          }
        }
      }
    }
  }

  private void makeNewRules() {
//    Object commonResAll = commonResValue(exampleVals.keySet());
//    if( commonResAll!=null ){
//      otherRulesResult=commonResAll;
//      return;
//    }
    RuleCond r = ruleByDecisionStump( exampleVals.keySet(), false );
    ruleCheckAndAdd(r, false);
    RuleCond rn = r.negate();
    ruleCheckAndAdd(rn, false);

    RuleCond rEqPrev = ruleByDecisionStump( exampleVals.keySet(), true );
    ruleCheckAndAdd(rEqPrev, true);
    RuleCond rEqPrevN = rEqPrev.negate();
    ruleCheckAndAdd(rEqPrevN, true);

    List<OneView> unex = unexplainedExamples();
    for( int j=0; j<10 && !unexplainedExamples().isEmpty(); j++ ){
      for( int i=unex.size()-1; i>=0; i-- ){
        if( singleAttrRuleHunting(unex.get(i)) ){
          //must try to find beautiful solution - break;
        }
        break; // process only last example
      }
      unex = unexplainedExamples();
    }

//    Object commonResUnex = commonResValue(unex);
//    if( commonResUnex!=null ){
//      otherRulesResult=commonResUnex;
//    }
  }

  boolean singleAttrRuleHunting(OneView vprev){
    Map<String, Object> m = vprev.getViewAll();
    for( String s : m.keySet() ){
      if( skippedViewKeys==null || !skippedViewKeys.contains(s) ){
        RuleCond r = new RuleCond(s, m.get(s), true);
        if( ruleCheckAndAdd(r, false) ){
          //must try to find beautiful solution - return true;
        }
      }
    }
    return false;
  }

  Collection<OneView> recentExamples(){
    List<OneView> r = new ArrayList<OneView>();
    for( int i=exList.size()-1; i>=0; i-- ){
      if( r.size()>=10 ){
        break;
      }
      r.add(exList.get(i));
    }
    return r;
  }

  void filterSkippedKeys(Map<String,Object> m){
    if( skippedViewKeys!=null ){
      for( String k : skippedViewKeys ){
        m.remove(k);
      }
    }
  }

  boolean ruleConvergent(RuleCond r){
    List<OneView> exList = examplesCondHolds(recentExamples(), r);
    if( exList.size()<2 ){
      return false;
    }
    PRule pr = new PRule(r);
    for( OneView v : exList ){
      Object val = exampleVals.get(v);
      pr.recordResult(val, v.prev, viewToValStatic);
    }
    return pr.convergent();
  }

  private boolean ruleCheckAndAdd(RuleCond r, boolean testResultEqPrev) {
    if( r.complexity()>2 ){
      return false;
    }

    //List<OneView> exList = examplesCondHolds(exampleVals.keySet(), r);
    List<OneView> exList = examplesCondHolds(recentExamples(), r);
    if( exList.size()<2 ){
      return false;
    }
    PRule pr = new PRule(r);
    for( OneView v : exList ){
      Object val = exampleVals.get(v);
      pr.recordResult(val, v.prev, viewToValStatic);
    }

    if( pr.convergent() ){
      Map<String,Object> inters = Utils.interstectingVals(exList);
      filterSkippedKeys(inters);
      r.addToCondition(inters);
      List<RuleCond> wider = r.widerConds();
      for( RuleCond w : wider ){
        if( ruleConvergent(w)
          @todo only if examples are versatile enough to exclude extra parm sensibly  ){
          if( !ruleIsExtra(w) ){
            prulesAdd(w);
            return true;
          }
        }
      }
      if( !ruleIsExtra(r) ){
        prulesAdd(r);
        return true;
      }
    }else{
      RuleCond subR = ruleByDecisionStump(exList, testResultEqPrev);
      RuleCond rnew = r.andRule(subR);
      if( rnew.complexity()>r.complexity() ){
        ruleCheckAndAdd(rnew, testResultEqPrev);
      }
      RuleCond rnewNeg = r.andRule(subR.negate());
      if( rnewNeg.complexity()>r.complexity() ){
        ruleCheckAndAdd(rnewNeg, testResultEqPrev);
      }
    }
    return false;
  }

  void prulesAdd(RuleCond r){
    r.disallowConditions(skippedViewKeys);

    PRule pr = new PRule(r.getCond(), r.getNegCond());
    prules.add(pr);
    for( OneView v : exampleVals.keySet() ){
      Object val = exampleVals.get(v);
      if( pr.condHolds(v) ){
        pr.recordResult(val, v.prev, viewToValStatic);
      }
    }
  }


  private void prulesRecordNewView(Object val, OneView vprev) {
    for( Iterator<PRule> i = prules.iterator(); i.hasNext(); ){
      PRule pr = i.next();
      if( pr.condHolds(vprev) ){
        pr.recordResult(val, vprev.prev, viewToValStatic);
      }
    }
  }

  PredictionResult predictWithDecisionStumpBasedRulesNoOther(OneView vprev, OneViewToVal v2v){
    Object ppred = predictWithPrules(vprev, v2v);
    if( ppred!=null ){
      return new PredictionResult(sensorName, ppred);
    }
    return new PredictionResult(sensorName, null);

//    Object res=null;
//    SRule rres=null;
//    for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
//      SRule sr = i.next();
//      if( sr.condHolds(vprev) ){
//        Object resi = sr.getPredictedResult(vprev, v2v);
//        if( resi!=null && res!=null && !resi.equals(res) ){
//          //throw new RuntimeException("rule conflict "+sr+" "+rres);
//          System.out.println("rule conflict "+sensorName+" "+sr+"    "+rres+"    view="+vprev);
//          PredictionResult pt = new PredictionResult();
//          pt.setWithRuleConflicts(true);
//          return pt;
//        }
//        res = resi;
//        rres = sr;
//      }
//    }
//    return new PredictionResult(sensorName, res);
  }

  private Object predictWithPrules(OneView vprev, OneViewToVal v2v) {
    List<PRule> matchingPrules = new ArrayList<PRule>();
    Map<Object,Double> allCounts = new HashMap<Object,Double>();
    for( Iterator<PRule> i = prules.iterator(); i.hasNext(); ){
      PRule pr = i.next();
      if( pr.condHolds(vprev) ){
        matchingPrules.add(pr);
      }
    }

    MinMaxFinder<PRule> mmf = new MinMaxFinder<PRule>();
    for( PRule pr : matchingPrules ){
      Map<Object,Double> n = pr.normalizedResCounts();
      mmf.add(n.size(), pr);
    }
    if( mmf.getMinVal()==1 && mmf.getMaxVal()!=1 ){
      // if there is outstanding clear rule(s) among matching, omit others
      matchingPrules = mmf.getMinNames();
    }


    for( PRule pr : matchingPrules ){
      Map<Object,Double> n = pr.normalizedResCounts();
      for( Object o : n.keySet() ){
        Double ov = allCounts.get(o);
        if( ov==null ){
          ov = n.get(o)/matchingPrules.size();
        }else{
          ov += n.get(o)/matchingPrules.size();
        }
        allCounts.put(o, ov);
      }
    }
    if( allCounts.size()==1 ){
      return allCounts.keySet().iterator().next();
    }
    return null;
  }

  PredictionResult predictWithDecisionStumpBasedRules(OneView vprev, OneViewToVal v2v){
    PredictionResult res = predictWithDecisionStumpBasedRulesNoOther(vprev, v2v);
    if( !res.isNull() ){
      return res;
    }


/*
    //rule hunding mustn't be done when predicting - defies unit testing

    if( otherRulesResult==null ){
      // we don't have prediction at hand
      singleAttrRuleHunting(vprev); // maybe we can derive it right now
      res = predictWithDecisionStumpBasedRulesNoOther(vprev);
      if( res!=null ){
        return res;
      }
    }
*/

    return new PredictionResult(sensorName, null);
    // - can be null if no global 'other' rule exists
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

  List<OneView> examplesCondHolds(Collection<OneView> views, RuleCond r) {
    List<OneView> ret = new ArrayList<OneView>();
    for (OneView v : views) {
      if (r.condHolds(v)) {
        ret.add(v);
      }
    }
    return ret;
  }

  public void printAsTestCase() {
    for (OneView v : exampleVals.keySet()) {
      System.out.println(exampleVals.get(v) + "" + v.getViewAll());
    }
  }


  public void addAsCurrent(Object val, OneView v) {
    //@todo frequency of wrong predition here is our model completeness feeling
    super.addAsCurrent(val, v);
    exList.add(v);

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
    PredictionResult pred1 = predictState(v, viewToValStatic);
    Object pred = pred1.val(getSensorName());
    return val.equals(pred);
    //return val.equals( predictWithWeka(v) );
    //return vals.get(val).acceptedByRules(v)!=null;
  }


  public Object predictWithWeka(OneView vnew){
//    J48 myClassif = new J48();
//    myClassif.setUnpruned(true);
//    myClassif.setConfidenceFactor(1);

    DecisionStump myClassif  = new DecisionStump();
    lastUsedClassifier = myClassif;

    WekaBuilder wf = buildClassifier(myClassif, exampleVals.keySet(), false);

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

  public OneViewToVal getViewToValStatic() {
    return viewToValStatic;
  }

  public PredictionResult predictState(OneView v, OneViewToVal v2v) {
    PredictionResult pred = predictWithDecisionStumpBasedRules(v, v2v);
    return pred;
    //return predictWithWeka(v);
    //return predictSimpleWay(v);
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
    for( Object v : vals ){
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
