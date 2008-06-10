package mem;

import java.util.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 21/3/2008
 * Time: 15:49:49
 */
public class Cause implements Serializable {
  DeepState ds;
  Map<String,Object> prediction;
  Map<String,Object> wrongPrediction = new HashMap<String,Object>();

  int countVerified;
  int countWrong;
  List<Cause> explainableBy = new ArrayList<Cause>();
  List<Cause> generifiedFrom=null;

  public boolean valid(){
    return countWrong==0;
  }

  public Map<String, Object> recentCoditionBase(){
    return ds.getElemsAtDepth(0);
  }

  boolean explainableByOther(){
    for( Cause c : explainableBy ){
      if( c.valid() && c.explains(this) /* 'c' could have already lost some of its validity */ ){
        return true;
      }
    }
    return false;
  }

  Map<String,Object> getPrediction(){
    return prediction;
  }

  public void event(Hist hnext){
    Map<String,Object> nextViewAll = hnext.getViewAll();
    for( Iterator<Map.Entry<String,Object>> i = prediction.entrySet().iterator(); i.hasNext(); ){
      Map.Entry<String,Object> e = i.next();
      String key = e.getKey();
      if( key.equals(Hist.NOOP_KEY) ){
        int level = ((Number)e.getValue()).intValue();
        Hist cmp = hnext.getAtDepth(level);
        if( !cmp.viewMatch(nextViewAll, false) ){
          i.remove();
        }
      }else{
        if( !nextViewAll.get(key).equals(e.getValue()) ){
          wrongPrediction.put(key, e.getValue());
          i.remove();
        }
      }
    }
    if( prediction.size()>0 ){
      countVerified++;
    }else{
      countWrong=1;
    }
//    boolean verifiedOrWrong = equalElems nextViewAll.get(Hist.RES_KEY).equals(getResult());
//    if( verifiedOrWrong ){
//      countVerified++;
//    }else{
//      countWrong++;
//    }
  }

  public int getCountVerified() {
    return countVerified;
  }

  public String toString() {
    return ds.toString() + " -> " + prediction
            + ( valid() ? "" : " ?" );
  }

  public Cause(DeepState ds, Map<String,Object> equalElems) {
    this.ds = ds;
    this.prediction = equalElems;
  }

  public Cause(DeepState ds, Map<String,Object> prediction, int countVerified) {
    this.ds = ds;
    this.countVerified = countVerified;
    this.prediction = prediction;
  }

  public boolean explains(Cause other){
    for( String k : other.prediction.keySet() ){
      if( !prediction.containsKey(k) ){
        return false;
      }
      if( !prediction.get(k).equals(other.prediction.get(k)) ){
        return false;
      }
    }

    if( ds.isSubsetOf(other.ds) ){
      return true;
    }
    return false;
  }

  public boolean equals(Cause other){
    if( !explains(other) ){
      return false;
    }
    return ds.equalsDS(other.ds);
  }

  public boolean canPredict(Hist h){
    return ds.match(h);
  }

  public DeepState intersect(Hist h){
    return ds.intersect(h);
  }

  public int getResult() {
    return (Integer) prediction.get(Hist.RES_KEY);
  }

  public boolean hasResult(){
    return prediction.containsKey(Hist.RES_KEY);
  }

  public boolean noop(){
    return prediction.containsKey(Hist.NOOP_KEY);
  }

  private static final long serialVersionUID = -4769956370801528527L;
}
