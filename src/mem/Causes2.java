package mem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 11/7/2008
 * Time: 10:27:00
 */
public class Causes2 {
  public List<Cause2> list = new ArrayList<Cause2>();

  public Cause2 find(String key, Object val){
    for( Cause2 c : list ){
      if( c.key.equals(key) && c.val.equals(val) ){
        return c;
      }
    }
    return null;
  }

  public static class PredictionBy{
    public List<Cause2> by = new ArrayList<Cause2>();
    public Map<String,Object> view;
  }

  public Map<String,Object> predictAllViewByCauses(Hist h){
    PredictionBy predictionBy = predictAllViewByCausesWithBy(h);
    if( predictionBy==null ){
      return Collections.emptyMap();
    }
    return predictionBy.view;
  }

  private static final Object VALUE_DIFF = new Object();
  void mergeEquals(Map<String,Object> var, Map<String,Object> compare){
    for( String s : compare.keySet() ){
      if( !var.containsKey(s) ){
        var.put(s, compare.get(s));
      }else if( !compare.get(s).equals(var.get(s)) ){
        var.put(s, VALUE_DIFF);
      }
    }
  }

  public PredictionBy predictAllViewByCausesWithBy(Hist h){
    PredictionBy pb = new PredictionBy();
    Map<String,Object> els=new HashMap<String,Object>();
    for( Cause2 cause : list ){
      if( cause.predicts(h) ){
        mergeEquals(els, cause.getPrediction(h));
        pb.by.add(cause);
      }
    }
    for( Iterator<String> i = els.keySet().iterator(); i.hasNext(); ){
      String s = i.next();
      if( els.get(s)==VALUE_DIFF ){
        i.remove();
      }
    }
    if( els.isEmpty() ){
      return null;
    }
    pb.view=els;
    return pb;
  }

  public static class SmacksOfResult{
    public Cause2 cause;
    public DeepState ds;

    public SmacksOfResult(Cause2 cause, DeepState ds) {
      this.cause = cause;
      this.ds = ds;
    }
  }

  public SmacksOfResult smacksOfResult(Hist h){
    for( Cause2 cause : list ){
      if( cause.isPositiveResult() ){
        DeepState ds = cause.intersect(h);
        if( ds!=null ){
          return new SmacksOfResult(cause, ds);
        }
      }
    }
    return null;
  }

  public void add(Cause2 c){
    list.add(c);
  }

}
