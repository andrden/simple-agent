package predict.singletarget;

import mem.OneView;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import weka.core.Instances;
import weka.core.Instance;
import weka.WekaUtils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 16 жовт 2008
 * Time: 13:16:30
 */
public class WekaBuilder {
  static final String RES_ATTR_NAME = "RES";
  static final String ATTR_DUMMY_VALUE = "DUMMY";

  LinkedHashMap<String,LinkedHashSet<String>> attrs = new LinkedHashMap<String, LinkedHashSet<String>>();
  Instances ins;

  void collectAttrs(OneView v, Set<String> skippedViewKeys){
    Map<String, Object> m = v.getViewAll();
    for( String s : m.keySet() ){
      if( skippedViewKeys!=null && skippedViewKeys.contains(s) ){
        continue;
      }
      LinkedHashSet<String> vals = attrs.get(s);
      if( vals==null ){
        vals = new LinkedHashSet<String>();
        attrs.put(s, vals);
      }
      vals.add(m.get(s).toString());
    }
  }

  void mkInstances(LinkedHashSet<String> forRes){
    attrs.put(RES_ATTR_NAME, forRes);

    for( String aname : attrs.keySet() ){
      LinkedHashSet<String> vs = attrs.get(aname);
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
}
