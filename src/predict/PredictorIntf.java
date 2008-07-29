package predict;

import mem.OneView;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:51:26
 */
public interface PredictorIntf {
  void add(OneView v);
  OneView predictNext(OneView v);
}
