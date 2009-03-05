package predict.singletarget;

import mem.OneView;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Mar 5, 2009
 * Time: 5:37:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PredictionResult {
  OneView pred;
  boolean withRuleConflicts;

  PredictionResult(){
  }

  PredictionResult(String key, Object val){
    if( val==null ){
      return;
    }
    pred = new OneView();
    pred.pt(key, val);
  }

  boolean isNull(){
    return pred==null && !withRuleConflicts;
  }

  public void setWithRuleConflicts(boolean withRuleConflicts) {
    this.withRuleConflicts = withRuleConflicts;
  }

  public boolean isWithRuleConflicts() {
    return withRuleConflicts;
  }

  public Object val(String key){
    if( pred==null ){
      return null;
    }
    return pred.get(key);
  }

  public OneView view(){
    return pred;
  }
  public void merge(PredictionResult part){
    if( pred==null ){
      pred = new OneView();
    }
    withRuleConflicts |= part.withRuleConflicts;
    OneView v = part.view();
    if( v!=null ){
      for( Object k : v.getViewAll().keySet() ){
        pred.pt((String)k, v.get((String)k));
      }
    }
  }
}
