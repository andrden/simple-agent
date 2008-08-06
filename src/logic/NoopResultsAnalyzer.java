package logic;

import mem.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 28/5/2008
 * Time: 19:43:44
 */
public class NoopResultsAnalyzer extends ResultsAnalyzer {
  int level;
  History history;
  Causes causes;

  public NoopResultsAnalyzer(History history, Causes causes, int level) {
    this.history = history;
    this.causes = causes;
    this.level = level;
  }

  public Cause test(List<Hist> found, DeepState groupingCond) {
//    Map<String,Object> equalElems=null;
//    int min=Integer.MAX_VALUE;
//    int max=Integer.MIN_VALUE;
//    boolean allCurrentlyPredicted=true;

    Set<Boolean> noopState = new HashSet<Boolean>();
    for (Hist h : found) {
      Map<String, Object> nextViewAll = h.next.getViewAll();
      Hist cmp = h.getAtDepth(level - 1);
      if (cmp == null) {
        continue;
      }
      boolean localNoop = cmp.viewMatch(nextViewAll, false);
      noopState.add(localNoop);
//      if( equalElems==null ){
//        equalElems = new HashMap<String,Object>(nextViewAll);
//      }else{
//        Hist.retainEqualsIn(nextViewAll, equalElems);
//      }
//      int res = history.getResult(h);
//      min = Math.min(min, res);
//      max = Math.max(max, res);

//      Integer currentPrediction = causes.predictResultByCauses(h);
//      if( currentPrediction==null || currentPrediction.intValue()!= history.getResult(h) ){
//        allCurrentlyPredicted=false;
//      }
    }

    boolean usefulCause = noopState.size() == 1 && noopState.contains(Boolean.TRUE); // && !allCurrentlyPredicted;
//
//
    if (usefulCause) {
      // threre's some definite relation:
      Map<String, Object> prediction = new HashMap<String, Object>();
      prediction.put(Hist.NOOP_KEY, new Integer(level));
      Cause newc = new Cause(groupingCond, prediction, found.size());
      return newc;
    }
    return null;
  }
}
