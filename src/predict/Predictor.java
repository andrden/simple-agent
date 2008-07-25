package predict;

import mem.OneView;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:59:55
 */
public class Predictor implements PredictorIntf {

  public void add(OneView v) {
  }

  public OneView predict() {
    OneView v = new OneView();
    return v;
  }
}
