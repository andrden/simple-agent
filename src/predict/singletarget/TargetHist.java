package predict.singletarget;

import mem.OneView;
import mem.ViewDepthElem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 12:09:00
 */
public class TargetHist {
  static final int DEEP_STATE_DEPTH = 4;

  final SensorHist sensor;
  final Object sensorVal;

  List<OneView> examples = new ArrayList<OneView>();
  List<Rule> rules = new ArrayList<Rule>();
  //List<Rule> oldRules = new ArrayList<Rule>();

  public int ruleHoldsCount(Rule r){
    int c=0;
    for( int i=0; i<examples.size(); i++ ){
      if( r.ruleHolds(examples.get(i)) ){
        c++;
      }
    }
    return c;
  }

  public TargetHist(SensorHist sensor, Object sensorVal) {
    this.sensor = sensor;
    this.sensorVal = sensorVal;
  }

  List<OneView> unexpainedExamples(){
    List<OneView> ret = new ArrayList<OneView>();
    for( OneView v : examples ){
      if( acceptedByRules(v)==null ){
        ret.add(v);
      }
    }
    return ret;
  }


  void addExample(OneView v) {
    if (v == null) {
      return;
    }
    examples.add(v);
    if( sensor.vals.size()==1 ){
      // I'm the only one value ever obtained, no rules needed, always predict me
      return;
    }
    if( acceptedByRules(v)==null ){
      if (examples.size() >= 2) {
        ruleFromExamples(examples.size()-1);
      }
    }

    for( TargetHist other : sensor.vals.values() ){
      if( other==this ){
        continue;
      }
      other.ruleInvalidateByOther(v);
    }
  }

  void ruleInvalidateByOther(OneView v){
    boolean rulesOk=true;
    List<Rule> removedRules = new ArrayList<Rule>();
    for( Iterator<Rule> i = rules.iterator(); i.hasNext(); ){
      Rule r = i.next();
      if( r.ruleHolds(v) ){ // rule was incorrect, alas
        removedRules.add(r);
        i.remove();
        rulesOk=false;
      }
    }
    if( !rulesOk ){
      ruleFromExamples();
    }
  }

  boolean ruleVerify(Rule r){
    for( TargetHist other : sensor.vals.values() ){
      if( other==this ){
        continue;
      }
      for( OneView ex : other.examples ){
        if( r.ruleHolds(ex) ){
          return false;
        }
      }
    }
    return true;
  }

  private void ruleFromExamples(){
    int runCount=0;
    for( int i=examples.size()-1; i>=0;  i-- ){
      if( unexpainedExamples().isEmpty() ){
        return;
      }
      ruleFromExamples(i);
      runCount++;
      if( runCount>10 ){
        return; // only last 10 examples - to avoid spending too much time
      }
    }
  }

  /**
   * Pairs comparison to derive a rule
   * @param cmpExampleIndex
   * @return
   */
  private void ruleFromExamples(int cmpExampleIndex) {
    for( int d = 0; d <= DEEP_STATE_DEPTH; d++ ){
      OneView cmpEx = examples.get(cmpExampleIndex);
      for( int i=0; i<cmpExampleIndex; i++ ){
        OneView vi = examples.get(i);
        Map<ViewDepthElem, Object> cmp = deepState(cmpEx, d);
        Map<ViewDepthElem, Object> m = deepState(vi, d);
        retainEquals(m, cmp);
        if (!m.isEmpty() && m.size()<5) {
          Rule rm = new Rule(m);
          if( !alreadyInList(rm) && ruleVerify(rm) ){  // only if not contradicts with other values
            //log("guess val="+this.sensorVal+" rule="+rm);
            newRuleToSet(rm);
            if( unexpainedExamples().isEmpty() ){
              return;
            }
          }
        }
      }
    }
  }

