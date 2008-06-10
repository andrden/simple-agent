package logic;

import mem.*;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 28/5/2008
 * Time: 19:43:44
 */
public class MainResultsAnalyzer extends ResultsAnalyzer{
  Set<String> importantEls;
  History history;
  Causes causes;

  public MainResultsAnalyzer(History history, Causes causes, Set<String> importantEls) {
    this.history = history;
    this.causes = causes;
    this.importantEls = importantEls;
  }

  public Cause test(List<Hist> found, DeepState groupingCond) {
    Map<String,Object> equalElems=null;
    int min=Integer.MAX_VALUE;
    int max=Integer.MIN_VALUE;
    boolean allCurrentlyPredicted=true;
    for( Hist h : found ){
      Map<String,Object> nextViewAll = history.getNextViewAll(h);
      if( equalElems==null ){
        equalElems = new HashMap<String,Object>(nextViewAll);
      }else{
        Hist.retainEqualsIn(nextViewAll, equalElems);
      }
      int res = history.getResult(h);
      min = Math.min(min, res);
      max = Math.max(max, res);

      Integer currentPrediction = causes.predictResultByCauses(h);
      if( currentPrediction==null || currentPrediction.intValue()!= history.getResult(h) ){
        allCurrentlyPredicted=false;
      }
    }

    equalElems.remove(Hist.CMD_KEY);
    boolean usefulCause = max==min && !allCurrentlyPredicted;
    if( !usefulCause && equalElems.size()>0 ){
      Hist hfuture = new Hist(found.get(0), equalElems, null);
      Integer ires = causes.predictResultByCauses(hfuture);
      if( ires!=null && ires.intValue()!=0 ){
        usefulCause=true;
      }
      if( !usefulCause ){
        if( causes.smacksOfResult(hfuture)!=null ){
          usefulCause=true;
        }
      }
    }
    if( importantEls!=null && !Utils.intersects(importantEls, equalElems.keySet()) ){
      usefulCause=false;
    }


    if( usefulCause ){
      // threre's some definite relation:
      Cause newc = new Cause(groupingCond, equalElems, found.size());
      return newc;
    }
    return null;
  }
}
