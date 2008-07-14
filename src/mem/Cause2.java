package mem;

import utils.Utils;

import java.util.*;

import com.pmstation.common.utils.CountingMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 11/7/2008
 * Time: 10:25:10
 */
public class Cause2 {
  public final String key;
  public final Object val;

  DeepState cond;

  List<Hist> examples = new ArrayList<Hist>(); // list of before-Hists
  List<Hist> negExamples = new ArrayList<Hist>(); // negative examples

  public Cause2(String key, Object val) {
    this.key = key;
    this.val = val;
  }

  public void addExample(Hist h){
    if( h.get(key).equals(val) ){
      examples.add(h.prev);

      if( examples.size()>1 ){
        Map<String, CountingMap> rpt = viewRepeats(examples);
      }
    }else if( unexplainedExamplesIntersect(h.prev.getViewAll())!=null ){
      negExamples.add(h.prev);
    }
  }

  List<Hist> unexplainedExamples(){
    List<Hist> ret = new ArrayList<Hist>();
    for( Hist he : examples ){
      if( !predicts(he) ){
        ret.add(he);
      }
    }
    return ret;
  }

  Map<String, CountingMap> viewRepeats(List<Hist> lh){
    Map<String, CountingMap> ret = new HashMap<String, CountingMap>();
    for( Hist h : lh ){
      Map<String, Object> va = h.getViewAll();
      for( String s : va.keySet() ){
        CountingMap cm = ret.get(s);
        if( cm==null ){
          cm = new CountingMap();
          ret.put(s, cm);
        }
        cm.increment(va.get(s));
      }
    }
    return ret;
  }

  public Hist unexplainedExamplesIntersect(Map<String, Object> view){
    ClosestHist pos = closest(unexplainedExamples(), view);
    ClosestHist neg = closest(negExamples, view);
    if( pos.maxCom>neg.maxCom ){
      return pos.h;
    }
    return null;
  }

  static class ClosestHist{
    Hist h=null;
    int maxCom=0;
    void update(Hist i, int comSize){
      if( comSize>maxCom ){
        maxCom=comSize;
        h=i;
      }
    }
  }

  ClosestHist closest(List<Hist> examples, Map<String, Object> view){
    ClosestHist ret = new ClosestHist();
    for( Hist h : examples ){
      Map<String,Object> com = Utils.intersection( h.getViewAll(), view );
      if( !com.isEmpty() ){
        ret.update(h, com.size());
      }
    }
    return ret;
  }

  /**
   *
   * @param h - before-Hist
   * @return
   */
  public boolean predicts(Hist h){
    if( cond==null ){
      return false;
    }
    return cond.match(h);
  }

  /**
   *
   * @param h - before-Hist
   * @return
   */
  Map<String,Object> getPrediction(Hist h){
    if( key.equals(Hist.NOOP_KEY) ){
      int level = ((Number)val).intValue();
      Hist cmp = h.getAtDepth(level-1);
      return cmp.getViewAllWithoutCmd();
    }

    return Collections.singletonMap(key, val);
  }

  public boolean isPositiveResult(){
    return key.equals(Hist.RES_KEY) && ((Number)val).intValue()>0;
  }

  public DeepState intersect(Hist h){
    if( cond==null ){
      return null;
    }
    return cond.intersect(h);
  }

  public Map<String, Object> recentCoditionBase(){
    return cond.getElemsAtDepth(0);
  }


}
