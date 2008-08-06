package predict;

import mem.OneView;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:51:26
 */
public interface PredictorIntf {
  void add(OneView v);

  void appendValsToLastView(Map<String, Object> sensors);

  OneView predictNext(OneView v);
}
