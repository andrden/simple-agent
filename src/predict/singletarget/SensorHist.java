package predict.singletarget;

import mem.OneView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 4/8/2008
 * Time: 17:37:58
 */
public class SensorHist {
  final String sensorName;
  Map<Object, TargetHist> vals = new HashMap<Object, TargetHist>();

  public SensorHist(String sensorName) {
    this.sensorName = sensorName;
  }

  void add(Object val, OneView v) {
    TargetHist th = vals.get(val);
    if (th == null) {
      th = new TargetHist(this, val);
      vals.put(val, th);
    }
    th.addExample(v.prev);
  }

  Object predict(OneView v) {
    Object retVal = null;
    long hitCount=0;
    for (Object val : vals.keySet()) {
      TargetHist th = vals.get(val);
      if (th.reasonablyAccepted(v)) {
        retVal = val;
        hitCount++;
      }
    }
    if( hitCount>1 ){
      return null; //conflict
    }
    return retVal;
  }
}
