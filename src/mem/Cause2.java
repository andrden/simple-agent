package mem;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 11/7/2008
 * Time: 10:25:10
 */
public class Cause2 {
  String key;
  Object val;

  DeepState cond;

  List<Hist> examples = new ArrayList<Hist>();

  public Cause2(String key, Object val) {
    this.key = key;
    this.val = val;
  }

  public void addExample(Hist h){
    examples.add(h);
  }

  public boolean predicts(Hist h){
    return cond.match(h);
  }

  Map<String,Object> getPrediction(Hist h){
    if( key.equals(Hist.NOOP_KEY) ){
      int level = ((Number)val).intValue();
      Hist cmp = h.getAtDepth(level-1);
      return cmp.getViewAllWithoutCmd();
    }

    return Collections.singletonMap(key, val);
  }

  public boolean isPositiveResult(){
    return key.equals(Hist.RES_KEY) && ((Number)val).intValue()>0;
  }

  public DeepState intersect(Hist h){
    return cond.intersect(h);
  }

  public Map<String, Object> recentCoditionBase(){
    return cond.getElemsAtDepth(0);
  }


}
