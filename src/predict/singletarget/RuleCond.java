package predict.singletarget;

import mem.OneView;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 22 жовт 2008
 * Time: 16:53:28
 */
public class RuleCond implements java.io.Serializable{
  Map<String,Object> cond = new HashMap<String,Object>();
  Map<String,Object> negCond = new HashMap<String,Object>();

  RuleCond(){

  }

  public RuleCond intersect(OneView v){
    RuleCond c = new RuleCond();
    for( String s : cond.keySet() ){
      Object sv = cond.get(s);
      if( sv.equals(v.get(s)) ){
        c.cond.put(s,sv);
      }
    }
    for( String s : negCond.keySet() ){
      Object sv = negCond.get(s);
      if( !sv.equals(v.get(s)) ){
        c.negCond.put(s,sv);
      }
    }
    if( c.cond.isEmpty() && c.negCond.isEmpty() ){
      return null;
    }
    return c;
  }

  public Map<String, Object> getCond() {
    return cond;
  }

  public Map<String, Object> getNegCond() {
    return negCond;
  }

  void disallowConditions(Set<String> skippedViewKeys){
    if( skippedViewKeys==null ){
      return;
    }
    for( String k : cond.keySet() ){
      if( skippedViewKeys.contains(k) ){
        throw new IllegalArgumentException(k);
      }
    }
    for( String k : negCond.keySet() ){
      if( skippedViewKeys.contains(k) ){
        throw new IllegalArgumentException(k);
      }
    }
  }

  void addToCondition(Map<String,Object> inters){
    cond.putAll(inters);
  }

  List<RuleCond> widerConds(){
    List<RuleCond> ret = new ArrayList<RuleCond>();
    for( String c : cond.keySet() ){
      RuleCond w = new RuleCond();
      w.cond = new HashMap<String,Object>(cond);
      w.negCond = new HashMap<String,Object>(negCond);
      w.cond.remove(c);
      ret.add(w);
    }
    for( String c : negCond.keySet() ){
      RuleCond w = new RuleCond();
      w.cond = new HashMap<String,Object>(cond);
      w.negCond = new HashMap<String,Object>(negCond);
      w.negCond.remove(c);
      ret.add(w);
    }
    return ret;
  }


  RuleCond(Map<String,Object> cond){
    this.cond=cond;
  }

  int complexity(){
    return cond.size() + negCond.size();
  }

  RuleCond(String attr, Object val, boolean positive){
    if( positive ){
      cond.put(attr, val);
    }else{
      negCond.put(attr, val);
    }
  }

  RuleCond negate(){
    RuleCond n = new RuleCond();
    n.cond.putAll(negCond);
    n.negCond.putAll(cond);
    return n;
  }

  RuleCond andRule(RuleCond r){
    RuleCond n = new RuleCond();

    n.cond.putAll(cond);
    n.cond.putAll(r.cond);

    n.negCond.putAll(negCond);
    n.negCond.putAll(r.negCond);

    return n;
  }

  boolean condWiderIn(RuleCond other){
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

  public String toString() {
    String ret = cond + " neg " + negCond;
    return ret;
  }


}