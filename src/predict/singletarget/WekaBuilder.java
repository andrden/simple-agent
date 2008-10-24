package predict.singletarget;

import mem.OneView;

import java.util.*;

import weka.core.Instances;
import weka.core.Instance;
import weka.WekaUtils;
import weka.classifiers.Classifier;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 16 жовт 2008
 * Time: 13:16:30
 */
public class WekaBuilder {
  static final String RES_ATTR_NAME = "RES";
  static final String ATTR_DUMMY_VALUE = "DUMMY";

  final Classifier clsf;
  LinkedHashMap<String,LinkedHashSet<Object>> attrs = new LinkedHashMap<String, LinkedHashSet<Object>>();
  Instances ins;

  LinkedHashSet<Object> forRes = new LinkedHashSet<Object>();
  List forResObj = new ArrayList();


  public WekaBuilder(Classifier clsf) {
    this.clsf = clsf;
  }

  void collectAttrs(OneView v, Set<String> skippedViewKeys){
    Map<String, Object> m = v.getViewAll();
    for( String s : m.keySet() ){
      if( skippedViewKeys!=null && skippedViewKeys.contains(s) ){
        continue;
      }
      LinkedHashSet<Object> vals = attrs.get(s);
      if( vals==null ){
        vals = new LinkedHashSet<Object>();
        attrs.put(s, vals);
      }
      vals.add(m.get(s));
    }
  }

  void addForRes(Object o){
    forResObj.add(o);
    forRes.add(o.toString());
  }

  void mkInstances(){
    attrs.put(RES_ATTR_NAME, forRes);

    for( String aname : attrs.keySet() ){
      LinkedHashSet<Object> vs = attrs.get(aname);
      if( vs.size()==1 ){ // J48 - Cannot handle unary class
        vs.add(ATTR_DUMMY_VALUE);
      }
    }

    ins = new WekaUtils().makeInstances(attrs);
  }

  void addInstance(OneView v, String clazz){
    Instance wi = mkInstance(v, clazz);
    ins.add(wi);
  }

  Instance mkInstance(OneView v){
    return mkInstance(v, null);
  }

  private Instance mkInstance(OneView v, String clazz) {
    Instance wi = new Instance(ins.numAttributes());
    wi.setDataset(ins);
    int i=0;
    for( String n : attrs.keySet() ){
      if( n.equals(RES_ATTR_NAME) ){
        if( clazz!=null ){
          wi.setValue(i, clazz);
        }
      }else{
        Object atValObj = v.get(n);
        if( atValObj!=null ){
          String atVal = atValObj.toString();
          if( attrs.get(n).contains(atVal) ){
            wi.setValue(i, atVal);
          }
        }
      }
      i++;
    }
    return wi;
  }

  public Instances getInstances() {
    return ins;
  }

  public Classifier getClassifier() {
    return clsf;
  }

  public Object getForResObj(int idx){
    return forResObj.get(idx);
  }

  public Object attVal(String attName, int idx){
    int i=0;
    for( Object v : attrs.get(attName) ){
      if( i==idx ){
        return v;
      }
      i++;
    }
    throw new NoSuchElementException(attName+" "+idx);
  }
}
