package mem;

import utils.Utils;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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

  List<Hist> examples = new ArrayList<Hist>(); // list of resulting Hists
  List<Hist> negExamples = new ArrayList<Hist>(); // negative examples

  public Cause2(String key, Object val) {
    this.key = key;
    this.val = val;
  }

  public void addExample(Hist h){
    if( h.get(key).equals(val) ){
      examples.add(h);
    }else if( unexplainedExamplesIntersect(h.prev.getViewAll())!=null ){
      negExamples.add(h);
    }
  }

  List<Hist> unexplainedExamples(){
    List<Hist> ret = new ArrayList<Hist>();
    for( Hist he : examples ){
      if( !predicts(he.prev) ){
        ret.add(he);
      }
    }
    return ret;
  }

  public Hist unexplainedExamplesIntersect(Map<String, Object> view){
    need finding max among positive and negative examples
    for( Hist h : unexplainedExamples() ){
      Map<String,Object> com = Utils.intersection( h.prev.getViewAll(), view );
      if( !com.isEmpty() ){
        return h;
      }
    }
    return null;
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
