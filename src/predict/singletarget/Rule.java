package predict.singletarget;

import mem.ViewDepthElem;
import mem.OneView;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 12/8/2008
 * Time: 19:18:56
 */
public class Rule {
  final Map<ViewDepthElem, Object> rule;

  public Rule(Map<ViewDepthElem, Object> rule) {
    this.rule = rule;
  }

  public boolean equals(Object other){
    return rule.equals( ((Rule)other).rule );
  }

  boolean ruleHolds(OneView v) {
    Map<ViewDepthElem, Object> cmp = deepState(v, ruleMaxDepth());
    Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(rule);
    TargetHist.retainEquals(ruleCopy, cmp);
    if (ruleCopy.size() == rule.size()) {
      return true;
    }
    return false;
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

  /**
   * test if 'other' contains more conditions
   * @param other
   * @return
   */
  boolean widerOrEqTo(Rule other){
    Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(other.rule);
    TargetHist.retainEquals(ruleCopy, rule);
    return ruleCopy.size()==rule.size(); // if contains all my conditions
  }

  Rule ruleIntersect(Rule other){
    Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(other.rule);
    TargetHist.retainEquals(ruleCopy, rule);
    if( ruleCopy.size()==0 ){
      return null;
    }
    return new Rule(ruleCopy);
  }

  Rule ruleIntersect(OneView v) {
    Map<ViewDepthElem, Object> cmp = deepState(v, ruleMaxDepth());
    Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(rule);
    TargetHist.retainEquals(ruleCopy, cmp);
    if (!ruleCopy.isEmpty()) {
      return new Rule(ruleCopy);
    }
    return null;
  }

  int ruleMaxDepth(){
    int m=0;
    for( ViewDepthElem e : rule.keySet() ){
      m = Math.max(m, e.getDepth());
    }
    return m;
  }


  public String toString() {
    return rule.toString();
  }
}
