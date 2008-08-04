package predict.singletarget;

import mem.OneView;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 4/8/2008
 * Time: 17:37:58
 */
public class SensorHist {
    Map<Object,TargetHist> vals = new HashMap<Object,TargetHist>();
    void add(Object val, OneView v){
        TargetHist th = vals.get(val);
        if( th==null ){
            th = new TargetHist();
            vals.put(val, th);
        }
        th.addExample(v.prev);
    }

    Object predict(OneView v){
        for( Object val : vals.keySet() ){
          TargetHist th = vals.get(val);
          if( th.ruleHolds(v) ){
            return val;
          }
        }
        return null;
    }
}
