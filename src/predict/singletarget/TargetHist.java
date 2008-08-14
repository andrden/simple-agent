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


  public TargetHist(SensorHist sensor, Object sensorVal) {
    this.sensor = sensor;
    this.sensorVal = sensorVal;
  }

  List<OneView> unexpainedExamples(){
    List<OneView> ret = new ArrayList<OneView>();
    for( OneView v : examples ){
      if( !acceptedByRules(v) ){
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
    if (examples.size() >= 2) {
      ruleFromExamples(examples.size()-1);
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
    for( Iterator<Rule> i = rules.iterator(); i.hasNext(); ){
      Rule r = i.next();
      if( r.ruleHolds(v) ){ // rule was incorrect, alas
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
    for( int i=1; i<examples.size(); i++ ){
      ruleFromExamples(i);
      if( unexpainedExamples().isEmpty() ){
        return;
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
        if (!m.isEmpty()) {
          Rule rm = new Rule(m);
          if( !alreadyInList(rm) && ruleVerify(rm) ){  // only if not contradicts with other values
            log("guess val="+this.sensorVal+" rule="+rm);
            rules.add(rm);
            if( unexpainedExamples().isEmpty() ){
              return;
            }
          }
        }
      }
    }
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


  boolean acceptedByRules(OneView v) {
    if (rules.size()>0) {
      for( Rule r : rules ){
        if( r.ruleHolds(v) ){
          return true;
        }
      }
    }
    return false;
  }

  boolean reasonablyAccepted(OneView v){
    if( acceptedByRules(v) ){
      return true;
    }

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
        return true;
      }
    }

    if( intersectingRulesAccept(v) ){
      return true;
    }

    return false;
  }

  boolean intersectingRulesAccept(OneView v){
    for( Rule r : rules ){
      Rule rinters = r.ruleIntersect(v); // trying a wider rule
      if( rinters!=null ){
        if( ruleVerify(rinters) ){
          return true; // the wider rule has no current objections, accepting
        }
      }
    }
    return false;
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
