package predict;

import mem.OneView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import predict.singletarget.PredictionResult;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:59:55
 */
public class Predictor implements PredictorIntf {
  predict.singletarget.Pred alg = new predict.singletarget.Pred();

  public Predictor() {
  }

  public PredictionResult predictNextState(OneView v) {
    return alg.predictNextState(v);
  }

  public void printRules(String elem) {
    alg.printRules(elem);
  }

  public String ruleStats() {
    return alg.ruleStats();
  }

  public void appendValsToLastView(Map<String, Object> sensors) {
    alg.appendValsToLastView(sensors);
  }

  public void add(OneView v) {
    alg.add(v);
  }

  public OneView predictNext(OneView v) {
    OneView vnext = new OneView();
    vnext.prev = v;
    OneView vi = alg.predictNext(v);
    vnext.mergeByAddNew(vi);
    return vnext;
  }

}
