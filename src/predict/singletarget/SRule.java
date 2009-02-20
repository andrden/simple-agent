package predict.singletarget;

import mem.OneView;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 22 жовт 2008
 * Time: 16:53:28
 */
public class SRule implements java.io.Serializable{
  private Map<String,Object> cond = new HashMap<String,Object>();
  private Map<String,Object> negCond = new HashMap<String,Object>();
  private Object result;
  boolean resultEqPrev=false;

  SRule(){

  }

  boolean resultUseful(Object resultValue){
    if( resultValue==null ){
      return false;
    }
    if( resultEqPrev ){
      return resultValue.equals("1");
    }
    return true;
  }

  Object getPredictedResult(OneView vprev, Map<OneView, Object> exampleVals){
    if( resultEqPrev ){
      return exampleVals.get(vprev.prev);
    }
    return getResult();
  }

  Object resultValue(OneView v, Map<OneView, Object> exampleVals){
    if( resultEqPrev ){
      String vval = "0";
      if( v.prev!=null && exampleVals.get(v.prev)!=null){
        if( exampleVals.get(v.prev).equals(exampleVals.get(v)) ){
          vval = "1";
        }
      }
      return vval;
    }else{
      return exampleVals.get(v);
    }
  }

  SRule(Map<String,Object> cond){
    this.cond=cond;
  }

  int complexity(){
    return cond.size() + negCond.size();
  }

  SRule(String attr, Object val, boolean positive){
    if( positive ){
      cond.put(attr, val);
    }else{
      negCond.put(attr, val);
    }
  }

  SRule negate(){
    SRule n = new SRule();
    n.cond.putAll(negCond);
    n.negCond.putAll(cond);
    n.result=result;
    return n;
  }

  SRule andRule(SRule r){
    SRule n = new SRule();

    n.cond.putAll(cond);
    n.cond.putAll(r.cond);

    n.negCond.putAll(negCond);
    n.negCond.putAll(r.negCond);

    return n;
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
    String ret = cond + " neg " + negCond + " => ";
    if( resultEqPrev ){
      ret += "resultEqPrev";
    }else{
      ret += result;
    }
    return ret;
  }
}
