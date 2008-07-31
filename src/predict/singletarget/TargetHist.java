package predict.singletarget;

import mem.OneView;
import mem.ViewDepthElem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 12:09:00
 */
public class TargetHist {
  final int DEEP_STATE_DEPTH=4;

  List<OneView> examples = new ArrayList<OneView>();
  Map<ViewDepthElem,Object> rule=null;

  void addExample(OneView v){
    if( examples.size()>0 ){
      Map<ViewDepthElem,Object> m = deepState(v);
      for( OneView vi : examples ){
        Map<ViewDepthElem, Object> cmp = deepState(vi);
        retainEquals(m, cmp);
      }
      if( !m.isEmpty() ){
        rule = m;
      }
    }
    examples.add(v);
  }

  boolean ruleHolds(OneView v){
    if( rule==null ){
      return false;
    }
    Map<ViewDepthElem, Object> cmp = deepState(v);
    Map<ViewDepthElem,Object> ruleCopy = new HashMap<ViewDepthElem,Object>(rule);
    retainEquals(ruleCopy, cmp);
    return ruleCopy.size()==rule.size();
  }

  void retainEquals(Map<ViewDepthElem,Object> where, Map<ViewDepthElem,Object> cmp){
    for( Iterator<ViewDepthElem> i = where.keySet().iterator(); i.hasNext();  ){
      ViewDepthElem e = i.next();
      if( !cmp.containsKey(e) || !where.get(e).equals(cmp.get(e)) ){
        i.remove();
      }
    }
  }

  Map<ViewDepthElem,Object> deepState(OneView v){
    Map<ViewDepthElem,Object> ret = new HashMap<ViewDepthElem,Object>();
    for( int i=0; v!=null && i<DEEP_STATE_DEPTH; i++ ){
      Map<String, Object> all = v.getViewAll();
      for( String k : all.keySet() ){
        ret.put(new ViewDepthElem(i, k), all.get(k));
      }
      v = v.prev;
    }
    return ret;
  }

}
