package predict.singletarget;

import predict.PredictorIntf;
import mem.OneView;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 11:54:45
 */
public class Pred implements PredictorIntf {
  Map<String,SensorHist> singles = new HashMap<String,SensorHist>();

  public void add(OneView v) {
    if( v.prev!=null ){
      Map<String, Object> m = v.getViewAll();
      for( String s : m.keySet() ){
        SensorHist th = singles.get(s);
        if( th==null ){
          th = new SensorHist();
          singles.put(s, th);
        }
        th.add(m.get(s), v.prev);
      }
    }
  }

  public OneView predictNext(OneView v) {
    OneView ret = new OneView();
    for( String t : singles.keySet() ){
      SensorHist th = singles.get(t);
      Object val = th.predict(v);
      if( val!=null ){
        ret.pt(t, val);
      }
    }
    return ret;
  }
}
