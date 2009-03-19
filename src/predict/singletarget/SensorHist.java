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
import com.pmstation.common.utils.CountingMap;
import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 4/8/2008
 * Time: 17:37:58
 */
public class SensorHist implements java.io.Serializable{
  final String sensorName;
  Set<Object> vals = new HashSet<Object>();
  Set<String> skippedViewKeys;

  Classifier lastUsedClassifier;
  LinkedHashMap<OneView, Object> exampleVals = new LinkedHashMap<OneView, Object>();
  List<OneView> exList = new ArrayList<OneView>();

  List<SRule> srules = new ArrayList<SRule>();
  Object otherRulesResult = null;

  Set<String> decisiveAttrs = new HashSet<String>();
  CountingMap valsCounts = new CountingMap();
  OneViewToVal viewToValStatic = new OneViewToVal(){
    public Object val(OneView v) {
      return exampleVals.get(v);
    }
  };

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

    // auto-check
    Object predictedNow = predict(v.prev);
    if( predictedNow!=null && !val.equals(predictedNow) ){
      throw new RuntimeException(  "just added "+val+" auto-check predicted "+predictedNow);
    }
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

  SRule ruleByDecisionStump(Collection<OneView> views, boolean seekForSameVal){
    DecisionStump myClassif  = new DecisionStump();
    WekaBuilder wf = buildClassifier(myClassif, views, seekForSameVal);

    Attribute splitAttr = wf.getInstances().attribute((Integer)PrivateFieldGetter.evalNoEx(myClassif,"m_AttIndex"));
    String attName = splitAttr.name();
    decisiveAttrs.add(attName);
    Object attVal = wf.attVal(attName, ((Double)PrivateFieldGetter.evalNoEx(myClassif,"m_SplitPoint")).intValue() );
    SRule r = new SRule(attName, attVal, true);
    if( seekForSameVal ){
        r.resultEqPrev=true;
    }
    return r;
  }

  void analyzeNewExample(Object val, OneView vprev){
    if( exampleVals.size()<2 ){
      return;
    }

    boolean explained = verifyRules(val, vprev);
    if( explained ){
      return;
    }

    /*
    if( exampleVals.size()>140 ){
      Map<String,Object> m = vprev.getViewAll();
      List<Weighter> wlist = new ArrayList<Weighter>();
      for( String s : m.keySet() ){
        Object sval = m.get(s);
        Weighter w = new Weighter(s, sval);
        wlist.add(w);
        for( OneView v : exampleVals.keySet() ){
          if( sval.equals(v.get(s)) ){
            if( exampleVals.get(v).equals(val) ){
              w.eq++;
            }else{
              w.no++;
            }
          }
        }
      }
      System.nanoTime();
    }
    */

    makeNewRules();

    //
    //if( !val.equals(predict(vprev)) && exampleVals.size()>3 ){
    if( !val.equals(predictWithDecisionStumpBasedRules(vprev).val(sensorName))
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
      SRule r = new SRule(m);

      List<OneView> exList = examplesCondHolds(exampleVals.keySet(), r);
      if( exList.size()>2 ){
        Object commonRes = commonResValue(exList);
        if( commonRes!=null ){
          r.setResult(commonRes);
          if( !ruleIsExtra(r) ){
            srulesAdd(r);
            break;
          }
        }
      }
    }
  }

  private void makeNewRules() {
    Object commonResAll = commonResValue(exampleVals.keySet());
    if( commonResAll!=null ){
      otherRulesResult=commonResAll;
      return;
    }
    SRule r = ruleByDecisionStump( exampleVals.keySet(), false );
    ruleCheckAndAdd(r);
    SRule rn = r.negate();
    ruleCheckAndAdd(rn);

    SRule rEqPrev = ruleByDecisionStump( exampleVals.keySet(), true );
    ruleCheckAndAdd(rEqPrev);
    SRule rEqPrevN = rEqPrev.negate();
    ruleCheckAndAdd(rEqPrevN);

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

    Object commonResUnex = commonResValue(unex);
    if( commonResUnex!=null ){
      otherRulesResult=commonResUnex;
    }
  }

