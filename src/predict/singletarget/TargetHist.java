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
  Map<ViewDepthElem, Object> rule = null;


  public TargetHist(SensorHist sensor, Object sensorVal) {
    this.sensor = sensor;
    this.sensorVal = sensorVal;
  }

  int ruleMaxDepth(Map<ViewDepthElem, Object> r){
    int m=0;
    for( ViewDepthElem e : r.keySet() ){
      m = Math.max(m, e.getDepth());
    }
    return m;
  }

  void addExample(OneView v) {
    if (v == null) {
      return;
    }
    if (examples.size() > 0) {
      ruleFromExamples(v);
    }
    examples.add(v);

    for( TargetHist other : sensor.vals.values() ){
      if( other==this ){
        continue;
      }
      if( other.rule!=null && other.ruleHolds(v) ){
        other.rule=null; // rule was incorrect, alas
        other.ruleFromExamples(other.examples.get(other.examples.size()-1));
      }
    }
  }

  boolean ruleVerify(Map<ViewDepthElem, Object> r){
    for( TargetHist other : sensor.vals.values() ){
      if( other==this ){
        continue;
      }
      for( OneView ex : other.examples ){
        if( ruleHolds(r, ex) ){
          return false;
        }
      }
    }
    return true;
  }

  private void ruleFromExamples(OneView v) {
    for( int d = 0; d <= DEEP_STATE_DEPTH; d++ ){
      Map<ViewDepthElem, Object> m = deepState(v, d);
      for (OneView vi : examples) {
        Map<ViewDepthElem, Object> cmp = deepState(vi, d);
        retainEquals(m, cmp);
      }
      if (!m.isEmpty()) {
        if( ruleVerify(m) ){  // only if not contradicts with other values
          rule = m;
          break;
        }
      }
    }
  }

  boolean ruleHolds(OneView v) {
    if (rule != null) {
      if (ruleHolds(rule, v)) return true;
    } else {
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
    }

    return false;
  }

  private boolean ruleHolds(Map<ViewDepthElem, Object> r, OneView v) {
    Map<ViewDepthElem, Object> cmp = deepState(v, ruleMaxDepth(r));
    Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(r);
    retainEquals(ruleCopy, cmp);
    if (ruleCopy.size() == r.size()) {
      return true;
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

  void retainEquals(Map<ViewDepthElem, Object> where, Map<ViewDepthElem, Object> cmp) {
    for (Iterator<ViewDepthElem> i = where.keySet().iterator(); i.hasNext();) {
      ViewDepthElem e = i.next();
      if (!cmp.containsKey(e) || !where.get(e).equals(cmp.get(e))) {
        i.remove();
      }
    }
  }

  Map<ViewDepthElem, Object> deepState(OneView v, int depth) {
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
