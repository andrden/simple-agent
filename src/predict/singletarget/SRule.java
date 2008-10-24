package predict.singletarget;

import mem.OneView;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 22 жовт 2008
 * Time: 16:53:28
 */
public class SRule {
  private Map<String,Object> cond = new HashMap<String,Object>();
  private Map<String,Object> negCond = new HashMap<String,Object>();
  private Object result;

  SRule(String attr, Object val, boolean positive){
    if( positive ){
      cond.put(attr, val);
    }else{
      negCond.put(attr, val);
    }
  }

  boolean condWiderIn(SRule other){
    for( String s : other.cond.keySet() ){
      if( !other.cond.get(s).equals(cond.get(s)) ){
        return false; // we don't have a condition from 'other'
      }
    }
    for( String s : other.negCond.keySet() ){
      if( !other.negCond.get(s).equals(negCond.get(s)) ){
        return false; // we don't have a condition from 'other'
      }
    }
    return true; // all conditions from 'other' are present in 'this'
  }

  boolean condHolds(OneView v){
    for( String s : cond.keySet() ){
      if( !cond.get(s).equals(v.get(s)) ){
        return false;
      }
    }
    for( String s : negCond.keySet() ){
      if( negCond.get(s).equals(v.get(s)) ){
        return false;
      }
    }
    return true;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

  public String toString() {
    return cond + " neg " + negCond + " => " + result;
  }
}