  boolean singleAttrRuleHunting(OneView vprev){
    Map<String, Object> m = vprev.getViewAll();
    for( String s : m.keySet() ){
      if( skippedViewKeys==null || !skippedViewKeys.contains(s) ){
        SRule r = new SRule(s, m.get(s), true);
        if( ruleCheckAndAdd(r) ){
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

  private boolean ruleCheckAndAdd(SRule r) {
    if( r.complexity()>2 ){
      return false;
    }

    //List<OneView> exList = examplesCondHolds(exampleVals.keySet(), r);
    List<OneView> exList = examplesCondHolds(recentExamples(), r);
    if( exList.size()<2 ){
      return false;
    }
    Object commonRes = commonResValue(r, exList);
    if( r.resultUseful(commonRes) ){
      if( !r.resultEqPrev ){
        r.setResult(commonRes);
      }
      Map<String,Object> inters =Utils.interstectingVals(exList);
      filterSkippedKeys(inters);
      r.addToCondition(inters);
      if( !ruleIsExtra(r) ){
//        if( r.resultEqPrev ){
//          r=r; // adding new 'resultEqPrev'-type rule
//        }

//        must add all common condition singletons to the SRule
//        ибо это чушь:
//
//this = {predict.singletarget.SensorHist@1136}"f {YELLOW,BLACK}"
//r = {predict.singletarget.SRule@1584}"{} neg {!=L} => BLACK"
//exList = {java.util.ArrayList@1585} size = 7
//[0] = {mem.Hist@1619}"#7 {f=BLACK, !=A2B, fl=WHITE, r=BLUE, ff=YELLOW, $=0, ffr=WHITE, frr=WHITE, rr=WHITE, fr=WHITE, l=YELLOW}"
//[1] = {mem.Hist@1620}"#6 {f=BLACK, !=B2, fl=WHITE, r=BLUE, ff=YELLOW, $=-1, ffr=WHITE, frr=WHITE, rr=WHITE, fr=WHITE, l=YELLOW}"
//[2] = {mem.Hist@1621}"#5 {f=BLACK, !=Fb, fl=WHITE, r=BLUE, ff=YELLOW, $=0, ffr=WHITE, frr=WHITE, rr=WHITE, fr=WHITE, l=YELLOW}"
//[3] = {mem.Hist@1622}"#4 {f=BLACK, !=B1, fl=WHITE, r=BLUE, ff=YELLOW, $=0, ffr=WHITE, frr=WHITE, rr=WHITE, fr=WHITE, l=YELLOW}"
//[4] = {mem.Hist@1623}"#3 {f=BLACK, !=A1, fl=WHITE, r=BLUE, ff=YELLOW, $=0, ffr=WHITE, frr=WHITE, rr=WHITE, fr=WHITE, l=YELLOW}"
//[5] = {mem.Hist@1624}"#2 {f=BLACK, !=N, fl=WHITE, r=BLUE, ff=YELLOW, $=0, ffr=WHITE, frr=WHITE, rr=WHITE, fr=WHITE, l=YELLOW}"
//[6] = {mem.Hist@1625}"#1 {f=YELLOW, !=R, fl=WHITE, r=BLUE, ff=BLACK, $=0, ffr=BLACK, frr=WHITE, rr=BLACK, fr=YELLOW, l=YELLOW}"


        srulesAdd(r);
        return true;
      }
    }else{
      SRule subR = ruleByDecisionStump(exList, r.resultEqPrev);
      SRule rnew = r.andRule(subR);
      if( rnew.complexity()>r.complexity() ){
        ruleCheckAndAdd(rnew);
      }
      SRule rnewNeg = r.andRule(subR.negate());
      if( rnewNeg.complexity()>r.complexity() ){
        ruleCheckAndAdd(rnewNeg);
      }

//      List<OneView> exList2 = examplesCondHolds(exList, subR);
//      Object commonRes2 = commonResValue(exList2);
//      if( commonRes2!=null && exList2.size()>=2 ){
//        System.na n oTime();
//      }
    }
    return false;
  }

  void srulesAdd(SRule r){
    r.disallowConditions(skippedViewKeys);
    srules.add(r);
  }

  private boolean verifyRules(Object val, OneView vprev) {
    boolean explained=false;
    for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
      SRule sr = i.next();
      if( sr.condHolds(vprev) ){
        if( !sr.getPredictedResult(vprev, viewToValStatic).equals(val) ){
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

  PredictionResult predictWithDecisionStumpBasedRulesNoOther(OneView vprev){
    Object res=null;
    SRule rres=null;
    for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
      SRule sr = i.next();
      if( sr.condHolds(vprev) ){
        Object resi = sr.getPredictedResult(vprev, view to val needed!);
        if( resi!=null && res!=null && !resi.equals(res) ){
          //throw new RuntimeException("rule conflict "+sr+" "+rres);
          System.out.println("rule conflict "+sensorName+" "+sr+"    "+rres+"    view="+vprev);
          PredictionResult pt = new PredictionResult();
          pt.setWithRuleConflicts(true);
          return pt;
        }
        res = resi;
        rres = sr;
      }
    }
    return new PredictionResult(sensorName, res);
  }

  PredictionResult predictWithDecisionStumpBasedRules(OneView vprev){
    PredictionResult res = predictWithDecisionStumpBasedRulesNoOther(vprev);
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

    return new PredictionResult(sensorName, otherRulesResult);
    // - can be null if no global 'other' rule exists
  }

  Object commonResValue(Collection<OneView> exList){
    return commonResValue(null, exList);
  }

  Object commonResValue(SRule rule, Collection<OneView> exList){
    Object com = null;
    for( OneView v : exList ){
      Object r = exampleVals.get(v);
      if( rule!=null ){
        r = rule.resultValue(v, exampleVals);
      }
      if( com!=null ){
        if( !com.equals(r) ){
          return null;
        }
      }
      com=r;
    }
    return com;
  }

  List<OneView> examplesCondHolds(Collection<OneView> views, SRule r) {
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
    valsCounts.increment(val);
    vals.add(val);
    exampleVals.put(v, val);
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
    Object pred = predict(v);
    return val.equals(pred);
    //return val.equals( predictWithWeka(v) );
    //return vals.get(val).acceptedByRules(v)!=null;
  }

  public WekaBuilder buildClassifier(Classifier myClassif, Collection<OneView> views,
                                     boolean seekForSameVal){
    WekaBuilder wf = new WekaBuilder(myClassif);
    for( OneView v : views ){
      wf.collectAttrs(v, skippedViewKeys);
    }

    if( seekForSameVal ){
      wf.addForRes("0");
      wf.addForRes("1");
    }else{
      for( Object o : vals ){
        wf.addForRes(o);
      }
    }
    wf.mkInstances();


    for( OneView v : views ){
      String vval = exampleVals.get(v).toString();
      if( seekForSameVal ){
        vval = "0";
        if( v.prev!=null && exampleVals.get(v.prev)!=null){
          if( exampleVals.get(v.prev).equals(exampleVals.get(v)) ){
            vval = "1";
          }
        }
      }
      wf.addInstance(v, vval);
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

  public PredictionResult predictState(OneView v) {
    PredictionResult pred = predictWithDecisionStumpBasedRules(v);
    return pred;
    //return predictWithWeka(v);
    //return predictSimpleWay(v);
  }

  public Object predict(OneView v) {
    PredictionResult pred = predictState(v);
    return pred.val(sensorName);
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
