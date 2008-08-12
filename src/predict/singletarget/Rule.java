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

  boolean ruleHolds(OneView v) {
    Map<ViewDepthElem, Object> cmp = TargetHist.deepState(v, ruleMaxDepth());
    Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(rule);
    TargetHist.retainEquals(ruleCopy, cmp);
    if (ruleCopy.size() == rule.size()) {
      return true;
    }
    return false;
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
