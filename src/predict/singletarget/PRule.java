package predict.singletarget;

import mem.OneView;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import com.pmstation.common.utils.CountingMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 22 жовт 2008
 * Time: 16:53:28
 */
public class PRule extends RuleCond implements java.io.Serializable{
  private CountingMap resultCounts = new CountingMap();
  private CountingMap<Boolean> resultCountsEqPrev = new CountingMap<Boolean>();

  private Object result;
  boolean resultEqPrev=false;

  PRule(){
  }

  PRule(RuleCond c){
    cond = new HashMap<String, Object>(c.cond);
    negCond = new HashMap<String, Object>(c.negCond);
  }

  boolean convergent(){
    return resultCounts.size()==1 || resultCountsEqPrev.getValOr0(Boolean.FALSE)==0;
  }

  Map<Object,Double> normalizedResCounts(OneView vprev, OneViewToVal v2v){
    CountingMap resC = resultCounts;
    if( resultCounts.size()>1 &&
        resultCountsEqPrev.size()==1 && resultCountsEqPrev.get(Boolean.FALSE)==null ){
      resC = new CountingMap();
      resC.increment( v2v.val(vprev), resultCountsEqPrev.get(Boolean.TRUE) );
    }
    double tot = resC.syncTotalCount();
    Map<Object,Double> ret = new HashMap<Object,Double>();
    for( Object v : resC.keySet() ){
      ret.put(v, resC.getValOr0(v)/tot);
    }
    return ret;
  }

  boolean useful(){
    return resultCounts.size()==1 ||
        resultCountsEqPrev.size()==1 && resultCountsEqPrev.get(Boolean.FALSE)==null;
  }

  void recordResult(Object val, OneView vprev, OneViewToVal v2v){
    resultCounts.increment(val);
    resultCountsEqPrev.increment( val.equals(v2v.val(vprev) ) );
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

  Object getPredictedResult(OneView vprev, OneViewToVal v2v){
    if( resultEqPrev ){
      return v2v.val(vprev/*.prev*/);
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

  PRule(Map<String,Object> cond, Map<String,Object> negCond){
    this.cond=cond;
    this.negCond=negCond;
  }

  PRule(String attr, Object val, boolean positive){
    if( positive ){
      cond.put(attr, val);
    }else{
      negCond.put(attr, val);
    }
  }

  PRule negate(){
    PRule n = new PRule();
    n.cond.putAll(negCond);
    n.negCond.putAll(cond);
    n.result=result;
    return n;
  }

  PRule andRule(PRule r){
    PRule n = new PRule();

    n.cond.putAll(cond);
    n.cond.putAll(r.cond);

    n.negCond.putAll(negCond);
    n.negCond.putAll(r.negCond);

    return n;
  }

  boolean condWiderIn(PRule other){
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


  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

  public String toString() {
    String ret = cond + " neg " + negCond + " => ";
//    if( resultEqPrev ){
//      ret += "resultEqPrev";
//    }else{
//      ret += result;
//    }
    ret += resultCounts.mapSortedDesc();
    if( resultCountsEqPrev.size()==1 && resultCountsEqPrev.get(Boolean.FALSE)==null ){
      ret += " eqPrev";
    }
    return ret;
  }

  public String condToString(){
    return super.toString();
  }
}