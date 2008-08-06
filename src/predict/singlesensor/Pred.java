package predict.singlesensor;

import mem.OneView;
import predict.PredictorIntf;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 11:54:45
 */
public class Pred implements PredictorIntf {
  Map<String, Object> singles = new HashMap<String, Object>();
  final static Object NO_CONST = new Object();

  public void add(OneView v) {
    Map<String, Object> m = v.getViewAll();
    add(m);
  }


  public void appendValsToLastView(Map<String, Object> sensors) {
    add(sensors);
  }

  private void add(Map<String, Object> m) {
    for (String s : m.keySet()) {
      Object obj = singles.get(s);
      if (obj == null) {
        singles.put(s, m.get(s));
      } else if (obj == NO_CONST) {
        // skip
      } else if (!m.get(s).equals(obj)) {
        singles.put(s, NO_CONST);
      }
    }
  }

  public OneView predictNext(OneView v) {
    OneView ret = new OneView();
    for (String s : singles.keySet()) {
      Object obj = singles.get(s);
      if (obj != NO_CONST) {
        ret.pt(s, obj);
      }
    }
    return ret;
  }
}