  private void newRuleToSet(Rule rm) {
    if( sensorVal.equals("BLACK") && sensor.sensorName.equals("ff")  ){
      System.currentTimeMillis();
    }
    for( Iterator<Rule> i = rules.iterator(); i.hasNext(); ){
      Rule r = i.next();
      if( rm.widerOrEqTo(r) ){
        i.remove(); // clean up list from partial more specific rules
      }else if( explainsMore(rm, r) ){
        i.remove(); // clean up list from partial extra complicated rules
      }
    }

    rules.add(rm);
    sortRulesByHoldsCount();
    if( examples.size()>10 && rules.size()>examples.size() ){
      throw new RuntimeException("Too many rules");
    }

      if( rules.size()>5 ){
        //throw new RuntimeException("Too many rules "+rules.size());
        rules.remove(rules.size()-1); // last is with least holds count
      }
  }

  void sortRulesByHoldsCount(){
    final Map<Integer,Comparable> m = new HashMap<Integer,Comparable>();
    for( Rule r : rules ){
      m.put(System.identityHashCode(r), ruleHoldsCount(r));
    }
    Collections.sort(rules, new Comparator<Rule>(){
      public int compare(Rule o1, Rule o2) {
        // sort by ruleHoldsCount() desc
        return m.get(System.identityHashCode(o2)).compareTo(m.get(System.identityHashCode(o1)));
      }
    });
  }


  boolean explainsMore(Rule newr, Rule oldr){
    boolean res=false;
    for( OneView v : examples ){
      boolean newh = newr.ruleHolds(v);
      boolean oldh = oldr.ruleHolds(v);
      if( !newh && oldh ){
          return false;
      }
      if( newh && !oldh ){
        res = true;
      }
    }
    return res;
  }

  boolean alreadyInList(Rule r){
    for( Rule ri : rules ){
      if( ri.widerOrEqTo(r) ){
        return true;
      }
    }
    return false;
  }

  void log(String s){
    System.out.println(s);
  }


  Rule acceptedByRules(OneView v) {
    if (rules.size()>0) {
      for( Rule r : rules ){
        if( r.ruleHolds(v) ){
          return r;
        }
      }
    }
    return null;
  }

  Rule reasonablyAccepted(OneView v){
    // try complete equality:
    if (fullExampleMatch(v)) {
      // check same condition for other values of the same sensor
      boolean noOther = true;
      for (TargetHist hi : sensor.vals.values()) {
        if (hi != this && hi.fullExampleMatch(v)) {
          noOther = false;
          break;
        }
      }
      if (noOther) {
        return new Rule(TargetHist.deepState(v,0));
      }
    }

    Rule intersect = intersectingRulesAccept(v);
    if( intersect!=null ){
      return intersect;
    }

    return null;
  }

  Rule intersectingRulesAccept(OneView v){
    for( Rule r : rules ){
      Rule rinters = r.ruleIntersect(v); // trying a wider rule
      if( rinters!=null ){
        if( ruleVerify(rinters) ){
          return rinters; // the wider rule has no current objections, accepting
        }
      }
    }
    return null;
  }


  boolean fullExampleMatch(OneView v) {
    Map<String, Object> m = v.getViewAll();
    for (OneView vi : examples) {
      boolean same = true;
      for (String k : m.keySet()) {
        if (!m.get(k).equals(vi.get(k))) {
          same = false;
        }
      }
      if (same) {
        return true;
      }
    }
    return false;
  }

  static void retainEquals(Map<ViewDepthElem, Object> where, Map<ViewDepthElem, Object> cmp) {
    for (Iterator<ViewDepthElem> i = where.keySet().iterator(); i.hasNext();) {
      ViewDepthElem e = i.next();
      if (!cmp.containsKey(e) || !where.get(e).equals(cmp.get(e))) {
        i.remove();
      }
    }
  }

  static Map<ViewDepthElem, Object> deepState(OneView v, int depth) {
    Map<ViewDepthElem, Object> ret = new HashMap<ViewDepthElem, Object>();
    for (int i = 0; v != null && i <= depth; i++) {
      Map<String, Object> all = v.getViewAll();
      for (String k : all.keySet()) {
        ret.put(new ViewDepthElem(i, k), all.get(k));
      }
      v = v.prev;
    }
    return ret;
  }

}
