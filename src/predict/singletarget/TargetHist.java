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
  List<OneView> examples = new ArrayList<OneView>();
  Map<ViewDepthElem, Object> rule = null;


  public TargetHist(SensorHist sensor) {
    this.sensor = sensor;
  }

  void addExample(OneView v) {
    if (v == null) {
      return;
    }
    if (examples.size() > 0) {
      Map<ViewDepthElem, Object> m = deepState(v);
      for (OneView vi : examples) {
        Map<ViewDepthElem, Object> cmp = deepState(vi);
        retainEquals(m, cmp);
      }
      if (!m.isEmpty()) {
        rule = m;
      }
    }
    examples.add(v);
  }

  boolean ruleHolds(OneView v) {
    if (rule != null) {
      Map<ViewDepthElem, Object> cmp = deepState(v);
      Map<ViewDepthElem, Object> ruleCopy = new HashMap<ViewDepthElem, Object>(rule);
      retainEquals(ruleCopy, cmp);
      if (ruleCopy.size() == rule.size()) {
        return true;
      }
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

  Map<ViewDepthElem, Object> deepState(OneView v) {
    Map<ViewDepthElem, Object> ret = new HashMap<ViewDepthElem, Object>();
    for (int i = 0; v != null && i < DEEP_STATE_DEPTH; i++) {
      Map<String, Object> all = v.getViewAll();
      for (String k : all.keySet()) {
        ret.put(new ViewDepthElem(i, k), all.get(k));
      }
      v = v.prev;
    }
    return ret;
  }

}
