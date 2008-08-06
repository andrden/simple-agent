package mem;

import utils.Utils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 2/4/2008
 * Time: 13:06:02
 */
public class Causes implements Serializable {
  public List<Cause> list = new ArrayList<Cause>();

  final static int VERIRIFIED_TO_ACCEPT_ZERO_CAUSE = 5;

  public List<Cause> validCauses() {
    List<Cause> l = new ArrayList<Cause>();
    for (Cause c : list) {
      if (c.valid() && !c.explainableByOther()) {
        l.add(c);
      }
    }
    return l;
  }

  public List<Cause> orderedValidCauses() {
    List<Cause> l = new ArrayList<Cause>(validCauses());
    Collections.sort(l, new Comparator<Cause>() {
      public int compare(Cause o1, Cause o2) {
        return new Integer(o2.getCountVerified()).compareTo(new Integer(o1.getCountVerified()));
      }
    });
    return l;
  }

  ;

  public void verifyAll(Hist hnext) {
    for (Cause c : list) {
      if (c.canPredict(hnext.prev)) {
        c.event(hnext);
      }
    }
  }

  public static class PredictionBy {
    public List<Cause> by = new ArrayList<Cause>();
    public Map<String, Object> view;
  }

  public Map<String, Object> predictAllViewByCauses(Hist h) {
    PredictionBy predictionBy = predictAllViewByCausesWithBy(h);
    if (predictionBy == null) {
      return null;
    }
    return predictionBy.view;
  }

  public List<Cause> applicableCauses(Hist h) {
    List<Cause> ret = new ArrayList<Cause>();
    for (Cause cause : validCauses()) {
      if (cause.canPredict(h)) {
        ret.add(cause);
      }
    }
    return ret;
  }

  public boolean predictsNoop(Hist h) {
    for (Cause cause : validCauses()) {
      if (cause.canPredict(h) && cause.noop()) {
        return true;
      }
    }
    return false;
  }

  public PredictionBy predictAllViewByCausesWithBy(Hist h) {
    PredictionBy pb = new PredictionBy();
    Map<String, Object> els = new HashMap<String, Object>();
    for (Cause cause : validCauses()) {
      if (cause.canPredict(h)) {
        mergeEquals(els, cause.getPrediction(h));
        pb.by.add(cause);
      }
    }
    for (Iterator<String> i = els.keySet().iterator(); i.hasNext();) {
      String s = i.next();
      if (els.get(s) == VALUE_DIFF) {
        i.remove();
      }
    }
    if (els.isEmpty()) {
      return null;
    }
    pb.view = els;
    return pb;
  }

  private static final Object VALUE_DIFF = new Object();

  void mergeEquals(Map<String, Object> var, Map<String, Object> compare) {
    for (String s : compare.keySet()) {
      if (!var.containsKey(s)) {
        var.put(s, compare.get(s));
      } else if (!compare.get(s).equals(var.get(s))) {
        var.put(s, VALUE_DIFF);
      }
    }
  }

  public Integer predictResultByCauses(Hist h) {
    Integer prediction = null;
    for (Cause cause : validCauses()) {
      if (cause.canPredict(h) && cause.hasResult()) {
        if (prediction == null) {
          prediction = cause.getResult();
        }
        if (prediction.intValue() != cause.getResult()) {
          return null; // no coherent picture
        }
      }
    }
    return prediction;
  }

  public static class SmacksOfResult {
    public Cause cause;
    public DeepState ds;

    public SmacksOfResult(Cause cause, DeepState ds) {
      this.cause = cause;
      this.ds = ds;
    }
  }

  public SmacksOfResult smacksOfResult(Hist h) {
    for (Cause cause : validCauses()) {
      if (cause.hasResult() && cause.getResult() != 0) {
        DeepState ds = cause.intersect(h);
        if (ds != null) {
          return new SmacksOfResult(cause, ds);
        }
      }
    }
    return null;
  }

  public boolean newCause(Cause newc) {
    // check if not already wrong according to other causes
    for (Cause cc : validCauses()) {
      if (cc.getCountVerified() > 0 && newc.ds.isSubsetOf(cc.ds) &&
              cc.hasResult() && newc.hasResult() &&
              newc.getResult() != cc.getResult()) {
        return false;
      }
    }

    for (Cause c : list) {
      if (c.equals(newc)) { // 'valid' doesn't matter here
        return false;
      }
      if (c.valid() && c.explains(newc) && c.getCountVerified() > newc.getCountVerified()) {
        return false; // nothing new here
      }
    }

    //don't add if CURRENT SITUATION can be explained with some another valid cause
    for (Cause c : list) {
      if (newc.explains(c)) {
        c.explainableBy.add(newc);
      }
    }

    if (newc.hasResult() && newc.getResult() == 0) {
      if (newc.countVerified < VERIRIFIED_TO_ACCEPT_ZERO_CAUSE) {
        return false;
      }
    }

    list.add(newc);
    return true; // added
  }

  public void generalize(History history) {
    for (Cause c1 : validCauses()) {
      for (Cause c2 : validCauses()) {
        if (c1 != c2 && c1.hasResult() && c2.hasResult() && c1.getResult() == c2.getResult()) {
          DeepState inters = c1.ds.intersect(c2.ds);
          if (inters != null) {
            Map<String, Object> eq = new HashMap<String, Object>(c1.prediction);
            Utils.retainEqualsIn(c2.prediction, eq);
            Cause newc = new Cause(inters, eq);

            newc.generifiedFrom = Arrays.asList(c1, c2);
            verifyNew(history, newc);
            if (newc.valid()) {
              newCause(newc);
            }
          }
        }
      }
    }
  }

  void verifyNew(History history, Cause c) {
    for (Hist h : history.list) {
      if (c.canPredict(h)) {
        Hist next = new Hist(h, h.next.getViewAll(), null);
        c.event(next);
      }
    }
  }

  public Set<ViewDepthElem> usedCauseElems() {
    Set<ViewDepthElem> s = new HashSet<ViewDepthElem>();
    for (Cause c : validCauses()) {
      s.addAll(c.ds.keySet());
    }
    return s;
  }

  private static final long serialVersionUID = 9024436449508241566L;
}
